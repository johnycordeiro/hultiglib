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





import hultig.sumo.Text;
import hultig.sumo.Sentence;
import hultig.sumo.Word;
import hultig.sumo.CorpusIndex;
import hultig.util.StringPair;
import hultig.util.Toolkit;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


/**
 * <p>This class contains an implementation of the <i>Needleman Wunsch</i> algorithm
 * for globally align sequence pairs (the whole sequences). This algorithm have been
 * used for DNA sequence alignment, in genetics, and is here adapted for the alignment
 * of words, between sentence pairs.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public class NWunsch {
    int[] va;
    int[] vb;
    int[][] ALG; // matriz dim(2 * n)

    double GAPENALTY;
    double[][] score;
    double[][] mutex;

    private Random r;
    private int ID; //identificador para output.
    private static PrintStream o= System.out;

    CorpusIndex dictionary;
    

    public NWunsch(int[] sa, int[] sb) {
        constructor(sa, sb);
    }


    public NWunsch(String sa, String sb) {
        Sentence sta= new Sentence(sa);
        Sentence stb = new Sentence(sb);
        dictionary= CorpusIndex.codifyOnFly(new Sentence[]{sta, stb});
        constructor(sta.getCodes(), stb.getCodes());
        this.buildMatrix();
    }

    
    public NWunsch(Sentence sa, Sentence sb) {
        dictionary= CorpusIndex.codifyOnFly(new Sentence[]{sa, sb});
        constructor(sa.getCodes(), sb.getCodes());
        this.buildMatrix();
    }
    
    
    private void constructor(int[] sa, int[] sb) {
        this.va = sa.clone();
        this.vb = sb.clone();
        this.GAPENALTY = -3;
        this.score = null;
        this.ALG = null;
        this.r= new Random();
        this.mutex= new double[sa.length][sb.length];
    }
    

    public void setDic(CorpusIndex dict) {
        this.dictionary = dict;
    }
    
    
    public CorpusIndex getDict() {
        return dictionary;
    }

    public String codes2str(int[] vs) {
        if ( this.dictionary == null ) return null;
        StringBuffer sb= new StringBuffer();
        for (int i=0; i<vs.length; i++) {
            String w= dictionary.get(vs[i]);
            if ( w != null ) {
                sb.append(w);
                if ( i< vs.length-1 ) sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Define metrica de distância entre "simbolos" do alfabeto.
     * @param i int
     * @param j int
     * @return double
     */
    public double similarity(int i, int j) {
        double MAX= 10.0;
        if (i == j) {
            return MAX; //nota: experimentar outros valores.
        }
        if (dictionary == null) { // <--- i != j
            return -1.0*MAX; //valor de penalização.
        }

        //Não descrimina muito bem as palavras pouco diferentes das com
        //muitas diferenças. Isto implica uma alta facilidade de mutação
        //genética nos algoritmos de alinhamento. NOTA(20070310, jpc).
        //return -1.0 * Word.distlex(dictionary.get(i), dictionary.get(j));

        String wi= dictionary.get(i); 
        //int ni= wi.length();
        String wj= dictionary.get(j); 
        //int nj= wj.length();
        
        if ( wi == null ) {
            o.printf("ERROR: wi is NULL i: %d\n", i);
            o.printf("va ---> [%d", va[0]);
            for (int k=1; k<va.length; k++)  {
                o.printf("   %d", va[k]);
            }
            o.print("]\n");
            System.exit(1);
        }
        if ( wj == null ) {
            o.printf("ERROR: wj is NULL i: %d\n", j);
            for (int k=0; k<vb.length; k++)
                o.printf("%3d   ", vb[k]);
            System.exit(1);
        }
        

        if ( Word.RPUNCT.indexOf(wi) >= 0 || Word.RPUNCT.indexOf(wj) >= 0 ) {
            return -1.0*MAX; //aqui já sabemos que i!=j
        }

        double dilex  = (double)Word.distlex(wi, wj);
        //double dilexs = (double)Word.distlex(wi, wj);
        double edit = (double)Word.editDistance(wi, wj);
        double maxseq= Word.distSeqMax(wi, wj);

        return -1.0*edit/(maxseq+0.01);
    }


    /**
     * Compute the similarity matrix, using dynamic programming strategy.
     * The mutex matrix will be generated.
     */
    public void buildMatrix() {
        int na, nb;
        na = va.length;
        nb = vb.length;

        double d = this.GAPENALTY;
        score = new double[na + 1][nb + 1];

        for (int i = 0; i <= na; i++) {
            score[i][0] = d * i;
        }
        for (int j = 1; j <= nb; j++) {
            score[0][j] = d * j;
        }

        for (int i = 1; i <= na; i++) {
            for (int j = 1; j <= nb; j++) {
                mutex[i-1][j-1] = similarity(va[i - 1], vb[j - 1]);
                /*
                System.out.printf("mutex(%2d,%2d) = %10.3f   (%s,%s)\n", i-1, j-1, mutex[i-1][j-1],
                        dictionary.get(va[i-1]),
                        dictionary.get(vb[j-1])
                );
                 */
                double scorn = score[i - 1][j - 1] + mutex[i-1][j-1];
                double left = score[i][j - 1] + d;
                double up = score[i - 1][j] + d;
                score[i][j] = Toolkit.maximum(left, scorn, up);
            }
        }
    }

    public void buildAlignment() {
        //se matriz não está construida vai criá-la.
        if (score == null) {
            this.buildMatrix();
        }
        /*
        o.println("-----------------------------------------------------");
        this.printMatrix();
        o.println("-----------------------------------------------------");
        */

        this.ID= 0; //<--- reinicia o identificador.

        int i = va.length;
        int j = vb.length;
        int n = Math.max(i, j) - 1;
        ArrayList<int[]> valg = new ArrayList<> (n);

        double d = this.GAPENALTY;

        double Score, ScoreUp, ScoreLeft, ScoreDiag;
        while (i > 0 && j > 0) {
            Score = score[i][j];
            ScoreDiag = score[i - 1][j - 1];
            ScoreUp = score[i][j - 1];
            ScoreLeft = score[i - 1][j];

            int[] u = new int[2];

            //if (Score == ScoreDiag + similarity(va[i - 1], vb[j - 1])) {
            if (Score == ScoreDiag + mutex[i - 1][j - 1]) {
                u[0] = va[i - 1];
                i--;
                u[1] = vb[j - 1];
                j--;
            }
            else if (Score == ScoreLeft + d) {
                u[0] = va[i - 1];
                i--;
                u[1] = -1;
            }
            else if (Score == ScoreUp + d) {
                u[0] = -1;
                u[1] = vb[j - 1];
                j--;
            }

            valg.add(0, u);
        }
        //System.out.printf("\t\t loop end (i,j) = (%d,%d)   n:%d\n", i, j, n);
        while (i > 0) {
            int[] u = new int[2];
            //System.out.printf("\t\t\t (i,j) = (%d,%d)   n:%d\n", i, j, n);
            u[0] = va[i - 1];
            i--;
            u[1] = -1;
            valg.add(0, u);
        }
        while (j > 0) {
            int[] u = new int[2];
            u[0] = -1;
            u[1] = vb[j - 1];
            j--;
            valg.add(0, u);
        }

        //transfer vector to array.
        ALG = new int[2][valg.size()];
        for (int k = 0; k < ALG[0].length; k++) {
            int[] u = valg.get(k);
            ALG[0][k] = u[0];
            ALG[1][k] = u[1];
        }
    }
    
    
    public static String[] getAlignmentH(Sentence sa, Sentence sb) {
        NWunsch nw= new NWunsch(sa, sb);
        return nw.getAlignmentH();
    }
    

    public String[] getAlignmentH() {
        if (ALG == null) {
            this.buildAlignment(); // <--- RECURSIVO INDIRECTO.
        }
        if (score == null) {
            o.println("\nERROR in getAlignmentH()\n=> The score matrix is null!\n");
            return null;
        }
        StringPair spair= AlignCommons.fromCodesToStringAlignments(ALG, dictionary);
        return new String[] {
            spair.getS1(),
            spair.getS2()
        };
    }

    public void printAlignmentH(int idpar) {
        this.ID++; //<--- identificador incrementado.
        System.out.printf("<par id=\"%d\">\n", idpar);
        String[] vs= this.getAlignmentH();
        System.out.printf("   S1 --> [%s]   ORIGINAL(S1) --> [%s]\n", vs[0], codes2str(this.va));
        System.out.printf("   S2 --> [%s]   ORIGINAL(S2) --> [%s]\n", vs[1], codes2str(this.vb));
        System.out.print("</par>\n\n");
    }

    public void printAlignmentV() {
        if (score == null) {
            return;
        }
        if (ALG == null) {
            this.buildAlignment(); // <--- RECURSIVO INDIRECTO.
        }

        System.out.print("<par>\n");
        for (int i = 0; i < ALG[0].length; i++) {
            int ka = ALG[0][i];
            int kb = ALG[1][i];

            System.out.printf("   %3d   %3d\n", ka, kb);
        }
        System.out.print("</par>\n");
    }

    public void printvectors() {
        Toolkit.printVector("va:", va);
        Toolkit.printVector("vb:", vb);
    }

    public void printMatrix() {
        if (score == null) {
            return;
        }

        int na = va.length;
        int nb = vb.length;

        System.out.print("\n\nMATRIX\n\n      |\n");
        System.out.print("      |            ");
        String hline =   "------|-";
        for (int i = 0; i < nb; i++) {
            System.out.print(vb[i] + "       ");
            hline += "--------";
        }
        System.out.printf("\n%s------", hline);
        for (int i = 0; i < na + 1; i++) {
            if (i > 0) {
                System.out.printf("  %3d | ", va[i - 1]);
            }
            else {
                System.out.print("\n      | ");
            }

            for (int j = 0; j < nb + 1; j++) {
                System.out.printf("%5.1f   ", score[i][j]);
            }
            System.out.print("\n      |\n");
        }
    }



    /**
     * Print the alignment matrix to be used in Latex.
     * Created on 10 June 2010.
     *
     * For example:
     *
        \tiny
        \begin{equation*}\label{MATRIX:Fexample}
        \left( \begin{array}{rrrrrrrrrrrrr}
                      &  \_\_  &  Gilbert  &   was  &   the & most & intense & storm &   on & record &  in  &  the & west \\
             \_\_ &   0.0  &    -3.0   &  -6.0  &  -9.0 & -12.0&  -15.0  & -18.0 & -21.0& -24.0  & -27.0& -30.0& -33.0\\
          Gilbert &  -3.0  &    10.0   &   7.0  &   4.0 &   1.0&   -2.0  &  -5.0 &  -8.0& -11.0  & -14.0& -17.0& -20.0\\
              was &  -6.0  &     7.0   &  20.0  &  17.0 &  14.0&   11.0  &   8.0 &   5.0&   2.0  &  -1.0&  -4.0&  -7.0\\
              the &  -9.0  &     4.0   &  17.0  &  30.0 &  27.0&   24.0  &  21.0 &  18.0&  15.0  &  12.0&   9.0&   6.0\\
             most & -12.0  &     1.0   &  14.0  &  27.0 &  40.0&   37.0  &  34.0 &  31.0&  28.0  &  25.0&  22.0&  19.0\\
          intense & -15.0  &    -2.0   &  11.0  &  24.0 &  37.0&   50.0  &  47.0 &  44.0&  41.0  &  38.0&  35.0&  32.0\\
        hurricane & -18.0  &    -5.0   &   8.0  &  21.0 &  34.0&   47.0  &  44.0 &  41.0&  38.0  &  35.0&  32.0&  29.0\\
             ever & -21.0  &    -8.0   &   5.0  &  18.0 &  31.0&   44.0  &  41.0 &  38.0&  35.0  &  36.0&  33.0&  30.0\\
         recorded & -24.0  &   -11.0   &   2.0  &  15.0 &  28.0&   41.0  &  38.0 &  35.0&  35.4  &  33.0&  30.0&  27.0\\
               in & -27.0  &   -14.0   &  -1.0  &  12.0 &  25.0&   38.0  &  35.0 &  32.0&  32.4  &  45.4&  42.4&  39.4\\
          western & -30.0  &   -17.0   &  -4.0  &   9.0 &  22.0&   35.0  &  32.0 &  29.0&  29.4  &  42.4&  39.4&  36.4\\
        hemisfere & -33.0  &   -20.0   &  -7.0  &   6.0 &  19.0&   32.0  &  29.0 &  26.0&  26.4  &  39.4&  37.2&  34.2
        \end{array} \right)
        \end{equation*}
        \normalsize
     */
    public void printMatrixLatex() {
        if (score == null) {
            return;
        }

        int na = va.length;
        int nb = vb.length;

        System.out.println("\n\\tiny");
        System.out.println("\\begin{equation*}\\label{MATRIX:???}");
        System.out.print(  "\\left( \\begin{array}{");
        for (int i = 0; i <= nb+1; i++) System.out.print('r');
        System.out.println('}');

        System.out.print("           & \\_\\_ ");
        for (int i = 0; i < nb; i++) {
            System.out.printf("& %s ", dictionary.get(vb[i]));
        }
        System.out.println("\\\\");

        System.out.print("      \\_\\_ ");
        for (int i = 0; i < na + 1; i++) {
            if (i > 0)
                System.out.printf("%10s ", dictionary.get(va[i - 1]));
            for (int j = 0; j < nb + 1; j++) {
                System.out.printf(Locale.US, "&%5.1f ", score[i][j]);
            }
            if (i < na ) 
                System.out.println("\\\\");
            else
                System.out.println();
        }

        System.out.println("\\end{array} \\right)");
        System.out.println("\\end{equation*}");
        System.out.println("\\normalsize");
    }


    /**
     * To process a paraphrase file. The output will be an equivalent
     * set of aligned paraphrases.
     * @param filename String
     * @return boolean
     */
    public static boolean processParaFile(String filename) {
        String infile = "p.txt"; // <--- ficheiro exemplo predefinido.
        if (filename != null) {
            infile = filename;
        }

        int idpar= 0;
        try {
            //System.out.printf("");
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

                if (line.startsWith("CLUSTER")) { // <--- inicio do paraph-cluster
                    txt = new Text();
                    continue;
                }

                if (line.length() == 0) { // <--- fim do paraph-cluster
                    if (txt != null) {
                        //System.out.println("\tCOMPUTE ALIGN ...");
                        computeAlignOut(txt, ++idpar);
                        txt = null;
                    }
                    continue;
                }

                int k = line.indexOf(" ---> [");
                if (k > 0) {
                    String s = line.substring(k + 7, line.length() - 1);
                    txt.add(s);
                    continue;
                }
            }
        }
        catch (Exception exc) {
            System.err.printf("\n\tERROR WHEN LOADING FILE: %s\n", infile);
            return false;
        }

        return true;
    }

    public static void computeAlignOut(Text txt, int idpar) {
        if (txt == null) {
            return;
        }

        txt.codify();
        CorpusIndex dict = txt.getCorpusIndex();

        int[] v1 = txt.getSentence(0).getCodes(); // \__ só as duas primeiras do cluster.
        int[] v2 = txt.getSentence(1).getCodes(); // /

        //2007/03/13 10:24 - um pequeno filtro sobre o tamanho das frases.
        if ( v1.length < 7 || v2.length < 7 )  return;

        //System.out.print("\tCODES OK\n");
        NWunsch nws = new NWunsch(v1, v2);
        nws.setDic(dict);
        //System.out.print("\tDICT OK\n");
        nws.buildMatrix();
        //System.out.print("\tMATRIX OK\n");
        nws.buildAlignment();
        //System.out.print("\tALIGN OK\n");
        nws.printAlignmentH(idpar);
    }

    
    public static void main(String[] args) {
        String[] v= new String[]{
            "Shocked locals claimed officers found explosives in a back garden after officers raided a home on a leafy.",
            "Shocked locals have described how officers raided a home on a leafy."
        };
        Sentence sa= new Sentence(v[0]);
        Sentence sb= new Sentence(v[1]);
        
        NWunsch nw= new NWunsch(sa, sb);
        String[] vs= nw.getAlignmentH();
        o.print("\n");
        o.printf("sa ---> [%s]\n", vs[0]);
        o.printf("sb ---> [%s]\n", vs[1]);
        o.print("\n");
    }
    
    
    public static void mainX1(String[] args) {
        if (args.length >= 1) {
            processParaFile(args[0]);
        }
        else {
            processParaFile(null);
        }
    }

    public static void mainX(String[] args) {
        int[] va = {
            8, 9, 3, 2, 1, 7, 7, 5};
        int[] vb = {
            8, 9, 7, 7, 5};

        NWunsch nws = new NWunsch(va, vb);
        nws.printvectors();
        System.out.print("\n\n");
        nws.buildMatrix();
        nws.buildAlignment();
        nws.printAlignmentH(1);
        System.out.print("\n\n");
        nws.printMatrix();
    }
}

