package com.strata.core;

/**
 * The two sides in the game.
 */
public enum Player {
    WHITE,
    BLACK;

    public Player opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
