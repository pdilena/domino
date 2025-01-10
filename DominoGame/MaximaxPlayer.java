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

/**
 * Implements an optimistic domino player that selects moves based on the maximax strategy.
 * <p>
 * This player chooses the tile that maximizes the maximum potential gain, 
 * considering all possible sets of tiles the opponent could hold.
 * It employs a pruning strategy to accelerate the search process.
 */
public class MaximaxPlayer implements DominoPlayer {
        private DominoPlayerId myid;
        private DominoPlayerId opid;
        private int currbestscore; // For debug only
        private boolean verbose;
        private Type type;

        /** Empty constructor */
        public MaximaxPlayer() {
                this.type = Type.MAXIMAXTT;
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id) {
                this.myid          = id;
                this.opid          = id.toggle();
                this.currbestscore = Integer.MAX_VALUE;
                this.verbose       = false;
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id, boolean verbose) {
                this.initPlayer(set,id);
                this.verbose = verbose;
        }

        /**
         * From the set of playable tile selects the tile maximizing the maximum potential gain, considering all possible sets of tiles the opponent could hold. 
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
                
                        if(this.type == Type.MAXIMAX)
                                playtile = this.maximaxSelect(myset,opset,endstile,opsize);
                        else if(this.type == Type.MAXIMAXPR)
                                playtile = this.maximaxprSelect(myset,opset,endstile,opsize);
                        else
                                playtile = this.maximaxttSelect(myset,opset,endstile,mysize,opsize);
                }

                return playtile;
        }

        private DominoTile maximaxSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int opsize) {
                DominoTile tile = new DominoTile();
                int bestscore   = Integer.MIN_VALUE;
                DominoBoard C   = new DominoBoard(myset,opset,endstile);
                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        int score = this.maximax(C,opsize);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + score);
                        if(bestscore < score || (bestscore == score && greedySelect(t,tile,myset) == t)) {
                                bestscore = score;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + bestscore);
                // Only for debugging
                if(bestscore > this.currbestscore)
                        throw new RuntimeException("Error: the current best score " + bestscore + " is greater than the previous one " + currbestscore);
                else
                        this.currbestscore = bestscore;

                return tile;
        }

        private DominoTile maximaxprSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int opsize) {
                DominoTile tile = new DominoTile();
                int bestscore   = Integer.MIN_VALUE;
                DominoBoard C   = new DominoBoard(myset,opset,endstile);

                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        int score = this.maximaxpr(C,opsize,bestscore);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + score);
                        if(bestscore < score || (bestscore == score && greedySelect(t,tile,myset) == t)) {
                                bestscore = score;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + bestscore);
                // Only for debugging
                if(bestscore > this.currbestscore)
                        throw new RuntimeException("Error: the current best score " + bestscore + " is greater than the previous one " + currbestscore);
                else
                        this.currbestscore = bestscore;

                return tile;
        }

        private DominoTile maximaxttSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int mysize, int opsize) {
                DominoTile         tile = new DominoTile();
                int           bestscore = Integer.MIN_VALUE;
                DominoBoard           C = new DominoBoard(myset,opset,endstile,mysize,opsize);
                HashMap<Long,Integer> T = new HashMap<>(); // Transposition Table

                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        int score = this.maximaxtt(C,opsize,bestscore,T);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + score);
                        if(bestscore < score || (bestscore == score && greedySelect(t,tile,myset) == t)) {
                                bestscore = score;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + bestscore);
                // Only for debugging
                if(bestscore > this.currbestscore)
                        throw new RuntimeException("Error: the current best score " + bestscore + " is greater than the previous one " + currbestscore);
                else
                        this.currbestscore = bestscore;

                return tile;
        }




        public String getName() {
                if(this.type == Type.MAXIMAX)
                        return "MaxiMax";
                else if(this.type == Type.MAXIMAXPR)
                        return "MaxiMaxPR";
                else
                        return "MaxiMaxTT";
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

        private int maximax(DominoBoard C, int size) {
                if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        return getMaxScore(C.trueTiles[0],C.trueTiles[1],size);
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        int score = Integer.MIN_VALUE;
                        for(DominoTile t: C.getCurrentPlayerMoves()) {
                                C.playTile(t);
                                score = Math.max(score,maximax(C,size));
                                C.unplayTile();
                        }
                        return score;
                } else {
                        int    id = DominoPlayerId.SECOND.ordinal();
                        int score = Integer.MIN_VALUE;
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();

                        for(DominoTile t: L) {
                                C.playTile(t);
                                score = Math.max(score,maximax(C,t.isEmpty() ? size : size-1));
                                C.unplayTile();
                        }

                        if(!L.peek().isEmpty()) {
                                LinkedList<DominoTile> save = C.trueTiles[id].removeMatches(C.getBoardEnds());
                                if(save.size() > 0 && C.trueTiles[id].size() >= size) {
                                        C.playTile(new DominoTile());
                                        score = Math.max(score,maximax(C,size));
                                        C.unplayTile();
                                }
                                C.trueTiles[id].add(save);
                        }
                        return score;
                }
        }

        private int maximaxpr(DominoBoard C, int size, int bestscore) {
                if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        return  getMaxScore(C.trueTiles[0],C.trueTiles[1],size);
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        int score = Integer.MIN_VALUE;
                        for(DominoTile t: C.getCurrentPlayerMoves()) {
                                C.playTile(t);
                                score = Math.max(score,maximaxpr(C,size,bestscore));
                                C.unplayTile();
                                bestscore = Math.max(score,bestscore);
                        }
                        return score;
                } else {
                        int id    = DominoPlayerId.SECOND.ordinal();
                        int score = Integer.MIN_VALUE;
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();

                        for(DominoTile t: L) {
                                C.playTile(t);
                                score = Math.max(score,maximaxpr(C,t.isEmpty() ? size : size-1,bestscore));
                                C.unplayTile();
                                bestscore = Math.max(score,bestscore);
                        }

                        if(!L.peek().isEmpty()) {
                                LinkedList<DominoTile> save = C.trueTiles[id].removeMatches(C.getBoardEnds());
                                if(save.size() > 0 && C.trueTiles[id].size() >= size) {
                                        C.playTile(new DominoTile());
                                        score = Math.max(score,maximaxpr(C,size,bestscore));
                                        C.unplayTile();
                                }
                                C.trueTiles[id].add(save);
                        }
                        return score;
                }
        }


        private int maximaxtt(DominoBoard C, int size, int bestscore, HashMap<Long,Integer> T) {
                int maxpossiblescore;
                long    key   = C.hashValue();
                Integer score = T.get(key);


                if(score != null) {
                      return score;
                } else if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        score = getMaxScore(C.trueTiles[0],C.trueTiles[1],size);
                        T.put(key,score);
                        return score;
                } else if((maxpossiblescore = C.getPlayerTiles(DominoPlayerId.SECOND).getMaxScore(size)) < bestscore) {
                        return maxpossiblescore;
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        score = Integer.MIN_VALUE;
                        for(DominoTile t: C.getCurrentPlayerMoves()) {
                                C.playTile(t);
                                score = Math.max(score,maximaxtt(C,size,bestscore,T));
                                C.unplayTile();
                                bestscore = Math.max(score,bestscore);
                        }
                        T.put(key,score);
                        return score;
                } else {
                        int id = DominoPlayerId.SECOND.ordinal();
                        score  = Integer.MIN_VALUE;
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();

                        for(DominoTile t: L) {
                                C.playTile(t);
                                score = Math.max(score,maximaxtt(C,t.isEmpty() ? size : size-1,bestscore,T));
                                C.unplayTile();
                                bestscore = Math.max(score,bestscore);
                        }

                        if(!L.peek().isEmpty()) {
                                LinkedList<DominoTile> save = C.trueTiles[id].removeMatches(C.getBoardEnds());
                                if(save.size() > 0 && C.trueTiles[id].size() >= size) {
                                        C.playTile(new DominoTile());
                                        score = Math.max(score,maximaxtt(C,size,bestscore,T));
                                        C.unplayTile();
                                }
                                C.trueTiles[id].add(save);
                        }
                        T.put(key,score);
                        return score;
                }
        }

        private int getMaxScore(DominoTileSet set1, DominoTileSet set2, int size) {
                int scorefirst  = set1.getScore();
                int scoresecond = size == 0 ? 0 : set2.getMaxScore(size);
                return scoresecond-scorefirst;
        }

        private enum Type {
                MAXIMAX,
                MAXIMAXPR,
                MAXIMAXTT;
        }

}
