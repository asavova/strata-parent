package com.strata.board;

import com.strata.core.GameConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometry helper for coordinate conversion and neighborhood logic.
 */
public final class BoardGeometry {
    private BoardGeometry() {
    }

    public static int toNodeId(int layer, int row, int column) {
        if (!isValid(layer, row, column)) {
            throw new IllegalArgumentException(
                    "Invalid coordinates: (" + layer + "," + row + "," + column + ")"
            );
        }
        return layer * GameConstants.NODES_PER_LAYER + row * GameConstants.COLUMNS + column;
    }

    public static int[] fromNodeId(int nodeId) {
        validateNode(nodeId);
        int layer = nodeId / GameConstants.NODES_PER_LAYER;
        int offset = nodeId % GameConstants.NODES_PER_LAYER;
        int row = offset / GameConstants.COLUMNS;
        int column = offset % GameConstants.COLUMNS;
        return new int[]{layer, row, column};
    }

    public static boolean isValid(int layer, int row, int column) {
        return layer >= 0 && layer < GameConstants.LAYERS
                && row >= 0 && row < GameConstants.ROWS
                && column >= 0 && column < GameConstants.COLUMNS;
    }

    public static boolean isValidNode(int nodeId) {
        return nodeId >= 0 && nodeId < GameConstants.TOTAL_NODES;
    }

    public static List<Integer> neighborsOf(int nodeId) {
        validateNode(nodeId);
        int[] c = fromNodeId(nodeId);
        List<Integer> result = new ArrayList<>();

        for (int dl = -1; dl <= 1; dl++) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dl == 0 && dr == 0 && dc == 0) {
                        continue;
                    }
                    int nl = c[0] + dl;
                    int nr = c[1] + dr;
                    int nc = c[2] + dc;
                    if (isValid(nl, nr, nc)) {
                        result.add(toNodeId(nl, nr, nc));
                    }
                }
            }
        }
        return result;
    }

    public static String format(int nodeId) {
        int[] c = fromNodeId(nodeId);
        return "(" + c[0] + "," + c[1] + "," + c[2] + ")";
    }

    private static void validateNode(int nodeId) {
        if (!isValidNode(nodeId)) {
            throw new IllegalArgumentException("Invalid nodeId: " + nodeId);
        }
    }
}
