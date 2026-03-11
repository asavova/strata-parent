package com.strata.util;

import com.strata.board.BoardGeometry;
import com.strata.model.MoveRecord;

/**
 * Readable formatting helpers.
 */
public final class NodeFormatter {
    private NodeFormatter() {
    }

    public static String formatNode(int nodeId) {
        return nodeId + " " + BoardGeometry.format(nodeId);
    }

    public static String formatMove(MoveRecord move) {
        return formatNode(move.getFromNode()) + " -> " + formatNode(move.getToNode());
    }
}
