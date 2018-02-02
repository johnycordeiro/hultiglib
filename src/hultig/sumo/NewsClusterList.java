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

import hultig.io.FileOUT;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hultig.util.CronoSensor;
import hultig.util.OSArguments;
import hultig.util.Toolkit;


/**
 * <b>NOT YET WELL COMMENTED</b>.
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro (March 26, 2009)
 * @version 1.0
 */
public class NewsClusterList extends ArrayList<NewsCluster> implements Serializable
{
    public static Toolkit to;
    CorpusIndex  dictionary;



    public NewsClusterList() {
        dictionary= null;
    }


    public NewsClusterList(String filename) {
        this();
        loadClusters(filename);
    }



     /**
     * Load all news groups from a given file, the one that is
     * defined with the <b>infile</b> attribute.
     * @return boolean
     */
    @SuppressWarnings("static-access")
    public boolean loadClusters(String filename) {
        try {
            to.print("\n\n");
            String shead= to.sprintf("LOADING GROUPS FROM FILE \"%s\"", filename);
            String sline= to.sline('-', shead.length()+3);
            to.printf("%s\n", sline);
            to.printf("%s\n", shead);
            to.printf("%s\n", sline);

            CronoSensor crono = new CronoSensor();
            InputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            dictionary= new CorpusIndex();
            dictionary.clearHash();
            //vclusters = new Vector<NewsCluster> ();
            int numstc= 0;
            for (int k = 0; ; k++) {
                //System.out.println("k:... "+k);
                NewsCluster cluster = new NewsCluster();
                if (!readCluster(br, cluster)) {
                    br.close();
                    break;
                }
                numstc+= cluster.numSentences();
                System.out.printf("CLUSTER(%2d) .... processing ... #sentences: %d\n", k, numstc);

                int ds= dictionary.hstab.size();

                //to.printf("\tGroup: %3d  --->  dictionary.size(): %8d\n", k + 1, ds);
                add(cluster);
            }
            System.out.printf("%d GROUPS LOADED (dt: %d ms)\n", size(), crono.dt());
            to.printf("%s\n", sline);
        }
        catch (Exception exc) {
            System.err.printf("ERROR WHEN LOADING GROUPS (FILE: \"%s\")\n", filename);
            System.err.printf("ERROR: [%s]\n", exc.toString());
            exc.printStackTrace();
            return false;
        }

        dictionary.rebuild();
        return true;
    }


    /**
     * Read the next news cluster, from the current reader, given by @param br.
     *
     * @param br BufferedReader
     * @param cluster NewsCluster
     * @return boolean
     * @throws Exception
     */
    public boolean readCluster(BufferedReader br, NewsCluster cluster) throws Exception {
        boolean incluster = false;
        Text txt = null;

        int txt_id= 0;
        for (;;) {
            String line = br.readLine();
            if (line == null) {
                //o.printf("NEWS CLUSTER HAS %d NEWS\n", txt_id);
                return false; //way out.
            }
            if (NewsCluster.startCluster(line)) {
                incluster = true;
                continue;
            }
            if (!incluster) {
                continue;
            }
            if (NewsCluster.startNew(line)) {
                //System.out.println("START NEW");
                txt = new Text();
                continue;
            }

            if (NewsCluster.endNew(line)) {
                //System.out.printf("END NEW:...... %d\n", txt.size());
                //Cut sentences with less than 3 words.
                if ( txt != null )  txt.cutIfLessThan(3);

                //group should contain at least 3 sentences.
                if (txt != null && txt.size() > 2) {
                    txt.toLowerCase();
                    for (int i=0; i<txt.size(); i++)  txt.get(i).cod= txt_id;

                    cluster.add(txt);
                    dictionary.addText(txt);
                }
                txt = null;
                txt_id++;
                continue;
            }
            if (NewsCluster.endCluster(line)) {
                //o.printf("\tGroup has %d news\n", txt_id);
                return true; //way out.
            }

            //Add a new sentence to the text.
            if (txt != null && passfilter(line)) {
                String s= cleanSentence(line.trim());
                //System.out.printf("   s ---> [%s]\n", s);
                Text ts= new Text(s);
                for (int i = 0; i < ts.size(); i++) {
                    Sentence sentence = ts.get(i);
                    if ( sentence.fracNumWords() > 0.7 )  txt.add(sentence);
                }
                //if (s.fracNumWords() > 0.7 )  txt.add(s);
            }
        }
    }


    /**
     * Define a filter to apply on the text.
     *
     * @param line String
     * @return boolean
     */
    public boolean passfilter(String line) {
        if (line == null || line.length() < 5) {
            return false;
        }
        String s = line.toLowerCase();
        if (s.matches(".*all rights reserved.*")) {
            return false;
        }

        StringTokenizer st = new StringTokenizer(s);
        if (st.countTokens() < 3) {
            return false;
        }

        return true;
    }


    /**
     * Clean a sentence from extra and meta simbols, like HTML/XML
     * tags.
     * @param s The readLn sentence.
     * @return The cleaned sentence, may be the same if no dirt exist, or null
     * if the complete sentence is a nonsense sequence of simbols.
     * JPC 2008/12/08
     */
    public static String cleanSentence(String s) {
        if ( s == null ) return null;

        String[] patterns = {
            "<[^>]*>",
            "_#[0-9]*",
            "&quot;"
        };

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

        return s;
    }


    public void printAllSentences(OpenNLPKit model) {
        int k= 0;
        for (int i = 0; i < this.size(); i++) {
            NewsCluster nc= this.get(i);
            Sentence[] vst= nc.getSentences();
            for (int j = 0; j < vst.length; j++) {
                Sentence s = vst[j];
                ChunkedSentence cs= new ChunkedSentence(s,model);
                System.out.printf("%7d   -------> [%s]\n", ++k, cs);
            }

            if ( i == 1 )  break;
        }
    }



    public static void gerar_pos_corpus(OpenNLPKit model, String foutname) {
        FileOUT fout= new FileOUT(foutname);
        if ( !fout.open() ) {
            System.err.printf("\n\tERROR OPENING OUTPUT FILE: [%s]\n\n", foutname);
            return;
        }

        String dirname= "/a/news@google";
        // Amostra aleatóriamente recolhida.
        String[] vfich = {
            "n20051228-17h.xml",
            "n20061027-05h.xml",
            "n20061124-05h.xml",
            "n20061205-05h.xml",
            "n20061207-05h.xml",
            "n20061209-05h.xml",
            "n20061212-05h.xml",
            "n20061219-05h.xml",
            "n20070129-05h.xml",
            "n20070131-05h.xml",
            "n20070205-05h.xml",
            "n20070305-05h.xml",
            "n20070319-05h.xml",
            "n20070321-05h.xml",
            "n20070331-05h.xml",
            "n20070403-05h.xml",
            "n20070513-05h.xml",
            "n20070519-05h.xml",
            "n20070520-05h.xml",
            "n20070523-05h.xml",
            "n20070525-05h.xml",
            "n20070529-05h.xml",
            "n20070602-05h.xml",
            "n20070607-05h.xml",
            "n20070613-05h.xml",
            "n20070622-05h.xml",
            "n20070706-05h.xml",
            "n20070715-05h.xml",
            "n20070716-05h.xml",
            "n20070719-05h.xml",
            "n20081126-04h.xml"
        };

        long cont_stc= 0L;
        CronoSensor crono= new CronoSensor();
        for (int i = 0; i < vfich.length; i++) {
            String pfile = dirname + '/' + vfich[i];
            //System.out.println(pfile);

            NewsClusterList Lnc= new NewsClusterList(pfile);
            System.out.println("\n\n");
            for (int j = 0; j < Lnc.size(); j++) {
                System.out.printf("PROCESSING CLUSTER %d ...\n", j);
                NewsCluster nc= Lnc.get(j);
                Sentence[] vst= nc.getSentences();
                if ( vst != null ) {
                    for (int k = 0; k < vst.length; k++) {
                        Sentence s = vst[k];
                        ChunkedSentence cs= new ChunkedSentence(s,model);
                        fout.printf("%s\n", cs.getSPOSig());
                    }
                }

                //if ( j == 1 )  break;
            }

            //break;
        }
        fout.close();
        System.out.println("\n\n");
        System.out.printf("%d NEWS FILE PROCESSED.\n", vfich.length);
        System.out.printf("%s TIME SPENT.\n", crono.dts());
    }



    /**
     * MAIN - For testing.
     * @param args
     */
    public static void main(String[] args) {
        OSArguments osa= new OSArguments(args);

        System.out.println("[LOAD OPEN NLP MODEL]");
        OpenNLPKit model= new OpenNLPKit("/a/tools/opennlp-tools-1.3.0/models/english");
        System.out.println("[MODEL LOADED]");

        if ( osa.contains("genPOScorpus") ) {
            String foutname= osa.get("genPOScorpus");
            gerar_pos_corpus(model, foutname);
            return;
        }


        NewsClusterList Lnc= new NewsClusterList("/a/news@google/n20070421-05h.xml");
        System.out.println("\n");
        Lnc.printAllSentences(model);
        System.out.println("\n");
    }
}
