/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Author: Pietro Di Lena
 * Year: 2025
 * 
 * For inquiries, contact: pietro.dilena@unibo.it
 */
import java.util.Iterator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class to manage the state and mechanics of a domino game.
 * <p>
 * This class manages the tile sets of the players, the board (represented as a sequence of played tiles),
 * the history of moves, and the current game state. While it allows players to play and unplay tiles in turn,
 * it does not enforce the legality of those moves. This class aims to provide a fast implementation of a 
 * domino game environment. For a more controlled but slower implementation, refer to the {@link DominoBoardPlus} class.
 */
public class DominoBoard {
        /** The actual tile sets of each player, which are hidden from their opponent. */
        protected DominoTileSet[]        trueTiles;     
        /** The sequence of tiles already played, arranged in a "snake" format on the board. */
        protected Deque<DominoTile>      boardTiles;    
        /** The history of all moves made during the game, stored as a sequence of historical board states. */
        protected Deque<DominoBoardHist> moveHist;     
        /** Identifier for the current player (either the first or second player). */
        protected DominoPlayerId         currentPlayer; 
        /** The current state of the game (either open or ended). */
        protected DominoGameState        currentState; 
        /** The number of playable tiles of ech player */
        protected int size[];

        /** Structures for the Zobrist hash */
        private long endsHash[];
        private long turnHash[];
        private long sizeHash[][];

        /**
         * Constructs a new domino board with the initial tile sets for both players.
         * <p>
         * Initializes the game by setting up the tile sets, an empty board for played tiles, 
         * an empty move history, and sets the first player to start the game.
         * 
         * @param set1 the domino tile set of the first player
         * @param set2 the domino tile set of the second player
         */
        public DominoBoard(DominoTileSet set1, DominoTileSet set2) {
                this.trueTiles     = new DominoTileSet[]{set1, set2};
                this.boardTiles    = new LinkedList<DominoTile>();
                this.moveHist      = new LinkedList<DominoBoardHist>();
                this.currentPlayer = DominoPlayerId.FIRST;
                this.currentState  = DominoGameState.OPEN;
                this.size          = new int[]{set1.size(),set2.size()};
                this.setupHash();
        }

        /**
         * Constructs a new domino board with the initial tile sets for both players and an intial tile on the board.
         * 
         * @param set1 the domino tile set of the first player
         * @param set2 the domino tile set of the second player
         * @param startile the starting tile on the board
         */
        public DominoBoard(DominoTileSet set1, DominoTileSet set2, DominoTile startile) {
                this(set1,set2);
                this.boardTiles.add(startile);
                this.moveHist.push(new DominoBoardHist(DominoBoardSide.CENTER,null,startile,startile));
        }


        /**
         * Constructs a new domino board with the initial tile sets for both players and an intial tile on the board and
         * each player is allowed to use a maximum number of tiles from his set.
         *
         * @param set1 the domino tile set of the first player
         * @param set2 the domino tile set of the second player
         * @param startile the starting tile on the board
         * @param size1 number of tiles playable by the first player
         * @param size2 number of tiles playable by the second player
         */
        public DominoBoard(DominoTileSet set1, DominoTileSet set2, DominoTile startile, int size1, int size2) {
                this(set1,set2,startile);
                this.size[0] = Math.min(size1,set1.size());
                this.size[1] = Math.min(size2,set2.size());
        }

        /**
         * Plays the specified <code>tile</code> for the current player.
         * <p>
         * The method checks whether the current player actually owns the
         * the specified tile and if it is a legal move.
         * @param tile tile to be played
         * @return domino game state (open or ended)
         * @throws IllegalArgumentException if the specified tile cannot be played
         */
        public DominoGameState playTile(DominoTile tile) throws IllegalArgumentException {
                //checkPlayedTile(tile);
                DominoTile boardEnds = this.getBoardEnds();

                if(tile.isEmpty()) {
                        if(this.wasLastTurnPass()) this.currentState = DominoGameState.ENDED;
                        this.moveHist.push(new DominoBoardHist(DominoBoardSide.UNPLACED,this.currentPlayer,tile,boardEnds));
                } else {
                        DominoTile t = new DominoTile(tile);
                        DominoBoardSide side;
                        if(t.leftMatches(boardEnds.left())) {
                                side = DominoBoardSide.LEFT;
                                this.boardTiles.addFirst(new DominoTile(t.right(),t.left()));
                        } else if(t.leftMatches(boardEnds.right())) {
                                side = DominoBoardSide.RIGHT;
                                this.boardTiles.addLast(t);
                        } else {
                                side = DominoBoardSide.CENTER;
                                this.boardTiles.add(t);
                        }
                        this.moveHist.push(new DominoBoardHist(side,this.currentPlayer,t,this.getBoardEnds()));
                        this.trueTiles[this.currentPlayer.ordinal()].remove(t);
                        //if (this.trueTiles[this.currentPlayer.ordinal()].size() == 0)
                        if(--this.size[this.currentPlayer.ordinal()] == 0) 
                                this.currentState = DominoGameState.ENDED;
                }

                this.currentPlayer = this.currentPlayer.toggle();

                return this.currentState;
        }

        private void checkPlayedTile(DominoTile tile) {
                DominoTile boardEnds = this.getBoardEnds();
                if(this.currentState == DominoGameState.ENDED)
                        throw new IllegalArgumentException("The game is over");
                if(!tile.isEmpty() && !this.trueTiles[this.currentPlayer.ordinal()].contains(tile))
                        throw new IllegalArgumentException("The " + this.currentPlayer + " player does not posses the tile " + tile);
                if(boardEnds.isEmpty()) {
                        if(!tile.isDouble() || !tile.equals(this.trueTiles[this.currentPlayer.ordinal()].getLargestDouble()))
                                throw new IllegalArgumentException("The " + this.currentPlayer + " player  cannot play " + tile + " as first tile");
                } else {
                        if(tile.isEmpty() && this.trueTiles[this.currentPlayer.ordinal()].matches(boardEnds))
                                throw new IllegalArgumentException("The " + this.currentPlayer + " player has playlable tiles, cannot pass");
                        if(!tile.isEmpty() && !tile.leftMatches(boardEnds))
                                throw new IllegalArgumentException("The left side of the tile " + tile + " does not match the current ends " + boardEnds);
                }
        }

        /**
         * Undoes the last move.
         * @throws IllegalArgumentException if there is no move to undo
         */
        public void unplayTile() throws IllegalArgumentException {
                if(this.moveHist.size() == 0 || (this.moveHist.size() == 1 && this.moveHist.peek().playerid == null))
                        throw new IllegalArgumentException("Not possible to undo: empty history");

                DominoBoardHist h = this.moveHist.pop();
                if(h.side == DominoBoardSide.LEFT)
                        this.trueTiles[h.playerid.ordinal()].add(this.boardTiles.removeFirst());
                else if(h.side == DominoBoardSide.RIGHT)
                        this.trueTiles[h.playerid.ordinal()].add(this.boardTiles.removeLast());

                if(!h.tile.isEmpty()) this.size[h.playerid.ordinal()]++;
                this.currentState  = DominoGameState.OPEN;
                this.currentPlayer = this.currentPlayer.toggle();
        }

        /**
         * Returns the list of tiles that can be legally played by the current player.
         * @return list of of tiles that can be legally played by the current player
         */
        public LinkedList<DominoTile> getCurrentPlayerMoves() {
                return this.trueTiles[this.currentPlayer.ordinal()].matchingTiles(this.getBoardEnds());
        }

        /**
         * Returns true if the last turn was passed.
         * @return true if the last turn was passed
         */
        private boolean wasLastTurnPass() {
                return this.moveHist.size() > 0 && this.moveHist.peek().side == DominoBoardSide.UNPLACED;
        }

        /**
         * Returns the identifier of the current player.
         * @return the identifier of the current player
         */
        public DominoPlayerId getCurrentPlayer() {
                return this.currentPlayer;
        }

        /**
         * Returns the current state of the game (open or ended).
         * @return the current state of the game (open or ended)
         */
        public DominoGameState getCurrentState() {
                return this.currentState;
        }

        /**
         * Returns the number of tiles still in possession of the specified player.
         * @param id player identifier
         * @return the number of tiles still in possession of the specified player
         */
        public int getNumOfTiles(DominoPlayerId id) {
                return this.trueTiles[id.ordinal()].size();
        }

        /**
         * Returns the number of tiles still in the hand of the specified player
         * @param id player identifier
         * @return the number of tiles still in the hand of the specified player
         */
        public int getNumOfHandTiles(DominoPlayerId id) {
                return this.size[id.ordinal()];
        }

        /**
         * Returns the tile set of the specified player.
         * @param playerid player identifier
         * @return the tile set of the specified player
         */
        public DominoTileSet getPlayerTiles(DominoPlayerId playerid) {
                return this.trueTiles[playerid.ordinal()];
        }

        /**
         * Returns the current ends of the domino "snake" on the board.
         * <p>
         * The domino "snake" is formed by the sequence of tiles that have been placed on the board. 
         * This method returns a domino tile representing the current left and right ends of the snake.
         * If no tiles have been placed yet, an empty tile is returned.
         * 
         * @return a domino tile representing the current ends of the snake, or an empty tile if the board is empty
         */
        public DominoTile getBoardEnds() {
                if(this.boardTiles.size() == 0)
                        return new DominoTile();
                else 
                        return new DominoTile(this.boardTiles.peekFirst().left(),this.boardTiles.peekLast().right());
        }

        /**
         * Returns the current score of the game.
         * <p>
         * The current score is equal to the difference between the tile set score
         * of the second and first player.
         * @return current score of the game 
         */
        public int getCurrentScore() {
                return this.trueTiles[DominoPlayerId.SECOND.ordinal()].getScore() - this.trueTiles[DominoPlayerId.FIRST.ordinal()].getScore();
        }

        private void setupHash() {
                // Setup the random generator seed by using the hash values of the tile sets
                Random rand = new Random(this.trueTiles[0].hashValue()+this.trueTiles[1].hashValue());
        
                // Create hash arrays
                int n = Math.max(this.trueTiles[0].getMaxValue(),this.trueTiles[0].getMaxValue());
                this.endsHash  = new long[(n+1)*(n+2)/2];
                this.turnHash  = new long[2];
                this.sizeHash  = new long[2][Math.max(this.size[0],this.size[1])+1];
                
         
                // Fill them with random stuff
                for (int i = 0; i < this.endsHash.length; i++)  
                        this.endsHash[i] = rand.nextLong();
                
                for(int i = 0; i < this.turnHash.length; i++)
                        this.turnHash[i] = rand.nextLong();
                        
                for(int i = 0; i < this.sizeHash.length; i++)
                        for (int j = 0; j < this.sizeHash[i].length; j++)
                                this.sizeHash[i][j] = rand.nextLong();
        }             

        /**                     
         * Returns a long hash value for this board
         * @return a long hash value for this board
         */                     
        public long hashValue() {
                long key = 0;

                key ^= this.trueTiles[0].hashValue();
                key ^= this.trueTiles[1].hashValue();
                key ^= this.sizeHash[0][this.size[0]];
                key ^= this.sizeHash[1][this.size[1]];
                key ^= this.turnHash[this.currentPlayer.ordinal()];

                DominoTile ends = this.getBoardEnds();
                if(!ends.isEmpty())
                        key ^= this.endsHash[ends.hashCode()];

                return key;
        }

        /**
         * Board status print (for debugging).
         */
        public void print() {
                System.out.println("\n********");
                System.out.println("True tiles FIRST : " +  this.trueTiles[0] + "\tPoints: " + this.trueTiles[0].getScore());
                System.out.println("True tiles SECOND: " +  this.trueTiles[1] + "\tPoints: " + this.trueTiles[1].getScore());
                System.out.println("\nBoard");
                for(DominoTile t: this.boardTiles)
                        System.out.print(t + " ");
                System.out.println("\n\nBoard ends\n" + this.getBoardEnds());
                System.out.println("\nHistory");
                for(DominoBoardHist h: this.moveHist)
                        System.out.println("Tile: " + h.tile + "\tPlayer: " + h.playerid + "\tTile position: " + h.side + "\tBoard ends: " + h.ends);
                System.out.println("********");
        }

        @Override
        public String toString() {
                return "P1 [" + this.size[0] + "]: " + this.trueTiles[0] + " P2 [" + this.size[1] + "]: " + this.trueTiles[1] + " BoardEnds: " + this.getBoardEnds() + " Turn: " + this.currentPlayer; 
        }

}
