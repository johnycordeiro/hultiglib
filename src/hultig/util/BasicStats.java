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

/**
 * <p>
 * The BasicStats class represents basic statistics about a certain
 * numeric sequence. It was thought to compute basic statistic
 * quantities, like mean and variance, iteratively for a flow of
 * numeric quantities.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 15:09:55 10/Jan/2012
 * @author J. P. Cordeiro
 */
public class BasicStats 
{
    /**
     * The number of values processed, for example
     * the size of a given sample.
     */
    private long N;

    /**
     * Holds the sum of the so far processed numbers. 
     */
    private double SUM;

    /*
     * Holds the sum of the square of the numbers being
     * introduced/processed so far.
     */
    private double SUM2;


    private double MIN;


    private double MAX;


    /**
     * Constructor.
     */
    public BasicStats() {
        N= 0L;
        SUM= 0.0;
        SUM2= 0.0;
    }

    /**
     * Adds a new double value to this statistics
     * @param xi The value to add.
     */
    public void addValue(double xi) {
        N++;
        SUM+= xi;
        SUM2+= xi*xi;
        if ( N == 1 ) {
            MIN= xi;
            MAX= xi;
        }
        else {
            if ( xi > MAX )   MAX= xi;
            else if(xi < MIN) MIN= xi;
        }
    }

    /**
     * Adds a new int value to this statistics.
     * @param xi The value to add.
     */
    public void addValue(int xi) {
        addValue((double)xi);
    }
    
    /**
     * Rescale the sample, by transforming their main
     * variables.
     * @param a 
     */
    public void multiplyAllValuesBy(double a) {
        MIN*= a;
        MAX*= a;
        SUM*= a;
        SUM2*= (a*a);
    }

    /**
     * Gives the number of values computed for this
     * statistics.
     * @return The number of values.
     */
    public long getNumValues() {
        return N;
    }

    /**
     * Gives the Mean value for this statistics.
     * @return The Mean value.
     */
    public double getMean() {
        return SUM/(1.0*N);
    }

    /**
     * Gives the Variance value for this statistics.
     * @return The Variance value.
     */
    public double getVariance() {
        return (N*SUM2-SUM*SUM) / (1.0*N*(N-1));
    }

    /**
     * Gives the Standard Deviation for this statistics.
     * @return The Standard Deviation value.
     */
    public double getStandDev() {
        return Math.sqrt(getVariance());
    }

    /**
     * Gives the minimum value added.
     * @return The minimum value.
     */
    public double getMin() {
        return MIN;
    }

    /**
     * Gives the maximum value added.
     * @return The maximum value.
     */
    public double getMax() {
        return MAX;
    }

    /**
     * Outputs the main statistic parameters.
     */
    public void print(String varname) {
        System.out.printf(
                "%10s   --->   N: %d    Mean: %f    Standard Deviation: %f\n",
                varname,
                getNumValues(),
                getMean(),
                getStandDev()
                );
    }

    /**
     * For testing/demonstration we use two samples of seven
     * numbers which give rise to near the same mean but
     * very different standard deviation.
     * @param args
     */
    public static void main(String[] args) {
        BasicStats salary= new BasicStats();
        salary.addValue(120.3);
        salary.addValue(140.5);
        salary.addValue(210.1);
        salary.addValue(50);
        salary.addValue(398);
        salary.addValue(391);
        salary.addValue(27);
        salary.print("salary");

        BasicStats velocity= new BasicStats();
        velocity.addValue(191);
        velocity.addValue(188);
        velocity.addValue(197);
        velocity.addValue(180);
        velocity.addValue(195);
        velocity.addValue(179);
        velocity.addValue(210);
        velocity.print("velocity");
    }
}
