package com.strata.ai;

import com.strata.board.MoveMasks;
import com.strata.core.GameConstants;
import com.strata.core.Player;
import com.strata.engine.GameState;
import com.strata.model.MoveRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orders moves for better alpha-beta pruning efficiency.
 */
public class MoveOrdering {

    public List<MoveRecord> orderMoves(GameState state, List<MoveRecord> moves, Player player) {
        List<ScoredMove> scored = new ArrayList<>();
        for (MoveRecord move : moves) {
            scored.add(new ScoredMove(move, scoreMove(state, move, player)));
        }

        scored.sort(Comparator.comparingInt(ScoredMove::score).reversed());

        List<MoveRecord> ordered = new ArrayList<>(moves.size());
        for (ScoredMove sm : scored) {
            ordered.add(sm.move());
        }
        return ordered;
    }

    private int scoreMove(GameState state, MoveRecord move, Player player) {
        int score = 0;

        if (move.isCapture()) {
            score += 1000;
            score += switch (move.getCapturedPiece()) {
                case ADEPT -> 100;
                case ELEMENTAL -> 160;
                case GUARDIAN -> 180;
            };
        }

        int to = move.getToNode();
        int from = move.getFromNode();

        if (MoveMasks.centerMask.isSet(to)) {
            score += 120;
        }

        for (int keyNode : GameConstants.KEY_NODES) {
            if (to == keyNode) {
                score += 200;
            }
        }

        if (state.getStage(from) == 7) {
            score += 80;
        } else if (state.getStage(from) == 8) {
            score += 120;
        }

        if (player == Player.WHITE && state.isBlack(to)) {
            score += 40;
        }
        if (player == Player.BLACK && state.isWhite(to)) {
            score += 40;
        }

        return score;
    }

    private record ScoredMove(MoveRecord move, int score) {
    }
}
