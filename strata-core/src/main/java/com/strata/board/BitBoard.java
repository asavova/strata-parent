package com.strata.board;

import com.strata.core.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A 126-bit bitboard implemented with two longs.
 *
 * lowBits  -> nodes 0..63
 * highBits -> nodes 64..125
 */
public final class BitBoard implements Cloneable {
    private long lowBits;
    private long highBits;

    public BitBoard() {
        this(0L, 0L);
    }

    public BitBoard(long lowBits, long highBits) {
        this.lowBits = lowBits;
        this.highBits = normalizeHighBits(highBits);
    }

    public static BitBoard empty() {
        return new BitBoard();
    }

    public static BitBoard fullBoardMask() {
        long low = -1L;
        int highCount = GameConstants.TOTAL_NODES - 64;
        long high = (1L << highCount) - 1L;
        return new BitBoard(low, high);
    }

    public long getLowBits() {
        return lowBits;
    }

    public long getHighBits() {
        return highBits;
    }

    public void set(int nodeId) {
        validateNode(nodeId);
        if (nodeId < 64) {
            lowBits |= (1L << nodeId);
        } else {
            highBits |= (1L << (nodeId - 64));
            highBits = normalizeHighBits(highBits);
        }
    }

    public void clear(int nodeId) {
        validateNode(nodeId);
        if (nodeId < 64) {
            lowBits &= ~(1L << nodeId);
        } else {
            highBits &= ~(1L << (nodeId - 64));
        }
    }

    public boolean isSet(int nodeId) {
        validateNode(nodeId);
        if (nodeId < 64) {
            return (lowBits & (1L << nodeId)) != 0L;
        }
        return (highBits & (1L << (nodeId - 64))) != 0L;
    }

    public BitBoard and(BitBoard other) {
        Objects.requireNonNull(other, "other");
        return new BitBoard(lowBits & other.lowBits, highBits & other.highBits);
    }

    public BitBoard or(BitBoard other) {
        Objects.requireNonNull(other, "other");
        return new BitBoard(lowBits | other.lowBits, highBits | other.highBits);
    }

    public BitBoard xor(BitBoard other) {
        Objects.requireNonNull(other, "other");
        return new BitBoard(lowBits ^ other.lowBits, highBits ^ other.highBits);
    }

    public BitBoard not() {
        return fullBoardMask().xor(this);
    }

    public int popcount() {
        return Long.bitCount(lowBits) + Long.bitCount(highBits);
    }

    public boolean isEmpty() {
        return lowBits == 0L && highBits == 0L;
    }

    public boolean intersects(BitBoard other) {
        Objects.requireNonNull(other, "other");
        return ((lowBits & other.lowBits) != 0L) || ((highBits & other.highBits) != 0L);
    }

    public void clearAll() {
        lowBits = 0L;
        highBits = 0L;
    }

    public List<Integer> toNodeList() {
        List<Integer> nodes = new ArrayList<>();
        for (int node = 0; node < GameConstants.TOTAL_NODES; node++) {
            if (isSet(node)) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public BitBoard clone() {
        return new BitBoard(lowBits, highBits);
    }

    @Override
    public String toString() {
        return "BitBoard{count=" + popcount() + ", low=" + lowBits + ", high=" + highBits + "}";
    }

    private static long normalizeHighBits(long bits) {
        int validBits = GameConstants.TOTAL_NODES - 64;
        long mask = (1L << validBits) - 1L;
        return bits & mask;
    }

    private static void validateNode(int nodeId) {
        if (nodeId < 0 || nodeId >= GameConstants.TOTAL_NODES) {
            throw new IllegalArgumentException("Invalid nodeId: " + nodeId);
        }
    }
}
