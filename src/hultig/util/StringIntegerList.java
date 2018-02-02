package hultig.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * <p>
 * The StringIntegerList class ...
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 21:58:30 12/Out/2012
 * @author J. P. Cordeiro
 */
public class StringIntegerList extends ArrayList<StringInteger> implements Comparator<StringInteger>
{
    long sumValues;
    
    /**
     * Constructor.
     */
    public StringIntegerList() {
        super();
        sumValues= 0L;
    }
    
    
    public boolean add(StringInteger si) {
        if ( si == null ) {
            return false;
        }
        
        sumValues+= si.n;
        return super.add(si);
    }
    
    
    public void add(int index, StringInteger si) {
        if ( si == null || index < 0 || index > size()-1 ) {
            return;
        }
     
        sumValues+= si.n;
        super.add(index, si);
    }    


    /**
     * @param args
     */
    public static void main(String[] args) {
    }

    
    public int compare(StringInteger o1, StringInteger o2) {
        return (int)(o2.n - o1.n);
    }
    
    
    public void sort() {
        Collections.sort(this, this);
    }
}
