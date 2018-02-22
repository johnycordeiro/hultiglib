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

import hultig.nlp.HashString;
import hultig.util.BestStrings;
import java.util.*;
import java.text.*;
import java.io.*;
import java.io.InputStreamReader;

import spiaotools.SentParDetector;
import hultig.util.CronoSensor;
import hultig.util.StringDouble;
import hultig.util.Toolkit;

/**
 * <p>
 * A class to represent and manage text. It can represent a textual document or
 * even a list of independent sentences, since internaly it is represented
 * through a linked list of sentences.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro (2004)
 * @version 1.0
 */
public final class Text extends LinkedList<Sentence>
{
    /**
     * The corpus index used for this text.
     */
    private CorpusIndex CINDEX;

    /**
     * Dynamically stores the vocabulary of this text.
     */
    private HashMap<String, Integer> VOCAB;

    /**
     * The total number of tokens in this text.
     */
    private int NUMTOKENS;


    /**
     * The default constructor.
     */
    public Text() {
        super();
        CINDEX = null;
        VOCAB= new HashMap<String,Integer>();
        NUMTOKENS= 0;
    }

    /**
     * Creates a new text from a given string. The string may contain
     * more than one sentence. Each sentence will be detected and
     * inserted.
     * @param s The text string.
     */
    public Text(String s) {
        super();
        CINDEX = null;
        VOCAB= new HashMap<String,Integer>();
        NUMTOKENS= 0;
        add(s);
    }

    /**
     * Creates a text from an array of strings. Each string may
     * contain more than one sentence, similarly to
     * {@link #Text(String s) Text(String s)}.
     * @param vs The array of strings.
     */
    public Text(String[] vs) {
        super();
        CINDEX = null;
        VOCAB= new HashMap<>();
        NUMTOKENS= 0;

        if (vs == null) {
            return;
        }
        for (String line : vs) {
            if (line.trim().length() > 0) {
                this.add(line);
            }
        }
    }

    /**
     * Creates a new text from a given string. The string may contain
     * more than one sentence. Each sentence will be detected and
     * inserted. This constructor uses the OpenNLP sentence detector,
     * which is now more accurate, based on the trained model for the
     * English language. See the OpenNLP project.
     * @param s The text string.
     * @param onlpk The OpenNLP Kit.
     */
    public Text(String s, OpenNLPKit onlpk) {
        super();
        CINDEX= null;
        VOCAB= new HashMap<String,Integer>();
        NUMTOKENS= 0;
        add(s,onlpk);
    }

    /**
     * Add all the sentences contained in the readLn string to this file.
     * Sentence boundary detection is made through Scott Piao's package.
     * Defined on 2007/03/09
     * @param s The text string.:
     * @return True if sentences were added to this file.
     */
    public boolean add(String s) {
        if (s == null) {
            return false;
        }

        try {
            //System.out.println("ADDING SENTENCES NOW ... ");
            SentParDetector spd = new SentParDetector();
            spd.markTitle(false);
            String sx = spd.markupRawText(2, s);
            String[] vs = sx.split("\n");
            for (int i = 0; i < vs.length; i++) {
                Sentence si= new Sentence(vs[i]);
                //System.out.println(":... ADD ... "+si.toString());
                super.add(si);
                countVocab(si);
            }
        } catch (Exception x) {
            addOld(s);
        }
        return true;
    }


    private void countVocab(Sentence s) {
        if ( VOCAB == null && s == null )  return;
        for (int i = 0; i < s.size(); i++) {
            String si= s.get(i).toString().toLowerCase();
            NUMTOKENS++;
            Integer Fwi= VOCAB.get(si);
            if ( Fwi == null )
                VOCAB.put(si,1);
            else
                VOCAB.put(si,++Fwi);
        }
    }

    /**
     * Add all the sentences contained in the readLn string to this file.
     * Sentence boundary detection is made through an OpenNLP model,
     * which performs better, based on the trained model for the
     * English language. See the OpenNLP project.
     * @param stxt The text string.
     * @param onlpk The OpenNLP Kit.
     * @return The true value on success.
     */
    public boolean add(String stxt, OpenNLPKit onlpk) {
        if (stxt == null)  return false;
        if (onlpk == null)  return add(stxt);

        String[] vstc = onlpk.splitSentences(stxt);
        if (vstc != null) {
            for (String s : vstc) {
                super.add(new Sentence(s));
            }
            return true;
        } else {
            return add(stxt);
        }
    }

    /**
     * Adds a sentence to this text, by inserting it at the end
     * of the list (appending a sentence).
     * @param s
     * @return
     */
    public boolean add(Sentence s) {
        countVocab(s);
        return super.add(s);
    }

    /**
     * Adds all the sentences contained in another Text object, to this text.
     * @param t The other text object.
     */
    public void add(Text t) {
        Sentence[] vs = t.getSentences();
        for (Sentence v : vs)
            if (v.length() > 0)  this.add(v);
    }

    /**
     * Adds all the sentences contained in the readLn string to this
     * text. Sentence boundary detection is made through the
     * BreakIterator class.
     * @param s The text string
     */
    private void addOld(String s) {
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.US);
        boundary.setText(s);
        int start = boundary.first();
        for (int end = boundary.next();
                end != BreakIterator.DONE;
                start = end, end = boundary.next()) {
            Vector<String> vs = splitIntoSentences(s.substring(start, end).trim());
            for (int i = 0; i < vs.size(); i++) {
                Sentence sentence = new Sentence(vs.get(i));
                super.add(sentence);
                countVocab(sentence);
            }

        }
    }

    /**
     * Old method used for splitting a text string into a list of sentence
     * strings. It was designed to complement the work of the BreakIterator,
     * by detecting sentence boundaries.
     * @param s The text string.
     * @return A list of sentence strings or null.
     */
    private Vector<String> splitIntoSentences(String s) {
        if (s == null) {
            return null;
        }

        Vector<String> v = new Vector<String>();
        char[] cset = {'.', '!', '?'};
        int[] vk = new int[cset.length];
        int a = 0;
        for (;;) {
            for (int i = 0; i < vk.length; i++) {
                vk[i] = s.indexOf(cset[i], a);
            }
            int k = Toolkit.minGreaterThan(-1, vk);
            if (k == -1 || k == s.length() - 1) {
                break;
            }
            if (k == 0) {
                v.add(s.substring(0, 1)); //add
                a = 1;
                continue;
            }
            boolean cond = Character.isLowerCase(s.charAt(k - 1)) || s.charAt(k - 1) == ' ';
            cond = cond && (Character.isUpperCase(s.charAt(k + 1)) || s.charAt(k + 1) == ' ');
            if (cond) {
                String stc = s.substring(a, k + 1).trim();
                if (stc.length() > 0) {
                    v.add(stc);  //add
                }
                s = s.substring(k + 1);
                a = 0;
            } else {
                a = k + 1;
            }
        }

        if (s.length() > 0) {
            v.add(s);
        }
        return v;
    }

    /**
     * Eliminate all sentences having less words than a given
     * minimum number.
     * @param numwords The minimum number of words.
     */
    public void cutIfLessThan(int numwords) {
        for (int i = 0; i < this.size(); i++) {
            Sentence stc = this.getSentence(i);
            if (stc.size() < numwords) {
                this.remove(i);
            }
        }
    }
    
    
    
    /**
     * Capitalizes the first letter of each sentences.
     * 2016-10-31 13:13 JPC
     */
    public void capitalize() {
        for (Sentence s : this) {
            char[] cw= s.get(0).toString().toCharArray();
            cw[0]= Character.toUpperCase(cw[0]);
            Word w= s.get(0);
            w.set(new String(cw));
            s.set(0,w);
        }
    }
    
    public String readFile(String filename) {
        return readFile(filename, "UTF-8", 0, 0);
    }
    
    public String readFile(String filename, String encoding) {
        return readFile(filename, encoding, 0, 0);
    }
    
    public String readFile(String filename, int offset, int sizeInChars) {
        return readFile(filename, "UTF-8", offset, sizeInChars);
    }

    /**
     * Add all the sentences contained in a given text file to this
     * text object. The new sentences will be sequentially added
     * after the possibly already existing ones.
     * @param filename The file from which to read the sentences.
     * @param encoding The default encoding is UTF-8.
     * @param offset
     * @param sizeInChars
     * @return 
     */
    public String readFile(String filename, String encoding, int offset, int sizeInChars) {
        long numCharsRead= 0L;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
            StringBuilder sb= new StringBuilder();
            char[] buffer= new char[1024];
            int n=0;
            while ( (n=br.read(buffer)) >= 0 ) {
                numCharsRead+= n;
                if ( offset>0 && numCharsRead<offset ) continue;
                sb.append(new String(buffer, 0, n));
                if ( sizeInChars>0 && numCharsRead>offset+sizeInChars ) break;
                /*
                if ( ++i % 10 == 0 ) System.out.print('.');
                if ( i%100 == 0 )  System.out.println();
                */
            }
            //System.out.println();
            br.close();
            String t= sb.toString(); 
            Text txtInFile= new Text(t);
            add(txtInFile);
            return t;
        } catch (Exception e) {
            System.err.printf("%s / %s\n", getClass().getCanonicalName(), "readFile(String filename)");
            System.err.println("ERROR: "+e);
            return null;
        }
    }
    
    
    /**
     * Save this text to a new file with the name equal to the
     * current time stamp in the format: YYYYMTDDHHMMSS.txt,
     * with YYYY, MT, DD, HH, MM, SS representing respectively
     * the year, month, day, hour, minute, and second. For
     * example: "20100320171545.txt"
     * @return The true value on success.
     */
    public boolean saveFile() {
        String filename = CronoSensor.nowStamp() + ".txt";
        return saveFile(filename);
    }

    /**
     * Saves the current text to a given file.
     * @param filename The name of the saved file.
     * @return The true value on success.
     */
    public boolean saveFile(String filename) {
        try {
            PrintWriter pwr = new PrintWriter(new FileOutputStream(filename));
            for (int k = 0; k < this.size(); k++) {
                String line = this.getSentence(k).toString();
                pwr.println(line);
            }
            pwr.close();
        } catch (Exception e) {
            System.out.println("EXCEPTION - ON SAVE: " + e.toString());
            return false;
        }
        return true;
    }

    /**
     * Turns every word from this file to lower case.
     */
    public void toLowerCase() {
        for (int i = 0; i < this.size(); i++) {
            this.get(i).toLowerCase();
        }
    }

    /**
     * Codifies every word from this text uppon a given corpus index
     * (CorpusIndex). The corpus index reference will be stored in
     * {@link #CINDEX CINDEX}.
     * @param idx The corpus index.
     */
    public void codify(CorpusIndex idx) {
        CINDEX = idx;

        for (int k = 0; k < size(); k++) {
            Sentence s = get(k);
            s.codify(idx);
            set(k, s);
        }
    }

    /**
     * Computes a lexical similarity between two texts, based on local
     * evidence. This function uses an adaptation of the TF*IDF vector
     * representation, without computing the heavy IDF component.
     * Based on the Zipf law, the word length and the relative frequency
     * are considered for the vectorial calculations.
     * @param othr The other sentence.
     * @return The similarity value in the <code>[0, 1]</code> interval.
     */
    public double similarity(Text othr) {
        double dotProduct= 0.0;
        double normA= 0.0;

        Iterator<String> words= VOCAB.keySet().iterator();
        while ( words.hasNext() ) {
            String sw= words.next();
            double pwA= this.prob(sw);
            double ai= sw.length()/pwA;
            normA+= ai*ai;

            double pwB= othr.prob(sw);
            if ( pwB != 0.0 ) {
                double bi= sw.length()/pwB;
                dotProduct+= ai*bi;
            }
        }
        normA= Math.sqrt(normA);

        double normB= 0.0;
        String[] vocabB= othr.getVocab();
        if ( vocabB != null ) {
            for (int i = 0; i < vocabB.length; i++) {
                String sw = vocabB[i];
                double pwB= othr.prob(sw);
                double bi= sw.length()/pwB;
                normB+= bi*bi;
            }
        }
        else
            normB= 1.0;
        normB= Math.sqrt(normB);

        //System.out.printf("|A| = %f   |B| = %f    A.B = %f\n", normA, normB, dotProduct);
        return dotProduct / (normA*normB);
    }


    /**
     * Codifies this text according to the corpus index
     * referenced by {@link #CINDEX CINDEX}.
     */
    public void codify() {
        //creates a dictionary
        Vector<String> vtokens = new Vector<String>();
        for (int i = 0; i < this.size(); i++) {
            Sentence s = this.getSentence(i);
            for (int j = 0; j < s.size(); j++) {
                vtokens.add(s.getWord(j));
            }
        }
        CorpusIndex dict = new CorpusIndex();
        dict.readCorpus(vtokens);

        //codify tokens
        this.codify(dict);
    }

    /**
     * Gives the reference of the corpus index stored in this
     * object, an possibly used to codify the text.
     * @return The corpus index reference.
     */
    public CorpusIndex getCorpusIndex() {
        return CINDEX;
    }

    /**
     * Tries to return the string of the j-th word from the i-th
     * sentence of this text.
     * @param i The sentence index position.
     * @param j The word index position in a given sentence.
     * @return The string of the word or null if it do not
     * exist, for the indicated i and j indexes.
     */
    public String getWord(int i, int j) {
        Sentence stc = this.getSentence(i);
        if (stc == null) {
            return null;
        }

        return stc.getWord(j);
    }
    
    /**
     * Gives an array list containing only the words
     * from this text.
     * @return The array list with the text words.
     */
    public ArrayList<Word> getOnlyWords() {
        ArrayList<Word> words= new ArrayList<Word>();
        for (Sentence sentence : this) {
            for (Word w : sentence) {
                if ( w.isWord() ) {
                    words.add(w);
                }
            }
        }        
        return words;
    }
    
    
    public int getNumWords() {
        int counter= 0;
        for (Sentence sentence : this)
            for (Word w : sentence)
                if ( w.isWord() ) counter++;
        return counter;
    }
    
    
    public int getNumWordsSmallThan(int size) {
        int counter= 0;
        for (Sentence sentence : this)
            for (Word w : sentence)
                if ( w.isWord() && w.length()<size ) counter++;
        return counter;
    }
    
    
    public StringDouble[] getNMostRelevantWords(int n, HashString corpus) {
        BestStrings bs= new BestStrings(n);
        for (Sentence sentence : this) {
            for (Word w : sentence) {
                if ( w.isWord() ) {
                    String sw= w.toString().toLowerCase();
                    double pwt= prob(sw);
                    double pw = corpus.getProbability(sw);
                    double rw= /*sw.length() */ Math.log(pwt/(pw+0.00000001))/Math.log(2);
                    bs.insert(new StringDouble(sw,rw));
                    //System.out.print(" "+sw+"/"+rw);
                }
            }
            //System.out.println();
        }
        return bs.getStrings();
    }
    
    
    public StringDouble[] getInformativeWords(double minInformation, HashString corpus) {
        ArrayList<StringDouble> array= new ArrayList<StringDouble>();
        for (Sentence sentence : this) {
            for (Word w : sentence) {
                if ( w.isWord() ) {
                    String sw= w.toString().toLowerCase();
                    double pwt= prob(sw);
                    double pw = corpus.getProbability(sw);
                    double wordValue= /*sw.length() */ Math.log(pwt/(pw+0.00000001))/Math.log(2);
                    if ( wordValue >  minInformation )  array.add(new StringDouble(sw,wordValue));
                    //System.out.print(" "+sw+"/"+rw);
                }
            }
            //System.out.println();
        }
        BestStrings bs= new BestStrings(array.size());
        for (StringDouble wx : array) {
            bs.insert(wx);
        }
        return bs.getStrings();
    }
    

    /**
     * Gives the i-th sentence from this text.
     * @param index The sentence index in the text.
     * @return The i-th sentence or null if not found
     * for the given index.
     */
    public Sentence getSentence(int index) {
        if (index < 0 || index >= this.size()) {
            return null;
        }
        return (Sentence) this.get(index);
    }

    /**
     * Gives an array with all the sentences from this text.
     * @return The array of sentences.
     */
    public Sentence[] getSentences() {
        Sentence[] vs = new Sentence[super.size()];
        for (int i = 0; i < vs.length; i++) {
            vs[i] = this.getSentence(i);
        }
        return vs;
    }


    public int getNumTokens() {
        return NUMTOKENS;
    }

    public String[] getVocab() {
        Set<String> ss= VOCAB.keySet();
        String[] vocab= new String[ss.size()];
        vocab= VOCAB.keySet().toArray(vocab);
        return vocab;
    }

    /**
     * Remove duplicate sentences from this text.
     */
    public void removeDuplicates() {
        for (int i = 0; i < this.size() - 1; i++) {
            String si = this.get(i).toString();
            for (int j = i + 1; j < this.size(); j++) {
                String sj = this.get(j).toString();
                if (si.equals(sj)) {
                    this.remove(j);
                }
            }
        }
    }

    /**
     * Eliminates randomly n sentences from this text.
     * @param n The number of sentences to be eliminated.
     */
    public void randomDrop(int n) {
        if (n < 1) {
            return;
        }
        if (n >= size()) {
            clear();
            return;
        }

        Random r = new Random();
        for (int k = 0; k < n; k++) {
            remove(r.nextInt(n - k));
        }
    }

    /**
     * Shuffles randomly the sentences in this text.
     * @return boolean
     */
    public boolean shuffle(Random r) {
        int N = super.size();
        if (N < 1) {
            return false;
        }

        for (int i = 0; i < N - 1; i++) {
            int j = i + r.nextInt(N - i);
            //swap positions
            if (i != j) {
                Sentence Si = this.getSentence(i);
                this.set(i, this.getSentence(j));
                this.set(j, Si);
            }
        }
        return true;
    }

    /**
     * Outputs the text sentences, one sentence per line.
     */
    public void print() {
        print(null, null, false);
    }
    
    /**
     * Prints the current text in a justified format, up to 
     * a given number of columns.
     * @param columns The number of character columns.
     */
    public void printJustified(int columns) {
        String stxt= this.toString(" ");
        System.out.print(Toolkit.justifyText(stxt, columns));
    }

    /**
     * Outputs the text sentences, one sentence per line. Each sentence
     * will be surrounded by a left and a right sequence and may be
     * marked with its sequential index.
     * right string.
     * @param sleft The left string context.
     * @param sright The right string context.
     * @param withIndex Print the sequential sentence index.
     */
    public void print(String sleft, String sright, boolean withIndex) {
        for (int k = 0; k < size(); k++) {
            Sentence s = getSentence(k);
            if (sleft != null) {
                System.out.print(sleft);
            }
            if (withIndex) {
                System.out.printf("%3d ---> ", k + 1);
            }
            System.out.printf("%s", s);
            if (sright != null) {
                System.out.print(sright);
            }
            System.out.println();
        }
    }
    
    public int freq(String sw) {
        Integer Fw= VOCAB.get(sw);
        return Fw == null ? 0 : Fw.intValue(); 
    }
    
    public double prob(String sw) {
        return 1.0 * freq(sw) / NUMTOKENS;
    }

    public void printVocabulary() {
        Iterator<String> words= VOCAB.keySet().iterator();
        while ( words.hasNext() ) {
            String sw= words.next();
            int fw= freq(sw);
            double pw= prob(sw);
            System.out.printf("F = %2d      P = %.7f    |W|/P = %6.2f     W = %s\n", fw, pw, sw.length()/pw, sw);
        }
        System.out.println("--------------------------------------------------------");
        System.out.printf( "TOTAL NUMBER OF TOKENS: %d\n", getNumTokens());
    }

    /**
     * Gives a concatenation of the sentences from this text.
     * @return A string representing this text.
     */
    @Override
    public String toString() {
        return this.toString("");
    }

    /**
     * Gives a concatenation of the sentences from this text. Between
     * each sentence a given separator is inserted.
     * @param separator The separator connecting two sentences
     * @return A string representation of this text.
     */
    public String toString(String separator) {
        String s = "";
        for (int i = 0; i < this.size(); i++) {
            s = s + this.getSentence(i).toString() + separator;
        }
        return s;
    }

    /**
     * The main method contains a general class tester.
     * @param argv One parameter may be indicated, containing the path to a file
     * to be processed.
     */
    public static void main(String[] argv) throws Exception {
        /** /
        if ( true ) {
            test20171202at02h21();
            return;
        }
        /** /
        Random rand= new Random();
        Text t= new Text();
        int offset= rand.nextInt(1024*1024*1024);
        int length= 4*1024*1024;
        //t.readFile("/Users/john/Dropbox/Research/Laboratory/Self-Similarity-Text/BibleKJV.txt", offset, length);
        t.readFile("/a/news@google/newscorpus.txt", offset, length);
        t.saveFile("/Users/john/Dropbox/Research/Laboratory/Self-Similarity-Text/GNewsRanSub.txt");
        if ( true ) return;
        /**/ 
        String s =
                "Pierre Vinken, 61 years old, will join the board as a "
                + "nonexecutive director Nov. 29. Mr. Vinken is chairman of "
                + "Elsevier N.V., the Dutch publishing group. Rudolph Agnew, "
                + "55 years old and former chairman of Consolidated Gold "
                + "Fields PLC, was named a director of this British "
                + "industrial conglomerate ."
                ;
        Text txt = new Text();
        if ( argv.length > 0  &&  txt.readFile(argv[0]) != null ) {
            txt.print("[", "]", true);
            return;
        }

        System.out.println("\n");
        System.out.println("[A TEST SHOWING SENTENCE BOUNDARY DETECTION]\n");

        System.out.println("ORIGINAL TEXT EXAMPLE:");
        System.out.println("   "+Toolkit.sline('-', 44));
        Toolkit.formatedWrite(s, 42, "   |", "|");
        System.out.println("   "+Toolkit.sline('-', 44));
        System.out.println();

        System.out.println("WITH DEFAULT SENTENCE BOUNDARY DETECTION:");
        txt= new Text(s);
        txt.print("[", "]", true);

        System.out.println("\nWITH OPEN NLP THE RESULT WOULD BE:");
        System.out.println("[  1 ---> Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29.]");
        System.out.println("[  2 ---> Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group.]");
        System.out.println("[  3 ---> Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a director of " +
                           "this British industrial conglomerate.]");

        /* ------------------------------------------------ *\
         * EXAMPLE USING OPEN NLP (MUST BE INSTALED).
         * TO UNCOMMENT THIS BLOCK AND TRY IT,
         * REDEFINE THE PATH FOR THE MODELS, BELOW.
         * ------------------------------------------------ * /
        System.out.println("\n");
        OpenNLPKit onlpk= new OpenNLPKit("/a/tools/opennlp-tools-1.5.0/models/english/");
        txt= new Text(s,onlpk);
        txt.print("[", "]", true);
         /* ----------------------------------------------- */

        testSimilarity();
    }


    public static boolean testSimilarity() {
        System.out.println("\n\n\n");
        System.out.println("[TEXT SIMILARITY TEST]\n");
        String message=
                  "Computes a lexical similarity between two texts, based on local evidence. "
                + "This function uses an adaptation of the TF*IDF vector representation, without "
                + "computing the heavy IDF component. Based on the Zipf law, the word length and "
                + "the relative frequency are considered for the vectorial calculations."
                ;
        Toolkit.formatedWrite(message, 60, "", "");
        System.out.println("\n");

        String stA =
                "Russian scientists are reporting success in their quest to drill into Lake Vostok, "
                + "a huge body of liquid water buried under the Antarctic ice. It is the first time "
                + "such a breakthrough has been made into one of the more than 300 sub-glacial lakes "
                + "known to exist on the White Continent. Researchers believe Vostok can give them "
                + "some fresh insights into the frozen history of Antarctica. They also hope to find "
                + "microbial lifeforms that are new to science. This fills my soul with joy, said "
                + "Valery Lukin, from Russia's Arctic and Antarctic Research Institute (AARI) in "
                + "St Petersburg, which has been overseeing the project, This will give us the "
                + "possibility to biologically evaluate the evolution of living organisms... "
                + "because those organisms spent a long time without contact with the atmosphere, "
                + "without sunlight, he was quoted as saying in a translation of national media "
                + "reports by BBC Monitoring."
                ;
        System.out.println("\nTEXT A");
        System.out.println(  "------");
        Text ta= new Text(stA);
        ta.printJustified(60);

        String stB =
                "Less than 600 frozen miles from the South Pole, Russian scientists in Antarctica say "
                + "they have successfully drilled down to Lake Vostok, a mysterious body of water "
                + "sealed two miles beneath the polar ice cap. They have been trying to reach it for "
                + "two decades. Sampling it, they say, may yield important clues about the history of "
                + "Earth, and perhaps other worlds as well. Valery Lukin, the head of Russia's Arctic "
                + "and Antarctic Research Institute (AARI) in charge of the mission, said in a "
                + "statement today that his team reached the lake's surface Sunday. They had been "
                + "cautious until now, saying they wanted to check readings from sensors on their "
                + "remote-controlled drill. There is no other place on Earth that has been in "
                + "isolation for more than 20 million years, Lev Savatyugin, a researcher with the "
                + "AARI who was involved in preparing the mission, told The Associated Press. "
                + "It's a meeting with the unknown."
                ;
        System.out.println("\nTEXT B");
        System.out.println(  "------");
        Text tb= new Text(stB);
        tb.printJustified(60);
        
        System.out.println("\n\n");
        for (Sentence s : tb.getSentences()) {
            System.out.println(s);
        }

        String stC =
                "High quality global journalism requires investment. Please share this article with "
                + "others using the link below, do not cut & paste the article. See our Ts&Cs and "
                + "Copyright Policy for more detail. Email ftsales.support@ft.com to buy additional "
                + "rights. Greek political leaders appeared to be moving nearer to approving further "
                + "austerity measures in return for a second ?130bn bail-out as they began a critical "
                + "meeting on Wednesday with Lucas Papademos, prime minister. The three party leaders "
                + "backing a technocrat-led, national unity government were given six hours before the "
                + "meeting to examine a 50-page document compiled by finance ministry officials that "
                + "detailed ?3bn of extra spending cuts aimed at shoring up this year?s budget and "
                + "averting a messy default."
                ;
        System.out.println("\nTEXT C");
        System.out.println(  "------");
        Text tc= new Text(stC);
        tc.printJustified(60);

        String stD =
                "Investment is requiered for high quality global journalism. Share this article with "
                + "others using the link below. Greek political leaders appeared to be moving nearer "
                + "to approving further "
                + "austerity measures in return for a second bail-out as they began a critical "
                + "meeting on Wednesday with Papademos. The three party leaders "
                + "were given six hours before the "
                + "meeting to examine a 50-page document compiled by finance ministry officials that "
                + "detailed $3bn of extra cuts aimed at shoring up this year?s budget and "
                + "averting a messy default."
                ;
        System.out.println("\nTEXT D");
        System.out.println(  "------");
        Text td= new Text(stD);
        td.printJustified(60);

        System.out.println("\n\nSIMILARITY VALUES:");
        System.out.println(    "------------------");
        System.out.printf("   sim(A,B) = %f\n", ta.similarity(tb));
        System.out.printf("   sim(B,A) = %f\n", tb.similarity(ta));
        System.out.printf("   sim(A,C) = %f\n", ta.similarity(tc));
        System.out.printf("   sim(B,C) = %f\n", tb.similarity(tc));
        System.out.printf("   sim(C,D) = %f\n", tc.similarity(td));
        return true;
    }
    
    
    public static void test20171202at02h21() {
        String s= "And on Friday, Trump denied reports, confirmed by his own advisers, that he is readying plans to get rid of Secretary of State Rex Tillerson.";
        Text t= new Text();
        Text tx= new Text(s.replaceAll("['\"]", ""));
        t.add(tx);
        t.print();
        
    }
}
