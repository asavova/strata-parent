package com.strata.model;

import com.strata.core.Player;

/**
 * Rich game termination description.
 */
public final class GameResultStatus {
    public enum Status {
        ONGOING,
        WHITE_WIN_KEY_CONTROL,
        BLACK_WIN_KEY_CONTROL,
        WHITE_WIN_ASCENSION_ELIMINATION,
        BLACK_WIN_ASCENSION_ELIMINATION,
        WHITE_WIN_IMMOBILIZATION,
        BLACK_WIN_IMMOBILIZATION,
        DRAW_NO_MATERIAL
    }

    private final Status status;

    public GameResultStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isTerminal() {
        return status != Status.ONGOING;
    }

    public Player winner() {
        return switch (status) {
            case WHITE_WIN_KEY_CONTROL,
                 WHITE_WIN_ASCENSION_ELIMINATION,
                 WHITE_WIN_IMMOBILIZATION -> Player.WHITE;
            case BLACK_WIN_KEY_CONTROL,
                 BLACK_WIN_ASCENSION_ELIMINATION,
                 BLACK_WIN_IMMOBILIZATION -> Player.BLACK;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return status.name();
    }
}
