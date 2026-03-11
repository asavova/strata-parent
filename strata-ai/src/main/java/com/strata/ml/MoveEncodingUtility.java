package com.strata.ml;

import com.strata.core.GameConstants;
import com.strata.model.Move;

import java.util.List;

/**
 * Encodes moves into indices for the neural network policy head.
 *
 * Move space: from_node * TOTAL_NODES + to_node
 * Total possible moves: 126 * 126 = 15876
 *
 * The policy head outputs a probability for each slot.
 * Only legal moves are masked in during training and inference.
 */
public class MoveEncoder {
    public static final int POLICY_SIZE = GameConstants.TOTAL_NODES * GameConstants.TOTAL_NODES;

    public static int encode(Move move) {
        return move.getFromNode() * GameConstants.TOTAL_NODES + move.getToNode();
    }

    public static int encode(int fromNode, int toNode) {
        return fromNode * GameConstants.TOTAL_NODES + toNode;
    }

    public static int[] decode(int moveIndex) {
        int from = moveIndex / GameConstants.TOTAL_NODES;
        int to = moveIndex % GameConstants.TOTAL_NODES;
        return new int[]{from, to};
    }

    /**
     * Creates a legal move mask (1.0 for legal, 0.0 for illegal).
     */
    public static float[] legalMoveMask(List<Move> legalMoves) {
        float[] mask = new float[POLICY_SIZE];
        for (Move move : legalMoves) {
            mask[encode(move)] = 1.0f;
        }
        return mask;
    }

    /**
     * Converts MCTS visit counts into a policy target distribution.
     */
    public static float[] visitCountsToPolicy(int[] visitCounts, List<Move> legalMoves) {
        float[] policy = new float[POLICY_SIZE];
        float totalVisits = 0;

        for (int i = 0; i < legalMoves.size(); i++) {
            totalVisits += visitCounts[i];
        }

        if (totalVisits == 0) {
            return policy;
        }

        for (int i = 0; i < legalMoves.size(); i++) {
            int idx = encode(legalMoves.get(i));
            policy[idx] = visitCounts[i] / totalVisits;
        }

        return policy;
    }
}
