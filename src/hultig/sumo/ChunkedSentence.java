/**
 * ***********************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2011 UBI/HULTIG All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General Public License Version 3 only ("GPL"). You may not use this file
 * except in compliance with the License. You can obtain a copy of the License at http://www.gnu.org/licenses/gpl.txt. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each file. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying information:
 *
 * "Portions Copyrighted [year] [name of copyright owner]" ************************************************************************
 */
package hultig.sumo;

import java.util.Arrays;
import java.util.ArrayList;

import hultig.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * A specialization of the {@code Sentence} class, for handling shallow parsed sentences in a more efficient way. It uses chunk marks
 * ({@code ChunkMark}) to represent the sequence of chunk boundaries.
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
public class ChunkedSentence extends Sentence {

    private String canonicalString =    null;
    private int[]  canonicalPositions = null;

    /**
     * Define the main chunk types considered.
     */
    private static final int[] MAIN_CHUNK_TYPES = new int[]{
        ChunkType.NP, ChunkType.VP, ChunkType.PP,
        ChunkType.PRT, ChunkType.ADVP
    };

    /**
     * To store a numerical value for each chunk type. It was conceived to compute sentence proximity based on the proximity of their chunks. The idea
     * is to differently weight different chunk types, for example giving more value to {@code NP} and {@code VP} chunks.
     */
    private double[] CHUNK_VALUE;

    /**
     * The array of chunk marks defining the sentence chunk boundaries and types.
     */
    private ChunkMark[] vcmark;

    /**
     * The default constructor, which invokes several default settings, including the {@link #define_chunk_values() definition of chunk values}.
     */
    public ChunkedSentence() {
        super();
        vcmark = null;
        define_chunk_values();
    }

    /**
     * This constructor receives a string and a language model, and creates a chunked sentence. The shallow parser is invoked from the language model
     * object ({@code model}).
     *
     * @param s A string representing a textual sentence.
     * @param model The language model which should had already be adequately loaded/configured.
     */
    public ChunkedSentence(String s, OpenNLPKit model) {
        super(s);
        define_chunk_values();
        chunk(model);
    }

    /**
     * This constructor receives a sentence and a language model, and creates an instance of a chunked sentence. The shallow parser is invoked from
     * the language model object ({@code model}).
     *
     * @param s The sentence for shallow parsing.
     * @param model The language model.
     */
    public ChunkedSentence(Sentence s, OpenNLPKit model) {
        super(s.toString());
        define_chunk_values();
        chunk(model);
    }

    /**
     * The value of each chunk type is defined here.
     */
    private void define_chunk_values() {
        /*
         CHUNK_VALUE= new double[MAIN_CHUNK_TYPES.length];
         CHUNK_VALUE[ChunkType.NP]=   3.0;
         CHUNK_VALUE[ChunkType.VP]=   3.0;
         CHUNK_VALUE[ChunkType.PP]=   3.0;
         CHUNK_VALUE[ChunkType.PRT]=  3.0;
         CHUNK_VALUE[ChunkType.ADVP]= 3.0;
         */
        CHUNK_VALUE = new double[ChunkTag.values().length];
        CHUNK_VALUE[ChunkTag.NP.ordinal()] = 3.0;
        CHUNK_VALUE[ChunkTag.VP.ordinal()] = 3.0;
        CHUNK_VALUE[ChunkTag.PP.ordinal()] = 3.0;
        CHUNK_VALUE[ChunkTag.PRT.ordinal()] = 3.0;
        CHUNK_VALUE[ChunkTag.ADVP.ordinal()] = 3.0;
        CHUNK_VALUE[ChunkTag.ADJP.ordinal()] = 3.0;
    }

    /**
     * The method that shallow parses this sentence, based on a given language model.
     *
     * @param model The language model.
     */
    private void chunk(OpenNLPKit model) {
        if (this.size() == 0) {
            return;
        }

        if (!super.get(0).hasPOS()) {
            Sentence s = (Sentence) model.postag(this).clone();
            //System.out.println("------>" + s.toStringPOS());
            if (s == null) {
                System.err.println("ERROR: can not make the POS tagging, for this sentence!\n");
                return;
            }
            super.clear();
            super.reload(s);
        }

        ArrayList<String> vs = new ArrayList<String>();
        process_chunk_string(model.chunk(toStringPOS()), vs);
        String[] chunks = vs.toArray(new String[vs.size()]);
        /*
         for (String s : chunks) {
         System.out.println("chunk: "+s);
         }*/

        super.clear();
        ArrayList<ChunkMark> vchunks = new ArrayList<ChunkMark>();
        for (int i = 0, num_words = 0; i < chunks.length; i++) {
            if (chunks[i].length() == 0) {
                continue;
            }

            String chunk_tag = null;
            String chki = chunks[i];
            if (chki.charAt(0) == '[') {
                int k = chki.indexOf(' ');
                chunk_tag = chki.substring(1, k);
                chki = chki.substring(k, chki.length() - 1).trim();
            }
            String[] arraySChunks = chki.trim().split(" ");

            //process i chunk
            int a = num_words; //---> beginning of the new chunk.
            for (String schunk : arraySChunks) {
                if (schunk.length() == 0) {
                    continue;
                }
                int r = schunk.lastIndexOf('/');
                Word w = new Word();
                if (r < 0) {
                    System.err.printf("\tERROR: in method ChunkedSentence.chunk(OpenNLPKit)\n");
                    System.err.printf("\t   SENTENCE:... [%s]\n", this.toString());
                    System.err.printf("\t   WORD:....... [%s]\n", schunk);
                    w.set(schunk);
                    w.setPOS(new char[]{'U', 'N', 'D', 'E', 'F'});
                    w.setChkCod(-1);
                    w.CHTAG = null;
                } else {
                    String spos = schunk.substring(r + 1);
                    w.set(schunk.substring(0, r));
                    w.setPOS(spos.toCharArray()); //---> para alterar no futuro.
                    w.setChkCod(ChunkType.str2cod(chunk_tag));
                    w.CHTAG = ChunkTag.parse(chunk_tag);
                    //System.out.printf("%s   %s\n", chunk_tag, w.CHTAG);
                }
                super.add(w);
                num_words++;
            }

            ChunkMark chunk_i = new ChunkMark(a, num_words - 1, chunk_tag);
            chunk_i.chtag = ChunkTag.parse(chunk_tag);
            vchunks.add(chunk_i);
        }
        vcmark = vchunks.toArray(new ChunkMark[vchunks.size()]);
    }

    /**
     * This is an auxiliary method thought to process a string, representing a shallow parsed sentence, obtaining their sequence of chunks.
     *
     * @param schunk The string with the chunked sentence.
     * @param vChunks The output parameter in which the sequence of chunks is recursively added.
     */
    private void process_chunk_string(String schunk, ArrayList<String> vChunks) {
        if (schunk == null) {
            return;
        }
        //System.out.println("schunk ---> "+schunk);
        schunk = schunk.trim();
        int n = schunk.length();
        if (n == 0) {
            return;
        }
        if (n == 1 && schunk.charAt(0) == '[') {
            return;
        }

        int a = schunk.indexOf('[');
        if (a == 0) {
            int b = schunk.indexOf(']', a);
            while (b + 1 < n && schunk.charAt(b + 1) == '/') {
                b = schunk.indexOf(']', b + 1);
            }

            if (b > 0) { //---> 2009/03/27, caso de parêntises
                // recto, no final da frase: "The president shout! ["
                vChunks.add(schunk.substring(a, b + 1));
                //==> RECURSION <== //
                process_chunk_string(schunk.substring(b + 1), vChunks);
                //==> --------- <== //
            }

            return;
        }

        String prefixo = null;
        String resto = null;
        if (a > 0) {
            prefixo = schunk.substring(0, a);
            resto = schunk.substring(a);
        } else {
            prefixo = schunk;
        }

        String[] vPrefixos = prefixo.split(" ");
        for (String p : vPrefixos) {
            if (p.indexOf('/') < 0) {
                continue;
            }
            vChunks.add(p);
        }
        //==> RECURSION <== //
        process_chunk_string(resto, vChunks);
        //==> --------- <== //
    }

    /**
     * Gives the number of effective words contained in this sentence.
     *
     * @return The number of words.
     */
    public int getNumWords() {
        int k = 0;
        for (int i = 0; i < size(); i++) {
            Word wi = this.get(i);
            if (wi.isWord()) {
                k++;
            }
        }
        return k;
    }

    /**
     * Gives the number of chunks contained in this sentence.
     *
     * @return The number of chunks.
     */
    public int getNumChunks() {
        return vcmark.length;
    }

    /**
     * Counts the number of chunks of a certain kind (tag).
     *
     * @param postag The chunk tag to be counted, for example "NP", "VP".
     * @return The number of chunks matching {@code postag}.
     */
    public int getNumChunks(String postag) {
        int num = 0;
        for (ChunkMark cm : vcmark) {
            if (cm.POS().equals(postag)) {
                num++;
            }
        }
        return num;
    }

    /**
     * Gets a string with the {@code k}-th chunk, from this sentence chunk sequence.
     *
     * @param index The chunk index.
     * @return String The string of the {@code k}-th chunk in the "usual" format, as for example: {@code [NP the/DT Pet/NNP passport/NN ]}. On error,
     * {@code null} will be returned.
     */
    public Chunk getChunk(int index) {
        ChunkMark cm = this.getChunkMark(index);
        //System.out.printf("C(%d) = %s\n", index, cm);
        if (cm == null) {
            return null;
        }

        Chunk c = new Chunk(this, cm);
        return c;
    }

    /**
     * Gets an array of strings containing the complete sequence of chunks, from this sentence, one chunk per array position.
     *
     * @return String[] The sequence of chunks or {@code null} on error.
     */
    public Chunk[] getChunks() {
        if (vcmark == null) {
            return null;
        }

        int n = getNumChunks();
        Chunk[] vs = new Chunk[n];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = getChunk(i);
        }

        return vs;
    }

    /**
     * Gives the chunk mark (boundaries and tag), for the chunk at position {@code index}, in the sequence of sentence chunks.
     *
     * @param index The chunk index.
     * @return ChunkMark The chunk mark.
     */
    public ChunkMark getChunkMark(int index) {
        try {
            return vcmark[index];
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Gives the chunk mark relative to the word at position {@code index}, in this sentence.
     *
     * @param index A valid index of a sentence word. It must be greater than zero and less than the number of words in the sentence.
     * @return Whether the corresponding chunk mark or {@code null}, on erroneous cases.
     */
    public ChunkMark getChunkOnPosition(int index) {
        for (int i = 0; i < vcmark.length; i++) {
            if (vcmark[i].a() <= index && index <= vcmark[i].b()) {
                return vcmark[i];
            }
        }
        return null;
    }

    /**
     * Gives the chunk mark for a word at position {@code index}, identifying first to which chunk does the word belong.
     *
     * @param index The word sequential index, in the sentence.
     * @return The chunk tag (e.g. {@code NP}, {@code VP}), or {@code null} on index out of bounds.
     */
    public String getWordChunkMark(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }

        int chk = get(index).getChkCod();
        if (chk >= 0) {
            String chks = ChunkType.cod2str(chk);
            return chks;
        }
        /* */
        for (int i = 0; i < vcmark.length; i++) {
            if (vcmark[i].a <= index && index <= vcmark[i].b) {
                return vcmark[i].POS();
            }
        }

        return null;
    }

    /**
     * Gives the array of part-of-speech tags, corresponding to the sequence of words in the sentence.
     *
     * @return The array of part-of-speech tags.
     */
    public String[] getVPOSig() {
        if (size() < 1) {
            return null;
        }
        String[] vpos = new String[size()];

        for (int i = 0; i < vpos.length; i++) {
            Word w = this.get(i);
            vpos[i] = w.getPOS();
        }

        return vpos;
    }

    /**
     * Gives a string with the sequence of part-of-speech tags, corresponding to to the sequence of words in the sentence.
     *
     * @param chconnect The connection character, between two tags, usually a blank space.
     * @return The string with part-of-speech sequence, or {@code null} on error. For example: {@code "NP VP PP NP VP"}.
     */
    public String getSPOSig(char chconnect) {
        String[] vpos = getVPOSig();
        if (vpos == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vpos.length; i++) {
            sb.append(vpos[i]);
            if (i < vpos.length - 1) {
                sb.append(chconnect);
            }
        }

        return sb.toString();
    }

    /**
     * The same as {@link #getSPOSig(char) getSPOSig(char chconnect)} with the connection character being equal to the default of a blank space.
     *
     * @return
     */
    public String getSPOSig() {
        return getSPOSig(' ');
    }
    
    
    public int[] getCanonicalPositions() {
        return canonicalPositions;
    }
    
    
    public int getIndex4CanonicalPosition(int position) {
        if ( position < 0 ) return -1;
        if ( canonicalPositions == null ) toString();
        for (int index=1; index<canonicalPositions.length; index++) {
            int pi = canonicalPositions[index];
            if ( pi == position ) return index;
            if ( pi >  position ) return index-1; 
        }
        return -1;
    }

    public int[] getIndexPair4MatchRegion(String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(toString());
        if (!m.find())  return null;

        int a = m.start();   a= getIndex4CanonicalPosition(a);
        int b = m.end();     b= getIndex4CanonicalPosition(b);

        return new int[]{a, b};
    }

    /**
     * Method Override.
     *
     * @return The canonical string representation for this sentence.
     */
    public String toString() {
        if (canonicalString == null) {
            int pos=0;
            canonicalPositions= new int[size()];
            StringBuilder s = new StringBuilder();
            String sp = ".,;:?!\"'";
            for (int i = 0; i < size(); i++) {
                Word w = this.get(i);
                if (w == null) {
                    continue;
                }
                String ws = w.toString();
                if (sp.contains(ws) || i == 0) {
                    s.append(ws);                 
                } else {
                    s.append(' ').append(ws);
                    pos++;
                }
                canonicalPositions[i]= pos;
                pos+= ws.length();
            }
            canonicalString = s.toString();
        }
        return canonicalString;
    }

    /**
     * Gives a string with only the part-of-speech tags.
     *
     * @return The POS string.
     */
    public String toPOString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            sb.append(get(i).getPOS());
            if (i < size() - 1) {
                sb.append(' ');
            }
        }

        return sb.toString().toLowerCase();
    }

    /**
     * Gives a shallow parsed representation of this chunked sentence. The representation follows a conventional format: {@code CHK1 CHK2 ... CHn},
     * where {@code CHKi} represents the {@code i}-th sentence chunk, with the following structure: {@code CHKi = [CT W1/T1, W2/T2, ..., Wn/Tn]},
     * where {@code CT} represents the chunk tag, and {@code Wj} and {@code Tj} the {@code j}-th chunk word and POS tag. For example:
     * <pre>
     *    [NP The/DT lazy/JJ fox/NN] [VP jumped/VBD] [PP over/IN] [NP the/DT fence/NN]</pre>
     *
     * @return The string representing the shallow parsed sentence.
     */
    public String toStringChunk() {
        Chunk[] vc = getChunks();
        /*
         for (Chunk c : vc) {
         System.out.println(c);
         }
         */
        if (vc == null || vc.length == 0) {
            return super.toStringPOS();
        }

        StringBuilder sb = new StringBuilder(vc[0].toString());
        for (int i = 1; i < vc.length; i++) {
            sb.append(' ').append(vc[i].toString());
        }

        return sb.toString();
    }

    /**
     * Gives another format of a shallow parsed representation of this sentence, in a format suitable for regular expression matching. The idea was to
     * be able to apply sentence simplification rules expressed expressed through regular expressions (13, February 2009, 11:57). This format is
     * exemplified in the following example:
     * <pre>
     *    np:&lt;the/dt lazy/jj fox/nn>:np  vp:&lt;jumped/vbd>:vp  pp:&lt;over/in>:pp  np:&lt;the/dt fence/nn>:np</pre>
     *
     * @return The string representing the shallow parsed sentence.
     */
    public String toStringRegex() {
        Chunk[] chunks = getChunks();
        if (chunks == null || chunks.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(chunks[0].toStringRegex());
        for (int i = 1; i < chunks.length; i++) {
            sb.append(" ").append(chunks[i].toStringRegex());
        }

        return sb.toString().toLowerCase();
    }

    /**
     * A {@code toString()} method type that gives a string representation of this chunked sentence, where each word is printed followed by its
     * part-of-speech tag, as shown in the next example:
     * <pre>
     *    the/dt lazy/jj fox/nn jumped/vbd over/in the/dt fence/nn</pre> (26, April 2009, 10:47)
     *
     * @return A string representation of this chunked sentence.
     */
    public String toStringRegexPOS() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            sb.append(get(i).toStringPOS());
            if (i < size() - 1) {
                sb.append(' ');
            }
        }

        return sb.toString().toLowerCase();
    }

    /**
     * This method is similar to {@link toStringRegexPOS() toStringRegexPOS()}, differing only in the fact that the chunk tag is also included in each
     * word printing, after the part-of-speech tag. For example:
     * <pre>
     *    the/dt/np lazy/jj/np fox/nn/np jumped/vbd/vp over/in/pp the/dt/np fence/nn/np</pre> (27, April 2009, 20:10)
     *
     * @return A string representation of this chunked sentence.
     */
    public String toStringRegexPOSCHK() {
        Chunk[] chunks = getChunks();
        if (chunks == null || chunks.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Chunk chunk : chunks) {
            for (int j = 0; j < chunk.size(); j++) {
                Word w = chunk.get(j);
                sb.append(w.toStringPOS()).append('/').append(chunk.POS()).append(' ');
            }
        }

        return sb.toString().trim().toLowerCase();
    }

    /**
     * This function was designed to compute a likelihood value for the "lexico-syntactic entailment" between this sentence (thesis) and the entailed
     * sentence - the other sentence (hypothesis). We say that sentence T entails sentence H if we can infer/conclude H by knowing T. This metric was
     * created to work with data from the
     * <a href="http://pascallin.ecs.soton.ac.uk/Challenges/RTE3/">RTE collections</a>. The calculations are based on lexical and syntactical (shallow
     * parsed sentence) features.
     *
     * @param hypot The sentence that represents the hypothesis.
     * @return A real value in the {@code [0,1]} interval.
     */
    public double lexicoSyntacticEntailmentMetric(ChunkedSentence hypot) {
        Chunk[] t = this.getChunks();
        Chunk[] h = hypot.getChunks();
        if (t == null || h == null) {
            return 0.0;
        }
        //if ( t.length < t.hashCode() ) {Chunk[] aux= t; t=h; h=aux; }

        ArrayList<Chunk> vt = new ArrayList<Chunk>(Arrays.asList(t));
        ArrayList<Chunk> vh = new ArrayList<Chunk>(Arrays.asList(h));

        //Local class for function definition.
        //--------------------------------------------------
        class AssignChunk implements AssignFunction<Chunk> {

            public double value(Chunk ca, Chunk cb) {
                if (ca.posUndefined() || cb.posUndefined()) {
                    return 0.0;
                } else {
                    return ca.connection(cb);
                }
            }
        }
        //--------------------------------------------------
        ArrayList<MatrixItem> bestAssign = Matrix.bestAssignment(vt, vh, new AssignChunk());

        double sum = 0.0;
        for (MatrixItem a : bestAssign) {
            Chunk ci = t[a.i()];
            Chunk cj = h[a.j()];
            double strength = a.aij();

            if (!ci.POS().equals(cj.POS())) {
                continue;
            }
            if (ci.POS().equals("NP")) {
                sum += CHUNK_VALUE[ChunkType.NP] * strength;
            } else if (ci.POS().equals("VP")) {
                sum += CHUNK_VALUE[ChunkType.VP] * strength;
            }
        }

        int nNP = hypot.getNumChunks("NP");
        int nVP = hypot.getNumChunks("VP");
        if (nNP == 0 || nVP == 0) {
            return 0.0;
        }

        return sum / (nNP * CHUNK_VALUE[ChunkType.NP] + nVP * CHUNK_VALUE[ChunkType.VP]);
    }

    /**
     * This method is a default shortcut for {@link #printArrayWords(java.lang.String)
     * printArrayWords(java.lang.String)}, with {@code label = null}.
     */
    public void printArrayWords() {
        printArrayWords(null);
    }

    /**
     * Outputs the sequence of words in this shallow parsed sentence with their corresponding lexico-syntactic codes.
     *
     * @param label A string to be printed before the whole sequence.
     */
    public void printArrayWords(String label) {
        System.out.print("ARRAY OF WORDS:\n");
        if (label != null) {
            System.out.print(label);
        }
        for (int k = 0; k < size(); k++) {
            Word wk = this.get(k);
            System.out.printf("(%s, %3d, %2d, %2d)   ", wk, wk.getLexCod(), wk.getPosCod(), wk.getChkCod());
        }
        System.out.print("\n");
    }

    /**
     * Gives a subsequence of this sentence, in the form of a list of words.
     *
     * @param fromIndex The inclusive starting index.
     * @param toIndex The inclusive ending index.
     * @return A sublist representing a subsequence of words, from this sequence.
     */
    @Override
    public ChunkedSentence subList(int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            return null;
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }
        int n = size();
        if (toIndex > n) {
            toIndex = n;
        }

        ChunkedSentence cstc = new ChunkedSentence();
        for (int i = fromIndex; i < toIndex; i++) {
            cstc.add(this.get(i));
        }
        return cstc;
    }

    public String getPOStrFixed() {
        //String sp= ".,;:?!\"'";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            Word w = get(i);
            //String ws= w.toString();
            String pos = Toolkit.padLeft(w.getPOS(), w.length());
            sb.append(pos).append(' ');
            /*
             if ( sp.indexOf(ws) >= 0 || i == 0 )
             sb.append(pos);
             else
             sb.append(' ').append(pos);
             *
             */
        }
        return sb.toString();
    }

    /**
     * Generally exemplifies the operative features of this class. In order to run the tests contained in this method, a language model
     * ({@code OpenNLP} object) must be previously set.
     *
     * @param args The are no arguments expected.
     */
    public static void main(String[] args) {
        OpenNLPKit model = new OpenNLPKit("/a/tools/opennlp-tools-1.5.0/models/english/");
        model.loadChunker();
        System.out.println();

        String[] vs = {
            //"The lazy fox jumped over the fence",
            //"Guatemala accepts the Pet passport and paper, as proof of vaccination?",
            //"A novel of the Long Emergency set in upstate New York in the not distant future.",
           
            "To be published by The Atlantic Monthly Press, he gave a talk on May.",
            "Levomepromazine has prominent sedative and anticholinergic sympatholytic effects.",
           //012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
            "The files are stored in a temporary directory on the VAX disks, where they are converted to VMS Backup format.",
            ""
        };

        for (int i = 0; i < vs.length - 1; i++) {
            ChunkedSentence cs = new ChunkedSentence(vs[i], model);
            System.out.printf("PART-OF-SPEECH:.. %s\n", cs.getPOStrFixed().toLowerCase());
            System.out.printf("SENTENCE %2d:..... %s\n", i, vs[i]);
            System.out.printf("... toStringChunk() ......... %s\n", cs.toStringChunk());
            System.out.printf("... toStringRegex() ......... %s\n", cs.toStringRegex());
            //System.out.printf("... toStringRegexPOS() ...... %s\n", cs.toStringRegexPOS());
            //System.out.printf("... toStringRegexPOSCHK() ... %s\n", cs.toStringRegexPOSCHK());
            //System.out.printf("... getSPOSig() ............. %s\n", cs.getSPOSig());
            System.out.print("\n\n");
        }

        ChunkedSentence cs = new ChunkedSentence(vs[2], model);
        int position= 53;
        int index= cs.getIndex4CanonicalPosition(position);
        System.out.printf("Index(%d) ===> %d\n", position, index);
        int[] v= cs.getCanonicalPositions();
        for (int i = 0; i < v.length; i++) {
            int x= v[i];
            int dx= cs.get(i).length();
            String wx= vs[2].substring(x,x+dx);
            System.out.printf("v(%2d) ---> %3d   dv=%2d    [%s]\n", i, x, dx, wx);
        }
        
        System.out.print("\n\n");
    }
}
