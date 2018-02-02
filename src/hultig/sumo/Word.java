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

import java.util.*;
import java.io.*;

import hultig.util.Toolkit;


/**
 * <p>A class to represent and process a textual word.</p>
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 * @author João Paulo Cordeiro
 * @version 1.5
 */
public class Word implements Serializable
{
    public static final long serialVersionUID = -5223039887894735826L;
    //public static final POSType postype= new POSType();
    
    private String word; 		//---> Hold the word string
    private Vector<String> META; 	//---> Hold meta data about the word.

    /**
     * Introduced later, in June 2008. The idea is to use several codes
     * representing different kind of tags, lexical, syntactical, among
     * possibly others. So far the first three positions are used to
     * store respectively the lexical, POS, and chunker codes.
     */
    public int[] cods;

    /**
     * Holds the part-of-speech tag of this word.
     * Introduced on 2007/11/11, but now obsolete due to
     * the <pre>cods</pre> array, added later on this class.
     */
    private char[] POS;


    public ChunkTag CHTAG;


    public static String RPUNCT= ",.;:!?";


    public long FREQ;

    
    /**
     * Default constructor.
     */
    public Word()
    {
        word= null;
        META= null;
        //code = -1;
        POS = null;
        cods= null;
        CHTAG= null;
        FREQ= 0L;
    }

    
    /**
     * Create a new word from a given received String. It assumes that
     * this string contains in fact a sequence of characters
     * representing just one word. This constructor invokes the
     * <pre>set(String word)</pre> method.
     * @param word The String containing the word.
     */
    public Word(String word)
    {
        set(word);
        //code = -1;
        cods= null;
        POS = null;
        CHTAG= null;
    }
  
    /**
     * Create a new word from a given received String. It assumes that
     * this string contains in fact a sequence of characters
     * representing just one word. This constructor invokes the
     * <pre>set(String word)</pre> method. The created word is also
     * labeled with a meta-tag.
     * @param word The String containing the word.
     * @param meta_item The meta-tag labeling the created word.
     */
    public Word(String word, String meta_item)
    {
        set(word, meta_item);
        //code = -1;
        cods= null;
        POS = null;
        CHTAG= null;
    }

    
    /**
     * Create a word, labeling it with an array of multi-tags.
     * @param word The String containing the word.
     * @param meta The array of multi-tags.
     */
    public Word(String word, String[] meta)
    {
        set(word, meta);
        //code = -1;
        cods = null;
        CHTAG= null;
    }
    
    /**
     * Create a Word and mark it with a syntactic code.
     * @param word
     * @param syntcod
     */
    public Word(String word, int syntcod) {
        set(word);
        cods= new int[2];
        cods[0]= -1; //--------> código lexico
        cods[1]= syntcod; //---> código sintáctico
        //System.out.printf("CODIGO :::::::::::::::::::::::::> %d\n", syntcod);
    }

    /**
     * Redefines this word based on the received string, which
     * is assumed to contain just the alpha sequence
     * representing a single word.
     * @param word The received string.
     */
    public final void set(String word) {
        this.word= word;
        /*
        if ( RPUNCT.indexOf(word) >= 0 ) {
            setPosCod(postype.getCode("PCT"));
        }
        */
        META= null;
    }

    /**
     * Redefines this word based on the received string, which
     * is assumed to contain just the alpha sequence
     * representing a single word. The created word also receives
     * a meta-tag.
     * @param word The received string.
     * @param meta_item The meta-tag associated with this word.
     */
    public final void set(String word, String meta_item)
    {
        this.word= word;
        META= new Vector<>(1);
        META.add(meta_item);
    }
    
    /**
     * Access a word character at a given position.
     * @param k The position to read.
     * @return The character read.
     * @throws IndexOutOfBoundsException
     */
    public char charAt(int k) throws IndexOutOfBoundsException {
        try {
            return word.charAt(k);
        }
        catch(Exception exc) {
            System.out.printf("word=[%s]   k=%d\n",word, k);
            return 0;
        }
    }
 
    /**
     * Defines the word lexical code.
     * @param lexcod The code.
     */
    public void setLexCod(int lexcod) {
        if ( cods == null ) {
            cods= new int[1];
            cods[0]= lexcod;
        }
        else
            cods[0]= lexcod;
        
        //code= lexcod;
    }
    
    /**
     * Sets the POS tag code for this word.
     * @param poscod The POS code.
     */
    public void setPosCod(int poscod) {
        if ( cods == null ) {
            cods= new int[2];
            cods[0]= -1;
            cods[1]= poscod;
        }
        else if ( cods.length < 2 ) {
            int c= cods[0];
            cods= new int[2];
            cods[0]= c;
            cods[1]= poscod;
        }
        else
            cods[1]= poscod;
    }
    
    /**
     * Sets the chunk code of this word, meaning that this word is
     * contained in a chunk (shallow parsing) with that code.
     * @param chkcod The chunk code.
     */
    public void setChkCod(int chkcod) {
        if ( cods == null ) {
            cods= new int[3];
            cods[0]= -1;
            cods[1]= -1;
            cods[2]= chkcod;
        }
        else if ( cods.length > 2 ) {
            cods[2]= chkcod;
        }
        else if ( cods.length == 2 ) {
            int[] vc= cods;
            cods= new int[3];
            cods[0]= vc[0];
            cods[1]= vc[1];
            cods[2]= chkcod;
        }
        else {
            int c= cods[0];
            cods= new int[3];
            cods[0]= c;
            cods[1]= -101;
            cods[2]= chkcod;
        }
    }
    
    /**
     * Obtain this word lexical code.
     * @return The lexical code.
     */
    public int getLexCod() {
        return cods == null ? -1 : cods[0];
    }
    
    /**
     * Obtain the POS code.
     * @return The code.
     */
    public int getPosCod() {
        if ( cods == null || cods.length < 2 )  return -1;
        return cods[1];        
    }
    
    /**
     * Obtain the chunk code.
     * @return The code.
     */
    public int getChkCod() {
        if ( cods == null || cods.length < 3 )  return -1;
        return cods[2];
    }

    
    public final void set(String word, String[] meta)
    {
        this.word= word;
        META= new Vector<String>(meta.length);
        META.addAll(Arrays.asList(meta));
    }

    
    public void setMetaTag(String metatag, String value)
    {
        if ( this.isEmpty() )  return;
        if ( this.META == null )  {
            String meta_item = metatag + "=" + value;
            META= new Vector<String>(1);
            META.add(meta_item);
            return;
        }

        for (int k=0; k<META.size(); k++)  {
            String metaitem= (String)META.get(k);
            if ( metaitem.startsWith(metatag+"=") )
                META.set(k, metatag + "=" + value);
        }
    }
    


    /**
     * Test whether this word is POS tagged or not.
     * @return boolean
     */
    public boolean hasPOS() {
        return POS == null ? false : true;
    }
   
    /**
     * Gives the POS tag of this word.
     * @return String The POS tag.
     */
    public String getPOS() {
        return POS == null ? "UNDEFINED" : new String(POS);
    }


    public String getPOS(POSType post) {
        if ( cods[1] == -1 )
            return "UDF";
        else
            return post.cod2str(cods[1]);
    }


    /**
     * Get the first @param size chars, from the POS label.
     * @param size int
     * @return String
     */
    public String getPOS(int size) {
        String spos= "UNDEFINED";
        if ( POS != null ) spos= new String(POS);

        return spos.substring(0, Math.min(size, spos.length()));
    }



    /**
      * Returns the POS tag of this word, to a valid POS tag.
      * @param v char[]
      */
    public void setPOS(char[] v) {
        if ( v == null ) return;
        POS= v.clone();
    }


    /**
      * Returns the POS tag of this word, to a valid POS tag.
      * @param tag String
      */
    public void setPOS(String tag) {
        if ( tag == null ) return;
        POS= tag.toCharArray();
    }


    /**
     * Convert all characters from this word to lower case.
     */
    public String toLowerCase()
    {
        word= word.toLowerCase();
        return word;
    }
    
    
    /**
     * Equality test for two words, this and the other one.
     * @param w The other word.
     * @return
     */
    public boolean equals(Word w) {
        /* 20081122 1606 --> misterio da comparação com os códigos léxicos.
        int ka= getLexCod();
        int kb= w.getLexCod();
        if ( ka != -1 || kb != -1 ) {
            if ( ka == kb )
                return true;
            else {
                System.out.printf("(ka,kb)=(%d,%d)", ka, kb);
                return false;
            }
            //return ka == kb ? true : false;
        }
        */

        return word.equals(w.word);
    }
    
    
    public boolean equals(String ws) {
        return word.endsWith(ws);
    }


    /**
     * Override of the <b>toString()</b> method.
     * @return String The string of this word.
     */
    @Override
    public String toString()
    {
        return toString(false);
    }


    /**
     * A specific toString method. If the parameter flag is true
     * the toStringPOS() method is invoked.
     * @param with_pos_tags The part-of-speech flag.
     * @return The string representing this word.
     */
    public String toString(boolean with_pos_tags)
    {
        if ( !with_pos_tags )
            return this.word;
        else
            return toStringPOS();
    }


    public void posLabel(POSType post) {
        if ( cods.length > 1 && cods[1] != -1 ) {
            POS= post.cod2str(cods[1]).toCharArray();
        }
    }



    /**
     * Similar to the toStringPOS() method, except that the
     * part-of-speech representation is passed by parameter.
     * @param postype The POS representation.
     * @return Examples: "the/DT", "cat/NN", "is/VBZ",
     * "flying/VBG".
     */
    public String toStringPOS(POSType postype) {
        if ( cods[1] == -1 )
            return this.word;
        else
            return this.word + '/' + postype.cod2str(cods[1]);
    }


    /**
     * A toString() type method giving the word string
     * concatenated with its part-of-speech tag, if
     * defined.
     * @return Examples: "the/DT", "cat/NN", "is/VBZ",
     * "flying/VBG".
     */
    public String toStringPOS() {
        if ( isRPUNCT() ) return word + '/' + "punct";
        if ( POS != null ) 
            return word+'/'+new String(POS);
        return word+'/'+getTag();
    }


    /**
     * Transform an array of words into a single string, with each
     * word concatenated with its POS tag.
     * @param words The array of words.
     * @param post The POS representation.
     * @return The concatenated word string.
     */
    public static String words2StringPOS(Word[] words, POSType post) {
        if ( words == null || words.length < 1 )  return null;

        StringBuffer sb= new StringBuffer(words[0].getPOS(post));
        for (int i = 1; i < words.length; i++) {
            Word w = words[i];
            sb.append(' ');
            sb.append(w.getPOS(post));
        }

        return sb.toString();
    }

    /**
     * Get the POS tag of this word, if any is defined.
     * @return The POS tag or null.
     */
    public String getTag() {
        if ( this.META == null )  return "";

        String stag= (String)this.META.get(0);
        if ( stag.startsWith("POS=") ) stag= stag.substring(4);
        return stag;
    }

    /**
     * Return a given meta-tag value associated with this word. Meta-tags
     * are stored in the "META" list, where each element is a pair of the
     * form "type=value". For example: "polarity=positive".
     * @param metatag The meta-tag (ex: "polarity")
     * @return The value for that meta-tag (ex: "positive").
     */
    public String getMetaValue(String metatag) {
        if ( META == null )  return null;

        for (int k=0; k<META.size(); k++)  {
            String metaitem = (String) META.get(k);
            if ( metaitem.startsWith(metatag + "="))  {
                // The value, for that metatag.
                return metaitem.substring(metatag.length() + 1);
            }
        }
        return null;
    }

    /**
     * Test whether this word is undefined or not.
     * @return The boolean test result.
     */
    public boolean isEmpty() {
        if ( this.word == null )  return true;
        return false;
    }
    
    /**
     * Test if a given character is a punctuation mark.
     * @param c The character to be tested.
     * @return The boolean test result.
     */
    public static boolean isPunct(char c) {
        if ( RPUNCT.indexOf(c) < 0 )
            return false;
        else
            return true;
    }


    /**
     * Test if whether this is really a word, and not
     * for example a number or a punctuation mark, or
     * any other token.
     * @return The boolean test result.
     */
    public boolean isWord() {
        if ( word == null )  return false;
        for (int k=0; k<word.length(); k++)
            if ( !Character.isLetter(word.charAt(k)) )  return false;
        
        return true;
    }


    /**
     * Test if this is a number or a word. That is, we have either a
     * sequence of letters or a sequence of digits.
     *
     * @return The boolean test result.
     */
    public boolean isNumWord() {
        if ( word == null )  return false;
        
        try {
            Double.parseDouble(word);
            return true;
        }
        catch (Exception exc) {}

        for (int k=0; k<word.length(); k++) {
            char c= word.charAt(k);
            if ( !Character.isLetterOrDigit(c) )  return false;
        }

        return true;
    }

    /**
     * Test whether this is a punctuation mark.
     * @return The boolean test result.
     */
    public boolean isRPUNCT() {
        if ( word != null && word.length() == 1 && RPUNCT.indexOf(word) >= 0 )
            return true;
        else
            return false;
    }

    /**
     * Gives the word length.
     * @return The length.
     */
    public int length() {
        return word.length();
    }

    /**
     * Implements a metric that calculates the lexical distance between
     * two words. It is based on the idea of the prefix significance,
     * i.e. the farther we are from the words starting positions the
     * less significant will be the differences.
     * @param sa One word string.
     * @param sb The other word string.
     * @param q A formula parameter.
     * @return The calculated distance.
     */
    public static float distlex(String sa, String sb, float q) {
        if ( sa == null && sb == null )  return 0;
        if ( sa != null && sb == null )  return sa.length();
        if ( sa == null && sb != null )  return sb.length();

        if ( sa.length() > sb.length() ) {
            String saux= sa;
            sa= sb;
            sb= saux;
        }

        int k;
        float dist=0, d=1;
        for (k=0; k<sb.length(); k++)  {
            if ( k<sa.length() ) {
                if (sa.charAt(k) != sb.charAt(k))
                    dist += 1/d;
            }
            else
                dist+= 1/d;

            d= d*q;
        }

        return dist;
    }

    /**
     * This method implements a similar metric as in "distlex". The
     * main difference is that the most significative character
     * here is the word's last one.
     *
     * @param sa One word string.
     * @param sb The other word string.
     * @param q A formula parameter
     * @return The calculated distance.
     */
    public static float distlexSuffix(String sa, String sb, float q) {
        if ( sa == null && sb == null )  return 0;
        if ( sa != null && sb == null )  return sa.length();
        if ( sa == null && sb != null )  return sb.length();

        if ( sa.length() > sb.length() ) {
            String saux= sa;
            sa= sb;
            sb= saux;
        }

        int na= sa.length();
        int nb= sb.length();

        int k;
        float dist=0, d=1;
        for (k=0; k<nb; k++)  {
            if ( k<na ) {
                if (sa.charAt(na-k-1) != sb.charAt(nb-k-1))
                    dist += 1/d;
            }
            else
                dist+= 1/d;

            d= d*q;
        }
        return dist;
    }

    /**
     * Calls "distlexSuffix(sa, sb, 2f)".
     * @param sb The other word string.
     * @param q A formula parameter
     * @return The calculated distance.
     */
    public static float distlexSuffix(String sa, String sb) {
        return distlexSuffix(sa, sb, 2f);
    }

    /**
     * Calls "distlex(sa, sb, 2f)".
     * @param sb The other word string.
     * @param q A formula parameter.
     * @return The calculated distance.
     */
    public static float distlex(String sa, String sb) {
        return distlex(sa,sb,2f);
    }

    /**
     * Calls "distlex(word.toString(), s, 2f)".
     * @param s The other word string.
     * @return The calculated distance.
     */
    public float distlex(String s) {
        return distlex(word.toString(), s, 2f);
    }

    /**
     * Calls "distlex(word.toString(), w.toString(), q)"
     * @param w The other word.
     * @param q A formula parameter
     * @return The calculated distance.
     */
    public float distlex(Word w, float q) {
        return distlex(word.toString(), w.toString(), q);
    }

    /**
     * Calls "distlex(word.toString(), w.toString(), 2f)"
     * @param w The other word.
     * @return The calculated distance.
     */
    public float distlex(Word w) {
        return distlex(word.toString(), w.toString(), 2f);
    }

    /**
     * Another lexical metric, based on the cosine. Each word
     * is transformed into a vector.
     * @param w The other word.
     * @return The calculated distance.
     */
    public double distcos(Word w)
    {
        double[] u= new double[Math.max(this.length(), w.length())];
        double[] v= new double[u.length];

        for (int i=0; i<u.length; i++) {
            u[i]= 0.0;
            v[i]= 0.0;
        }

        String wu= this.toString();
        for (int i=0; i<wu.length(); i++) {
            char c= wu.charAt(i);
            u[i]= (double)('a' - c);
        }

        String wv= w.toString();
        for (int i=0; i<wv.length(); i++) {
            char c= wv.charAt(i);
            v[i]= (double)('a' - c);
        }

        double uv=0.0, u2=0.0, v2=0.0;
        for (int i=0; i<u.length; i++) {
            uv+= u[i]*v[i];
            u2+= u[i]*u[i];
            v2+= v[i]*v[i];
        }

        return uv/Math.sqrt(u2*v2);
    }

    /**
     * Computes a normalized <i>Edit Distance</i> of two words. The
     * <i>Edit Distance</i> value is divided by the maximum length
     * of the two words.
     * @param w The other word to compare to.
     * @return The normalized (in [0,1]) distance.
     */
    public double dnormEditDistance(Word w) {
        String sa= this.toString();
        String sb= w.toString();
        double d= editDistance(sa,sb);
        int na= sa.length();
        int nb= sb.length();

        if ( na > nb )
            d= 1.0 - d/na;
        else
            d= 1.0 - d/nb;

        return d;
    }


    /**
     * A normalized <i>Edit Distance</i> which normalizes by taking the
     * maximum common sequence between the two sentences
     * (Presented at ACL 2007).
     *
     * @param sa One string.
     * @param sb The other string.
     * @return double
     */
    public static double distSeqMax(String sa, String sb) {
        char[] a= sa.toCharArray();
        char[] b= sb.toCharArray();

        int maxseq= 0;
        for (int i=0; i<a.length; i++) {
            int ki=i;
            for (int j = 0; j<b.length; j++) {
                int kj= j, seqsz = 0;
                seqsz= prefixeSize(a, b, ki, kj);
                if ( seqsz > maxseq )  maxseq=seqsz;
            }
        }
        int n= Math.max(a.length, b.length);
        return (double)maxseq/n;
    }

    /**
     * An auxiliar method for calculating the common prefix size.
     * @param a One word's array codes
     * @param b The other word's array codes.
     * @param afrom Start position, in the a array.
     * @param bfrom Start positiion in the b array.
     * @return
     */
    private static int prefixeSize(char[] a, char[] b, int afrom, int bfrom) {
        int i= afrom, j= bfrom, size= 0;
        while ( i<a.length && j<b.length && a[i] == b[j] ) {
            i++;
            j++;
            size++;
        }
        return size;
    }


    /**
     * The <i>Edit Distance</i> complement. This is calculated
     * as follows: <br />
     * <pre>   size(max(wa,wb)) - editDistance(wa, wb)</pre>
     * @param w The other word to compare to.
     * @return The calculated value.
     */
    public int editProximity(Word w) {
        int na= this.length();
        int nb= w.length();

        int ed= editDistance(w);
        return na>nb ? na-ed : nb-ed;
    }

    /**
     * Calls the method "editDistance(this.toString(), w.toString())"
     * @param w The other word.
     * @return The calculated value.
     */
    public int editDistance(Word w) {
        return editDistance(this.toString(), w.toString());
    }


    /**
     * Computes <i>Levenshtein Distance</i>, also known as the
     * <i>Edit Distance</i>
     * @param s One string.
     * @param t The other string.
     * @return The calculated value.
     */
    public static int editDistance(String s, String t)
    {
        int d[][]; 	//--> matrix
        int n; 		//--> length of s
        int m; 		//--> length of t
        int i; 		//--> iterates through s
        int j; 		//--> iterates through t
        char s_i; 	//--> ith character of s
        char t_j; 	//--> jth character of t
        int cost; 	//--> cost

        // Step 1
        n = s.length ();
        m = t.length ();

        if (n == 0)  return m;
        if (m == 0)  return n;

        d = new int[n+1][m+1];
        // Step 2
        for (i = 0; i <= n; i++)  d[i][0] = i;
        for (j = 0; j <= m; j++)  d[0][j] = j;

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt (i - 1);
            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt (j - 1);
                // Step 5
                cost= s_i == t_j ? 0 : 1;
                // Step 6
                d[i][j] = Toolkit.minimum(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
            }
        }
        // Step 7
        return d[n][m];
    }


    /**
     * Cost of aligning two words. This formula was used to compute a
     * "Word Mutation Matrix", like in the gene mutation matrixes, in Biology.
     * (<i>Published on ACL07, by JPC</i>). <br />
     * <br />
     * <pre>   costAlign: Word x Word |-------> [0, +00[ </pre>
     * <br />
     * Remark that this function is a cost, it means that the grather the value the
     * more unlikely the alignment will be.
     * 
     * @param w Word
     * @return double
     */
    public double costAlign(Word w) {
        double epsilon= 0.01;
        if ( w == null )  return 2000; //---> 20/epsilon (because maximum word length < 20)

        String sa= this.toString();
        String sb= w.toString();

        int edit_distance= editDistance(sa, sb);
        double normalized_max_seq= distSeqMax(sa, sb);

        return (double)edit_distance / ( epsilon + normalized_max_seq );
    }

    /**
     * Similar to costAlign but inverted and normalized in the [0, 1] interval.
     * This function express the lexical conectivity between two words.
     * @param w Word
     * @return double
     */
    public double connectProb(Word w) {
        if ( w == null )  return 0; //---> No conectivity exists.

        String sa= this.toString();
        String sb= w.toString();

        //System.out.printf("         w.connectProb(Word w)   --->   sa:[%s]   sb[%s]\n", sa, sb);

        int edit_distance= editDistance(sa, sb);
        double normalized_max_seq= distSeqMax(sa, sb);

        //Note: the 0.25 parameter tunes the graphic curve to meet practical objectives.
        return 1.0 - 2.0 * Math.atan( 0.25 * edit_distance/(0.001 + normalized_max_seq) ) / Math.PI;
    }



    /**
     * The main method tests this class by executing several experiments for
     * a predefined set of word pairs.
     * @param args String[]
     */
    public static void main(String[] args)
    {
        PrintStream o= System.out;

        if ( args.length >= 2 ) {
            Word w1= new Word(args[0]);
            Word w2= new Word(args[1]);
            o.printf("   sim(%s,%s) = %f\n", w1, w2, w1.distcos(w2));
            o.printf("   dlex(%s,%s) = %f\n", w1, w2, 1.0-w1.distlex(w2,3.0f));
            return;
        }

        String[][] A= {
            {"correr", "correndo"}, {"reutilizar", "utilizar"}, {"arroz", "atroz"},
            {"arroz", "arrozal"}, {"informatica", "informatizado"}, {"tomate", "pepino"},
            {"procurador", "procurar"}, {"gosta", "gostou"}, {"the", "thin"}, {"in", "include"},
            {"in", "by"}, {"of", "by"}, {"docente", "doente"}, {"professor","aula"}, {"his", "him"},
            {"Bud", "and"}, {"rule", "ruler"}, {"governor", "governed"}, {"pay", "paying"},
            {"hamburger", "spiritual"}, {"reinterpretation","interpreted"}
        };

        o.println();
        o.println("------------------------------------------------------------------------------------------------------------------------------------------------");
        o.println("|     WORD A      |     WORD B     |           edit    dlex       dlexs        gmean       maxseq         (1)            (2)            (3)");
        o.println("------------------------------------------------------------------------------------------------------------------------------------------------");
        for (int i=0; i<A.length; i++) {
            String s1= new String(A[i][0]);  Word w1= new Word(s1);
            String s2= new String(A[i][1]);  Word w2= new Word(s2);
            int n1= s1.length();
            int n2= s2.length();
            int edist= Word.editDistance(s1, s2);
            double dlex=  Word.distlex(s1, s2);
            double dlexs= Word.distlexSuffix(s1, s2);
            double maxseq= Word.distSeqMax(s1, s2);
            o.printf(Locale.US,
               "%16s  %16s      --->   %3d   %.7f  %.7f   %10.7f   %.7f   %12.7f   %12.7f   %12.7f\n",
               s1, s2,
               edist, dlex, dlexs, Math.sqrt(dlex*dlexs),
               maxseq,
               edist*dlex/(0.01+maxseq),
               //edist/(0.01+maxseq)
               w1.costAlign(w2),
               w1.connectProb(w2)
            );
        }
        o.println("------------------------------------------------------------------------------------------------------------------------------------------------");
        o.print("LEGENDA:\n");
        o.print("    (1) - edit*dlex/maxseq\n");
        o.print("    (2) - edit/maxseq\n");
        o.print("    (3) - connectProb(Wa,Wb): Connection likelihood\n");
        o.print("\n");
        
        o.printf("Word(não).isWord() = %s\n\n", (new Word("não")).isWord() );
    }
}
