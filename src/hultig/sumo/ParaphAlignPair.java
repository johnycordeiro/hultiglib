/*************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2012 UBI/HULTIG All rights reserved.
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
package hultig.sumo;

import java.util.Vector;
import java.io.PrintStream;


import hultig.align.*;
import hultig.util.Toolkit;

/**
 * <p>
 * This class represents an aligned paraphrase pair, that is a paraphrasic
 * sentence pair having their common and similar words aligned. This class
 * enables alignment representation with various levels of interpretation:
 * lexical, syntactical, and at the chunk level.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @since 10:44:43 8/Mai/2008
 * @author João Paulo Cordeiro (10:44:43 8/Mai/2008)
 */
public class ParaphAlignPair {

    /**
     * The array of words from one sentence.
     */
    private Word[] wa;
    /**
     * The array of words from the other sentence.
     */
    private Word[] wb;
    /**
     * One sentence, represented at a chunk level.
     */
    private ChunkedSentence csa;
    /**
     * The other sentence, represented at a chunk level.
     */
    private ChunkedSentence csb;
    
    /**
     *  Store the pair identifier, possibly gotten from an XML file.
     */
    private int ID= 0;
    
    private static final byte LEFTEXTREME= -1;
    private static final byte NORMALBUBBLE= 0;
    private static final byte RIGHTEXTREME= 1;
    
    
    

    /**
     * The default constructor is based on the two sentence strings.
     * @param sa One sentence string.
     * @param sb The other sentence string.
     */
    public ParaphAlignPair(String sa, String sb) {
        this(sa, sb, null);
    }

    /**
     * A more general constructor where a language model is taken to be
     * used for shallow parsing.
     * @param sa One sentence string.
     * @param sb The other sentence string.
     * @param model The language model.
     */
    public ParaphAlignPair(String sa, String sb, OpenNLPKit model) {
        String[] va = sa.split("  *");
        String[] vb = sb.split("  *");

        int na = va.length;
        int nb = vb.length;
        int m = Math.max(na, nb);
        wa = new Word[m];
        wb = new Word[m];
        for (int i = 0; i < m; i++) {
            String sia = i < na ? va[i] : Toolkit.sline('_', vb[i].length());
            String sib = i < nb ? vb[i] : Toolkit.sline('_', va[i].length());
            wa[i] = new Word(sia);
            wb[i] = new Word(sib);
        }

        if (model != null) {
            csa = new ChunkedSentence(Toolkit.sclean(sa, "_"), model);
            csb = new ChunkedSentence(Toolkit.sclean(sb, "_"), model);
            //-------------------------------------------
            //===> 2014/01/08 JPC
            //-------------------------------------------
            POSType postype= new POSType();
            for (int i=0; i<csa.size(); i++) {
                Word w= csa.get(i);
                if ( w.isRPUNCT() ) w.setPOS("PCT");
                if ( i < 1 && w.equals("...") ) w.setPOS("BGN");
                if ( i == csa.size()-1 && w.charAt(0)=='~' ) w.setPOS("END");
            }
            for (int i=0; i<csb.size(); i++) {
                Word w= csb.get(i);
                if ( w.isRPUNCT() ) w.setPOS("PCT");
                if ( i < 1 && w.equals("...") ) w.setPOS("BGN");
                if ( i == csb.size()-1 && w.charAt(0)=='~' ) w.setPOS("END");
            }
            //-------------------------------------------
            // Para marcar a pontuação. Preparação para
            // o processamento do DUC, para o Mohammad.
            //-------------------------------------------
        } else {
            csa = null;
            csb = null;
        }
    }
    
    
    public void setID(int id) {
        this.ID= id;
    }
    
    
    public int getID() {
        return this.ID;
    }
    
    
    public Word[][] getWordAlignment() {
        Word[][] a= new Word[2][];
        a[0]= wa.clone();
        a[1]= wb.clone();
        return a;
    }
    

    /**
     * Uses the Needleman Wunsch algorithm for globally align the
     * paraphrasic sentences of this class.
     * @param postype The definition of the POS tags, to mark the
     * generated alignment.
     */
    public void align(POSType postype) {
        NWunsch nw = new NWunsch(csa, csb);
        String[] vs = nw.getAlignmentH();
        if (vs == null) {
            return;
        }

        String[] va = vs[0].split("  *");
        String[] vb = vs[1].split("  *");

        if (va.length != vb.length) {
            System.err.printf("ERROR: in method %s.align()  (a)\n", getClass().getName());
            return;
        }

        int n = va.length;
        CorpusIndex d = nw.getDict();

        wa = new Word[n];
        wb = new Word[n];
        for (int i = 0, ia = 0, ib = 0; i < n; i++) {
            wa[i] = new Word(va[i]);
            wb[i] = new Word(vb[i]);

            if (va[i].charAt(0) == '_') {
                wa[i].setLexCod(-1);
                wa[i].setPosCod(-1);
                wa[i].setChkCod(-1);
            } else {
                Word csak = csa.get(ia);
                int lexcod = d.get(va[i]);
                int poscod = postype.str2cod(csak.getPOS());
                wa[i].setLexCod(lexcod);
                wa[i].setPosCod(poscod);
                wa[i].setChkCod(csa.get(ia).getChkCod());
                csak.setLexCod(lexcod);
                csak.setPosCod(poscod);
                ia++;
            }

            if (vb[i].charAt(0) == '_') {
                wb[i].setLexCod(-1);
                wb[i].setPosCod(-1);
                wb[i].setChkCod(-1);
            } else {
                Word csbk = csb.get(ib);
                int lexcod = d.get(vb[i]);
                int poscod = postype.str2cod(csbk.getPOS());
                wb[i].setLexCod(lexcod);
                wb[i].setPosCod(poscod);
                wb[i].setChkCod(csb.get(ib).getChkCod());
                csbk.setLexCod(lexcod);
                csbk.setPosCod(poscod);
                ib++;
            }
        }
    }

    /**
     * Codifies the aligned sentences according to a given corpus index.
     * @param dic The corpus index.
     */
    public void codify(CorpusIndex dic) {
        if (dic == null) {
            return;
        }
        
        for (int i = 0; i < wa.length; i++) {
            String swai= wa[i].toString(); 
            String swbi= wb[i].toString();
            if (swai.charAt(0) != '_') {
                wa[i].setLexCod(dic.get(swai));
            }
            else if ( i==0 && swai.equals("...")) {
                wa[i].setLexCod(-8); //==> inspirado na expressão: "nem oito nem oitenta".
            }
            else {
                wa[i].setLexCod(-1);
            }
            if (swbi.charAt(0) != '_') {
                wb[i].setLexCod(dic.get(swbi));
            }
            else if ( swbi.charAt(0) == '~' ) {
                wb[i].setLexCod(-80); //==> inspirado na expressão: "nem oito nem oitenta".
            }
            else {
                wb[i].setLexCod(-1);
            }
        }

    }

    /**
     * Codifies the chunks of the aligned sentences according to a given
     * part-of-speech tag set.
     * @param postype The POS set considered.
     */
    public void codifyChunks(POSType postype) {
        codifyChunks(wa, csa, postype);
        codifyChunks(wb, csb, postype);
    }

    /**
     * Codifies an array of words, representing a chunked sentence, according
     * to the codification already defined for a given chunked sentence.
     * @param wv The array of words.
     * @param cs The chunked sentence.
     * @param postype The set of POS tags
     */
    private void codifyChunks(Word[] wv, ChunkedSentence cs, POSType postype) {
        int k = 0;

        for (int i = 0; i < cs.getNumChunks() && k < wv.length; i++) {
            Chunk ci = cs.getChunk(i);

            for (int j = 0; j < ci.size() && k < wv.length; j++) {
                Word wij = ci.get(j);
                while (k < wv.length && wv[k].toString().startsWith("_")) {
                    wv[k].setChkCod(-1);
                    wv[k].setPosCod(-1);
                    wv[k].setLexCod(-1);
                    k++;
                }
                if (k < wv.length && wij.toString().equals(wv[k].toString())) {
                    int cod = ChunkType.str2cod(ci.POS());
                    wv[k].setChkCod(cod);
                    String spos = wij.getPOS();
                    int xcod = postype.str2cod(spos);
                    wv[k].setPosCod(xcod);
                    k++;
                }
            }
        }
    }

    /**
     * The length of this alignment, in terms of the number of tokens in
     * each sentence, including the void tokens, marked usualy with
     * sequences of underscores.
     * @return The alignment size/lenght.
     */
    public int size() {
        return wa.length;
    }

    /**
     * Gives a sub-sequence of this alignment, delimited by two positions.
     * @param a The left position.
     * @param b The right positions
     * @return The two strings from the aligned sub-sequence.
     */
    public String[] subSequence(int a, int b) {
        if (a < 0) {
            a = 0;
        }
        if (b > size()) {
            b = size();
        }
        if (a >= b) {
            return null;
        }
        StringBuffer[] vsb = new StringBuffer[2];
        vsb[0] = new StringBuffer(wa[a].toString());
        vsb[1] = new StringBuffer(wb[a].toString());
        for (int k = a + 1; k < b; k++) {
            String wka = wa[k].toString();
            String wkb = wb[k].toString();
            if (wka.length() != wkb.length()) {
                int n = Math.max(wka.length(), wkb.length());
                wka = Toolkit.padLeft(wka, n);
                wkb = Toolkit.padLeft(wkb, n);
            }

            vsb[0].append(' ').append(wka);
            vsb[1].append(' ').append(wkb);
        }

        String[] vs = {vsb[0].toString(), vsb[1].toString()};
        return vs;
    }

    /**
     * Extracts all possible bubbles from an aligned paraphrase.
     * For extracting a bubble a certain confidence criteria must
     * hold, namely the length of the contexts (left and right) must
     * outweigh the length of the middle region.
     * @param minValue The minimum value upon which a bubble is
     * extracted.
     * @return The list with all bubbles found in this pair.
     */
    public Vector<XBubble> extractNXBubbles(double minValue) {
        Vector<XBubble> vbub = new Vector<XBubble>();
        SegmentAlign[] vsa = getSegAligns();
        int n = vsa.length;
        for (int i = 0; i < n; i++) {
            // for each "depression" ...
            if (vsa[i].value < 0) {
                double leftv = 0.0;
                double righv = 0.0;
                if (0 < i) {
                    leftv = vsa[i - 1].value;
                }
                if (i < n - 1) {
                    righv = vsa[i + 1].value;
                }
                if (leftv == 0.0 || righv == 0.0) {
                    continue;
                }

                double valorTi = vsa[i].value + leftv + righv;
                if (valorTi >= minValue) {
                    int nL = 0;
                    if (leftv != 0.0) {
                        nL = vsa[i - 1].b - vsa[i - 1].a + 1;
                    }

                    int nR = 0;
                    if (righv != 0.0) {
                        nR = vsa[i + 1].b - vsa[i + 1].a + 1;
                    }

                    XBubble xbub = extBub(nL, vsa[i].a, vsa[i].b, nR);
                    if (xbub != null) {
                        vbub.add(xbub);
                    }
                }
            }
        }
        return vbub;
    }


    /**
     * Extracts all possible bubbles from an aligned paraphrase.
     * For extracting a bubble a certain confidence criteria must
     * hold, namely the length of the contexts (left and right) must
     * outweigh the length of the middle region.
     * @return The list with all bubbles found in this pair.
     */
    public Vector<XBubble> extractNXBubbles() {
        return extractNXBubbles(0.0);
    }

    /**
     * A new version of the {@link #extractNXBubbles() extractNXBubbles()} method
     * in which the {@code BEGIN} and {@code END} meta-tags are considered. If one
     * of the contexts is equal to one of this tags, the value of the bubble is
     * recomputed differently taking into account only the other context and 
     * doubling its value.
     * @since 2012-04-25 11:45
     * @return The list with all bubbles found in the pair.
     */
    public Vector<XBubble> extractBubblesWithBoundaries() {
        Vector<XBubble> bubbles = new Vector<XBubble>();
        SegmentAlign[] depressions = getSegAligns();
        for (int i = 1; i < depressions.length-1; i++) {
            // for each "depression" ...
            //System.out.printf("i:%d\n", i);
            if (depressions[i].value < 0) {
                double leftv = 0.0;
                double righv = 0.0;
                int depressionCode= depressionWithBoundary(i,depressions);
                if (  depressionCode == LEFTEXTREME ) {
                    righv= depressions[i+1].value;
                    leftv= righv;
                }
                else if ( depressionCode == RIGHTEXTREME ) {
                    leftv= depressions[i-1].value;
                    righv= leftv;
                }
                else { //==> more general case (code == 0): NORMALBUBBLE.
                    leftv= depressions[i-1].value;
                    righv= depressions[i+1].value;
                }

                double valorTi = depressions[i].value + leftv + righv;
                //System.out.printf("valorTi(%d) = %f\n", i, valorTi);
                if (valorTi >= 0.0) {
                    int nL = 0;
                    if (leftv != 0.0) {
                        nL = depressions[i - 1].b - depressions[i - 1].a + 1;
                    }

                    int nR = 0;
                    if (righv != 0.0) {
                        nR = depressions[i + 1].b - depressions[i + 1].a + 1;
                    }

                    XBubble xbub = extBub(nL, depressions[i].a, depressions[i].b, nR);
                    if (xbub != null) {
                        bubbles.add(xbub);
                    }
                }
            }
        }
        return bubbles;
    }


    private int depressionWithBoundary(int i, SegmentAlign[] depressions) {
        if ( i == 1 ) {
            if ( depressions[i-1].a - depressions[i-1].b == 0 && wa[0].toString().equalsIgnoreCase("XBEGIN") )
                return LEFTEXTREME;
        }
        else if(i < depressions.length - 1) {
            String sa= wa[depressions[i+1].a].toString();
            String tester= "XEND.!?";
            if ( tester.indexOf(sa) >= 0 ) {
                //System.out.printf("sa -----> [%s]\n", sa);
                return RIGHTEXTREME;
            }
        }
        return NORMALBUBBLE;
    }



    /**
     * Extracts all sub-segments from a previously identified bubble.<br />
     * <b>Illustration:</b>
     * <pre>
     *      0 1 2 3   4 5 6 7 8   9 0 1 2   3 4 5 6 7 8
     *      -------------------------------------------
     *      X Y Z _ | A B J C D | X Y Z _ | U K A | K A P
     *      _ _ _ T | A B L C D | _ _ _ S | U K A | _ _ _
     *                            |     |
     * val:   -4         4.1      | -4  |      3     -3
     *                            V     V
     *                  nL=5      a     b    nR=3
     * </pre>
     *
     * JPC, Dez 2008
     *
     * @param nL Number of elements in the left context.
     * @param a The bubble left limit.
     * @param b The bubble right limit.
     * @param nR Number of elements in the right context.
     * @return A new bubble.
     */
    private XBubble extBub(int nL, int a, int b, int nR) {
        //Left
        Word[] vL = null;
        if (nL > 0) {
            vL = new Word[nL];
            System.arraycopy(wa, a - nL, vL, 0, nL);
        }
        /*
        if (numTrueWords(vL) == 0) {
            return null; //----> EXIT
        }
         */
        //Right
        Word[] vR = null;
        if (nR > 0) {
            vR = new Word[nR];
            System.arraycopy(wa, b + 1, vR, 0, nR);
        }
        /*
        if (numTrueWords(vR) == 0) {
            return null; //----> EXIT
        }
         */
        //Kernel
        int n = b - a + 1;
        Word[][] vX = new Word[2][n];
        System.arraycopy(wa, a, vX[0], 0, n);
        System.arraycopy(wb, a, vX[1], 0, n);
        for (int j = 0; j < vX[0].length; j++) {
            if (vX[0][j].charAt(0) == '_') {
                vX[0][j] = null;
            }
            if (vX[1][j].charAt(0) == '_') {
                vX[1][j] = null;
            }
        }
        if (numTrueWords(vX[0]) == 0 && numTrueWords(vX[1]) == 0) {
            return null; //----> EXIT
        }
        return new XBubble(vL, vX, vR);
    }

    /**
     * Count the number of true words, contained in a given array
     * of words. Here a string is considered as a word if it
     * starts with a letter and ends also with a letter or a
     * digit.
     * @param v The given array of words.
     * @return A value between 0 and v.lenght-1.
     */
    public static int numTrueWords(Word[] v) {
        int n = 0;
        for (int i = 0; i < v.length; i++) {
            Word wi = v[i];
            if (wi == null) {
                continue;
            }
            char c1 = wi.charAt(0);
            char cn = wi.charAt(wi.length() - 1);
            if (Character.isLetter(c1) && Character.isLetterOrDigit(cn)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Gives the array of sub-sequences to be considered for bubble extraction.
     * @return The array of sub-sequences or null.
     */
    private SegmentAlign[] getSegAligns() {
        if (wa == null || wa.length < 1) {
            return null;
        }

        int a = 0;
        double soma = compareWords(wa[0], wb[0]);
        Vector<SegmentAlign> vsa = new Vector<SegmentAlign>();
        for (int i = 1; i < wa.length; i++) {
            double cab = compareWords(wa[i], wb[i]);
            if (soma * cab > 0) //---> permanência de sinal.
            {
                soma += cab;
            } else {
                int b = i - 1;
                vsa.add(new SegmentAlign(a, b, soma));
                a = b + 1;
                soma = cab;
            }
        }
        vsa.add(new SegmentAlign(a, wa.length - 1, soma));

        SegmentAlign[] vs = new SegmentAlign[vsa.size()];
        vs = vsa.toArray(vs);
        return vs;
    }

    /**
     * Compares two words, based on their lexical codes.
     * @param wi One word.
     * @param wj The other word.
     * @return The comparison value, which in this case
     * might be: -1 (lexical code not defined), 1 (equal),
     * or 0.1 (different).
     */
    private double compareWords(Word wi, Word wj) {
        int ci = wi.getLexCod();
        int cj = wj.getLexCod();

        if (ci == -1 || cj == -1) {
            return -1.0;
        }
        if (ci == cj) {
            return 1.0;
        }
        return 0.1;
    }

    /**
     * A shortcut for the {@link #print(int level) print(int level)}
     * method, with level equal to 3.
     */
    public void print() {
        print(3);
    }

    /**
     * Outputs this alignment.
     * @param level A code stating the amount of information to
     * be printed in the standard output.
     */
    public void print(int level) {
        PrintStream o = System.out;
        String[] vs = subSequence(0, size());
        o.print("ALIGNED SENTENCES:\n");
        o.printf("   sa ---> %s\n", vs[0]);
        o.printf("   sb ---> %s\n", vs[1]);
        o.println();

        if (level > 1 && csa != null && csb != null) {
            o.print("CHUNKED SENTENCES:\n");
            o.printf("   csa--> %s\n", csa.toStringChunk());
            o.printf("   csb--> %s\n", csb.toStringChunk());
        }

        if (level > 2) {
            o.print("ARRAY OF WORDS:\n");
            o.print("   wa ---> ");
            for (int k = 0; k < wa.length; k++) {
                o.printf("(%s, %3d, %2d, %2d)   ", wa[k], wa[k].getLexCod(), wa[k].getPosCod(), wa[k].getChkCod());
            }
            o.print("\n");
            o.print("   wb ---> ");
            for (int k = 0; k < wb.length; k++) {
                o.printf("(%s, %3d, %2d, %2d)   ", wb[k], wb[k].getLexCod(), wb[k].getPosCod(), wb[k].getChkCod());
            }
        }
        o.print("\n");
    }
    
    
    public void printWithSyntaxInfo(POSType pos) {      
        PrintStream o = System.out;
        
        StringBuffer[] v = new StringBuffer[6];
        for (int i = 0; i < v.length; i++) 
            v[i]= new StringBuffer("");
        
        for (int k = 0; k < size(); k++) {
            String wka = wa[k].toString();
            String wkb = wb[k].toString();
            String posa= wa[k].getPosCod() < 0 ? " " : pos.cod2str(wa[k].getPosCod());
            String posb= wb[k].getPosCod() < 0 ? " " : pos.cod2str(wb[k].getPosCod());
            String chka= wa[k].getChkCod() < 0 ? " " : ChunkType.cod2str(wa[k].getChkCod());
            String chkb= wb[k].getChkCod() < 0 ? " " : ChunkType.cod2str(wb[k].getChkCod());
            
            int n = Toolkit.maximum(
                    wka.length(),  wkb.length(),  posa.length(),  posb.length(),
                    chka.length(), chkb.length()
                    );
            wka = Toolkit.padRight(wka, n);
            wkb = Toolkit.padRight(wkb, n);
            posa= Toolkit.padRight(posa, n);
            posb= Toolkit.padRight(posb, n);
            chka= Toolkit.padRight(chka, n);
            chkb= Toolkit.padRight(chkb, n);
       
            v[0].append(wka);
            v[1].append(wkb);
            v[2].append(posa);
            v[3].append(posb);
            v[4].append(chka);
            v[5].append(chkb);
            if ( k < size()-1 )
                for (int i = 0; i < v.length; i++) v[i].append(' ');
        }
        //System.out.println();
        o.printf("   %s\n", v[4]);
        o.printf("   %s\n", v[2]);
        o.printf("   %s\n", v[0]);
        o.printf("   %s\n", v[1]);
        o.printf("   %s\n", v[3]);
        o.printf("   %s\n", v[5]);
    }
    
    
    public static String strPad(int padSize, String s) {
        String format= String.format("%%%ds", padSize);
        return String.format(format, s);
    }
    

    /**
     * Gives a pair of xml tags related to a given chunk code, as defined in
     * {@link #ChunkType ChunkType} class. The returned tags are used for
     * chromatic tagging in chunks.
     * @param cod The ChunkType code.
     * @return A pair of xml open and close tags.
     */
    private String[] colorTags(int cod) {
        String[] colortag = new String[]{
            cod >= 0 ? '<' + ChunkType.cod2str(cod) + '>' : "",
            cod >= 0 ? "</" + ChunkType.cod2str(cod) + '>' : ""
        };
        return colortag;
    }

    /**
     * Gives the string pair containing this alignment, marked with XML chromatic tags.
     * See {@link colorTags(int cod) colorTags(int cod)}.
     * @return The alignment pair.
     */
    public String[] colorizedChunks() {
        return colorizedChunks(null);
    }

    /**
     * Gives the string pair containing this alignment, marked with XML chromatic tags.
     * See {@link colorTags(int cod) colorTags(int cod)}. If the parameter flag is
     * active (true), and array with four strings is returned, where the third and
     * fourth ones are corresponding part-of-speech strings for the first and
     * second strings. These last two contain the chromatic marked sentences.
     * @param withPOS The part-of-speech flag.
     * @return An array with two or four strings, depending on the {@code withPOS}
     * flag.
     */
    public String[] colorizedChunks(POSType post) {
        if (wa == null || wb == null) {
            return null;
        }
        int n = wa.length;
        if (n != wb.length) {
            return null;
        }
        int cod_a = wa[0].getChkCod();
        int cod_b = wb[0].getChkCod();
        String[] cortag_a = colorTags(cod_a);
        String[] cortag_b = colorTags(cod_b);

        StringBuffer[] vs = post != null ? new StringBuffer[4] : new StringBuffer[2];
        int szWord = Math.max(wa[0].length(), wb[0].length());
        vs[0] = new StringBuffer(cortag_a[0] + Toolkit.padLeft(wa[0].toString(), szWord));
        vs[1] = new StringBuffer(cortag_b[0] + Toolkit.padLeft(wb[0].toString(), szWord));
        if (post != null) {
            String posa = wa[0].getPOS(post);
            if (posa.equals("UDF")) {
                posa = " ";
            }
            String posb = wb[0].getPOS(post);
            if (posb.equals("UDF")) {
                posb = " ";
            }
            vs[2] = new StringBuffer(Toolkit.padLeft(posa, szWord) + ' ');
            vs[3] = new StringBuffer(Toolkit.padLeft(posb, szWord) + ' ');
        }
        for (int k = 1; k < n; k++) {
            //primeira sequência.
            int codak = wa[k].getChkCod();
            if (codak != cod_a) {
                vs[0].append(cortag_a[1]); //tag de fecho
                cortag_a = colorTags(codak); //novas tags
                cod_a = codak;
                vs[0].append(" ").append(cortag_a[0]);
            } else {
                vs[0].append(" ");
            }
            szWord = Math.max(wa[k].length(), wb[k].length());

            vs[0].append(Toolkit.padLeft(wa[k].toString(), szWord));

            //segunda sequência.
            int codbk = wb[k].getChkCod();
            if (codbk != cod_b) {
                vs[1].append(cortag_b[1]); //tag de fecho
                cortag_b = colorTags(codbk); //novas tags
                cod_b = codbk;
                vs[1].append(" ").append(cortag_b[0]);
            } else {
                vs[1].append(" ");
            }
            vs[1].append(Toolkit.padLeft(wb[k].toString(), szWord));

            if (post != null) {
                String posa = wa[k].getPOS(post);
                if (posa.equals("UDF")) {
                    posa = " ";
                }
                String posb = wb[k].getPOS(post);
                if (posb.equals("UDF")) {
                    posb = " ";
                }
                vs[2].append(Toolkit.padLeft(posa, szWord)).append(' ');
                vs[3].append(Toolkit.padLeft(posb, szWord)).append(' ');
            }
        }
        vs[0].append(cortag_a[1]);
        vs[1].append(cortag_b[1]);

        return post != null
                ? new String[]{vs[0].toString(), vs[1].toString(), vs[2].toString(), vs[3].toString()}
                : new String[]{vs[0].toString(), vs[1].toString()};
    }

    /**
     * Outputs this alignment marked with XML chromatic tags.
     */
    public void printWithColors() {
        PrintStream o = System.out;
        String[] vs = this.colorizedChunks();
        if (vs == null) {
            return;
        }
        o.printf("   %s\n", vs[0]);
        o.printf("   %s\n", vs[1]);
    }
    
    

    /**
     * The main method exemplifies the use of this class.
     * @param args
     */
    public static void main(String[] args) {
        PrintStream o = System.out;
        o.print("\n[LOADING LANGUAGE MODELS]\n");
        OpenNLPKit model = new OpenNLPKit("/a/tools/opennlp-tools-1.5.0/models/english/");
        model.loadTokenizer();
        model.loadSentenceDetector();
        model.loadTagger();
        model.loadChunker();
        o.print("[LANGUAGE MODELS LOADED]\n\n");

        POSType postype = new POSType();
        /*
        if (!postype.loadXML("/a/pen-pos-type.xml")) {
            return;
        }*/
        //postype.print();
        o.print("\n");

        //DATA SET
        String[] vs = new String[]{
            /** /
            "president george w bush and secretary of state condoleezza rice had  worked personally",
            "president ______ _ bush and secretary of state ___________ rice have worked __________",

            "on friday bush said the ____ historic agreement would _____ place india",
            "__ ______ bush said the deal ________ _________ would bring _____ india",

            "___ _______ _______ external affairs minister pranab mukherjee ________ echoed his views and said the passage of the bill.",
            "the india's foreign ________ affairs minister pranab mukherjee welcomed ______ ___ _____ ___ ____ the passage of the bill.",

            "_____ _______ facing a financial crisis that is _____ ___ ____ going to get worse , obama said __ _____ ___ ____ ____ his top ____ ________ priority is _____ building a recovery plan equal to the task . ",
            "asked whether ______ _ _________ ______ that is still his plan _____ __ ___ _____ , obama said we don't yet know what ___ the best approach ________ is going ________ _ ________ ____ _____ to be  ____ . ",

            "the president-elect waved concerns about the rising deficit , commenting , \" we 've got to provide a blood infusion to the patient right now __ ____ ____ ___ _______ __ __________ .",
            "___ _______________ _____ ________ _____ ___ ______ _______ _ __________ _ \" we 've got to provide a blood infusion to the patient right now to make sure the patient is stabilized .",

            "__________ ____ the alaska volcano observatory  has been monitoring activity _______________ around the clock since the weekend .",
            "scientists from the alaska volcano observatory have been monitoring activity round-the-clock ______ ___ _____ since the weekend .",
            /**/

            "Like any other contract , equipment maintenance contracts are negotiable .",
            "____ ___ _____ ________ _ Equipment maintenance contracts are negotiable .",
            
            "The project is under construction at many organizations _ ____________ __ ______ !",
            "The project is under construction at many organizations , particularly in Europe !",
        };

        CorpusIndex idx = new CorpusIndex();
        for (int i = 0; i < vs.length; i++) {
            idx.add(vs[i]);
            vs[i]= "... " + vs[i] + " ~"; 
            //==> Nova forma de introduzir delimitadores silenciosos.
        }   //    Veio substituir o BEGIN eo END,  2014/01/07 13:50 GMT.
        idx.rebuild();

        for (int k = 0; k < vs.length; k += 2) {
            System.out.print("#############################\n");
            System.out.printf("#######   EXAMPLE %d   #######\n", (k + 2) / 2);
            System.out.print("#############################\n\n");
            ParaphAlignPair pa = new ParaphAlignPair(vs[k], vs[k + 1], model);
            pa.codify(idx);
            pa.align(postype);
            pa.printWithSyntaxInfo(postype);
            //o.print("\n");
            //pa.print();
            
            // ATTENTION: JUMPING DOOR !!!
            //------------------------------
            //if ( true ) continue;
            //------------------------------

            /* UNCOMMENT FOR TESTING */
            System.out.println("SEGMENTS");
            SegmentAlign[] vsa = pa.getSegAligns();
            for (int i = 0; i < vsa.length; i++) {
                System.out.printf("   [%2d, %2d] ---> value: %7.1f\n", vsa[i].a, vsa[i].b, vsa[i].value);
            }
            /**/

            o.print("EXTRACTED BUBBLES:\n");
            //Vector<XBubble> vb = pa.extractNXBubbles();
            Vector<XBubble> vb = pa.extractBubblesWithBoundaries();
            for (XBubble b : vb) {
                o.printf("   %s\n", b);
            }

            /* UNCOMMENT FOR TESTING
            o.print("\nCHROMATIC CHUNKS\n");
            pa.printWithColors();
             */

            o.print("\n\n\n");
        }

        o.print("\n\n\n");
    }
}

/**
 * An auxiliary class used inside ParaphAlignPair, representing
 * the boundaries and value of an aligned subsegment. This 
 * class consists basically in a data structure storing the key
 * positions of an aligned subsegment.
 * @author João Paulo Cordeiro.
 */
class SegmentAlign {

    /**
     * The left boundary of the aligned sub-segment.
     */
    int a;
    /**
     * The right boundary of the aligned sub-segment.
     */
    int b;
    /**
     * The value of the represented sub-sequence. It comprehends two polarities, one
     * positive and the other negative. The positive value is used when the
     * sub-sequence consists of not null aligned symbols. The negative value when we
     * have null (empty) symbols in one of the sequences.
     */
    double value;

    /**
     * The default constructor requires all the three sub-segment information
     * parameters.
     * @param a The left boundary position.
     * @param b The right boundary position.
     * @param value The value of the represented sub-segment.
     */
    public SegmentAlign(int a, int b, double value) {
        this.a = a;
        this.b = b;
        this.value = value;
    }
}
