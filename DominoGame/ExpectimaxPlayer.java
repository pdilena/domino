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
import java.util.LinkedList;
import java.util.HashMap;
import java.text.DecimalFormat;

/**
 * Implements a realistic domino player that selects moves based on the expectimax strategy.
 * <p>
 * This player chooses the tile that maximizes the expected outcome, 
 * considering all possible sets of tiles the opponent could hold.
 */
public class ExpectimaxPlayer implements DominoPlayer {
        private DominoPlayerId myid;
        private DominoPlayerId opid;
        private boolean verbose;
        private Type type;
        private DecimalFormat F;

        /** Empty constructor */
        public ExpectimaxPlayer() {
                this.type = Type.EXPECTIMAXTT;
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id) {
                this.myid    = id;
                this.opid    = id.toggle();
                this.verbose = false;
                this.F       = new DecimalFormat("#0.000");
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id, boolean verbose) {
                this.initPlayer(set,id);
                this.verbose = verbose;
        }

        /**
         * From the set of playable tile selects the tile maximizing the expected outcome, considering all possible sets of tiles the opponent could hold. 
         * @param B current state of the domino board
         * @return the domino tile to play
         */
        public DominoTile selectTile(DominoBoardView B) {
                DominoTile playtile      = new DominoTile();
                LinkedList<DominoTile> L = B.getPlayableTiles();

                if (L.size() == 1) {
                        playtile = L.removeFirst();
                } else {
                        DominoTile endstile     = B.getBoardEnds();
                        DominoTileSet myset     = B.getPlayerTileSet();
                        DominoTileSet opset     = new DominoTileSet(myset.getMaxValue(),B.getOpponentCandTileSet());
                        int mysize              = B.getNumOfTiles(this.myid);
                        int opsize              = B.getNumOfTiles(this.opid);

                        if(verbose) {
                                System.out.println("\nOpponent's candidate set: " + opset + " Size: " + opsize);
                                System.out.println("My true set: " + myset + " Size: " + mysize);
                                System.out.print("\nMy playable tiles: ");
                                for(DominoTile t: L)
                                        System.out.print(" " + t);
                                System.out.println();
                        }

                        if(this.type == Type.EXPECTIMAX)
                                playtile = this.expectimaxSelect(myset,opset,endstile,opsize);
                        else
                                playtile = this.expectimaxttSelect(myset,opset,endstile,mysize,opsize);
                }

                return playtile;
        }

        private DominoTile greedySelect(DominoTile tile1, DominoTile tile2, DominoTileSet set) {
                int n = tile1.totValue() - tile2.totValue();
                if(n > 0) return tile1;
                if(n < 0) return tile2;

                n = set.matchesCount(tile1.right()) - set.matchesCount(tile2.right());

                if(n > 0) return tile1;
                if(n < 0) return tile2;

                n = tile2.right() - tile1.right();

                if(n > 0) return tile1;

                return tile2;
        }

        private DominoTile expectimaxSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int opsize) {
                DominoTile tile  = new DominoTile();
                double bestscore = -Double.MAX_VALUE;
                DominoBoard C    = new DominoBoard(myset,opset,endstile);

                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        double score = this.expectimax(C,opsize);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + F.format(score));
                        if(bestscore < score || (bestscore == score && greedySelect(t,tile,myset) == t)) {
                                bestscore = score;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + F.format(bestscore));
                return tile;
        }

        private DominoTile expectimaxttSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int mysize, int opsize) {
                DominoTile       tile  = new DominoTile();
                double       bestscore = -Double.MAX_VALUE;
                DominoBoard          C = new DominoBoard(myset,opset,endstile,mysize,opsize);
                HashMap<Long,Double> T = new HashMap<>(); // Transposition Table

                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        double score = this.expectimaxtt(C,opsize,T);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + F.format(score));
                        if(bestscore < score || (bestscore == score && greedySelect(t,tile,myset) == t)) {
                                bestscore = score;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + F.format(bestscore));
                return tile;
        }

        

        public String getName() {
                if(this.type == Type.EXPECTIMAX)
                        return "ExpectiMax";
                else
                        return "ExpectiMaxTT";
        }

        private double expectimax(DominoBoard C, int size) {
                if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        return getAvgScore(C.trueTiles[0],C.trueTiles[1],size);
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        double score = -Double.MAX_VALUE;
                        for(DominoTile t: C.getCurrentPlayerMoves()) {
                                C.playTile(t);
                                score = Math.max(score,expectimax(C,size));
                                C.unplayTile();
                        }
                        return score;
                } else {
                        double score = 0;
                        int       id = DominoPlayerId.SECOND.ordinal();
                        LinkedList<DominoTileProb> P = getDominoTileProb(C,size);
                        LinkedList<DominoTile>     L = new LinkedList<DominoTile>();
                        
                        double currprob = 0;
                        for(DominoTileProb p: P) {              
                                if(p.tile.isEmpty() && P.size() > 1) L = C.trueTiles[id].removeMatches(C.getBoardEnds());
                                C.playTile(p.tile);
                                score += p.prob*expectimax(C,(p.tile.isEmpty() ? size : size-1));
                                C.unplayTile();
                                if(p.tile.isEmpty()) C.trueTiles[id].add(L);
                                currprob += p.prob;    
                        }
                        // Only for debugging
                        if(Math.abs(currprob-1.0) > 1e-12) {
                                System.out.print("P:");
                                for(DominoTileProb p: P)
                                        System.out.print(" (" + p.tile + "," + p.prob + ")");
                                System.out.println();
                                throw new RuntimeException("The probabilities do not sum to 1.0. Computed value = " + currprob);
                        }

                        return score;
                }
        }

        private double expectimaxtt(DominoBoard C, int size, HashMap<Long,Double> T) {
                long     key = C.hashValue();
                Double score = T.get(key); 

                if(score != null) {
                        return score;
                } else if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        score = getAvgScore(C.trueTiles[0],C.trueTiles[1],size);
                        T.put(key,score);
                        return score;
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        score = -Double.MAX_VALUE;
                        for(DominoTile t: C.getCurrentPlayerMoves()) {
                                C.playTile(t);
                                score = Math.max(score,expectimax(C,size));
                                C.unplayTile();
                        }
                        T.put(key,score);
                        return score;
                } else {
                        score = 0.0;
                        int       id = DominoPlayerId.SECOND.ordinal();
                        LinkedList<DominoTileProb> P = getDominoTileProb(C,size);
                        LinkedList<DominoTile>     L = new LinkedList<DominoTile>();

                        double currprob = 0;
                        for(DominoTileProb p: P) {
                                if(p.tile.isEmpty() && P.size() > 1) L = C.trueTiles[id].removeMatches(C.getBoardEnds());
                                C.playTile(p.tile);
                                score += p.prob*expectimax(C,(p.tile.isEmpty() ? size : size-1));
                                C.unplayTile();
                                if(p.tile.isEmpty()) C.trueTiles[id].add(L);
                                currprob += p.prob;    
                        }
                        // Only for debugging
                        if(Math.abs(currprob-1.0) > 1e-12) {
                                System.out.print("P:");
                                for(DominoTileProb p: P)
                                        System.out.print(" (" + p.tile + "," + p.prob + ")");
                                System.out.println();
                                throw new RuntimeException("The probabilities do not sum to 1.0. Computed value = " + currprob);
                        }
                        T.put(key,score);
                        return score;
                }
        }

        

        private LinkedList<DominoTileProb> getDominoTileProb(DominoBoard C, int setSize) {
                LinkedList<DominoTileProb>  P = new LinkedList<DominoTileProb>();
                DominoTile           endstile = C.getBoardEnds();

                LinkedList<DominoTile>     L  = C.getCurrentPlayerMoves();
                int doubleSize = !L.peekFirst().isDouble() && L.peekFirst().equals(endstile) ? 1 : 0;
                int singleSize = L.peekFirst().isEmpty() ? 0 : L.size() - 2*doubleSize;
                int candSize   = C.getNumOfTiles(DominoPlayerId.SECOND);

                double singleProb = getTilePlayProb(true,singleSize,doubleSize,setSize,candSize);
                double doubleProb = getTilePlayProb(false,singleSize,doubleSize,setSize,candSize);

                for(DominoTile t: L) {
                        if(!t.isDouble() && t.equals(endstile))
                                P.add(new DominoTileProb(t,doubleProb));
                        else if(!t.isEmpty())
                                P.add(new DominoTileProb(t,singleProb));
                }

                if(candSize-singleSize-doubleSize >= setSize) {
                        double passProb = 1.0*choose(candSize-singleSize-doubleSize,setSize)/choose(candSize,setSize);
                        P.add(new DominoTileProb(new DominoTile(),passProb));
                }

                return P;        
        }

        private double getTilePlayProb(boolean single, int singleSize, int doubleSize, int setSize, int candSize) {
                double n = choose(candSize,setSize);
                double p = 0;

                int nomatchSize = candSize-singleSize-doubleSize;
                if(single) singleSize--;
                else       doubleSize--;
                setSize--;

                for(int i = 0; i <= singleSize; i++) {
                        for(int j = 0; j <= doubleSize && i+j <= setSize; j++) {
                                double set_prob  = choose(nomatchSize,setSize-i-j)*choose(singleSize,i)*choose(doubleSize,j)/n;
                                double pick_prob = 1.0/((single ? 1:2)+i+2*j);
                                p += set_prob*pick_prob;
                         }
                }
                return p;
        }

        private double getAvgScore(DominoTileSet set1, DominoTileSet set2, int size) {
                double scorefirst  = set1.getScore();
                double scoresecond = size == 0 ? 0 : 1.0*set2.getScore()*size/set2.size();
                return scoresecond-scorefirst;
        }

        private int choose(int n, int k) {
                if (k < 0 || k > n) return 0;
                if (k > n/2) //choose(n,k) == choose(n,n-k), 
                        k = n - k;
        
                double den = 1.0, num = 1.0;
                for (int i = 1; i <= k; i++) {
                        den *= i;
                        num *= (n + 1 - i);
                }
                return (int)(num/den);
        }

        private class DominoTileProb {
                DominoTile tile;
                double     prob;

                public DominoTileProb(DominoTile tile, double prob) {
                        this.tile = new DominoTile(tile);
                        this.prob = prob;
                }
        }

        private enum Type {
                EXPECTIMAX,
                EXPECTIMAXTT;
        }     
                
}
