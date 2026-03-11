package com.strata;

import com.strata.ai.AIEngine;
import com.strata.api.JsonExporter;
import com.strata.engine.GameEngine;
import com.strata.engine.GameState;
import com.strata.model.GameResultStatus;
import com.strata.model.MoveRecord;
import com.strata.rules.MoveGenerator;
import com.strata.rules.WinConditionChecker;
import com.strata.util.BoardPrinter;
import com.strata.util.NodeFormatter;

import java.util.List;

/**
 * Demo entry point showing basic engine startup gameplay.
 */
public class Main {
    public static void main(String[] args) {
        GameState state = new GameState();
        GameEngine engine = new GameEngine(state);
        MoveGenerator moveGenerator = new MoveGenerator();
        WinConditionChecker resultChecker = new WinConditionChecker();
        AIEngine ai = new AIEngine();
        JsonExporter exporter = new JsonExporter();

        System.out.println("=== STRATA ENGINE DEMO ===");
        System.out.println("Initial board:");
        System.out.println(BoardPrinter.print(state));

        for (int turn = 1; turn <= 6; turn++) {
            GameResultStatus result = resultChecker.evaluate(state);
            if (result.isTerminal()) {
                System.out.println("Game finished early: " + result);
                break;
            }

            List<MoveRecord> legalMoves = moveGenerator.generateLegalMoves(state);
            System.out.println("Turn " + turn + " - Active player: " + state.getActivePlayer());
            System.out.println("Legal moves available: " + legalMoves.size());

            if (legalMoves.isEmpty()) {
                System.out.println("No legal moves available.");
                break;
            }

            MoveRecord selected = ai.chooseMoveWithAlphaBeta(state, 2);
            if (selected == null) {
                selected = legalMoves.getFirst();
            }

            System.out.println("Selected move: " + NodeFormatter.formatMove(selected));
            engine.applyMove(selected);
            engine.switchPlayer();

            System.out.println(BoardPrinter.print(state));
        }

        GameResultStatus finalResult = resultChecker.evaluate(state);
        System.out.println("Final result: " + finalResult);

        System.out.println("=== JSON EXPORT ===");
        System.out.println(exporter.exportGameState(state));
    }
}
