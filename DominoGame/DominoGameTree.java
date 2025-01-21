import java.util.ArrayList;

/**
 * Outputs detailed statistics about the game tree of a Domino game.
 * <p>
 * The generated output includes:
 * <ul>
 *   <li>The first Domino tile set (containing the tile with the largest double).</li>
 *   <li>The second Domino tile set.</li>
 *   <li>A list of leaf scores. Scores followed by an "X" indicate that the match ended in a block condition.</li>
 *   <li>The minimax score of the match.</li> 
 *   <li>The total running time in milliseconds.</li>
 * </ul>
 */
public class DominoGameTree {

	private static ArrayList<DominoTile> parseTiles(String tiles) throws NumberFormatException {
		ArrayList<DominoTile> T = new ArrayList<>();
		String[] tokens   = tiles.trim().split("\\s+");

		for(String t: tokens) {
			String[] num = t.split("|");
			int x = Integer.parseInt(num[0]);
			int y = Integer.parseInt(num[2]);
			T.add(new DominoTile(x, y));
		}

		return T;
	}

        private static int minimax(DominoBoardPlus B) {
                if(B.getCurrentState() == DominoGameState.ENDED) {
                        int score = B.getCurrentScore();
                        if(B.getNumOfTiles(DominoPlayerId.FIRST) == 0 || B.getNumOfTiles(DominoPlayerId.SECOND) == 0)
                                System.out.print(" " + score);
                        else
                                System.out.print(" " + score + "X");
                        //B.print();
                        return score;
                } else if (B.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        int score = Integer.MIN_VALUE;
                        for(DominoTile tile: B.getCurrentPlayerMoves()) {
                                B.playTile(tile);
                                score = Math.max(score,minimax(B));
                                B.unplayTile();
                        }
                        return score;
                } else {
                        int score = Integer.MAX_VALUE;
                        for(DominoTile tile: B.getCurrentPlayerMoves()) {
                                B.playTile(tile);
                                score = Math.min(score,minimax(B));
                                B.unplayTile();
                        }
                        return score;
                }
        }


    /**
     * DominoGameTree main method.     
     * <p>
     * Run without parameter to show the usage
     * @param args The command line arguments.
     **/ 
	public static void main(String[] args) {
		if(args.length != 2) {
			System.err.println("Usage: DominoGameTree <tiles player1> <tiles player2>\n");
			System.exit(0);
		}
		try {
			DominoTileSet set1 = new DominoTileSet(6,parseTiles(args[0]));
			DominoTileSet set2 = new DominoTileSet(6,parseTiles(args[1]));
            DominoBoardPlus  B = new DominoBoardPlus(set1,set2);
            System.out.print(set1 + "\t" + set2 + "\t");
			long start = System.currentTimeMillis();
			int score = minimax(B);
			long end = System.currentTimeMillis();
            System.out.println("\t" + score + "\t" + (end-start));
		} 
		catch(NumberFormatException e) {
			e.printStackTrace();
		}
		catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
