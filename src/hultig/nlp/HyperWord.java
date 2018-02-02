package hultig.nlp;

import hultig.io.FileIN;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * <p>
 * The HyperWord class ...
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 11:38:19 11/Out/2012
 * @author J. P. Cordeiro
 */
public final class HyperWord extends Hashtable<String, TermData> 
implements Serializable, Comparator<TermData>
{
    public static final long serialVersionUID = 7L;
    
    private String descriptionalText;
    
    /**
     * Contains the number of words processed, inserted in this hashtable.
     */
    private long totalNumOfWords= 0L;
    
    /**
     * The encoding being used for corpora reading.
     */
    protected String ENCODE;
    
    
    public final int ASCENDING= 1;
    public final int DESCENDING= -1;
    public int orderMode= ASCENDING;
    
    
    /**
     * Constructor.
     */
    public HyperWord() {
	super();
        setEncoding("UTF-8");
        descriptionalText= null;
    }


    public void setEncoding(String encode) {
        this.ENCODE= encode;
    }


    public String getEncoding() {
        return this.ENCODE;
    }


    public void countPair(String term, String term2) {
        TermData tdata= this.get(term);
	if ( tdata == null ) {
            tdata= new TermData(term);
            tdata.countNeighbour(term2);
            this.put(term, tdata);
        }
	else {
            tdata.countNeighbour(term2);
	    this.put(term, tdata);
	}
	totalNumOfWords++;
    }


    /**
     * Numero de strings incrementadas, na
     * estrutura, por exemplo: o numero
     * total de palavras.
     */
    public long getNumWords() {
	return totalNumOfWords;
    }


    /**
     * Gives the frequency of a given string/term/word.
     */
    public long getFrequency(String key) {
	if ( key == null ) {
            return -1;
        }

	TermData td = this.get(key);
        return td ==null ? 0 : td.getTermFrequency();
    }


    /**
     * Probabilidade estimada da string "key",
     * na estrutura.
     */
    public double getProbability(String key) {
	double p= (double) getFrequency(key) / getNumWords();
	return p;
    }

    /**
     * Gives the set of keys from this hashtable, in the form
     * of an array os strings.
     */
    public String[] getKeys() {
	String[] vs= new String[size()];

	Enumeration e= keys();
	for (int k=0; k<size(); k++) {
            vs[k]= (String)e.nextElement();
        }

	return vs;
    }
    
    
    public void processFile(String fileName) {
        FileIN f= new FileIN(fileName);
        if ( f == null || !f.isFile() ) {
            System.err.println("ERROR OPENNING FILE "+fileName);
            return;
        }
        f.setEncoding("ISO-8859-1");
        f.open();
        System.out.println("PROCESSING FILE "+fileName);
        for(;;) {
            String line= f.read();
            if ( line == null ) {
                break;
            }
            String[] vBlocos= line.toLowerCase().split("[.;,!?]+");
            for (String bloco : vBlocos) {
                String[] vs= bloco.split("[ ]+");
                processBloco(vs);
            }
        }
        System.out.println("COMPLETE");
        f.close();
    }
    
 
    private void processBloco(String[] v) {
        if ( v.length < 2 ) {
            return;
        }
        for (int i = 0; i < v.length-1; i++) {
            if ( isWord(v[i]) ) {
                if ( isWord(v[i+1]) ) {
                    countPair(v[i], v[i+1]);
                }
                else { //==> avança ...
                    countPair(v[i], "NOT_A_WORD");
                    i++;
                }
                this.totalNumOfWords++;
            }
        }
    }
    
    
    private boolean isWord(String s) {
        if ( s == null || s.length()<1 ) {
            return false;
        }    
        if ( Character.isLetter(s.charAt(0)) ) {
            if ( Character.isLetter(s.charAt(s.length() - 1)) ) {
                return true;
            }
        }
        return false;
    }
    
    
    public void printAllNumMaxNeigh(int numMax) {
        ArrayList<TermData> terms= new ArrayList<TermData>(values());
        orderMode= ASCENDING;
        Collections.sort(terms,this);
        
        for (int i = 0; i < Math.min(terms.size(), 1000); i++) {
            TermData tdata = terms.get(i);
            System.out.printf("\n### ID[%d]   ", i);
            tdata.printBestNeighbours(numMax);
        }
    }
    
    
    public boolean save(String fname) {
	try  {
	    FileOutputStream ostream= new FileOutputStream(fname);
	    ObjectOutputStream oos= new ObjectOutputStream(ostream);

	    oos.writeObject(this);
	    oos.flush();
	    oos.close();
	}
	catch (Exception e)  {
	    System.out.printf("Error saving object: %s\n%s\n", this.getClass(), e);
	    return false;
	}

	return true;
    } 
    
    
    public void load(HyperWord h) {
	this.clear();
	Enumeration keys= h.keys();
	while ( keys.hasMoreElements() )  {
	    String skey= (String)keys.nextElement();
	    TermData tdata= h.get(skey);
            this.put(skey, tdata);
	}
    }


    public boolean load(String fname) {
	try  {
	    FileInputStream istream= new FileInputStream(fname);
	    ObjectInputStream ois= new ObjectInputStream(istream);

	    HyperWord hword=(HyperWord) ois.readObject();
	    this.load(hword);
	    ois.close();
	}
	catch (Exception e)  {
	    System.out.println("Error Loading HyperWord: "+e);
	    return false;
	}

	return true;
    }    
    
    
    public static void main(String[] args) {
        args= new String[]{"/a/hyperWord.hst"};
        
        HyperWord hyperw= new HyperWord();
        if ( args.length < 1 ) {
            hyperw.processFile("/a/ctempub/P01.txt");
            hyperw.save("/a/hyperWord.hst");
        }
        else {
            hyperw.load(args[0]);
        }
        
        hyperw.printAllNumMaxNeigh(20);
    }

    
    public int compare(TermData o1, TermData o2) {
        return orderMode * (o1.compareTo(o2));
    }        
}
