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
 * Alpha-beta search with move ordering.
 */
public class AlphaBetaPruning {
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
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (MoveRecord move : moves) {
            GameState copy = state.clone();
            GameEngine engine = new GameEngine(copy);
            engine.applyMove(move);
            engine.switchPlayer();

            int score = search(copy, depth - 1, alpha, beta, false, root);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }

        return bestMove;
    }

    private int search(
            GameState state,
            int depth,
            int alpha,
            int beta,
            boolean maximizing,
            Player rootPlayer
    ) {
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

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (MoveRecord move : moves) {
                GameState copy = state.clone();
                GameEngine engine = new GameEngine(copy);
                engine.applyMove(move);
                engine.switchPlayer();

                value = Math.max(value, search(copy, depth - 1, alpha, beta, false, rootPlayer));
                alpha = Math.max(alpha, value);

                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (MoveRecord move : moves) {
                GameState copy = state.clone();
                GameEngine engine = new GameEngine(copy);
                engine.applyMove(move);
                engine.switchPlayer();

                value = Math.min(value, search(copy, depth - 1, alpha, beta, true, rootPlayer));
                beta = Math.min(beta, value);

                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }
    }
}
