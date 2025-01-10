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
 * Implements a pessimistic domino player that selects moves based on the minimax strategy.
 * <p>
 * This player chooses the tile that minimizes the maximum potential loss, 
 * considering all possible sets of tiles the opponent could hold.
 * It employs the alpha-beta pruning strategy to accelerate the search process.
 */
public class MinimaxPlayer implements DominoPlayer {
        private DominoPlayerId myid;
        private DominoPlayerId opid;
        private int currbestscore; // Only for debug
        private boolean verbose;
        private Type type;

        /** Empty constructor */
        public MinimaxPlayer() {
                this.type = Type.MINIMAXTT;
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id) {
                this.myid          = id;
                this.opid          = id.toggle();
                this.currbestscore = -set.getScore();
                this.verbose       = false;
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id, boolean verbose) {
                this.initPlayer(set,id);
                this.verbose = verbose;
        }

        /**
         * From the set of playable tile selects the tile minimizing the maximum potential loss, considering all possible sets of tiles the opponent could hold. 
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

                        if(this.type == Type.MINIMAX)
                                playtile = this.minimaxSelect(myset,opset,endstile,opsize);
                        else if(this.type == Type.MINIMAXAB)
                                playtile = this.minimaxabSelect(myset,opset,endstile,opsize);
                        else
                                playtile = this.minimaxttSelect(myset,opset,endstile,mysize,opsize);
                }

                return playtile;
        }

        private DominoTile minimaxSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int opsize) {
                DominoTile tile = new DominoTile();
                int bestscore   = Integer.MIN_VALUE;
                DominoBoard C   = new DominoBoard(myset,opset,endstile);
                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        int score = this.minimax(C,opsize);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + score);
                        if(bestscore < score || (bestscore == score && greedySelect(t,tile,myset) == t)) {
                                bestscore = score;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + bestscore);
                // Only for debugging
                if(bestscore < this.currbestscore)
                        throw new RuntimeException("Error: the current best score " + bestscore + " is worst than the previous one " + currbestscore);
                else
                        this.currbestscore = bestscore;

                return tile;
        }

        private DominoTile minimaxabSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int opsize) {
                DominoTile tile = new DominoTile();
                int bestscore   = Integer.MIN_VALUE;
                DominoBoard C   = new DominoBoard(myset,opset,endstile);

                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        //int score = this.minimaxab(C,opsize,Integer.MIN_VALUE,Integer.MAX_VALUE);
                        int score = this.minimaxab(C,opsize,Integer.MIN_VALUE,Integer.MAX_VALUE);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + score);
                        if(bestscore < score || (bestscore == score && greedySelect(t,tile,myset) == t)) {
                                bestscore = score;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + bestscore);
                // Only for debugging
                if(bestscore < this.currbestscore)
                        throw new RuntimeException("Error: the current best score " + bestscore + " is worst than the previous one " + currbestscore);
                else
                        this.currbestscore = bestscore;

                return tile;
        }

        private DominoTile minimaxttSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int mysize, int opsize) {
                DominoTile         tile = new DominoTile();
                int           bestscore = Integer.MIN_VALUE;
                DominoBoard           C = new DominoBoard(myset,opset,endstile,mysize,opsize);
                HashMap<Long,Score>   T = new HashMap<>(); // Transposition Table

                for(DominoTile t: myset.matchingTiles(endstile)) {
                        C.playTile(t);
                        Score score = this.minimaxtt(C,opsize,Integer.MIN_VALUE,Integer.MAX_VALUE,T);
                        //Score score = this.minimaxtt(C,opsize,Integer.MIN_VALUE,Integer.MAX_VALUE,T);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + score.value);
                        if(bestscore < score.value || (bestscore == score.value && greedySelect(t,tile,myset) == t)) {
                                bestscore = score.value;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Score: " + bestscore);
                // Only for debugging
                if(bestscore < this.currbestscore)
                        throw new RuntimeException("Error: the current best score " + bestscore + " is worst than the previous one " + currbestscore);
                else
                        this.currbestscore = bestscore;

                return tile;
        }




        public String getName() {
                if(this.type == Type.MINIMAX)
                        return "MiniMax";
                else if(this.type == Type.MINIMAXAB)
                        return "MiniMaxAB";
                else
                        return "MiniMaxTT";
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

        private int minimax(DominoBoard C, int size) {
                if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        return getMinScore(C.trueTiles[0],C.trueTiles[1],size);
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        int score = Integer.MIN_VALUE;
                        for(DominoTile t: C.getCurrentPlayerMoves()) {
                                C.playTile(t);
                                score = Math.max(score,minimax(C,size));
                                C.unplayTile();
                        }
                        return score;
                } else {
                        int score = Integer.MAX_VALUE;
                        int id    = DominoPlayerId.SECOND.ordinal();
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();

                        for(DominoTile t: L) {
                                C.playTile(t);
                                score = Math.min(score,minimax(C,t.isEmpty() ? size : size-1));
                                C.unplayTile();
                        }

                        if(!L.peek().isEmpty()) {
                                LinkedList<DominoTile> save = C.trueTiles[id].removeMatches(C.getBoardEnds());
        
                                if(save.size() > 0 && C.trueTiles[id].size() >= size) {
                                        C.playTile(new DominoTile());
                                        score = Math.min(score,minimax(C,size));
                                        C.unplayTile();
                                }
                                C.trueTiles[id].add(save);
                        }
                        return score;
                }
        }

        

        private int minimaxab(DominoBoard C, int size, int alpha, int beta) {
                if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        return getMinScore(C.trueTiles[0],C.trueTiles[1],size);
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        int score = Integer.MIN_VALUE;
                        for(DominoTile t: C.getCurrentPlayerMoves()) {
                                C.playTile(t);
                                score = Math.max(score,minimaxab(C,size,alpha,beta));
                                C.unplayTile();
                                alpha = Math.max(alpha,score);
                                if(beta <= alpha) 
                                        break;
                                
                        }
                        return score;
                } else {
                        int score = Integer.MAX_VALUE;
                        int id    = DominoPlayerId.SECOND.ordinal();
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();

                        for(DominoTile t: L) {
                                C.playTile(t);
                                score = Math.min(score,minimaxab(C,t.isEmpty() ? size : size-1,alpha,beta));
                                C.unplayTile();
                                beta = Math.min(beta,score);
                                if(beta <= alpha)
                                        break;
                        }

                        if(beta > alpha && !L.peek().isEmpty()) {
                                LinkedList<DominoTile> save = C.trueTiles[id].removeMatches(C.getBoardEnds());

                                if(save.size() > 0 && C.trueTiles[id].size() >= size) {
                                        C.playTile(new DominoTile());
                                        score = Math.min(score,minimaxab(C,size,alpha,beta));
                                        C.unplayTile();
                                        beta = Math.min(beta,score);
                                }
                                C.trueTiles[id].add(save);
                        }

                        return score;
                }
        }

        private Score minimaxtt(DominoBoard C, int size, int alpha, int beta, HashMap<Long,Score> T) {
                long key    = C.hashValue();
                Score score = T.get(key);
                int value; 
                
                if(score != null) {
                        if(score.type == Flag.EXACT)
                                return score;
                        if(score.type == Flag.LOWERBOUND && score.value >= beta)
                                return score;
                        if(score.type == Flag.UPPERBOUND && score.value <= alpha)
                                return score;
                        if(score.type == Flag.LOWERBOUND)
                                alpha = Math.max(alpha,score.value);
                        if(score.type == Flag.UPPERBOUND)
                                beta  = Math.min(beta,score.value);
                }

                double a = alpha, b = beta; // Save alpha and beta

                if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        score = new Score(getMinScore(C.trueTiles[0],C.trueTiles[1],size),Flag.EXACT);
                        T.put(key,score);
                        return score;
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        value = Integer.MIN_VALUE;
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();

                        for(DominoTile t: L) {
                                C.playTile(t);
                                value = Math.max(value,minimaxtt(C,size,alpha,beta,T).value);
                                C.unplayTile();
                                alpha = Math.max(alpha,value);
                                if(beta <= alpha)
                                        break;
                        }
                } else {
                        int id = DominoPlayerId.SECOND.ordinal();
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();
                        value = Integer.MAX_VALUE;

                        for(DominoTile t: L) {
                                C.playTile(t);
                                value = Math.min(value,minimaxtt(C,t.isEmpty() ? size : size-1,alpha,beta,T).value);
                                C.unplayTile();
                                beta = Math.min(beta,value);
                                if(beta <= alpha)
                                        break;
                        }
                        // Evaluate the case in which the oppoent has no playable tiles and should pass
                        if(beta > alpha && !L.peek().isEmpty()) {
                                LinkedList<DominoTile> save = C.trueTiles[id].removeMatches(C.getBoardEnds());

                                if(save.size() > 0 && C.trueTiles[id].size() >= size) {
                                        C.playTile(new DominoTile());
                                        value = Math.min(value,minimaxtt(C,size,alpha,beta,T).value);
                                        C.unplayTile();
                                }
                                C.trueTiles[id].add(save);
                        }

                }

                if(value <= a)
                        score = new Score(value,Flag.UPPERBOUND);
                else if(value >= b)
                        score = new Score(value,Flag.LOWERBOUND);
                else
                        score = new Score(value,Flag.EXACT);
                T.put(key,score);
                return score;

        }



        private int getMinScore(DominoTileSet set1, DominoTileSet set2, int size) {
                int scorefirst  = set1.getScore();
                int scoresecond = size == 0 ? 0 : set2.getMinScore(size);
                return scoresecond-scorefirst;
        }

        private class Score {
                int value;
                Flag type;

                public Score(int value, Flag type) {
                        this.value = value;
                        this.type  = type;
                }

                public String toString() {
                        return "Score: " + value + " Type: " + type;
                }
        }

        private enum Flag {
                EXACT,
                LOWERBOUND,
                UPPERBOUND;
        }

        private enum Type {
                MINIMAX,
                MINIMAXAB,
                MINIMAXTT;
        }        

        
}
