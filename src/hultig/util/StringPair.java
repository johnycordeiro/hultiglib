package hultig.util;

/**
 *
 * Date: 2015/11/10
 * @author JPC
 */
public class StringPair {
    private String S1;
    private String S2;

    
    public StringPair() {
        this.S1 = "";
        this.S2 = "";
    }
    
    public StringPair(String S1, String S2) {
        this.S1 = S1;
        this.S2 = S2;
    }

    public String getS1() {
        return S1;
    }

    public void setS1(String S1) {
        this.S1 = S1;
    }

    public String getS2() {
        return S2;
    }

    public void setS2(String S2) {
        this.S2 = S2;
    }
}
