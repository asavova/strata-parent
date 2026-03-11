package com.strata.ml;

import java.util.List;

/**
 * Orchestrates the training loop:
 *
 * 1. Self-play: Java engine plays N games, generating training examples
 * 2. Training:  Examples are sent to Python for neural network training
 * 3. Repeat:    New network weights improve the next generation of self-play
 *
 * This is the AlphaZero-style training loop.
 */
public class TrainingCoordinator {
    private final SelfPlayDriver selfPlayDriver;
    private final NeuralNetClient nnClient;

    private static final int GAMES_PER_BATCH = 50;
    private static final int TRAINING_ITERATIONS = 100;

    public TrainingCoordinator(NeuralNetClient nnClient) {
        this.selfPlayDriver = new SelfPlayDriver();
        this.nnClient = nnClient;
    }

    /**
     * Run the full training pipeline.
     */
    public void runTrainingPipeline() {
        System.out.println("=== STRATA TRAINING PIPELINE ===");
        System.out.println("Games per batch: " + GAMES_PER_BATCH);
        System.out.println("Training iterations: " + TRAINING_ITERATIONS);

        for (int iteration = 1; iteration <= TRAINING_ITERATIONS; iteration++) {
            System.out.println("\n--- Iteration " + iteration + "/" + TRAINING_ITERATIONS + " ---");

            // Phase 1: Self-play
            System.out.println("[Phase 1] Running self-play...");
            long startTime = System.currentTimeMillis();
            List<TrainingExample> examples = selfPlayDriver.runSelfPlayBatch(GAMES_PER_BATCH);
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.printf("[Phase 1] Generated %d examples in %.1f seconds%n",
                    examples.size(), elapsed / 1000.0);

            // Phase 2: Submit to Python for training
            if (nnClient.isConnected()) {
                System.out.println("[Phase 2] Submitting training data to Python ML server...");
                boolean success = nnClient.submitTrainingData(examples);
                if (success) {
                    System.out.println("[Phase 2] Training step completed successfully.");
                } else {
                    System.out.println("[Phase 2] Training submission failed.");
                }
            } else {
                System.out.println("[Phase 2] ML server not connected. Saving examples locally.");
                saveExamplesLocally(examples, iteration);
            }
        }

        System.out.println("\n=== TRAINING COMPLETE ===");
    }

    private void saveExamplesLocally(List<TrainingExample> examples, int iteration) {
        // In production, serialize to disk as binary/npy files
        System.out.printf("[Storage] Would save %d examples for iteration %d%n",
                examples.size(), iteration);
    }
}
