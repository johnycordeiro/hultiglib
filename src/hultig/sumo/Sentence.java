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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hultig.util.Toolkit;



/**
 *
 * <p>Represents a textual sentence using various schemes or interpretations.
 * For instance, a sentence may be interpreted as a sequence of characters or
 * as a sequence of words, represented by a linked list of words. This class
 * manages different kind of sentence representations.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br>
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro
 * @version 1.5
 */
@SuppressWarnings("static-access")
public class Sentence extends LinkedList<Word> implements Comparable
{
    private static Toolkit to;
    
    /**
     * The set of punctuation marks.
     */
    public static String pontuacao = ",.!\"?:;()[]{}<>'`´";

    /**
     * The set of text delimiters.
     */
    public static String parentise = "()[]{}<>";

    /**
     * Internal string representation of this sentence.
     */
    protected String stx;

    /**
     * This label defines a sentence meta-tag.
     */
    public String label;


    @Override
    public int compareTo(Object other) {
        if ( other instanceof Sentence ) {
            String sother= ((Sentence)other).toString();
            String sthis= toString();
            return sthis.compareTo(sother);
        }
        else
            return 1;
    }

    /**
     * Defines the codes for the sentence similarity functions.<br>
     * Description:
     * <pre>
     * SUMO ------- Sumo metric (default) (label: "sumo").
     * NGRAM ------ Simple Word N-Gram Overlap (label: "ngram").
     * XGRAM ------ N-Gram Overlap with suffix arrays (label: "xgram").
     * BLEU ------- The BLEU metric (label: "bleu").
     * EDIT ------- The sentence edit distance (label: "edit").
     * ENTROPY ---- The entropy metric (label: "entropy").
     * GAUSS ------ The Gauss metric (label: "gauss").
     * SUMOWSIZE -- The sumo accounting for word size (label: "sumowsize").
     * TRIGNOM ---- The trignometric similarity function (label: "trignom").
     * </pre>
     */
    public static enum Metric {
        SUMO, UNIGRAM, NGRAM, XGRAM, BLEU, EDIT, ENTROPY, GAUSS, SUMOWSIZE, TRIGNOM
    };

    public Metric metric= Metric.SUMO;
    
    /**
     * A sentence index, used in news clustering.
     * @since 2008-06-05
     */
    public int cod;


    /**
     * Default constructor.
     */
    public Sentence()
    {
        stx = null;
        metric= Metric.SUMO;
        label= null;
    }

    /**
     * Creates a new sentence from a given string.
     * @param s - The string containing a sentence.
     */
    public Sentence(String s)
    {
        this();
        set(s);
    }
    
    /**
     * Recreate this sentence from another one.
     * @param s The other sentence.
     */
    public void reload(Sentence s) {
        if ( s == null ) return;

        stx= "";
        super.clear();
        for (int i=0, n=s.size(); i<n; i++) {
            this.addWord(s.get(i));
        }

        metric= Metric.SUMO;
        this.label= s.label;
    }

    /**
     * Append a new word to this sentence.
     * @param w The word to be appended
     */
    public void addWord(Word w) {
        super.add(w);
        if ( stx == null ) {
            stx= ""+w;
            return;
        }
    
        if ( w.isRPUNCT() )
            stx= stx + w;
        else
            stx= stx + " " + w;
    }

    /**
     * Recreate this sentence from a given string.
     * Altered on November, 1th, 2017
     * @param s The indicated string.
     */
    public void set(String s) {
        List<String> list= tokenize(s);
        if (list == null)  { stx= null;  return; }
        for (String t : list) {
            super.add(new Word(t));
        }
        stx= s;
    }
    
    public static List<String> tokenize(String s) {
        if (s == null) return null;
        String[] tokens = s.trim().split("[  \t\n\r\f]+"); // <=== The second character is a "NO-BREAK SPACE".
        int a, b, n, ka = 0;
        ArrayList<String> as= new ArrayList<>();
        for (String t : tokens) {
            n = t.length();

            // Punctuation before word - ex: ["This]
            for (a = 0; a < n; a++) {
                char c = t.charAt(a);
                if (Character.isLetterOrDigit(c)) {
                    break;
                }
                as.add(""+c);
            }

            for (b = n - 1; b > a; b--) {
                char c = t.charAt(b);
                if (Character.isLetterOrDigit(c)) {
                    break;
                }
            }

            if (b + 1 - a > 0) {
                as.add(t.substring(a, b + 1));
            }

            // Punctuation after word - ex: [good."]
            for (int k = b + 1; k < n; k++) {
                as.add(""+t.charAt(k));
            }

            ka += n + 1;
        }
        return as;
    }
    
    /**
     * Recreate this sentence from a given string.
     * @param s The indicated string.
     */
    public void setOLD20171101(String s) {
        if (s == null)  {
            stx= null;
            return;
        }
        stx= s;
        StringTokenizer st = new StringTokenizer(stx.trim());

        int a, b, n, ka = 0;
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            n = word.length();

            // Punctuation before word - ex: ["This]
            for (a = 0; a < n; a++) {
                char c = word.charAt(a);
                if (pontuacao.indexOf(c) < 0) break;
                super.add(new Word(""+c));
            }

            for (b = n - 1; b > a; b--) {
                char c= word.charAt(b);
                if (pontuacao.indexOf(c) < 0) break;
            }

            if ( b+1-a > 0 )
                super.add(new Word(word.substring(a,b+1)));

            // Punctuation after word - ex: [good."]
            for (int k = b + 1; k < n; k++) {
                super.add(new Word(""+word.charAt(k)));
            }

            ka+= n+1;
        }
    }

    /**
     * Codifies this sentence according to a given previously
     * processed dictionary.
     * @param dic The indicated dictionary.
     */
    public void codify(CorpusIndex dic) {
        if ( dic == null )  return;

        for (int i=0; i<size(); i++) {
            get(i).setLexCod(dic.get(getWord(i).toLowerCase()));
        }
    }

    /**
     * Static method to codify a bunch of sentences.
     * @param vs Sentence[]
     */
    public static void codify(Sentence ... vs) {
        CorpusIndex dic= new CorpusIndex();
        dic.clearHash();
        for (int i=0, n=vs.length; i<n; i++) {
            dic.add(vs[i]);
        }
        dic.rebuild();

        for (int i=0, n=vs.length; i<n; i++) {
            vs[i].codify(dic);
        }
    }

    /**
     * This sentence string length.
     * @return The length value.
     */
    public int length() {
        if ( stx == null )  return 0;
        return stx.length();
    }

    /**
     * Gives the sentence word positioned at a given index.
     * @param index The index to read from.
     * @return The word read in the string form.
     */
    public String getWord(int index) {
        if ( index < 0  ||  index >= size() )  return null;
        return super.get(index).toString();
    }
    
    /**
     * Gives an array of strings, containing all the
     * words in this sentence.
     * @return The array of words.
     */
    public String[] getWords() {
        String[] vs= new String[size()];
        for (int i=0; i<vs.length; i++)  vs[i]= super.get(i).toString();
        return vs;
    }
    
    
    /**
     * Gives the original string size, used to create this sentence. 
     * @return The original sentence size
     */
    public int getOriginalSize() {
        if ( stx == null )
            return -1;
        else
            return stx.length();
    }
    

    /**
     * The POS tag, if defined, for a given word.
     * @param index The word position in the sentence.
     * @return The POS tag read or else the null value.
     */
    public String getTag(int index) {
        if (index < 0 || index >= size())return null;
        return get(index).getTag();
    }

    /**
     * Gives the array of lexical codes representing this
     * sentence. It assumes that the sentence was already
     * codified.
     * @return The array of lexical codes.
     */
    public int[] getCodes() {
        int[] v= new int[size()];
        for (int i=0; i<size(); i++)  v[i]= get(i).getLexCod();
	return v;
    }

    /**
     * Gives the array of POS tags for that sentence, assuming
     * it was already tagged.
     * @return The array of tags.
     */
    public String[] getTags() {
        String[] v= new String[size()];
        for (int i=0; i<size(); i++)  v[i]= get(i).getTag();
        return v;
    }

    /**
     * Verifies whether this sentence has been marked with a
     * CorpusIndex object.
     * @return The {@code true} value on success.
     */
    public boolean isCodefied() {
        if ( size() < 1 ) return false;
        return get(0).getLexCod() != -1;
    }

    /**
     * Tests if a given string is a punctuation mark.
     * @param s The string to be tested.
     * @return The test result.
     */
    public static boolean isPunct(String s) {
        if ( s == null || s.length() != 1 )  return false;
        return Sentence.pontuacao.contains(s);
    }
    
    /**
     * Test if a given string is a word.
     * @param s The string to be tested
     * @return True if the string is a word.
     */
    public static boolean isWord(String s) {
        if ( s == null || s.length() < 1 )  return false;
        if ( !Character.isLetter(s.charAt(0)) )  return false;
        if ( !Character.isLetter(s.charAt(s.length()-1)) )  return false;
        return s.indexOf(' ') < 0;
    }
    
    /**
     * Counts the number of words in this sentence.
     * @return The number of effective words found.
     * @author JPC (16:40:01 13/02/2008)
     */
    public int countNumWords() {
        int counter= 0;
        for (Word w : this) {
            if ( w.isWord() )  counter++;
        }
        return counter;
    }
    
    /**
     * Gives the index of a string in this sentence. The input string
     * will be compared with each sentence word and the position of
     * the first occurence will be given.
     * @param s The string to be scaned in this sentence.
     * @return The index found, or else the -1 value will be returned.
     * @author JPC (2008-02-13 17:01)
     */
    public int indexOf(String s) {
        return indexOf(s, 0);
    }
    
    /**
     * Gives the index of a string occurence within this sentence,
     * starting the search from a given position.
     * @param s The string to be scaned in this string.
     * @param from The starting index.
     * @return The index found, or else the -1 value will be returned.
     * @see #indexOf(java.lang.String) indexOf(String s)
     * @author JPC (2008-02-13 17:04)
     */
    public int indexOf(String s, int from) {
        for (int i=from; i<size(); i++) {
            String wi= this.getWord(i);
            if ( wi.equals(s) )  return i;
        }
        return -1;
    }


    /**
     * Gives a sub-sentence from this sentence, between
     * positions a and b, which should be valid. We can
     * have a &lt; b or b &lt; a. It is only required
     * that 0 &lt; a, b, &lt; "sentence length".
     * @param a One index.
     * @param b The other index.
     * @return The sub-sentence
     */
    public Sentence subs(int a, int b) {
        Sentence s= new Sentence();
        if ( a < 0 )  a= 0;
        if ( b > size() )  b= size();

        for (int i = a; i < b; i++) {
            s.add(this.get(i));
        }

        return s;
    }

    /**
     * Split a sentence based on the punctuations found.
     * @return The array of sentences obtained.
     */
    public Sentence[] splitPunct() {
        List<Sentence> vs= new ArrayList<>();
        int a=0;
        for (int k = 0; k < size(); k++) {
            Word w= get(k);
            if (  w.isRPUNCT() ) {
                if ( k > a )  vs.add(subs(a,k+1));
                a= k+1;
            }
        }

        Sentence[] v= new Sentence[vs.size()];
        v= vs.toArray(v);
        return v;
    }

    /**
     * This method applies the <i>Edit Distance</i> (ED) metric to compare
     * this sentence with another one. The basic comparition unit is
     * the word, not the character as in conventional ED.
     * @param other The other sentence.
     * @return The calculated distance.
     */
    public int dsLevenshtein(Sentence other) {
        if ( other == null ) return -1;

        int d[][];   //-------------> matrix;
        int n= other.size();
        int m= this.size();
        int cost;

        if ( n == 0 ) return m;
        if ( m == 0 ) return n;

        // step 2
        d= new int[n+1][m+1];
        for(int i=0; i<=n; i++)  d[i][0]= i;
        for(int j=0; j<=m; j++)  d[0][j]= j;

        // step 3
        for(int i=1; i<=n; i++) {
            Word wi= other.get(i-1);
            // step 4
            for(int j=1; j<=m; j++) {
                Word wj= this.get(j-1);
                // step 5
                if ( wi.getLexCod() >= 0  &&  wj.getLexCod() >= 0 )
                    cost= (wi.getLexCod() == wj.getLexCod()) ? 0 : 1;
                else
                    cost = wi.toString().equals(wj.toString()) ? 0 : 1;
                // step 6
                d[i][j]= Toolkit.minimum(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1]+cost);
            }
        }

        // step 7
        return d[n][m];
    }

    /**
     * Gives the array of sub-codes corresponding to a
     * sub-sentence of this sentences. It assumes that
     * the sentence was already some how been codified,
     * for example through a dictionary ("CorpusIndex")
     * previously computed.
     * @param start
     * @param end
     * @return The sub-array of codes.
     */
    public int[] subcodes(int start, int end) {
        if ( end > this.size() )
            end= this.size();
        if ( start > end || start > this.size()-1 )
            return null;

        int N= end-start;
        int[] vsub= new int[N];
        for (int k=0; k<N; k++) {
            vsub[k] = get(start + k).getLexCod();
        }
        return vsub;
    }


    /**
     * Counts the number of occurrences of a sub-array inside
     * another, presumably longer, array. This method is 
     * used for simple n-gram match counting.
     *
     * @param vsub The sub-array.
     * @param v The longer array.
     * @return The number of occurrences.
     */
    public static int match(int[] vsub, int[] v)
    {
        int n= vsub.length;
        int m= v.length;
        if ( n > m )  return 0;

        int counter= 0;
        for (int i=0; i<m-n+1; i++) {
            int j;
            for (j = 0; j < n; j++)
                if (vsub[j] != v[i + j]) break;
            if ( j == n ) {
                counter++;
            }
        }

        return counter;
    }

    /**
     * Gives a list with all n-grams contained in a given
     * sentence. Each n-gram is represented by its
     * sentence sequence codes.
     * @param N The n-gram size.
     * @param S The sentence from which to obtain the n-grams
     * @return LinkedList The list of n-grams.
     */
    private static LinkedList<int[]> ngramsList(int N, Sentence S)
    {
        int[] vcodes= S.getCodes();
        LinkedList<int[]> list= new LinkedList<>();
        for (int k=0; k<S.size()-N+1; k++) {
            int[] v= new int[N];
            for (int j=0; j<N; j++) v[j]= vcodes[k+j];
            list.add(v);
        }

        return list;
    }

    /**
     * Verifies whether two arrays are equal.
     * @param u The first array.
     * @param v The second array.
     * @return The test result
     */
    public static boolean equalArrays(int[] u, int[] v) {
        if ( u == null && v == null )  return true;
        if ( u == null || v == null )  return false;
        if ( u.length != v.length )  return false;

        for (int k=0; k<u.length; k++)
            if ( u[k] != v[k] )  return false;

        return true;
    }

    /**
     * Counts the number of n-grams match between two sentences.
     * @param N The n-gram size.
     * @param sa The first sentence.
     * @param sb The second sentence.
     * @return The number of n-gram matches.
     */
    public static int ctMatchNGram(int N, Sentence sa, Sentence sb) {
        int Na= sa.size();
        int Nb= sb.size();
        //System.out.println("N: "+N);
        if ( N > Math.min(Na,Nb) )  return 0;
        LinkedList<int[]> La= ngramsList(N,sa);
        LinkedList<int[]> Lb= ngramsList(N,sb);

        int contador= 0;
        for (int i=0; i<La.size(); i++) {
            int[] vna= La.get(i);
            for (int j=0; j<Lb.size(); j++) {
                int[] vnb= Lb.get(j);
                if ( equalArrays(vna,vnb) ) {
                    Lb.remove(j);
                    contador++;
                    break;
                }
            }
        }

        return contador;
    }

    /**
     * Counts the number of exclusive n-gram matches, between
     * two sentences.
     * @param N The n-gram size.
     * @param sa The first sentence.
     * @param sb The other sentence.
     * @return The number of matches.
     */
    public static int countMatchNGram(int N, Sentence sa, Sentence sb) {
        int counter= 0;

        if ( sa.size() > sb.size() ) {
            Sentence s= sa;
            sa= sb;
            sb= s;
        }

        if ( N > 0 )  return ctMatchNGram(N,sa,sb);

        int[] vb= sb.getCodes();
        for (int i=0; i<sa.size()-N+1; i++) {
            //sa.print(i, i+N);
            int[] vnga= sa.subcodes(i,i+N);
            //System.out.println("------------------------");
            int m= match(vnga, vb);
            counter+= m; //(m>0? 1 : 0);
        }
        return counter;
    }

    /**
     * Returns the set of links between two sentences: A = {a1,a2,...an}
     * where ak = (k1, k2) is an integer pair representing the link
     * between word in position k1, in one sentence, and k2 the k2-th
     * word in the other sentence.
     * @since 2007-03-23
     * @param other The other sentence.
     * @return An array of integer pairs representing the links.
     */
    public int[][] readLinks(Sentence other) {
        return readLinks(this.getCodes(), other.getCodes());
    }

    /**
     * Returns the set of links between two sentences: A = {a1,a2,...an}
     * where ak = (k1, k2) is an integer pair representing the link
     * between word in position k1, in one sentence, and k2 the k2-th
     * word in the other sentence. In this method the sentences are
     * represented by their array codes.
     * @since 2007-03-23
     * @param va The first sentence array.
     * @param vb The second sentence array.
     * @return An array of integer pairs representing the links.
     */
    public static int[][] readLinks(int[] va, int[] vb) {
        if ( va == null || vb == null || va[0] < 0 || vb[0] < 0 ) return null;

        List<int[]> v= new ArrayList<>();
        for (int i=0; i<va.length; i++) {
            for (int j=0; j<vb.length; j++) {
                if ( va[i] == vb[j] ) {
                    int[] link= new int[2];
                    link[0]= i; //<--- va position
                    link[1]= j; //<--- vb position
                    v.add(link);
                    vb[j]= -1*vb[j];
                    break;
                }
            }
        }

        int[][] A= new int[2][v.size()];
        for (int j=0; j<A[0].length; j++) {
            int[] u= v.get(j);
            A[0][j]= u[0];
            A[1][j]= u[1];
        }

        return A;
    }


    public int countMatch(String regex) {
        int k=0;
        Pattern p= Pattern.compile(regex);
        for (Word w : this) {
            Matcher m= p.matcher(w.toString());
            if ( m.matches() )  k++;
        }
        return k;
    }

    /**
     * Counts the number of words from this sentence that do
     * not match a given regular expression.
     * @param regex The indicated regular expression.
     * @return The number of condition matches.
     */
    public int countNotMatch(String regex) {
        int k=0;
        Pattern p= Pattern.compile(regex);
        for (Word w : this) {
            Matcher m= p.matcher(w.toString());
            if ( !m.matches() )  k++;
        }
        return k;
    }

    /**
     * Counts te number of link intersections existing between the two sentences.
     * This method is fundamental for making the runtime decision of choosing
     * the alignment algorithm: <i>Smith Waterman</i> or <i>Needleman Wunsch</i>.
     * This id better explained in:
     * <p>
     * Cordeiro, J.P., Dias, G.Cleuziou G. (2007). Biology Based Alignments of
     * Paraphrases for Sentence Compression. In Proceedings of the Workshop on
     * Textual Entailment and Paraphrasing (ACL-PASCAL / ACL2007). Prague,
     * Czech Republic.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/W07-1429.pdf">link</a>].
     * </p>
     * @since 2007-03-23
     * @param sa The first sentence.
     * @param sb The second sentence.
     * @return The number of link intersections.
     */
    public static int countIntersectLinks(Sentence sa, Sentence sb) {
        if ( sa == null || sb == null || sa.length() < 1 || sb.length() < 1 ) return -1;

        int[] va= sa.getCodes();
        int[] vb= sb.getCodes();

        int[][] A= readLinks(va, vb);
        if ( A == null) {
            Text t= new Text();
            t.add(sa);
            t.add(sb);
            t.codify();
            va= t.get(0).getCodes();
            vb= t.get(1).getCodes();
            A= readLinks(va, vb);
        }
        if ( A == null )  return -101;

        int counter= 0;
        for (int i=0; i<A[0].length-1; i++) {
            for (int j = i+1; j < A[0].length; j++) {
                //compare ai =<x,a> with aj=<y,b>
                int x= A[0][i], y= A[0][j];
                int a= A[1][i], b= A[1][j];
                if ( (x-y)*(a-b) < 0 ) {
                    counter++;
                }
            }
        }
        return counter;
    }


    /**
     * Percentage of link intersections existing between the two sentences. This method is
     * related with the "countIntersectLinks(Sentence sa, Sentence sb)" method. The only
     * difference is normalization.
     * @since 2007-03-23
     * @param sa The first sentence
     * @param sb The second sentence
     * @return A value in the [0,1] interval.
     */
    public static double countNormIntersectLinks(Sentence sa, Sentence sb) {
        if ( sa == null || sb == null || sa.length() < 1 || sb.length() < 1 ) return -1;

        int[] va= sa.getCodes();
        int[] vb= sb.getCodes();

        int[][] A= readLinks(va, vb);
        if ( A == null) {
            Text t= new Text();
            t.add(sa);
            t.add(sb);
            t.codify();
            va= t.get(0).getCodes();
            vb= t.get(1).getCodes();
            A= readLinks(va, vb);
        }
        if ( A == null )  return -101;

        int counter= 0;
        for (int i=0; i<A[0].length-1; i++) {
            for (int j = i+1; j < A[0].length; j++) {
                int x= A[0][i], y= A[0][j];
                int a= A[1][i], b= A[1][j];
                if ( (x-y)*(a-b) < 0 ) counter++;
            }
        }

        int n= A[0].length;

        return 2.0*counter/(n*(n-1));
    }

    /**
     * Computes the BLEU metric between two sentences. A sentence
     * proximity value.
     * @param sa The first sentence.
     * @param sb The second sentence.
     * @return A value in the [0,1] interval.
     */
    public static double dsBLEU(Sentence sa, Sentence sb) {
        int N= 4;
        double sum= 0.0;
        double xa= (double)sa.size();
        double xb= (double)sb.size();
        for (int n=1; n<=N; n++) {
            int m= countMatchNGram(n,sa,sb);
            if ( m == 0 )  continue;
            double na= xa-n+1;
            double nb= xb-n+1;
            double pn= m/(Math.min(na,nb)+0.0001);
            sum += Math.log(pn);
        }
        if ( sum == 0.0 )  return 0.0;

        double BP = Math.exp( 1.0 - Math.min(xa,xb) / Math.max(xa,xb) );
        return BP*Math.exp(1/N * sum);
    }

    /**
     * Computes the simple n-gram overlap between two sentences, with 4
     * as the maximum n-gram counted.
     * @param sa The first sentence.
     * @param sb The second sentence.
     * @return A value in the [0,1] interval.
     */
    public static double dsNgram(Sentence sa, Sentence sb) {
        return dsNgram(4,sa,sb);
    }

    /**
     * Computes the simple n-gram overlap between two sentences, considering
     * a maximum number of n-grams.
     * @param N The maximum number of n-grams.
     * @param sa The first sentence.
     * @param sb The second sentence.
     * @return A value in the [0,1] interval.
     */
    public static double dsNgram(int N, Sentence sa, Sentence sb) {
        if ( N < 0 )  N= -1*N;
        else if ( N == 0 )  N= 1;

        ensureCodification(sa,sb);

        double sum= 0.0;
        for (int n=1; n<=N; n++) {
            int m= countMatchNGram(n,sa,sb);
            double na= (double)sa.size()-n+1; //max n-grams in sa
            double nb= (double)sb.size()-n+1; //max n-grams in sb
            double pn= m/(Math.min(na,nb)+0.0001);
            sum+= 1.0/N * pn;
        }
        return sum;
    }
    
    
    public static double dsUnigram(Sentence sa, Sentence sb) {
        int m= countMatchNGram(1,sa,sb);
        double pn= (double)m/(Math.min(sa.size(),sb.size())+0.0001);
        return pn;
    }
    
    
    public static double dsUnigramX(Sentence sa, Sentence sb) {
        int[] u= sa.getCodes();
        int[] v= sb.getCodes();
        /*
        int n, m;
        if (u.length > v.length) {
            n = v.length;
            m = u.length;
        }
        else {
            n= u.length;
            m= v.length;
        }
        */
        //compute word sizes.
        int[] vsz= new int[v.length];
        for (int i = 0; i < vsz.length; i++)  vsz[i]= sb.get(i).toString().length();

        //count the number of matches
        int NLZ= 0;
        int MZ= sa.toString().length();
        int NZ= sb.toString().length();
        if (NZ > MZ ) {int aux=MZ; MZ=NZ; NZ=aux;}
        for (int i=0; i<u.length; i++)
            for (int j=0; j<v.length; j++)
                if ( v[j] >= 0  &&  u[i] >= 0  &&  Math.abs(u[i]-v[j]) == 0 )  {
                    NLZ+= vsz[j]; //word size.
                    v[j]= -1*v[j];
                    u[i]= -1*u[i];
                    break;
                }

        //procced to calculations
        double pm= (double) NLZ/MZ;
        double pn= (double) NLZ/NZ;
        double a, b;
        if ( MZ > NZ ) {
            a= 2.0/3;
            b= 1.0/3;
        }
        else {
            b= 2.0/3;
            a= 1.0/3;            
        }
       
        return Math.exp(a*Math.log(pm) + b*Math.log(pn));
    }
    

    /**
     * A metric for calculating sentence proximity, based on suffix
     * arrays comparisons of n-grams, as defined by Church and
     * Yamamoto.
     *
     *
     * @since 2006-03-28
     * @param sa The first sentence.
     * @param sb The second sentence.
     * @return A value in the [0,1] interval.
     */
    public static double dsuffixArrays(Sentence sa, Sentence sb) {
        ensureCodification(sa,sb);
        int[] va= sa.getCodes();  int na= va.length;
        int[] vb= sb.getCodes();  int nb= vb.length;
        int[] v= new int[na+nb+1];
        int j=0;
        for (int i=0; i<na; i++)  v[j++]= va[i];
        v[j++]= -1001; //end f sentence code.
        for (int i=0; i<nb; i++)  v[j++]= vb[i];

        NGramsV ngs= new NGramsV(v);  ngs.compute();

        //count n-grams overlapping.
        int[] match= new int[ngs.maxN()];
        for(int i=0; i<match.length; i++)  match[i]= 0;

        Enumeration<String> es= ngs.hstng.keys();
        while ( es.hasMoreElements() ) {
            String ngkey= es.nextElement();
            Integer Fs= ngs.hstng.get(ngkey);
            int n= Integer.parseInt( ngkey.substring(0, ngkey.indexOf('<')) );

            /**
             * Estimativa para o cálculo do DF
             * se DF >= 2 então considera-se que
             * houve overlapping.
             */
            if ( Fs < 2 ) continue;
            match[n-1]++;
        }

        double maxp= 0.0;
        int N= Math.min(4, match.length);
        
        for (int n=1; n<=N; n++) {
            if ( match[n-1] == 0 )  continue;

            int ng= Math.min(na-n+1,nb-n+1);
            if ( ng == 0 )  continue;
            
            double pn= (double) match[n-1] / ng;
            if ( pn > maxp )  maxp= pn;
        }

        return maxp;
    }

    /**
     * Counts the number of exclusive links between two arrays of integers.
     * This method was created to serve the "<code>dsumo</code>" funtion,
     * and their different versions. Usually, it is expected that the arrays
     * contain the word index codes of two sentences.
     * @param u One array of codes.
     * @param v The other array of codes.
     * @return The number of exclusive links.
     */
    private static int numExclusiveLinks(int[] u, int[] v) {
        int numExLk= 0;
        for (int i=0; i<u.length; i++)
            for (int j=0; j<v.length; j++)
                if ( v[j] >= 0  &&  u[i] >= 0  &&  Math.abs(u[i]-v[j]) == 0 )  {
                    numExLk++;
                    v[j]= -1*v[j];
                    u[i]= -1*u[i];
                    break;
                }
        return numExLk;
    }


    /**
     * The "sumo metric" for calculating the similarity between two sentences.
     * As presented in:
     * <p>
     * Cordeiro, J.P., Dias, G.   Brazdil, P. (2007). Learning Paraphrases
     * from WNS Corpora. 20th International FLAIRS Conference. AAAI Press.
     * Key West, Florida, USA.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/Flairs07-040.pdf">link</a>]
     * </p>
     * @since 2006-03-25
     * @param other The other sentence to compare with.
     * @return A value in the [0,1] interval.
     */
    public double dsumo(Sentence other) {
        if ( other == null || other.length() < 1 || length() < 1 )  return 0.0;

        ensureCodification(this,other);

        int NL=0;
        int n=size(), m= other.size();

        if (get(0).getLexCod() >= 0) {
            int[] u = this.getCodes();
            int[] v = other.getCodes();
            NL = numExclusiveLinks(u, v);
            if (NL == 0) return 0.0;
        }
        else { //==> Sentence not codified.
            String[] va=  this.getWords();
            String[] vb= other.getWords();
            for (String s : va) {
                for (int j = 0; j < vb.length; j++) {
                    if (vb[j] != null && s.equals(vb[j])) {
                        //System.out.printf("EQ(%d,%d) = %s\n", i,j,va[i]);
                        NL++;
                        vb[j]= null;
                        break;
                    }
                }
            }
        }
        

        //proceed to final calculations
        if ( m < n ) {int aux=m; m=n; n=aux;}
        double pm= (double)NL/m;
        double pn= (double) NL/n;

        double alfa= 0.5;
        double beta= 1.0 - alfa;
        double similarity= - alfa*Toolkit.log2(pm) - beta*Toolkit.log2(pn);
        //System.out.printf("\nNL: %d   m:%d   n: %d   pm: %f   pn: %f   sim: %f\n", NL, m, n, pm, pn, similarity);
        if ( similarity > 1.0 )  similarity= 1.0/Math.exp(3*similarity);

        return similarity == -0.0 ? 0.0 : similarity;
    }

    /**
     * The "sumo metric" for calculating the similarity between two sentences.
     * represented by their arrays of codes.
     * As presented in:
     * <p>
     * Cordeiro, J.P., Dias, G.   Brazdil, P. (2007). Learning Paraphrases
     * from WNS Corpora. 20th International FLAIRS Conference. AAAI Press.
     * Key West, Florida, USA.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/Flairs07-040.pdf">link</a>]
     * </p>
     * @since 2006-05-04
     * @param u The first array of codes.
     * @param v The second array of codes.
     * @return The similarity value in the [0,1] interval.
     */
    public static double dsumo(int[] u, int[] v) {
        if ( u == null || v == null )  return 0.0;

        int n, m;
        if (u.length > v.length) {
            n = v.length;
            m = u.length;
        }
        else {
            n= u.length;
            m= v.length;
        }

        //count the number of matches
        int NL= numExclusiveLinks(u, v);
        if ( NL == 0 )  return 0.0;

        //procced to calculations
        double pm= (double) NL/m;
        double pn= (double) NL/n;
        //pn= (double) WNL/M;

        //if ( pm < 0.5  &&  pn < 0.5 )  return 0.0;

        double alfa= 0.5; //alfa= (double) WNL/M;
        double beta= 1.0 - alfa;
        //alfa=1.0;

        //calculate pair strength.
        double interest= - alfa*Toolkit.log2(pm) - beta*Toolkit.log2(pn);
        if ( interest > 1.0 )  interest= 1.0/Math.exp(3*interest);

        return interest == -0.0 ? 0.0 : interest;
    }

    /**
     * A different version of the {@link #dsumo(Sentence other) sumo} function
     * for calculating sentence similarity between two sentences. The main
     * difference consists in counting differently the lexical exclusive
     * links between the two sentences. The "weight" of each link directly
     * depends from the connected word sizes.
     *
     * @param other The other sentence to compare with.
     * @return double A value in the [0,1] interval.
     */
    public double dsumoWSize(Sentence other) {
        if ( other == null )  return 0.0;

        ensureCodification(this,other);

        int[] u= this.getCodes();
        int[] v= other.getCodes();
        int n, m;
        if (u.length > v.length) {
            n = v.length;
            m = u.length;
        }
        else {
            n= u.length;
            m= v.length;
        }

        //compute word sizes.
        int[] vsz= new int[v.length];
        for (int i = 0; i < vsz.length; i++)  vsz[i]= other.get(i).toString().length();

        //count the number of matches
        int NLZ= 0;
        int MZ= this.toString().length();
        int NZ= other.toString().length();
        if (NZ > MZ ) {int aux=MZ; MZ=NZ; NZ=aux;}
        for (int i=0; i<u.length; i++)
            for (int j=0; j<v.length; j++)
                if ( v[j] >= 0  &&  u[i] >= 0  &&  Math.abs(u[i]-v[j]) == 0 )  {
                    NLZ+= vsz[j]; //word size.
                    v[j]= -1*v[j];
                    u[i]= -1*u[i];
                    break;
                }

        //procced to calculations
        double pm= (double) NLZ/MZ;
        double pn= (double) NLZ/NZ;

        double alfa= 0.5; //alfa= (double) WNL/M;
        double beta= 1.0 - alfa;
        //alfa=1.0;

        //calculate pair strength.
        double interest= - alfa*Toolkit.log2(pm) - beta*Toolkit.log2(pn);
        if ( interest > 1.0 )  interest= 1.0/Math.exp(3*interest);

        return interest == -0.0 ? 0.0 : interest;
    }



    /**
     * The "entropy metric" for calculating the similarity
     * between two sentences.
     * <p>
     * Cordeiro, J.P., Dias, G.   Cleuziou G.   Brazdil P.
     * (2007). New Functions for Unsupervised Asymmetrical
     * Paraphrase Detection. In Journal of Software.
     * Volume:2, Issue:4, Page(s): 12-23. Academy Publisher.
     * Finland. ISSN: 1796-217X. October 2007.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/jsw02041223.pdf">link</a>]
     * </p>
     * <p>Date: 2007-06-18</p>
     * @param other The other sentence to compare with.
     * @return A value in the [0,1] interval.
     */
    public double dsEntropy(Sentence other) {
        ensureCodification(this,other);
        double[] vpr= contarPrecRecall(other);
        if ( vpr == null )  return 0.0;

        double px= vpr[0];
        double py= vpr[1];

        double p= px*py;
        double q= 1.0-p;
        if ( p <= 0.0 || q <= 0.0 ) return 0.0;

        double interest= -p*Toolkit.log2(p) - q*Toolkit.log2(q); // <--- Entropy{px*py}
        return interest;
    }

    /**
     * A simple version of the gaussian similarity between twon sentences.
     * In this particular case the gaussian parameters are as follows: a=1,
     * b=0.5, and c=0.3.
     * <p>
     * Cordeiro, J.P., Dias, G.   Cleuziou G.   Brazdil P.
     * (2007). New Functions for Unsupervised Asymmetrical
     * Paraphrase Detection. In Journal of Software.
     * Volume:2, Issue:4, Page(s): 12-23. Academy Publisher.
     * Finland. ISSN: 1796-217X. October 2007.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/jsw02041223.pdf">link</a>]
     * </p>
     * @param other The other sentence.
     * @return A value in the [y0,1] interval, where y0 corresponds to x = 0.0 (no overlapping),
     * which means that y0 = exp{-0.5^2/(2*0.3^2)} = 0.29435
     */
    public double dgauss(Sentence other) {
        if ( other == null )  return 0.0;

        ensureCodification(this,other);

        int[] u= this.getCodes();
        int[] v= other.getCodes();
        int n, m;
        if (u.length > v.length) {
            n = v.length;
            m = u.length;
        }
        else {
            n= u.length;
            m= v.length;
        }
        if ( m == 0 || n == 0 ) return 0.0;

        //count the number of matches
        int NL= 0;
        for (int i=0; i<u.length; i++)
            for (int j=0; j<v.length; j++)
                if ( v[j] >= 0  &&  u[i] >= 0  &&  Math.abs(u[i]-v[j]) == 0 )  {
                    NL++;
                    v[j]= -1*v[j];
                    u[i]= -1*u[i];
                    break;
                }

        //procced to calculations

        double p= (double)NL/n;  //------> frase menor
        double r= (double)NL/m;  //------> frase maior
        double x= p*r;

        return Math.exp(-1.0*Toolkit.sqr(x-0.5)/(2.0*Toolkit.sqr(0.30)) );
    }


    /**
     * The gaussian similarity between two sentences. Like the gaussian function
     * family, it depends from four parameters, which here have the meaning
     * listed bellow. This function was also presented in the following article:
     * <p>
     * Cordeiro, J.P., Dias, G.   Cleuziou G.   Brazdil P.
     * (2007). New Functions for Unsupervised Asymmetrical
     * Paraphrase Detection. In Journal of Software.
     * Volume:2, Issue:4, Page(s): 12-23. Academy Publisher.
     * Finland. ISSN: 1796-217X. October 2007.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/jsw02041223.pdf">link</a>]
     * </p>
     * @param other The other sentence.
     * @param p0 The expected precision of sentences token match.
     * @param r0 The expected recall of sentences token match.
     * @param sp0 The expected precision variance
     * @param sr0 The expected recall variance.
     * @return A value in the [0,1] interval.
     */
    public double dgauss(Sentence other, double p0, double r0, double sp0, double sr0) {
        if ( other == null )  return 0.0;

        ensureCodification(this,other);

        int[] u= this.getCodes();
        int[] v= other.getCodes();
        int n, m;
        if (u.length > v.length) {
            n = v.length;
            m = u.length;
        }
        else {
            n= u.length;
            m= v.length;
        }

        //count the number of matches
        int NL= 0;
        for (int i=0; i<u.length; i++)
            for (int j=0; j<v.length; j++)
                if ( v[j] >= 0  &&  u[i] >= 0  &&  Math.abs(u[i]-v[j]) == 0 )  {
                    NL++;
                    v[j]= -1*v[j];
                    u[i]= -1*u[i];
                    break;
                }

        //reset link marks
        for (int i=0; i<u.length; i++) if ( u[i] < 0 )  u[i]= -1*u[i];
        for (int i=0; i<v.length; i++) if ( v[i] < 0 )  v[i]= -1*v[i];

        //procced to calculations
        double p= (double)NL/n;  //------> frase menor
        double r= (double)NL/m;  //------> frase maior
        
        //double x= p*r;
        Toolkit t = new Toolkit();
        return Math.exp(-1.0*(t.sqr(p-p0)/(2.0*t.sqr(sp0)) + t.sqr(r-r0)/(2.0*t.sqr(sr0))));
        //return Math.exp(-1.0*sqr(x-0.5)/(2.0*sqr(0.30)) );
    }

    /**
     * The parabolic sentence similarity metric. As presented in the following
     * article:
     * <p>
     * Cordeiro, J.P., Dias, G.   Cleuziou G.   Brazdil P.
     * (2007). New Functions for Unsupervised Asymmetrical
     * Paraphrase Detection. In Journal of Software.
     * Volume:2, Issue:4, Page(s): 12-23. Academy Publisher.
     * Finland. ISSN: 1796-217X. October 2007.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/jsw02041223.pdf">link</a>]
     * </p>
     * @since 2007-06-18
     * @param other The other sentence.
     * @return A value in the [0,1] interval.
     */
    public double dParabolic(Sentence other) {
        ensureCodification(this,other);

        double[] vpr= contarPrecRecall(other);
        if ( vpr == null )  return 0.0;

        double px= vpr[0];
        double py= vpr[1];

        double p= px*py;
        double q= 1.0-p;

        double interest= 4*p-4*p*p;

        return interest;
    }

    /**
     * The linear similarity metric between two sentences. It is based
     * on the triangular function in the [0,1] interval taking as
     * arguments the precision and recall of sentence token overlapping
     * between the two sentences.
     * 
     * @since 2007-06-18
     * @param other The other sentence
     * @return A value in the [0,1] interval.
     */
    public double dLinear(Sentence other) {
        ensureCodification(this,other);

        double[] vpr= contarPrecRecall(other);
        if ( vpr == null )  return 0.0;

        double px= vpr[0];
        double py= vpr[1];

        double x= px*py;

        double interest= 1.0 - Math.abs(2.0*(x-0.5));

        return interest;
    }


    private double[] contarPrecRecall(Sentence s) {
        if (s == null) return null;

        int[] u = this.getCodes();
        int[] v = s.getCodes();
        int n, m;
        if (u.length > v.length) {
            n = v.length;
            m = u.length;
        }
        else {
            n = u.length;
            m = v.length;
        }
        if ( m == 0 || n == 0 )  return null;

        //count the number of matches
        int NL = 0;
        for (int i = 0; i < u.length; i++)
            for (int j = 0; j < v.length; j++)
                if (v[j] >= 0 && u[i] >= 0 && Math.abs(u[i] - v[j]) == 0) {
                    NL++;
                    v[j] = -1 * v[j];
                    u[i] = -1 * u[i];
                    break;
                }

        //reset link marks
        for (int i = 0; i < u.length; i++)if (u[i] < 0) u[i] = -1 * u[i];
        for (int i = 0; i < v.length; i++)if (v[i] < 0) v[i] = -1 * v[i];

        //procced to calculations
        double[] vpr= new double[5]; //-----> protocol: <precision, recall>
        vpr[0] = (double) NL / n; //------> frase menor (precision ou Lx)
        vpr[1] = (double) NL / m; //------> frase maior (recall ou Ly)
        vpr[2] = (double) n;
        vpr[3] = (double) m;
        vpr[4] = (double) NL;

        return vpr;
    }

    /**
     * The trignometric function for calculating the similarity between
     * two sentences. It is based on the sin function. Presented in the
     * following article:
     * <p>
     * Cordeiro, J.P., Dias, G.   Cleuziou G.   Brazdil P.
     * (2007). New Functions for Unsupervised Asymmetrical
     * Paraphrase Detection. In Journal of Software.
     * Volume:2, Issue:4, Page(s): 12-23. Academy Publisher.
     * Finland. ISSN: 1796-217X. October 2007.
     * [<a href="http://www.di.ubi.pt/~jpaulo/competence/publications/jsw02041223.pdf">link</a>]
     * </p>
     * @param other
     * @return A value in the [0,1] interval.
     */
    public double dSin(Sentence other) {
        ensureCodification(this,other);
       
        double[] vpr= this.contarPrecRecall(other);
        if ( vpr == null ) return 0.0;

        double px= vpr[0];
        double py= vpr[1];

        double p= px*py;

        double interest= Math.sin(p*Math.PI);
        return interest;
    }

    /**
     * Ensures that a given set of sentences is codified, which means
     * that their words have been marked with a word indexer (a
     * {@code CorpusIndex} object). If not, the set of sentences will
     * be marked with a new and specific word indexer, constructed
     * only from the set of sentences receive as parameter.
     * @param sentences The set
     */
    public static void ensureCodification(Sentence... sentences) {
        boolean ok= true;
        for (Sentence s : sentences) {
            if ( !s.isCodefied() ) {
                ok= false;
                break;
            }
        }

        if ( !ok ) {
            CorpusIndex.codifyOnFly(sentences);
        }
    }

    /**
     * The minumum lexical distance of a word to any word in
     * this sentence.
     * @param w The input word.
     * @return The minimum distance.
     */
    public double distlex(Word w) {
        if ( w == null ) return 1000.0; // +00

        double dmin= 1000.0; // +00
        for (Word u : this) {
            double d= u.distlex(w, 3.0f);
            if ( d < dmin ) dmin= d;
        }
        return dmin;
    }

    /**
     * The proportion of effective words contained in this sentence.
     * @return A value in the [0,1] interval.
     */
    public double fracNumWords() {
        if ( isEmpty() )  return 0.0;

        int num= 0;
        for (Word w : this) {
            if (w.isNumWord()) {
                num++;
            }
        }

        return (double) num/size();
    }

    /**
     * Defines which should be the default similarity function to be
     * used in the sentence similarity computation.
     * @see Sentence#metric The defined metric codes.
     * @param smetric Contains the name of the similarity function.
     * The possible values are: ngram, xgram, bleu, edit, entropy,or sumo.
     */
    public void setMetric(String smetric) {
        switch (smetric) {
            case "sumo":
                metric= Metric.SUMO;
                break;
            case "ngram":
                metric= Metric.NGRAM;
                break;
            case "xgram":
                metric= Metric.XGRAM;
                break;
            case "bleu":
                metric= Metric.BLEU;
                break;
            case "edit":
                metric= Metric.EDIT;
                break;
            case "entropy":
                metric= Metric.ENTROPY;
                break;
            case "gauss":
                metric= Metric.GAUSS;
                break;
            case "sumowsize":
                metric= Metric.SUMOWSIZE;
                break;
            case "trignom":
                metric= Metric.TRIGNOM;
                break;
            default:
                metric= Metric.SUMO;
                break;
        }
    }

    
    public static double similarity(Sentence sa, Sentence sb, Sentence.Metric metric) {
        sa.metric= metric;
        sb.metric= metric;
        return sa.similarity(sb);
    }
    
    
    /**
     * Compute the similarity metric between two sentences. The "sumo metric"
     * is the default similarity function used.
     * @see Sentence#metric The defined metric codes.
     * @param other The other sentence.
     * @return The similarity value [0,1], according to some
     * specified metric.
     */
    public double similarity(Sentence other) {
        if ( metric == Metric.NGRAM ) 
            return Sentence.dsNgram(this,other);
        if ( metric == Metric.XGRAM ) 
            return Sentence.dsuffixArrays(this,other);
        if ( metric == Metric.UNIGRAM )
            return dsUnigramX(this, other);
        if ( metric == Metric.BLEU ) 
            return Sentence.dsBLEU(this,other);
        if ( metric == Metric.EDIT ) {
            // normalized similarity.
            int m = Math.max(this.size(), other.size());
            return 1.0 - (double) this.dsLevenshtein(other) / m;
        }
        if ( metric == Metric.ENTROPY ) 
            return this.dsEntropy(other);
        if ( metric == Metric.GAUSS ) 
            return this.dgauss(other, 0.5, 0.8, 0.25, 0.1);
        if ( metric == Metric.SUMOWSIZE ) 
            return this.dsumoWSize(other);

        return Sentence.dsumo(this.getCodes(), other.getCodes());
    }

    /**
     * Calculates the similarity between two sentences using a
     * given similarity function.
     *
     * @since 2009-11-17
     * @param other The other sentence.
     * @param metric The name of the similarity function.
     * @return A value in the [0,1] interval.
     */
    public double similarity(Sentence other, String metric) {
        setMetric(metric);
        return this.similarity(other);
    }

    /**
     * Outputs the words of this sentence, between two positions.
     * @param a The fist position.
     * @param b The second position.
     */
    public void print(int a, int b) {
        for (int i=a; i<b; i++)
        {
            System.out.print(getWord(i)+" ");
        }
        System.out.println();
    }

    /**
     * Outputs the string representing this sentence.
     */
    public void print() {
        System.out.println(stx);
    }

    /**
     * Outputs all words from this sentence, one word
     * per line.
     */
    public void println() {
        for (int i=0; i<size(); i++)
            System.out.printf("%s\n", getWord(i));
    }

    /**
     * Converts every word to lower case and transforms their
     * {@link CorpusIndex} codes to -1. Thus, any lexical
     * codification will be eliminated.
     */
    public void toLowerCase() {
        for (Word w : this) {
            w.toLowerCase();
            w.setLexCod(-1);
        }
    }

    /**
     * Converts every word to lower case and redefines each
     * word's lexical code, basesd on a supplied dictionary.
     * @param dic The dictionary.
     */
    public void toLowerCase(CorpusIndex dic) {
        for (Word w : this) {
            int code= dic.get(w.toLowerCase());
            w.setLexCod(code);
        }        
    }

    /**
     * The overriding of the toString() method.
     * @return A string representing this sentence.
     */
    @Override
    public String toString()
    {
        StringBuilder s= new StringBuilder();
        String sp= ".,;:?!\"'";
        int n= this.size();
        for (int i=0; i<n; i++) {
            Word w= this.get(i);
            if ( w == null ) continue;
            String ws= w.toString();
            if ( sp.contains(ws) || i == 0 ) {
                /* 2016-02-12 ==> Projeto VPLP.* /
                char c0= ws.charAt(0);
                if ( i == 0 && Character.isLetter(c0) ) {
                    ws= Character.toUpperCase(c0) + ws.substring(1);
                }
                /**/
                s.append(ws);
            }
            else
                s.append(' ').append(ws);
        }
        return s.toString();
    }
    
    

    public String toStringCapitalize()
    {
        StringBuilder s= new StringBuilder();
        String sp= ".,;:?!";
        int n= this.size();
        for (int i=0; i<n; i++) {
            Word w= this.get(i);
            if ( w == null ) continue;
            String ws= w.toString();
            if ( i == 0 && Character.isLetter(ws.charAt(0)) )
                ws= Character.toUpperCase(ws.charAt(0)) + ws.substring(1);
            if ( sp.contains(ws) || i == 0 ) {
                s.append(ws);
            }
            else
                s.append(' ').append(ws);
        }
        return s.toString();
    }    


    /**
     * A toString() type method giving each word joined with its
     * respective part-of-speech tag
     * @return String
     */
    public String toStringPOS() {
        int n= this.size();
        if ( n < 1 )  return "";

        StringBuilder sb= new StringBuilder();
        for (int i=0; i<n; i++) {
            Word word= this.get(i);
            String tag= word.getTag();
            if ( tag.length() == 0 )
                sb.append(' ').append(word.toString());
            else
                sb.append(' ').append(word.toString()).append('/').append(tag);


        }

        return sb.substring(1);
    }

    /**
     * Transform this sentence into a kind of a multi-word-unit (MWU) expression.
     * Each word will be connected to their neighbors through underscores. For
     * example, the sentence "The big cat" will give rise to "The_big_cat".
     * @since 2010-02-12 (Created for the work with Gintare).
     * @return The multi-word-unit.
     */
    public String toMWUString() {
        StringBuilder sb= new StringBuilder(getWord(0));
        for (int i = 1; i < size(); i++) {
            sb.append('_');
            sb.append(getWord(i));
        }

        return sb.toString();
    }

    /**
     * Produces a given number of random "mutations" in this sentence. This method was
     * used in several early paraphrase detection experiments. A "mutation" consists
     * in transforming a sentence word into a constant of mutation ("XMUT") token.
     * @param n The maximum and likely number of mutations.
     * @return A mutated sentence.
     */
    public Sentence mutation(int n)
    {
        Sentence sx= (Sentence)this.clone();
        if ( n == 0 )  return sx;

        Random r= new Random();
        int a= sx.size();
        for (int i=0; i<n; i++) {
            Word w= new Word("XMUT");
            w.setLexCod(-2001);
            int k= r.nextInt(a);  //System.out.printf("\t k: %d\n", k);
            sx.set(k, w);
        }

        return sx;
    }


    public static void x201102012359() {
        String[] vs= {
            "Israel's air force has struck more than 30 targets in gaza in the past 24 hours, " +
                    "hitting roads, bridges and the strip's only power plant.",
            "The israeli air force has struck more than 30 targets in the past 24 hours, " +
                    "hitting roads, bridges and power plants, in a massive offensive meant " +
                    "to pressure hamas militants to release cpl.",
            "In his letter to attorney general alberto gonzales on sunday, hastert asked the " +
                    "justice department to investigate who had specific knowledge of the content " +
                    "and what actions such individuals took, if any, to provide them to law " +
                    "enforcement.",
            "Hastert asked the department to investigate who had knowledge of the content and " +
                    "what actions such individuals took, to provide them to law enforcement, in " +
                    "a letter to attorney general alberto gonzales on sunday.",
            "Shalit was captured sunday when gaza guerrillas tunnelled under the border, attacking " +
                    "an israeli military outpost and killing two other soldiers.",
            "Gilad shalit, 19, who was captured sunday when gaza militants tunneled under the " +
                    "border, attacking an israeli outpost and killing two other soldiers.",
        };

        Sentence[] vsentence= new Sentence[vs.length];
        CorpusIndex dictionary= new CorpusIndex();
        System.out.println("\n\n### CLASS TESTING");
        System.out.println("### CREATE AN ARRAY OF SENTENCES");
        for (int i = 0; i < vs.length; i++) {
            vsentence[i]= new Sentence(vs[i]);
            dictionary.add(vsentence[i]);

            System.out.printf("SENT(%d) ---> %s\n", i, vsentence[i]);
        }

        System.out.println("\n\n### CODIFYING SENTENCES ...");
        dictionary.rebuild();
        for (Sentence s : vsentence) {
            s.codify(dictionary);
        }
        System.out.println(    "### .............. DONE ...\n\n");



        System.out.println("### TWO EXAMPLES OF SENTENCE SIMILARITY TESTING:\n\n");
        System.out.printf("A) The Sumo Function -- Using: dsumo(Sentence other)\n\n");
        for (int i = 0; i < vsentence.length-1; i++) {
            for (int j = i; j < vsentence.length; j++) {
                double sij= vsentence[i].dsumo(vsentence[j]);
                System.out.printf("s(%d,%d)=%.5f  ", i, j, sij);
            }
            System.out.println();
        }
        System.out.println("\n");

        System.out.printf("B) The N-gram Function -- Using: dsNgram(Sentence sa, Sentence sb)\n\n");
        for (int i = 0; i < vsentence.length-1; i++) {
            for (int j = i; j < vsentence.length; j++) {
                double sij= Sentence.dsNgram(vsentence[i], vsentence[j]);
                System.out.printf("s(%d,%d)=%.5f  ", i, j, sij);
            }
            System.out.println();
        }

        System.out.println("\n");
    }


    /**
     * Correcções na sequência dos testes exaustivos realizados pelo
     * Steven Burrows.
     */
    public static void x201102281055() {
        String[] vsentence = {
            //PAR 1
            "It can only boast of the remains of lofty pilasters, and the marks " +
            "of what was once an inscription; and the inside being converted " +
            "into a paltry-looking palais de justice, will hardly repay the " +
            "trouble of waiting for the concierge.",
            "It can only boast of the remains of lofty pilasters, and the marks " +
            "of what was once an inscription; and the inside being converted " +
            "into a paltry-looking palais de justice, will hardly repay the " +
            "trouble of waiting for the concierge.",

            //PAR 2
            "Then to Pierre: For thee, thou shalt know the desert and the storm and " +
            "the lonely hills; thou shalt neither seek nor find.  Go, and return no " +
            "more. The two men, Sherburne falteringly, stepped down and moved to " +
            "the open plain.",
            "",

            //PAR 3
            "RITA (quietly)",
            "(quietly) RITA"
        };


        testaMetricas(vsentence[4], vsentence[5]);
    }


    public static void testaMetricas(String s1, String s2) {
        Sentence sa= new Sentence(s1);
        Sentence sb= new Sentence(s2);
        CorpusIndex.codifyOnFly(sa,sb);

        System.out.printf("[%s]\n", sa);
        System.out.printf("[%s]\n", sb);
        System.out.printf("      dsNgram(sa,sb) = %.5f\n", dsNgram(sa, sb));
        System.out.printf("dsuffixArrays(sa,sb) = %.5f\n", dsuffixArrays(sa, sb));
        System.out.printf("       dsBLEU(sa,sb) = %.5f\n", dsBLEU(sa, sb));
        System.out.printf("        sa.dsumo(sb) = %.5f\n", sa.dsumo(sb));
        System.out.printf("         sa.dSin(sb) = %.5f\n", sa.dSin(sb));
        System.out.printf("   sa.dParabolic(sb) = %.5f\n", sa.dParabolic(sb));
        System.out.printf("    sa.dsEntropy(sb) = %.5f\n", sa.dsEntropy(sb));
        System.out.printf("       sa.dgauss(sb) = %.5f\n", sa.dgauss(sb));
        System.out.printf("      sa.dLinear(sb) = %.5f\n", sa.dLinear(sb));
    }


    public static void demoForWebPage() {
        String[] vs = {
            //S1
            "Radiation from this solar flare will hit Earth's magnetic field on Wednesday, with impact on air traffic.",
            //S2
            "Our magnetic field will be affected, next Wednesday, by this solar flare.",
            //S3
            "Tim Cook and Philip Schiller unveil the company's newest iPad."
        };
        /*
        vs = new String[] {
            "As soon as they arrived, they threw themselves amidst the hedges, nearly all in columns, and sustained thus the attacks of the enemies, and an engagement which every moment grew hotter, without having the means to arranging themselves in any order.",
            " In his profession Weir had a reputation, built on relentless toil and sound ideas and daring achievements--a reputation enhanced by a character of mystery, for the man was unmarried, reserved, without intimates or even friends, locking his lips about his life, and welcoming and executing with grim indifference to risk engineering commissions of extreme hazard, on which account he had acquired the soubriquet of Cold Steel Weir.",
        };
        */
        

        System.out.println("\n\n[ARRAY OF SENTENCES]\n");

        Sentence[] vst= new Sentence[vs.length];
        for (int i = 0; i < vst.length; i++) {
            vst[i]= new Sentence(vs[i]);
            System.out.printf("S%d --> %s\n", i+1, vst[i].toString());
        }

        System.out.println("\n\n[SENTENCE SIMILARITY - METHOD: Sumo]\n");
        System.out.println("        S1          S2          S3\n");
        for (int i = 0; i < vst.length; i++) {
            System.out.printf("   S%d", i+1);
            for (Sentence s : vst) {
                double dx = vst[i].dsumo(s);
                System.out.printf("   %.7f", dx);
            }
            System.out.println("\n");
        }

        System.out.println("\n\n[SENTENCE DISSIMILARITY - METHOD: Edit Distance]\n");
        System.out.println("          S1     S2     S3\n");
        for (int i = 0; i < vst.length; i++) {
            System.out.printf("   S%d", i+1);
            for (Sentence s : vst) {
                double dx = vst[i].dsLevenshtein(s);
                System.out.printf("   %4d", (int)dx);
            }
            System.out.println("\n");
        }
    }


    /**
     * The main method contains a general class tester.
     * @param args
     */
    public static void main(String[] args) {
        demoForWebPage();
        //test20171101at13h31();
    }
    
    
    
    public static void test20171101at13h31() {
        String[] vs = new String[]{
            "After the ruling, around 600 **protesters#~*# marched from the courthouse through downtown St. Louis, chanting ?No justice, no peace\" and ?Hey hey!",
            "While for the most part the protests started peacefully, St. Louis police said Friday evening that demonstrators threw rocks at the mayors home -- breaking windows and smashing the windshield of a police vehicle."
        };
        Sentence st = new Sentence(vs[1]);
        for (Word w : st) {
            System.out.printf("|%s", w);
        }
        System.out.println();
    }
}



