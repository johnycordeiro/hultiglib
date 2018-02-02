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

import hultig.nlp.HashString;
import hultig.util.BestStrings;
import hultig.util.StringDouble;
import java.util.Vector;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <b>NOT YET WELL COMMENTED</b>.
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public class NewsCluster extends Vector<Text>
{
    HashString vocabDFS;
    
    
    public NewsCluster()
    {
        super();
        vocabDFS= null;
    }

    
    /**
     * This
     * @return
     */
    public int numSentences()
    {
        int num= 0;
        for (int i=0; i<size(); i++) {
            Text txt= get(i);
            num+= txt.size();
        }

        return num;
    }


    public void codify(CorpusIndex dict)
    {
        for (int i=0; i<size(); i++) {
            Text txt= get(i);
            txt.codify(dict);
            set(i,txt);
        }
    }
    
    
    public void computeDFs() {
        if ( vocabDFS == null ) {
            vocabDFS= new HashString();
        }
        for (Text t : this) {
            String[] tVocab= t.getVocab();
            for (String word : tVocab) {
                vocabDFS.countKey(word);
            }
        }
    }


    public Sentence[] getSentences()
    {
        if ( size() < 1 )  return null;
        
        Sentence[] vs= new Sentence[numSentences()];
        int k= 0;
        for (int i=0; i<size(); i++) {
            Text txt = get(i);
            for (int j=0; j<txt.size(); j++) {
                vs[k++]= txt.getSentence(j);
            }
        }

        return vs;
    }


    /**
     * Clean the whole set of sentences in the news cluster.
     * @return The set of cleaned sentences.
     */
    public Sentence[] getCleanSentences()
    {
        if ( size() < 1 )  return null;

        LinkedList<Sentence> L= new LinkedList<Sentence>();
        for (int i=0; i<size(); i++) {
            Text txt = get(i);
            for (int j=0; j<txt.size(); j++) {
                Sentence Sij= cleanSentence(txt.getSentence(j));
                double fsx= Sij.fracNumWords();
                //System.out.printf("fsx ---> %.5f   [%s]\n", fsx, Sij);

                if ( true && fsx >= 0.7 )  L.add(Sij);
            }
        }

        Sentence[] vs= new Sentence[L.size()];
        vs= L.toArray(vs);
        return vs;
    }
    
    
    public StringDouble[] getNMostRelevantWords(int n) {
        if ( vocabDFS == null ) {
            computeDFs();
        }
        BestStrings bestStrings= new BestStrings(n);
        int N= this.size();
        String[] keys= vocabDFS.getKeys();
        for (String word : keys) {
            double idfWord= Math.log(1.0*N/vocabDFS.getFrequency(word));
            bestStrings.insert(new StringDouble(word,idfWord));
        }
        
        return bestStrings.getStrings();
    }


    /**
     * Clean a sentence from extra and meta simbols, like HTML/XML
     * tags.
     * @param s The input sentence.
     * @return The cleaned sentence, may be the same if no dirt exist, or null
     * if the complete sentence is a nonsense sequence of simbols.
     */
    public static Sentence cleanSentence(Sentence stc) {
        if ( stc == null ) return null;

        String[] patterns = {
            "<[^>]*>",
            "_#[0-9]*"
        };

        String s= stc.toString();
        for (String ps : patterns) {
            Pattern p= Pattern.compile(ps);
            Matcher m= p.matcher(s);
            StringBuffer sb= new StringBuffer();
            int a= 0;
            while ( m.find() ) {
                int b= m.start();
                sb.append(s.substring(a, b));
                a= m.end();
            }
            sb.append(s.substring(a));
            s= sb.toString();
        }

        Sentence sto= new Sentence(s);
        sto.cod= stc.cod;

        return sto;
    }




    public static boolean startCluster(String s)
    {
        if ( s.matches(".*<cluster.*>.*") )  return true;
        else
            return false;
    }


    public static boolean endCluster(String s)
    {
        if ( s.matches(".*</cluster.*>.*") )  return true;
        else
            return false;
    }


    public static boolean startNew(String s)
    {
        if ( s.matches(".*<new.*>.*") )  return true;
        else
            return false;
    }


    public static boolean endNew(String s)
    {
        if ( s.matches(".*</new.*>.*") )  return true;
        else
            return false;
    }
}
