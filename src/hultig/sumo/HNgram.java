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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import hultig.io.FileIN;
import hultig.util.Toolkit;


/**
 * An efficient representation of a large set of n-grams. Based on a
 * {@code HashMap}, it associates an integer - the frequency of the
 * corresponding n-gram. <br />
 * (9:37:45 13 April 2009)
 */
public class HNgram extends Hashtable<String, Integer>
{
    /**
     * The n-gram dimensionality: 1-gram, 2-gram, 3-gram, ... The
     * default is a 1-gram, also mentioned as a bigram.
     */
    private int N= 1;

    /**
     * The sum of frequencies - the number of processed tokens.
     */
    private long soma=0L;

    /**
     * The n-gram table, holding the frequency of each n-gram
     * in the processed corpora.
     */
    //private Hashtable<String, Integer> hsubngram;


    public HNgram() {
        super();
        soma= 0L;
        //hsubngram= new Hashtable<>();
    }


    /**
     * Creates this object and loads the n-gram table from a given file.
     * The expected file has a textual representation, with one n-gram
     * and frequency pair per line. The default n-gram dimensionality is
     * two (bigrams). This constructor invokes the
     * {@link #set(hultig.io.FileIN) set(FileIN)} method.
     * @param fname The name of the text file to be processed.
     */
    public HNgram(String fname) {
        FileIN f= new FileIN(fname);
        f.open();
        if ( !f.isFile() ) {
            System.err.printf("\n   ERROR - FILE NOT FOUND - file name: %s\n", fname);
            return;
        }
        //hsubngram= new Hashtable<>();
        set(f);
    }

    /**
     * Creates this object and loads the n-gram table from a given file.
     * This constructor invokes the {@link #HNgram(String fname) HNgram(String)}
     * method.
     * @param fname The name of the text file to be processed.
     * @param n The n-gram dimensionality value.
     */
    public HNgram(String fname, int n) {
        this(fname);
        N= n;
    }

    /**
     * This method loads the n-gram table from a given file. It is assumed
     * that the n-grams dimensionality was already defined.
     * @param f Represents the n-gram table file to be processed.
     */
    public final void set(FileIN f) {
        for (;;) {
            String line= f.read();
            if ( line == null ) {
                f.close();
                return;
            }

            int p= line.lastIndexOf(' ');
            if ( p < 1 ) continue;

            String ngram= line.substring(0,p).trim();
            try {
                Integer freq= Integer.parseInt( line.substring(p+1) );
                put(ngram, freq);
                soma+= freq;
                //storeSubNgram(ngram, freq);
            } catch (NumberFormatException e) {}
        }
    }

    /* * COMENTED AT July, 23, 2014.
     * Processes and stores a given n-gram with its frequency
     * in this table.
     * @param ngram The n-gram string, for example: "{@code \"to be or not\"}"
     * @param freq The frequency of the indicated n-gram.
     * /
    private void storeSubNgram(String ngram, int freq) {
        //EXAMPLE: ngram = "to be or not to be"
        //                  012345678901234567
        int a= ngram.indexOf(' ');     // a ==  2
        int b= ngram.lastIndexOf(' '); // b == 15  
        if ( a < 1 || b < 1 ) return;
        String pNgram= ngram.substring(0,b); // pNgram == "to be or not to"
        String sNgram= ngram.substring(a+1); // sNgram == "be or not to be"
        String[] Ngrams= {pNgram, sNgram};

        for (String ng : Ngrams ) {
            if (ng != null && ng.length() > 0) {
                Integer f = hsubngram.get(ng);
                if (f == null) {
                    hsubngram.put(ng, freq);
                } else {
                    hsubngram.put(ng, f + freq);
                }
            }
        }
    }
    */
    

    public void countNGram(String sngram) {
        if ( sngram == null || sngram.length() < 1 )  return;
        
        Integer f= get(sngram);
        if ( f == null ) {
            put(sngram, 1);
        }
        else {
            put(sngram, f+1);
        }

        soma++;
    }

    /**
     * Removes all n-grams from this table, that satisfies a given
     * string pattern (regular expression).
     * @param pattern The regular expression.
     * @return The number of n-grams removed.
     */
    public int exclude(String pattern) {
        int cont= 0;
        Pattern pat=  Pattern.compile(pattern);

        //Iterator<String> ek= super.keySet().iterator();
        Enumeration<String> ek= super.keys();
        for(; ek.hasMoreElements(); ) {
            String sk= ek.nextElement();
            Matcher m= pat.matcher(sk);
            if ( m.find() ) {
                Integer f= this.get(sk);
                remove(sk);
                soma-= f;
                cont++;
            }
        }
        return cont;
    }

    /**
     * Returns the frequency of a given n-gram.
     * @param ngram The indicated n-gram.
     * @return The frequency of this n-gram (>= 0)
     */
    public int freq(String ngram) {
        Integer f= get(ngram);
        if ( f == null ) return 0;
        return f;
    }

    /**
     * Returns the frequency of a given n-gram, indicated through
     * an array of strings.
     * @param v The array containing the n-gram word sequence.
     * @return The frequency of the n-gram, or -1 upon inexistence
     * or erroneous situations.
     */
    public int freq(String[] v) {
        if ( v == null || v.length < 1 )  return -1;

        return freq(Toolkit.joinStrings(v));
    }

    /**
     * The estimated probability of a given n-gram, for the data
     * in this table.
     * @param ngram The indicated n-gram
     * @return The probability estimation, in percentage.
     */
    public double prob(String ngram) {
        int f= freq(ngram);
        return (double) 100.0 * (f<0?0:f) / soma;
    }

    /**
     * The estimated probability of a given n-gram, for the data
     * in this table. The n-gram is represented through an array
     * with n strings.
     * @param v The n-gram representation.
     * @return The probability estimation, in percentage.
     */
    public double prob(String[] v) {
        return (double) 100.0 * freq(v) / soma;
    }


    /**
     * Gives the sum of frequencies for all n-grams stored in this
     * table, a value necessary for n-gram probability estimation.
     * @return The sum of frequencies.
     */
    public long getSum() {
        return soma;
    }
}
