package com.strata.ai;

import com.strata.engine.GameState;
import com.strata.model.MoveRecord;

/**
 * AI facade for selecting moves.
 */
public class AIEngine {
    private final MinimaxSearch minimaxSearch = new MinimaxSearch();
    private final AlphaBetaPruning alphaBetaSearch = new AlphaBetaPruning();

    public MoveRecord chooseMoveWithMinimax(GameState state, int depth) {
        return minimaxSearch.findBestMove(state, depth);
    }

    public MoveRecord chooseMoveWithAlphaBeta(GameState state, int depth) {
        return alphaBetaSearch.findBestMove(state, depth);
    }
}
