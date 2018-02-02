/**
 * ***********************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2012 UBI/HULTIG All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General Public License Version 3 only ("GPL"). You may not use this file
 * except in compliance with the License. You can obtain a copy of the License at http://www.gnu.org/licenses/gpl.txt. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each file. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying information:
 *
 * "Portions Copyrighted [year] [name of copyright owner]" ************************************************************************
 */
package hultig.sumo;

import hultig.util.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>NOT YET WELL COMMENTED</b>. This class a sentence reduction rule, obtained from <i>Aleph</i>, an Inductive Logic Programming (ILP) system.
 *
 * @date 15:01:15 11/Feb/2009
 * @author J. P. Cordeiro
 */
public class Rule {

    int ID;
    private int COVER;

    private String STRCONDS;

    private LConditions VCOND;
    private String LPATTERN;
    private String MPATTERN;
    private String RPATTERN;

    private static char WORDPOSEP = '/';

    public static int ALL = 1001;
    public static int LEFT = -1;
    public static int MIDX = 0;
    public static int RIGHT = 1;

    /**
     * The size (number of words) in the middle source (X) segment.
     */
    private int DMX = -1;

    /**
     * The size (number of words) of the middle target (Y) segment.
     */
    private int DMY = 0;

    /**
     * Used for calculating the frequency of rule applicability in a set of sentences (2012/01/05).
     */
    public int COUNTER = 0;

    /**
     * Examples of pattern instantiation:<br/>
     * <pre>left(3)=the</pre>
     * <pre>right(1)=pos(dt)</pre>
     * <pre>chunk(left)=vp</pre>
     */
    private static Pattern GENPATTERN = Pattern.compile("([^(]*)\\((.*)\\)=(.*)");

    /**
     * The default constructor.
     */
    public Rule() {
        ID = -1;
        COVER = 0;
        VCOND = null;
        LPATTERN = null;
        MPATTERN = null;
        RPATTERN = null;
    }

    /**
     * A constructor based on three parameters: the rule identification, the number of learning instances covered, and the string with the rule
     * conditions.
     *
     * @param id The rule identifier.
     * @param cover The rule coverage: number of positive learning instances covered.
     * @param sconditions The string containing the conditions. It will be parsed, extracting their conditions.
     */
    public Rule(int id, int cover, String sconditions) {
        this();
        set(id, cover, sconditions);
    }

    /**
     * Defines the current instance upon three supplied instances. This method is invoked by the non-trivial constructor.
     *
     * @param id The rule identifier.
     * @param cover The rule coverage: number of positive learning instances covered by this rule.
     * @param sconditions
     */
    public final void set(int id, int cover, String sconditions) {
        if (!parseConditionString(sconditions)) {
            return;
        }
        ID = id;
        COVER = cover;

        STRCONDS = sconditions.substring(0, sconditions.length() - 1);
    }

    /**
     * Parses a string containing a sentence reduction rule. The rule conditions are identified and extracted. Internally, three regular expressions
     * are generated, representing the conditions for the three rule main regions: left, middle, and right.
     *
     * @param srule The string holding a sentence reduction rule.
     * @return The appropriate logical value: {@code true} on success.
     */
    public boolean parseConditionString(String srule) {
        if (srule == null) {
            return false;
        }
        srule = srule.trim();
        int n = srule.length();
        if (n < 1 || srule.charAt(n - 1) != '.') {
            return false;
        }
        srule = srule.substring(0, n - 1);
        String[] ruleTerms = srule.split(", ");

        VCOND = new LConditions();
        for (String t : ruleTerms) {
            String tt = transform(t);
            VCOND.add(tt);
            //System.out.printf("Add Conditions: %20s   value: %d\n", tvci, LConditions.value(tvci));
        }
        VCOND.sort();

        int i = 0;
        //[Handles the "dim:X--->Y" condition
        String cond0 = VCOND.get(0);
        //System.out.printf("CONDITION 0:... [%s]\n", cond0);
        if (cond0.startsWith("dim:")) {
            int p = cond0.indexOf("--->");
            if (p > 0) {
                try {
                    DMX = Integer.parseInt(cond0.substring(4, p));
                } catch (NumberFormatException exc) {
                    DMX = -1;
                }
                try {
                    DMY = Integer.parseInt(cond0.substring(p + 4));
                } catch (NumberFormatException exc) {
                    DMY = 0;
                }
            }
            i++;
        }
        //System.out.println("\n\n\n"+VCOND+"\n");
        //System.out.printf("DMX: %d\n", DMX);
        //Generate patterns
        i = this.generatePatternLEFT(i);
        LPATTERN = LPATTERN.trim();
        i = this.generatePatternMIDX(i);
        i = this.generatePatternRIGHT(i);
        //===> Temporary solution: 2014/01/17 01:07
        //------------------------------------------
        int p = RPATTERN.indexOf("/punct END");
        if (p > 0) {
            RPATTERN = RPATTERN.substring(0, p) + "/punct>:punct end/end";
        }
        //------------------------------------------
        return true;
    }

// XPTO 2009/04/15 18:53
// CASO - EXEMPLO: irx(A,center:x,1,pod(dt))
    /**
     * A bunch of regular expressions to transform the <i>Aleph</i>'s rule conditions into a more convenient forma. For example,
     * {@code inx(A,right,1,pos(nn))} is transformed into {@code right(1)=pos(nn)}.
     *
     * @param s A string containing the rule condition.
     * @return The transformed condition.
     */
    private String transform(String s) {
        Pattern pinx = Pattern.compile("inx\\(A,([^,]*),([0-9]*),(.*)\\)");
        Pattern pirx = Pattern.compile("irx\\(A,([^,]*),([0-9]*),(.*)\\)");
        Pattern pchk = Pattern.compile("chunk\\(A,([^,]*),(.*)\\)");
        Pattern ptdm = Pattern.compile("transfdim.A,n.([0-9]*),([0-9]*).");

        Matcher m = pinx.matcher(s);
        if (m.find()) {
            String func = m.group(1);
            String argm = m.group(2);
            String valr = m.group(3);
            int n = valr.length();
            if (valr.charAt(0) == '\'' && valr.charAt(n - 1) == '\'') {
                valr = valr.substring(1, n - 1);
            }
            return Toolkit.sprintf("%s(%s)=%s", func, argm, valr);
        }

        m = pirx.matcher(s);
        if (m.find()) {
            return Toolkit.sprintf("%s(-%s)=%s", m.group(1), m.group(2), m.group(3));
        }

        m = pchk.matcher(s);
        if (m.find()) {
            return Toolkit.sprintf("chunk(%s)=%s", m.group(1), m.group(2));
        }

        m = ptdm.matcher(s);
        if (m.find()) {
            String ts = Toolkit.sprintf("dim:%s--->%s", m.group(1), m.group(2));
            return ts;
        }

        //The input is returned, meaning no understanting.
        return s;
    }

    /**
     * Generates a regular expression that represents the sequence of rule conditions for the "left segment".
     *
     * @param start The starting point index in the {
     * @VCOND VCOND} array.
     * @return The final index position in the {
     * @VCOND VCOND} array
     */
    private int generatePatternLEFT(int start) {
        if (VCOND == null) {
            return -1;
        }

        int lastIndex = 0;
        boolean flag_word_open = false;
        StringBuilder sb = new StringBuilder("");
        for (int i = start; i < VCOND.size(); i++) {
            String cond = VCOND.get(i);
            Matcher m = GENPATTERN.matcher(cond);
            if (!m.find() || m.groupCount() < 3) {
                continue;
            }

            String func = m.group(1); //==> expected: left, chunk
            String argm = m.group(2); //==> expected: left, 1, 2, 3, ...
            String valu = m.group(3); //==> expected: nn, jj, np, vp, the, fly, etc

            //------------------------------------------------------------------------
            if (!func.equals("left") && !argm.equals("left")) {
            //------------------------------------------------------------------------
                //==> The conditions related with the left segment have already finished.
                //    Proceed for finishing the pattern related with the left segment.
                //------------------------------------------------------------------------
                if (sb.length() > 0) {
                    if (flag_word_open) {
                        sb.append(WORDPOSEP).append("[^>]*");
                        flag_word_open = false;
                    }
                    for (int j = lastIndex - 1; j > 0; j--) {
                        sb.append(" [^ >]*");
                    }
                    sb.append(">:[^ ]*");
                    LPATTERN = sb.toString();
                } else {
                    LPATTERN = "";
                }
                return i; //---> Já estava a analisar um novo.
                //------------------------------------------------------------------------
            }

            if (func.equals("chunk")) {
                if (sb.length() > 0) {
                    if (flag_word_open) {
                        sb.append(WORDPOSEP).append("[^> ]*");
                        flag_word_open = false;
                    }
                    for (int j = lastIndex - 1; j > 0; j--) {
                        sb.append(" [^> ]*");
                    }

                    sb.append(">:");
                    sb.append(valu);
                    LPATTERN = sb.toString();
                } else {
                    LPATTERN = ':' + valu;
                }
                return i + 1; //---> irá analisar o próximo.
            }

            int index = Integer.parseInt(argm);
            if (index < lastIndex) {
                if (flag_word_open) {
                    sb.append(WORDPOSEP).append("[^ >]*");
                    flag_word_open = false;
                }
                for (int j = index + 1; j < lastIndex; j++) {
                    sb.append(" [^ ]*");
                }
            }

            if (valu.startsWith("pos(")) {
                int k = valu.indexOf(')');
                String tag = valu.substring(4, k);
                if (lastIndex > index) {
                    sb.append(" [^ >]*");
                }
                sb.append(WORDPOSEP).append(tag);
                flag_word_open = false;
            } else {
                sb.append(' ').append(valu);
                flag_word_open = true;
            }

            lastIndex = index;
        }

        return VCOND.size();
    }

    /**
     * Generates the regular expression that represents the sequence of rule conditions, for the center region.
     *
     * @param start The index starting point in the VCOND array.
     * @return
     */
    private int generatePatternMIDX(int start) {
        if (VCOND == null) {
            return -1;
        }

        int last_index = 0; //==> the segment last index position.
        boolean flag_word_open = false;
        StringBuilder sb = new StringBuilder("");
        for (int i = start; i < VCOND.size(); i++) {
            String cond = VCOND.get(i);
            Matcher m = GENPATTERN.matcher(cond);
            if (!m.find() || m.groupCount() < 3) {
                continue;
            }

            String func = m.group(1);
            String argm = m.group(2);
            String valu = m.group(3);

            /**
             * Entra aqui se já estiver a processar um literal que não pertence à parte central. Normalmente deve ser um literal da região "right".
             */
            if (!func.equals("center:x") && !argm.equals("center:x")) {
                //System.out.println("last_index:....... "+last_index);
                if (sb.length() > 0) {
                    if (last_index > 0) {
                        // Faz as finalizações do pattern central.
                        if (flag_word_open) {
                            sb.append(WORDPOSEP).append("[^ >]*");
                            flag_word_open = false;
                        }
                        //System.out.printf("last_index: %d    DMX: %d\n", last_index, DMX);
                        if (last_index < DMX) {
                        //==> Situação em que temos, p.ex: "dim:4--->0" e a nossa
                            //    restrição foi para uma posição (index) inferior a 4.
                            //    Neste caso há necessidade de completar as posições
                            //    em falta com wildcard de palavra.
                            for (int k = last_index; k < DMX - 1; k++) {
                                sb.append(" [^ ]*");
                            }
                            sb.append(" [^>]*");
                        }
                        //sb.append("[^>]*>:[^ ]*"); //---> qq pos e qq chunk.
                        MPATTERN = "[^:]*:<" + sb.toString() + ">:[^ ]*"; //--> qq chunk.
                    } else {
                        if (flag_word_open) {
                            sb.append(WORDPOSEP).append("[^ >]*"); //--> qq pos.
                            flag_word_open = false;
                        }
                        //System.out.printf("LAST INDEX: ---> %d  sb: [%s]\n", last_index, sb);
                        for (int k = last_index + 1; k < 0; k++) {
                            sb.append(" [^ >]*");
                        }
                        MPATTERN = "[^:]*:<" + sb.toString() + ">:[^ ]*";
                    }
                } else {
                    //==> neste estado o sb está vazio.
                    if (DMX > 0) {
                        for (int k = 1; k < DMX; k++) {
                            sb.append("[^ ]* ");
                        }
                        sb.append("[^>]*"); //==> último wildcard do segmento central.
                    } else {
                        sb.append("[^>]*");
                    }

                    // Cria o pattern central mais geral possível.
                    MPATTERN = "[^: ]*:<" + sb.toString() + ">:[^ ]*";
                }
                return i;
            }

            //-------------------------------------------------
            //==> The chunk conditions for the middle segment.
            //-------------------------------------------------
            if (func.equals("chunk")) {
                if (flag_word_open) {
                    //==> Fecha o par w/t, assumindo uma tag qualquer.
                    sb.append(WORDPOSEP).append("[^ ]*");
                    flag_word_open = false;
                }

                if (sb.length() == 0) {
                    if (DMX > 0) {
                        for (int k = 1; k < DMX; k++) {
                            sb.append("[^ ]* ");
                        }
                        //sb.append("[^ ]* ");
                    }
                    sb.append("[^>]*");
                } else {
                    if (last_index < DMX) {
                        for (int k = last_index + 1; k < DMX; k++) {
                            sb.append(" [^ ]*");
                        }
                        sb.append(" [^>]*");
                    }
                }

                if (valu.equals("multi")) { //==> Became obsolet on 27 Oct 2011
                    MPATTERN = "[^:]*:<" + sb.toString() + ">:[^ ]*";
                } else {
                    int pa = valu.indexOf('-');
                    int pb = valu.indexOf('*');
                    if (pa > 0) {
                        String chk1 = valu.substring(0, pa);
                        String chk2 = valu.substring(pa + 1);
                        MPATTERN = chk1 + ":<" + sb.toString() + "[^>:]*>:" + chk1 + " " + chk2 + ":<" + "[^>:]*>:" + chk2;
                    } else if (pb > 0) {
                        String chk1 = valu.substring(0, pb);
                        String chk2 = valu.substring(pb + 1);
                        String mdxs = sb.toString();
                        if (mdxs.equals("[^>]*")) {
                            mdxs = ".*";
                        }
                        MPATTERN = chk1 + ":<" + mdxs + ">:" + chk2;
                    } else {
                        MPATTERN = valu + ":<" + sb.toString() + ">:" + valu;
                    }
                }
                return i + 1;
            }//------------------------------------------------

            //==> the semgment index position
            int index = Integer.parseInt(argm);
            //System.out.printf("\nfunc:[%s]   argm:[%s]   valu:[%s]   index:%d   last_index: %d\n", func, argm, valu, index, last_index);
            if (index > 0) {
            //==> Neste caso temos um exemplo de restrição esquerda, no center:x
                //==> Exemplo:  center:x(1)=the
                if (index > last_index) {
                //==> Existem espaços em falta que é necessário preencher.
                    //==> Exemplo: center:x(1)=the /\ center:x(3]=pos(jj)
                    if (flag_word_open) {
                    //==> Exemplo: "... the/" ---> lex/void. O "void" tem de
                        //==> ser substituido por uma seq regular adequada.
                        sb.append(WORDPOSEP).append("[^ ]*");
                        flag_word_open = false;
                    }
                    // Para cada posição do intervalo, uma expressão regular
                    // equivalente.
                    for (int j = last_index + 1; j < index; j++) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append("[^ ]*");
                    }
                }

                if (valu.startsWith("pos(")) {
                    int k = valu.indexOf(')');
                    String tag = valu.substring(4, k);
                    if (sb.length() > 0 && index > last_index) {
                        sb.append(' ');
                    }
                    if (index > last_index) {
                        sb.append("[^ ]*");
                    }
                    sb.append(WORDPOSEP).append(tag);
                    flag_word_open = false;
                } else {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(valu);
                    flag_word_open = true;
                }
            } else {
            // Neste caso temos um exemplo de restrição direita, no center:x
                // Exemplo:  center:x(-2)=pos(dt)
                if (last_index > 0) {
                    //O ultimo indice era relativo ao lado esquerdo de center:x    
                    sb.append("[^>]*");
                } else {
                    if (flag_word_open) {
                    // Exemplo: "... the/" ---> lex/void. O "void" tem de
                        // ser substituido por uma seq regular adequada.
                        sb.append(WORDPOSEP).append("[^ ]*");
                        flag_word_open = false;
                    }
                    // Para cada indice de intervalo, uma expressão regular 
                    // equivalente.
                    for (int j = last_index + 1; j < index; j++) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append("[^ ]*");
                    }
                }

                if (valu.startsWith("pos(")) {
                    int k = valu.indexOf(')');
                    String tag = valu.substring(4, k);
                    if (sb.length() == 0) {
                        sb.append("[^>]*");
                    } else if (sb.length() > 0 && index > last_index) {
                        sb.append(' ');
                    }
                    if (index > last_index) {
                        sb.append("[^ ]*");
                    }

                    sb.append(WORDPOSEP).append(tag);
                    flag_word_open = false;
                } else {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    } else // a primeira palavra de uma condição direita a ser introduzida.
                    {
                        sb.append("[^>]* ");
                    }
                    sb.append(valu);
                    flag_word_open = true;
                }
            }

            last_index = index;
            //System.out.printf("sb ---> [%s]\n", sb);
        } //for (int i = start; i < VCOND.size(); i++)

        if (flag_word_open) {
            sb.append(WORDPOSEP);
            flag_word_open = false;
        }

        //Poderão sobrar posições vazias no final.
        if (last_index < 0) {
            for (int k = last_index - 1; k > 0; k--) {
                sb.append(" [^ ]*");
            }
        }

        if (last_index > 0) {
            sb.append("[^>]*");
        }

        MPATTERN = "[^:]*:<" + sb.toString() + ">:[^ ]*";
        return VCOND.size();
    }

    /**
     * Generate the regular expression that represents the sequence of rule conditions, for the right region.
     *
     * @param start The index starting point in the VCOND array.
     * @return
     */
    private int generatePatternRIGHT(int start) {
        if (VCOND == null) {
            return -1;
        }

        int last_index = 0;
        boolean flag_word_open = false;
        StringBuilder sb = new StringBuilder("");
        for (int i = start; i < VCOND.size(); i++) {
            String cond = VCOND.get(i);
            Matcher m = GENPATTERN.matcher(cond);
            if (!m.find() || m.groupCount() < 3) {
                continue;
            }

            String func = m.group(1);
            String argm = m.group(2);
            String valu = m.group(3);

            if (func.equals("chunk")) {
                if (sb.length() > 0) {
                    //RPATTERN= valu + ":{" + sb.toString() + "}:" + valu;
                    RPATTERN = valu + ":<" + sb.toString();
                } else {
                    RPATTERN = valu + ':';
                }
                return i;
            }

            int n = sb.length();

            int index = Integer.parseInt(argm);
            if (last_index < index) {
                if (flag_word_open) {
                    sb.append(WORDPOSEP).append("[^ ]*");
                    flag_word_open = false;
                }
                for (int j = last_index + 1; j < index; j++) {
                    if (n > 0) {
                        sb.append(' ');
                        n++;
                    } else {
                        n = 5;
                    }
                    sb.append("[^ ]*");
                }
            }

            if (valu.startsWith("pos(")) {
                int k = valu.indexOf(')');
                String tag = valu.substring(4, k);
                if (n > 0 && index > last_index) {
                    sb.append(' ');
                }
                if (index > last_index) {
                    sb.append("[^ ]*");
                }
                sb.append(WORDPOSEP).append(tag);
                flag_word_open = false;
            } else {
                if (n > 0) {
                    sb.append(' ');
                }
                sb.append(valu);
                flag_word_open = true;
            }

            //Está na última posição.
            if (i == VCOND.size() - 1) {
                if (sb.length() > 0) {
                    //System.out.println("---->"+sb);
                    //sb.append("[^}]*}:[^ ]*");
                    RPATTERN = "[^:]*:<" + sb.toString();
                } else {
                    //RPATTERN= "[^: ]*:{[^}]*}:[^ ]*";
                    RPATTERN = ".*";
                }
                return i;
            }

            last_index = index;
        }

        return VCOND.size();
    }

    /**
     * Pattern without chunks - it is a simpler version. JPC 2009/04/26 19:51
     *
     * @param szmid
     * @return
     */
    public String genPattern(int szmid) {
        StringBuilder sbL = new StringBuilder();
        StringBuilder sbX = new StringBuilder();
        StringBuilder sbR = new StringBuilder();

        for (int i = 0; i < VCOND.size(); i++) {
            Matcher m = GENPATTERN.matcher(VCOND.get(i));
            if (!m.find() || m.groupCount() < 3) {
                continue;
            }
            String func = m.group(1);
            String argm = m.group(2);
            int karg = 0;
            if (!func.equals("chunk")) {
                karg = Integer.parseInt(argm);
            }
            String valu = m.group(3);

            if (func.equals("left") && sbL.length() < 1) {
                sbL.append(procValue(valu));
                for (int k = karg - 1; k > 0; k--) {
                    sbL.append(" [^ ]*");
                }
            }

            if (func.equals("right") && sbR.length() < 1) {
                sbR.append(procValue(valu));
                for (int k = karg - 1; k > 0; k--) {
                    sbR.insert(0, "[^ ]* ");
                }
            }

            if (func.equals("center:x") && sbX.length() < 1) {
                String svp = procValue(valu);
                for (int k = 1; k <= szmid; k++) {
                    if (k != karg) {
                        sbX.append("[^ ]*");
                    } else {
                        sbX.append(svp);
                    }

                    if (k < szmid) {
                        sbX.append(' ');
                    }
                }
            }
        }

        return '(' + sbL.toString() + ") (" + sbX.toString() + ") (" + sbR.toString() + ')';
    }

    /**
     * Pattern without chunks - it is a simpler version. JPC 2009/04/27 19:50
     *
     * <b>PROVISÓRIO</b>: Pensado para a permissa de existirem no máximo 3 literais, um para cada região. Isto deve ser uniformizado, no futuro.
     *
     * @param szmid
     * @return
     */
    public String genPatternCHK(int szmid) {
        StringBuilder sbL = new StringBuilder();
        StringBuilder sbX = new StringBuilder();
        StringBuilder sbR = new StringBuilder();

        for (int i = 0; i < VCOND.size(); i++) {
            Matcher m = GENPATTERN.matcher(VCOND.get(i));
            if (!m.find() || m.groupCount() < 3) {
                continue;
            }
            String func = m.group(1);
            String argm = m.group(2);
            int karg = 0;
            if (!func.equals("chunk")) {
                karg = Integer.parseInt(argm);
            }
            String valu = m.group(3);

            if (func.equals("left") && sbL.length() < 1) {
                sbL.append(procValueCHK(valu));
                for (int k = karg - 1; k > 0; k--) {
                    sbL.append(" [^ ]*");
                }
            }

            if (func.equals("right") && sbR.length() < 1) {
                sbR.append(procValueCHK(valu));
                for (int k = karg - 1; k > 0; k--) {
                    sbR.insert(0, "[^ ]* ");
                }
            }

            if (func.equals("center:x") && sbX.length() < 1) {
                String svp = procValueCHK(valu);
                for (int k = 1; k <= szmid; k++) {
                    if (k != karg) {
                        sbX.append("[^ ]*");
                    } else {
                        sbX.append(svp);
                    }

                    if (k < szmid) {
                        sbX.append(' ');
                    }
                }
            }

            if (func.equals("chunk")) {
                if (argm.equals("left")) {

                    sbL.append("/").append(valu);
                } else if (argm.equals("right")) {
                    sbR.append("[^/ ]*/[^/ ]*/").append(valu);
                } else if (argm.equals("center:x")) {
                    for (int k = 1; k <= szmid; k++) {
                        sbX.append("[^/ ]*/[^/ ]*/").append(valu);
                        if (k < szmid) {
                            sbX.append(' ');
                        }
                    }
                    break;
                }
            }
        }

        return '(' + sbL.toString() + ") (" + sbX.toString() + ") (" + sbR.toString() + ')';
    }

    private String procValue(String value) {
        if (value.startsWith("pos(")) {
            int k = value.indexOf(')');
            String tag = value.substring(4, k);
            return "[^ ]*/" + tag;
        } else {
            return value + "/[^ ]*";
        }
    }

    private String procValueCHK(String value) {
        if (value.startsWith("pos(")) {
            int k = value.indexOf(')');
            String tag = value.substring(4, k);
            return "[^/ ]*/" + tag + "/[^ ]";
        } else {
            return value + "/[^/ ]*/[^ ]*";
        }
    }

    /**
     * Verify if a given rule is entailed by this rule. Here, the entailment mean that the given rule is more specific than this rule, i.e at least
     * all literals from this rule are present in the set of literars, on the other rule.
     *
     * @param r
     * @return
     */
    public boolean entails(Rule r) {
        for (int i = 0; i < VCOND.size(); i++) {
            if (!r.VCOND.contains(VCOND.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifies whether two rules are equivalent, by comparing their conditions.
     *
     * @param r The other rule to compare with.
     * @return The appropriate logical value.
     */
    public boolean equivalent(Rule r) {
        for (int i = 0; i < VCOND.size(); i++) {
            if (!r.VCOND.contains(VCOND.get(i))) {
                return false;
            }
        }

        for (int i = 0; i < r.VCOND.size(); i++) {
            if (!VCOND.contains(r.VCOND.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gives the regular expression representing the conditions over a certain region, specified by code. By default the whole pattern will be
     * returned (
     * <pre>code = ALL</pre>).
     *
     * @param code The region code: LEFT, MIDX, RIGHT or ALL
     * @return The region pattern.
     */
    public String getPattern(int code) {
        if (code == LEFT) {
            return LPATTERN;
        }
        if (code == MIDX) {
            return MPATTERN;
        }
        if (code == RIGHT) {
            return RPATTERN;
        }

        // Default is "ALL"
        return '(' + LPATTERN + ") (" + MPATTERN + ") (" + RPATTERN + ')';
    }

    /**
     * Gives the support value for this rule, obtained from the induction process. That is, the number of positive learning instances from which this
     * rule was learned.
     *
     * @return The coverage value, normally greater than zero.
     */
    public int getCover() {
        return COVER;
    }

    /**
     * An ID related to the one labeled by the Aleph system, during the induction process.
     *
     * @return
     */
    public int getID() {
        return ID;
    }

    /**
     * The string conditions
     *
     * @return The string with the rule conditions, suplied to the constructor and used to shape this object. See the set/3 method.
     */
    public String getStrConditions() {
        return STRCONDS;
    }

    public String getStrFOLogic() {
        return "" + VCOND;
    }

    @Override
    public String toString() {
        String spattern = LPATTERN + "      " + MPATTERN + "      " + RPATTERN;
        return Toolkit.sprintf("RULE(%4d)  <====   (cover: %d)\n      CONDITION: %s\n        PATTERN: %s\n",
                ID, COVER, VCOND, spattern);
    }

    /**
     * MAIN - For testing.
     *
     * @param args
     */
    public static void main(String[] args) {
        /*
         Pattern patt= Pattern.compile(":np   vp:<[^>]*>:vp   np:");
         Matcher m= patt.matcher("os dados>:np   vp:<fluiram depressa>:vp   np:<naquele dia>:np");
         if ( m.find() ) {
         int a= m.start();
         int b= m.end();
         System.out.printf("\n a: %d   b: %d\n\n", a, b);
         }
        
         if ( true )  return;
         */

        ArrayList<Rule> ruleSet = new ArrayList<Rule>();
        /*
         vr.add(new Rule(28, 5, "inx(A,left,1,the), inx(A,center:x,3,pos(vbd)), inx(A,right,2,pos(dt))."));
         vr.add(new Rule(603, 14, "chunk(A,left,vp), chunk(A,right,np), inx(A,center:x,1,pos(rb))."));
         vr.add(new Rule(1269, 119, "chunk(A,left,np), chunk(A,right,np), inx(A,center:x,2,great)."));
         vr.add(new Rule(408, 15, "chunk(A,right,np), inx(A,left,3,pos(nn)), irx(A,center:x,1,the)."));
         vr.add(new Rule(499, 17, "chunk(A,right,vp), chunk(A,left,np), chunk(A,center:x,pp)."));
         vr.add(new Rule(2011, 10,
         "chunk(A,left,np), chunk(A,right,punct), chunk(A,center:x,multi), "
         + "inx(A,left,1,pos(nn)), inx(A,left,3,pos(dt)), inx(A,right,1,','), "
         + "inx(A,right,1,pos(punct))."
         ));
         * 
         */
        /**
         * /
         * vr.add(new Rule(2011, 18, "transfdim(A,n(4,0)), chunk(A,left,np), chunk(A,right,pp), chunk(A,center:x,multi), " + "inx(A,left,1,pos(nn)),
         * inx(A,right,1,pos(in))." )); vr.add(new Rule(2011, 18, "inx(A,left,1,pos(nn)), transfdim(A,n(7,0)), inx(A,center:x,2,pos(nn)), " +
         * "inx(A,center:x,3,that), chunk(A,right,vp)." )); vr.add(new Rule(2011, 19, "inx(A,left,1,pos(nn)), transfdim(A,n(5,0)),
         * chunk(A,center:x,pp), " + "chunk(A,right,vp), inx(A,center:x,2,york)." )); vr.add(new Rule(2011, 13, "chunk(A,left,np),
         * chunk(A,center:x,multi), inx(A,left,1,pos(nn)), " + "inx(A,left,2,pos(jj)), inx(A,right,1,and), inx(A,right,1,pos(cc))." ));
         *
         * vr.add(new Rule(2011, 14, "transfdim(A,n(5,0)), chunk(A,left,np), chunk(A,right,np), chunk(A,center:x,np), " + "inx(A,center:x,3,the)." ));
         * vr.add(new Rule(2011, 27, "chunk(A,left,np), chunk(A,right,pp), chunk(A,center:x,pp-np), inx(A,left,1,pos(nn)), " +
         * "inx(A,right,1,pos(in)), inx(A,center:x,2,pos(in))." )); vr.add(new Rule(2011, 28, "chunk(A,left,np), chunk(A,right,np),
         * chunk(A,center:x,np*np)." ));
        /*
         */
        ruleSet.add(new Rule(2012, 1,
                "chunk(A,left,np), chunk(A,right,punct), inx(A,right,1,'.'), inx(A,right,1,pos(punct)), "
                + "inx(A,right,2,'END'), inx(A,center:x,1,with), inx(A,center:x,1,pos(in)), "
                + "inx(A,left,1,cora), inx(A,left,2,cora2), left(1)=pos(nnp)"
                + "."
        ));
        /**/
        ruleSet.add(new Rule(2014, 1,
                "chunk(A,right,punct), inx(A,left,1,pos(nns)), inx(A,right,1,'.'), "
                + "inx(A,right,1,pos(punct)), inx(A,right,2,'END'), "
                + "inx(A,right,2,pos(end)), inx(A,center:x,2,pos(nn))."
        ));

        System.out.print("\n\n");
        for (Rule ri : ruleSet) {
            System.out.printf("%s\n", ri);
        }
        System.out.print("\n\n");

        /**
         * Rule r= vr.get(5); System.out.println("\n\n");
         * System.out.println("------------------------------------------------------------------------"); System.out.printf("RULE:....... %s\n", r);
         * System.out.printf("PATTERN:.... %s\n", r.genPatternCHK(3));
         */
    }
}

/**
 * This class represents a set of rule conditions, enabling the definition of a complete order, in this set. This complete order was used to define
 * the rule regular expression representation.
 *
 * @author JPC, 2009-02-13 19:03
 */
class LConditions extends ArrayList<String> implements Comparator<String> {

    public LConditions() {
        super();
    }

    public LConditions(String[] vconds) {
        if (vconds == null) {
            return;
        }
        for (String cond : vconds) {
            if (cond.startsWith("chunk(")
                    || cond.startsWith("left(")
                    || cond.startsWith("right(")
                    || cond.startsWith("center:x(")) {
                this.add(cond);
            }
        }
    }

    /**
     * Assigns a unique integer value, in the {1,...,300} range, for a given rule condition. This value is important for the definition of a total
     * order in the set of rule conditions. This order is fundamental in the construction of a regular expression that represents the corresponding
     * sentence reduction rule.
     *
     * @param condition Holds the rule condition.
     * @return The condition value, or the 999 value on condition parsing failure.
     */
    public static int value(String condition) {
        if (condition.startsWith("chunk(left)")) {
            return 100;
        }
        if (condition.startsWith("chunk(center:x)")) {
            return 200;
        }
        if (condition.startsWith("chunk(right)")) {
            return 300;
        }

        Pattern[] patterns = {
            Pattern.compile("dim:([0-9]*)--->([0-9]*)"),
            Pattern.compile("left.([0-9]*).="),
            Pattern.compile("center:x.([\\-0-9]*).="),
            Pattern.compile("right.([0-9]*).=")
        };

        for (int i = 0; i < patterns.length; i++) {
            Matcher m = patterns[i].matcher(condition);
            if (m.find()) {
                int a = Integer.parseInt(m.group(1));
                int b = 0; // is postag
                int k = condition.indexOf('=');
                if (k > 0 && condition.startsWith("pos(", k + 1)) {
                    b = 1;
                }

                //System.out.printf("   i: %d   a: %d   b: %d   k: %d\n", i, a, b, k);
                if (i == 0) {
                    return 5;
                }
                if (i == 1) {
                    return 100 - (2 * a - b);   //LEFT
                }
                if (i == 2) { //MIDDLE
                    if (a > 0) {
                        return 100 + 2 * a + b;
                    } else {
                        return 200 - b;  //==> ??? 25/Apr/2012 18:20 ???
                    }
                }
                if (i == 3) //RIGHT
                //return i*100 - (2*a + b);
                {
                    return 200 + 2 * a + b;
                }
            }
        }

        return 999;
    }

    public void sort() {
        Collections.sort(this, this);
    }

    /**
     * Compare two rule conditions, according to the defined value function.
     *
     * @param cond1 The first condition.
     * @param cond2 The second condition.
     * @return
     */
    public int compare(String cond1, String cond2) {
        int x1 = value(cond1);
        int x2 = value(cond2);
        if (x1 > x2) {
            return 1;
        }
        if (x1 < x2) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        if (size() < 1) {
            return "";
        }

        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                sb.append("  /\\  ");
            }
            sb.append(get(i));
            /* DEBUGING
             int x= value(get(i));
             sb.append("["+x+"]");
             */
        }

        return sb.toString();
    }

}
