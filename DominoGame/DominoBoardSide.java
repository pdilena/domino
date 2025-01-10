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
 * Domino board positions for a tile.
 */ 
public enum DominoBoardSide {
        /** First tile played */
        CENTER,
        /** Tile placed on the left */
        LEFT,
        /** Tile placed on the right */
        RIGHT,
        /** Tile non played (i.e. empty tile) */
        UNPLACED;
}
