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
 * Implements a domino player that selects moves according to a greedy strategy.
 * <p>
 * This player chooses a the largest score tile from the set of playable tiles.
 */
public class GreedyPlayer implements DominoPlayer {
        private DominoTileSet set;

        /** Empty constructor */
        public GreedyPlayer() {
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id) {
                this.set = set.copy();
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id, boolean verbose) {
                this.initPlayer(set,id);
        }

        /**
         * Selects the largest score tile from the set of playable tiles.
         * @param B current state of the domino board
         * @return the domino tile to play
         */
        public DominoTile selectTile(DominoBoardView B) {
                DominoTile endstile = B.getBoardEnds();
                DominoTile tile     = new DominoTile();
                
                for(DominoTile t: B.getPlayableTiles())
                        if(greedyStrategy(t,tile,B.getPlayerTileSet()))
                                tile = t;
                

                return tile;
        }

        private boolean greedyStrategy(DominoTile tile1, DominoTile tile2, DominoTileSet set) {
                int n = tile1.totValue() - tile2.totValue();
                if(n > 0) return true;
                if(n < 0) return false;

                n = set.matchesCount(tile1.right()) - set.matchesCount(tile2.right());

                if(n > 0) return true;
                if(n < 0) return false;

                n = tile2.right() - tile1.right();
                
                if(n > 0) return true;
                
                return false;
        }

        public String getName() {
                return "Greedy";
        }
}
