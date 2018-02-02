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
package hultig.util;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.io.FileInputStream;

/**
 * <p>
 * The Toolkit class contains various static methods being used within this library. It gathers a set of auxiliary functions, ranging from string
 * manipulation to basic mathematical functions, that are used in a variety of classes, throughout the library.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro (2007)
 * @version 1.0
 */
public class Toolkit {

    /**
     * A shortcut for the standard output.
     */
    public static PrintStream out = System.out;

    /**
     * Default constructor.
     */
    public Toolkit() {
    }

    /**
     * Constructor with encoding definition.
     */
    public Toolkit(String encoding) {
        try {
            out = new PrintStream(System.out, true, encoding);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            out = System.out;
        }
    }

    /**
     * A static method for reading a string from the standard input.
     *
     * @return The obtained string.
     */
    public static String readLn() {
        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

        String strin = null;
        try {
            strin = userIn.readLine();
        } catch (IOException ioe) {
            System.err.println("[INPUT ERROR] - {" + ioe + "}");
        }

        return strin;
    }

    /**
     * Makes a pause in the standard output flow, waiting for the user to hit the <b>RETURN</b> key.
     */
    public static void pause() {
        pause(null);
    }

    /**
     * Makes a pause in the standard output flow, waiting for the user to hit the <b>RETURN</b> key, and prompting him with a message.
     *
     * @param message The pause prompt message.
     */
    public static void pause(String message) {
        if (message != null) {
            print(message);
        }
        try {
            System.in.read();
        } catch (IOException ex) {
        }
    }

    /**
     * A shortcut for the standard print.
     *
     * @param s The string to be written.
     */
    public static void print(String s) {
        out.print(s);
    }

    /**
     * A shortcut for the standard println.
     *
     * @param s The string to be written.
     */
    public static void println(String s) {
        out.println(s);
    }

    /**
     * The <b>sprintf</b> function, similar to the one existing in the C language.
     *
     * @param format The format string.
     * @param args The array of objects to be inserted in the corresponding positions, defined by the format string.
     * @return The composed string by combining the format and the objects.
     */
    public static String sprintf(String format, Object... args) {
        return new Formatter().format(Locale.US, format, args).toString();
    }

    /**
     * A shortcut for the <b>printf</b> function.
     *
     * @param format The format string.
     * @param args The array of objects to be inserted in the corresponding positions, defined by the format string.
     */
    public static void printf(String format, Object... args) {
        System.out.print(new Formatter().format(Locale.US, format, args).
                toString());
    }

    /**
     * Outputs a static array of strings.
     *
     * @param v String[] The array of strings to be printed.
     * @param indexes If true, array indexes will be printed.
     */
    public static void print(String[] v, boolean indexes) {
        if (indexes) {
            for (int i = 0; i < v.length; i++) {
                printf(" v(%d) ---> [%s]\n", i, v[i]);
            }
        } else {
            for (int i = 0; i < v.length; i++) {
                println(v[i]);
            }
        }
    }

    /**
     * Outputs a static array of strings.
     *
     * @param v The array of strings to be printed.
     */
    public static void print(String[] v) {
        print(v, false);
    }

    /**
     * Outputs an array of integer values.
     *
     * @param v The array to be printed.
     */
    public static void print(int[] v) {
        if (v == null) {
            return;
        }

        StringBuilder sb = new StringBuilder("<" + v[0]);
        for (int i = 1; i < v.length; i++) {
            sb.append(", ").append(v[i]);
        }
        sb.append(">");
        print(sb.toString());
    }

    /**
     * Outputs an array of double values.
     *
     * @param v The array to be printed.
     */
    public static void print(double[] v) {
        if (v == null) {
            return;
        }

        StringBuilder sb = new StringBuilder("<" + v[0]);
        for (int i = 1; i < v.length; i++) {
            sb.append(", ").append(v[i]);
        }
        sb.append(">");
        print(sb.toString());
    }

    /**
     * Outputs an array of integers.
     *
     * @param prefix The prefix string.
     * @param v The array to be printed.
     */
    public static void printVector(String prefix, int[] v) {
        int n = v.length;
        System.out.printf("\n %s <", prefix);
        for (int i = 0; i < n - 1; i++) {
            System.out.printf("%d, ", v[i]);
        }
        System.out.print(v[n - 1] + ">\n");
    }

    /**
     * Constructs a string of repeated characters, with a given size.
     *
     * @param c The string character.
     * @param size The string length.
     * @return The constructed string.
     */
    public static String sline(char c, int size) {
        if (size < 1) {
            return "";
        }
        char[] vc = new char[size];
        for (int i = 0; i < size; i++) {
            vc[i] = c;
        }
        return (new String(vc));
    }

    /**
     * Generates a '-' string with a given size.
     *
     * @param size The string length.
     * @return The constructed string.
     */
    public static String sline(int size) {
        return sline('-', size);
    }

    /**
     * Generates a string of underscores, with a given length.
     *
     * @param size The length of the generated string
     * @return The generated string.
     */
    public static String underscore(int size) {
        return sline('_', size);
    }

    /**
     * Transforms a string into one with a given length, by filling its left hand side with spaces.
     *
     * @param s The string to be transformed.
     * @param n The string's output length.
     * @return The transformed string.
     */
    public static String padLeft(String s, int n) {
        int dn = n - s.length();
        if (dn <= 0) {
            return n > 0 ? s.substring(0, n) : s;
        }

        char[] spc = new char[dn];
        for (int k = 0; k < dn; k++) {
            spc[k] = ' ';
        }
        return new String(spc) + s;

    }

    /**
     * Transforms a string into one with a given length, by filling its right hand side with spaces.
     *
     * @param s The string to be transformed.
     * @param n The string's output length.
     * @return The transformed string.
     */
    public static String padRight(String s, int n) {
        int dn = n - s.length();
        if (dn <= 0) {
            return s;
        }

        char[] spc = new char[dn];
        for (int k = 0; k < dn; k++) {
            spc[k] = ' ';
        }
        return s + new String(spc);

    }

    /**
     * Concatenates the strings contained in an array, joining them through the space character, as a string separator.
     *
     * @param vs The array of strings
     * @return The concatenated string.
     */
    public static String joinStrings(String[] vs) {
        return joinStrings(vs, ' ');
    }

    /**
     * Concatenates the strings contained in an array, joining them through a given separator character.
     *
     * @param vs The array of strings.
     * @param separator The separator.
     * @return The concatenated string.
     */
    public static String joinStrings(String[] vs, char separator) {
        if (vs == null) {
            return "";
        }
        int n = vs.length;
        if (n < 1) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n - 1; i++) {
            sb.append(vs[i]);
            sb.append(separator);
        }
        sb.append(vs[n - 1]);

        return sb.toString();
    }

    /**
     * Cleans a string by eliminating any character contained in a givens set of characters.
     *
     * @param s The string to be cleaned.
     * @param chset The set of characters to eliminate.
     * @return The cleaned string.
     */
    public static String sclean(String s, String chset) {
        if (s == null) {
            return null;
        }
        if (chset == null) {
            return s;
        }

        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < s.length(); k++) {
            char ck = s.charAt(k);
            if (chset.indexOf(ck) < 0) {
                sb.append(ck);
            }
        }
        return sb.toString().trim();
    }

    /**
     * Cleans all strings in a given array, by eliminating any character from a given set.
     *
     * @param vs The array of strings to be cleaned.
     * @param chset The character set to eliminate.
     */
    public static void sclean(String[] vs, String chset) {
        if (vs == null || chset == null) {
            return;
        }
        for (int i = 0; i < vs.length; i++) {
            vs[i] = sclean(vs[i], chset);
        }
    }

    /**
     * Gives a string containing the current moment, in terms of year, month, day, hour, minute, and second. For example: "2011/06/02 09:11:12".
     *
     * @return The moment string.
     */
    @SuppressWarnings("static-access")
    public static String moment() {
        Calendar c = Calendar.getInstance();
        String mes = "" + (c.get(c.MONTH) + 1);
        mes = mes.length() == 1 ? '0' + mes : mes;
        String dia = "" + c.get(c.DAY_OF_MONTH);
        dia = dia.length() == 1 ? '0' + dia : dia;
        String hor = "" + c.get(c.HOUR);
        if (c.get(c.AM_PM) == c.PM) {
            hor = "" + (c.get(c.HOUR) + 12);
        }
        hor = hor.length() == 1 ? '0' + hor : hor;
        String min = "" + c.get(c.MINUTE);
        min = min.length() == 1 ? '0' + min : min;
        String sec = "" + c.get(Calendar.SECOND);
        sec = sec.length() == 1 ? '0' + sec : sec;
        String s = c.get(c.YEAR) + "/" + mes + "/" + dia + " " + hor + ":"
                + min + ":" + sec;
        return s;
    }

    /**
     * Outputs a string with a maximum number of columns. If it is a textual string, the division is made on spaces, otherwise the division is made
     * strictly at the given length.
     *
     * @param s The string to be written
     * @param nmx The maximum number of columns.
     * @param sleft The left context string, possibly a sequence of spaces.
     * @param sright The right context string.
     */
    public static void formatedWrite(String s, int nmx, String sleft, String sright) {
        int k = 0;
        try {
            if (nmx < 1 || s == null) {
                return;
            }

            int n = s.length();
            boolean left = sleft != null && sleft.length() > 0;
            boolean right = sright != null && sright.length() > 0;
            if (nmx >= n) {
                if (left) {
                    s = sleft + s;
                }
                if (right) {
                    s = s + sline(' ', nmx - n) + sright;
                }
                out.println(s);
                return;
            }

            k = nmx;
            for (; k > 0; k--) {
                if (s.charAt(k) == ' ' || s.charAt(k) == '\t') {
                    break;
                }
            }
            if (k == 0) {
                k = nmx;
            }

            String sk = s.substring(0, k);
            if (left) {
                sk = sleft + sk;
            }
            if (right) {
                sk = sk + sline(' ', nmx - k) + sright;
            }
            out.println(sk);
        } catch (Exception e) {
            System.err.printf("k = %3d   s ---> [%s]\n", k, s);
            e.printStackTrace();
        }

        formatedWrite(s.substring(k).trim(), nmx, sleft, sright);
    }

    /**
     * Counts the occurrence of a regular expression in a given text file.
     *
     * @param pattern The regular expression to be counted.
     * @param filename The name of the text file.
     * @return The number of counts.
     */
    public static long countPattern(String pattern, String filename) {
        long counter = 0L;

        InputStream in = null;
        BufferedReader br = null;
        try {
            in = new FileInputStream(filename);
            br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));

            Pattern p = Pattern.compile(pattern);

            for (;;) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                Matcher match = p.matcher(line);
                while (match.find()) {
                    counter++;
                }
            }

            br.close();
        } catch (Exception ex) {
            System.err.println("IO ERROR: " + ex);
            return 0L;
        }

        return counter;
    }

    /**
     * Reads a certain number of lines from a given file, using a specified encoding.
     *
     * @param filename The name of the file to be read.
     * @param encoding The encoding string.
     * @param limit The number of lines.
     * @return The array of strings read.
     */
    public static String[] readFile(String filename, String encoding, int limit) {
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        Vector<String> v = new Vector<String>();
        try {
            InputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    encoding));

            for (int i = 0; i < limit; i++) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                v.add(line);
            }
            br.close();
        } catch (Exception ex) {
            System.err.println("IO ERROR: " + ex);
            System.err.printf("READING FILE %s\n", filename);
        }
        String[] vs = new String[v.size()];
        vs = v.toArray(vs);
        return vs;
    }

    /**
     * Counts the number of lines in a given file.
     *
     * @param filename The file name.
     * @return The number of lines.
     */
    public static int countLines(String filename) {
        int counter = 0;

        InputStream in = null;
        BufferedReader br = null;
        try {
            in = new FileInputStream(filename);
            br = new BufferedReader(new InputStreamReader(in));

            for (;;) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                counter++;
            }
            br.close();
        } catch (Exception ex) {
            System.err.println("IO ERROR: " + ex);
        }

        return counter;
    }

    /**
     * Creates an ordered random sample of integers, between 0 and <b>max</b>, uniformly distributed.
     *
     * @param size int The sample size.
     * @param max int The sample maximum value.
     * @return int[] The generated sample.
     */
    public static int[] createRandomSample(int size, int max) {
        LinkedList<Integer> v = new LinkedList<Integer>();
        for (int i = 0; i < size; i++) {
            v.add(new Integer(i + 1));
        }

        int[] sample = new int[size];
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            int j = r.nextInt(v.size());
            sample[i] = v.get(j).intValue();
            v.remove(j);
        }
        Arrays.sort(sample);

        return sample;
    }

    /**
     * Executes a specific command in the operating system.
     *
     * @param command The command string to be executed.
     */
    public static boolean execute(String command) {
        printf("\nOS - EXECUTION - CMD: [%s]\n", command);
        try {
            Runtime rt = Runtime.getRuntime();
            Process q = rt.exec(command);
            q.waitFor();
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Tests the nullity of an array of objects.
     *
     * @param oset The variable array of objects.
     * @return Is true if one object is null.
     */
    public static boolean nulity(Object... oset) {
        if (oset == null) {
            return true;
        }
        for (Object obj : oset) {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * The minimum, among three values.
     *
     * @param a value
     * @param b value
     * @param c value
     * @return The minimum.
     */
    public static int minimum(int... v) {
        if (v == null || v.length < 1) {
            return Integer.MIN_VALUE;
        }

        int min = v[0];
        for (int i = 1; i < v.length; i++) {
            if (v[i] < min) {
                min = v[i];
            }
        }
        return min;
    }

    /**
     * The minimum, among three values.
     *
     * @param a value
     * @param b value
     * @param c value
     * @return The minimum.
     */
    public static float minimum(float... v) {
        if (v == null || v.length < 1) {
            return Integer.MIN_VALUE;
        }

        float min = v[0];
        for (int i = 1; i < v.length; i++) {
            if (v[i] < min) {
                min = v[i];
            }
        }
        return min;
    }

    /**
     * The maximum, among three values.
     *
     * @param a value
     * @param b value
     * @param c value
     * @return The minimum.
     */
    public static int maximum(int... v) {
        if (v == null || v.length < 1) {
            return Integer.MIN_VALUE;
        }

        int max = v[0];
        for (int i = 1; i < v.length; i++) {
            if (v[i] > max) {
                max = v[i];
            }
        }
        return max;
    }

    /**
     * The maximum, among three values.
     *
     * @param a value
     * @param b value
     * @param c value
     * @return The minimum.
     */
    public static double maximum(double... v) {
        if (v == null || v.length < 1) {
            return Integer.MIN_VALUE;
        }

        double max = v[0];
        for (int i = 1; i < v.length; i++) {
            if (v[i] > max) {
                max = v[i];
            }
        }
        return max;
    }

    /**
     * Gives the smallest value from an array, which is greater than a given infimum value.
     *
     * @param infimum The infimum value.
     * @param v The array from which to extract the value.
     * @return The searched value.
     */
    public static int minGreaterThan(int infimum, int[] v) {
        int min = infimum;
        int i;
        for (i = 0; i < v.length; i++) {
            if (v[i] > infimum) {
                min = v[i];
                break;
            }
        }
        for (; i < v.length; i++) {
            if (v[i] > infimum && v[i] < min) {
                min = v[i];
            }
        }
        return min;
    }

    /**
     * Computes the dot product between two vectors, represented by two arrays.
     *
     * @param u Represents one vector.
     * @param v Represents the other vector.
     * @return The dot product value.
     */
    public static double dotProduct(double[] u, double[] v) {
        if (u == null || v == null) {
            return 0.0f;
        }

        int n = Math.min(u.length, v.length);
        double soma = 0.0f, smu = 0.0f, smv = 0.0f;
        for (int k = 0; k < n; k++) {
            soma += u[k] * v[k];
            smu += u[k] * u[k];
            smv += v[k] * v[k];
        }

        return soma / Math.sqrt(smu * smv);
    }

    /**
     * Computes the likelihood to have text, for a given string. It is simply based on the number of letters and spaces contained in the string.
     *
     * @param s The string to be measured.
     * @return A value in the unitary interval: [0,1].
     */
    public static double probGoodText(String s) {
        if (s == null) {
            return 0.0;
        }

        char c, cb = ' ';
        long count = 0L;
        long N = s.length();
        for (int k = 0; k < N; k++, cb = c) {
            c = s.charAt(k);
            if (!Character.isLetter(c) && c != ' ') {
                count++;
            }
            if (k > 0 && c == ' ' && !Character.isLetter(cb)) {
                count++;
            }
        }

        return 1.0 - (double) count / N;
    }

    /**
     * Gives the square of a value.
     *
     * @param x The value to be squared.
     * @return The square.
     */
    public static double sqr(double x) {
        return x * x;
    }

    /**
     * Computes the log value in a given base.
     *
     * @param x The input value.
     * @param base The base value.
     * @return The log value.
     */
    public static double log(double x, double base) {
        return Math.log(x) / Math.log(base);
    }

    /**
     * Gives the logarithm in base 2.
     *
     * @param x The input value.
     * @return The base 2 logarithm
     */
    public static double log2(double x) {
        return Math.log(x) / Math.log(2.0);
    }

    /**
     * Computes the F-measure, for a given precision and recall.
     *
     * @param precision The precision value.
     * @param recall the recall value.
     * @return The F-measure value.
     */
    public static double Fmeasure(double precision, double recall) {
        return Fmeasure(precision, recall, 1.0);
    }

    /**
     * Computes the F-measure, for a given precision, recall, and beta parameter.
     *
     * @param precision The precision value;
     * @param recall The recall value
     * @param beta The beta parameter.
     * @return The F-measure value.
     */
    public static double Fmeasure(double precision, double recall, double beta) {
        if (precision == 0 && recall == 0) {
            return 0.0;
        }

        double b2 = sqr(beta);
        return (1.0 + b2) * precision * recall / (b2 * precision + recall);
    }

    /**
     * Outputs the histogram corresponding to a given array of integers. For each value, the corresponding number of bars printed is relative to a
     * maximum value, provided as a parameter.
     *
     * @param v The array of values for the histogram.
     * @param maxbars The maximum number of bars printed. That is, for the maximum value of <b>maxbars</b> bars will be printed.
     */
    public static void printHistogram(int[] v, int maxbars) {
        if (v == null || maxbars < 2) {
            return;
        }

        int max = v[1];
        for (int i = 2; i < v.length; i++) {
            if (v[i] > max) {
                max = v[i];
            }
        }

        System.out.println("      |");
        System.out.println("      |");
        for (int i = 1; i < v.length; i++) {
            double p = 1.0 * v[i] / v[0];
            int nbars = (int) (v[i] / max * maxbars);
            System.out.printf("   %2d |", i);
            for (int j = 0; j < nbars; j++) {
                System.out.print('|');
            }
            System.out.printf("  %2d       (%4.2f%%)\n", v[i], 100.0 * p);
            System.out.print("      |\n");
        }

        System.out.print("    ");
        for (int i = 0; i < maxbars + 6; i++) {
            System.out.print('-');
        }
        System.out.println("-->\n");
    }
    

    public static String justifyText(String text, int columns) {
        String[] blocks = text.split("[ \n]+");
        if (blocks.length < 1) {
            return null;
        }
        //txtArea.setText(blocks[0]+" "+columns+" "+blocks[2]);
        //int r= 0;
        StringBuilder newText = new StringBuilder();
        for (int i = 0; i < blocks.length;) {
            int j = i, n = 0;
            for (; j < blocks.length; j++) {
                int bjn = blocks[j].length() + 1;
                if (n + bjn > columns) {
                    //System.out.printf("%d   %2d   %2d   %s\n", r, n, bjn, blocks[j]);
                    break;
                }
                n += bjn;
            }

            if (j == blocks.length) {
                //==> last line => do not justify
                newText.append(buildLine(blocks, i, j, 0)).append("\n");
                break;
            }
            String line = buildLine(blocks, i, j, columns - n + 1);
            if (line != null && line.length() > 0) {
                newText.append(line).append("\n");
            }
            i = i < j ? j : i + 1;
            //r++;
        }
        return newText.toString();
    }

    /**
     * Justifies the text for a given line to be written. Concatenates the strings from a
     * given array, between two given positions. Afterwards, the remaining spaces will be 
     * filled with blank ones, inserted among the words.
     * @param v The array of all tokens.
     * @param a The starting positions.
     * @param b The ending position.
     * @param spacesRemainingToFill The amount of space needed to be filled.
     * @return The string in a justified format.
     */
    private static String buildLine(String[] v, int a, int b, int spacesRemainingToFill) {
        if (v == null) {
            return null;
        }
        StringBuilder[] vb = new StringBuilder[v.length];
        for (int i = a; i < b; i++) {
            vb[i] = (new StringBuilder(v[i])).append(' ');
        }

        int numIntervals = b - a - 1;
        if (numIntervals > 0) {
            /*
            System.out.println();
            System.out.println("spacesRemainingToFill:... "+spacesRemainingToFill);
            System.out.println("numIntervals:............ "+numIntervals);
            */
            int spacesByInterval = spacesRemainingToFill / numIntervals;
            //System.out.println("spacesByInterval:........ "+spacesByInterval);
            if (spacesByInterval > 0) {
                char[] vc = new char[spacesByInterval];
                for (int i = 0; i < vc.length; i++) {
                    vc[i] = ' ';
                }
                for (int i = a; i < b - 1; i++) {
                    vb[i].append(vc);
                }
            }

            int spacesRemaining = spacesRemainingToFill % numIntervals;
            if (spacesRemaining > 0) {
                int pa = a, pb = b-1, pm = (pa + pb) / 2;
                int k = 0, i = pm;
                int di = Math.min((vb.length - 1) / spacesRemaining, 1);
                //System.out.printf("pa:%d  pb:%d  pm:%d\n", pa, pb, pm);
                for (int j = 0; j < spacesRemaining; j++) {
                    //System.out.print(i+" ");
                    vb[i].append(' ');
                    if (j % 2 == 0) {
                        k++;
                        i = pm + k * di;
                    } else {
                        i = pm - k * di;
                    }
                    if (i < pa || i > pb) {
                        k = j % 2 == 0 ? 0 : 1;
                        i = pm + k * di;
                    }
                }
                //System.out.println();
            }
            /** /
            System.out.println("spacesRemaining:......... "+spacesRemaining);
            int k = b - 2;
            while (k >= a && spacesRemaining > 0) {
                vb[k].append(' ');
                spacesRemaining--;
                k--;
            }
            /**/
        }

        StringBuilder sb = new StringBuilder();
        for (int i = a; i < b; i++) {
            sb.append(vb[i]);
        }
        return sb.toString();
    }
    
    
    /**
     * Computes the word cosine similarity between two strings.
     * @param a The first string.
     * @param b The second string. 
     * @return A similarity value in the [0,1] interval.
     */
    public static double cosSimStr(String a, String b) {
        String[] va= a.toLowerCase().split("[ ,;:.\t()!?]+"); Arrays.sort(va);
        String[] vb= b.toLowerCase().split("[ ,;:.\t()!?]+"); Arrays.sort(vb);
        int i=0, j= 0;
        double axb= 0.0;
        double nai= 0.0;
        double nbj= 0.0;
        while ( i<va.length && j<vb.length ) {
            int cmp= va[i].compareTo(vb[j]);
            //System.out.printf("%3d  %-10s   %3d   %-10s    cmp: %d\n", i, va[i], j, vb[j], cmp);
            if ( cmp < 0 ) {
                nai+= va[i].length()*va[i].length();
                i++;
            } else if ( cmp > 0 ) {
                nbj+= vb[j].length()*vb[j].length();
                j++;
            }
            else {
                axb+= va[i].length() * vb[j].length();
                nai+= va[i].length()*va[i].length();
                nbj+= vb[j].length()*vb[j].length();
                i++;
                j++;
            }
        }
        
        return axb / (Math.sqrt(1+nai) * Math.sqrt(1+nbj));
    }
    

    /**
     * Purpose: Class self testing.
     * @param args System arguments.
     */
    public static void main(String[] args) {
        System.out.println();
        int n = maximum(3, 5, -3, 6, -7);
        System.out.println("max: " + n);
        
        String s= "The Red Cross had been in touch with staff in Torba province in Vanuatu's "
                + "north, were people were reported to be safe. Earlier unconfirmed reports "
                + "had said more than 40 may have been killed in the northern region.";
        System.out.println("\n"+justifyText(s, 50));
    }
}
