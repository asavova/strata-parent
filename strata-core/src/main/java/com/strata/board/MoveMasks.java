package com.strata.board;

import com.strata.core.GameConstants;

/**
 * Precomputed one-step masks for all nodes.
 */
public final class MoveMasks {
    public static final BitBoard[] adjacentMask = new BitBoard[GameConstants.TOTAL_NODES];
    public static final BitBoard[] diagonalMask = new BitBoard[GameConstants.TOTAL_NODES];
    public static final BitBoard[] orthogonalMask = new BitBoard[GameConstants.TOTAL_NODES];
    public static final BitBoard[] sameLayerAdjacentMask = new BitBoard[GameConstants.TOTAL_NODES];
    public static final BitBoard centerMask = new BitBoard();

    static {
        initializeMasks();
        initializeCenterMask();
    }

    private MoveMasks() {
    }

    private static void initializeMasks() {
        for (int node = 0; node < GameConstants.TOTAL_NODES; node++) {
            adjacentMask[node] = new BitBoard();
            diagonalMask[node] = new BitBoard();
            orthogonalMask[node] = new BitBoard();
            sameLayerAdjacentMask[node] = new BitBoard();

            int[] c = BoardGeometry.fromNodeId(node);
            int layer = c[0];
            int row = c[1];
            int col = c[2];

            for (int dl = -1; dl <= 1; dl++) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dl == 0 && dr == 0 && dc == 0) {
                            continue;
                        }

                        int nl = layer + dl;
                        int nr = row + dr;
                        int nc = col + dc;

                        if (BoardGeometry.isValid(nl, nr, nc)) {
                            adjacentMask[node].set(BoardGeometry.toNodeId(nl, nr, nc));
                        }

                        if (dl == 0 && BoardGeometry.isValid(layer, nr, nc)) {
                            sameLayerAdjacentMask[node].set(BoardGeometry.toNodeId(layer, nr, nc));
                        }
                    }
                }
            }

            int[][] diagonals = {
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            };
            for (int dl = -1; dl <= 1; dl++) {
                for (int[] d : diagonals) {
                    int nl = layer + dl;
                    int nr = row + d[0];
                    int nc = col + d[1];
                    if (BoardGeometry.isValid(nl, nr, nc)) {
                        diagonalMask[node].set(BoardGeometry.toNodeId(nl, nr, nc));
                    }
                }
            }

            int[][] orthogonals = {
                    {0, 1, 0},
                    {0, -1, 0},
                    {0, 0, 1},
                    {0, 0, -1},
                    {1, 0, 0},
                    {-1, 0, 0}
            };
            for (int[] d : orthogonals) {
                int nl = layer + d[0];
                int nr = row + d[1];
                int nc = col + d[2];
                if (BoardGeometry.isValid(nl, nr, nc)) {
                    orthogonalMask[node].set(BoardGeometry.toNodeId(nl, nr, nc));
                }
            }
        }
    }

    private static void initializeCenterMask() {
        int[][] coords = {
                {3, 0, 2}, {3, 0, 3},
                {3, 1, 1}, {3, 1, 2}, {3, 1, 3}, {3, 1, 4},
                {3, 2, 2}, {3, 2, 3}
        };

        for (int[] coord : coords) {
            centerMask.set(BoardGeometry.toNodeId(coord[0], coord[1], coord[2]));
        }
    }
}
