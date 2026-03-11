package com.strata.ml;

import com.strata.ai.EvaluationFunction;
import com.strata.ai.MoveOrdering;
import com.strata.core.Player;
import com.strata.engine.GameEngine;
import com.strata.engine.GameState;
import com.strata.model.GameResult;
import com.strata.model.Move;
import com.strata.rules.MoveGenerator;
import com.strata.rules.WinConditionChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Drives self-play games to generate training data.
 *
 * Architecture:
 * 1. Java plays games using the current policy (neural net or classical)
 * 2. Each position is encoded and stored with the move played
 * 3. After the game ends, value targets are backfilled with the outcome
 * 4. Training examples are sent to Python for neural network training
 *
 * Initially uses classical evaluation + noise for exploration.
 * Once a neural network is trained, switches to neural MCTS.
 */
public class SelfPlayDriver {
    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final WinConditionChecker winChecker = new WinConditionChecker();
    private final EvaluationFunction evalFunction = new EvaluationFunction();
    private final MoveOrdering moveOrdering = new MoveOrdering();
    private final Random random = new Random();

    private static final int MAX_GAME_LENGTH = 300;
    private static final double EXPLORATION_RATE = 0.25;

    /**
     * Play one complete self-play game and return training examples.
     */
    public List<TrainingExample> playSelfPlayGame() {
        GameState state = new GameState();
        GameEngine engine = new GameEngine(state);
        List<PositionRecord> positions = new ArrayList<>();

        for (int ply = 0; ply < MAX_GAME_LENGTH; ply++) {
            GameResult result = winChecker.evaluate(state);
            if (result.isTerminal()) {
                break;
            }

            List<Move> legalMoves = moveGenerator.generateLegalMoves(state);
            if (legalMoves.isEmpty()) {
                break;
            }

            // Encode current position
            float[] encodedState = StateEncoder.encode(state);
            Player perspective = state.getActivePlayer();

            // Select move with exploration
            MoveSelection selection = selectMoveWithExploration(state, legalMoves);

            // Record position
            positions.add(new PositionRecord(encodedState, selection.policy(), perspective));

            // Apply move
            engine.applyMove(selection.move());
            engine.switchPlayer();
        }

        // Determine game outcome
        float whiteOutcome = determineOutcome(state);

        // Backfill value targets
        List<TrainingExample> examples = new ArrayList<>();
        for (PositionRecord record : positions) {
            float value = record.perspective() == Player.WHITE ? whiteOutcome : -whiteOutcome;
            examples.add(new TrainingExample(record.state(), record.policy(), value));
        }

        return examples;
    }

    /**
     * Run multiple self-play games.
     */
    public List<TrainingExample> runSelfPlayBatch(int numGames) {
        List<TrainingExample> allExamples = new ArrayList<>();
        for (int game = 0; game < numGames; game++) {
            List<TrainingExample> gameExamples = playSelfPlayGame();
            allExamples.addAll(gameExamples);

            if ((game + 1) % 10 == 0) {
                System.out.printf("[SelfPlay] Completed %d/%d games, %d examples so far%n",
                        game + 1, numGames, allExamples.size());
            }
        }
        return allExamples;
    }

    private MoveSelection selectMoveWithExploration(GameState state, List<Move> legalMoves) {
        List<Move> ordered = moveOrdering.orderMoves(state, legalMoves, state.getActivePlayer());

        // Compute scores for each move
        int[] visitCounts = new int[ordered.size()];
        int totalVisits = 0;

        for (int i = 0; i < ordered.size(); i++) {
            GameState copy = state.clone();
            GameEngine tempEngine = new GameEngine(copy);
            tempEngine.applyMove(ordered.get(i));

            int score = evalFunction.evaluate(copy, state.getActivePlayer());
            // Convert score to pseudo visit count (higher score = more visits)
            int visits = Math.max(1, score + 5000);
            visitCounts[i] = visits;
            totalVisits += visits;
        }

        // Add exploration noise (Dirichlet-like)
        Move selectedMove;
        if (random.nextDouble() < EXPLORATION_RATE) {
            selectedMove = ordered.get(random.nextInt(ordered.size()));
        } else {
            // Temperature-weighted selection
            double r = random.nextDouble() * totalVisits;
            double cumulative = 0;
            selectedMove = ordered.getFirst();
            for (int i = 0; i < ordered.size(); i++) {
                cumulative += visitCounts[i];
                if (cumulative >= r) {
                    selectedMove = ordered.get(i);
                    break;
                }
            }
        }

        float[] policy = MoveEncoder.visitCountsToPolicy(visitCounts, ordered);
        return new MoveSelection(selectedMove, policy);
    }

    private float determineOutcome(GameState state) {
        GameResult result = winChecker.evaluate(state);
        if (result.winner() == Player.WHITE) {
            return 1.0f;
        }
        if (result.winner() == Player.BLACK) {
            return -1.0f;
        }
        return 0.0f;
    }

    private record PositionRecord(float[] state, float[] policy, Player perspective) {
    }

    private record MoveSelection(Move move, float[] policy) {
    }
}
