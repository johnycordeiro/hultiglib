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

import hultig.sumo.Word;

import java.util.Arrays;

import java.io.PrintStream;
import java.util.ArrayList;


/**
 * A collection of static tools to handle matrix arrays</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: UBI/HULTIG</p>
 *
 * @author JPC
 * @version 1.0
 */
public class Matrix {

    /**
     * Given two generic vectors and an assignment function, computes the best possible assignment
     * among the two vector elements.
     * @param u Vector
     * @param v Vector
     * @param f Assignment
     * @return Vector
     */
    public static<T> ArrayList<MatrixItem> bestAssignment(ArrayList<T> u, ArrayList<T> v, AssignFunction<T> f) {
        if (u == null || v == null) {
            return null;
        }

        ArrayList<MatrixItem> vbest = new ArrayList<MatrixItem> ();
        int m = u.size();
        int n = v.size();
        if (m == 0 || n == 0) {
            return vbest;
        }

        //double[][] A= new double[m][n];
        ArrayList<MatrixItem> vA = new ArrayList<MatrixItem> (m * n);
        for (int i = 0; i < m; i++) {
            T ui = u.get(i);
            for (int j = 0; j < n; j++) {
                T vj = v.get(j);
                double aij = f.value(ui, vj);
                MatrixItem Aij = new MatrixItem(i, j, aij);

                boolean inserted = false;
                for (int k = 0; k < vA.size(); k++) {
                    if (aij > vA.get(k).aij()) {
                        vA.add(k, Aij);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    vA.add(Aij);
                }
            }
        }

        return selectBestAssignment(vA, Math.min(m, n));
    }

    /**
     * Given a matrix &lt;index(i,j), value&gt; vector, select the n best
     * exclusive assignments.
     * @param v Vector
     * @param n 
     * @return Vector
     */
    public static ArrayList<MatrixItem> selectBestAssignment(ArrayList<MatrixItem> v, int n) {
        ArrayList<MatrixItem> vbest = new ArrayList<MatrixItem> ();

        for (int k = 0; k < v.size() && vbest.size() < n; k++) {
            MatrixItem a = v.get(k);
            if (a.aij() == 0.0) {
                break;
            }

            boolean unbound = true;
            for (int r = 0; r < vbest.size(); r++) {
                MatrixItem b = vbest.get(r);
                if (a.i() == b.i() || a.j() == b.j()) {
                    unbound = false;
                    break;
                }
            }

            if (unbound) {
                vbest.add(a);
            }
        }

        return vbest;
    }

    /**
     * A bunch of tests for this class.
     * @param args String[]
     */
    public static void main(String[] args) {
        String[] sa = {"correr", "amar", "saltar", "guardar", "falar", "comer", "pensar"};
        String[] sb = {"correu", "amou", "saltaram", "guardasse", "falarás", "come", "pensava"};
        ArrayList<String> va = new ArrayList<String> (Arrays.asList(sa));
        ArrayList<String> vb = new ArrayList<String> (Arrays.asList(sb));
        AssignStrings f = new AssignStrings();
        ArrayList<MatrixItem> vbest = bestAssignment(va, vb, f);

        o.println(Toolkit.sline('-', 80));
        o.println("va ---> " + Arrays.toString(sa));
        o.println("vb ---> " + Arrays.toString(sb));
        o.println(Toolkit.sline('-', 80));
        for (MatrixItem item : vbest) {
            o.printf("%s   --->   <%s, %s>\n", item.toString(), sa[item.i()], sb[item.j()]);
        }
        o.println(Toolkit.sline('-', 80));

        o.print("\n\n\n");

        Integer[] xa = {40, 5, -3, -120, -180, 1502, 770};
        Integer[] xb = {1000, 1500, -175, -119, 7, 2};
        ArrayList<Integer> vxa = new ArrayList<Integer> (Arrays.asList(xa));
        ArrayList<Integer> vxb = new ArrayList<Integer> (Arrays.asList(xb));
        AssignInteger g = new AssignInteger();
        vbest = bestAssignment(vxa, vxb, g);

        o.println(Toolkit.sline('-', 80));
        o.println("va ---> " + Arrays.toString(xa));
        o.println("vb ---> " + Arrays.toString(xb));
        o.println(Toolkit.sline('-', 80));
        for (MatrixItem item : vbest) {
            o.printf("%s   --->   <%s, %s>\n", item.toString(), xa[item.i()], xb[item.j()]);
        }
        o.println(Toolkit.sline('-', 80));

    }

    private static PrintStream o= System.out;
}

/**
 *
 * <p>Title: Assign Function</p>
 *
 * <p>Used for prototypal experimentation, in main(.)
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: UBI/HULTIG</p>
 *
 * @author JPC
 * @version 1.0
 */
class AssignStrings implements AssignFunction<String> {
    public double value(String sa, String sb) {
        return Word.distSeqMax(sa, sb);
    }
}

/**
 *
 * <p>Title: </p>
 *
 * <p>Used for prototypal experimentation, in main(.)
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: UBI/HULTIG</p>
 *
 * @author JPC
 * @version 1.0
 */
class AssignInteger implements AssignFunction<Integer> {
    public double value(Integer A, Integer B) {
        double a= A.doubleValue();
        double b= B.doubleValue();
        double y= Math.exp(2.0 - 0.01*Math.abs(a-b));
        return y;
    }
}
