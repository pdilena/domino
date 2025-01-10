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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Random;

/**
 * Class representing a domino tile set.
 * <p>
 * A domino tile set is defined by a collection of tiles. It cannot contain
 * duplicated tiles or empty tiles.
 */
public class DominoTileSet {
        private HashSet<DominoTile> set;       // Set of tiles
        private int[]               count;     // Keeps tracks of the values in the set
        private int                 maxvalue;  // Maximum possible value for this set 
        private int                 score;     // Total set score
        private long[]              hash;      // Random hash codes for Zobrist hashing
        private long                key;       // hash code for this set

        /**
         * Creates an empty tile set.
         * @param maxvalue maximum value for a tile in this set
         * @throws IllegalArgumentException if <code>maxvalue</code> is a negative value
         */
        public DominoTileSet(int maxvalue) throws IllegalArgumentException {
                if(maxvalue < 0) {
                        throw new IllegalArgumentException(maxvalue + " is not a valid maximum value for a tile");
                } else {
                        this.maxvalue = maxvalue;
                        this.set      = new HashSet<DominoTile>();
                        this.count    = new int[maxvalue + 1];
                        this.score    = 0;
                        this.hash     = new long[(maxvalue+1)*(maxvalue+2)/2];
                        this.key      = 0;
                        
                        // maxvalue used as seed: tile sets sharing wthe same
                        // tiles will be considered equal only if their
                        // maxvalue is the same, too
                        Random rand = new Random(maxvalue);
                        for(int i = 0; i < this.hash.length; i++)
                                this.hash[i] = rand.nextLong();
                }
        }

        /**
         * Creates a tile set collecting the tiles contained into an iterable object.
         * @param maxvalue maximum value for a tile in this set
         * @param L iterable object containing domino tiles
         * @throws IllegalArgumentException if <code>maxvalue</code> is a negative value of if some tile in <code>L</code> has a value larger than <code>maxvalue</code>
         */
        public DominoTileSet(int maxvalue, Iterable<DominoTile> L) throws IllegalArgumentException {
                this(maxvalue);
                for(DominoTile t: L)
                        this.addTile(t.left(),t.right());
        }

        /**
         * Creates a tile set collecting the tiles contained into an array.
         * @param maxvalue maximum value for a tile in this set
         * @param L array containing domino tiles
         * @throws IllegalArgumentException if <code>maxvalue</code> is a negative value of if some tile in <code>L</code> has a value larger than <code>maxvalue</code>
         */
        public DominoTileSet(int maxvalue, DominoTile[] L) throws IllegalArgumentException {
                this(maxvalue);
                for(DominoTile t: L)
                        this.addTile(t.left(),t.right());
        }

        /**
         * Returns a list of tiles from this set whose left value matches the (left or right value) of the provided <code>tile</code> parameter.
         * <p>
         * If the input <code>tile</code> is an empty tile (i.e., both left and right values are {@link DominoTile#EMPTYVALUE}), 
         * this method returns the largest double tile (if one exists) from this set. If no matching tile is found in the set, 
         * the output list will contain only an empty tile. For non-double tiles in this set such that both left and right values
         * match the provided <code>tile</code>, the method will include two copies of the matching tile: the original tile and
         * its swapped version (where left and right values are reversed).
         * If the input <code>tile</code> is not a double tile and this set contains a copy of <code>tile</code> (both left and right values match), 
         * that tile (and its swapped version) will be placed at the beginning of the output list.
         * 
         * @param tile the domino tile to match against the tiles in this set
         * @return a list of tiles whose left value match the input tile, or the empty tile
         */
        public LinkedList<DominoTile> matchingTiles(DominoTile tile) {
                LinkedList<DominoTile> L = new LinkedList<DominoTile>();

                if (tile == null || tile.isEmpty()) {
                        L.add(this.getLargestDouble());
                } else if (!this.matches(tile)) {
                        L.add(new DominoTile());
                } else {
                        for(DominoTile t: this.set) {
                                if(!tile.isDouble() && t.equals(tile)) {
                                        L.addFirst(new DominoTile(t.right(),t.left())); 
                                        L.addFirst(new DominoTile(t));
                                } else if(t.leftMatches(tile)) {
                                        L.add(new DominoTile(t));
                                } else if(t.rightMatches(tile)) {
                                        L.add(new DominoTile(t.right(),t.left()));
                                }
                        }
                }

                return L;
        }

        
        
        /**
         * Checks whether this set contains a tile matching the <code>tile</code> parameter.
         * @param tile the domino tile to match against the tiles in this set
         * @return true is this set contain a tile matching <code>tile</code>
         */
        public boolean matches(DominoTile tile) {
                return this.matches(tile.left()) || this.matches(tile.right());
        }

        /**
         * Checks whether this set contains a tile matching the <code>val</code> parameter.
         * @param val the value to match against the tiles in this set
         * @return true is this set contain a tile matching <code>val</code>
         */
        public boolean matches(int val) {
                return val >= 0 && val <= this.maxvalue && this.count[val] > 0;
        }

        /**
         * Returns the number of tiles matching the <code>val</code> parameter.
         * @param val the value to match against the tiles in this set
         * @return the number of tiles matching <code>val</code>
         */
        public int matchesCount(int val) {
                if(val < 0 || val > this.maxvalue || this.count[val] == 0)
                        return 0;
                else
                        return this.count[val]-(this.contains(new DominoTile(val,val)) ? 1 : 0);
        }

        /**
         * Adds to this set the tiles in the interable object <code>L</code>.
         * <p>
         * The set does not allow duplicated tiles, empty tiles and tiles whose
         * maximum value is larger than the <code>maxvalue</code> constructor parameter. 
         * @param L iterable object containing domino tiles
         * @throws IllegalArgumentException if <code>L</code> contains a tile that cannot be added to this set
         */
        public void add(Iterable<DominoTile> L) throws IllegalArgumentException {
                for(DominoTile t: L)
                        this.addTile(t.left(),t.right());
        }

        /**
         * Adds to this set the tiles in the array <code>L</code>.
         * <p>
         * The set does not allow duplicated tiles, empty tiles and tiles whose
         * maximum value is larger than the <code>maxvalue</code> constructor parameter. 
         * @param L array containing domino tiles
         * @throws IllegalArgumentException if <code>L</code> contains a tile that cannot be added to this set
         */
        public void add(DominoTile[] L) throws IllegalArgumentException {
                for(DominoTile t: L)
                        this.addTile(t.left(),t.right());
        }

        /**
         * Adds to this set the specified <code>tile</code>.
         * <p>
         * The set does not allow duplicated tiles, empty tiles and tiles whose
         * maximum value is larger than the <code>maxvalue</code> constructor parameter. 
         * @param tile a domino tile
         * @throws IllegalArgumentException if <code>tile</code> cannot be added to this set
         */
        public void add(DominoTile tile) throws IllegalArgumentException {
                if(tile == null) {
                        throw new IllegalArgumentException("Not a valid tile");
                } else {
                        this.addTile(tile.left(), tile.right());
                }
        }

        private void addTile(int left, int right) throws IllegalArgumentException {
                if(left < 0 || right < 0 || left > this.maxvalue || right > this.maxvalue) 
                        throw new IllegalArgumentException("Not valid tile values for this set: " + (new DominoTile(left,right)));

                DominoTile tile = new DominoTile(left,right);
                if(this.set.add(tile)) {
                        this.count[left]++;
                        this.count[right]++;
                        this.score += left+right;
                        this.key ^= this.hash[tile.hashCode()];
                } else {
                        throw new IllegalArgumentException("Warning: the set already contains tile " + tile + "(the tile will not be duplicated)");
                        //System.err.println("Warning: the set already contains tile " + (new DominoTile(left,right)) + "(the tile will not be duplicated)");
                } 
        }

        /**
         * Removes the specified <code>tile</code> from this set, if it is present. 
         * @param tile the domino tile to be removed from this set
         */
        public void remove(DominoTile tile) {
                if(this.set.remove(tile)) {
                        this.count[tile.left()]--;
                        this.count[tile.right()]--;
                        this.score -= tile.totValue();
                        this.key ^= this.hash[tile.hashCode()];
                }
        }

        /**
         * Removes all the tiles specified in the iterable object <code>L</code> if present in this set.
         * @param L list of domino tiles to be removed from this set
         */
        public void remove(Iterable<DominoTile> L) {
                for(DominoTile t: L)
                        this.remove(t);
        }

        /**
         * Removes all the tiles specified in the array <code>L</code> if present in this set.
         * @param L list of domino tiles to be removed from this set
         */
        public void remove(DominoTile[] L) {
                for(DominoTile t: L)
                        this.remove(t);
        }


        /**
         * Removes and returns all the tiles matching the specfied <code>val</code> from this set.
         * @param val the value to be matched against the tiles in this set
         * @return list of removed tiles
         */
        public LinkedList<DominoTile> removeMatches(int val) {
                LinkedList<DominoTile> L = new LinkedList<DominoTile>();
                if (this.matches(val)) {
                        Iterator<DominoTile> iter = this.set.iterator();

                        while(iter.hasNext()) {
                                DominoTile t = iter.next();
                                if (t.matches(val))
                                        L.add(t);
                        }
                        this.remove(L);
                }
                return L;
        }


        /**
         * Removes and returns all the tiles matching the specfied <code>tile</code> from this set.
         * @param tile the domino tile to be matched against the tiles in this set
         * @return list of removed tiles
         */
        public LinkedList<DominoTile> removeMatches(DominoTile tile) {
                LinkedList<DominoTile> L = new LinkedList<DominoTile>();
                if (this.matches(tile)) {
                        Iterator<DominoTile> iter = this.set.iterator();

                        while(iter.hasNext()) {
                                DominoTile t = iter.next();
                                if (t.matches(tile))
                                        L.add(t);
                        }
                        this.remove(L);
                }
                return L;
        }


        /**
         * Returns the number of tiles in this set.
         * @return the number of tiles in this set
         */
        public int size() {
                return this.set.size();
        }

        /**
         * Checks whether this set contains the specified <code>tile</code> parameter.
         * @param tile the domino tile to search
         * @return true if this set contains the the specified <code>tile</code>
         */
        public boolean contains(DominoTile tile) {
                return this.set.contains(tile);
        }


        /**
         * Returns the largest double tile in this set or the empty tile.
         * @return the largest double tile in this set or the empty tile
         */
        public DominoTile getLargestDouble() {
                DominoTile tile = new DominoTile();
                for(DominoTile t: this.set)
                        if(t.isDouble() && tile.maxValue() < t.maxValue())
                                tile = new DominoTile(t);
                return tile; 
        }

        /**
         * Returns the sum of the individual scores of all the tiles in this set.
         * @return the sum of the individual scores of all the tiles in this set
         */
        public int getScore() {
                /*
                int score = 0;
                for(DominoTile t: this.set)
                        score += t.totValue();
                return score;
                */
                return this.score;
        }

        /**
         * Returns the sum of the scores of the smallest <code>n</code> tiles in this set, 
         * based on their total value (sum of left and right values).
         * <p>
         * The tiles are sorted in ascending order of their total value, and the scores 
         * of the first <code>n</code> tiles are summed. If the set contains fewer than 
         * <code>n</code> tiles, the method sums the scores of all the tiles in the set.
         * 
         * @param n the number of tiles to consider for the score calculation
         * @return the sum of the scores of the smallest <code>n</code> tiles in this set
         */
        public int getMinScore(int n) {
                int score = 0;
                DominoTile[] A = this.toArray();

                Arrays.sort(A,new compmin());

                for(int i = 0; i < Math.min(n,A.length); i++)
                        score += A[i].totValue();

                return score;
        }


        /**
         * Returns the sum of the scores of the largest <code>n</code> tiles in this set, 
         * based on their total value (sum of left and right values).
         * <p>
         * The tiles are sorted in descending order of their total value, and the scores 
         * of the first <code>n</code> tiles are summed. If the set contains fewer than 
         * <code>n</code> tiles, the method sums the scores of all the tiles in the set.
         * 
         * @param n the number of tiles to consider for the score calculation
         * @return the sum of the scores of the largest <code>n</code> tiles in this set
         */
        public int getMaxScore(int n) {
                if(n == 0) {
                        return 0;
                } else if (n == 1) {
                        int score = 0;
                        for(DominoTile t: this.set)
                                if(score < t.totValue())
                                        score = t.totValue();
                        return score;
                } else if(n >= this.set.size()) {
                        return this.getScore();
                } else {
                        int score = 0;
                        DominoTile[] A = this.toArray();

                        Arrays.sort(A,new compmax());

                        for(int i = 0; i < n; i++)
                                score += A[i].totValue();

                        return score;
                }
        }

        /**
         * Returns an array containing all the tiles in this set.       
         * @return an array containing all the tiles in this set
         */
        public DominoTile[] toArray() {
                DominoTile[] A = new DominoTile[this.set.size()];
                int i = 0;
                for(DominoTile t: this.set)
                        A[i++] = new DominoTile(t);
                //Arrays.sort(A, new compmin());
                return A;
        }

        /**
         * Returns the maximum allowed (left or right) value for a tile in this set.
         * @return the maximum allowed (left or right) value for a tile in this set
         */
        public int getMaxValue() {
                return this.maxvalue;
        }

        @Override
        public String toString() {
                String str = "";
                DominoTile[] A = this.toArray();
                Arrays.sort(A, new stdcmp());
                for(DominoTile t: A) {
                        if(t.left() > t.right()) t.swap();
                        str = str + " " + t;
                }
                return str.trim();
        }

        /**
         * Returns a copy of this set
         * @return a copy of this set
         */
        public DominoTileSet copy() {
                return new DominoTileSet(this.maxvalue,this.set);
        }

        /**
         * Returns a long hash value for this set
         * @return a long hash value for this set
         */
        public long hashValue() {
                return this.key;
        }


        private class compmin implements Comparator<DominoTile> {

                public int compare(DominoTile t1, DominoTile t2) {
                        return t1.totValue() - t2.totValue();
                }
        }

        private class compmax implements Comparator<DominoTile> {

                public int compare(DominoTile t1, DominoTile t2) {
                        return t2.totValue() - t1.totValue();
                }
        }

        private class stdcmp implements Comparator<DominoTile> {

                public int compare(DominoTile t1, DominoTile t2) {
                        return t1.hashCode() - t2.hashCode();
                }
        }
  
}
