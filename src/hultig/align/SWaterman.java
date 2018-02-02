/*************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2011 UBI/HULTIG All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 3 only ("GPL"). You may not use this
 * file except in compliance with the License. You can obtain a copy of
 * the License at http://www.gnu.org/licenses/gpl.txt. See the License
 * for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the software, include this License Header Notice
 * in each file. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 *
 *       "Portions Copyrighted [year] [name of copyright owner]"
 *************************************************************************
 */
package hultig.align;



import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Random;

import hultig.sumo.Text;
import hultig.sumo.Word;
import hultig.sumo.CorpusIndex;
import hultig.util.StringPair;

/**
 * <p>This class contains an implementation of the <i>Smith Waterman</i> algorithm
 * for locally align sequence pairs. This algorithm have been used for DNA sequence
 * alignment, in genetics, and is here adapted for the alignment of words, between
 * sentence pairs. Another adaptation consists in the capability of output more
 * than one sub-alignment.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public final class SWaterman
{
    private final double scoreThreshold = 10.0;
    //private final double scoreThreshold = 4.9;

    /**
     * The dictionary, for the index vectors.
     */
    private CorpusIndex dictionary;

    /**
     * The first vector
     * JPC
     */
    private int[] vs1;

    /**
     * The second vector
     * JPC
     */
    private int[] vs2;

    /**
     * The first input string.
     * Original algorithm.
     */
    private String str1;

    /**
     * The second input string.
     * Original algorithm.
     */
    private String str2;

    /**
     * The lengths of the input strings.
     */
    private int length1, length2;

    /**
     * The score matrix.
     * The true scores should be divided by the normalization factor.
     */
    private double[][] score;

    /**
     * The normalization factor.
     * To get the true score, divide the integer score used in computation
     * by the normalization factor.
     */
    static final double NORM_FACTOR = 10.0;
    //static final double NORM_FACTOR = 5.0;

    /**
     * The similarity function constants.
     * They are amplified by the normalization factor to be integers.
     */
    //static final int MATCH_SCORE = 10;
    static final int MATCH_SCORE = 10;
    //static final int MISMATCH_SCORE = -8;
    static final int MISMATCH_SCORE = -10;
    static final int INDEL_SCORE = -3;
    //static final int INDEL_SCORE = -1;

    /**
     * Constants of directions.
     * Multiple directions are stored by bits.
     * The zero direction is the starting point.
     */
    static final int DR_LEFT = 1;   // 0001
    static final int DR_UP = 2;     // 0010
    static final int DR_DIAG = 4;   // 0100
    static final int DR_ZERO = 8;   // 1000

    /**
     * The directions pointing to the cells that
     * give the maximum score at the current cell.
     * The first index is the column index.
     * The second index is the row index.
     */
    private int[][] prevCells;

    private Random rand;
    private static PrintStream o= System.out;


    /**
     * Hold the dictionary word indexes, for a set of
     * defined key words defined through the SWaterman/4
     * constructor. Created after a request made by
     * the researchr Gintare, on Dec 2009, after visiting
     * HULTIG.
     */
    private int[] VKEYWS= null;


    public SWaterman(String str1, String str2) {
        this.str1 = str1;
        this.str2 = str2;
        this.vs1 = null;
        this.vs2 = null;

        length1 = str1.length();
        length2 = str2.length();

        score = new double[length1 + 1][length2 + 1];
        prevCells = new int[length1 + 1][length2 + 1];

        this.dictionary= null; //<--- JPC

        buildMatrix();
    }

    /**
     *	JPC - Para vectores representando frases.
     */
    public SWaterman(int[] vs1, int[] vs2) {
        this.vs1 = vs1;
        this.vs2 = vs2;
        this.str1 = null;
        this.str2 = null;

        length1 = vs1.length;
        length2 = vs2.length;

        score = new double[length1 + 1][length2 + 1];
        prevCells = new int[length1 + 1][length2 + 1];

        rand= new Random();
        buildMatrix();
    }


    /**
     * JPC - Para vectores representando frases. Acrescenta a
     * passagem do dicionário. 2007/03/14
     */
    public SWaterman(int[] vs1, int[] vs2, CorpusIndex dict) {
        this.vs1 = vs1;
        this.vs2 = vs2;
        this.str1 = null;
        this.str2 = null;

        length1 = vs1.length;
        length2 = vs2.length;

        score = new double[length1 + 1][length2 + 1];
        prevCells = new int[length1 + 1][length2 + 1];

        setDictionary(dict);

        rand= new Random();
        buildMatrix();
    }



    public SWaterman(int[] vs1, int[] vs2, CorpusIndex dict, int[] vkeyws) {
        this.vs1 = vs1;
        this.vs2 = vs2;
        this.str1 = null;
        this.str2 = null;

        length1 = vs1.length;
        length2 = vs2.length;

        score = new double[length1 + 1][length2 + 1];
        prevCells = new int[length1 + 1][length2 + 1];

        setDictionary(dict);

        rand= new Random();
        VKEYWS= vkeyws;
        buildMatrix();
    }


    public void setDictionary(CorpusIndex dictionary) {
        this.dictionary= dictionary;
    }


    /**
     * Test for a key word.
     * @param kw The key word dictionary index.
     * @return true if it is a key word.
     */
    private boolean isKeyWord(int kw) {
        if ( VKEYWS != null ) {
            for (int i = 0; i < VKEYWS.length; i++) {
                if ( VKEYWS[i] == kw )  {
                    //System.out.print('.');
                    return true;
                }
                if ( kw < VKEYWS[i] )  {
                    //System.out.printf(" i: %2d   VKEYWS[i]: %5d    kw: %d\n", i, VKEYWS[i], kw);
                    return false;
                } //for efficiency
            }
        }
        return false;
    }


    /**
     * Compute the similarity score of substitution: use a substitution matrix if the cost model
     * The position of the first character is 1.
     * A position of 0 represents a gap.
     * @param i Position of the character in str1
     * @param j Position of the character in str2
     * @return Cost of substitution of the character in str1 by the one in str2
     */
    private double similarity(int i, int j) {
        if (i == 0 || j == 0) {
            // it's a gap (indel)
            return INDEL_SCORE;
        }

        //--- JPC ---
        if (this.vs1 != null) {
            if ( this.dictionary == null) {
                //o.print('.');
                double dist;
                if ( vs1[i - 1] == vs2[j - 1] ) {
                    if ( isKeyWord(vs1[i-1]) )
                        dist= 5*MATCH_SCORE;
                    else
                        dist= MATCH_SCORE;
                } else
                    dist= MISMATCH_SCORE;
                //2010-03-17 System.out.print("x");
                return dist;
            }
            else {
                if ( vs1[i-1] == vs2[j-1] ) {
                    //System.out.print('.');
                    if ( isKeyWord(vs1[i-1]) ) {
                        //System.out.print('=');
                        return 5*MATCH_SCORE;
                    }
                    else
                        return MATCH_SCORE;
                }

                String wa= dictionary.get(vs1[i - 1]);
                String wb= dictionary.get(vs2[j - 1]);
                //double edist= -1.0*Word.editDistance(wa, wb)*Word.distlex(wa,wb); 20070330
                double dist= -1.0*Word.editDistance(wa, wb)/(0.01+Word.distSeqMax(wa,wb));

                //edist= -1.0*(1+rand.nextInt(9));
                //o.printf("%5.2f  i:%d  j:%d\n", edist, i, j);
                //2010-03-17 System.out.print("y");
                return dist;
            }
        }
        //-----------

        return (str1.charAt(i - 1) ==
                str2.charAt(j - 1)) ? MATCH_SCORE : MISMATCH_SCORE;
        // will be replaced by a Matrix class and distMatrix.get(i,j)
        // This call should also check valid alphabet at i and j positions
        //return distMatrix[i][j];
    }

    /**
     * Build the score matrix using dynamic programming.
     * Note: The indel scores must be negative. Otherwise, the
     * part handling the first row and column has to be
     * modified.
     */
    private void buildMatrix() {
        if (INDEL_SCORE >= 0) {
            throw new Error("Indel score must be negative");
        }

        //if (isDistanceMatrixNull()) {
        //    throw new Error ("Distance Matrix is NULL");
        //}

        int i; // length of prefix substring of str1
        int j; // length of prefix substring of str2

        // base case
        score[0][0] = 0;
        prevCells[0][0] = DR_ZERO; // starting point

        // the first row
        for (i = 1; i <= length1; i++) {
            score[i][0] = 0;
            prevCells[i][0] = DR_ZERO;
        }

        // the first column
        for (j = 1; j <= length2; j++) {
            score[0][j] = 0;
            prevCells[0][j] = DR_ZERO;
        }

        // the rest of the matrix
        for (i = 1; i <= length1; i++) {
            for (j = 1; j <= length2; j++) {
                double diagScore = score[i - 1][j - 1] + similarity(i, j);
                double upScore = score[i][j - 1] + similarity(0, j);
                double leftScore = score[i - 1][j] + similarity(i, 0);

                score[i][j] =
                    Math.max(diagScore, Math.max(upScore, Math.max(leftScore, 0)));
                prevCells[i][j] = 0;

                // find the directions that give the maximum scores.
                // the bitwise OR operator is used to record multiple
                // directions.
                if (diagScore == score[i][j]) {
                    prevCells[i][j] |= DR_DIAG;
                }

                if (leftScore == score[i][j]) {
                    prevCells[i][j] |= DR_LEFT;
                }

                if (upScore == score[i][j]) {
                    prevCells[i][j] |= DR_UP;
                }

                if (0 == score[i][j]) {
                    prevCells[i][j] |= DR_ZERO;
                }
            }
        }
    }



    /**
     * Get the maximum value in the score matrix.
     */
    private double getMaxScore() {
        double maxScore = 0;

        // skip the first row and column
        for (int i = 1; i <= length1; i++) {
            for (int j = 1; j <= length2; j++) {
                if (score[i][j] > maxScore) {
                    maxScore = score[i][j];
                }
            }
        }

        return maxScore;
    }

    /**
     * Get the alignment score between the two input strings.
     */
    public double getAlignmentScore() {
        return getMaxScore() / NORM_FACTOR;
    }


    /**
     * Returns the local alignments ending in the (i, j) cell.
     * aligned1 and aligned2 are suffixes of final aligned strings
     * found in backtracking before calling this function.
     * Note: the strings are replicated at each recursive call.
     * Use buffers or stacks to improve efficiency.
     *
     * adapted by JPC, 2006/07/07
     */
    public int[][] getAlignmentsVect(int i, int j, Vector<Integer> va1, Vector<Integer> va2) {
        // we've reached the starting point, so print the alignments
        // => terminates the recursive sequence.
        if ( (prevCells[i][j] & DR_ZERO) > 0) {
            int n = Math.min(va1.size(), va2.size());
            int[][] va = new int[2][n];

            for (int k = 0; k < n; k++) {
                va[0][k] = va1.get(k).intValue();
                va[1][k] = va2.get(k).intValue();
            }

            return va;
        }

        Integer I = new Integer(vs1[i - 1]);
        Integer J = new Integer(vs2[j - 1]);
        Integer SPC = new Integer( -1); //space code is -1.

        // find out which directions to backtrack
        if ( (prevCells[i][j] & DR_LEFT) > 0) {
            va1.insertElementAt(I, 0);
            va2.insertElementAt(SPC, 0);
            return getAlignmentsVect(i - 1, j, va1, va2);
        }
        if ( (prevCells[i][j] & DR_UP) > 0) {
            va1.insertElementAt(SPC, 0);
            va2.insertElementAt(J, 0);
            return getAlignmentsVect(i, j - 1, va1, va2);
        }
        if ( (prevCells[i][j] & DR_DIAG) > 0) {
            va1.insertElementAt(I, 0);
            va2.insertElementAt(J, 0);
            return getAlignmentsVect(i - 1, j - 1, va1, va2);
        }

        return null;
    }

    /**
     * Devolve um vector de sub-alinhamentos.
     * @return Vector
     */
    public Vector<int[][]> getAlignementsOLD() {
        if (vs1 == null || vs2 == null) {
            return null;
        }

        // find the cell with the maximum score
        double maxScore = getMaxScore();

        Vector<int[][]> v = new Vector<int[][]> ();
        // skip the first row and column
        for (int i = 1; i <= length1; i++) {
            for (int j = 1; j <= length2; j++) {
                if (score[i][j] == maxScore) {
                    Vector<Integer> va1 = new Vector<Integer> (); // acumuladores
                    Vector<Integer> va2 = new Vector<Integer> (); // acumuladores
                    int[][] va = getAlignmentsVect(i, j, va1, va2); //recursivo.
                    v.add(va);
                }
            }
        }

        return v;
    }


    /**
     * Devolve um vector de sub-alinhamentos.
     * @return Vector
     */
    public Vector<int[][]> getAlignments() {
        if (vs1 == null || vs2 == null) {
            return null;
        }

        List matches= this.getMatches();
        Vector<SimpleChaining.Match> vbest= getXBestMatches(matches);
        Vector<int[][]> vout = new Vector<int[][]> (vbest.size()); // output vector
        for (int i=0; i<vbest.size(); i++) {
            SimpleChaining.Match m= vbest.get(i);
            Vector<Integer> va1 = new Vector<Integer> (); //------------------> acumuladores
            Vector<Integer> va2 = new Vector<Integer> (); //------------------> acumuladores
            int[][] va = getAlignmentsVect(m.getToA(), m.getToB(), va1, va2); //recursivo.
            vout.add(va);
            //o.printf("vbest(%2d) ---> %s\n", i, m);
        }

        return vout;
    }


    /**
     * Output the local alignments ending in the (i, j) cell.
     * aligned1 and aligned2 are suffixes of final aligned strings
     * found in backtracking before calling this function.
     * Note: the strings are replicated at each recursive call.
     * Use buffers or stacks to improve efficiency.
     */
    private void printAlignments(int i, int j, String aligned1, String aligned2) {
        // we've reached the starting point, so print the alignments
        if ( (prevCells[i][j] & DR_ZERO) > 0) {
            System.out.println(aligned1);
            System.out.println(aligned2);
            System.out.println("");

            // Note: we could check other directions for longer alignments
            // with the same score. we don't do it here.
            return;
        }

        // find out which directions to backtrack
        if ( (prevCells[i][j] & DR_LEFT) > 0) {
            printAlignments(i - 1, j, str1.charAt(i - 1) + aligned1,
                            "_" + aligned2);
        }
        if ( (prevCells[i][j] & DR_UP) > 0) {
            printAlignments(i, j - 1, "_" + aligned1,
                            str2.charAt(j - 1) + aligned2);
        }
        if ( (prevCells[i][j] & DR_DIAG) > 0) {
            printAlignments(i - 1, j - 1, str1.charAt(i - 1) + aligned1,
                            str2.charAt(j - 1) + aligned2);
        }
    }

    public static String underscore(int n) {
        char[] vu = new char[n];
        for (int i = 0; i < vu.length; i++) {
            vu[i] = '_';
        }

        String su = new String(vu);
        return su;
    }

    /**
     * Output the local alignments ending in the (i, j) cell.
     * aligned1 and aligned2 are suffixes of final aligned strings
     * found in backtracking before calling this function.
     * Note: the strings are replicated at each recursive call.
     * Use buffers or stacks to improve efficiency.
     *
     * adapted by JPC, 2006/07/07
     */
    private void printAlignmentsVect(int i, int j, String aligned1, String aligned2) {
        // we've reached the starting point, so print the alignments
        // => terminates the recursive sequence.
        if ( (prevCells[i][j] & DR_ZERO) > 0) {
            System.out.println(aligned1);
            System.out.println(aligned2);
            System.out.println("");

            // Note: we could check other directions for longer alignments
            // with the same score. we don't do it here.
            return;
        }

        String si = " " + vs1[i - 1] + " ";
        String sj = " " + vs2[j - 1] + " ";
        String spci = ' ' + underscore(si.length() - 2) + ' ';
        String spcj = ' ' + underscore(sj.length() - 2) + ' ';

        // find out which directions to backtrack
        if ( (prevCells[i][j] & DR_LEFT) > 0) {
            printAlignmentsVect(i - 1, j, si + aligned1, spci + aligned2);
        }
        if ( (prevCells[i][j] & DR_UP) > 0) {
            printAlignmentsVect(i, j - 1, spcj + aligned1, sj + aligned2);
        }
        if ( (prevCells[i][j] & DR_DIAG) > 0) {
            printAlignmentsVect(i - 1, j - 1, si + aligned1, sj + aligned2);
        }
    }

    /**
     * given the bottom right corner point trace back  the top left conrner.
     *  at entry: i, j hold bottom right (end of Aligment coords)
     *  at return:  hold top left (start of Alignment coords)
     */
    private int[] traceback(int i, int j) {

        // find out which directions to backtrack
        while (true) {
            if ( (prevCells[i][j] & DR_LEFT) > 0) {
                if (score[i - 1][j] > 0) {
                    i--;
                }
                else {
                    break;
                }
            }
            if ( (prevCells[i][j] & DR_UP) > 0) {
                //return traceback(i, j-1);
                if (score[i][j - 1] > 0) {
                    j--;
                }
                else {
                    break;
                }
            }
            if ( (prevCells[i][j] & DR_DIAG) > 0) {
                //return traceback(i-1, j-1);
                if (score[i - 1][j - 1] > 0) {
                    i--;
                    j--;
                }
                else {
                    break;
                }
            }
        }
        int[] m = {i, j};
        return m;
    }

    /**
     * Output the local alignments with the maximum score.
     */
    public void printAlignments() {
        // find the cell with the maximum score
        double maxScore = getMaxScore();
        System.out.println("Max. Score: " + maxScore);

        List matches= this.getMatches();
        List<SimpleChaining.Match> vbest= getXBestMatches(matches);
        for (int i=0; i<vbest.size(); i++) {
            SimpleChaining.Match m= vbest.get(i);
            if (vs1 == null)
                printAlignments(m.getToA(), m.getToB(), "", "");  //recursivo.
            else
                printAlignmentsVect(m.getToA(), m.getToB(), "", "");  //recursivo.
        }

        /* 20070316
        // skip the first row and column
        for (int i = 1; i <= length1; i++) {
            for (int j = 1; j <= length2; j++) {
                if (score[i][j] == maxScore) {
                    if (vs1 == null) {
                        printAlignments(i, j, "", "");
                    }
                    else {
                        printAlignmentsVect(i, j, "", "");
                    }
                }
            }
        }
        // Note: empty alignments are not printed.
        */
    }


    /**
     * print the dynmaic programming matrix
     */
    public void printDPMatrix() {
        System.out.print("      ");
        for (int j = 1; j <= length2; j++) {
            if ( vs2 != null )
                o.printf("%2d   ", vs2[j-1]);
            else
                o.print("     " + str2.charAt(j - 1));
        }
        System.out.println();
        for (int i = 0; i <= length1; i++) {
            if (i > 0) {
                if ( vs1 != null )
                    o.printf("%2d   ", vs1[i - 1]);
                else
                    o.print(str1.charAt(i - 1) + "   ");
            }
            else {
                o.print("    ");
            }
            for (int j = 0; j <= length2; j++) {
                o.printf("%.2f  ", score[i][j] / NORM_FACTOR);
            }
            o.print("\n\n");
        }
    }

    /**
     *  Return a set of Matches idenfied in Dynamic programming matrix.
     * A match is a pair of subsequences whose score is higher than the
     * preset scoreThreshold
     */
    public List getMatches() {
        ArrayList matchList = new ArrayList();
        int fA = 0, tA = 0, fB = 0, tB = 0;
        //      skip the first row and column, find the next maxScore after prevmaxScore
        for (int i = 1; i <= length1; i++) {
            for (int j = 1; j <= length2; j++) {
                if (score[i][j] > scoreThreshold) {
                    // should be lesser than prev maxScore
                    fA = i;
                    fB = j;
                    // sets the x, y to startAlignment coordinates
                    int[] f = traceback(fA, fB);
                    matchList.add(new SimpleChaining.Match(f[0], i, f[1], j,
                        score[i][j] / NORM_FACTOR));
                }
            }
        }
        return matchList; // could be empty if no HSP scores are > scoreThreshold
    }
    
 

    public static void x200607070830() {
        String s1 = "gaza city, gaza strip ( ap ) - palestinian prime minister ismail haniyeh said friday that israel's offensive in gaza - including the kidnappings of some of his cabinet ministers - was part of a premeditated plan to bring down the hamas-led government.";
        String s2 = "haniyeh said friday that israel's offensive in gaza was part of a premeditated plan to bring down the hamas-led government, but palestinian leaders were working hard to end the standoff.\"";

        Text txt = new Text();
        txt.add(s1);
        txt.add(s2);
        txt.codify();

        int[] v1 = txt.getSentence(0).getCodes();
        int[] v2 = txt.getSentence(1).getCodes();

        //printvect(v1);
        //printvect(v2);

        SWaterman sw = new SWaterman(v1, v2);
        System.out.println("------------------------------");
        sw.printAlignments();
        System.out.println("------------------------------");
    }

    public static void x200607201745() {
        String s1 = "gaza city, gaza strip ( ap ) - palestinian prime minister ismail haniyeh said friday that israel's offensive in gaza - including the kidnappings of some of his cabinet ministers - was part of a premeditated plan to bring down the hamas-led government.";
        String s2 = "haniyeh said friday that israel's offensive in gaza was part of a premeditated plan to bring down the hamas-led government, but palestinian leaders were working hard to end the standoff.\"";

        Text txt = new Text();
        txt.add(s1);
        txt.add(s2);

        printParaAligns(txt);

        /*
                  txt.codify();

                  CorpusIndex dict= txt.getCorpusIndex();


                  int[] v1= txt.getSentence(0).getCodes();
                  int[] v2= txt.getSentence(1).getCodes();

                  //printvect(v1);
                  //printvect(v2);

                  SWaterman sw = new SWaterman(v1,v2);
                  Vector<int[][]> v= sw.getAlignements();
         System.out.println("-------------------------------------------------------");
                  for (int i=0; i<v.size(); i++) {
            int[][] va= v.get(i);
            for (int j=0; j<va[0].length; j++) {
                String wa, wb;
                if ( va[0][j] == -1 ) wa = "___";
                else
                    wa= dict.get(va[0][j]);
                if ( va[1][j] == -1 ) wb = "___";
                else
                    wb= dict.get(va[1][j]);

                System.out.printf("%20s   %s\n", wa, wb);
            }
            System.out.println("\n");
                  }
         System.out.println("-------------------------------------------------------");
         */
    }
    
    
    public static ArrayList<StringPair> getAlignmentsH(String sentenceA, String sentenceB) {
        Text txt = new Text();
        txt.add(sentenceA);
        txt.add(sentenceB);
        txt.codify();

        int[] v1 = txt.getSentence(0).getCodes();
        int[] v2 = txt.getSentence(1).getCodes();

        ArrayList<StringPair> alignmentPairs= new ArrayList<>();
        SWaterman sw = new SWaterman(v1,v2);
        List<int[][]> alignments= sw.getAlignments();
        for (int[][] a : alignments) {
            StringPair sp= AlignCommons.fromCodesToStringAlignments(a, txt.getCorpusIndex());
            alignmentPairs.add(sp);
        }
        
        return alignmentPairs;
    }


    /**
     * INTRODUZIDO POR JPC
     * OLD METHOD ==> vertical print.
     */
    public static void printParaAligns(Text txt) {
        txt.codify();
        CorpusIndex dict = txt.getCorpusIndex();

        int[] v1 = txt.getSentence(0).getCodes();
        int[] v2 = txt.getSentence(1).getCodes();

        SWaterman sw = new SWaterman(v1, v2);
        Vector<int[][]> v = sw.getAlignments();

        String spcleft = "";
        System.out.print("<pair>\n");
        for (int i = 0; i < v.size(); i++) {
            int[][] valg = v.get(i);
            for (int j = 0; j < valg[0].length; j++) {
                String wa, wb;
                if (valg[0][j] == -1) {
                    wa = "________";
                }
                else {
                    wa = dict.get(valg[0][j]);
                }
                if (valg[1][j] == -1) {
                    wb = "________";
                }
                else {
                    wb = dict.get(valg[1][j]);
                }

                System.out.print(spcleft);
                System.out.printf("%20s   %s\n", wa, wb);
            }
            System.out.print("\n");
            spcleft += "   ";
            break; //------------> NEW on 20070108
        }

        System.out.printf("stc 1 ---> [%s]\n", txt.getSentence(0));
        System.out.printf("stc 2 ---> [%s]\n", txt.getSentence(1));
        System.out.print("</pair>\n\n\n");
    }


    private static String genUnderline(int size) {
        StringBuilder buffer= new StringBuilder();
        for (int i=0; i<size; i++) {
            buffer.append('_');
        }

        return buffer.toString();
    }


    /**
     *	Introduced by JPC
     */
    public static void printParaAlignsHoriz(Text txt) {
        txt.codify();
        CorpusIndex dict = txt.getCorpusIndex();

        int[] v1 = txt.getSentence(0).getCodes();
        int[] v2 = txt.getSentence(1).getCodes();

        printParaAlignsHoriz(v1, v2, dict);
    }


    /**
     * Introduced by JPC
     * Modified: 2007/03/16.
     */
    public Vector<String[]> getParaAlignsHoriz() {
        //SWaterman sw = new SWaterman(v1, v2, dict);
        Vector<int[][]> v = this.getAlignments(); //<------- vector de sub-alinhamentos de codigos.
        Vector<String[]> vs= new Vector<String[]>(); //<---- vector de sub-alinhamentos com strings.

        for (int i = 0; i < v.size(); i++) {
            int[][] va = v.get(i);
            StringBuilder sa= new StringBuilder();
            StringBuilder sb= new StringBuilder();
            for (int j = 0; j < va[0].length; j++) {
                StringBuffer wa= new StringBuffer();
                StringBuffer wb= new StringBuffer();

                if ( va[0][j] == -1  &&  va[1][j] == -1 ) {
                    wa.append("___");
                    wb.append("___");
                }
                else if ( va[0][j] == -1 ) {
                    wb.append(dictionary.get(va[1][j]));
                    wa.append(genUnderline(wb.length()));
                }
                else if ( va[1][j] == -1 ) {
                    wa.append(dictionary.get(va[0][j]));
                    wb.append(genUnderline(wa.length()));
                }
                else {
                    wa.append(dictionary.get(va[0][j]));
                    wb.append(dictionary.get(va[1][j]));
                    int na= wa.length();
                    int nb= wb.length();
                    if ( na < nb )
                        for (int k=0; k< nb-na; k++)  wa.append(' ');
                    else if ( nb < na )
                        for (int k=0; k< na-nb; k++)  wb.append(' ');
                }
                //tos.o.printf("\twa: [%s]   wb: [%s]\n", wa, wb);

                sa.append(wa); sa.append(' ');
                sb.append(wb); sb.append(' ');
            }
            //---> NEW on 20070316  break; //---> NEW on 20070108
            String[] as= new String[2];
            as[0]= sa.toString();
            as[1]= sb.toString();
            //o.print("\t ........... "); for (int j=0; j<va[0].length; j++) o.printf(", %2d", va[0][j]); o.println();
            //o.print("\t ........... "); for (int j=0; j<va[1].length; j++) o.printf(", %2d", va[1][j]); o.println();
            vs.add(as); //---> acutaliza array de subalinhamentos 20070316
        }

        return vs;
    }


    /**
     *	Introduced by JPC
     */
    public static void printParaAlignsHoriz(int[] v1, int[] v2, CorpusIndex dict) {
        SWaterman sw= new SWaterman(v1, v2, dict);
        Vector<String[]> vs= sw.getParaAlignsHoriz();
        o.print( "<pairs>\n");
        o.print( "   <src>\n");
        o.printf("      s1: [%s]\n", dict.get(v1));
        o.printf("      s2: [%s]\n", dict.get(v2));
        o.print( "   </src>\n");
        for (int k=0; k<vs.size(); k++) {
            String[] as= vs.get(k);
            o.print( "   <p>\n");
            o.printf("      s1 ---> [%s]\n", as[0]);
            o.printf("      s2 ---> [%s]\n", as[1]);
            o.print( "   </p>\n");
        }
        o.print( "</pairs>\n\n");
        //sw.printDPMatrix();
    }



    /**
     *	INTRODUZIDO POR JPC
     */
    public static boolean processParaFile(String filename) {
        String infile = "/a/datasets/p6063021.txt";
        if ( System.getProperty("os.name").toLowerCase().contains("windows") )  infile= "c:" + infile;
        if (filename != null)  infile = filename;

        try {
            InputStream in = new FileInputStream(infile);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));

            Text txt = null;
            for (; ; ) {
                String line = br.readLine();
                //System.out.println('{'+line+'}');
                if (line == null) {
                    br.close();
                    break;
                }

                if (line.startsWith("CLUSTER")) {
                    txt = new Text();
                    continue;
                }

                if (line.length() == 0) {
                    if (txt != null) {
                        printParaAlignsHoriz(txt);
                        txt = null;
                    }
                    continue;
                }

                int k = line.indexOf(" ---> [");
                if (k > 0) {
                    String s = line.substring(k + 7, line.length() - 1);
                    //tos.o.printf("s: [%s]\n", s);
                    txt.add(s);
                    continue;
                }
            }
            //System.out.printf("");
        }
        catch (Exception exc) {
            System.err.printf("\n\tERROR WHEN LOADING FILE: %s\n", infile);
            return false;
        }

        return true;
    }


    /**
     * Devolver a melhor cadeia.
     * Por JPC 20070317
     * @param matches List
     * @return Match
     */
    private SimpleChaining.Match getBestMatch(List matches) {
        if ( matches == null ) return null;

        int n= matches.size();
        SimpleChaining.Match mbest= (SimpleChaining.Match)matches.get(0);
        for (int i=1; i<n; i++) {
            SimpleChaining.Match m= (SimpleChaining.Match)matches.get(i);
            if ( m.getScore() > mbest.getScore() ) mbest= m;
        }

        return mbest;
    }


    /**
     * Remover todas as cadeias sobrepostas a uma dada,
     * da lista de cadeias.
     * JPC 20070317
     * @param m Match
     * @param matches List
     */
    private void removeOverlapped(SimpleChaining.Match m, List matches) {
        for (int k=0; k<matches.size(); k++) {
            SimpleChaining.Match mk= (SimpleChaining.Match)matches.get(k);
            if ( !m.notOverlap(mk) ) {
                matches.remove(k);
                k--; // rebobina uma posição a contar com o incremento fixo.
            }
        }
    }


    /**
     * Por JPC 20070317
     * @param matches List
     * @return Vector
     */
    public Vector<SimpleChaining.Match> getXBestMatches(List matches) {
        Vector<SimpleChaining.Match> v= new Vector<SimpleChaining.Match>();

        while ( matches.size() > 0 ) {
            SimpleChaining.Match mbest= getBestMatch(matches);
            v.add(mbest);
            removeOverlapped(mbest, matches);
        }

        return v;
    }


    /**
     *
     *	M A I N
     *
     */
    public static void main(String[] args) {
        //---------------------------
        // JPC EXPR BLOCK
        x200607070830();
        double x = 2007.0316;
        if (x < 0.0) {
            String filename = null;
            if (args.length > 0) {
                filename = args[0];
            }
            processParaFile(filename);
            if (true) {
                return;
            }
        }
        else
            return;
        //---------------------------

        // --------------------------
        // JPC 20070131
        // ALETRAÇÕES: 20070316
        // --------------------------
        if (args.length > 0) {
            // compute and output the score and alignments
            SWaterman sw = new SWaterman(args[0], args[1]);
            o.println("\nThe maximum alignment score is: " + sw.getAlignmentScore());
            o.println("\nThe alignments with the maximum score are: \n");
            sw.printAlignments();
            sw.printDPMatrix();

            /*
            List ms = sw.getMatches ();
            for (int i=0; i<ms.size(); i++) {
                SimpleChaining.Match m= (SimpleChaining.Match)ms.get(i);
                o.printf("chain(%2d) ---> %s\n", i, m);
            }

            o.print("\n\n");
            Vector<SimpleChaining.Match> vbest= sw.getXBestMatches(ms);
            for (int i=0; i<vbest.size(); i++) {
                SimpleChaining.Match m= vbest.get(i);
                sw.getAlignmentsVect(m.toA, m.toB);
                o.printf("vbest(%2d) ---> %s\n", i, m);
            }
            */

            /* para que serve isto ???
            o.println ("Chaining demo: ");
            SimpleChaining.chaining (ms, false);
            */
            return;
        }
        // -------------------------


        // DEFAULT TEST.
        try {
            String str1 = null, str2 = null;

            if (args.length < 2) {
                System.out.println("Compute the local alignments between two strings ");
                System.out.println("i.e. bettybitter vs. peterpiper");

                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

                System.out.print("Enter string 1: ");
                str1 = in.readLine();

                System.out.print("Enter string 2: ");
                str2 = in.readLine();
            }
            else {
                str1 = args[0];
                str2 = args[1];
            }

            // compute and output the score and alignments
            SWaterman sw = new SWaterman(str1, str2);
            System.out.println("\nThe maximum alignment score is: " + sw.getAlignmentScore());
            System.out.println("\nThe alignments with the maximum score are: \n");
            sw.printAlignments();
            //System.out.println ("The dynamic programming distance matrix is");
            //sw.printDPMatrix ();
            //
            //
            //List ms = sw.getMatches ();
            //System.out.println ("Chaining demo: ");
            //SimpleChaining.chaining (ms, true);

        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
    }
}
