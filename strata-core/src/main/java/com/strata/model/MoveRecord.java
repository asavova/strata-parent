package com.strata.model;

import com.strata.core.Player;

/**
 * Move record with enough state for undo.
 */
public final class MoveRecord {
    private final int fromNode;
    private final int toNode;
    private final PieceType movedPiece;
    private final Player mover;

    private final PieceType capturedPiece;
    private final Player capturedOwner;

    private final byte previousStage;
    private final byte capturedStage;

    public MoveRecord(
            int fromNode,
            int toNode,
            PieceType movedPiece,
            Player mover,
            PieceType capturedPiece,
            Player capturedOwner,
            byte previousStage,
            byte capturedStage
    ) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.movedPiece = movedPiece;
        this.mover = mover;
        this.capturedPiece = capturedPiece;
        this.capturedOwner = capturedOwner;
        this.previousStage = previousStage;
        this.capturedStage = capturedStage;
    }

    public int getFromNode() {
        return fromNode;
    }

    public int getToNode() {
        return toNode;
    }

    public PieceType getMovedPiece() {
        return movedPiece;
    }

    public Player getMover() {
        return mover;
    }

    public PieceType getCapturedPiece() {
        return capturedPiece;
    }

    public Player getCapturedOwner() {
        return capturedOwner;
    }

    public byte getPreviousStage() {
        return previousStage;
    }

    public byte getCapturedStage() {
        return capturedStage;
    }

    public boolean isCapture() {
        return capturedPiece != null;
    }

    @Override
    public String toString() {
        return "MoveRecord{" +
                "from=" + fromNode +
                ", to=" + toNode +
                ", movedPiece=" + movedPiece +
                ", mover=" + mover +
                ", capturedPiece=" + capturedPiece +
                '}';
    }
}
