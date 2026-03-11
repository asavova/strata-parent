package com.strata.ml;

/**
 * One training example generated during self-play.
 *
 * state:   encoded board state (1008 floats)
 * policy:  MCTS visit distribution (15876 floats, sparse)
 * value:   game outcome from this position's perspective (-1, 0, +1)
 */
public record TrainingExample(
        float[] state,
        float[] policy,
        float value
) {
}
