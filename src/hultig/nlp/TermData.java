package hultig.nlp;

import java.io.Serializable;

/**
 * <p>
 * The TermData class ...
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 11:39:26 11/Out/2012
 * @author J. P. Cordeiro
 */
public class TermData implements Serializable, Comparable<TermData>
{
    public static final long serialVersionUID = 7L;
    
    private String term;
    private Integer termFrequency;
        
    HashString neighboursRight;
    
    /**
     * Constructor.
     */
    public TermData(String term) {
        this.term= term;
        this.termFrequency= 0;
        this.neighboursRight= new HashString();
    }

    public String getTerm() {
        return term;
    }

    public Integer getTermFrequency() {
        return termFrequency;
    }

    public HashString getNeighboursRight() {
        return neighboursRight;
    }
    
    
    public void countNeighbour(String term) {
        neighboursRight.countKey(term);
        termFrequency++;
    }
    
    
    public int compareTo(TermData o) {
        return o.getTermFrequency() - getTermFrequency();
    }    
    
    
    public void printBestNeighbours(int n) {
        if ( n < 1 ) {
            n= 10;
        }
        String[] vs= neighboursRight.getMostFrequent(n);
        if (vs != null) {
            System.out.printf("T:[%s]   TF:[%d]\n", this.term, this.getTermFrequency());
            for (int i = 0; i < vs.length; i++) {
                String[] vvs= vs[i].split("/");
                double prob= 100.0 * Double.parseDouble(vvs[2].replace(',', '.'));
                System.out.printf(" %15s   %5s   %6.3f%%\n", vvs[0], vvs[1], prob);
            }
        }
    }
    

    /**
     * @param args
     */
    public static void main(String[] args) {
        TermData t= new TermData("the");
        t.countNeighbour("car");
        t.countNeighbour("man");
        t.countNeighbour("car");
        t.countNeighbour("car");
        t.countNeighbour("moon");
        t.printBestNeighbours(10);
    }
}
