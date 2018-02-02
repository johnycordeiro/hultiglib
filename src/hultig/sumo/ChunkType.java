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

/**
 * <p>
 * Represents a chunk type obtained from a shallow parser. The tag set
 * used is the <a href="http://www.cis.upenn.edu/~treebank/">Penn Treebank</a>.
 * Only six different tags are considered here for sentence chunks:
 * {@code NP, VP, PP, PRT, ADVP}, and {@code UNDEFINED}. In the future more
 * chunk tags can be added/defined.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro (2008)
 * @version 1.0
 */
public class ChunkType 
{
    /**
     * Internal representation of an undefined chunk.
     */
    public static final int UND = -1;

    /**
     * The code representing a Noun Phrase.
     */
    public static final int NP =  0;

    /**
     * The code representing a Verb Phrase.
     */
    public static final int VP =  1;

    /**
     * The code representing a Prepositional Phrase.
     */
    public static final int PP =  2;

    /**
     * The code representing a particle. Category
     * for words that should be tagged RP.
     */
    public static final int PRT=  3;

    /**
     * The code representing an Adverb Phrase.
     */
    public static final int ADVP= 4;

    /**
     * The set of string tags for labeling chunks, stored in this
     * array of strings.
     */
    public static final String[] DOMAIN= {
        "NP", "VP", "PP", "PRT", "ADVP",
    };


    
    /**
     * Gives the tag code corresponding to a given chunk tag,
     * taking into account the defined internal codes, in
     * this class (the static int fields).
     * @param scod The chunk tag string.
     * @return The code of the given chunk tag, or {@link #UND UND}
     * code (undefined/not known) if the string is not recognizable.
     * return
     */
    public static int str2cod(String scod) {
        for (int k=0; k<DOMAIN.length; k++)
            if ( DOMAIN[k].equalsIgnoreCase(scod) )  return k;
        
        return UND;
    }
    
    /**
     * Giving the tag codes defined in this class (static fields),
     * converts a tag code into its corresponding string.
     * @param cod The chunk tag code.
     * @return The chunk tag. If the code is unknown it will
     * return the "UNDEFINED" string.
     */
    public static String cod2str(int cod) {
        if ( cod < 0 || cod >= DOMAIN.length )  return "UNDEFINED";
        return DOMAIN[cod];
    }


    /**
     * This main method lists the set of chunk tags and their corresponding codes.
     * It also tests the conversion methods.
     * @param args No arguments are expected.
     */
    public static void main(String[] args) {
        int[] vc= {0, 1, 2, 3, 4, 5, 6, 7};
        for (int k : vc)
            System.out.printf("   cod(%d) = %3s\n", k, cod2str(k));
        
        System.out.print("\n\n");
        String[] vsc= {"vp", "und", "np", "xpt", "pp", "", "advp", "prt"};
        for (String s : vsc)
             System.out.printf("   cod(%4s) = %d\n", s, str2cod(s));
    }
}
