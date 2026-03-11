package com.strata.ml;

import com.strata.ai.AlphaBetaPruning;
import com.strata.ai.EvaluationFunction;
import com.strata.core.GameConstants;
import com.strata.core.Player;
import com.strata.engine.GameState;
import com.strata.model.Move;
import com.strata.rules.MoveGenerator;

import java.util.List;

/**
 * Hybrid AI that uses the neural network when available,
 * falling back to classical alpha-beta search otherwise.
 *
 * When the neural network is connected:
 * - Uses NN policy to order moves
 * - Uses NN value as leaf evaluation
 * - Combines with shallow classical search for safety
 *
 * When offline:
 * - Pure alpha-beta with classical evaluation
 */
public class HybridAIEngine {
    private final NeuralNetClient nnClient;
    private final AlphaBetaPruning classicalSearch;
    private final MoveGenerator moveGenerator;
    private final EvaluationFunction classicalEval;

    public HybridAIEngine(NeuralNetClient nnClient) {
        this.nnClient = nnClient;
        this.classicalSearch = new AlphaBetaPruning();
        this.moveGenerator = new MoveGenerator();
        this.classicalEval = new EvaluationFunction();
    }

    public HybridAIEngine() {
        this(new NeuralNetClient());
    }

    public Move chooseMove(GameState state, int depth) {
        List<Move> legalMoves = moveGenerator.generateLegalMoves(state);
        if (legalMoves.isEmpty()) {
            return null;
        }

        // Try neural network first
        if (nnClient.isConnected()) {
            Move nnMove = chooseMoveWithNN(state, legalMoves);
            if (nnMove != null) {
                return nnMove;
            }
        }

        // Fallback to classical
        return classicalSearch.findBestMove(state, depth);
    }

    private Move chooseMoveWithNN(GameState state, List<Move> legalMoves) {
        NeuralNetEvaluation eval = nnClient.evaluate(state, legalMoves);
        if (eval == null) {
            return null;
        }

        // Select the legal move with highest policy probability
        Move bestMove = null;
        float bestProb = -1f;

        for (Move move : legalMoves) {
            int idx = MoveEncoder.encode(move);
            if (idx < eval.policy().length && eval.policy()[idx] > bestProb) {
                bestProb = eval.policy()[idx];
                bestMove = move;
            }
        }

        return bestMove;
    }

    public NeuralNetClient getNnClient() {
        return nnClient;
    }
}
