package com.strata.core;

import com.strata.board.BoardGeometry;

/**
 * Shared immutable constants for the engine.
 */
public final class GameConstants {
    public static final int LAYERS = 7;
    public static final int ROWS = 3;
    public static final int COLUMNS = 6;
    public static final int NODES_PER_LAYER = ROWS * COLUMNS;
    public static final int TOTAL_NODES = LAYERS * NODES_PER_LAYER;

    public static final int TRANSFORMATION_STAGE_COUNT = 9;

    /**
     * Primary victory key nodes on the central layer.
     */
    public static final int[] KEY_NODES = {
            BoardGeometry.toNodeId(3, 1, 1),
            BoardGeometry.toNodeId(3, 1, 2),
            BoardGeometry.toNodeId(3, 1, 3)
    };

    private GameConstants() {
    }
}
