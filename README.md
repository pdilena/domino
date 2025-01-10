# Domino Block Computational Strategies
This repository contains the source code and experimental data for the paper:
"Domino Game Strategies Under Uncertainty: Risk-Averse, Risk-Seeking, Risk-Neutral, and Regret-Averse Approaches."

The repository enables users to explore various computational strategies for playing Domino, including Expectimax and Minregret. The provided code allows for simulating matches between different strategies on either randomly selected or predefined sets of tiles.

# Getting Started

## Running a Match
To run a match between two players, use the following command:

```
java DominoGame ExpectimaxPlayer MinregretPlayer
```

This will execute a match between the Expectimax and Minregret players using a randomly selected set of tiles.

## Running a Match with a Predefined Tile Set
To specify the tiles for both players, use the -1 and -2 options as follows:

```
java DominoGame MinregretPlayer ExpectimaxPlayer -1 "0|1 0|2 2|2 3|3 0|4 2|4 5|6" -2 "0|3 3|4 1|5 1|6 2|6 3|6 4|6"
```
+ -1 defines the tile set for the first player.
+ -2 defines the tile set for the second player.
+ 
## Output Example
The program will output the result of the match and the running time in milliseconds. For example:

```
MinRegretTT   0|1 0|2 2|2 3|3 0|4 2|4 5|6    ExpectiMaxTT   0|3 3|4 1|5 1|6 2|6 3|6 4|6    7   12938
```
+ The tile sets used by each player are listed.
+ The match result (7 in this case).
+ The total computation time in milliseconds (12938 ms).

## Repository Contents

+ src/: The source code for the Domino strategies and match execution.
+ data/: Experimental data used in the associated paper.
+ README.md: This documentation.
+ LICENSE: License information for the repository.

## Requirements

+ Java: Version 11 or higher.
+ An environment capable of running Java applications (e.g., terminal or IDE).

## Citation

If you use this repository or its content in your research, please cite the associated paper:
"Domino Game Strategies Under Uncertainty: Risk-Averse, Risk-Seeking, Risk-Neutral, and Regret-Averse Approaches."

