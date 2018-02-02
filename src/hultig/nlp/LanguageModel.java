package hultig.nlp;

import hultig.sumo.HNgram;
import hultig.util.Toolkit;
import java.util.Locale;

/**
 * <p>
 * The LanguageModel class ...
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 14:22:02 23/jul/2014
 * @author J. P. Cordeiro
 */
public class LanguageModel 
{
    /**
     * 
     */
    private final int N;
    
    /**
     * 
     */
    HNgram ngramN;
   
    /**
     * 
     */
    HNgram ngramN1;
    
    /**
     * 
     * @param n
     * @param fN
     * @param fN1 
     */
    public LanguageModel(int n, String fN, String fN1) {
        this.N= n<2 ? 2 : n;
        
        ngramN=  new HNgram(fN,  n);
        ngramN1= new HNgram(fN1, n-1);
        if ( ngramN.isEmpty() ||  ngramN1.isEmpty() ) {
            ngramN=  null;
            ngramN1= null;
        } 
    }
    
    
    public int exclude(String pattern) {
        if ( ngramN == null ) return -1;
        int n= ngramN.exclude( pattern);
        return n+ngramN1.exclude(pattern);
    }
    
    
    public long getSum() {
        return ngramN==null? -1L : ngramN.getSum();
    }
    
    
    public long getSubSum() {
        return ngramN1==null? -1L : ngramN1.getSum();
    }
    
    
    public int getFreq(String s) {
        return ngramN==null? -1 : ngramN.freq(s);
    }
    
  
    public int getSubFreq(String s) {
        return ngramN1==null? -1 : ngramN1.freq(s);
    }
        
    
    /**
     * Computes the log-likelihood of a given word sequence,
     * based on the n-gram model stored in this object.
     * @param sws The word sequence.
     * @return The estimated log-likelihood in the {@code ]-00, 0]} interval.
     */
            
    private double probabilidade(String sws) {
        String[] vw= sws.split("  *");
        int n= vw.length;
        int[] v= new int[n+2]; v[0]= 0;
        StringBuilder sb= new StringBuilder(" ");
        for (int i = 0; i < n; i++) {
            sb.append(vw[i]).append(' ');
            v[i+1]= sb.length()-1;
        }
        v[n+1]= sb.length()-1;

        double sum= 0.0;
        double infN=  1.0 / (double)ngramN.getSum();
        double infN1= 1.0 / (double)ngramN1.getSum();
        System.out.printf("infN: %.12f    infN1: %.12f\n", infN, infN1);
        for (int k=N; k<v.length-1; k++) {
            String ngram= sb.substring(v[k-N]+1, v[k]);
            String pgram= sb.substring(v[k-N]+1, v[k-1]);

            double Fk=  1.0*ngramN.freq( ngram);  // k
            double Fk1= 1.0*ngramN1.freq(pgram);  // k-1
            
            double p=  (Fk) / (Fk1+infN1);
            sum+= Math.log(p);
            
            System.out.printf(" Count(%s) / Count(%s)  =  %f/ %f     p: %.12f\n", ngram, pgram, Fk, Fk1, p);
        }

        return sum;
    }
    
    
    public Double getLogLike(String sws) {
        if ( ngramN == null ) return null;
        String[] vw= sws.split("  *");
        return getLogLike(vw);
    }
        
    
    public Double getLogLike(String[] vw) {
        if ( ngramN == null || vw == null ) return null;
        int n= vw.length;
        int[] v= new int[n+2]; v[0]= 0;
        StringBuilder sb= new StringBuilder(" ");
        for (int i = 0; i < n; i++) {
            sb.append(vw[i]).append(' ');
            v[i+1]= sb.length()-1;
        }
        v[n+1]= sb.length()-1;

        double p, sum= 0.0;
        double infN=  1.0 / (double)ngramN.getSum();
        double infN1= 1.0 / (double)ngramN1.getSum();
        //System.out.printf("infN: %.12f    infN1: %.12f\n", infN, infN1);
        for (int k=N; k<v.length-1; k++) {
            String ngram= sb.substring(v[k-N]+1, v[k]);
            String pgram= sb.substring(v[k-N]+1, v[k-1]);

            double Fk=0.0, Fk1= 1.0*ngramN1.freq(pgram);      // k-1
            if ( Fk1 == 0 )
                p= infN1;
            else {
                Fk=  1.0*ngramN.freq( ngram);  // k
                p=  (Fk + infN) / Fk1;
            }
            sum+= Math.log(p);           
            //System.out.printf(" Count(%s) / Count(%s)  =  %f/ %f     p: %.12f\n", ngram, pgram, Fk, Fk1, p);
        }

        return sum;// Math.sqrt(vw.length);        
    }
    
    
    public Double getWeightedLogLike(String sws) {
        if ( ngramN == null ) return null;
        String[] vw= sws.split("  *");
        int m= vw.length;
        return -1.0 *  m / getLogLike(vw);
    }
    

    /**
     * Computes the likelihood of a given word sequence,
     * based on the n-gram model stored in this object.
     * @param sws The word sequence.
     * @return The estimated likelihood in the {@code [0,1]} interval, or
     * an error code: -101.0;
     */
    public double probability(String sws) {
        if ( ngramN == null ) return -101.0;
        return Math.exp(probabilidade(sws));
    }

    
    
    /**
     * The {@code main} is used for demonstration. It creates an instance
     * of this class by loading a given table of a 4-gram model of
     * part-of-speech tags. Afterwards, tag sequence likelihood is
     * calculated.
     * @param args No arguments are expected.
     */
    public static void main(String[] args) {
        //if ( test201110191137() ) return;

        int n= 2; //==> bigrams
        System.out.printf("LOAD LANGUAGE MODEL FOR %d-GRAMS ...", n);
        String[] hngramFiles= {
            "/Users/john/Research/Lab/POSCORP/POS2.wngram",
            "/Users/john/Research/Lab/POSCORP/POS1.wngram",
        };
        LanguageModel posModel= new LanguageModel(n, hngramFiles[0], hngramFiles[1]);
        System.out.println(" OK\n");
        //System.out.printf("SIZE(N=%d):.......... %8d   SUM: %d\n", hngram.size(), hngram.getSum());
        //int nx= posModel.exclude(" \\. ");
        //System.out.printf("SUM(%d-Gram) = %d\n", n, posModel.getSum());
        
        String[] vs= {
            "dt nn",
            "dt jj nn",
            "dt jj nn vbp",
            /**/
            "dt nn nns vbn in vbn jj nn ",
            "wp md vb wp md vb nns in nn nn pos nn .",
            "wp md vb nns in nn nn pos nn .",
            "dt vb nns in nn vbp dt jj jj nn cc nn nns in nn in",
            "dt jj nns in nn vbp dt jj jj nn cc nn nns in nn in",
            "dt jj nns in nn vbp dt       nn cc nn nns in nn in",
            "dt jj nns in nn vbp dt jj jj     cc nn nns in nn in",
            "bgn dt nn",
            "    dt nn",
            "xyz dt nn",
            "xy yx yz",
            "dt nn nn . end",
            "dt nn nn",
            /**/
        };

        System.out.println();
        for (String v : vs) {
            if (v.length() < 1) {
                continue;
            }
            System.out.printf("%12.7f  =  -m/LogLikeP2 { <%s> }\n", posModel.getWeightedLogLike(v), v);
        }
        System.out.println();
        
        for (;;) {
            System.out.println();
            System.out.print("INPUT$> ");
            String s= Toolkit.readLn();
            if ( s.length() < 1 )  break;
            System.out.printf(Locale.US, "        -m / LogLikeP2  =  %15.12f\n", posModel.getWeightedLogLike(s));
        }
    }
}
