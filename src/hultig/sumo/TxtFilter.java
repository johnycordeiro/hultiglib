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



import java.io.*;
import java.util.StringTokenizer;


/**
 * <b>NOT YET WELL COMMENTED</b>.
 *
 * @author João Paulo Cordeiro
 * @version HULTIG/SUMO 2006
 */
public class TxtFilter
{
    /**
     * A boolean true value defined.
     */
    public byte ON= 1;

    /**
     * A boolean false value defined.
     */
    public byte OFF= 0;

    /**
     * If greater than 0, activates number tagging, i.e every number
     * will be replaced by the <NUM> tag.
     */
    public byte NUMTAG;

    /**
     * The string minimum length.
     */
    public int MINLEN;

    /**
     * The minimum number of words allowed, by line.
     */
    public int MINWORDS;

    /**
     * Number of words with 1, 2, ..., 10
     * NUMWORDS[0] will contain the total number of words.
     * leters.
     */
    public int[] NUMWORDS= new int[100];


    /**
     * The number of upper characters, found on the last string
     * processed (method procstr/1).
     */
    private int numuppers;

    /**
     * The number of lower characters, found on the last string
     * processed (method procstr/1).
     */
    private int numlowers;

    /**
     * The number of "good" letters, found on the last string
     * processed (method procstr/1).
     */
    private int numletters;

    /**
     * The number of tokens, found on the last string
     * processed (method procWords/1).
     */
    public int numtokens;

    public int numUpperWords;



    /**
     * Default constructor.
     */
    public TxtFilter()
    {
        NUMTAG= OFF;
        MINLEN= 5;
        MINWORDS= 3;

        numuppers= 0;
        numlowers= 0;
        numletters= 0;
        numtokens= 0;
        numUpperWords= 0;
        for (int i = 0; i < NUMWORDS.length; i++)  NUMWORDS[i]= 0;
    }


    public static boolean isLetter(byte c) {
        return Character.isLetter(c);
    }



    /**
     * Processes a string and setup a bunch of state
     * variables, like number of characters, number of
     * upper and lower characters, probability to
     * be a "good text", etc.
     *
     * @param s The string to be processed.
     * @return true if successful and false otherwise.
     */
    public boolean procstr(String s)
    {
        if (s == null)  return false;

        char c, cb = ' ';

        numlowers= 0;
        numuppers= 0;
        numletters= 0;
        for (int k = 0; k < s.length(); k++, cb = c) {
            c = s.charAt (k);
            if (!Character.isLetter (c) && c != ' ')  numletters++;
            if (k > 0 && c == ' ' && !Character.isLetter (cb))  numletters++;

            if ( Character.isUpperCase(c) )  numuppers++;
            if ( Character.isLowerCase(c) )  numlowers++;
        }

        return true;
    }


    public boolean procWords(String s) {
        if ( s == null )  return false;

        numUpperWords= 0;
        for (int i = 0; i < NUMWORDS.length; i++)  NUMWORDS[i]= 0;

        int a, b;
        String[] vs= s.split("[ -]+");
        numtokens= vs.length;
        for (int i = 0; i < numtokens; i++) {
            byte[] w= vs[i].getBytes();
            for (a=0; a<w.length; a++)
                if ( isLetter(w[a]) )  break;
            if ( a == w.length )  continue;

            for (b= w.length-1; b>=0; b--)
                if ( isLetter(w[b]) )  break;

            int n= b-a+1;
            if ( n < 1 || n>99 )  continue;

            NUMWORDS[n]++; // words with n
            NUMWORDS[0]++; // total number of words.
            if ( Character.isUpperCase(w[0]) )  numUpperWords++;
        }

        return true;
    }



    public double probBeText() {
        double pword= (double)NUMWORDS[0]/numtokens;
        return pword;
    }


    public double probBeText(String s) {
        procWords(s);
        return probBeText();
    }


    public int[] getWordHistogram() {
        return NUMWORDS;
    }


    public double getPercUpperWords() {
        return 1.0*numUpperWords/NUMWORDS[0];
    }


    /**
     * Test a set of constraints.
     * @param pwords The minimum word percentage.
     * @param reqhistogram Requested word histogram satisfaction.
     * @return true if all conditions are satisfied.
     */
    public boolean satisfyWordConstraints(double pwords, int[] reqhistogram) {
        if ( probBeText() < pwords )  return false;
        for (int i = 0; i < reqhistogram.length; i++) {
            if ( NUMWORDS[i] < reqhistogram[i] ) return false;
        }

        return true;
    }


    /**
     * Estimates the probability of <b>s</b> to be "good text".
     * By "good text" we mean that it contains some message
     * written in a natural western language, like English.
     *
     * @param s String
     * @return A probability value in the interval [0,1]
     */
    public double probGoodText (String s)
    {
	if ( ! procstr(s) )  return 0.0;

	return 1.0 - (double) numletters / s.length();
    }



    /**
     * Verifies whether a given string satisfies a number of text rules.
     * @param sx The input string or the string to be testes.
     * @return true if successful and false otherwise.
     */
    public boolean satisfySpecialRules(String sx)
    {
        if ( sx == null )  return false;

        String s= sx.trim().toLowerCase();
        if ( s.length() < MINLEN )  return false;

        //last character rule
        char last= s.charAt(s.length()-1);
        if ( last != '.' && last != '!' && last != '?' && last != '"'  && last != '\'' )
            return false;

        //count the number of words
        StringTokenizer st= new StringTokenizer(s);
        int numwords= 0;
        while ( st.hasMoreTokens() ) {
            String w= st.nextToken();
            int i;
            for (i=0; i<w.length(); i++)
                if ( !Character.isLetter(w.charAt(i)) )  break;
            if ( i == w.length() )  numwords++;
        }
        if ( numwords < MINWORDS )  return false;

        if ( s.indexOf('©') >= 0 )  return false;
        if ( s.indexOf("all rights reserved") >= 0 )  return false;

        return true;
    }


    /**
     * A shortcut for the process/2 method.
     *
     * @param file The file name to be processed.
     * @return true if no problems during processing, false otherwise.
     */
    public boolean process(String file)
    {
        return process(file, null);
    }



    /**
     * Process a file by applying a set of filtering rules.
     *
     * @param file The input file name.
     * @param fout The output file name, if it is null the output will be produced to the standard output.
     * @return true if no problems during processing, false otherwise.
     */
    public boolean process(String file, String fout)
    {
        BufferedReader br= null;  //---> input
        PrintWriter pwr= null; //------> output
        try  {
            int n=0;
            InputStream in= new FileInputStream(file);
            br= new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
            if ( fout != null )
                pwr= new PrintWriter(new FileOutputStream(fout));
        }
        catch (Exception e)  {
            System.err.println("[EXCEPTION] - ("+e.toString()+")");
            return false;
        }

        //the main processing loop
        for (int count=1;;count++)  {
            //----------
            //READ LINE
            //----------
            String line= "";
            try {
                line= br.readLine();
                if ( line == null ) {
                    br.close();
                    break;
                }
            }
            catch (IOException e) {
                System.err.println("[EXCEPTION] - ("+e.toString()+")");
                return false;
            }
            //----------

            //ignores meta-data (some xml tags)
            if ( line.toLowerCase().matches(".*<[/]*[nc].*>.*") )  {
                if ( pwr == null )
                    System.out.printf("%s\n",line);
                else
                    pwr.println(line);
                continue;
            }

            if ( NUMTAG == ON )
                line= line.replaceAll("[0-9][0-9]*","<NUM>");

            //replace quotes.
            line= line.replaceAll(" Mr\\.", " Mr ");
            line= line.replaceAll(" Mrs\\.", " Mrs ");
            line= line.replaceAll(" Ms\\.", " Ms ");
            line= line.replaceAll(" Dr\\.", " Dr ");
            line= line.replaceAll(" Rep\\.", " Rep ");
            line= line.replaceAll("''", "\"");
            line= line.replaceAll("``", "\"");
            line= line.replaceAll("â\\?\\?", "'");

           if ( ! satisfySpecialRules(line) )  continue;

            //decision uppon good text probability
            double p= probGoodText(line); //=> execution of procstr/1
            if ( pwr == null )
                System.out.printf("(p: %.5f) ---> [%s]\n", p, line);
            else
                if ( p > 0.7 &&  numuppers < numlowers )  pwr.println(line);

            //the number of thousand lines already processed
            if ( count%1000 == 0 )  {
                System.out.printf("    ... %7d lines\n", count);
                pwr.flush();
            }
        }

        if (  br != null)  try { br.close(); } catch(Exception e) {}
        if ( pwr != null)  try { pwr.close(); } catch(Exception e) {}

        return true;
    }

    /**
     * Filtering an input string uppon a bunch of rules.
     *
     * @param line String
     * @return The input string after filtered.
     */
    public String filtering(String line) {
        //ignores meta-data (some xml tags)
        if (line.toLowerCase().matches(".*<[/]*[nc].*>.*")) {
            return line;
        }

        if (NUMTAG == ON) {
            line = line.replaceAll("[0-9][0-9]*", "<NUM>");
        }

        //replace quotes.
        line = line.replaceAll(" Mr\\.", " Mr ");
        line = line.replaceAll(" Mrs\\.", " Mrs ");
        line = line.replaceAll(" Ms\\.", " Ms ");
        line = line.replaceAll(" Dr\\.", " Dr ");
        line = line.replaceAll(" Rep\\.", " Rep ");
        line = line.replaceAll("''", "\"");
        line = line.replaceAll("``", "\"");
        line = line.replaceAll("â\\?\\?", "'");

        /** 
         * This is not working yet ...
         * Still to be analyzed in the future.
         * 2011-10-07 13:46
        line.replaceAll("\\x93", "\"");
        line.replaceAll("\\x94", "\"");
        line.replaceAll("\\x96", "--");
         */
        
        if (!satisfySpecialRules(line)) {
            return null; //---> unsuccessfull ending.
        }
        //heuristic decision uppon good text probability
        double p = probGoodText(line); //=> execution of procstr/1
        // probGoodText(line) => (numuppers, numlowers)
        if (p > 0.7 && numuppers < numlowers) {
            return line;
        } else {
            return null;
        }
    }

    /**
     * The Main class.
     *
     * @param args The array with the input arguments
     */
    public static void main(String[] args)
    {
	if ( args.length < 1 )  {
	    System.out.println("\n\tSINTAXE: java JF <file>\n");
	    System.out.println("\n\tSINTAXE: java JF <file> <outfile>\n");
	    return;
	}

        TxtFilter txf= new TxtFilter();

        System.out.println( "\n");
        System.out.printf("FILTERING FILE:\n\n\t %10s ... \n\n", args[0]);
        System.out.println("..............................");
        System.out.println(".........................START");
        System.out.println("..............................");

        if ( args.length < 2 )
            txf.process(args[0]);
        else
            txf.process(args[0], args[1]);

	System.out.println("..............................");
	System.out.println("..................... COMPLETE");
        System.out.println("..............................");
    }
}
