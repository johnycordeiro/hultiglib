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
 * dentifying information:
 *
 *       "Portions Copyrighted [year] [name of copyright owner]"
 *************************************************************************
 */
package hultig.sumo;

import java.io.*;
import java.util.*;
import java.text.BreakIterator;
import java.io.Serializable;

import hultig.util.CronoSensor;
import hultig.util.JPrimes;


/**
 * <p>Represents a corpora lexical index, by associating a unique number,
 * the index, to each word. This main goal of this class is to have a
 * more efficient (faster) text processing.
 * </p>
 * <p>
 * The text corpus may be incrementally added, file by file, and the
 * redefinition of the index is executed by invoking the
 * {@link #rebuild() rebuild()} method. The dictionary is reset through 
 * the {@link #clearHash() clearHash()} method.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public class CorpusIndex implements Serializable
{
    /**
     * A corpora index with the words/tokens being
     * the keys. Given a word we can obtain its
     * numeric index.
     */
    public TreeMap<String, Integer> sdict;

    /**
     * A corpora index with the numeric index being
     * the key. Given a numeric index we can get
     * the corresponding word.
     */
    public TreeMap<Integer, String> idict;

    /**
     * An hash table for counting word frequencies in corpora.
     */
    public Hashtable<String, Integer> hstab;

    /**
     * Size of word truncation. If this value is greater than
     * zero, the corpora read tokens will be truncated, they
     * are stored with {@code TRUNCV} maximum length.
     */
    public int TRUNCV;

    /**
     * The code for disabling word truncation,
     * see {@link #TRUNCV TRUNCV}
     */
    public static int NO_TRUNC = -1;

    /**
     * The text encoding string used to read the text corpora,
     * for example {@code UTF-8}, or {@code ISO-8859-1}.
     */
    private String ENCODE;
    
    
    /**
     * Whenever a given word is added, this flag will be
     * activated, returning to the "false" state only 
     * when the rebuild method was called.
     */
    private boolean somethingNewWasAdded= false;
    

    /**
     * The default constructor initializes the class main
     * properties and components, by also calling the
     * {@link #clearHash() clearHash()} method. The
     * default encoding is {@code UTF-8}.
     */
    public CorpusIndex() {
        this.sdict = null;
        this.idict = null;
        this.clearHash();
        this.TRUNCV = NO_TRUNC;
        this.ENCODE= "UTF-8";
    }

    /**
     * Provides the main initializations on this class, by also calling
     * the {@link #clearHash() clearHash()} method, and sets the word
     * truncation value.
     * @param truncv The word truncation value
     * (see {@link #TRUNCV TRUNCV}).
     */
    public CorpusIndex(int truncv) {
        this.sdict = null;
        this.idict = null;
        this.clearHash();
        this.TRUNCV = truncv;
        this.ENCODE= "UTF-8";
    }

    /**
     * Defines several string patterns as being meta-data that should
     * not be considered as textual tokens. The method tests if a given
     * string satisfies such patterns.
     * @param s The string to be tested
     * @return The {@code true true} value is the input string matches
     * one of the defined patterns.
     */
    private boolean isMetaData(String s) {
        s = s.toLowerCase();

        //The old format.
        if (s.matches(".*<cluster.*>.*")) {
            return true;
        }
        if (s.matches(".*</cluster>.*")) {
            return true;
        }
        //-----------

        if (s.matches(".*<group.*>.*")) {
            return true;
        }
        if (s.matches(".*</group>.*")) {
            return true;
        }

        if (s.matches(".*<new.*>.*")) {
            return true;
        }
        if (s.matches(".*</new>.*")) {
            return true;
        }

        return false;
    }

    /**
     * Recreates the current index main table {@link #hstab hstab}.
     */
    public final void clearHash() {
        hstab = new Hashtable<String, Integer>();
    }

    /**
     * Splits a given string sentence in a list of words.
     * @param s The string sentence.
     * @return The list of words/tokens found in {@code s}.
     */
    public static Vector<String> splitWords(String s) {
        if (s == null) {
            return null;
        }

        //Starts by spliting the string in possibly several sentences.
        BreakIterator boundary = BreakIterator.getWordInstance(Locale.UK);
        boundary.setText(s);
        int a = boundary.first();

        Vector<String> v = new Vector<String> ();
        for (int b = boundary.next(); b != BreakIterator.DONE; a = b,
             b = boundary.next()) {
            String word = s.substring(a, b).trim();
            if (word.length() > 0) {
                v.add(word);
            }
        }
        return v;
    }

    /**
     * Recreates the index from a list of string tokens, presumably words.
     * @param vtokens The list of string tokens.
     * @return The {@code true} value on success, and {@code false} if
     * some erroneous situation occurs.
     */
    public boolean readCorpus(Vector<String> vtokens) {
        if (vtokens == null) {
            return false;
        }

        clearHash();
        for (int i = 0; i < vtokens.size(); i++) {
            String token = vtokens.elementAt(i);
            Integer I = hstab.get(token);
            if (I == null) {
                hstab.put(token, new Integer(1));
            }
            else {
                hstab.put(token, new Integer(I.intValue() + 1));
            }
        }

        if (hstab.size() < 1) {
            return false;
        }
        recreateTreeMaps();
        return true;
    }

    /**
     * Reads a corpus text file, recreating the index. This method calls
     * {@link #readCorpus(java.lang.String, boolean) readCorpus(filename, false)}.
     * @param filename The file name from which text will be read.
     * @return The {@code true} value on success, and {@code false} if
     * some erroneous situation occurs.
     */
    public boolean readCorpus(String filename) {
        return readCorpus(filename, false);
    }

    /**
     * Reads a corpus text file, incrementally adding their new "unseen" words to
     * this object. The index is only recreated if {@code adding = false}.
     * @param filename The file name from which the corpus is read.
     * @param adding A flag that determines whether previously read corpora data
     * should be maintained, or cleaned.
     * @return The {@code true} value on success, and {@code false} if
     * some erroneous situation occurs.
     */
    public boolean readCorpus(String filename, boolean adding) {
        try {
            InputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, ENCODE));

            System.out.printf("   READING CORPUS (%s) ...\n", filename);
            CronoSensor crono = new CronoSensor();
            if ( ! adding )  clearHash();
            for (long nLns=0L; ; ) {
                String line = br.readLine();
                if (line == null) {
                    br.close();
                    break;
                }

                //ingnore some xml corpus tags
                if (isMetaData(line)) {
                    continue;
                }

                //add words to the index
                Vector<String> v = splitWords(line);
                for (int k = 0; k < v.size(); k++) {
                    String token = v.elementAt(k);
                    //if ( !token.matches("[a-zA-Z].*") ) continue;
                    if (TRUNCV > 0 && token.length() > TRUNCV) {
                        token = token.substring(0, TRUNCV);
                    }

                    incToken(token, hstab);
                    incToken(token.toLowerCase(), hstab);
                }

                if ( ++nLns % 1000 == 0 )
                    System.out.printf("   PROC CORPUS ...      N.LINES: %6d       N.WORDS: %d\n", nLns, hstab.size());
            }
            in.close();

            if (hstab.size() < 1) {
                return false;
            }
            int k = recreateTreeMaps();
            System.out.printf("   CORPUS ANALYZED (#tokens: %d   dt: %d ms)\n", k, crono.dt());
        }
        catch (Exception exc) {
            System.err.println("   [readCorpus] --> (" + exc.toString() + ")");
            return false;
        }

        return true;
    }

    /**
     * Incrementally adds the words of a given {@code Text} to this corpora
     * index. This method should be adequately used and combined with the
     * methods {@link #clearHash() clearHash()} and {@link #rebuild()
     * rebuild()}, as exemplified below:
     * <pre>
     *    CorpusIndex dic= new CorpusIndex();
     *    dic.clearHash();
     *    dic.addText(txt1);
     *    dic.addText(txt2);
     *    dic.addText(txt3);
     *    dic.rebuild();
     * </pre>
     * @param txt The text to be added to this index.
     */
    public void addText(Text txt) {
        for (int i = 0; i < txt.size(); i++) {
            Sentence stc = txt.getSentence(i);
            this.add(stc);
        }
    }

    /**
     * Incrementally adds the words of a given {@code Sentence} to
     * this corpora index. Operates similarly to
     * {@link #addText(hultig.sumo.Text) addText(Text)}.
     * @param stc The sentence to be added to this index.
     */
    public void add(Sentence stc) {
        for (int k = 0, n = stc.size(); k < n; k++) {
            String word = stc.getWord(k);
            //System.out.printf("   ... w: %s\n", word);
            //incToken(word, hstab);
            incToken(word.toLowerCase(), hstab);
        }
        somethingNewWasAdded= true;
    }

    /**
     * Adds the words contained in a given string to this corpus index.
     * @param str The input string.
     */
    public void add(String str) {
        if ( str == null )  return;
        String[] vs= str.split("[^a-zA-Z-]+");
        for (String s : vs) {
            incToken(s, hstab);
            String sL= s.toLowerCase();
            if ( !s.equals(sL) )
                incToken(sL, hstab);            
        }
        somethingNewWasAdded= true;
    }
    
    /**
     * Adds the words contained in an array of strings to this corpus
     * index. This method invokes the {@link #add(java.lang.String)
     * add(String)} method.
     * @param vs The array of strings to be processed and integrated.
     */
    public void add(String[] vs) {
        if ( vs == null )  return;
        for (String w : vs)  add(w);
    }

    /**
     * Adds the words contained in an array os sentences to this corpus
     * index. This method invokes the {@link #add(hultig.sumo.Sentence)
     * add(Sentence)} method.
     * @param vs The array of sentences from which to add the words.
     */
    public void add(Sentence[] vs) {
        if ( vs == null )  return;
        for (Sentence s : vs)  add(s);
    }

    /**
     * Increments one unit the frequency of a given token in a given
     * hash table. If the token do not belong to the table, a new
     * entry is created for that token, with frequency equal to one.
     * @param token The token to get their frequency incremented.
     * @param hst The hash table from which the token belongs.
     */
    private void incToken(String token, Hashtable<String, Integer> hst) {
        Integer I = hst.get(token);
        
        if (I == null) {
            hst.put(token, new Integer(1));
        }
        else {
            hst.put(token, new Integer(I.intValue() + 1));
        }
    }

    /**
     * Recreates the corpus index upon the text loaded so far.
     * The numeric indexes are recomputed.
     */
    public void rebuild() {
        this.recreateTreeMaps();
    }

    private int recreateTreeMaps() {
        idict = new TreeMap<Integer, String> ();
        sdict = new TreeMap<String, Integer> ();
        Vector<String> vkeys = new Vector<String> (hstab.keySet());
        Collections.sort(vkeys);
        int k= 0;
        for (; k < vkeys.size(); k++) {
            Integer IK= new Integer(k+1);
            idict.put(IK, vkeys.elementAt(k));
            sdict.put(vkeys.elementAt(k), IK);
        }
        somethingNewWasAdded= false;
        return k;
    }

    /**
     * Prints the corpus index in a text file (see {@link #printDict(java.io.PrintStream)
     * printDict(PrintStream)}).
     * @param fout The file name into which the corpus index is going to be printed.
     * @return The {@code true} value on success, and {@code false} if some erroneous
     * situation occurs.
     */
    public boolean printDict(String fout) {
        PrintStream output;
        try {
            output = new PrintStream(new FileOutputStream(fout));
        }
        catch (Exception expc) {
            System.err.printf(
                "ERROR: when open %f file => standard output assumed\n", fout);
            output = System.out;
        }

        return printDict(output);
    }

    /**
     * Prints the corpus index in a text file. Each word is printed with its numeric index
     * and its the frequency, on word per line, in the format "{@code KEY  WORD  FREQ}", for
     * example: {@code 10045  economy  2795}.
     * @param out The file stram into which the corpus index is going to be printed.
     * @return The {@code true} value on success, and {@code false} if some erroneous
     * situation occurs.
     */
    public boolean printDict(PrintStream out) {
        System.out.printf("PRINT DICTIONARY START\n");
        if (idict == null || idict.size() < 1) {
            System.err.println("ERROR - A");
            return false;
        }

        for (Map.Entry<Integer,String> entry : idict.entrySet()) {
            Integer k= entry.getKey();
            String  w= entry.getValue();
            Integer f= hstab.get(w);
            out.printf("%012d %s %d\n", k, w, f.intValue());
        }

        if (out != System.out) {
            out.close();
        }
        
        System.out.println("PRINT DICTIONARY END");
        return true;
    }

    /**
     *	Load the Dictionary from an ASCII file.
     */
    /**
     * Loads a corpus index table from a text file. The expected format is a
     * 3-tuple per line as follows: {@code KEY  TOKEN  FREQ}, similarly to
     * the scheme and example shown in method {@link #printDict(java.io.PrintStream)
     * printDict(PrintStream)}. This last one is the symmetric method of this one.
     * @param filename The file name from which to load the table.
     * @return The {@code true} value on success, and {@code false} if some erroneous
     * situation occurs.
     */
    public boolean loadASCIIDictionary(String filename) {
        try {
            InputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            idict = new TreeMap<Integer, String> ();
            sdict = new TreeMap<String, Integer> ();
            hstab = new Hashtable<String, Integer> ();

            for (; ; ) {
                String line = br.readLine();
                if (line == null) {
                    br.close();
                    break;
                }
                StringTokenizer st = new StringTokenizer(line);
                if (st.countTokens() < 3) {
                    System.err.printf("ERROR - READING LINE (#TOKENS < 3)\n");
                    System.err.printf("LINE = [%f]\n", line);
                    continue;
                }

                Integer K = new Integer(st.nextToken()); //KEY
                String S = st.nextToken(); // TOKEN
                Integer F = new Integer(st.nextToken()); //FREQ

                idict.put(K, S);
                sdict.put(S, K);
                hstab.put(S, F);
            }
            System.out.printf("DICTIONARY LOADED WITH %d ENTRIES.\n",
                              idict.size());
        }
        catch (Exception exc) {
            System.err.printf("ERROR WHEN LOADING DICT. (FILE: %s)\n", filename);
            return false;
        }

        return true;
    }

    /**
     * Redefines this corpus index, based on an already existing one.
     * @param d The new index that redefines this object.
     */
    public void load(CorpusIndex d) {
        this.idict = d.idict;
        this.sdict = d.sdict;
        this.hstab = d.hstab;
        this.TRUNCV = d.TRUNCV;
        this.somethingNewWasAdded= d.somethingNewWasAdded;
    }

    /**
     * Loads a given corpora index from a binary file, previously saved
     * by and instance of this class, through the method:
     * {@link #save(java.lang.String) save(String)}.
     * @param fname The file name from which to read.
     * @return The {@code true} value on success, and {@code false} if
     * some erroneous situation occurs.
     */
    public boolean load(String fname) {
        try {
            FileInputStream istream = new FileInputStream(fname);
            ObjectInputStream ois = new ObjectInputStream(istream);
            CorpusIndex dict = (CorpusIndex) ois.readObject();
            load(dict);
            istream.close();
        }
        catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Saves this object to a binary file.
     * @return The {@code true} value on success, and {@code false}
     * if some erroneous situation occurs.
     */
    public boolean save(String fname) {
        System.out.printf("SAVE CORPORA INDEX STARTED (FILE: %s)\n", fname);
        try {
            FileOutputStream ostream = new FileOutputStream(fname);
            ObjectOutputStream oos = new ObjectOutputStream(ostream);

            oos.writeObject(this);
            oos.flush();
            oos.close();
        }
        catch (Exception e) {
            System.out.println("(save) --> [" + e + "]");
            return false;
        }

        System.out.println("SAVE CORPORA INDEX CONCLUDED.");
        return true;
    }

    /**
     *	Codifies a file according to the loaded dictionary.
     */
    /**
     * Codifies a file according to the loaded dictionary.
     * @param infile The file to be codified.
     * @param outfile The generated codified file.
     */
    public void codeFile(String infile, String outfile) {
        //open output stream
        PrintStream out;
        try {
            out = new PrintStream(new FileOutputStream(outfile));
        }
        catch (Exception expc) {
            out = System.out;
        }

        //main loop
        try {
            InputStream in = new FileInputStream(infile);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            for (; ; ) {
                String line = br.readLine();
                if (line == null) {
                    br.close();
                    break;
                }

                if (isMetaData(line)) {
                    out.print(line + '\n');
                    continue;
                }

                //for each sentence read.
                Vector<String> v = splitWords(line);
                for (int k = 0; k < v.size(); k++) {
                    String token = v.elementAt(k);
                    if (TRUNCV > 0 && token.length() > TRUNCV) {
                        token = token.substring(0, TRUNCV);
                    }

                    Integer I = sdict.get(token);
                    if (I == null) {
                        out.print("-1 ");
                    }
                    else {
                        out.print(I + " ");
                    }
                }
                out.print("\n");
            }
            in.close();
        }
        catch (Exception exc) {
        }

        //close output stream
        if (out != System.out) {
            out.close();
        }
    }

    /**
     * Get the token from a given code key. If such key do not belong to this
     * dictionary then the <b>null</b> value is returned.
     */
    public String get(int key) {
        if (idict == null) {    
            if ( hstab == null )
                return null;
            else
                rebuild(); //===> idict will become ? null
        }
        
        return idict.get(key);
    }

    /**
     * Given an array of codes, expecting to represent a word sequence, like
     * for example a sentence, it returns its corresponding string form.
     * @param vkeys int[] The array of word keys
     * @return String
     */
    public String get(int[] vkeys) {
        if (vkeys == null) {
            return null;
        }
        StringBuilder sbuff = new StringBuilder();
        for (int i = 0; i < vkeys.length; i++) {
            String w = this.get(vkeys[i]);
            sbuff.append(w);
            if (i < vkeys.length - 1) {
                sbuff.append(' ');
            }
        }
        return sbuff.toString();
    }

    /**
     * Get the code from a given token.
     * @param token The token string.
     * @return The code or -1 if something is wrong.
     */
    public int get(String token) {
        if (sdict == null) {
            if ( hstab == null )
                return -1;
            else
                rebuild(); //===> sdict will become ? null
        }
        
        try {
            int cod = sdict.get(token);
            return cod;
        } catch (Exception exc) {
            //System.out.println("\ttoken: ("+token+")   erro: ("+exc+")");
            return -1;
        }   
    }

    /**
     * Gives the token frequency.
     * @param token
     * @return The -1 value when a given token was not found in
     * this dictionary.
     */
    public int freq(String token) {
        if (token == null || hstab == null) {
            return -1;
        }
        Integer I = hstab.get(token);
        return I == null ? -1 : I.intValue();
    }

    /**
     * Sums the frequencies for all tokens.
     * @return The sum or else -1 meaning that the dictionary hashtable
     * is not defined.
     */
    public int sum() {
        if (hstab == null) {
            return -1;
        }
        int soma = 0;
        for (Enumeration<Integer> e = hstab.elements(); e.hasMoreElements(); ) {
            Integer I = e.nextElement();
            soma += I.intValue();
        }
        return soma;
    }


    /**
     * Codifies any "Word" contained in an array of Sentences, according
     * to this dictionary. By "codifying" here we mean that any word
     * get its dictionary index.
     * @param vs
     */
    public void codify(Sentence[] vs) {
        for (int i = 0; i < vs.length; i++) {
            vs[i].codify(this);
        }
    }
       
    /**
     * Codification "on the fly" for a given array of sentences. It
     * means that the dictionary is automatically created for the
     * received array of sentences and their words are codified
     * accordingly.
     * @param sentences The array of sentences to be codified.
     */
    public static CorpusIndex codifyOnFly(Sentence... sentences) {
        if ( sentences == null )  return null;
        
        CorpusIndex dic= new CorpusIndex();
        for (Sentence s : sentences)  dic.add(s);
        dic.rebuild();
        
        for (Sentence s : sentences)  s.codify(dic);
        
        return dic;
    }

    
    /**
     * Codification "on the fly" for an array of chunked sentences.
     * This method is similar to the {@link #codifyOnFly(hultig.sumo.Sentence[])
     * codifyOnFly(Sentence[] sentences)} method
     * @param sentences The array of chunked sentences to be codified.
     */
    public static void codifyOnFly(ChunkedSentence[] sentences) {
        if ( sentences == null )  return;
        
        CorpusIndex dic= new CorpusIndex();
        for (ChunkedSentence s : sentences)  dic.add(s);
        dic.rebuild();
        
        for (ChunkedSentence s : sentences)  s.codify(dic);
    }

    /**
     * Defines a new encoding for reading corpora text files.
     * @param encode The encoding string, for example:
     * {@code UTF-8}, or {@code ISO-8859-1}.
     */
    public void setEncoding(String encode) {
        this.ENCODE= encode;
    }

    /**
     * Gives the current encoding string, used to read corpora
     * files.
     * @return The encoding string.
     */
    public String getEncoding() {
        return this.ENCODE;
    }
    
    /**
     * Prints the set of arguments that can be passed through the command
     * line ({@link #main(java.lang.String[]) main}).
     */
    public static void printHelp() {
        System.out.println("\n");
        System.out.println(
            "--------------------------------------------------------------");
        System.out.println(
            " SINTAXE: java CorpusDict -inp <corpus file> [OPTIONS]");
        System.out.println(
            "--------------------------------------------------------------");
        System.out.println(" OPTIONS:");
        System.out.println("----------");
        System.out.println(
            "   -inp     It's mandatory and specifies the input file.");
        System.out.println("   -outd    Specifies the dict. output file.");
        System.out.println("   -dict    Specifies the dict. to be used.");
        System.out.println("   -pdic    Print dict. to file.");
        System.out.println("   -codf    Specifies the output code file.");
        System.out.println("   -trunc   The value for dicttionary truncation.");
        System.out.println("   -help    Display this help.");
        System.out.println("\n");
        System.out.println("EXAMPLES:");
        System.out.println(
            "   java ... -inp a.txt -outd a.dic -codf a.cod -trunc 7");
        System.out.println("   java ... -inp a.txt -dict a.dic -codf a.cod");
        System.out.println("   java ... -inp fna.txt -outd fna.dic");
        System.out.println(
            "--------------------------------------------------------------");
        System.out.println("\n");
    }


    public static void demoForWeb() {
        String s1= "Radiation from this solar flare will hit Earth's magnetic field on Wednesday";
        String s2= "Our magnetic field will be affected, next Wednesday, by this solar flare.";
        String s3= "Tim Cook and Philip Schiller unveil the company's newest iPad.";

        CorpusIndex dict= new CorpusIndex(); //==> Creates the indexer
        dict.add(s1); //==> Adds one string.
        dict.add(s2); //==> Adds another string.

        Text t= new Text(s2); //==> Creates a new text, from string s2.
        t.codify(dict); //==> Codifies the created text.

        dict.add(s3); //====> Adds a new string to the indexer.
                          //> new code.
        t.add(s3); //=======> Adds a new string to the text.
        t.codify(dict); //==> Re-cofdifies the text, now with two sentences,
                          //> with the rebuilded indexer.
        dict.printDict(System.out);
    }
    

    /**
     * This "main" method enables the command line execution of this
     * class in order to create a given corpus dictionary.
     * @param args Should comply with the syntax defined in the
     * {@link #printHelp() printHelp()} method.
     */
    public static void main(String[] args) {
        demoForWeb();
        if (true) return; 
        
        String input = null;
        String outdic = null;
        String codf = null;
        String dict = null;
        String pdic = null;
        int trunc_value = CorpusIndex.NO_TRUNC;

        //Process Arguments
        for (int k = 0; k < args.length; k++) {
            if (args[k].equals("-inp") && k < args.length) {
                input = args[++k];
            }
            if (args[k].equals("-outd") && k < args.length) {
                outdic = args[++k];
            }
            if (args[k].equals("-dict") && k < args.length) {
                dict = args[++k];
            }
            if (args[k].equals("-pdic") && k < args.length) {
                pdic = args[++k];
            }
            if (args[k].equals("-codf") && k < args.length) {
                codf = args[++k];
            }
            if (args[k].equals("-trunc") && k < args.length) {
                try {
                    trunc_value = Integer.parseInt(args[++k]);
                }
                catch (Exception exc) {
                    trunc_value = CorpusIndex.NO_TRUNC;
                }
            }

            if (args[k].equals("-help") && k < args.length) {
                printHelp();
                return;
            }
        }
        //---

        if (input == null) {
            printHelp();
            return;
        }

        CorpusIndex dictionary = new CorpusIndex(trunc_value);
        if (dict == null) {
            dictionary.readCorpus(input);
        }
        else {
            dictionary.load(dict);
        }

        if (pdic != null) {
            dictionary.printDict(pdic);
        }

        if (outdic != null) {
            dictionary.save(outdic);
        }

        if (codf != null) {
            System.out.println("GENERATING CODE FILE ...");
            dictionary.codeFile(input, codf);
            System.out.println("CODE FILE COMPLETE");
        }
    }
}
