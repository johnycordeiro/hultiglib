package hultig.nlp;

import hultig.io.FileIN;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import hultig.util.BasicStats;
import hultig.util.CronoSensor;
import hultig.util.OSArguments;
import hultig.util.StringInteger;
import hultig.util.StringIntegerList;
import hultig.util.Toolkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * <p>
 * The HashString class ...
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 11:26:07 8/Out/2012
 * @author J. P. Cordeiro
 */
public final class HashString extends Hashtable<String, Long> implements Serializable
{
    public static final long serialVersionUID = 7L;

    
    /**
     * Contains the number of words processed, inserted in this hashtable.
     */
    private long totalNumOfWords= 0L;
    
    /**
     * The encoding being used for corpora reading.
     */
    protected String ENCODE;
    
    /**
     * To be used in the implemented command line
     */
    File currentDir;
    
    /**
     * Constructor.
     */
    public HashString() {
	super();
        setEncoding("UTF-8");
        currentDir= new File(System.getProperty("user.home"));
    }


    public void setEncoding(String encode) {
        this.ENCODE= encode;
    }


    public String getEncoding() {
        return this.ENCODE;
    }

    
    public void countKey(String skey)
    {
        countKey(skey,1);
    }


    public void countKey(String skey, long numTimes) {
        Long I= this.get(skey);
	if ( I == null ) {
            this.put(skey, numTimes);
        }
	else {
	    this.put(skey, I+numTimes);
	}
	totalNumOfWords+= numTimes;
    }


    /**
     * Numero de strings incrementadas, na
     * estrutura, por exemplo: o numero
     * total de palavras.
     * @return The total number of words.
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

	Long I= this.get(key);
        return I ==null ? 0 : I.longValue();
    }


    /**
     * Probabilidade estimada da string "key",
     * na estrutura.
     */
    public double getProbability(String key)
    {
	double p= (double) getFrequency(key) / getNumWords();
	return p;
    }

    /**
     * Gives the set of keys from this hashtable, in the form
     * of an array os strings.
     */
    public String[] getKeys()
    {
	String[] vs= new String[size()];

	Enumeration e= keys();
	for (int k=0; k<size(); k++) {
            vs[k]= (String)e.nextElement();
        }

	return vs;
    }


    /**
     * Gives an array containing the "{@code number}" most 
     * frequent words.
     * @param number
     * @return The array with most frequent words.
     */
    public String[] getExtremes(int number, boolean most) {       
	number= Math.min(number, size());
        ArrayList<StringInteger> vbest= new ArrayList<StringInteger>(number);

        Enumeration<String> e= keys();
	while ( e.hasMoreElements() ) {
            String w= e.nextElement();
            long   f= getFrequency(w);
            
            boolean inserted= false;
            for (int i=0; i < vbest.size(); i++) {
                StringInteger vbi= vbest.get(i);
                if ( most && f > vbi.n || !most && f < vbi.n ) {
                    vbest.add(i, new StringInteger(w,f));
                    inserted = true;
                    if ( vbest.size() > number ) {
                        vbest.remove(vbest.size()-1);
                    }
                    break;
                }
            }
            
            if ( !inserted  &&  vbest.size() < number ) {
                vbest.add(new StringInteger(w,f));
            }
        }
        
        String[] vs= new String[vbest.size()];
        for (int i = 0; i < vs.length; i++) {
            StringInteger si= vbest.get(i);
            vs[i] = String.format("%s/%.7f", si.toString(), 1.0*si.n/getNumWords());
        }
	return vs;
    }
    
    /**
     * Select all the keys having value greater than a given minimum. 
     * @param min The minimum value.
     * @return The array with the selected keys.
     */
    public String[] getKeysWithValueGreatherThan(int min) {
        ArrayList<String> vkeys= new ArrayList<String>();
        Enumeration<String> e= keys();
	while ( e.hasMoreElements() ) {
            String w= e.nextElement();
            long   f= getFrequency(w);
            if ( f > min ) {
                vkeys.add(w);
            }
        }
        String[] vs= new String[vkeys.size()];
        return vkeys.toArray(vs);
    }
    
    
    public String[] getLessFrequent(int number) {
        boolean mostFrequent= false;
        return getExtremes(number, mostFrequent);
    }
    
    
    public String[] getMostFrequent(int number) {
        boolean mostFrequent= true;
        return getExtremes(number, mostFrequent);        
    }
    
    
    public BasicStats getBasicStats() {
        BasicStats bstat= new BasicStats();
        
        Enumeration<String> e= keys();
	while ( e.hasMoreElements() ) {
            String w= e.nextElement();
            long   f= getFrequency(w);
            bstat.addValue(f);
        }        
        return bstat;
    }
    
    /**
     * Removes all entries satisfying a given pattern, provided by
     * a regular expression.
     * @param regExpression The regular expression.
     */
    public void cleanWithPattern(String regExpression) {
        TreeSet<String> tree= new TreeSet<>(keySet());
	Iterator iter= tree.iterator();
        Pattern pattern= Pattern.compile(regExpression);
	for (int i=0; iter.hasNext(); i++)  {
	    String skey=(String) iter.next();
            Matcher matcher= pattern.matcher(skey);
            if ( matcher.find() ) {
                long TF= get(skey);
                totalNumOfWords-= TF;
                this.remove(skey);
                System.out.printf("# (TF:%7d)  %s  removed\n", TF, skey);
            }
	}
    }
    
    public void printMassLessThan(double pmax) {
        StringIntegerList L= new StringIntegerList();
        String[] words= getKeys();
        for (String w : words) {
            L.add(new StringInteger(w,get(w)));
        }
        L.sort();
        int quartil= 0;
        long sum= 0L;
        for (int i = 0; i < L.size(); i++) {
            StringInteger si= L.get(i);
            sum+= si.n;
            double p= (double)sum/totalNumOfWords;
            System.out.printf("%-20s   %6.3f%%      (%06d ~~ %6.3f%%)         tf:%d\n", si.s, 100*p, i, 100.0*i/size(), si.n);
            if ( p > pmax ) break;
        }
    }


    /**
     * Imprime ordenadamente a tabela de Hash.
     */
    public void print()
    {
	print(null,null);
    }


    /**
     * Imprime ordenadamente a tabela de Hash. Os valores
     * impressos estarão compreendidos entre <b>sa</b> e
     * <b>sb</b> se estes dois parametros forem não
     * nulos.
     */
    public void print(String sa, String sb)
    {
	if ( size() < 1 ) return;

	TreeSet<String> tree= new TreeSet<String>(keySet());
	SortedSet sset= tree.tailSet(tree.first());
	Iterator iter= sset.iterator();
        
	for (int i=0; iter.hasNext(); i++)  {
	    String skey=(String) iter.next();
	    if ( sa != null  &&  skey.compareTo(sa) < 0 )  continue;
	    if ( sb != null  &&  skey.compareTo(sb) > 0 )  break;
	    System.out.printf("%4d   %10d   [%s]\n", i, get(skey), skey);
	}
    }


    /**
     * Similar to the method with two arguments. A more general version of it,
     * by considering all tokens, instead of only verbs.
     * @param filename
     * @return
     */
    public boolean processFile(String fname) {
        return processFile(fname, false, false);
    }


    /**
     * Read the text from a file and incrementally setup the hashtable.
     * @param fname
     * @param onlyWords
     * @return
     */
    public boolean processFile(String fname, boolean onlyWords, boolean caseSensitive) {
	try  {
            InputStream in = new FileInputStream(genPathForFname(fname));
	    BufferedReader br= new BufferedReader(new InputStreamReader(in,ENCODE));

	    for(;;)  {
		String line= br.readLine();
		if ( line == null )  break;
                
                if ( !caseSensitive )  line= line.toLowerCase();
                String[] words= line.split("[ ,.;:()\"\'!?]+");
                for (String w : words) {
                    int N= w.length();
                    if ( N < 1 ) continue;
                    char c0= w.charAt(0);
                    char cN= w.charAt(N-1);
                    if ( !onlyWords || Character.isLetter(c0) && Character.isLetter(cN) )
                        countKey(w);
                }
	    }

	    br.close();
	}
	catch (Exception e)  {
	    System.out.println("ERROR - "+e);
	    return false;
	}

	return true;
    }
    
    
    public void processDirectory() {
        processDirectory(currentDir.getAbsolutePath());
    } 
    
    
    public void processDirectory(String dirPath) {
        File fdir= new File(dirPath);
        if ( !fdir.isDirectory() ) return;
        
        String[] files= fdir.list();
        for (String fn : files) {
            if ( fn.endsWith(".txt") ) {
                System.out.printf("Processing File %s ....... ", fn);
                processFile(dirPath+'/'+fn, true, false);
                System.out.printf("|Vocab| = %d\n", size());
            }
        }
    }
    
    
    public void printHistogram(int maxTF) {
        BasicStats bstat= new BasicStats();
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setGroupingSize(3);
        
        int[] vconts= new int[maxTF+1];
        Enumeration<String> e= keys();
	while ( e.hasMoreElements() ) {
            String w= e.nextElement();
            long   f= getFrequency(w);
            bstat.addValue(f);
            if ( f > maxTF) {
                vconts[maxTF]++;
            }
            else {
                vconts[(int)f-1]++;
            }
        }
        
        double sumF= 0.0;
        for (int i = 0; i < vconts.length-1; i++) {
            double pwi= 100.0 * (i+1) * vconts[i]/totalNumOfWords;
            sumF+= pwi;
            System.out.printf("|F(w)  = %4d|  ... %7d   %7.4f%%    %6.5f %%\n", i+1, vconts[i], pwi, sumF);
        }
        
        System.out.printf(    "|F(w)  > %4d|  ... %7d   %7.4f%%\n", vconts.length, vconts[vconts.length-1], 100.0-sumF);
        bstat.multiplyAllValuesBy(1.0/size());
        
        bstat.print(     "\nWORD PROBABILITIES");
        System.out.println("------------------");
        System.out.printf("| Vocabulary |    =  %12s\n", decimalFormat.format(size()));
        System.out.printf("|Proc. Tokens|    =  %12s\n", decimalFormat.format(totalNumOfWords));
    }


    /**
     * Carrega a Hashtable, a partir de uma outra
     * tabela de Hash.
     */
    public void load(HashString ht) {
	this.clear();
	Enumeration keys= ht.keys();
	while ( keys.hasMoreElements() )  {
	    String skey= (String)keys.nextElement();
	    this.countKey(skey, ht.get(skey));
	}
        //System.out.println("SIZE ---> "+size());
    }


    /**
     * Loads the hashtable directly from an object file previously saved.
     * @param fname The path to the object file.
     * @return The success flag.
     */
    public boolean load(String fname)
    {
	try  {
	    FileInputStream istream= new FileInputStream(genPathForFname(fname));
	    ObjectInputStream ois= new ObjectInputStream(istream);

	    HashString hstr=(HashString) ois.readObject();
	    this.load(hstr);
	    ois.close();
            //System.out.println("SIZE ---> "+hstr.size());
	}
	catch (Exception e)  {
	    System.out.println("Error Loading HashString: "+e);
	    return false;
	}

	return true;
    }
    
    
    public void reduceByWordStemming() {
        Enumeration<String> enumKeys= this.keys();
        while ( enumKeys.hasMoreElements() ) {
            String word= enumKeys.nextElement();
            String wost= Stemmer.stem(word);
            Long fqWord= get(word);
            remove(word);  totalNumOfWords-= fqWord;
            countKey(wost, fqWord);
        }
    }


    /**
     * Grava a Hashtable num ficheiro com o nome "fname".
     */
    public boolean save(String fname)
    {
	try  {
	    FileOutputStream ostream= new FileOutputStream(genPathForFname(fname));
	    ObjectOutputStream oos= new ObjectOutputStream(ostream);

	    oos.writeObject(this);
	    oos.flush();
	    oos.close();
	}
	catch (Exception e)  {
	    System.out.println("Error Saving HashString"+e);
	    return false;
	}

	return true;
    }
    
    
    private String genPathForFname(String fname) {
        if ( fname == null || fname.length() < 1 )  return null;
        
        if ( fname.charAt(0) == '/' ) return fname;
        return currentDir.getAbsolutePath() + '/' + fname;
    }
    
    /**
     * Imports the hashtable from a text file previously exported. This method
     * was lately created (2016/01/07) to cope with previous object versions.
     * @param fileName The path to the text file to import.
     */
    private void importTableFromFile(String fileName) {
        FileIN f= new FileIN(fileName);
        if ( f==null || !f.exists() || !f.isFile() ) {
            System.err.println("ERROR: cannot read from indicated file "+fileName);
            return;
        }
        f.open();
        long count= 0;
        HashString h= new HashString();
        for (;;) {
            String    s= f.read(); if (s == null) break;
            //System.out.println(s);
            String[] as= s.split("[ ]+");
            if ( as.length < 3) continue;
            try {
                int TF=  Integer.parseInt(as[1]);
                char c0= as[2].charAt(0);
                char cn= as[2].charAt(as[2].length()-1);
                if ( c0 == '[' && cn == ']' )  as[2]= as[2].substring(1, as[2].length()-1);
                if ( as[2].length() > 0 ) {
                    h.countKey(as[2], new Long(TF));
                    count++;
                }
            }
            catch (Exception x) {}
        }
        if ( count > 0 ) {
            System.out.printf("# %d terms imported from file %s\n", count, fileName);
            load(h);
        }
        f.close();
    }


    /**
     *  -------
     *  M A I N
     *  -------
     */
    public static void main(String[] args)
    {
        OSArguments osa= new OSArguments(args);
        if ( osa.contains("-procfolder") ) {
            String folderPath= osa.get("-procfolder");
            return;
        }
        
	HashString hasht= new HashString();
        hasht.setEncoding("ISO-8859-1");
	String command;

	while ( true )  {
	  System.out.print("HashString> ");
	  command= Toolkit.readLn().trim();
	  if ( command.startsWith("exit") ) {
                break;
          }
	  parse(command,hasht);
	}
    }


    private static boolean parse(String command, HashString ht) {  
	if ( command == null || command.length() < 1 || command.equals("help")  ||  command.equals("?") )  {
            System.out.println("\tbest [num] ............. Lists the num most frequent words.");
            System.out.println("\tclean <pattern> ........ Clean all tokens satisfying a given regular expression.");
            System.out.println("\texit ................... Terminates this command  prompt.");
            System.out.println("\timport <filename> ...... Imports table from a text format.");
	    System.out.println("\tload <filename> ........ Load the HashString from a given object file.");
	    System.out.println("\tprint .................. Prints the hash keys.");
	    System.out.println("\tproc <filename> ........ Process a plain text file.");
	    System.out.println("\tsave <filename> ........ Save this HashString into a file.");
            System.out.println("\tstats .................. Corpus mean frequency and stdv.");
            System.out.println("\ttotal .................. Prints the total number of tokens counted on corpora.");
            System.out.println("\tworst [num] ............ Lists the num less frequent words.");
	    return true;
	}

	if ( command.startsWith("print") )  {
	    ht.print();
	    return true;
	}
        
        if ( Character.isDigit(command.charAt(0)) ) {
            command= "proc /a/ctempub/P0"+command+".txt";
        }
        
        if ( command.startsWith("clean") ) {
            String spattern= command.substring(5).trim();
            ht.cleanWithPattern(spattern);
            return true;
        }
        
        if ( command.startsWith("import") ) {
            System.out.println("# importing data ...");
            String fileName= command.substring(6).trim();
            ht.importTableFromFile(fileName);
            return true;
        }
        
        if ( command.startsWith("total") ) {
            System.out.printf("# total number of counts: %d\n", ht.getNumWords());
            return true;
        }

	if ( command.startsWith("proc") )  {  
	    String filename= command.substring(4).trim();
            CronoSensor time= new CronoSensor();
            System.out.println("-------------------------------------------------------------------");
	    System.out.printf("PROCESSING FILE %s", filename);
	    boolean bool= ht.processFile(filename, true, false);
            System.out.printf("   Nª TOKENS: %d\n", ht.getNumWords());
	    System.out.println("-------------------------------------------------------------------");
	    System.out.printf("PROCESSED / VOCAB. SIZE: %d\n", ht.size());
            System.out.printf("TIME TAKEN: %s\n\n", time.dts());
	    return bool;
	}
        
        if ( command.startsWith("pdir") )  {
            String dirPath= command.substring(4).trim();
            if ( dirPath.length() > 0 )
                ht.processDirectory(dirPath);
            else
                ht.processDirectory();
            return true;
        }

	if ( command.startsWith("save") )  {
	    String filename= command.substring(4).trim();
	    return ht.save(filename);
	}

	if ( command.startsWith("load") )  {
	    String filename= command.substring(4).trim();
	    return ht.load(filename);
	}
        
        if ( command.charAt(0) == '$' ) {
            String w= command.substring(1).trim();
            System.out.printf("$(%s) = %d\n", w, ht.getFrequency(w));
            return true;
        }
        
        if ( command.startsWith("best") ) {
            String sn= command.substring(4).trim();
            int i=0, n; 
            try {
                n= Integer.parseInt(sn);
            }
            catch(Exception x) {
                n= 10;
            }
            String[] vbest= ht.getMostFrequent(n);
            for (String w : vbest) {
                String[] vs= w.split("/");
                System.out.printf("   %5d   %-20s   %10s       %s\n", ++i, vs[0], vs[1], vs[2]);
            }
            return true;
        }
        
        if ( command.startsWith("worst") ) {
            String sn= command.substring(5).trim();
            int i=0, n= Integer.parseInt(sn);
            String[] vbest= ht.getLessFrequent(n);
            for (String w : vbest) {
                String[] vs= w.split("/");
                System.out.printf("   %5d   %-20s   %10s       %s\n", ++i, vs[0], vs[1], vs[2]);
            }
            return true;
        }
        
        if ( command.startsWith("stats") ) {
            String sn= command.substring(5).trim();
            double pmax= 0.75;
            if ( sn.length() > 0 ) pmax= Double.parseDouble(sn);
            ht.printMassLessThan(pmax);
            return true;
        }
        
        if ( command.startsWith("hist") ) {
            int n= 100;
            String sn= command.substring(4).trim();
            if ( sn.length() > 0 ) n= Integer.parseInt(sn);
            ht.printHistogram(n);
            return true;
        }
        
        if ( command.startsWith("ls") || command.startsWith("dir")) {
            String[] files = ht.currentDir.list();
            System.out.println("PATH: " + ht.currentDir.getAbsolutePath());
            for (int i = 0; i < files.length; i += 3) {
                for (int j = 0; j < 3; j++) {
                    if (i + j > files.length - 1) {
                        break;
                    }
                    System.out.printf("%-30s   ", files[i + j]);
                }
                System.out.println();
            }
        }
        
        if ( command.startsWith("cd") ) {
            String direction= command.substring(2).trim();
            File newDir= null;
            if ( direction.equals("..") ) {
                String cpath= ht.currentDir.getAbsolutePath();
                if ( cpath.equals("/") ) {
                    return true;
                }
                int pos= cpath.lastIndexOf('/');
                newDir= new File(cpath.substring(0, pos));
            }
            else
            if ( direction.charAt(0) == '/' ) {
                newDir= new File(direction);
            }
            else {
                String cpath = ht.currentDir.getAbsolutePath();
                newDir = new File(cpath + '/' + direction);
            }
            
            if ( newDir != null && newDir.isDirectory()) {
                ht.currentDir = newDir;
                System.out.println("PATH: " + ht.currentDir.getAbsolutePath());
            }
            return true;
        }
        
        
	return false;
    }
}



