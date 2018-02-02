package hultig.util;

/**
 * <p>
 * The StringInteger class defines a data unit which associates an 
 * integer value to a string. Such a data unit has several utilities,
 * like for example in storing the frequêncy of a word.
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 11:40:33 11/Out/2012
 * @author J. P. Cordeiro
 */
public class StringInteger 
{
    public String s;
    public long   n;
    
    public StringInteger(String s, long n) {
        this.s= s;
        this.n= n;
    }
    
    public String toString() {
        return s+'/'+n;
    }
}
