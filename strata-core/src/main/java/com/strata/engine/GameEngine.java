package com.strata.engine;

import com.strata.model.MoveRecord;

/**
 * Applies and undoes moves while keeping bitboards in sync.
 */
public class GameEngine {
    private final GameState state;

    public GameEngine(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    public void applyMove(MoveRecord move) {
        int from = move.getFromNode();
        int to = move.getToNode();

        state.clearNode(to);
        state.placePiece(move.getMover(), move.getMovedPiece(), to, move.getPreviousStage());
        state.clearNode(from);
        state.advanceStage(to);
        state.incrementPly();
    }

    public void undoMove(MoveRecord move) {
        int from = move.getFromNode();
        int to = move.getToNode();

        state.clearNode(from);
        state.placePiece(move.getMover(), move.getMovedPiece(), from, move.getPreviousStage());

        state.clearNode(to);
        if (move.isCapture()) {
            state.placePiece(move.getCapturedOwner(), move.getCapturedPiece(), to, move.getCapturedStage());
        }
    }

    public void switchPlayer() {
        state.setActivePlayer(state.getActivePlayer().opposite());
    }
}
