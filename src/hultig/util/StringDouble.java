package hultig.util;

import java.io.Serializable;

/**
 * <p>
 * The StringDouble class defines a data unit which associates a 
 * number (double) to a string. Such a data unit has several utilities,
 * like for example in assigning a relevance value to a given word, in
 * a text document.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 11:42:05 11/Out/2012
 * @author J. P. Cordeiro
 */
public class StringDouble implements Serializable 
{
    public static final long serialVersionUID = 7L;
    public static final byte SIMPLE= 0;
    public static final byte COMPLETE= 1;
    public static int toStringMode= COMPLETE;
    
    public String s;
    public double x;
    
    public StringDouble(String s, double x) {
        this.s= s;
        this.x= x;
    }
    
    public String toString() {
        if ( toStringMode == COMPLETE )
            return s+'/'+x;
        else
            return s;
    }
}
