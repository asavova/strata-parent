package com.strata.rules;

import com.strata.core.GameConstants;
import com.strata.core.Player;
import com.strata.engine.GameState;
import com.strata.model.GameResultStatus;
import com.strata.model.TransformationStage;

/**
 * Evaluates terminal conditions and richer game outcomes.
 */
public class WinConditionChecker {
    public boolean hasWon(GameState state, Player player) {
        GameResultStatus result = evaluate(state);
        return result.isTerminal() && result.winner() == player;
    }

    public boolean hasLockedWin(GameState state, Player player) {
        return controlsCentralKeyNodes(state, player)
                || capturedAllOpponentPhilosophers(state, player);
    }

    public GameResultStatus evaluate(GameState state) {
        if (controlsCentralKeyNodes(state, Player.WHITE)) {
            return new GameResultStatus(GameResultStatus.Status.WHITE_WIN_KEY_CONTROL);
        }
        if (controlsCentralKeyNodes(state, Player.BLACK)) {
            return new GameResultStatus(GameResultStatus.Status.BLACK_WIN_KEY_CONTROL);
        }

        if (capturedAllOpponentPhilosophers(state, Player.WHITE)) {
            return new GameResultStatus(GameResultStatus.Status.WHITE_WIN_ASCENSION_ELIMINATION);
        }
        if (capturedAllOpponentPhilosophers(state, Player.BLACK)) {
            return new GameResultStatus(GameResultStatus.Status.BLACK_WIN_ASCENSION_ELIMINATION);
        }

        MoveGenerator generator = new MoveGenerator();
        if (state.countPieces(Player.WHITE) == 0 && state.countPieces(Player.BLACK) == 0) {
            return new GameResultStatus(GameResultStatus.Status.DRAW_NO_MATERIAL);
        }

        if (generator.generateLegalMoves(state, Player.WHITE).isEmpty()) {
            return new GameResultStatus(GameResultStatus.Status.BLACK_WIN_IMMOBILIZATION);
        }
        if (generator.generateLegalMoves(state, Player.BLACK).isEmpty()) {
            return new GameResultStatus(GameResultStatus.Status.WHITE_WIN_IMMOBILIZATION);
        }

        return new GameResultStatus(GameResultStatus.Status.ONGOING);
    }

    public boolean controlsCentralKeyNodes(GameState state, Player player) {
        for (int node : GameConstants.KEY_NODES) {
            if (player == Player.WHITE && !state.isWhite(node)) {
                return false;
            }
            if (player == Player.BLACK && !state.isBlack(node)) {
                return false;
            }
        }
        return true;
    }

    public boolean capturedAllOpponentPhilosophers(GameState state, Player player) {
        Player opponent = player.opposite();
        return state.countPiecesAtStage(opponent, TransformationStage.PHILOSOPHERS_STONE) == 0;
    }
}
