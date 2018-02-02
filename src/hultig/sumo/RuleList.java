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
package hultig.sumo;

import hultig.io.FileIN;
import hultig.util.OSArguments;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * <b>NOT YET WELL COMMENTED</b>. 
 * Represents a collection of sentence compression/transformation rules. These
 * rules were generated by the <i>Aleph</i> learning system, which is based on
 * <i>Inductive Logic Programming</i> (ILP). The learned rules are saved into
 * a file, and in this class it can be opened and their rules loaded, through
 * the method {@link #loadFromFile(java.lang.String, int) loadFromFile(String, int)}.
 * There is also a related method for loading rules from a given directory.
 * @date 20:51:15 11/Feb/2009
 * @author J. P. Cordeiro
 */
public class RuleList extends ArrayList<Rule> implements Serializable, Comparator<Rule>
{
    public static final long serialVersionUID = -7L;
    /**
     * The constant that represents undefined.
     */
    private static final int UNDEF= -1;
    
    /**
     * Defines the ascending sorting criterion.
     */
    public static final int  ASCENDING=  1;

    /**
     * Defines the descending sorting criterion constant.
     */
    public static final int DESCENDING= -1;

    /**
     * Holds the sorting criteria.
     */
    private int MODE;

    /**
     * The Sort criteria for the rules list.
     */
    public enum SortType {COVER, COUNTER};
    public SortType STYPE= SortType.COVER;

    /**
     * The default constructor.
     */
    public RuleList() {
        MODE= ASCENDING;
    }

    /**
     * Loads a set of sentence reduction rules, generated by <i>Aleph</i>,
     * from a given text file.
     * @param fname The name of the file.
     * @param mincover Loads rules having coverage greater than this parameter.
     * @return The appropriate meaningful logical value.
     */
    public boolean loadFromFile(String fname, int mincover) {
        FileIN f= new FileIN(fname);
        if ( !f.exists() ) {
            System.err.printf("\n\tERROR:  File [%s] does not exist!\n\n", fname);
            return false;
        }

        f.open();
        String line;
        StringBuilder ruleBody= null;
        int id=UNDEF, cover=UNDEF; //ii=0;
        Pattern rulePattern= Pattern.compile("Rule ([0-9]*).*Pos cover = ([0-9]*)");
        while ( (line=f.read()) != null ) {
            Matcher m= rulePattern.matcher(line);
            if (m.find()) {
                //System.out.printf("%4d   %s\n", ii++, line);
                try {
                    id = Integer.parseInt(m.group(1));
                    cover = Integer.parseInt(m.group(2));
                    ruleBody= new StringBuilder();
                } catch (NumberFormatException exc) {
                    id = UNDEF;
                    cover = UNDEF;
                    ruleBody= null;
                    System.out.printf("EXCEPTION: %s\n", exc.getMessage());
                }
                continue;
            }

            if ( ruleBody != null ) {
            //==> Reading rule body.
                line= line.trim();
                if ( line.startsWith("rule(A) :-") )
                    continue;
                if ( line.length() == 0 ) {
                    ruleBody.append("true.");//==> erroneous situation, dot mark missed.
                    add(new Rule(id,cover,ruleBody.toString()));
                    ruleBody= null; id=UNDEF; cover= UNDEF;
                    continue;
                }
                if (line.endsWith(",")) {
                    ruleBody.append(line).append(' ');
                } else if (line.endsWith(".")) {
                    //==> normal situation, rule ending with dot mark.
                    ruleBody.append(line);
                    //System.out.println("rule(A) :- " + ruleBody.toString());
                    if ( cover >= mincover )
                        add(new Rule(id,cover,ruleBody.toString()));
                    ruleBody= null; id=UNDEF; cover= UNDEF;
                }
            }
        }
        //System.out.printf("RULE LIST LENGTH: %d\n", size());
        f.close();
        return true;
    }

    /**
     * Loads sentence reduction rules from a given directory. Any file
     * having a name starting with "rset" and ending with a "txt"
     * extension, will be scanned.
     * @param dirname The directory from which to scan for rule files.
     * @param mincover Loads rules having coverage greater than this parameter.
     */
    public void loadFromDir(String dirname, int mincover) {
        File fdir= new File(dirname);
        if ( !fdir.isDirectory() ) {
            System.err.printf("\n\tThe directory name is not valid: %s\n", dirname);
            return;
        }
        String dpath= fdir.getAbsolutePath();
        String[] fnames= fdir.list();
        for (String f : fnames) {
            if ( f.startsWith("rset") && f.endsWith(".txt") ) {
                System.out.printf("READ RULE FILE: %s", dpath+'/'+f);
                loadFromFile(dpath+'/'+f, mincover);
                System.out.printf("   List Size: %d\n", this.size());
            }
        }
        System.out.println("\n");
    }


    /**
     * Compare two rules based on their coverage values.
     * @param ra The first rule.
     * @param rb The second rule.
     * @return A negative, zero, or positive value, meaning respectively
     * {@code ra < rb }, {@code ra = rb}, or {@code ra > rb}.
     */
    public int compare(Rule ra, Rule rb) {
        int xa= 0;
        int xb= 0;
        if ( STYPE == SortType.COVER ) {
            xa= ra.getCover();
            xb= rb.getCover();
        }
        else if ( STYPE == SortType.COUNTER ) {
            xa= ra.COUNTER;
            xb= rb.COUNTER;
        }

        return (xa-xb)*MODE;
    }

    /**
     * Sorts this list of reduction rules, according to the
     * specified comparator and criterion.
     */
    public void sort() {
        sort(ASCENDING);
    }


    public void sort(int mode) {
        MODE= mode;
        Collections.sort(this, this);
    }

    public void sort(int mode, SortType stype) {
        STYPE= stype;
        sort(mode);
    }

    /**
     * Eliminates duplicate rules, from this rule list.
     * @return The number of duplications removed.
     */
    public int removeDuplicateRules() {
        int contador= 0;
        for (int i = 0; i < size()-1; i++) {
            Rule ri= get(i);
            for (int j = i+1; j < size(); j++) {
                Rule rj= get(j);
                //if ( ri.entails(rj) && rj.entails(ri) ) {
                if ( ri.equivalent(rj) ) {
                    remove(j--);
                    contador++;
                }
            }
        }

        return contador;
    }


    public void printEntailments() {
        int nequiv= 0;
        int nimpli= 0;
        int n= size();
        for (int i = 0; i < n-1; i++) {
            Rule ri= this.get(i);
            String si= ri.getStrConditions();
            for (int j = i+1; j < n; j++) {
                Rule rj= this.get(j);
                String sj= rj.getStrConditions();
                boolean eij= ri.entails(rj);
                boolean eji= rj.entails(ri);
                if ( eij && eji ) {
                    System.out.printf("%20s   |==|   %s\n", si, sj);
                    nequiv++;
                }
                else if ( eij ) {
                    System.out.printf("%20s   |==    %s\n", si, sj);
                    nimpli++;
                }
                else if ( eji ) {
                    System.out.printf("%20s   |==    %s\n", sj, si);
                    nimpli++;
                }
            }
        }
        System.out.printf("\nNUM(=>): %d    NUM(<=>): %d    TOTAL: %d\n\n", nimpli, nequiv, nimpli+nequiv);
    }


    /**
     * Outputs the whole list of sentence reduction rules.
     */
    public void print() {
        for (int i = 0; i < size(); i++) {
            System.out.printf("%4d  %s\n", i+1, get(i));
        }
    }

    /**
     * Exemplifies the main operation of this class.
     * @param args The array of arguments.
     */
    public static void main(String[] args) {
        /**/
        args= new String[] {
          //"-f",  "/a/news@google/DMarcu/rset20120425150100.txt"
          //"-f",  "/a/news@google/DMarcu/rset20140115102755.txt"
            "-f",  "/Users/john/Desktop/x/rset20151010162956.txt"
        };
        /**/
        OSArguments osa= new OSArguments(args);
        if ( !osa.contains("f") && !osa.contains("d") ) {
            System.err.print("\n\tSYNTAX:  java ListFileRules "
                    + "-[f|d] <file|dir name> [-entails] "
                    + "[-cover <int>]\n\n");
            return;
        }

        int mincover= 0;
        if ( osa.contains("cover") ) {
            try {
                mincover= Integer.parseInt(osa.get("cover"));
            } catch (NumberFormatException numberFormatException) {
                mincover= 0;
            }
        }

        RuleList vrule= new RuleList();
        if ( osa.contains("f") )  vrule.loadFromFile(osa.get("f"), mincover);
        if ( osa.contains("d") )  vrule.loadFromDir( osa.get("d"), mincover);

        if ( osa.contains("entails") ) {
            vrule.printEntailments();
            //return;
        }

        vrule.sort(DESCENDING,SortType.COVER);
        vrule.print();
    }
}