package com.strata.ai;

import com.strata.board.BoardGeometry;
import com.strata.core.GameConstants;
import com.strata.engine.GameState;
import com.strata.model.PieceType;

/**
 * Console rendering helper for quick debugging.
 */
public final class BoardPrinter {
    private BoardPrinter() {
    }

    public static String print(GameState state) {
        StringBuilder sb = new StringBuilder();

        for (int layer = 0; layer < GameConstants.LAYERS; layer++) {
            sb.append("Layer ").append(layer).append(":\n");
            for (int row = 0; row < GameConstants.ROWS; row++) {
                for (int col = 0; col < GameConstants.COLUMNS; col++) {
                    int node = BoardGeometry.toNodeId(layer, row, col);
                    if (!state.isOccupied(node)) {
                        sb.append(".  ");
                        continue;
                    }

                    char owner = state.isWhite(node) ? 'W' : 'B';
                    PieceType type = state.getPieceType(node);
                    char archetype = switch (type) {
                        case ADEPT -> 'A';
                        case ELEMENTAL -> 'E';
                        case GUARDIAN -> 'G';
                    };

                    sb.append(owner).append(archetype).append(' ');
                }
                sb.append('\n');
            }
            sb.append('\n');
        }

        return sb.toString();
    }
}
