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

/**
 * Class to manage the state and mechanics of a domino game with additional functionalities.
 * <p>
 * This class handles the players' tile sets, the board (represented as a sequence of played tiles),
 * the history of moves, and the current game state. It facilitates the gameplay by tracking each player's 
 * visible and hidden tiles, and managing the board where the tiles are placed in a "snake" format.
 * It also allows to play/unplay tiles in turn and check whether the played tiles are legal moves.
 * Keeping the visible tile set of each player updated may be expensive under many unplay operations.
 */
public class DominoBoardPlus extends DominoBoard {
        /** Candidate tile sets (the candidate set of first player is the set of tiles that the first player may own according to the second player, and viceversa) */
        protected DominoTileSet[]        candTiles;     
        /** Board view for each player (shows only the part of the board that is visibile to the respective player) */
        protected DominoBoardView[]      boardView;
        /** Number of tiles of each player */
        private int[] numOfTiles;

        /**
         * Constructs a new domino board with the initial tile sets for both players.
         * <p>
         * Initializes the game by setting up the true and candidate tile sets, an empty board for played tiles, 
         * an empty move history, and sets the first player to start the game.
         * 
         * @param set1 the domino tile set of the first player
         * @param set2 the domino tile set of the second player
         */
        public DominoBoardPlus(DominoTileSet set1, DominoTileSet set2) {
                super(set1,set2);
                this.setupLocalData();
        }

        /**
         * Constructs a new domino board with the initial tile sets for both players and an intial tile on the board.
         * 
         * @param set1 the domino tile set of the first player
         * @param set2 the domino tile set of the second player
         * @param startile the starting tile on the board
         */
        public DominoBoardPlus(DominoTileSet set1, DominoTileSet set2, DominoTile startile) {
                super(set1,set2,startile);
                this.setupLocalData();
        }

        private void setupLocalData() {
                DominoTileSet true1 = this.trueTiles[DominoPlayerId.FIRST.ordinal()];
                DominoTileSet true2 = this.trueTiles[DominoPlayerId.SECOND.ordinal()];
                this.numOfTiles = new int[]{true1.size(),true2.size()};
                this.candTiles  = new DominoTileSet[]{buildCandTileSet(true1,true2),buildCandTileSet(true2,true1)};

                DominoTileSet cand1 = this.candTiles[DominoPlayerId.FIRST.ordinal()];
                DominoTileSet cand2 = this.candTiles[DominoPlayerId.SECOND.ordinal()];
                this.boardView      = new DominoBoardView[2];
                this.boardView[DominoPlayerId.FIRST.ordinal()]  = new DominoBoardView(DominoPlayerId.FIRST,true1,cand2,boardTiles,moveHist,numOfTiles);
                this.boardView[DominoPlayerId.SECOND.ordinal()] = new DominoBoardView(DominoPlayerId.SECOND,true2,cand1,boardTiles,moveHist,numOfTiles);
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
         * Plays the specified <code>tile</code> for the current player.
         * <p>
         * The method checks whether the current player actually owns the
         * the specified tile and if it is a legal move.
         * @param tile tile to be played
         * @return domino game state (open or ended)
         * @throws IllegalArgumentException if the specified tile cannot be played
         */
        @Override
        public DominoGameState playTile(DominoTile tile) throws IllegalArgumentException {
                checkPlayedTile(tile);
                int id = this.currentPlayer.ordinal();
                DominoGameState state = super.playTile(tile);

                if(tile.isEmpty()) 
                        this.candTiles[id].removeMatches(this.getBoardEnds()); 
                else
                        this.candTiles[id].remove(tile);

                this.numOfTiles[id] = this.trueTiles[id].size();
                return state;
        }

        /**
         * Undoes the last move.
         * @throws IllegalArgumentException if there is no move to undo
         */
        @Override
        public void unplayTile() throws IllegalArgumentException {
                DominoBoardHist h = this.moveHist.peek();
                super.unplayTile();

                int id = h.playerid.ordinal();

                if(!h.tile.isEmpty())
                        this.candTiles[id].add(h.tile);
                else
                        this.candTiles[id] = rebuildCandTileSet(h.playerid);

                this.numOfTiles[id] = this.trueTiles[id].size();
        }

        private DominoTileSet buildCandTileSet(DominoTileSet set1, DominoTileSet set2) {
                int m = set1.getMaxValue();
                int n = Math.max(set1.getLargestDouble().maxValue(),set2.getLargestDouble().maxValue());
                DominoTileSet set = new DominoTileSet(m);
                for(int i = 0; i <= m; i++)
                        for(int j = i; j <= m; j++) {
                                DominoTile tile = new DominoTile(i,j);
                                if(!set2.contains(tile) && !(i == j && i>n))
                                        set.add(tile);
                        }
                return set;
        }


        private DominoTileSet rebuildCandTileSet(DominoPlayerId playerid) {
                int id1 = playerid.ordinal();
                int id2 = playerid.toggle().ordinal();
                int m  = this.trueTiles[id1].getMaxValue(); 
                int n  = this.moveHist.peekLast().playerid == null ? m : this.moveHist.peekLast().tile.maxValue();
                DominoTileSet set = new DominoTileSet(m);

                for(int i = 0; i <= m; i++)
                        for(int j = i; j <= m; j++) {
                                DominoTile tile = new DominoTile(i,j);
                                if(!this.trueTiles[id2].contains(tile) && !(i == j && i>n))
                                        set.add(new DominoTile(i,j));
                        }
                for(DominoBoardHist h: this.moveHist)
                        if(h.playerid == playerid && h.tile.isEmpty())
                                set.removeMatches(h.ends);
                        else
                                set.remove(h.tile);
                               
                return set;
        }

        /**
         * Returns a read-only view of the current board for the current player.
         * <p>
         * On a board view the current player can only check all the informations that
         * are visible to him (history, played tiles, number of tiles of the opponent, etc).
         * @param playerid identifier of the player that owns the view
         * @return a read-only view of the current board for the current player
         */
        public DominoBoardView getBoardView(DominoPlayerId playerid) {
                return boardView[playerid.ordinal()];
        }

        /**
         * Board status print (for debugging).
         */
        public void print() {
                System.out.println("\n********");
                System.out.println("True tiles FIRST : " +  this.trueTiles[0] + "\tPoints: " + this.trueTiles[0].getScore());
                System.out.println("True tiles SECOND: " +  this.trueTiles[1] + "\tPoints: " + this.trueTiles[1].getScore());
                System.out.println("Cand tiles FIRST : " +  this.candTiles[0]);
                System.out.println("Cand tiles SECOND: " +  this.candTiles[1]);
                System.out.println("\nBoard");
                for(DominoTile t: this.boardTiles)
                        System.out.print(t + " ");
                System.out.println("\n\nBoard ends\n" + this.getBoardEnds());
                System.out.println("\nHistory");
                for(DominoBoardHist h: this.moveHist)
                        System.out.println("Tile: " + h.tile + "\tPlayer: " + h.playerid + "\tTile position: " + h.side + "\tBoard ends: " + h.ends);
                System.out.println("********");
        }
}
