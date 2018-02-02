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
import hultig.io.FileOUT;
import hultig.io.FileX;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import hultig.util.Toolkit;
import hultig.util.CronoSensor;
import hultig.util.OSArguments;



/**
 * <b>NOT YET WELL COMMENTED</b>.
 * @date 22:38:55 18/Set/2008
 * @author J. P. Cordeiro
 */
public class XBubbleList extends ArrayList<XBubble> implements Serializable, Comparator<XBubble>
{
    public static final long serialVersionUID = -2118567303945736768L;
    private static final String DOMAIN=
            "{UNDEF, VOID, SYT0, CC, CD, DT, EX, FW, IN, JJ, JJR, JJS, LS, MD, NN, NNS, NNP, NNPS, " +
            "PDT, POS, PRP, PRP$, RB, RBR, RBS, RP, SYM, TO, UH, VB, VBD, VBG, VBN, VBP, VBZ, WDT, " +
            "WP, WP$, WRB}";
           
    public static int ARFF= 1;
    public static int C45=  2;
    public static int ILP=  3;
    public int FORMAT= ARFF;
    
    private static Toolkit to;
    private POSType postype;
 
    
    public XBubbleList() {
        postype= null;
    }


    public XBubbleList(POSType pos) {
        postype= pos;
    }
    
   /**
    * A ordenação baseia-se na ordem vectorial númerica, definida pelos XBubble.
    * @param bx A primeira bolha.
    * @param by A segunda bolha.
    * @return Devolve:
    * <pre>
    *   -1 se bx < by
    *    1 se bx > by
    *    0 se bx = by
    * </pre>
    */
    @Override
    public int compare(XBubble bx, XBubble by) {
        int[] x= bx.getOrdArray(postype);
        int[] y= by.getOrdArray(postype);
        int nx= x.length;
        int ny= y.length;
        int n= Math.min(nx, ny);
        
        for (int i=0; i<n; i++) {
            if ( x[i] < y[i] ) return -1;
            if ( x[i] > y[i] ) return  1;
        }
        
        return 0;
    }

    /**
     * Add element to the list, maintaining the order and discarding
     * repeated elements.
     * @param xb
     */
    public void addOrd(XBubble xb) {
        if ( xb == null )  return;

        for (int i = 0; i < size(); i++) {
            int cmp= compare(xb, get(i));
            if ( cmp == 0 )
                return;
            else if ( cmp > 0 ) {
                this.add(i, xb);
                return;
            }
        }
        add(xb);

    }
    
    public POSType getPOSType() {
        return postype;
    }
    
    
    /**
     * Ordena esta lista em ordem decrescente, de acordo com a ordem definida 
     * no método compar(,).
     */
    public void sort() {
        Collections.sort(this, this);
    }
    
    
    /**
     * Tira as linhas em duplicado, depois de ordenar a lista, invocando o "sort()".
     */
    public void removeDuplicate() {
        sort();
        for (int i=0, j; i<size()-1; i++) {
            //System.out.printf("i: %3d ", i);
            XBubble xi= get(i);
            j=i+1;
            while ( j<size() ) {
                if ( xi.equal(get(j)) ) {
                    remove(j);
                    //System.out.printf(" => R{size = %d}\n", j, size());
                    //System.out.printf("i: %3d ", i);
                }
                else
                    break;
            }
            //System.out.printf("        xi:[%s]\n", xi);
            //System.out.println("");
        }
    }


    /**
     * Apaga as bolhas cuja parte não nula
     */
    public void removeBubNotWords() {
        for (int i = 0; i < size(); ) {
            XBubble xbi= get(i);
            int k0= 0;
            int k1= 0;
            for (int j = 0; j < xbi.WX[0].length; j++) {
                Word w0= xbi.WX[0][j];
                Word w1= xbi.WX[1][j];
                if ( w0 != null && w0.isNumWord() )  k0++;
                if ( w1 != null && w1.isNumWord() )  k1++;
            }

            if ( k0 + k1 == 0 )
                remove(i);
            else
                i++;
        }
    }
    
    
    /**
     * Grava este objecto num ficheiro.
     * @param fname Nome/path do ficheiro de output.
     * @return
     */
    public boolean save(String fname) {
        try  {
            FileOutputStream ostream= new FileOutputStream(fname);
            ObjectOutputStream oos= new ObjectOutputStream(ostream);

            oos.writeObject(this);
            oos.flush();
            oos.close();
        }
        catch (Exception e)  {
            System.out.println("ERROR: "+e);
            return false;
        }

        return true;
    }


    /**
     * Appends all elements from a given list to this one.
     * @param lxb The list to be appended.
     */
    public void addList(XBubbleList lxb) {
        if ( lxb == null )  return;
        for (int i = 0; i < lxb.size(); i++) {
            add(lxb.get(i));
        }
    }
    
    
    /**
     * Inserts all the elements from a given list, maintaining
     * the defined order.  
     * @param lxb
     */
    public void addListOrd(XBubbleList lxb) {
        if ( lxb == null )  return;
        for (int i = 0; i < lxb.size(); i++) {
            addOrd(lxb.get(i));
        }
    }
    

    
    /**
     * Carrega a Hashtable, a partir de uma outra
     * tabela de Hash.
     */
    public void load(XBubbleList lxb)
    {
        if ( lxb == null )  return;
        
        //this.clear();
        for (int i=0; i< lxb.size(); i++) {
            XBubble xb= lxb.get(i);

            int nx= XBubble.numWords(xb.WX[0]);
            int ny= XBubble.numWords(xb.WX[1]);
            if ( nx < ny ) {
                Word[] W= xb.WX[0];
                xb.WX[0]= xb.WX[1];
                xb.WX[1]= W;
            }

            add(xb);
        }
        
        if ( postype == null ) postype= lxb.getPOSType();
    }


    /**
     * Carrega a Hashtable, a partir do ficheiro
     * "fname".
     */
    public boolean load(String fname) {
        XBubbleList Lxbub= loadList(fname);
        if ( Lxbub == null )  return false;

        load(Lxbub);
        posLabel(); // 2009/03/21 09:55
        return true;
    }
    
    
    public static XBubbleList loadList(String fname) {
        try {
            FileInputStream istream= new FileInputStream(fname);
            ObjectInputStream ois= new ObjectInputStream(istream);

            XBubbleList Lxbub=(XBubbleList) ois.readObject();
            ois.close();
            return Lxbub;
        }
        catch (Exception e)  {
            e.printStackTrace();
            return null;
        }
    }


    public boolean loadFromDir(String dirname) {
        return loadFromDir(dirname, false);
    }
    
    
    public boolean loadFromDir(String dirname, boolean sorted) {
        try {
            File dir= new File(dirname);
            if ( !dir.isDirectory() )  return false;
            String[] vnames= dir.list();
            for (int i = 0; i < vnames.length; i++) {
                if ( vnames[i].endsWith(".dat") ) {
                    String fidat= dir.getAbsolutePath() + '/' + vnames[i];
                    System.out.printf("LOADING FILE: %s\n", fidat);
                    XBubbleList Lxbn= loadList(fidat);
                    if ( Lxbn != null ) {
                        if ( sorted )
                            addListOrd(Lxbn);
                        else
                            addList(Lxbn);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }


    public void posLabel(POSType post) {
        for (int i = 0; i < size(); i++) {
            this.get(i).posLabel(post);
        }
    }


    public void posLabel() {
        for (int i = 0; i < size(); i++) {
            this.get(i).posLabel(postype);
        }
    }
   

    @SuppressWarnings("static-access")
    public void print() {
        for (int i=0; i<size(); i++) {
            XBubble bi= get(i);
            //System.out.print("KEY ORDR ---> "); to.print(bi.getOrdArray(postype)); to.print("\n");
            //bi.print_instance(3, postype, 20); //o.print("\n");
            System.out.printf("BUBBLE(%d)\n", i);
            bi.print();
            System.out.print("\n");
        }
    }


    public void print_pos() {
        for (int i=0; i<size(); i++) {
            XBubble bi= get(i);
            System.out.printf("bub(%3d) -------> %s\n", i, bi.toString(true));
        }
    }
    
    
    /**
     * Output instance set in various formats: ARFF, C5, ILP.
     */
    public void print_instances(int format, PrintStream out) {
        PrintStream o= System.out;
        if ( format == ARFF ) {
            CronoSensor t= new CronoSensor();
            out.printf("%%\n%% DADOS GERADOS POR ListXBubble\n%% DATA %s\n%%\n\n", t.dts());
            out.print("@relation extract\n\n");
            for (int i=1; i<=20; i++) o.printf("@attribute X%d %s\n", i, DOMAIN);
            for (int i=1; i<=20; i++) o.printf("@attribute Y%d %s\n", i, DOMAIN);
            for (int i=1; i<=3; i++)  o.printf("@attribute L%d %s\n", i, DOMAIN);
            for (int i=1; i<=3; i++)  o.printf("@attribute R%d %s\n", i, DOMAIN);
            out.print("@attribute extract {true, false}\n\n\n");
            
            out.print("@data\n");
        }
        
        for (int i=0; i<size(); i++)  {
            if ( format == ARFF ) {
                String spos= get(i).pos_instance(3, postype, 20);  int kp= spos.indexOf("), ");
                String sneg= get(i).neg_instance(spos, postype);   int kn= sneg.indexOf("), ");

                out.printf("%s\n", spos.substring(kp+3, spos.length()-1));
                //out.printf("%s\n", sneg.substring(kn+3, sneg.length()-1));
            }
            
            if ( format == ILP )
                get(i).print_ILP_instance(i, postype, out);
        }
    }



    /**
     * Novo método para escrita de bolhas para o Aleph.
     * @param out A referência de output.
     * @param xsize O tamanho da bolha, na parte mais longa. Se
     * for igual a zero, então deixa de haver esta restrição e
     * todas as bolhas serão escritas.
     */
    public int printInstancesILP(PrintStream out, int xsize) {
        int kcont= 0;
        for (int i=0; i<size(); i++)  {
            if ( get(i).print_ILP_instance(kcont, postype, out, xsize) ) {
                kcont++;
            }
        }
        return kcont;
    }
    
    
    private static void teste_geral(POSType post) {
        PrintStream o= System.out;
        o.print("\n");
        XBubble[] vxb= new XBubble[] {
            new XBubble("O/DT meu/WP gato/NN [muito/RB gordo/JJ | __ bucha/JJ ] comeu/VB o/DT bolo/NN", post),
            new XBubble("O/DT gato/NN [muito/RB feliz/JJ e/DT gordo/JJ] comeu/VB o/DT bolo/NN", post),
            new XBubble("be able to [__ __ __ pass through | be ready for __ __] the great depression", post),
            new XBubble("bush/NN [ on/IN friday/NN | __ __ ] said/VBD the/DT", post),
            new XBubble("vote/NN on/IN the/DT [overall/JJ | __] deal/NN", post),
            new XBubble("trust/VB in/IN the/DT [big/JJ | __] deal/NN", post),
            new XBubble("bush/NN [ on/IN friday/NN | __ __ ] said/VBD the/DT", post),
            new XBubble("bush/NN [ on/IN friday/NN | __ __ ] said/VBD the/DT", post),
            new XBubble("lasting incentive for india [ not | __ ] to", post),
            new XBubble("lasting incentive for india [ not | __ ] to", post),
            new XBubble("be able to [__ __ __ pass through | be ready for __ __] the great depression", post)
        };
        
        o.print("[CREATE LIST]\n");
        XBubbleList lxb= new XBubbleList(post);
        for (XBubble xb : vxb)  lxb.add(xb);
        
        o.print("[SORT LIST]\n");
        lxb.sort();
        
        o.print("[PRINT LIST]\n");
        lxb.print();
        
        o.print("[REMOVE DUPLICATES]\n");
        lxb.removeDuplicate();
        lxb.print();
        
        //o.print("\n\n[SAVE LIST]\n");
        //lxb.save("/Users/john/LXB.dat");        
    }


    @SuppressWarnings("static-access")
    private static void experimenta_ilp(POSType post, String path, String fdatname) {
        if ( to.nulity(post,path) )  return;
        path= path.trim();
        
        if ( path.charAt(path.length()-1) != '/' )  path= path+'/';

        FileX fdat= null;
        XBubbleList lxbn= new XBubbleList(post);
        //É uma directoria, com vários ficheiros *.dat (listas de bolhas).
        if ( fdatname == null || fdatname.equals("dir")) {
            if ( !lxbn.loadFromDir(path, false) )  return;
            fdat= new FileX(path+"lxbub.dat");
        }
        else {
            fdat= new FileX(path+fdatname);
            if ( !lxbn.load(fdat.getAbsolutePath()) ) return;
        }

        String base= fdat.getBase();
       
        //lxbn.removeDuplicate();
        //lxbn.removeBubNotWords();
        //lxbn.print();

        System.out.print("\nGENERATE *.lgt FILE .......... ");
        PrintStream out= System.out;
        try {
            out = new PrintStream(path+base+".lgt");
        } catch (Exception exc) {
            out = System.out;
        }
        lxbn.print_instances(ILP, out);
        System.out.printf("OK --> %d INSTANCES FOUND\n", lxbn.size());

        //System.out.print("GENERATE *.f AND *.n FILES ... ");
        System.out.print("GENERATE *.f FILE ............ ");
        FileOUT posfile = new FileOUT(path+base+".f");
        //FileOUT negfile = new FileOUT(path+base+".n");
        //if ( posfile.open() && negfile.open() ) {
        if ( posfile.open() ) {
            int n= lxbn.size();
            for (int i = 0; i < n; i++) {
                posfile.printf("rule(%d).\n", i);
                //negfile.printf("rule(%d).\n", n+i);
            }
            posfile.close();
            //negfile.close();
        }
        System.out.println("OK");
        
        System.out.print("GENERATE *.b FILE ............ ");
        lxbn.aleph_btemplate(path, base, "/lib/prolog/bub-", fdatname.equals("dir"));
        System.out.println("OK\n");      
    }


    @SuppressWarnings("static-access")
    private void experimenta_ilp(String path, String basename, int xsize) {
        if ( to.nulity(path, basename) )  return;
        path= path.trim();

        if ( path.charAt(path.length()-1) != '/' )  path= path+'/';

        //lxbn.removeDuplicate();
        //lxbn.removeBubNotWords();
        //lxbn.print();

        System.out.print("\nGENERATE *.lgt FILE .......... ");
        PrintStream out= System.out;
        try {
            out = new PrintStream(path+basename+".lgt");
        } catch (Exception exc) {
            out = System.out;
        }
        int numinst= printInstancesILP(out, xsize);
        System.out.printf("OK --> %d INSTANCES FOUND\n", numinst);

        //System.out.print("GENERATE *.f AND *.n FILES ... ");
        System.out.print("GENERATE *.f FILE ............ ");
        FileOUT posfile = new FileOUT(path+basename+".f");
        //FileOUT negfile = new FileOUT(path+base+".n");
        //if ( posfile.open() && negfile.open() ) {
        if ( posfile.open() ) {
            for (int i = 0; i < numinst; i++) {
                posfile.printf("rule(%d).\n", i);
                //negfile.printf("rule(%d).\n", n+i);
            }
            posfile.close();
            //negfile.close();
        }
        System.out.println("OK");

        System.out.print("GENERATE *.b FILE ............ ");
        aleph_btemplate(path, basename, "/lib/prolog/bub-", false);
        System.out.println("OK\n");
    }


    @SuppressWarnings("static-access")
    public boolean aleph_btemplate(String path, String basename, String basetemplate, boolean dir) {
        FileIN ftemplate= new FileIN(basetemplate+"b.lgt");
        FileIN ftemp_yap= new FileIN(basetemplate+"yap.lgt");
        if ( !ftemplate.isFile() || !ftemp_yap.isFile() ) {
            ftemplate= new FileIN(basetemplate+"template.lgt");
            if ( !ftemplate.isFile() )
                return aleph_btemplate(path, basename);
            else
                ftemp_yap= null;
        }

//        System.out.printf("ftemplate: %s\n", ftemplate.getAbsoluteFile());
//        System.out.printf("ftemp_yap: %s\n", ftemp_yap.getAbsoluteFile());

        // Variáveis para substituir no template.
        String[][] vars= new String[][] {
            {"$PROG_NAME",      getClass().getSimpleName()+".java"},
            {"$INPUT_FILE",     (dir?"*":basename)+".dat"},
            {"$MOMENT",         to.moment()},
            {"$DATA_SIZE",      ""+size()},
            {"$DATA_FILE",      ":- [-'"+basename+".lgt']."},
            {"$YAP_FILE",       ":- [-'"+basename+".yap']."}
        };

        
        String fname= path+basename+".b";
        FileOUT fbacknow= new FileOUT(fname);
        if ( !fbacknow.open() ) {
            System.out.printf("\nERROR: while creating background knowledge file: %s\n", fname);
            return false;
        }

        ftemplate.open();
        String[] vtemplate= ftemplate.readAll();
        for (String s : vtemplate) {
            StringBuffer sb= new StringBuffer(s);
            for (int i = 0; i < vars.length; i++) {
                int p= sb.indexOf(vars[i][0]);
                if ( p >= 0 ) {
                    int n= vars[i][0].length();
                    sb.replace(p, p+n, vars[i][1]);
                }
            }
            fbacknow.printf("%s\n", sb.toString());
        }
        fbacknow.close();


        if ( ftemp_yap != null ) {
            System.out.print("=> Include Yap File ... ");
            fname = path + basename + ".yap";
            FileOUT fyap = new FileOUT(fname);
            if (!fyap.open()) {
                System.out.printf("\nERROR: while creating yap knowledge file: %s\n", fname);
                return false;
            }

            ftemp_yap.open();
            vtemplate = ftemp_yap.readAll();
            for (String s : vtemplate) {
                StringBuffer sb = new StringBuffer(s);
                for (int i = 0; i < vars.length; i++) {
                    int p = sb.indexOf(vars[i][0]);
                    if (p >= 0) {
                        int n = vars[i][0].length();
                        sb.replace(p, p + n, vars[i][1]);
                    }
                }
                fyap.printf("%s\n", sb.toString());
            }
            fyap.close();
        }


        return true;
    }


    /**
     * Metodo obsoleto. Substituido por:
     * <pre>aleph_btemplate(String path, String basename, String sftemplate, boolean dir)</pre>
     * @param path
     * @param basename
     * @return
     */
    @SuppressWarnings("static-access")
    public boolean aleph_btemplate(String path, String basename) {
        String fname= path+basename+".b";
        FileOUT fbacknow= new FileOUT(fname);
        if ( !fbacknow.open() ) {
            System.out.printf("\nERROR: while creating background knowledge file: %s\n", fname);
            return false;
        }

        //----------------------------------
        // ALEPH BACKGROUND KNOWLEDGE FILE.
        //----------------------------------
        String[] vtemplate= new String[] {
            "%",
            "%---------------------------------------------------------------------------------------------",
            "% AUTOMATICALLY GENERATED BY:... \""+getClass().getSimpleName()+".java\"",
            "% INPUT FILE:................... "+path+basename+".dat",
            "% MOMENT:....................... " + to.moment(),
            "%---------------------------------------------------------------------------------------------",
            "%",
            "%",
            "% ALEPH PARAMETERS",
			":- set(clauselength,8).",
			":- set(evalfn,posonly).",
			":- set(samplesize,0).",
			":- set(minpos, 4).",
			":- set(verbosity, 0).",
			"",
			"%:- set(minscore, 0.0).",
			"",
            "",
            "",
			"%---------------------------------------------------------------------------------------------",
			"% MODE DECLARATIONS",
			"%---------------------------------------------------------------------------------------------",
			":- modeh(1, rule(+bub)).",
			"",
			":- modeb(*, dimension(+bub, #side, #nat)).",
            ":- modeb(*, inKPOS(+bub, #k, #side, #tag)).",
			":- modeb(*, inPOS(+bub, #side, #tag)).",
			":- modeb(*, inLEX(+bub, #side, #word)).",
			"",
			":- determination(rule/1, dimension/3).",
			":- determination(rule/1, inKPOS/4).",
            ":- determination(rule/1, inPOS/3).",
			":- determination(rule/1, inLEX/3).",
			"",
			"",
			":- [-'/lib/prolog/utils.lgt'].",
			"",
			"bub(X) :- entre(X,1,"+2*size()+").",
			"",
			"",
			":- [-'/lib/prolog/penpostags.lgt'].",
			"",
			"tag(X) :- penpost(_, X, _).",
			"",
			"",
			"side(left).",
			"side(right).",
			"side(center:x).",
			"side(center:y).",
			"",
			"",
            "k(1).  k(2).   k(3).",
            "",
            "",
            "nat(X) :- entre(X,0,20).",
			"",
            "",
			"%---------------------------------------------------------------------------------------------",
			"% DOMAIN KNOWLEDGE",
			"%---------------------------------------------------------------------------------------------",
			":- op(250, xfx, --->).",
			"",
			"",
			"% DIMENSIONS",
			"dimension(ID, center:x, Xn) :- bub(ID, _, X--->_, _), length(X, Xn).",
			"dimension(ID, center:y, Yn) :- bub(ID, _, _--->Y, _), length(Y, Yn).",
			"",
			"",
			"",
			"% LEXICAL SCAN",
			"inLEX(ID, center:x, Word) :- bub(ID, _, X--->_, _),  inLEX(Word, X).",
			"inLEX(ID, left,   Word) :-   bub(ID, Context, _, _), inLEX(Word, Context).",
			"inLEX(ID, right,  Word) :-   bub(ID, _, _, Context), inLEX(Word, Context).",
			"inLEX(ID, center:y, Word) :- bub(ID, _, _--->Y, _),  inLEX(Word, Y).",
			"",
			"inLEX(Word, [Word/_|_]).",
			"inLEX(Word, [_|Tail]) :- inLEX(Word, Tail).",
			"",
			"",
			"% SYNTACTICAL SCAN",
			"inPOS(ID, center:x, Tag) :- bub(ID, _, X--->_, _),  inPOS(Tag, X).",
			"inPOS(ID, left,   Tag) :-   bub(ID, Context, _, _), inPOS(Tag, Context).",
			"inPOS(ID, right,  Tag) :-   bub(ID, _, _, Context), inPOS(Tag, Context).",
			"inPOS(ID, center:y, Tag) :- bub(ID, _, _--->Y, _),  inPOS(Tag, Y).",
			"",
			"inPOS(POS, [_/POS|_]).",
			"inPOS(POS, [_|Tail]) :- inPOS(POS, Tail).",
			"",
            "",
            "% POSITIONAL SYNTACTICAL SCAN",
            "inKPOS(ID, left,  K,  Tag) :- ",
            "    bub(ID, Context, _, _),",
            "    k(K),",
            "    inKPOS(Tag, K,  Context)",
            "    .",

            "inKPOS(ID, right, K,  Tag) :-",
            "    bub(ID, _, _, Context),",
            "    k(K),",
            "    inKPOS(Tag, K,  Context)",
            "    .",
            "",
            "inKPOS(ID, center:x, K,  Tag) :-",
            "    bub(ID, _, CX--->_, _),",
            "    k(K),",
            "    inKPOS(Tag, K,  CX)",
            "    .",
            "",
            "inKPOS(ID, center:y, K,  Tag) :-",
            "    bub(ID, _, _--->CY, CY),",
            "    k(K),",
            "    inKPOS(Tag, K,  CY)",
            "    .",
            "",
            "inKPOS(POS, 1, [_/POS|_]).",
            "inKPOS(POS, K, [_|Tail]) :-",
            "    K > 1,",
            "    K1 is K-1,",
            "    inKPOS(POS, K1, Tail)",
            "    .",
            "",
            "%---------------------------------------------------------------------------------------------",
            "% DATA FILE",
            "%---------------------------------------------------------------------------------------------",
            ":- [-'"+basename+".lgt'].",
            "",
            ""
        };

        for (int i = 0; i < vtemplate.length; i++) {
            fbacknow.printf("%s\n", vtemplate[i]);
        }
        fbacknow.close();
        return true;
    }

    
    
    public static void help() {
        PrintStream o= System.out;
        o.print("\n   SINTAXE: java ListXBubble -listf <list file> [OPTIONS]" +
                "\n            Or example (2)\n");
        o.print("\n   OPTIONS:\n" +
                "      -listf -----------> Output the whole list, one bubble per line and with pos tagging.\n" +
                "      -listb -----------> Output the whole list, in bubble format.\n" +
                "      -post ------------> Define the POS XML tag file.\n" +
                "      -outf ------------> Instance output file (default to standard output.\n" +
                "      -format ----------> Output format: C4.5, ARFF, ILP (default).\n" +
                "      -aleph <base> ----> Generate Aleph files, for ILP induction.\n" +
                "\n   EXAMPLES:\n" +
                "      (1) java hultig.sumo.ListXBubble -listf Lxb.dat -outf bolhas.lgt -formar ILP\n" +
                "      (2) java hultig.sumo.ListXBubble -listb Lxb.dat\n" +
                "      (3) java hultig.sumo.ListXBubble -aleph listbub.dat\n" +
                "      (4) java hultig.sumo.ListXBubble -aleph dir\n" +
                "\n");
    }


    public void rulePartition(String rulefile) {
        RuleList LR= new RuleList();
        if ( !LR.loadFromFile(rulefile, 0) ) return;

        int n= size();
        PrintStream o= System.out;
        for (int i = 0; i < LR.size(); i++) {
            Rule r= LR.get(i);
            String sr= r.getStrConditions();
            //if ( r.getCover() < 100 ) continue;

            System.out.println("\n\n");
            System.out.printf("%s\n", Toolkit.sline(150));
            o.printf("%4d   ID: %4d   size: %3d   COVER: %4d   <===   %s\n", i+1, r.getID(), n, r.getCover(), sr);
            System.out.printf("%s\n", Toolkit.sline(150));
            int cont= 0;
            for (int j = 0; j < n; j++) {
                //o.print(j);
                XBubble x= get(j);
                
                if (x.satisfy(sr)) {
                    o.printf("\t   %s\n", x.toString(true));
                    cont++;
                }
            }
            o.printf("\n\t   TOTAL:......... %4d\n", cont);
            o.printf("\t   %s\n", Toolkit.sline(150));
        }
        o.print("\n\n");
    }


    public void viewSatisfy(String srule) {
        System.out.printf("\n\nRULE:.... %s\n\n\n", srule);

        int cont= 0;
        for (int k=0; k< size(); k++) {
            XBubble xk= get(k);
            //System.out.printf("%s\n", xk.toString(true));
            if ( xk.satisfy(srule) ) {
                xk.print();
                System.out.print("\n\n");
                //System.out.printf("   %s\n", xk.toString(true));
                cont++;
            }
        }
        System.out.printf("NUMBER: %d\n\n", cont);
    }


    public void genMidPOSCorp(String fname, POSType post) {
        genMidPOSCorp(fname, post, 0);
    }


    public void genMidPOSCorp(String fname, POSType post, int sizex) {
        if ( fname == null )  return;

        System.out.printf("\nSAVING CORPUS IN %s FILE ... ", fname);
        FileOUT fout= new FileOUT(fname, "UTF-8");
        for (int i = 0; i < this.size(); i++) {
            XBubble xb= this.get(i);
            int n0= XBubble.numWords(xb.WX[0]);
            int n1= XBubble.numWords(xb.WX[1]);
            if ( n0*n1 != 0 )  continue;

            int n= n1;
            Word[] W= xb.WX[1];
            if ( n < n0 ) {
                n= n0;
                W= xb.WX[0];
            }
            if ( sizex > 0 && n != sizex )  continue;

            String spos= Word.words2StringPOS(W, post);
            if ( sizex < 1 )
                fout.printf("<s> %s </s>\n", spos.toLowerCase());
            else {
                int nL= xb.WL.length;
                StringBuffer sbL= new StringBuffer("");
                if ( nL > 0 )
                    sbL.insert(0, xb.WL[nL-1].getPOS(post));
                else
                    sbL.insert(0, "VDF");

                int nR= xb.WR.length;
                StringBuffer sbR= new StringBuffer("");
                if ( nR > 0 )
                    sbR.append(xb.WR[0].getPOS(post));
                else
                    sbR.append("VDF");

                fout.printf("p(%s, %s, %s).\n",
                        sbL.toString().toLowerCase(),
                        spos.toLowerCase(),
                        sbR.toString().toLowerCase()
                );
            }
        }
        System.out.println("OK");
        fout.close();
        System.out.println("\n");
    }


    /**
     * Cria um histograma com os comprimentos das "midx", das bolhas.
     * @return Devolve o array do histograma. A posição zero indica
     * o número total de elementos do conjunto, neste caso, o número
     * de bolhas.
     */
    public int[] lengthMidHistogram() {
        int[] vn= new int[31];
        for (int i = 0; i < vn.length; i++)  vn[i]= 0;

        for (int i = 0; i < this.size(); i++) {
            XBubble xb= this.get(i);
            int n0= XBubble.numWords(xb.WX[0]);
            int n1= XBubble.numWords(xb.WX[1]);
            if ( n0*n1 != 0 )  continue;

            int n= n0 < n1 ? n1 : n0;
            if ( n > 30)
                vn[30]++;
            else
                vn[n]++;

            vn[0]++;
        }
        Toolkit.printHistogram(vn, 200);

        return vn;
    }

    
    
    /**
     * MAIN - For testing.
     * @param args
     */
    public static void main(String[] args) {
        OSArguments osa= new OSArguments(args);
        if ( osa.size() < 1 ) {
            help();
            return;
        }

        PrintStream o= System.out;
        POSType post= new POSType();

        if ( osa.contains("post") ) {
            if ( !post.loadXML( osa.get("post") ) )
                if ( !post.loadXML("/a/pen-pos-type.xml") ) return;
        }
        else
            if ( !post.loadXML("/a/pen-pos-type.xml") ) {
                help();
                return; //---> EXIT!
            }        
        o.printf("[NUMBER OF POS TAGS LOADED: %d]\n\n", post.size());

        //O conteudo de "aleph" pode ser um directório contendo ficheiros
        //*.dat. Neste caso todos serão processados (reunidos) num só
        //conjunto de dados.
        if ( osa.contains("aleph") ) {
            experimenta_ilp(post, "./", osa.get("aleph"));
            return; //---> EXIT!
        }

        if ( !osa.contains("listf") && !osa.contains("listb") && !osa.contains("dim") ) {
            help();
            return; //---> EXIT!
        }
        
        int format= XBubbleList.ILP;
        if ( osa.contains("format") ) {
            if ( osa.get("format").equals("ARFF") )  format= XBubbleList.ARFF;
            if ( osa.get("format").equals("C4.5") )  format= XBubbleList.C45;
        }
        
        PrintStream out= System.out;
        if ( osa.contains("outf") )
            try {
                out = new PrintStream(osa.get("outf"));
            } catch (Exception exc) {
                out = System.out;
            }
        
        XBubbleList lxbn= new XBubbleList(post);
        
        if ( osa.contains("partition") ) {
            if ( lxbn.load( osa.get("listf") ) ) {
                /*
                lxbn.viewSatisfy("chunk(A,right,np), chunk(A,center:x,vp), inx(A,left,1,pos(vb))");
                System.exit(0);
                 */

                lxbn.rulePartition(osa.get("partition") );
            }

            return; //---> EXIT!
        }


        // Abril 2009
        if ( osa.contains("senta") ) {
            String fname= osa.get("senta");
            char[] fc= {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
            for (int i = 0; i < fc.length; i++) {
                String fn = "/a/news@google/test3days/"+fc[i]+"/lbub.dat";
                System.out.printf("READ FROM: %s ... ", fn);
                lxbn.load(fn);
                lxbn.removeDuplicate();
                System.out.printf("LIST SIZE: %d\n", lxbn.size());
            }
            lxbn.genMidPOSCorp(fname, post, 1);
            return;
        }


        // Os testes dimensionais: zd1, zd2, zd3, ...
        // Abril 2009
        if ( osa.contains("dim") ) {
            int n= Integer.parseInt(osa.get("dim"));
            if ( n < 1 )  return;

            n= 3;
            System.out.printf("\n\nDIMENSION: %d\n", n);
            /**/
            char[] fc= {
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o'
            };
            //char[] fc= {'a', 'b'};
            for (int i = 0; i < fc.length; i++) {
                String fn = "/a/news@google/test3days/"+fc[i]+"/lbub.dat";
                System.out.printf("READ FROM: %s ... ", fn);
                lxbn.load(fn);
                lxbn.removeDuplicate();
                System.out.printf("LIST SIZE: %d\n", lxbn.size());
            }
            lxbn.save("/a/news@google/test3days/zd"+n+"/lxbub.dat");
            /**/
            lxbn.experimenta_ilp("/a/news@google/test3days/zd"+n+"/", "lxbub", n);
            return;
        }
        
        o.printf("[LOAD LIST BINARY FILE]: %s\n", osa.get("listf"));
        if ( osa.contains("listf") && lxbn.load( osa.get("listf") ) ) {
            o.print("[COMPLETE]\n\n");
            o.print("[PRINT INSTANCES]\n");
            lxbn.sort();
            lxbn.print_pos();
            o.printf("[COMPLETE] ---> list size: %d\n\n", lxbn.size());
        }
        else if ( osa.contains("listb") && lxbn.load( osa.get("listb") ) ) {
            o.print("[COMPLETE]\n\n");
            o.print("[PRINT INSTANCES]\n");
            lxbn.print();
            o.printf("[COMPLETE] ---> list size: %d\n\n", lxbn.size());
        }
        else
            o.printf("ERROR !!!\n");
    }      
}


