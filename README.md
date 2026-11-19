# Strata Engine

Strata is a Java 21 engine for a turn-based strategy board game played across seven layers. The project focuses on board representation, movement rules, game-state management, and efficient move generation.

## Core engine

The standalone `strata-core` module uses a 126-node board (7 layers of 3 × 6 positions) and a two-`long` bitboard representation. It contains:

- Board geometry and coordinate conversion
- Precomputed movement masks
- Piece ownership, types, and transformation stages
- Move generation and capture rules
- Win-condition checks
- Game-state copying, move application, and partial undo support

The core is plain Java. It does not require a Python service, neural network, external API, or GPU.

## Build

Requirements: JDK 21 and Maven 3.9+.

From the repository root:

```bash
mvn clean verify
```

The default Maven reactor builds the `strata-core` module. A GitHub Actions workflow checks this build using Java 21.

## Repository layout

- `strata-core/` — standalone board-game logic, rules, and state management; **the scope of the core engine project**
- `strata-engine/` — early application scaffold, not in the default build
- `strata-app/` — application assembly, not in the default build
- `strata-protocol/` — experimental integration protocol, not in the default build
- `strata-ai/` — optional classical search and experimental ML integration, not in the default build

The integration modules are retained as historical/experimental work. They are **not required for the standalone engine and are not part of the proposed non-ML development scope**. Building them separately may require additional fixes and dependencies.

## Current limitations

- The default build covers `strata-core`, not every module in the repository.
- Full undo state restoration and comprehensive automated tests still need work.
- Engine performance and consistency under larger game workloads have not been benchmarked.

## License

MIT License. See [LICENSE](LICENSE).

## Author

Anelia Savova
