package hultig.align;

import hultig.sumo.CorpusIndex;
import hultig.util.StringPair;
import hultig.util.Toolkit;

/**
 * Date: 2015/11/10
 * @author JPC
 */
public class AlignCommons 
{
    public static StringPair fromCodesToStringAlignments(int[][] codes, CorpusIndex dictionary) {
        StringPair spair= new StringPair();
        
        StringBuilder sa = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codes[0].length; i++) {
            int ka = codes[0][i];
            int kb = codes[1][i];

            String wa = "", wb = "";
            if (dictionary != null) {
                if (ka >= 0) {
                    wa = dictionary.get(ka);
                }
                if (kb >= 0) {
                    wb = dictionary.get(kb);
                }
            }
            else {
                wa = "" + ka;
                wb = "" + kb;
            }

            int na = wa.length();
            int nb = wb.length();
            int n = Math.max(na, nb);

            if (na == 0 && nb == 0) { // ===> This must be a strange case
                wa = "_x_";
                wb = "_x_";
            }
            else if (na == 0) {
                wa = Toolkit.underscore(nb);
            }
            else if (nb == 0) {
                wb = Toolkit.underscore(na);
            }
            else { // <--- na != 0 AND nb != 0
                if (na < nb) {
                    wa = Toolkit.padRight(wa, nb);
                }
                if (na > nb) {
                    wb = Toolkit.padRight(wb, na);
                }
            }
            sa.append(wa).append(' ');
            sb.append(wb).append(' ');
        }
        
        spair.setS1(sa.substring(0, sa.length() - 1));
        spair.setS2(sb.substring(0, sb.length() - 1));
        return spair;
    }
}
