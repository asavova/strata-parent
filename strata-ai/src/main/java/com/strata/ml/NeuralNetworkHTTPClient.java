package com.strata.ml;

import com.strata.core.GameConstants;
import com.strata.engine.GameState;
import com.strata.model.Move;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Client that sends positions to the Python neural network server.
 *
 * Uses HTTP+JSON for simplicity. Can be upgraded to gRPC once protobuf
 * code generation is wired up in the build.
 *
 * Endpoints:
 *   POST /evaluate   - evaluate a single position
 *   POST /train      - submit training examples
 *   GET  /health     - check server status
 */
public class NeuralNetClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private boolean connected;

    public NeuralNetClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.connected = false;
    }

    public NeuralNetClient() {
        this("http://localhost:9000");
    }

    public boolean connect() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/health"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            connected = response.statusCode() == 200;
        } catch (Exception e) {
            connected = false;
        }

        if (connected) {
            System.out.println("[NeuralNet] Connected to ML server at " + baseUrl);
        } else {
            System.out.println("[NeuralNet] ML server not available. Using classical evaluation.");
        }
        return connected;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Evaluate a position using the neural network.
     * Returns [value, policy[0], policy[1], ...].
     */
    public NeuralNetEvaluation evaluate(GameState state, List<Move> legalMoves) {
        if (!connected) {
            return null;
        }

        try {
            float[] encoded = StateEncoder.encode(state);
            float[] legalMask = MoveEncoder.legalMoveMask(legalMoves);

            String json = buildEvalRequest(encoded, legalMask);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/evaluate"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseEvalResponse(response.body());
            }
        } catch (Exception e) {
            System.err.println("[NeuralNet] Evaluation failed: " + e.getMessage());
        }

        return null;
    }

    /**
     * Submit training examples from self-play.
     */
    public boolean submitTrainingData(List<TrainingExample> examples) {
        if (!connected) {
            return false;
        }

        try {
            String json = buildTrainingBatch(examples);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/train"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[NeuralNet] Training submission failed: " + e.getMessage());
            return false;
        }
    }

    private String buildEvalRequest(float[] state, float[] legalMask) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"state\": [");
        for (int i = 0; i < state.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(state[i]);
        }
        sb.append("], \"legal_mask\": [");
        for (int i = 0; i < legalMask.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(legalMask[i]);
        }
        sb.append("]}");
        return sb.toString();
    }

    private String buildTrainingBatch(List<TrainingExample> examples) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"examples\": [");
        for (int e = 0; e < examples.size(); e++) {
            if (e > 0) sb.append(",");
            TrainingExample ex = examples.get(e);
            sb.append("{\"state\": [");
            for (int i = 0; i < ex.state().length; i++) {
                if (i > 0) sb.append(",");
                sb.append(ex.state()[i]);
            }
            sb.append("], \"policy\": [");
            for (int i = 0; i < ex.policy().length; i++) {
                if (i > 0) sb.append(",");
                sb.append(ex.policy()[i]);
            }
            sb.append("], \"value\": ").append(ex.value());
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private NeuralNetEvaluation parseEvalResponse(String json) {
        // Lightweight manual parsing — replace with Gson if needed
        try {
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            float value = obj.get("value").getAsFloat();

            com.google.gson.JsonArray policyArr = obj.getAsJsonArray("policy");
            float[] policy = new float[policyArr.size()];
            for (int i = 0; i < policyArr.size(); i++) {
                policy[i] = policyArr.get(i).getAsFloat();
            }

            return new NeuralNetEvaluation(value, policy);
        } catch (Exception e) {
            System.err.println("[NeuralNet] Failed to parse response: " + e.getMessage());
            return null;
        }
    }
}
