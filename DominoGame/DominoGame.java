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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.HashMap;

/**
 * Runs a game against two DominoPlayer classes and prints the game score.
 */
public class DominoGame {
        private static int             M = 6;
        private static int             N = 7;
        private static DominoTileSet[] S = new DominoTileSet[2];
        private static DominoPlayer[]  P = new DominoPlayer[2];
        private static DominoBoardPlus B;
        private static boolean         VERBOSE   = false;
        private static boolean         SWAP      = false;
        private static boolean         TOURNMENT = false;

        private static int playDomino(DominoPlayer[] P, DominoBoardPlus B) {
                while(B.getCurrentState() == DominoGameState.OPEN) {
                        if(VERBOSE) System.out.println("\n" + B.getCurrentPlayer() + " player (" + P[B.getCurrentPlayer().ordinal()].getName() + ") turn");
                        DominoTile tile = P[B.getCurrentPlayer().ordinal()].selectTile(B.getBoardView(B.getCurrentPlayer()));
                        B.playTile(tile);
                        if(VERBOSE) B.print();
                }

                return B.getCurrentScore();
        }

        private DominoGame() {
        }

        private static void printUsage() {
                System.err.println("Usage: DominoGame [OPTIONS] <DominoPlayer class> <DominoPlayer class>");
                System.err.println("OPTIONS:");
                System.err.println("  -m <int>      Maximum value for a tile. Default: " + M);
                System.err.println("  -n <int>      Number of tiles per player. Default: " + N);
                System.err.println("  -1 <Tile set> Set of tiles for the first player. Default: random");
                System.err.println("  -2 <Tile set> Set of tiles for the second player. Default: random");
                System.err.println("  -s            Play again swapping first and second player. Default: " + SWAP);
                System.err.println("  -t            Tournment mode. Play on random sets until one player reaches 100 points. Default: " + TOURNMENT);
                System.err.println("  -v            Verbose mode. Default: " + VERBOSE);
        }

        private static Stat run(DominoPlayer[] P, DominoTileSet S[]) {
                P[0].initPlayer(S[0].copy(),DominoPlayerId.FIRST,VERBOSE);
                P[1].initPlayer(S[1].copy(),DominoPlayerId.SECOND,VERBOSE);
                B = new DominoBoardPlus(S[0].copy(),S[1].copy());

                long start = System.currentTimeMillis();
                int  score = playDomino(P,B);
                long   end = System.currentTimeMillis();
                return new Stat(score,end-start);
        }
        
        private static void tournment(DominoPlayer[] P) {
                DominoPlayer P0 = P[0], P1 = P[1];
                DominoTileSet[] S;
                HashMap<DominoPlayer,Integer> H = new HashMap<DominoPlayer,Integer>();
                H.put(P0,0);
                H.put(P1,0);
                
                while(Math.max(H.get(P[0]),H.get(P[1])) < 100) {
                        S = setupDominoTileSets(null,null);
                        System.out.print(P[0].getName() + "\t" + S[0] + "\t" + P[1].getName() + "\t" + S[1]);
                        int score = run(P,S).score;
                        System.out.print("\t" + score);
                        if(score > 0)
                                H.put(P[0],H.get(P[0])+score);
                        else
                                H.put(P[1],H.get(P[1])-score);
                        System.out.println("\t" + P0.getName() + ": " + H.get(P0) + "\t" + P1.getName() + ": " + H.get(P1));
                }

                System.out.println("\nLoser:  " + (H.get(P0) < H.get(P1) ? P0.getName() : P1.getName()) + " Score: " + Math.min(H.get(P0),H.get(P1)));
                System.out.println("\nWinner: " + (H.get(P0) > H.get(P1) ? P0.getName() : P1.getName()) + " Score: " + Math.max(H.get(P0),H.get(P1)));
                
        }

        private static void singlematch(DominoPlayer[] P, DominoTileSet[] S) {
                System.out.print(P[0].getName() + "\t" + S[0] + "\t" + P[1].getName() + "\t" + S[1]);
                Stat stat = run(P,S);
                System.out.println("\t" + stat.score + "\t" + stat.time);
        }

        /**
         * DominoGame main method.
         * <p>
         * Run without parameter to show the usage
         * @param args The command line arguments.
         **/
        public static void main(String[] args) {
                if (args.length == 0) {
                        printUsage();
                        System.exit(0);
                }

                try {
                        parseArgs(args);
                        if(TOURNMENT) {
                                tournment(P);
                        } else {
                                singlematch(P,S);
                                if(SWAP) {
                                        DominoPlayer tmp = P[0];
                                        P[0] = P[1];
                                        P[1] = tmp;
                                        singlematch(P,S); 
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                }

        }

        private static ArrayList<DominoTile> parseTiles(String tiles) throws NumberFormatException {
                ArrayList<DominoTile> T = new ArrayList<>();
                String[] tokens         = tiles.trim().split("\\s+");

                for(String t: tokens) {
                        String[] num = t.split("|");
                        int x = Integer.parseInt(num[0]);
                        int y = Integer.parseInt(num[2]);
                        T.add(new DominoTile(x, y));
                }

                return T;
        }

        private static boolean containsDouble(ArrayList<DominoTile> A) {
                for(DominoTile t: A)
                        if(t.isDouble())
                                return true;
                return false;
        }

        private static ArrayList<DominoTile> drawTiles(int M, int N, ArrayList<DominoTile> A) {
                ArrayList<DominoTile> L = new ArrayList<>();
                Random rand = new Random();

                if(A != null && !containsDouble(A)) {
                        int i = rand.nextInt(M + 1);
                        L.add(new DominoTile(i,i));
                }

                while(L.size() != N) {
                        DominoTile tile = new DominoTile(rand.nextInt(M+1),rand.nextInt(M+1));
                        if((A == null || !A.contains(tile)) && !L.contains(tile))
                                L.add(tile);
                }
                return L;
        }

        private static ArrayList<DominoTile> drawTiles(int M, int N) {
                return drawTiles(M,N,null);
        }

        private static void parseArgs(String[] A) {
                ArrayList<String>     L  = new ArrayList<String>();
                ArrayList<DominoTile> S0 = null;
                ArrayList<DominoTile> S1 = null;

                for(int i = 0; i < A.length; i++) {
                        if(A[i].charAt(0) != '-') {
                                L.add(A[i]);
                        } else {
                                switch(A[i].charAt(1)) {
                                        case '1':
                                                if(++i >= A.length)
                                                        throw new IllegalArgumentException("Expected parameter after -1");
                                                S0 = parseTiles(A[i]); 
                                                break;
                                        case '2':
                                                if(++i >= A.length)
                                                        throw new IllegalArgumentException("Expected parameter after -2");
                                                S1 = parseTiles(A[i]); 
                                                break;
                                        case 'm':
                                                if(++i >= A.length)
                                                        throw new IllegalArgumentException("Expected parameter after -m");
                                                M = Integer.parseInt(A[i]);
                                                if(M < 0)
                                                        throw new IllegalArgumentException("The maximum tile value parameter cannot be negative");
                                                break;
                                        case 'n':
                                                if(++i >= A.length)
                                                        throw new IllegalArgumentException("Expected parameter after -m");
                                                N = Integer.parseInt(A[i]);
                                                if(N <= 0)
                                                        throw new IllegalArgumentException("Each player should draw at least one tile");
                                                break;
                                        case 's':
                                                SWAP = true;
                                                break;
                                        case 't':
                                                TOURNMENT = true;
                                                break;
                                        case 'v':
                                                VERBOSE = true;
                                                break;
                                        default:
                                                throw new IllegalArgumentException("Unrecognized option:  " + A[i]);
                                                
                                }
                        }
                }                


                if(2*N > (M+1)*(M+2)/2) 
                        throw new IllegalArgumentException("It is not possible to draw two sets of tiles of size " + N + " if the maximum value is " + M);
                if((S0 != null && S0.size() != N) || (S1 != null && S1.size() != N))
                        throw new IllegalArgumentException("One tile set does not contain " + N + " tiles");
                
                if (L.size() != 2)
                        throw new IllegalArgumentException("Wrong number of parameters");

                for(int i = 0; i <= 1; i++) {
                        try {
                                P[i] = (DominoPlayer)Class.forName(L.get(i)).getDeclaredConstructor().newInstance();
                        } catch (ClassNotFoundException e) {
                                throw new IllegalArgumentException("Illegal argument: '" + L.get(i) + "' class not found");
                        } catch (ClassCastException e) {
                                throw new IllegalArgumentException("Illegal argument: '" + L.get(i) + "' class does not implement the DominoPlayer interface");
                        } catch (NoSuchMethodException e) {
                                throw new IllegalArgumentException("Illegal argument: '" + L.get(i) + "' needs to have an empty constructor");
                        } catch (Exception e) {
                                throw new IllegalArgumentException("Illegal argument: '" + L.get(i) + "' class (unexpected exception) " + e);
                        }
                }

                if(TOURNMENT == false)
                        S = setupDominoTileSets(S0,S1);
        }

        private static  DominoTileSet[] setupDominoTileSets(ArrayList<DominoTile> S0, ArrayList<DominoTile> S1) {
                DominoTileSet S[] = new DominoTileSet[2];
                if(S0 == null && S1 == null) {
                        S0 = drawTiles(M,N);
                        S1 = drawTiles(M,N,S0);
                } else if(S0 == null && S1 != null) {
                        S0 = drawTiles(M,N,S1);
                } else if(S0 != null && S1 == null) {
                        S1 = drawTiles(M,N,S0);
                }

                S[0] = new DominoTileSet(M,S0);
                S[1] = new DominoTileSet(M,S1);
                setFirstSet(S);
                return S;
        }

        private static void setFirstSet(DominoTileSet S[]) {
                DominoTile t0 = S[0].getLargestDouble();
                DominoTile t1 = S[1].getLargestDouble();

                if(t0.isEmpty() && t1.isEmpty())
                        throw new IllegalArgumentException("No player has a double tile: cannot start the game");

                if(t1.maxValue() > t0.maxValue()) {
                        DominoTileSet tmps = S[0];
                        S[0]               = S[1];
                        S[1]               = tmps;
                        DominoPlayer tmpp  = P[0];
                        P[0]               = P[1];
                        P[1]               = tmpp;
                }
        }

        private static class Stat {
                final int  score;
                final long time;

                public Stat(int score, long time) {
                        this.score = score;
                        this.time  = time;
                }
        }
}
