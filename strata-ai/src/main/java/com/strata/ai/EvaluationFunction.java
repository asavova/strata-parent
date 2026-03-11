package com.strata.ai;

import com.strata.board.MoveMasks;
import com.strata.core.GameConstants;
import com.strata.core.Player;
import com.strata.engine.GameState;
import com.strata.model.TransformationStage;
import com.strata.rules.MoveGenerator;

/**
 * Static evaluator for AI search.
 */
public class EvaluationFunction {
    private final MoveGenerator moveGenerator = new MoveGenerator();

    public int evaluate(GameState state, Player perspective) {
        int white = evaluateSide(state, Player.WHITE);
        int black = evaluateSide(state, Player.BLACK);
        int score = white - black;
        return perspective == Player.WHITE ? score : -score;
    }

    private int evaluateSide(GameState state, Player player) {
        int score = 0;

        score += state.countPieces(player) * 100;
        score += countCenterControl(state, player) * 35;
        score += countKeyNodes(state, player) * 90;
        score += mobility(state, player) * 3;
        score += stageScore(state, player);

        return score;
    }

    private int countCenterControl(GameState state, Player player) {
        int count = 0;
        for (int node : MoveMasks.centerMask.toNodeList()) {
            if ((player == Player.WHITE && state.isWhite(node))
                    || (player == Player.BLACK && state.isBlack(node))) {
                count++;
            }
        }
        return count;
    }

    private int countKeyNodes(GameState state, Player player) {
        int count = 0;
        for (int node : GameConstants.KEY_NODES) {
            if ((player == Player.WHITE && state.isWhite(node))
                    || (player == Player.BLACK && state.isBlack(node))) {
                count++;
            }
        }
        return count;
    }

    private int mobility(GameState state, Player player) {
        return moveGenerator.generateLegalMoves(state, player).size();
    }

    private int stageScore(GameState state, Player player) {
        int score = 0;
        for (int node : state.getPieces(player)) {
            byte stage = state.getStage(node);
            score += stage * 7;
            if (stage == TransformationStage.PHILOSOPHERS_STONE.ordinal()) {
                score += 45;
            }
        }
        return score;
    }
}
