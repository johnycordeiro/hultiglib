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


import java.util.Locale;


/**
 * <b>NOT YET WELL COMMENTED</b>.
 * @date 13:04:21 7/Jun/2008
 * @author J. P. Cordeiro
 */
public class MatrixT
{
    private double[][] A;
    
    
    public MatrixT(int N) {
        A= new double[N][];
        for (int i=0; i<N; i++) {
            A[i]= new double[N-i];
            for (int j=0; j<A[i].length; j++)  A[i][j]= 0.0;
        }
    }
    
    
    public int ordN() {
        return A.length;
    }
    
    
    public double get(int i, int j) {
        return i>j ? A[j][i-j] : A[i][j-i];
    }
    
    
    public double[] getLine(int i) {
        if ( i < 0 || i >= ordN() )  return null;
        
        double[] v= new double[ordN()];
        for (int j=0; j<ordN(); j++)  v[j]= get(i,j);
        
        return v;
    }
    
    
    public double[] getColumn(int j) {
        return getLine(j);
    }
    
    
    public void set(int i, int j, double x) {
        if ( i > j )
            A[j][i-j]= x;
        else
            A[i][j-i]= x;
    }
    
    
    public void inc(int i, int j, double x) {
        set(i, j, get(i,j)+x);
    }
    
    
    public double sum() {
        double sum= 0.0;
        for (int i=0; i<ordN(); i++) {
            sum+= A[i][0];
            for (int j=i+1; j<ordN(); j++)
                sum+= 2*A[i][j-i];
        }
        
        return sum;
    }
    
    
    public double[] sumLines() {
        double[] v= new double[ordN()];
        for (int i=0; i<ordN(); i++) {
            v[i]= 0.0;
            for (int j=0; j<ordN(); j++)
                v[i]+= get(i,j);
        }
        
        return v;
    }


    public double mean() {
        return sum()/(ordN()*ordN());
    }


    public int getNumOfNonZeros() {
        int counter= 0;
        for (int i=0; i<ordN(); i++)
            for (int j=i+1; j<ordN(); j++)
                if ( get(i,j) != 0 )
                    counter++;
        return counter;
    }


    public int[] getVecNumOfNonZeros() {
        int[] v= new int[ordN()];
        for (int i=0; i<ordN(); i++) {
            v[i]= 0;
            for (int j=0; j<ordN(); j++)
                if ( get(i,j) != 0 )
                    v[i]++;
        }

        return v;
    }
    
    
    public void print(String sfmt) {
        for (int i=0; i<ordN(); i++) {
            for (int j=0; j<ordN(); j++)
                System.out.printf(Locale.US, sfmt, get(i,j));
            System.out.println();
        }
    }


    /**
     * MAIN - For testing.
     * @param args
     */
    public static void main(String[] args) {   
        MatrixT A= new MatrixT(5);
        for (int i=0; i<A.ordN(); i++)
            for (int j=i; j<A.ordN(); j++) {
                A.set(i, j, (i+j));
                A.inc(i, j, 0.01);
            }
        
        System.out.println("\nTRIANGULAR MATRIX\n");
        A.print("%8.5f ");
        System.out.println();
        System.out.printf("SUM: %f   MEAN: %f\n\n", A.sum(), A.mean());
        
        System.out.println("V SUM LINES");
        double[] vs= A.sumLines();
        for (int i=0; i<vs.length; i++)
            System.out.printf(Locale.US, "%8.5f ", vs[i]);
        
        System.out.println("\n");
    }
}
