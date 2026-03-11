package com.strata.ml;

import com.strata.board.BoardGeometry;
import com.strata.core.GameConstants;
import com.strata.core.Player;
import com.strata.engine.GameState;
import com.strata.model.PieceType;

/**
 * Encodes the game state into a flat float array suitable for neural network input.
 *
 * Encoding layout (per node, 126 nodes):
 *   Channel 0: is white piece present (0 or 1)
 *   Channel 1: is black piece present (0 or 1)
 *   Channel 2: is ADEPT (0 or 1)
 *   Channel 3: is ELEMENTAL (0 or 1)
 *   Channel 4: is GUARDIAN (0 or 1)
 *   Channel 5: transformation stage normalized (0.0 .. 1.0)
 *   Channel 6: is active player's piece (0 or 1)
 *   Channel 7: layer normalized (0.0 .. 1.0)
 *
 * Total input size: 126 * 8 = 1008 floats
 *
 * Alternative: 3D tensor [7 layers x 3 rows x 6 cols x 8 channels]
 */
public class StateEncoder {
    public static final int CHANNELS = 8;
    public static final int FLAT_SIZE = GameConstants.TOTAL_NODES * CHANNELS;

    /**
     * Returns a flat float array for neural network consumption.
     */
    public static float[] encode(GameState state) {
        float[] encoded = new float[FLAT_SIZE];
        Player active = state.getActivePlayer();

        for (int node = 0; node < GameConstants.TOTAL_NODES; node++) {
            int base = node * CHANNELS;
            int[] coords = BoardGeometry.fromNodeId(node);

            if (state.isWhite(node)) {
                encoded[base] = 1.0f;
            }
            if (state.isBlack(node)) {
                encoded[base + 1] = 1.0f;
            }

            PieceType type = state.getPieceType(node);
            if (type != null) {
                switch (type) {
                    case ADEPT -> encoded[base + 2] = 1.0f;
                    case ELEMENTAL -> encoded[base + 3] = 1.0f;
                    case GUARDIAN -> encoded[base + 4] = 1.0f;
                }
            }

            encoded[base + 5] = state.getStage(node) / 8.0f;

            Player owner = state.getOwner(node);
            if (owner != null && owner == active) {
                encoded[base + 6] = 1.0f;
            }

            encoded[base + 7] = coords[0] / 6.0f;
        }

        return encoded;
    }

    /**
     * Returns a shaped tensor [7][3][6][8] for convolutional networks.
     */
    public static float[][][][] encodeTensor(GameState state) {
        float[][][][] tensor = new float[GameConstants.LAYERS][GameConstants.ROWS][GameConstants.COLUMNS][CHANNELS];
        float[] flat = encode(state);

        for (int node = 0; node < GameConstants.TOTAL_NODES; node++) {
            int[] c = BoardGeometry.fromNodeId(node);
            int base = node * CHANNELS;
            System.arraycopy(flat, base, tensor[c[0]][c[1]][c[2]], 0, CHANNELS);
        }

        return tensor;
    }
}
