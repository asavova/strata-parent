package com.strata.ai;

import com.strata.core.Player;
import com.strata.engine.GameEngine;
import com.strata.engine.GameState;
import com.strata.model.GameResultStatus;
import com.strata.model.MoveRecord;
import com.strata.rules.MoveGenerator;
import com.strata.rules.WinConditionChecker;

import java.util.List;

/**
 * Baseline minimax search.
 */
public class MinimaxSearch {
    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final EvaluationFunction evaluationFunction = new EvaluationFunction();
    private final WinConditionChecker resultChecker = new WinConditionChecker();
    private final MoveOrdering moveOrdering = new MoveOrdering();

    public MoveRecord findBestMove(GameState state, int depth) {
        Player root = state.getActivePlayer();
        List<MoveRecord> moves = moveOrdering.orderMoves(
                state,
                moveGenerator.generateLegalMoves(state, root),
                root
        );

        MoveRecord bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (MoveRecord move : moves) {
            GameState copy = state.clone();
            GameEngine engine = new GameEngine(copy);
            engine.applyMove(move);
            engine.switchPlayer();

            int score = minimax(copy, depth - 1, false, root);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int minimax(GameState state, int depth, boolean maximizing, Player rootPlayer) {
        GameResultStatus result = resultChecker.evaluate(state);
        if (depth == 0 || result.isTerminal()) {
            return evaluationFunction.evaluate(state, rootPlayer);
        }

        Player current = state.getActivePlayer();
        List<MoveRecord> moves = moveOrdering.orderMoves(
                state,
                moveGenerator.generateLegalMoves(state, current),
                current
        );

        if (moves.isEmpty()) {
            return evaluationFunction.evaluate(state, rootPlayer);
        }

        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (MoveRecord move : moves) {
            GameState copy = state.clone();
            GameEngine engine = new GameEngine(copy);
            engine.applyMove(move);
            engine.switchPlayer();

            int score = minimax(copy, depth - 1, !maximizing, rootPlayer);

            if (maximizing) {
                best = Math.max(best, score);
            } else {
                best = Math.min(best, score);
            }
        }

        return best;
    }
}
