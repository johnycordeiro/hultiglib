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

import hultig.util.Toolkit;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b><b>NOT YET WELL COMMENTED</b></b>.
 * @date 19:12:04 3/Set/2008
 * @author J. P. Cordeiro
 */
public class XBubble implements Serializable
{
    public static final long serialVersionUID = -5798479126800064641L;
    private static PrintStream o= System.out;
    public static String PONTUACAO= ",.;:!?";
    
    public Word[] WL;
    public Word[] WR;
    public Word[][] WX;

    public POSType POST;

    private Random rand= new Random();

    
    public XBubble(Word[] wL, Word[][] wX, Word[] wR) {
        WL= wL;
        WX= wX;
        WR= wR;
        POST= null;
    }
    
    
    public XBubble(String sL, String[] sX, String sR, POSType pos) {
        WL= WR= null; WX= null;
        if ( sX == null || sX.length != 2 )  return;

        this.POST= pos;
        WL= this.__str2vword(sL, POST);
        WR= this.__str2vword(sR, POST);
        WX= new Word[2][];
        WX[0]= this.__str2vword(sX[0], POST);
        WX[1]= this.__str2vword(sX[1], POST);

        if ( numWords(WX[0]) < numWords(WX[1]) ) {
            Word[] W= WX[0];
            WX[0]= WX[1];
            WX[1]= W;
        }
    }
    
    
    /**
     * Para facilitar a definição de bolhas, no código. 
     * @param sformatada A bolha em formato de string - exemplo:<br />
     * <pre>
     *      "O/DT gato/NN [muito/RB gordo/JJ | _ _] comeu/VB o/DT bolo/NN"
     * 
     * é equivalente a
     * 
     *      "O/DT gato/NN muito/RB gordo/JJ comeu/VB o/DT bolo/NN"
     *      "O/DT gato/NN ________ ________ comeu/VB o/DT bolo/NN" 
     * </pre>
     */
    public XBubble(String sfmt, POSType pos) {
        WL= WR= null; WX= null;
        if ( sfmt == null )  return;
        
        int a= sfmt.indexOf('[');
        int b= sfmt.indexOf("]");
        if ( a < 0 || b < 0 || a > b ) return;
        
        String sL= sfmt.substring(0, a).trim();
        String sR= sfmt.substring(b+1).trim();
        String sx= sfmt.substring(a+1, b).trim();
        
        int c= sx.indexOf('|');
        String[] sX= new String[2];
        //não exste '|'.
        if ( c < 0 ) {
            String[] vsx= sx.split("  *");
            StringBuilder sbuff= new StringBuilder(Toolkit.sline('_', vsx[0].length()));
            for (int i=1; i<vsx.length; i++) {
                sbuff.append(" ").append(Toolkit.sline('_', vsx[i].length()));
            }
            sX[0]= sx;
            sX[1]= sbuff.toString();
        }
        else {
            sX[0]= sx.substring(0, c).trim();
            sX[1]= sx.substring(c+1).trim();
        }
        
        //o.printf("   sX(0) ---> [%s]\n", sX[0]);
        //o.printf("   sX(1) ---> [%s]\n", sX[1]);
        
        WL= this.__str2vword(sL, pos);
        WR= this.__str2vword(sR, pos);
        WX= new Word[2][];
        WX[0]= this.__str2vword(sX[0], pos);
        WX[1]= this.__str2vword(sX[1], pos);

        if ( numWords(WX[0]) < numWords(WX[1]) ) {
            Word[] W= WX[0];
            WX[0]= WX[1];
            WX[1]= W;
        }
    }

    
    
    /**
     * Converte uma frase para um aaray de Word. A frase pode estar
     * etiquetada sintácticamente. Exemplo: <br />
     * <pre>
     *    The/DT big/JJ cat/NN eat/VB the/DT cake/NN
     * </pre>
     * 
     * @param s
     * @param POST
     * @return
     */
    private Word[] __str2vword(String s, POSType pos) {
        if ( s == null || s.length() < 1 )  return null;
        
        String[] v= s.split("  *");
        if ( v == null || v.length < 1 )  return null;
        
        Word[] vw= new Word[v.length];
        for (int i=0; i<v.length; i++) {
            String[] vi= v[i].split("/");
            if ( vi.length < 2 ) {
                if ( vi[0].charAt(0) == '_' )
                    vw[i]= null;
                else
                    vw[i]= new Word(vi[0]);
            }
            else {
                vw[i]= new Word(vi[0], pos.str2cod(vi[1]));
                vw[i].setPOS(vi[1]);
                //o.printf("vi(%d) ---> %s/%s  ---> cod: %d\n", i, vi[0], vi[1], pos.str2cod(vi[1]));
            }
        }
        
        return vw;
    }


    public String[] toVString() {
        return toVString(false);
    }
    
    
    public String[] toVString(boolean with_pos_tags) {
        //CONTEXTO ESQUERDO
        StringBuilder sL= new StringBuilder();
        if ( WL != null && WL.length > 0 ) {
            //sL.append(WL[0]);
            for (int i=0; i<WL.length; i++)  {
                if ( i > 0 ) sL.append(' ');
                sL.append(WL[i]);
                if ( with_pos_tags  &&  WL[i].hasPOS() ) {
                    //o.print("\nHAS LEFT CONTEXT!\n");
                    sL.append("/").append(WL[i].getPOS());
                }
            }
            //o.printf("LEFT CONTEXT -------> [%s]\n", sL.toString());
        }
        
        //NUCLEO
        StringBuffer[] sX= new StringBuffer[] {
            new StringBuffer(),
            new StringBuffer()
        };
        if ( WX != null && WX.length >= 2 ) {
            for (int i=0; i<WX[0].length; i++) {                                
                String[] vs= __format(WX[0][i], WX[1][i], with_pos_tags);
                if ( vs == null ) continue;
                
                sX[0].append(vs[0]);
                sX[1].append(vs[1]);
                if ( i < WX[0].length-1 ) {
                    sX[0].append(' ');
                    sX[1].append(' ');
                }
            }
        }
        
        //CONTEXTO DIREITO
        StringBuilder sR= new StringBuilder();
        if ( WR != null && WR.length > 0 ) {
            for (int i=0; i<WR.length; i++) {
                if ( i > 0 ) sR.append(' ');
                sR.append(WR[i]);

                if ( with_pos_tags  &&  WR[i].hasPOS() ) {
                    sR.append("/").append(WR[i].getPOS());
                }
            }
        }        

        return new String[]{
            sL.toString(),
            sX[0].toString(),
            sX[1].toString(),
            sR.toString()
        };        
    }
    

    /**
     * Output da bolha.
     */
    public void print() {
        String[] vs= toVString(true);
        if ( vs == null ) return;
        
        o.printf("   |%s %s %s|\n", vs[0], vs[1], vs[3]);
        o.printf("   |%s %s %s|\n", vs[0], vs[2], vs[3]);        
    }
    
    
    private String __vword2list(Word[] W, POSType pos) {
        if ( W == null || W.length < 1 )  return "[]";
        return __vword2list(W, pos, W.length);
    }
    
    
    private String __vword2list(Word[] W, POSType pos, int size) {
        if ( W == null || W.length < 1 )  return "[]";
        
        int a, b, inc;
        if ( size > 0 ) {
            a= 0;
            b= Math.min(size, W.length);
            inc= 1;
        }
        else {
            a= W.length-1;
            b= Math.max(-1, a+size);
            inc= -1;
        }
        
        StringBuilder sb= new StringBuilder("[");
        for (int k=a; k!=b; k+=inc) {
            if ( W[k] == null )  continue;
            
            String wrdK= W[k].toString();

            String posK = null;
            String chkK= null;
            // 2009-02-09
            //------------
            if ( wrdK.length() == 1  &&  PONTUACAO.indexOf(wrdK.charAt(0)) >= 0 ) {
                posK= "punct";
                chkK= "punct";
            }
            else {
                posK = pos.cod2str(W[k].getPosCod()).toLowerCase();
                chkK= ChunkType.cod2str(W[k].getChkCod()).toLowerCase();
            }
            //------------
            
            wrdK= atom_for_prolog_sintaxe(wrdK);
            posK= atom_for_prolog_sintaxe(posK);
            chkK= atom_for_prolog_sintaxe(chkK);

            //2009-02-09 18:06
            //----------------
            if ( wrdK.equals("'-'") )  wrdK= "'CHR_MINUS'";
            //----------------

            if ( wrdK != null && posK != null ) {
                if ( sb.length() > 1 )  sb.append(", ");
                sb.append(wrdK).append('/').append(posK).append('/').append(chkK);
                //if ( k != b - inc )  sb.append(", ");
            }

        }
        sb.append(']');
        return sb.toString();
    }
    

    /**
     * String transformation to ensure prolog atom compatibility.
     * @param s
     * @return
     */
    private String atom_for_prolog_sintaxe(String s) {
        if ( s == null || s.trim().length() < 1 )  return null;
        
        boolean critical= false;
        StringBuilder atom= new StringBuilder(s.trim());
        if ( Character.isUpperCase(atom.charAt(0)) )  critical= true;
        if ( !Character.isLetter(s.charAt(0)) )  critical= true;
        for (int i=0; i<atom.length(); i++) {
            char c= atom.charAt(i);
            if ( !Character.isLetterOrDigit(c) ) 
                critical= true;
            
            if ( c == '\'') {
                atom.insert(i, '\\');
                i++;
            }
        }
            
        if ( critical ) {
            atom.insert(0, '\'');
            atom.append('\'');
        }
            
        return atom.toString();
    }



    /**
     * Atalho, para bolhas com qualquer comprimento.
     * @param id
     * @param pos
     * @param out
     */
    public boolean print_ILP_instance(int id, POSType pos, PrintStream out) {
        return print_ILP_instance(id, pos, out, 0);
    }

    

    /**
     * Imprime a bolha no formato adequado ao Aleph.
     * @param id Um identificador
     * @param POST Tipos de POS defindos.
     * @param out Referência de output.
     * @param xsize O tamanho exacto da bolha (parte maior). Se for igual a zero então
     * qualquer tamanho é admitido.
     */
    public boolean print_ILP_instance(int id, POSType pos, PrintStream out, int xsize) {
        if ( WX == null || WX[0] == null || WX[0].length == 0 )  return false;
        
        //ID e contexto esquerdo.
        int m= numWords(WX[0]);  
        int n= numWords(WX[1]);
        if ( m < n ) {
            // swap
            int aux= m;  m= n;  n= aux;
        }

        if ( xsize > 0 && xsize != WX[0].length )  return false;

        out.printf("bub(%3d, t(%d,%d),\n", id, m, n);
        out.printf("    %s,\n", __vword2list(WL, pos, -3));
        
        String sx= __vword2list(WX[0],pos);
        String sy= __vword2list(WX[1],pos);
        if ( sx.length() > sy.length() )
            out.printf("    %s ---> %s,\n", sx, sy);
        else
            out.printf("    %s ---> %s,\n", sy, sx);
        
        //Contexto direito
        out.printf("    %s\n", __vword2list(WR, pos, 3));
        out.print( "    ).\n");

        return true;
    }
    
    
    /**
     * Devolve a bolha em forma de instância. O tamanho da instância será igual
     * a:
     * <pre>
     *      1 + M + N + 2 * size_context
     * </pre>
     * sendo:
     * <pre>
     *      M = |WX(BIG) != NULL|
     *      N = |WX(SMALL) != NULL|
     * </pre>
     * Exemplo:
     * <pre>
     *      [N(2,1), muito, RB, bonita, JJ, bonita, JJ, Ela, WP, é, VP, ...]
     * </pre>
     * @param size_context
     * @param POST
     * @return A string com a instância codificada.
     */
    public String pos_instance(int size_context, POSType pos, int maxX) {
        if ( WX == null || WX[0] == null || WX[0].length == 0 ) return null;
        
        //NUCLEO
        Word[] WX0= WX[0]; //WX(BIG) ----> array maior.
        Word[] WX1= WX[1]; //WX(SMALL) --> array menor.
        int nX0= WX0.length - numVoids(WX0);
        int nX1= WX1.length - numVoids(WX1);
        if ( nX0 < nX1 ) {
            WX0= WX[1];
            WX1= WX[0];
            int aux= nX0; nX0= nX1; nX1= aux;
        }        
        StringBuilder sinst= new StringBuilder("[N("+nX0+","+nX1+")");
        for (int i=0; i<maxX; i++)  {
            if ( i<WX0.length && WX0[i] != null ) {
                //sinst.append(", "+WX0[k]); //elemento lexico
                sinst.append(", ").append(pos.cod2str(WX0[i].getPosCod()));
            }
            else
                sinst.append(", VOID");
        }
        for (int i=0; i<maxX; i++)  {
            if ( i<WX1.length && WX1[i] != null ) {
                //sinst.append(", "+WX1[k]); //elemento lexico
                sinst.append(", ").append(pos.cod2str(WX1[i].getPosCod()));
            }
            else
                sinst.append(", VOID");
        }            
        
        //CONTEXTO ESQUERDO
        sinst.append(__context2instance(WL, 3, pos));
        
        //CONTEXTO DIREITO
        sinst.append(__context2instance(WR, 3, pos));
        
        //FIM
        sinst.append(", true]");        
        return sinst.toString();
    }
    
    
    public void print_instance(int size_context, POSType pos, int maxX) {
        String sinst= this.pos_instance(size_context, pos, maxX);
        o.printf("INSTANCE ---> %s\n", sinst);
    }
    
    
    public String neg_instance(String pos_instance, POSType pos) {
        if ( pos_instance == null ) return null;
        if ( !pos_instance.startsWith("[N(") )  return null;
        
        String[] vs= pos_instance.substring(1, pos_instance.length()-1).split(", ");
        StringBuilder sbuff= new StringBuilder("[X"+vs[0].substring(1));
        String constantes= "{UNDEF, SYT0, VOID}";
        for (int i=1; i<vs.length-1; i++) {
            if ( constantes.indexOf(vs[i]) > 0 ) {
                sbuff.append(", ").append(vs[i]);
                //o.printf("k: %3d   [%7s] ---> [%7s]\n", k, vs[k], vs[k]);
            }
            else {
                String  sr= pos.getRandomTag();
                //o.printf("k: %3d   [%7s] ---> [%7s]\n", k, vs[k], sr);
                sbuff.append(", ").append(sr);
            }
        }
        //FIM
        sbuff.append(", false]");
        return sbuff.toString();
    }
    
    
    public void print_neg_instance(String pos_instance, POSType pos) {
        String sinst= this.neg_instance(pos_instance, pos);
        o.printf("INSTANCE ---> %s\n", sinst);
    }
    
    
    private String __context2instance(Word[] vw, int size, POSType pos) {
        StringBuilder sb= new StringBuilder();
        for (int i=0; i<size; i++) {
            if ( vw != null && i < vw.length ) {
                //sb.append(", "+vw[k]);
                sb.append(", ").append(pos.cod2str(vw[i].getPosCod()));
            }
            else {
                //sb.append(", LEX0");
                sb.append(", SYT0");                
            }                
        }
        
        return sb.toString();
    }
    
    
    public int[] getOrdArray(POSType pos) {
        if ( WX == null || WX[0] == null || WX[0].length == 0 ) return null;
        Word[] W= WX[0][0] == null ? WX[1] : WX[0];
        int nW= W.length - numVoids(W);
        
        int[] v= new int[1 + nW + 2*3];
        v[0]= nW;
        for (int i=1; i<v[0]+1; i++) {
            if ( W[i-1] != null )
                v[i]= W[i-1].getPosCod();
        }
        
        if ( WL != null && WL.length > 0 ) {
            for (int i = 0; i < 3; i++) {
                if ( i < WL.length )
                    v[1+v[0]+i]= WL[i].getPosCod();
                else
                    v[1+v[0]+i]= -1;      
            }
        }
        
        if ( WR != null && WR.length > 0 ) {
            for (int i = 0; i < 3; i++) {
                if ( i < WR.length )
                    v[1+v[0]+3+i]= WR[i].getPosCod();
                else
                    v[1+v[0]+3+i]= -1;      
            }
        }
        
        return v;
    }
    
    
    /**
     * Formatar duas palavras, de modo a manter o mesmo tamanho. Utilizam-se espaços
     * esquerdo e direito e tambem '___' para termos null 
     * @param wa Primeira palavra
     * @param wb Segunda palavra
     * @return Um array com as strings das duas palavras alinhadas - exemplos: <br />
     * <pre>
     *   correr     ___________  
     *   ______     informatica
     * 
     * 
     *    corre     informado
     *   corrida    informar
     * </pre>
     */
    private String[] __format(Word wa, Word wb, boolean with_pos_tags) {
        if ( wa == null && wb == null )  return new String[]{"", ""};
        
        if ( wa != null && wb != null ) {
            int na = wa.length();
            int nb = wb.length();
            if ( na == nb )
                return new String[] {
                    wa.toString(with_pos_tags),
                    wb.toString(with_pos_tags)
                };
            else if ( na > nb ) {
                int dL= (na-nb)/2;
                int dR= na-nb-dL;
                return new String[] {
                    wa.toString(with_pos_tags),
                    chstr(' ',dL)+wb.toString()+chstr(' ',dR)
                };
            }
            else {
                int dL= (nb-na)/2;
                int dR= nb-na-dL;
                return new String[] {
                    chstr(' ',dL)+wa.toString(with_pos_tags)+chstr(' ',dR),
                    wb.toString(with_pos_tags)
                };                
            }
        }
               
        Word w= wa != null ? wa : wb;
        String[] vs= new String[2];
        if ( wa != null ) {
            vs[0]= w.toString(with_pos_tags);
            vs[1]= chstr('_', vs[0].length());
        }
        else {
            vs[1]= w.toString(with_pos_tags);
            vs[0]= chstr('_', vs[1].length());
        }

        return vs;

        /*
        return wa != null ?
            new String[]{ w.toString(with_pos_tags), chstr('_', w.length())}
        :
            new String[]{ chstr('_', w.length()), w.toString(with_pos_tags)}
        ;
        */
    }
    
    
    private String chstr(char c, int size) {
        if ( size < 1 )  return "";
        char[] vc= new char[size];
        for (int i = 0; i < vc.length; i++)  vc[i] = c;
        return new String(vc);
    }


    public static int numWords(Word[] W) {
        int n= 0;
        for (int i = 0; i < W.length; i++) {
            if ( W[i] != null && W[i].charAt(0) != '_' )  n++;
        }
        return n;
    }
    
    
    public static int numVoids(Word[] W) {
        if ( W == null )  return -1;
        
        int n= 0;
        for(int i=0; i<W.length; i++) {
            if ( W[i] == null ) n++;
        }
        
        return n;
    }


    public void posLabel(POSType post) {
        //LEFT
        for (int i = 0; i < WL.length; i++) {
            Word word = WL[i];
            if ( word != null ) word.posLabel(post);
        }

        //KERNEL
        for (int j = 0; j < WX[0].length; j++) {
            if ( WX[0][j] != null )  WX[0][j].posLabel(post);
            if ( WX[1][j] != null )  WX[1][j].posLabel(post);
        }

        //RIGHT
        for (int i = 0; i < WR.length; i++) {
            Word word = WR[i];
            if ( word != null ) word.posLabel(post);
        }
    }
    
    
    public String toString(boolean with_pos_tag) {
        String[] vs= toVString(with_pos_tag);
        if ( vs == null ) return "null";

        Toolkit.sclean(vs,"_");
        return Toolkit.sprintf("[%s]   [%s | %s]   [%s]", vs[0], vs[1], vs[2], vs[3]);
    }


    @Override
    public String toString() {
        return toString(false);
    }
    
    
    public boolean equal(XBubble b) {
        if ( b == null ) return false;
        /*
        if ( WL == null && b.WL != null  ||  WL != null && b.WL == null)  return false;
        //System.out.print("1");
        if ( WR == null && b.WR != null  ||  WR != null && b.WR == null)  return false;
        //System.out.print("2");
        if ( WX == null && b.WX != null  ||  WX != null && b.WX == null) return false;
        //System.out.print("3");
        if ( WL.length != b.WL.length || WR.length != b.WR.length || WX.length != b.WX.length )  return false;
        //System.out.print("4");
        if ( WX[0] == null && b.WX[0] != null  ||  WX[0] != null && b.WX[0] == null)  return false;
        //System.out.print("5");
        if ( WX[1] == null && b.WX[1] != null  ||  WX[1] != null && b.WX[1] == null)  return false;
        //System.out.print("6");
        */
        
        if ( !equal(WL, b.WL) )  return false;
        //System.out.print("7");
        if ( !equal(WR, b.WR) )  return false;
        //System.out.print("8");
        if ( !equal(WX[0], b.WX[0]) && !equal(WX[0], b.WX[1]) )  return false;
        //System.out.print("9");
        if ( !equal(WX[1], b.WX[1]) && !equal(WX[1], b.WX[0]) )  return false;
        //System.out.print("a");
        
        return true;
    }
    
    
    private boolean equal(Word[] wa, Word[] wb) {
        if ( wa == null && wb == null )  return true;
        if ( wa == null && wb != null  ||  wa != null && wb == null )  return false;
        if ( wa.length != wb.length )  return false;
        
        for (int k=0; k<wa.length; k++) {
            //System.out.print(".");
            if ( wa[k] == null && wb[k] == null )
                continue;
            else if ( wa[k] != null  &&  wb[k] == null )
                return false;
            else if ( wa[k] == null  &&  wb[k] != null )
                return false;
            else
                if ( !wa[k].equals(wb[k]) )  {
                    //System.out.printf("#(k:%d) --->  wa:[%s]   wb:[%s]", k, wa[k], wb[k]);
                    return false;
                }

        }
        
        return true;
    }


    /**
     * Verifica se esta bolha satisfaz uma determinada regra, gerada
     * pelo Aleph. Por exemplo:
     *
     *<pre>"chunk(A,left,np), chunk(A,right,np), inx(A,center:x,1,state)."</pre>
     *
     * @param sconditions
     * @return
     */
    public boolean satisfy(String sconditions) {
        String[] vconds= sconditions.split(",  *");
        //tools.print(vconds);
        //o.print("aaaaaaaaaa\n");
        //System.out.printf("vconds(.length: %d\n", vconds.length);

        Pattern pinx=  Pattern.compile("inx\\(A,([^,]*),([0-9]*),(.*)\\)");
        Pattern pchk=  Pattern.compile("chunk\\(A,([^,]*),(.*)\\)");

        for (int i = 0; i < vconds.length; i++) {
            Matcher mx= pinx.matcher(vconds[i]);
            //System.out.printf("   vconds(%d):... %s\n", i, vconds[i]);
            if ( mx.find() ) {
                String side= mx.group(1);
                int K= Integer.parseInt(mx.group(2));
                String value= mx.group(3);
                //System.out.printf("      side: %s   k: %d   value: %s\n", side, K, value);
                if ( !satisfy(side, K, value) )  {
                    //o.print('~');
                    return false;
                }
                //System.out.println("      OK");
            }
            else {
                Matcher mk= pchk.matcher(vconds[i]);
                if ( mk.find() ) {
                    String side= mk.group(1);
                    String value= mk.group(2);
                    if ( !satisfy(side, 0, value) )  {
                        //o.print('#');
                        return false;
                    }
                }
                else {
                    //o.print("o\n");
                    return false;
                }
            }
        }


        return true;
    }


    /**
     * Particularização do método satisfy/1.
     * @param side A região a testar - exemplo: left, center:x, right.
     * @param k A posição, dentro da região (>1). Se for igual a 0 então
     * não existe uma restrição posicional forte, mas sim uma restrição
     * regional. Por exemplo uma restrição relativa a "chunk".
     * @param value O valor a restringir, por exemplo uma palavra ou
     * uma etiqueta sintáctica.
     * @return
     */
    public boolean satisfy(String side, int k, String value) {
        //if ( rand.nextDouble() < 0.15 )
            //System.out.printf("side: %s   k: %d   value: %s\n", side, k, value);
        if ( k < 0 ) return false;

        Word W= null;

        boolean is_chunk_type= false;
        if ( k < 1 )
            is_chunk_type= true;
        else
            k--;

        int codigo= 0;
        if ( side.equals("left") ) {
            if ( WL == null )  return false;
            int n= WL.length;
            if ( n == 0 || k >= n ) return false;
            else {
                W= WL[n-k-1];
                codigo= 1;
            }
        }
        else if ( side.equals("right") ) {
            if ( WR == null || k >= WR.length )
                return false;
            else {
                W= WR[k];
                codigo= 2;
            }
        }
        else if ( side.equals("center:x") ) {
            if ( WX[0] == null || k >= WX[0].length )
                return false;
            else {
                W= WX[0][k];
                codigo= 3;
            }
        }
        else
            return false;

        //System.out.printf("      side: %s   k: %d   value: %s   codigo: %d\n", side, k, value, codigo);

        if ( W == null ) {
            System.out.printf("side: [%s]   k: [%d]   value: [%s]\n", side, k, value);
            System.out.printf("WX[0].len: %d   WX[1].len: %d   codigo: %d\n", WX[0].length, WX[1].length, codigo);
            this.print();
            for (int i = 0; i < WX[0].length; i++) {
                 System.out.printf("WX(%d) = [%20s]       WX(%d) = [%s]\n", i, WX[0][i], i, WX[1][i]);
            }
            //System.exit(1);
            return false;
        }


        // O literal é da forma "chunk(_, side, value)"
        if ( is_chunk_type ) {
            if ( value.equalsIgnoreCase("punct") ) {
                if ( !W.isRPUNCT() )
                    return false;
                else
                    return true;
            }
            if ( !value.equalsIgnoreCase("multi") && !value.equalsIgnoreCase(ChunkType.cod2str(W.getChkCod())) ) return false;
        }
        else {
            if ( value.startsWith("pos(") ) {
                // exemplo: inx(_, left, 1, pos(dt))
                int b= value.indexOf(')');
                String ptag= value.substring(4, b);
                //System.out.printf("%s(%d) = postag(%s)   pos(W): %s   W: [%s]\n", side, k, ptag, W.getPOS(), W);
                if ( ptag.equals("punct") ) {
                    if ( !W.isRPUNCT() )  {
                        //System.out.println("\n\nUNEXPECTED !!!");
                        //System.out.printf("%s(%d) = postag(%s)   pos(W): %s   W: [%s]\n\n\n", side, k, ptag, W.getPOS(), W);
                        return false;
                    }
                }
                /**/
                else if ( ptag.equals("undef") ) {
                    if ( !W.isRPUNCT() && W.getPosCod() < 0 )
                        return true;
                    else
                        return false;
                }
                /**/
                else {
                    int n= ptag.length();
                    if ( ptag.charAt(0) == '\'' && ptag.charAt(n-1) == '\'' )  ptag= ptag.substring(1, n-1);
                    if ( !W.getPOS().equalsIgnoreCase(ptag) )  return false;
                }
            }
            else {
                // exemplo: ink(_, right, 2, great)
                //System.out.printf("       x -----> [%s]\n", W.toString());
                if ( value.length() == 3 && value.charAt(0) == '\'' && value.charAt(2) == '\'' )  value= ""+value.charAt(1);

                if ( !W.toString().equalsIgnoreCase(value) )  return false;
            }
        }

        return true;
    }


    public String getSegment(boolean big) {
        StringBuilder sb= new StringBuilder(__vword2str(WL));
        if ( sb.length() > 0 )  sb.append(' ');

        sb.append(getX(big));
        if ( sb.length() > 0 )  sb.append(' ');

        sb.append(__vword2str(WR));
      
        return sb.toString();
    }
    
    
    public String getBigSegment() {
        return getSegment(true);
    }


    public String getSmallSegment() {
        return getSegment(false);
    }


    public String getX(boolean big) {
        int n0= numWords(WX[0]);
        int n1= numWords(WX[1]);
        Word[] W= n0 > n1 ? WX[0] : WX[1];
        if ( !big )
           W= n0 < n1 ? WX[0] : WX[1];

        return __vword2str(W);
    }


    public String getBigX() {
        return getX(true);
    }


    public String getSmallX() {
        return getX(false);
    }




    private String __vword2str(Word[] W) {
        if ( W == null )  return "";

        StringBuilder sb= new StringBuilder("");
        for (int i = 0; i < W.length; i++) {
            Word wi = W[i];
            if ( wi == null )  continue;
            if ( i>0 && !wi.isRPUNCT() ) sb.append(' ');
            sb.append(wi.toString());
        }

        return sb.toString();
    }



    /**
     * MAIN - For testing.
     * @param args
     */
    public static void main(String[] args) {
        POSType pos= new POSType();
        o.print("\n");
        pos.loadXML("/a/pen-pos-type.xml");
        o.printf("[NUMBER OF POS TAGS LOADED: %d]\n", pos.size());
        
        XBubble[] vxb= new XBubble[] {
            new XBubble("O/DT meu/WP gato/NN [?/? muito/RB gordo/JJ | __ __ bucha/JJ ] comeu/VB o/DT bolo/NN", pos),
            new XBubble("[overall/JJ | __] the/DT deal/NN ?/? It/in is/VB great/JJ !/punct", pos),
            new XBubble("O/DT gato/NN [muito/RB feliz/JJ e/DT gordo/JJ] comeu/VB o/DT bolo/NN", pos),
            new XBubble("bush/NN [ on/IN friday/NN | __ __ ] said/VBD the/DT", pos),
            new XBubble("vote/NN (gazum) on/IN the/DT [overall/JJ | __] deal/NN", pos),
            new XBubble("bush/NN [ on/IN friday/NN | __ __ ] said/VBD the/DT", pos),
            new XBubble("buss/NN [ levou/VB com/DT o/DT sapato/DT __ | __ __ __ __ apanhou/vb] dele/DT", pos),
            new XBubble(" [overall/JJ | __] the/DT deal/NN", pos),
            new XBubble("trust/VB in/IN the/DT [big/JJ | __] deal/NN", pos),
            new XBubble("o/DT bush/NN [ arcou/VB com/DT a/DT vergonha/JJ __ | __ __ __ __ apanhou/vb] em/PP ,/punct/punct", pos)
        };

        /*
        o.print("\n");
        for (int i=0; i<vxb.length; i++) {
            o.print("KEY ORDR ---> "); Toolkit.print(vxb[i].getOrdArray(pos)); o.print("\n");
            //o.printf("   NUMVOIDS:   WX(0) = %d   WX(1) = %d\n", numVoids(vxb[k].WX[0]), numVoids(vxb[k].WX[1]));
            //vxb[i].print_instance(3, POST, 7); //o.print("\n");
            vxb[i].print();
            o.print("\n\n");
        }
        o.print("\n");
        o.printf("NUMBER OF POS TAGS LOADED: %d\n", pos.size());

        o.printf("\n\nTESTE DE IGUALDADE:\n");
        for (int i=0; i< vxb.length; i++) {
            for (int j = i+1; j < vxb.length; j++) {
                if ( vxb[i].equal(vxb[j]) )
                    System.out.printf("   bub(%d) = bub(%d)\n", i, j);
            }
        }

        o.print("\n\n");
        o.printf("%s\n", tools.linha(100));
        o.print("LISTAGEM GERAL\n");
        o.printf("%s\n", tools.linha(100));
        for (int i = 0; i < vxb.length; i++) {
            o.printf("vxb(%d): %s\n", i, vxb[i].toString(true));
        }
        o.printf("%s\n", tools.linha(100));
        */

        
        String rule= "inx(A,left,1,pos(nn)), inx(A,center:x,1,pos(vb)), inx(A,right,2,pos(punct))";
        o.print("\n\n\n");
        o.printf("%s\n", Toolkit.sline(100));
        o.print("TESTE DE SATISFAZIBILIDADE CONDICIONAL\n");
        o.printf("RULE: %s\n", rule);
        o.printf("%s\n", Toolkit.sline(100));
        for (int i = 0; i < vxb.length; i++) {
            o.printf("BIG ------> [%s]\n", vxb[i].getBigSegment());
            o.printf("SMALL ----> [%s]\n", vxb[i].getSmallSegment());
            if (vxb[i].satisfy(rule)) {
                o.printf("   xbub(%2d):  %s\n", i, vxb[i].toString(true));
            }
            o.println();
        }
        o.printf("%s\n", Toolkit.sline(100));

        o.print("\n\n");

        XBubble xb= vxb[0];
        System.out.printf("\nWL ---> %s\n", xb.__vword2list(xb.WL, pos, -3));
        System.out.printf("\nWR ---> %s\n", xb.__vword2list(xb.WR, pos, 3));


        /*
        String sinst= vxb[0].print_instance(3, POST, 5);
        vxb[0].print_neg_instance(sinst, POST);
        o.print("\n");
        */ 
    }
}
