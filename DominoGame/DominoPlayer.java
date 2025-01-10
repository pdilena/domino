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
 * Interface for a generic Domino Player
 */
public interface DominoPlayer {

        /**
         * Initialize the player.
         * @param set domino tile set for this player
         * @param id player identifier (first or second)
         */
        void initPlayer(DominoTileSet set, DominoPlayerId id);

        /**
         * Initialize the player.
         * @param set domino tile set for this player
         * @param id player identifier (first or second)
         * @param verbose if true the player is allowed to print at terminal
         */
        void initPlayer(DominoTileSet set, DominoPlayerId id, boolean verbose);

        /**
         * Selects a tile to play
         * @param B current state of the domino board
         * @return the domino tile to play
         */
        public DominoTile selectTile(DominoBoardView B);


        /**
         * Returns the name of this player
         * @return string representing the name of this player
         */
        public String getName();
}
