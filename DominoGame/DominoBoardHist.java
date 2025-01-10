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

/**
 * Class representing an entry in the Domino board history
 */
public class DominoBoardHist {
        /** Where the played tile has been placed on the board */
        final DominoBoardSide side;
        /** Player that owns the played tile */
        final DominoPlayerId  playerid;
        /** Played tile */
        final DominoTile      tile;
        /** Board ends tile */
        final DominoTile      ends;

        /**
         * Build a Domino board history entry
         * @param side tile position on the board
         * @param playerid identifier of the player that played the tile
         * @param tile last played domino tile 
         * @param ends current board ends after the tile has been played
         */
        public DominoBoardHist(DominoBoardSide side, DominoPlayerId playerid, DominoTile tile, DominoTile ends) {
                this.side     = side;
                this.playerid = playerid;
                this.tile     = tile.copy();
                this.ends     = ends.copy();
        }
}
