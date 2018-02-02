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
package hultig.util;

import java.util.Comparator;
import java.util.Locale;

/**
 * <p>Represents an item from a matrix, with its (i,j)
 * coordinates its respective value (Aij).
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: UBI/HULTIG</p>
 *
 * @author JPC
 * @version 1.0
 */
public class MatrixItem implements Comparator {
    private int i;
    private int j;
    private double aij;

    /**
     * Creates a matrix object.
     * @param i The i-th (line) matrix position.
     * @param j The j-th (column) matrix position.
     * @param x double
     */
    public MatrixItem(int i, int j, double aij) {
        this.i= i;
        this.j= j;
        this.aij= aij;
    }

    /**
     * Gives the matrix i-th (line reference) position, for this
     * item.
     * @return The line reference for this item.
     */
    public int i() {
        return i;
    }

    /**
     * Gives the matrix j-th (column reference) position, for this
     * item.
     * @return The column reference for this item.
     */
    public int j() {
        return j;
    }

    /**
     * Gives the matrix (i,j) position value.
     * @return double
     */
    public double aij() {
        return aij;
    }

    /**
     * The comparator used for sorting.
     * @param o1 The first object.
     * @param o2 The second object.
     * @return Zero if the objects are equal, -1 if o1 &lt; o2,
     * or 1 if o1 &gt; o2.
     */
    public int compare(Object o1, Object o2) {
        MatrixItem a= (MatrixItem)o1;
        MatrixItem b= (MatrixItem)o2;
        if ( a.aij == b.aij ) return 0;
        if ( a.aij < b.aij ) return -1;
        return 1;
    }

    /**
     * Converts this item to a string format.
     * @return The string representing this item.
     */
    public String toString() {
        return String.format(Locale.US, "<%2d, %2d, %f>", i, j, aij);
    }
}
