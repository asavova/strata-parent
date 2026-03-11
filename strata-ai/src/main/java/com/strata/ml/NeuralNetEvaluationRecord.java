package com.strata.ml;

/**
 * Result from the neural network.
 *
 * value:  position evaluation from active player's perspective [-1, 1]
 * policy: probability distribution over all possible moves
 */
public record NeuralNetEvaluation(
        float value,
        float[] policy
) {
}
