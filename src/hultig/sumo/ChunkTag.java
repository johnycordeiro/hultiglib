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


/**
 * Represents the Penn Treebank tags at a phrase level, on a shallow parsed
 * sentence.
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 18:24:06 20/Set/2011
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public enum ChunkTag
{
    /**
     * Noun Phrase.
     */
    NP("Noun Phrase"),

    /**
     * Verb Phrase.
     */
    VP("Verb Phrase"),

    /**
     * Prepositional Phrase.
     */
    PP("Prepositional Phrase"),
    
    /**
     * Adverb Phrase.
     */
    ADVP("Adverb Phrase"),
    
    /**
     * Adjective Phrase.
     */
    ADJP("Adjective Phrase"),

    /**
     * Conjunction Phrase.
     */
    CONJP("Conjunction Phrase"),

    /**
     * Fragment
     */
    FRAG("Fragment"),

    /**
     * Interjection. Corresponds approximately to the part-of-speech tag UH.
     */
    INTJ("Interjection. Corresponds approximately to the part-of-speech tag UH"),

    /**
     * List marker. Includes surrounding punctuation.
     */
    LST("List marker. Includes surrounding punctuation"),

    /**
     * Not a Constituent; used to show the scope of certain prenominal modifiers
     * within an NP.
     */
    NAC("Not a Constituent; used to show the scope of certain prenominal modifiers within an NP"),

    /**
     * Used within certain complex NPs to mark the head of the NP. Corresponds
     * very roughly to N-bar level but used quite differently.
     */
    NX("Used within certain complex NPs to mark the head of the NP. Corresponds very roughly to N-bar level but used quite differently"),

    /**
     * Parenthetical.
     */
    PRN("Parenthetical"),

    /**
     * Particle. Category for words that should be tagged RP.
     */
    PRT("Particle. Category for words that should be tagged RP"),

    /**
     * Quantifier Phrase (i.e. complex measure/amount phrase); used within NP.
     */
    QP("Quantifier Phrase (i.e. complex measure/amount phrase); used within NP"),

    /**
     * RRC - Reduced Relative Clause.
     */
    RRC("Reduced Relative Clause"),

    /**
     * Unlike Coordinated Phrase.
     */
    UCP("Unlike Coordinated Phrase"),

    /**
     * Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot.
     */
    WHADJP("Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot."),

    /**
     * Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing
     * the 0 complementizer) or lexical, containing a wh-adverb such as how or why.
     */
    WHAVP("Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing a wh-adverb such as how or why"),

    /**
     * WHNP - Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing
     * the 0 complementizer) or lexical, containing some wh-word, e.g. who, which book,
     * whose daughter, none of which, or how many leopards.
     */
    WHNP("Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing some wh-word, e.g. who, which book, whose daughter, none of which, or how many leopards"),

    /**
     * WHPP - Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase
     * (such as of which or by whose authority) that either introduces a PP gap or is
     * contained by a WHNP.
     */
    WHPP("Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase (such as of which or by whose authority) that either introduces a PP gap or is contained by a WHNP");
    
    /**
     * Stores the a description of the tag.
     */
    private final String description;

    ChunkTag(String sdescr) {
        this.description= sdescr;
    }

    public String getDescription() {
        return description;
    }

    public static ChunkTag parse(String sctag) {
        if ( sctag == null )  return null;

        try {ChunkTag t= valueOf(sctag); return t;}
        catch (IllegalArgumentException exc) {return null;}
    }


    /**
     * Prints the whole domain, each element with its corresponding 
     * textual description.
     * @param args
     */
    public static void main(String[] args) {
        int k=1;
        int[] v= new int[values().length];

        System.out.println("[CHUNK TAGS LISTING]");
        for (ChunkTag t : values()) {
            v[t.ordinal()] = 1000+k;
            System.out.printf("TAG(%02d):  %6s  ---> %s\n", k++, t, t.getDescription());
        }

        System.out.println("\n\n[ORDINAL VALUES]");
        for (int i = 0; i < v.length; i++) {
            System.out.printf("v(%02d) = %d   (%s)\n", i, v[i], ChunkTag.values()[i]);
        }
    }
}