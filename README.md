# Strata Engine

A Java-based layered strategy board-game engine built with a focus on bitboard optimization, 3D board topology, and AI-ready architecture.

This project is designed as a modular, extensible engine suitable for research, prototyping, and advanced game AI development.

---

## Overview

Strata Engine implements a 126-node 3D board system using high-performance bitboards and a layered coordinate structure.

The engine includes:
- Bitboard-based game state representation
- Multi-layer (3D) board geometry
- Precomputed movement masks
- Turn-based game engine with undo/redo support
- Piece transformation system
- AI-ready evaluation and search framework
- JSON export for external integrations

---

## Board Architecture

The board consists of:
- 7 layers
- Each layer is a 3 × 6 grid
- Total positions: 126 nodes

### Coordinate System

(layer, row, column)

### Node ID Formula

nodeId = layer * 18 + row * 6 + column

---

## Bitboard System

Due to the 126-node board, each bitboard is implemented using two long values:

class BitBoard {
    long lowBits;  // nodes 0–63
    long highBits; // nodes 64–125
}

### Supported Operations

- set(nodeId)
- clear(nodeId)
- isSet(nodeId)
- and(BitBoard)
- or(BitBoard)
- xor(BitBoard)
- not()
- popcount()

---

## Gameplay Features

### Players
- WHITE
- BLACK

### Piece Types
Each player controls:
- ADEPT – same-layer movement
- ELEMENTAL – diagonal and vertical movement
- GUARDIAN – orthogonal movement

Each player starts with 27 pieces.

---

## Transformation System

Each piece progresses through a 9-stage transformation cycle:

CALCINATION → DISSOLUTION → SEPARATION → CONJUNCTION → FERMENTATION → DISTILLATION → COAGULATION → ILLUMINATION → PHILOSOPHERS_STONE

Each move advances the stage:
stage = (stage + 1) % 9

---

## Movement Rules

Movement is generated using precomputed masks:
- adjacentMask
- diagonalMask
- orthogonalMask

### Movement Types

ADEPT:
- Moves to adjacent nodes

ELEMENTAL:
- Moves diagonally and across layers until blocked

GUARDIAN:
- Moves orthogonally and across layers until blocked

Rules:
- Cannot land on friendly pieces
- Can capture enemy pieces
- Sliding movement stops at blockers

---

## AI System

The engine supports AI via:
- Minimax Search
- Alpha-Beta Pruning
- Evaluation Function

### Evaluation Factors
- Center layer control
- Piece count
- Piece transformation stage
- Mobility

---

## Win Conditions

Primary:
- Control key nodes on the central layer (layer 3)

Alternative:
- Capture all opponent pieces that reached PHILOSOPHERS_STONE

---

## JSON Export

Game state can be exported using:

JsonExporter.exportGameState(GameState state)

The output includes:
- Piece locations
- Piece types
- Piece stages
- Active player

This enables integration with external AI systems.

---

## Project Structure

core/      Core engine logic  
board/     Board geometry and coordinates  
model/     Data models (GameState, Move, Pieces)  
engine/    Game engine logic  
rules/     Movement and game rules  
ai/        AI (Minimax, evaluation, search)  
api/       JSON export and interfaces  
util/      Utilities and helpers  

---

## Getting Started

Requirements:
- Java 17+
- IntelliJ IDEA recommended

Steps:
1. Clone the repository:
   git clone https://github.com/asavova/strata-parent.git

2. Open in IntelliJ IDEA
3. Build and run the project

---

## Design Principles

- Bitboard efficiency for fast computation
- Precomputed movement masks for performance
- Clean modular architecture
- AI-first design
- Extensibility

---

## Future Improvements

- Advanced AI heuristics
- Neural network integration
- Parallel move evaluation
- Multiplayer support
- Visualization tools

---

## License

This project is intended for educational and research purposes.

---

## Author

Anelia Savova  

