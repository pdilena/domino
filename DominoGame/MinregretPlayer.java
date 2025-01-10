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
 * Implements a domino player that selects moves based on minizing the maximum expected regret.
 * <p>
 * This player chooses the tile that minimizes the maxumum expected regret, 
 * considering all possible sets of tiles the opponent could hold.
 */
public class MinregretPlayer implements DominoPlayer {
        private DominoPlayerId myid;
        private DominoPlayerId opid;
        private boolean verbose;
        private Type type;
        private DecimalFormat F;

        /** Empty constructor */
        public MinregretPlayer() {
                this.type = Type.MINREGRETTT;
        }

        public void initPlayer(DominoTileSet set, DominoPlayerId id) {
                this.myid    = id;
                this.opid    = id.toggle();
                this.verbose = false;
                this.F       = new DecimalFormat("+#0.000;-#0.000");
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

                        if(this.type == Type.MINREGRET)
                                playtile = this.minregretSelect(myset,opset,endstile,opsize);
                        else
                                playtile = this.minregretttSelect(myset,opset,endstile,mysize,opsize);
                }

                return playtile;
        }


        /*
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
        */

        private DominoTile minregretSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int opsize) {
                DominoTile          tile = new DominoTile();
                double         bestscore = -Double.MAX_VALUE;
                DominoBoard            C = new DominoBoard(myset,opset,endstile);
                LinkedList<DominoTile> L = myset.matchingTiles(endstile);
                Score[]                S = new Score[L.size()];
                int                    k = 0;

                for(DominoTile t: L) {
                        C.playTile(t);
                        S[k++] = this.minregret(C,opsize);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + S[k-1]);
                }
                
                double    minregret = Double.MAX_VALUE;
                int        bestindx = 0;

                for(int i = 0; i < L.size(); i++) {
                        double   maxregret = -Double.MAX_VALUE;
                        DominoTile       t = L.get(i);
                        if(verbose) System.out.print(t);
                        for(int j = 0; j < L.size(); j++) {
                                if(i != j) {
                                        maxregret = Math.max(maxregret,regret(S[i],S[j]));
                                        if(verbose) System.out.print(" " + F.format(regret(S[i],S[j])));
                                }
                        }
                        if(verbose) System.out.println(" Max regret: " + F.format(maxregret));
                        if(minregret > maxregret || (minregret == maxregret && (S[i].escore() > S[bestindx].escore() || (S[i].escore() == S[bestindx].escore() && greedySelect(t,tile,myset) == t)))) {
                                minregret = maxregret;
                                bestindx  = i;
                                tile      = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Regret: " + F.format(minregret) + " EU: " + F.format(S[bestindx].escore()));
                return tile;
        }

        private DominoTile minregretttSelect(DominoTileSet myset, DominoTileSet opset, DominoTile endstile, int mysize, int opsize) {
                DominoTile          tile = new DominoTile();
                double         bestscore = -Double.MAX_VALUE;
                DominoBoard            C = new DominoBoard(myset,opset,endstile,mysize,opsize);
                LinkedList<DominoTile> L = myset.matchingTiles(endstile);
                Score[]                S = new Score[L.size()];
                int                    k = 0;
                HashMap<Long,Score>    T = new HashMap<>(); // Transposition Table

                for(DominoTile t: L) {
                        C.playTile(t);
                        S[k++] = this.minregrettt(C,opsize,T);
                        C.unplayTile();
                        if(verbose) System.out.println("Evaluating: " + t + " Score: " + S[k-1]);
                }

                double    minregret = Double.MAX_VALUE;
                int        bestindx = 0;

                for(int i = 0; i < L.size(); i++) {
                        double maxregret = -Double.MAX_VALUE;
                        DominoTile       t = L.get(i);
                        if(verbose) System.out.print(t);
                        for(int j = 0; j < L.size(); j++) {
                                if(i != j) {
                                        maxregret = Math.max(maxregret,regret(S[i],S[j]));
                                        if(verbose) System.out.print(" " + F.format(regret(S[i],S[j])));
                                }
                        }
                        if(verbose) System.out.println(" Max regret: " + F.format(maxregret));
                        if(minregret > maxregret || (minregret == maxregret && (S[i].escore() > S[bestindx].escore() || (S[i].escore() == S[bestindx].escore() && greedySelect(t,tile,myset) == t)))) {
                                minregret = maxregret;
                                 bestindx = i;
                                     tile = t;
                        }
                }
                if(verbose) System.out.println("Selected tile: " + tile + " Regret: " + F.format(minregret) + " EU: " + F.format(S[bestindx].escore()));

                return tile;
        }

        

        public String getName() {
                if(this.type == Type.MINREGRET)
                        return "MinRegret";
                else
                        return "MinRegretTT";
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


        private Score minregret(DominoBoard C, int size) {
                if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        return getAvgScore(C.trueTiles[0],C.trueTiles[1],size);
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();
                        Score[]                S = new Score[L.size()];
                        int                    k = 0;
                        for(DominoTile t: L) {
                                C.playTile(t);
                                S[k++] = minregret(C,size);
                                C.unplayTile();
                        }
                        double minregret = Double.MAX_VALUE;
                        int     bestindx = 0;
                        for(int i = 0; i < S.length; i++) {
                                double maxregret = -Double.MAX_VALUE;
                                for(int j = 0; j < S.length; j++)
                                        if(i != j) maxregret = Math.max(maxregret,regret(S[i],S[j]));
                                if(minregret > maxregret || (minregret == maxregret && S[i].escore() > S[bestindx].escore())) {
                                        minregret = maxregret;
                                         bestindx = i;
                                }
                        }
                        return S[bestindx];
                } else {
                        int                       id = DominoPlayerId.SECOND.ordinal();
                        Score                  score = new Score();
                        LinkedList<DominoTileProb> P = getDominoTileProb(C,size);
                        LinkedList<DominoTile>     L = new LinkedList<DominoTile>();

                        double currprob = 0;
                        for(DominoTileProb p: P) {
                                if(p.tile.isEmpty() && P.size() > 1) L = C.trueTiles[id].removeMatches(C.getBoardEnds());
                                C.playTile(p.tile);
                                score.merge(minregret(C,(p.tile.isEmpty() ? size : size-1)),p.prob);
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

        private Score minregrettt(DominoBoard C, int size, HashMap<Long,Score> T) {
                long    key = C.hashValue();
                Score score = T.get(key); 
                if(score != null) {
                        return score;
                } else if (C.getCurrentState() == DominoGameState.ENDED || size == 0) {
                        score = getAvgScore(C.trueTiles[0],C.trueTiles[1],size);
                        T.put(key,score);
                        return score;
                } else if(C.getCurrentPlayer() == DominoPlayerId.FIRST) {
                        LinkedList<DominoTile> L = C.getCurrentPlayerMoves();
                        Score[]                S = new Score[L.size()];
                        int k = 0;
                        for(DominoTile t: L) {
                                C.playTile(t);
                                S[k++] = minregret(C,size);
                                C.unplayTile();
                        }
                        double minregret = Double.MAX_VALUE;
                        int     bestindx = 0;
                        for(int i = 0; i < S.length; i++) {
                                double maxregret = -Double.MAX_VALUE;
                                for(int j = 0; j < S.length; j++)
                                        if(i != j) maxregret = Math.max(maxregret,regret(S[i],S[j]));
                                if(minregret > maxregret || (minregret == maxregret && S[i].escore() > S[bestindx].escore())) {
                                        minregret = maxregret;
                                        bestindx  = i;
                                }
                        }
                        T.put(key,S[bestindx]);
                        return S[bestindx];
                } else {
                        int                       id = DominoPlayerId.SECOND.ordinal();
                        LinkedList<DominoTileProb> P = getDominoTileProb(C,size);
                        LinkedList<DominoTile>     L = new LinkedList<DominoTile>();
                        score = new Score();

                        double currprob = 0;
                        for(DominoTileProb p: P) {
                                if(p.tile.isEmpty() && P.size() > 1) L = C.trueTiles[id].removeMatches(C.getBoardEnds());
                                C.playTile(p.tile);
                                score.merge(minregret(C,(p.tile.isEmpty() ? size : size-1)),p.prob);
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

        private double regret(Score S1, Score S2) {
                return S1.nprob*S2.pscore-S2.pprob*S1.nscore;
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

        private Score getAvgScore(DominoTileSet myset, DominoTileSet opset, int opsize) {
                int     n = choose(opset.size(),opsize);
                int score = -myset.getScore();
                if(opsize == 0) {
                        if(score == 0)
                                return new Score(1,1,0,0);
                        else
                                return new Score(0,1,0,score);
                } else {
                        return getAvgScores(opset,opsize,score,1.0/n);
                }
        }


        private Score getAvgScores(DominoTileSet opset, int opsize, int score, double prob) {
                if(opsize == 0) {
                        if(score > 0) 
                                return new Score(prob,0,prob*score,0);
                        else if(score < 0)
                                return new Score(0,prob,0,prob*score);
                        else
                                return new Score(prob,prob,0,0);
                } else {
                        Score        S = new Score();
                        DominoTile[] L = opset.toArray();
                        for(DominoTile t : L) {
                                opset.remove(t);
                                S.merge(getAvgScores(opset,opsize-1,score+t.totValue(),prob));
                        }
                        opset.add(L);
                        return S;
                }
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

        private class Score {
                double pprob;
                double nprob;
                double pscore;
                double nscore;
                

                public Score() {
                        pprob  = nprob  = 0;
                        pscore = nscore = 0;
                }

                public Score(double pprob, double nprob, double pscore, double nscore) {
                        this.pprob  = pprob;
                        this.nprob  = nprob;
                        this.pscore = pscore;
                        this.nscore = nscore;
                }
        
                public void merge(Score score) {
                        this.pprob  += score.pprob;
                        this.nprob  += score.nprob;
                        this.pscore += score.pscore;
                        this.nscore += score.nscore;
                }

                public void merge(Score score, double prob) {
                        this.pprob  += prob*score.pprob;
                        this.nprob  += prob*score.nprob;
                        this.pscore += prob*score.pscore;
                        this.nscore += prob*score.nscore;
                }

                public double escore() {
                        return pscore+nscore;
                }
        
                public String toString() {
                        return "EU: " + F.format(pscore+nscore) + " E+: " + F.format(pscore) + " E-: " + F.format(nscore) + " P+: " + F.format(pprob) + " P-: " + F.format(nprob);
                }
        }


        private enum Type {
                MINREGRET,
                MINREGRETTT;
        }     
                
}
