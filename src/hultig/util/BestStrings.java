package hultig.util;


import java.util.ArrayList;

/**
 * <p>
 * The BestStrings class ...
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 14:59:05 26/Jun/2013
 * @author J. P. Cordeiro
 */
public class BestStrings extends ArrayList<StringDouble> 
{
    int sizeMAX;
    
    /**
     * Constructor.
     */
    public BestStrings(int size) {
        if ( size < 3 ) size= 3;
        this.sizeMAX= size;
    }
    
    public void insert(StringDouble sd) {
        int i=0;
        boolean inserted= false;
        while ( i < size() ) {
            StringDouble sdi= get(i);
            if ( sd.x == sdi.x ) {
                if ( sd.s.equals(sdi.s) ) {
                    inserted= true;
                    break;
                }
            }
            if ( sd.x > sdi.x ) {
                this.add(i, sd);
                inserted= true;
                break;
            }
            i++;
        }
        if ( !inserted ) this.add(sd);
        while ( size() > sizeMAX ) {
            //System.out.printf("DELETE AT POS: %d\n", size()-1);
            this.remove(size()-1);
        }
    }
    
    
    public StringDouble[] getStrings() {
        StringDouble[] vsd= new StringDouble[size()];
        vsd= toArray(vsd);
        return vsd;
    }
    
    
    public void print() {
        for (int i = 0; i < this.size(); i++) {
            System.out.printf("%12.7f   %s\n", get(i).x, get(i).s);
        }
    }
    

    /**
     * @param args
     */
    public static void main(String[] args) {
        BestStrings bs= new BestStrings(3);
        bs.insert(new StringDouble("Coimbra", 7.45));
        bs.insert(new StringDouble("Porto",   9.61));
        bs.insert(new StringDouble("Faro",    1.57));
        bs.insert(new StringDouble("Lisboa",  2.45));
        bs.insert(new StringDouble("Braga",   9.73));
        bs.insert(new StringDouble("Beja",    0.73));
        bs.print();
        
        StringDouble[] vsd= bs.getStrings();
        for (StringDouble sd : vsd) {
            System.out.println(sd.s);
        }
    }
}
