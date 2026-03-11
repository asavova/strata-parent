package com.strata.rules;

import com.strata.board.BoardGeometry;
import com.strata.board.MoveMasks;
import com.strata.core.GameConstants;
import com.strata.core.Player;
import com.strata.engine.GameEngine;
import com.strata.engine.GameState;
import com.strata.model.MoveRecord;
import com.strata.model.PieceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates legal moves for the current board state.
 *
 * Stronger rules in this iteration:
 * - ADEPT moves only on the current layer
 * - ELEMENTAL slides diagonally, including across layers
 * - GUARDIAN slides orthogonally, including vertical movement
 * - no friendly landing
 * - sliders stop at blockers
 * - only active side pieces are generated
 */
public class MoveGenerator {
    private static final int[][] ELEMENTAL_DIRECTIONS = {
            {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1},
            {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1},
            {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1}
    };

    private static final int[][] GUARDIAN_DIRECTIONS = {
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1},
            {1, 0, 0}, {-1, 0, 0}
    };

    public List<MoveRecord> generateLegalMoves(GameState state) {
        return generateLegalMoves(state, state.getActivePlayer());
    }

    public List<MoveRecord> generateLegalMoves(GameState state, Player player) {
        List<MoveRecord> pseudoLegal = new ArrayList<>();

        for (int node = 0; node < GameConstants.TOTAL_NODES; node++) {
            if (!belongsToPlayer(state, node, player)) {
                continue;
            }

            PieceType type = state.getPieceType(node);
            if (type == null) {
                continue;
            }

            switch (type) {
                case ADEPT -> generateAdeptMoves(state, player, node, pseudoLegal);
                case ELEMENTAL -> generateSlidingMoves(state, player, node, ELEMENTAL_DIRECTIONS, pseudoLegal);
                case GUARDIAN -> generateSlidingMoves(state, player, node, GUARDIAN_DIRECTIONS, pseudoLegal);
            }
        }

        return filterLegalMoves(state, player, pseudoLegal);
    }

    private void generateAdeptMoves(GameState state, Player player, int fromNode, List<MoveRecord> moves) {
        for (int toNode = 0; toNode < GameConstants.TOTAL_NODES; toNode++) {
            if (!MoveMasks.sameLayerAdjacentMask[fromNode].isSet(toNode)) {
                continue;
            }
            if (isFriendly(state, player, toNode)) {
                continue;
            }
            moves.add(buildMove(state, player, fromNode, toNode));
        }
    }

    private void generateSlidingMoves(
            GameState state,
            Player player,
            int fromNode,
            int[][] directions,
            List<MoveRecord> moves
    ) {
        int[] origin = BoardGeometry.fromNodeId(fromNode);

        for (int[] d : directions) {
            int l = origin[0] + d[0];
            int r = origin[1] + d[1];
            int c = origin[2] + d[2];

            while (BoardGeometry.isValid(l, r, c)) {
                int toNode = BoardGeometry.toNodeId(l, r, c);

                if (isFriendly(state, player, toNode)) {
                    break;
                }

                moves.add(buildMove(state, player, fromNode, toNode));

                if (state.isOccupied(toNode)) {
                    break;
                }

                l += d[0];
                r += d[1];
                c += d[2];
            }
        }
    }

    private List<MoveRecord> filterLegalMoves(GameState state, Player player, List<MoveRecord> candidates) {
        List<MoveRecord> legal = new ArrayList<>();
        WinConditionChecker checker = new WinConditionChecker();

        for (MoveRecord move : candidates) {
            GameState copy = state.clone();
            GameEngine engine = new GameEngine(copy);
            engine.applyMove(move);

            if (!checker.hasLockedWin(copy, player.opposite())) {
                legal.add(move);
            }
        }

        return legal;
    }

    private MoveRecord buildMove(GameState state, Player player, int fromNode, int toNode) {
        PieceType capturedPiece = state.getPieceType(toNode);
        Player capturedOwner = state.getOwner(toNode);
        byte capturedStage = capturedPiece == null ? 0 : state.getStage(toNode);

        return new MoveRecord(
                fromNode,
                toNode,
                state.getPieceType(fromNode),
                player,
                capturedPiece,
                capturedOwner,
                state.getStage(fromNode),
                capturedStage
        );
    }

    private boolean belongsToPlayer(GameState state, int nodeId, Player player) {
        return player == Player.WHITE ? state.isWhite(nodeId) : state.isBlack(nodeId);
    }

    private boolean isFriendly(GameState state, Player player, int nodeId) {
        return belongsToPlayer(state, nodeId, player);
    }
}
