package com.strata.engine;

import com.strata.board.BitBoard;
import com.strata.board.BoardGeometry;
import com.strata.core.GameConstants;
import com.strata.model.PieceType;
import com.strata.model.TransformationStage;
import com.strata.core.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable game state.
 *
 * Ownership bitboards:
 * - whitePieces
 * - blackPieces
 *
 * Type bitboards:
 * - adepts
 * - elementals
 * - guardians
 *
 * Per-node stage:
 * - pieceStage[126]
 */
public class GameState implements Cloneable {
    private final BitBoard whitePieces = new BitBoard();
    private final BitBoard blackPieces = new BitBoard();

    private final BitBoard adepts = new BitBoard();
    private final BitBoard elementals = new BitBoard();
    private final BitBoard guardians = new BitBoard();

    private final byte[] pieceStage = new byte[GameConstants.TOTAL_NODES];

    private Player activePlayer = Player.WHITE;
    private int plyCount = 0;

    public GameState() {
        initializeStartingPosition();
    }

    private GameState(boolean initialize) {
        if (initialize) {
            initializeStartingPosition();
        }
    }

    private void initializeStartingPosition() {
        placeSide(Player.WHITE, new int[]{0, 1, 2}, 0, 2);
        placeSide(Player.BLACK, new int[]{4, 5, 6}, 3, 5);
    }

    private void placeSide(Player player, int[] layers, int colStart, int colEnd) {
        for (int layer : layers) {
            for (int col = colStart; col <= colEnd; col++) {
                placePiece(player, PieceType.ADEPT, BoardGeometry.toNodeId(layer, 0, col), (byte) 0);
                placePiece(player, PieceType.ELEMENTAL, BoardGeometry.toNodeId(layer, 1, col), (byte) 0);
                placePiece(player, PieceType.GUARDIAN, BoardGeometry.toNodeId(layer, 2, col), (byte) 0);
            }
        }
    }

    public boolean isOccupied(int nodeId) {
        return whitePieces.isSet(nodeId) || blackPieces.isSet(nodeId);
    }

    public boolean isWhite(int nodeId) {
        return whitePieces.isSet(nodeId);
    }

    public boolean isBlack(int nodeId) {
        return blackPieces.isSet(nodeId);
    }

    public Player getOwner(int nodeId) {
        if (isWhite(nodeId)) {
            return Player.WHITE;
        }
        if (isBlack(nodeId)) {
            return Player.BLACK;
        }
        return null;
    }

    public PieceType getPieceType(int nodeId) {
        if (!isOccupied(nodeId)) {
            return null;
        }
        if (adepts.isSet(nodeId)) {
            return PieceType.ADEPT;
        }
        if (elementals.isSet(nodeId)) {
            return PieceType.ELEMENTAL;
        }
        if (guardians.isSet(nodeId)) {
            return PieceType.GUARDIAN;
        }
        throw new IllegalStateException("Occupied node missing type bit: " + nodeId);
    }

    public byte getStage(int nodeId) {
        return pieceStage[nodeId];
    }

    public void setStage(int nodeId, byte stage) {
        pieceStage[nodeId] = stage;
    }

    public void advanceStage(int nodeId) {
        pieceStage[nodeId] = TransformationStage.next(pieceStage[nodeId]);
    }

    public void clearNode(int nodeId) {
        whitePieces.clear(nodeId);
        blackPieces.clear(nodeId);
        adepts.clear(nodeId);
        elementals.clear(nodeId);
        guardians.clear(nodeId);
        pieceStage[nodeId] = 0;
    }

    public void placePiece(Player player, PieceType type, int nodeId, byte stage) {
        clearNode(nodeId);

        if (player == Player.WHITE) {
            whitePieces.set(nodeId);
        } else {
            blackPieces.set(nodeId);
        }

        switch (type) {
            case ADEPT -> adepts.set(nodeId);
            case ELEMENTAL -> elementals.set(nodeId);
            case GUARDIAN -> guardians.set(nodeId);
        }

        pieceStage[nodeId] = stage;
    }

    public List<Integer> getPieces(Player player) {
        List<Integer> result = new ArrayList<>();
        BitBoard board = player == Player.WHITE ? whitePieces : blackPieces;
        for (int node = 0; node < GameConstants.TOTAL_NODES; node++) {
            if (board.isSet(node)) {
                result.add(node);
            }
        }
        return result;
    }

    public int countPieces(Player player) {
        return player == Player.WHITE ? whitePieces.popcount() : blackPieces.popcount();
    }

    public int countPiecesOfType(Player player, PieceType type) {
        int count = 0;
        for (int node : getPieces(player)) {
            if (getPieceType(node) == type) {
                count++;
            }
        }
        return count;
    }

    public int countPiecesAtStage(Player player, TransformationStage stage) {
        int count = 0;
        for (int node : getPieces(player)) {
            if (pieceStage[node] == stage.ordinal()) {
                count++;
            }
        }
        return count;
    }

    public BitBoard getWhitePieces() {
        return whitePieces;
    }

    public BitBoard getBlackPieces() {
        return blackPieces;
    }

    public BitBoard getAdepts() {
        return adepts;
    }

    public BitBoard getElementals() {
        return elementals;
    }

    public BitBoard getGuardians() {
        return guardians;
    }

    public byte[] getPieceStageArray() {
        return pieceStage;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer;
    }

    public int getPlyCount() {
        return plyCount;
    }

    public void incrementPly() {
        plyCount++;
    }

    @Override
    public GameState clone() {
        GameState copy = new GameState(false);

        for (int node = 0; node < GameConstants.TOTAL_NODES; node++) {
            if (whitePieces.isSet(node)) {
                copy.whitePieces.set(node);
            }
            if (blackPieces.isSet(node)) {
                copy.blackPieces.set(node);
            }
            if (adepts.isSet(node)) {
                copy.adepts.set(node);
            }
            if (elementals.isSet(node)) {
                copy.elementals.set(node);
            }
            if (guardians.isSet(node)) {
                copy.guardians.set(node);
            }
            copy.pieceStage[node] = pieceStage[node];
        }

        copy.activePlayer = activePlayer;
        copy.plyCount = plyCount;
        return copy;
    }
}
