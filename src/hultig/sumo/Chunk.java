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
package hultig.sumo;

import hultig.util.*;

import java.util.ArrayList;

/**
 * <p>This class represents a phrasal chunk from a sentence. A shallow parser
 * divides a given sentence into a sequence of chunks, where each one is formed
 * by a sequence of one or more words. For example, the following sentence:
 * <p>
 * <pre>   The brown fox jumped over the fence.</pre>
 * <p>
 * has the following chunks:
 * </p>
 * <pre>   [NP The/DT brown/JJ fox/NN] [VP jumped/VBD] [PP over/IN] [NP the/DT fence/NN] ./.</pre>
 * <p>
 * two noun phrases ({@code NP}), one verb phrase ({@code VP}), and one prepositional
 * phrase ({@code PP}).
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 * 
 * @author João Paulo Cordeiro
 * @version 1.0
 */
@SuppressWarnings("static-access")
public class Chunk extends ChunkMark
{
    /**
     * The sequence of words contained in this phrasal chunk.
     */
    private final Word[] words;

    
    /**
     * This constructor requires a ChunkedSentence, which is a sentence marked with
     * chunk positions through an array of ChunkMark objects. A ChunkedSentence is
     * a specialization of a Sentence, which contains a {@code ChunkMark} array
     * defining the boundaries of each chunk. The constructor takes a
     * {@code ChunkedSentence} and a {@code ChunkMark} object to create an
     * instance of this class.
     * @param cs The chunked sentence.
     * @param cm The chunk mark.
     */
    public Chunk(ChunkedSentence cs, ChunkMark cm) {
        super(cm.a(), cm.b(), cm.POS());
        words= new Word[b-a+1];
        for (int i=0; i<words.length; i++) {
            words[i]= cs.get(a+i);
        }
    }

    /**
     * Gives the number of words contained in this chunk.
     * @return The number of words.
     */
    public int size() {
        return words.length;
    }

    /**
     * Gives the word at a given position, from the chunk's
     * sequence of words.
     * @param i The position in the chunk.
     * @return The word obtained. On error returns null.
     */
    public Word get(int i) {
        try {
            return words[i];
        }
        catch (Exception exc) {
            return null;
        }
    }

    /**
     * Gives the token from this chunk at a given position.
     * @param i The word position, in the chunk word sequence.
     * @return The corresponding token or null if something
     * gets wrong (e.g. invalid position).
     */
    public String getToken(int i) {
        try {
            return words[i].toString();
        }
        catch (Exception exc) {
            return null;
        }
    }

    /**
     * Gives the word's part-of-speech, at a given position from
     * this chunk word sequence.
     * @param i The word position, in the chunk word sequence.
     * @return The POS tag for the word at the given position, or null
     * if something gets wrong.
     */
    public String getPOS(int i) {
        try {
            return words[i].getPOS();
        }
        catch (Exception exc) {
            return null;
        }
    }

    /**
     * This function computes the connection strength between two chunks,
     * measured in terms of a numeric value. This function is based in
     * the lexical connectivity between the chunks, as well as the word's
     * POS relatedness.
     * @param cother The other chunk to compare with.
     * @return The connection strength, a value laying in the [0, 1] interval.
     */
    public double connection(Chunk cother) {
        if ( cother == null ) return 0.0;
        if ( this.POS() == null &&  cother.POS() != null ) return 0.0;
        if ( this.POS() != null &&  cother.POS() == null ) return 0.0;

        //---------------------------------
        //Compute word conectivity matrix.
        //---------------------------------
        int m= this.size();
        int n= cother.size();

        double[][] A= new double[m][n];
        ArrayList<MatrixItem> vA= new ArrayList<MatrixItem>(m*n);
        for (int i=0; i<m; i++) {
            Word wi= this.get(i);
            String wiPOS= wi.getPOS(2);

            for (int j = 0; j < n; j++) {
                Word wj = cother.get(j);
                String wjPOS = wj.getPOS(2);
                A[i][j] = !wiPOS.equals(wjPOS) ? 0.0 : wi.connectProb(wj);

                //The matrix ordered vector
                boolean inserted= false;
                MatrixItem Aij= new MatrixItem(i, j, A[i][j]);
                for (int k = 0; k < vA.size(); k++) {
                    if (A[i][j] > vA.get(k).aij()) {
                        vA.add(k, Aij);
                        inserted= true;
                        break;
                    }
                }
                if ( !inserted ) vA.add(Aij);
            }
        }
        //---------------------------------

        double connection= 0.0;
        ArrayList<MatrixItem> vbest= Matrix.selectBestAssignment(vA, Math.min(m,n));
        for (int k=0; k<vbest.size(); k++) {
            int i= vbest.get(k).i();
            int j= vbest.get(k).j();
            Word wi= this.get(i);
            Word wj= cother.get(j);
            connection+= A[i][j];
        }

        return vbest.isEmpty() ? 0 : connection/vbest.size() ;
    }
    
    /**
     * Test if both chunks have the same POS tag.
     * @param cother The chunk to compare with.
     * @return The true value on success, and false otherwise.
     */
    public boolean eqaulPOS(Chunk cother) {
        return POS().equals(cother.POS()) ? true : false;
    }
    
    /**
     * Tests if this chunk word sequence is equal to a given string.
     * @param sc The string to compare to.
     * @return The true value on success, and false otherwise.
     */
    public boolean equal(String sc) {
        return toString().equals(sc) ? true : false; 
    }
     
    /**
     * Gives the index of a tagged word, represented by a string, in
     * this Chunk.
     * @param cw The tagged word, for example: "addicted/VBN"
     * @return The index occurrence, or -1 if not found.
     */
    public int index(String cw) {
        if ( cw == null ) return -1;
        for (int i=0; i<words.length; i++)
            if ( cw.equals(words[i].toStringPOS() ) )  return i;
        
        return -1;
    }
    
    /**
     * Test if a tagged word occurs in this chunk. This method is similar
     * to {@link #index(java.lang.String) index}.
     * @param cw The tagged word, for example: "addicted/VBN"
     * @return The {@code true} value if contained, and {@code false}
     * otherwise.
     */
    public boolean contains(String cw) {
        return index(cw) < 0 ? false : true;
    }
    
    /**
     * Gives a string representation of this chunk, in the form of:
     * {@code POS[w1 w2 ... wn]}, where {@code POS} is the chunk
     * part-of-speech tag and {@code w1 ... wn} the sequence of n
     * words forming this chunk.
     * @return A string representation of this chunk in the previously
     * described format.
     */
    @Override
    public String toString() {
        String cpos= POS();
        if ( cpos == null || "UNDEFINED".equals(cpos) ) {
            if ( words.length == 1 && words[0].isRPUNCT() ) {
                return "[PUNCT "+words[0].toStringPOS()+"]";
            }
        }
        StringBuilder sb= new StringBuilder("["+cpos);
        for (Word w : words) {
            sb.append(' ').append(w.toStringPOS());
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Gives another string representation of this chunk, in the
     * form of: {@code POS:<w1 w2 ... wn>:POS}, where {@code POS}
     * is the chunk tag, and {@code w1 ... wn} are the sequence
     * of words in this chunk. The method was thought to
     * create regular expressions representing sentence reduction
     * rules. (JPC, 13 February, 2009)
     * @return A string representation of this chunk.
     */
    public String toStringRegex() {
        if ( words.length == 1 && words[0].toStringPOS().toLowerCase().equals("end/end") ) {
            return "end/end"; //==> 2014/01/17 00:24 JPC
        }
        StringBuilder sb= new StringBuilder("<");
        for (int i=0; i<words.length; i++) {
            if ( i > 0 )  sb.append(' ');
            sb.append(words[i].toStringPOS());
        }
        String cpos= this.POS();
        if ( cpos == null || cpos.equals("UNDEFINED") )  {
            if ( words.length == 1 && words[0].isRPUNCT() )
                cpos= "punct";
            else
                cpos="undefined";
        }
        sb.append(">:");  sb.append(cpos);
        sb.insert(0, cpos+":");
        return sb.toString();
    }
    
    /**
     * The main method exemplifies the role of a chunk, in the context
     * of a chunked sentence (obtained from shallow parsing), as well
     * as the {@link #connection(hultig.sumo.Chunk) connection strength}
     * method, for chunk comparison.
     * @param args No argument is expected.
     */
    public static void main(String[] args) {
        OpenNLPKit model= new OpenNLPKit("/a/tools/opennlp-tools-1.5.0/models/english/");
        model.loadChunker();
        String[] sset = {
            "The brown fox jumped over the fence.",
            "Yesterday, the fox jumped over it."
        };

        ChunkedSentence csa= new ChunkedSentence(sset[0], model);
        ChunkedSentence csb= new ChunkedSentence(sset[1], model);
        System.out.println("\n");
        System.out.printf("CHUNKED SENTENCE A\n   %s\n", csa.toStringChunk());
        System.out.printf("   %s\n\n", csa.toStringRegex());
        System.out.printf("CHUNKED SENTENCE B\n   %s\n", csb.toStringChunk());
        System.out.printf("   %s\n\n", csb.toStringRegex());
        
        /*
        Chunk[] vcsa= csa.getChunks();
        Chunk[] vcsb= csb.getChunks();
        for (int j=0; j<vcsb.length; j++)
            for (int i=0; i<vcsa.length; i++)
                if ( vcsb[j].eqaulPOS(vcsa[i]) ) {
                    double dx= vcsb[j].connection(vcsa[i]);
                    System.out.printf("CONNECTION STRENGTH:... %.5f\n   CSA(%2d) = %s\n   CSB(%2d) = %s\n\n",
                                       dx, j, vcsb[j], i, vcsa[i]);
                }
        */
    }
}