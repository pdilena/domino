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

import java.util.Deque;
import java.util.LinkedList;

/**
 * Class representing a view of the current state of the domino game, providing access to each player's 
 * visible information.
 */
public class DominoBoardView {
        /** Player id (first or second) of the player that owns this view */
        private DominoPlayerId playerid; 
        /** The true tile set of the player that owns this view. */
        private final DominoTileSet          trueTiles;  
        /** The visible (candidate) tile sets for the opponent of the player that owns this view. */
        private final DominoTileSet          candTiles;  
        /** The tiles that have already been played, arranged in a "snake" on the board. */
        private final Deque<DominoTile>      boardTiles; 
        /** The history of all moves made during the game. */
        private final Deque<DominoBoardHist> moveHist;   
        /** Number of tiles of each player */
        private int[] numOfTiles;

        /**
         * Constructs a new view of the domino board with the specified tile sets, board tiles, and move history.
         * <p>
         * A board view should contain only information visible to one of the two players, not both;
         * 
         * @param playerid identifier of the player that owns the view
         * @param trueTiles the true tile sets
         * @param candTiles  the candidate tile sets
         * @param boardTiles the tiles already played on the board
         * @param moveHist the history of all moves made so far
         * @param numOfTiles number of tiles each player can still play
         */
        public DominoBoardView(DominoPlayerId playerid, DominoTileSet trueTiles, DominoTileSet candTiles, Deque<DominoTile> boardTiles, Deque<DominoBoardHist> moveHist, int[] numOfTiles) {
                this.playerid   = playerid;
                this.trueTiles  = trueTiles;
                this.candTiles  = candTiles;
                this.boardTiles = boardTiles;
                this.moveHist   = moveHist;
                this.numOfTiles = numOfTiles;
        }

        /**
         * Returns the number of tiles in the specified player's true tile set.
         * 
         * @param id the identifier for the player
         * @return the number of tiles in the player's true tile set
         */
        public int getNumOfTiles(DominoPlayerId id) {
                return this.numOfTiles[id.ordinal()];
        }

        /**
         * Returns the candidate tile set of the opponent player (i.e., not the player that owns this view)
         * @return an array of tiles in the player's visible (candidate) tile set
         */
        public DominoTile[] getOpponentCandTileSet() {
                return this.candTiles.toArray();
        }

        /**
         * Returns the tiles of the player that owns this view.
         * @return an array of tiles of the player that owns this view
         */
        public DominoTile[] getPlayerTiles() {
                return this.trueTiles.toArray();
        }

        /**
         * Returns the true tile set of the player that owns this view.
         * @return the tile set of the player that owns this view
         */
        public DominoTileSet getPlayerTileSet() {
                return this.trueTiles.copy();
        }

        /**
         * Returns the identifier of  player that owns this view.
         * @return the identifier of  player that owns this view
         */
        public DominoPlayerId getPlayerId() {
                return this.playerid;
        }

        /**
         * Returns the tiles currently placed on the board as an array.
         * 
         * @return an array of tiles representing the current state of the board
         */
        public DominoTile[] getBoardTiles() {
                return (DominoTile[])this.boardTiles.toArray();
        }

        /**
         * Returns the list of tiles that can be legally played by the player that owns this view
         * @return list of of tiles that can be legally played by the player that owns this view
         */
        public LinkedList<DominoTile> getPlayableTiles() {
                return this.trueTiles.matchingTiles(this.getBoardEnds());
        }


        /**
         * Returns the history of all moves made during the game as an array.
         * 
         * @return an array representing the history of all moves
         */
        public DominoBoardHist[] getMoveHist() {
                DominoBoardHist[] H = new DominoBoardHist[this.moveHist.size()];
                int i = 0;
                for(DominoBoardHist h: this.moveHist)
                        H[i++] = new DominoBoardHist(h.side,h.playerid,h.tile,h.ends);
                return H; 
        }

        /**
         * Returns the identifier of the current player. If no moves have been made, the first player starts.
         * @return the identifier of the current player
         */
        public DominoPlayerId getCurrentPlayer() {
                if(this.moveHist.size() == 0)
                        return DominoPlayerId.FIRST;
                else
                        return this.moveHist.peek().playerid.toggle();
        }
        
        /**
         * Returns the current ends of the domino "snake" on the board.
         * <p>
         * If no tiles have been placed yet, an empty tile is returned.
         * 
         * @return a tile representing the current ends of the snake, or an empty tile if no tiles are placed
         */
        public DominoTile getBoardEnds() {
                if(this.moveHist.size() == 0)
                        return new DominoTile();
                else
                        return new DominoTile(this.moveHist.peek().ends);
        }

        /**
         * Checks if the last turn was a pass (i.e., no tile was placed).
         * 
         * @return <code>true</code> if the last turn was a pass, <code>false</code> otherwise
         */
        public boolean wasLastTurnPass() {
                return this.moveHist.size() > 0 && this.moveHist.peek().side == DominoBoardSide.UNPLACED;
        }

        /**
         * Returns the last tile that was played on the board, or an empty tile if no tile was played.
         * 
         * @return the last played tile, or an empty tile if no tile was played
         */
        public DominoTile getLastPlayedTile() {
                return this.moveHist.size() > 0 ? new DominoTile(this.moveHist.peek().tile) : new DominoTile(); 
        }

}
