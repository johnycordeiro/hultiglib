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
 * <p>This class represents a phrase chunk, by storing only the chunk limits
 * and its POS label. A representation of this chunk is obtained through the
 * {@link #toString() toString()} method, which returns a string of the form
 * "POS(a, b)", where "POS" is the chunk label and "a" and "b" are
 * respectively the left and right chunk limits, in terms word position in
 * the sentence (interpreted as an sequence of words).
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public class ChunkMark
{
    /**
     * The chunk left limit, in terms of word position in the sentence.
     */
    protected int a;

    /**
     * The chunk right limit, in terms of word position in the sentence.
     */
    protected int b;

    /**
     * The chunk part-of-speech label.
     */
    private char[] pos;

    /**
     * The chunk's part-of-speech label.
     */
    protected ChunkTag chtag;

    /**
     * The constructor involves the chunk part-of-speech label and two integers
     * representing a sentence interval, from one word (first index) to the other
     * one (second index). This is so because a chunk mark can comprehend a
     * sequence of several words.
     * @param a The position of the first word.
     * @param b The position of the second word.
     * @param pos The part-of-speech chunk label.
     */
    public ChunkMark(int a, int b, String pos) {
        set(a, b, pos);
    }

    public ChunkMark(int a, int b, ChunkTag tag) {
        set(a, b, tag);
    }

    /**
     * Sets this chunk mark, through their three parameters: the left and right
     * sentence word positions, and the chunk part-of-speech label.
     * @param a The position of the left limit.
     * @param b The position of the right limit.
     * @param pos char[] The part-of-speech chunk label.
     */
    public final void set(int a, int b, String pos) {
        this.a= a;
        this.b= b;
        if ( pos == null || pos.equals("UNDEFINED") )
            this.pos= null;//new String("UNDEFINED").toCharArray();
        else
            this.pos= pos.toCharArray().clone();
    }

    public final void set(int a, int b, ChunkTag tag) {
        this.a= a;
        this.b= b;
        this.chtag= tag;
    }

    /**
     * Gives the chunk left limit, in terms of sentence word position.
     * @return The position of the left limit.
     */
    public int a() {
        return a;
    }

    /**
     * Gives the chunk right limit, in terms of sentence word position.
     * @return The position of the right limit.
     */
    public int b() {
        return b;
    }

    /**
     * Gives the chunk tag of this mark.
     * @return A string with the label of this chunk mark.
     */
    public String POS() {
        return pos != null ? new String(pos) : "UNDEFINED";
    }

    /**
     * Tests whether this chunk mark is labeled or not.
     * @return The appropriate logical value.
     */
    public boolean posUndefined() {
        if ( pos == null ) return true;
        else
            return false;
    }

    /**
     * Gives the string representation of this chunk mark.
     * @return The string that represents this chunk mark, in the form of
     * "POS(a, b)", where "POS" is the chunk label and "a" and "b" are
     * respectively the left and right chunk limits, in terms word position
     * in the sentence.
     */
    @Override
    public String toString() {
        return POS() + "(" + a + ", " + b + ")";
    }
}
