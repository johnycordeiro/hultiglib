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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hultig.io.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;


/**
 * <b>NOT YET WELL COMMENTED</b>. Representa um conjunto de etiquetas
 * sintácticas. Estas podem ser criadas dinâmicamente, ou
 * carregadas a partir de um ficheiro XML. 
 * @date 9:37:05 12/Jun/2008
 * @author J. P. Cordeiro
 */
public class POSType extends ArrayList<String[]> implements Serializable
{
    public static final long serialVersionUID = 6692922179875162089L;
    Random ran;  
    
    public POSType() {
        super();
        ran= new Random();
        String[][] tags = new String[][]{
            {"CC", "Coordinating conjunction"},
            {"CD", "Cardinal number"},
            {"DT", "Determiner"},
            {"EX", "Existential there"},
            {"FW", "Foreign word"},
            {"IN", "Preposition or subordinating conjunction"},
            {"JJ", "Adjective"},
            {"JJR", "Adjective, comparative"},
            {"JJS", "Adjective, superlative"},
            {"LS", "List item marker"},
            {"MD", "Modal"},
            {"NN", "Noun, singular or mass"},
            {"NNS", "Noun, plural"},
            {"NNP", "Proper noun, singular"},
            {"NNPS", "Proper noun, plural"},
            {"PDT", "Predeterminer"},
            {"POS", "Possessive ending"},
            {"PRP", "Personal pronoun"},
            {"PRP$", "Possessive pronoun"},
            {"RB", "Adverb"},
            {"RBR", "Adverb, comparative"},
            {"RBS", "Adverb, superlative"},
            {"RP", "Particle"},
            {"SYM", "Symbol"},
            {"TO", "to"},
            {"UH", "Interjection"},
            {"VB", "Verb, base form"},
            {"VBD", "Verb, past tense"},
            {"VBG", "Verb, gerund or present participle"},
            {"VBN", "Verb, past participle"},
            {"VBP", "Verb, non-3rd person singular present"},
            {"VBZ", "Verb, 3rd person singular present"},
            {"WDT", "Wh-determiner"},
            {"WP", "Wh-pronoun"},
            {"WP$", "Possessive wh-pronoun"},
            {"WRB", "Wh-adverb"},
            {"PCT", "Punctuation"}, //added by JPC, 2014/01/08
            {"BGN", "Sentence meta-delimiter: begin"},
            {"END", "Sentence meta-delimiter: end"},
        };
        set(tags);
    }
    
    
    public POSType(String[][] tags) {
        set(tags);
    }


    private void set(String[][] tags) {
        if ( tags == null )  return;
        this.addAll(Arrays.asList(tags));
    }
    
    
    public int index(String spos) {
        for (int k=0; k<size(); k++)
            if ( get(k)[0].equals(spos) )  return k;
        return -1;
    }
    
    
    public void insert(String[] vpos) {
        if ( vpos == null || vpos[0] == null )  return;
        
        int k= index(vpos[0]);        
        if ( k >= 0 ) {
            this.remove(k);
            this.add(k, vpos);
        }
        else
            this.add(vpos);
    }
    
    
    public int str2cod(String scod) {
        return index(scod);
        
    }
    
    
    public String cod2str(int cod) {
        if ( cod < 0 || cod >= size() )
            return "UNDEF";
        else
            return get(cod)[0];
    }
    
    
    /**
     * Gravação das etiquetas sintaticas em ficheiro
     * XML.
     * @param fname
     * @return
     */
    public boolean saveXML(String fname) {
        FileOUT fout= new FileOUT(fname);
        if ( !fout.open(("UTF-8")) ) return false;
        
        fout.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        fout.print("<root>\n");
        for (int k=0; k<size(); k++) {
            String[] pos= get(k);
            
            if ( k > 0 )  fout.print("\n");
            fout.print("   <postag>\n");
            fout.printf("      <pos>%s</pos>\n", pos[0]);
            fout.printf("      <dsc>%s</dsc>\n", pos[1] == null ? "" : pos[1]);
            fout.print("   <postag>\n");
        }
        fout.print("</root>\n");
        fout.close();
            
        return true;
    }
    
    
    public boolean loadXML(String fname) {
        try {
            FileInputStream is = new FileInputStream(fname);
            return loadXML(is);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(POSType.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    /**
     * Carregamento das etiquetas sintácticas, a partir de um 
     * ficheiro XML
     * @param fname O nome/caminho para o ficheiro.
     * @return Se houve sucesso ou não, no carregamento.
     */
    public boolean loadXML(InputStream istr) {
        //o.printf("[LOAD POS TAGS FILE: %s]\n", fname);
        clear();
        try {
            //Create the parse tree to doc.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(istr);

            Element root = doc.getDocumentElement();
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node pair = children.item(i);
                if (!(pair instanceof Element)) {
                    continue;                
                }
                
                //Nó <postag>
                String[] vpos = null;
                NodeList pairChild = pair.getChildNodes();
                for (int j = 0; j < pairChild.getLength(); j++) {
                    Node nj = pairChild.item(j);
                    if (!(nj instanceof Element)) {
                        continue;
                    }
                    Element ej = (Element) nj;
                    String tagname = ej.getTagName().toLowerCase();

                    if (tagname.equals("pos")) {
                        vpos= new String[2];
                        vpos[0]= ej.getTextContent();
                        vpos[1]= null;
                    }
                    
                    if (tagname.equals("dsc") && vpos != null ) {
                        vpos[1]= ej.getTextContent();
                    }
                }
                if ( vpos != null )
                    this.insert(vpos);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }

        o.print("[FILE LOADED]\n");
        return true;
    }
    
    
    /**
     * Útil para geração de instâncias negativas (2008/09/22)
     * @return
     */
    public String getRandomTag() {
        return get(ran.nextInt(size()))[0];
    }
    
    
    public int getCode(String spos) {
        for (int i = 0; i < this.size(); i++) {
            if ( this.get(i)[0].equals(spos) ) return i;
        }
        return -1;
    }
    
    
    /**
     * Prints the list of part-of-speech tags.
     */
    public void print() {
        o.print("\n");
        for (int k=0; k<size(); k++) {
            String[] vpos= get(k);
            o.printf("POS(%2d):   [%6s] - %s\n", k+1, vpos[0], vpos[1]);
        }
        o.print("\n");
    }
    
    /**
     * MAIN - For testing.
     * @param args
     */
    public static void main(String[] args) {
        POSType postyp= new POSType();
        postyp.print();
        System.out.println("\n\n");
        
        postyp.loadXML("/a/pen-pos-type.xml");
        postyp.print();
    }
    
    private static PrintStream o= System.out;
}
