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
 * Class representing a domino tile.
 * <p>
 * A domino tile is defined by two non-negative values, referred to as the left and right values.
 * Negative values are not allowed, except for a special case where both values represent an empty tile.
 * A tile with its left and right values swapped is considered equivalent to the original tile.
 * An empty tile is indicated when both values are set to {@link #EMPTYVALUE}.
 */
public class DominoTile {
        private int left;
        private int right;
        /** 
         * Constant representing the value for an empty tile.
         * An empty tile has both its left and right values set to this value.
         */
        public static final int EMPTYVALUE = -1;

        /**
         * Constructs an empty tile.
         * Both the left and right values of the tile will be set to {@link #EMPTYVALUE}.
         */
        public DominoTile() {
                this.left = this.right = EMPTYVALUE;
        }

        /**
         * Constructs a domino tile with specified left and right values.
         * 
         * @param left  the left value of the tile
         * @param right the right value of the tile
         * @throws IllegalArgumentException if the provided values are negative 
         *         (unless both values represent an empty tile, indicated by {@link #EMPTYVALUE}).
         */
        public DominoTile(int left, int right) throws IllegalArgumentException {
                if(!(left == EMPTYVALUE && right == EMPTYVALUE) && (left < 0 || right < 0))
                        throw new IllegalArgumentException("Tile values should be >= 0, except for the empty tile " + EMPTYVALUE+"|"+EMPTYVALUE);
                this.left  = left;
                this.right = right;
        }

        /**
         * Constructs a new domino tile by copying another tile.
         * 
         * @param tile the tile to be copied
         *             If the provided tile is null, an empty tile is created.
         */
        public DominoTile(DominoTile tile) {
                this.left  = tile == null ? EMPTYVALUE : tile.left();
                this.right = tile == null ? EMPTYVALUE : tile.right();
        }

        /**
         * Swaps left and right values of this tile.
         */
        public void swap() {
                int tmp    = this.left;
                this.left  = this.right;
                this.right = tmp;
        }

        /**
         * Returns true if this is an empty tile
         * @return true if this tile is empty
         */
        public boolean isEmpty() {
                return this.left == EMPTYVALUE;
        }

        /**
         * Returns true if this is a double tile (left and right values are the same)
         * @return true if this is a non-empty double tile
         */
        public boolean isDouble() {
                return this.left == this.right && this.left != EMPTYVALUE;
        }

        /**
         * Checks whether this tile matches a value
         * @param val value to be matched against this tile
         * @return true if the left or right value is equal to <code>val</code>
         */
        public boolean matches(int val) {
                return this.left == val || this.right == val;
        }

        /**
         * Checks whether this tile matches another tile
         * @param tile domino tile to be matched against this tile
         * @return true if the left or right value is equal to the left of right value of <code>tile</code>
         */
        public boolean matches(DominoTile tile) {
                return tile.matches(this.left) || tile.matches(this.right);
        }

        /**
         * Checks whether the left value of this tile matches a value
         * @param val value to be matched against the left value of this tile
         * @return true if the left value is equal to <code>val</code>
         */
        public boolean leftMatches(int val) {
                return this.left == val;
        }

        /**
         * Checks whether the left value of this tile matches another tile
         * @param tile domino tile to be matched against the left value of this tile
         * @return true if left value is equal to the left of right value of <code>tile</code>
         */
        public boolean leftMatches(DominoTile tile) {
                return tile.matches(this.left);
        }

        /**
         * Checks whether the right value of this tile matches a value
         * @param val value to be matched against the right value of this tile
         * @return true if the right value is equal to <code>val</code>
         */
        public boolean rightMatches(int val) {
                return this.right == val;
        }

        /**
         * Checks whether the right value of this tile matches another tile
         * @param tile domino tile to be matched against the right value of this tile
         * @return true if right value is equal to the left of right value of <code>tile</code>
         */
        public boolean rightMatches(DominoTile tile) {
                return tile.matches(this.right);
        }

        /**
         * Returns the left value of this tile
         * @return the left value of this tile
         */
        public int left() {
                return this.left;
        }

        /**
         * Returns the right value of this tile
         * @return the right value of this tile
         */
        public int right() {
                return this.right;
        }

        /**
         * Returns the sum of the left and right values of this tile
         * @return the sum of the left and right values
         */
        public int totValue() {
                return this.left + this.right;
        }

        /**
         * Returns the greater of the left and right values of this tile
         * @return the greater of the left and right values of this tile
         */
        public int maxValue() {
                return Math.max(this.left, this.right);
        }

        /**
         * Returns the smaller of the left and right values of this tile
         * @return the smaller of the left and right values of this tile
         */
        public int minValue() {
                return Math.min(this.left, this.right);
        }

        /**
         * Returns a copy of this tile
         * @return a copy of this tile
         */
        public DominoTile copy() {
                return new DominoTile(this.left, this.right);
        }

        @Override
        public String toString() {
                if(this.isEmpty())
                        return "-|-";
                else
                        return this.left + "|" + this.right;
        }

        @Override
        public boolean equals(Object o) {
                if(o != null && o instanceof DominoTile) {
                        return this.left == ((DominoTile)o).left()  && this.right == ((DominoTile)o).right() || 
                               this.left == ((DominoTile)o).right() && this.right == ((DominoTile)o).left();
                 } else {
                        return false;
                }
        }
        
        @Override
        public int hashCode() {
                //return (int)(Math.pow(2,this.minValue())*Math.pow(3, this.maxValue()));
                int x = this.minValue(), y = this.maxValue();
                return y*(y+1)/2+x;
        }
}
