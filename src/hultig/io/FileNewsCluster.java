/*************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2011 UBI/HULTIG All rights reserved.
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
package hultig.io;

import hultig.sumo.CorpusIndex;
import hultig.sumo.NewsCluster;
import hultig.sumo.Sentence;
import hultig.sumo.Text;
import hultig.util.CronoSensor;
import hultig.util.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class was designed to handle a web news files, which are XML data files
 * containing news stories extracted from the web. The news are stored in
 * clusters of related stories. Therefore, the general structure of such a
 * file is illustrated below:
 * <pre>
 *    &lt;news-clusters>
 *       &lt;cluster i="1" url="http://news.google.com/...">
 *          &lt;new i="1" url="...">
 *             Wall Street stocks began the final week of one of their worst
 *             years...
 *          &lt;/new>
 *          ...
 *       &lt;/cluster>
 *       ...
 *       ...
 *    &lt;/news-clusters>
 *
 * </pre>
 * Each cluster is sequentially identified and contains the URL of its source,
 * as well as each new story. <br />
 * (18:22:07, 16, February, 2009)
 */
public class FileNewsCluster extends File
{
    /**
     * The corpus index reference for this class.
     */
    private CorpusIndex dictionary;

    /**
     * The list of news clusters loaded.
     */
    private ArrayList<NewsCluster> VCLUSTERS;

    /**
     * The default constructor.
     */
    public FileNewsCluster(String fpath) {
        super(fpath);
        VCLUSTERS= null;
    }
    
    /**
     * Gives the list of news clusters in this object. Each news cluster
     * contains a list of related news stories.
     * @return The list of news clusters.
     */
    public ArrayList<NewsCluster> getNewsClusters() {
        return VCLUSTERS;
    }


    public NewsCluster getNewsCluster(int index) {
        if ( VCLUSTERS == null || index<0 || index >= VCLUSTERS.size() )  return null;

        NewsCluster nc= VCLUSTERS.get(index);  nc.codify(dictionary);
        return nc;
    }
    
    /**
     * Gives the set of sentences contained in the {@code i}-th news cluster,
     * from this object.
     * @param index The {@code i}-th news cluster.
     * @return An array with all sentences from a given cluster.
     */
    public Sentence[] getNewsClusterSentences(int index) {
        if ( VCLUSTERS == null || index<0 || index >= VCLUSTERS.size() )  return null;
        
        NewsCluster nc= VCLUSTERS.get(index);  nc.codify(dictionary);
        Sentence[]  vs= nc.getSentences();
        return vs;
    }


    public Sentence[] loadAllSentences() {
        if ( VCLUSTERS == null ) {
            if ( !loadClusters() ) {
                return null;
            }
        }
        ArrayList<Sentence> allSentences= new ArrayList<Sentence>();
        for (int i = 0; i < VCLUSTERS.size(); i++) {
            NewsCluster nc= VCLUSTERS.get(i);  nc.codify(dictionary);
            Sentence[]  vs= nc.getSentences();
            if ( vs != null )
                allSentences.addAll(Arrays.asList(vs));
        }
        
        return allSentences.toArray(new Sentence[allSentences.size()]);
    }

    
    /**
     * Gives the reference to the corpus index used in this object.
     * @return A corpus index reference.
     */
    public CorpusIndex getDictionary() {
        return dictionary;
    }

    /**
     * Gives the number of clusters of web news stories loaded.
     * @return The number of clusters loaded.
     */
    public int getNumClusters() {
        if ( VCLUSTERS == null )
            return 0;
        else
            return VCLUSTERS.size();
    }

    /**
     * Loads news clusters contained in a given file. The loaded clusters
     * are stored in {@link #VCLUSTERS VCLUSTERS}.
     * @return The {@code true} value if the loading process succeeds,
     * and {@code false} otherwise.
     */
    @SuppressWarnings("static-access")
    public boolean loadClusters() {
        if ( !exists() || !isFile() ) {
            System.err.println("UNABLE TO FIND FILE: "+getAbsolutePath());
            return false;
        }
        try {
            String shead= Toolkit.sprintf("LOADING CLUSTERS FROM FILE \"%s\"", getAbsolutePath());
            String sline= Toolkit.sline('-', shead.length()+3);
            System.out.printf("%s\n", sline);
            System.out.printf("%s\n", shead);
            System.out.printf("%s\n", sline);
            
            CronoSensor crono = new CronoSensor();
            InputStream in = new FileInputStream(this);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));

            dictionary= new CorpusIndex();
            dictionary.clearHash();
            VCLUSTERS = new ArrayList<NewsCluster> ();
            for (int k = 0; ; k++) {
                NewsCluster cluster = new NewsCluster();
                if (!readCluster(br, cluster)) {
                    br.close();
                    break;
                }
                
                int ds= dictionary.hstab.size();

                System.out.printf("\tGroup: %3d  --->  dictionary.size(): %8d\n", k + 1, ds);
                VCLUSTERS.add(cluster);
            }
            System.out.printf("%d GROUPS LOADED (dt: %d ms)\n", VCLUSTERS.size(), crono.dt());
            System.out.printf("%s\n\n\n", sline);
        }
        catch (Exception exc) {
            System.err.printf("ERROR WHEN LOADING GROUPS (FILE: \"%s\")\n", getAbsolutePath());
            System.err.printf("ERROR: [%s]\n", exc.toString());
            return false;
        }
        
        dictionary.rebuild();
        return true;
    }

    /**
     * Reads a given news cluster, from the current file reader ({@code BufferedReader}).
     * @param br The file reader from which the news cluster should be read.
     * @param cluster An output parameter with the read news clusters.
     * @return boolean The {@code true} value if the loading process succeeds,
     * and {@code false} otherwise.
     * @throws Exception
     */
    public boolean readCluster(BufferedReader br, NewsCluster cluster) throws Exception {
        boolean incluster = false;
        Text txt = null;

        int txtID= 0;
        ArrayList<int[]> vk;
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
                txt = new Text();
                continue;
            }

            if (NewsCluster.endNew(line)) {
                //Ignore sentences with less than 3 words.
                txt.cutIfLessThan(3);

                //The cluster should contain at least 3 sentences.
                if (txt != null && txt.size() > 2) {
                    txt.toLowerCase();
                    for (int i=0; i<txt.size(); i++)
                        //==> Any sentence gets the code (id) of the news
                        //    story being processed.
                        txt.get(i).cod= txtID;

                    cluster.add(txt);
                    dictionary.addText(txt);
                }
                txt = null;
                txtID++;
                continue;
            }
            if (NewsCluster.endCluster(line)) {
                //o.printf("\tGroup has %d news\n", txt_id);
                return true; //way out.
            }

            //Adds a new sentence to the text.
            if (txt != null && passfilter(line)) {
                String s= cleanSentence(line.trim());
                Text ts= new Text(s);
                for (int i = 0; i < ts.size(); i++) {
                    Sentence sentence = ts.get(i);
                    //System.out.println("S--->"+sentence);
                    if ( sentence.fracNumWords() > 0.7 )  txt.add(sentence);
                    //System.out.println("ADDED");
                }
                //if (s.fracNumWords() > 0.7 )  txt.add(s);
            }
        }
    }


    /**
     * Defines a filter to be applied to the text, preventing certain
     * exotic or uninteresting strings to be rejected, as for example
     * lines with less than 5 characters, or sentences with less than
     * three words.
     *
     * @param line The string to be tested.
     * @return boolean The {@code true} value if the input string
     * passes the test, {@code false} otherwise.
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
     * Cleans a sentence string from certain extra/meta symbols, like HTML/XML
     * tags.
     * @param s The input sentence string.
     * @return The cleaned sentence string.
     * (8, August, 2008)
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
            StringBuilder sb= new StringBuilder();
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

    /**
     * Demonstrates the class main operators, including
     * the load and manipulation of web news stories.
     * @param args No parameters are expected.
     */
    public static void main(String[] args) {
        FileNewsCluster fnc= new FileNewsCluster("/a/news@google/n20090101-04h.xml");
        if ( !fnc.loadClusters() )  {
            System.err.println("Unable to load clusters from "+fnc.getAbsolutePath());
            return;
        }

        System.out.println("\n");
        System.out.printf("%d NEWS CLUSTERS WERE LOADED\n", fnc.getNumClusters(), fnc.getNumClusters());
        if ( fnc.getNumClusters() > 0 ) {
            System.out.printf("LIST OF SENTENCES FROM THE FIRST CLUSTER:\n");
            Sentence[] vs= fnc.getNewsClusterSentences(0);
            for (int i = 0; i < vs.length; i++) {
                System.out.printf("   [S%4d].... %s\n", i, vs[i]);
            }
        }

    }
}
