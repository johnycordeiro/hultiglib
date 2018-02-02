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
package hultig.util;


import java.io.*;
import java.util.*;


/**
 * <b>NOT YET WELL COMMENTED</b>.
 *
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: UBI/HULTIG</p>
 *
 * @author JPC
 * @version 1.0
 */
public final class HashStr extends Hashtable<String, Integer> implements Serializable
{
    private long M= 0L;
    protected String ENCODE;


    /**
     * Creates an instance of this HashMap.
     */
    public HashStr() {
	super();
        setEncode("UTF-8");
    }


    public void setEncode(String encode) {
        this.ENCODE= encode;
    }


    public String getEncode() {
        return this.ENCODE;
    }


    public void add(String skey)
    {
	//com acentos
	add(skey, false);
    }


    /**
     * Adiciona uma nova String à tabela de hash, se esta
     * ainda não existir. Se a flag <b>flagTiraAcentos</b>
     * for true então tira todos os acentos da string
     * enviada.
     */
    public void add(String skey, boolean flagTiraAcentos)
    {
	//if ( flagTiraAcentos )  skey= StrX.tiraAcentos(skey);

	if ( ! this.containsKey(skey) )  {
	    this.put(skey, new Integer(1));
	    M++;
	}
    }


    /**
     * Realiza o método <b>add</b> para
     * um vector de strings.
     */
    public void add(String[] vs)
    {
	if ( vs == null )  return;

	for (int i=0; i<vs.length; i++)  add(vs[i]);
    }


    /**
     * Adiciona ou incrementa o valor inteiro correspondente
     * à String enviada <b>skey</b>, de <b>kink</b> unidades.
     */
    public void increment(String skey, int kinc)
    {
	if ( ! this.containsKey(skey) )
	    this.put(new String(skey), new Integer(kinc));
	else  {
	    Integer I= (Integer)this.get(skey);
	    int ik= I.intValue() + kinc;
	    this.put(skey,new Integer(ik));
	}

	M+= kinc;
    }


    /**
     * Realiza o método <b>add</b> para
     * um vector de strings.
     */
    public void increment(String[] vs, int kinc)
    {
	if ( vs == null )  return;

	for (int i=0; i<vs.length; i++)  increment(vs[i], kinc);
    }


    /**
     * Numero de strings incrementadas, na
     * estrutura, por exemplo: o numero
     * total de palavras.
     */
    public long getTotal()
    {
	return M;
    }


    /**
     * Frequência da string «key», na
     * estrutura.
     */
    public int freq(String key)
    {
	if ( key == null ) {
            return -1;
        }

	Integer I= (Integer)this.get(key);
	if ( I == null )  {
	    return 0;
	}
	else {
            return I.intValue();
        }
    }


    /**
     * Probabilidade estimada da string "key",
     * na estrutura.
     */
    public double prob(String key)
    {
	double p= (double) freq(key) / getTotal();
	return p;
    }


    /**
     * Devolve um array de Strings contendo, as
     * chaves da tabela de hash.
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


    public String[] rankBestN(int N) {
        return rankN(this, N);
    }

    /**
     * Devolve um array com as N melhores Strings.
     */
    public static String[] rankN(Hashtable<String, Integer> ht, int N)
    {
	if ( ht == null || ht.size() < 1 )  return null;

	Hashtable<String, Integer> h= (Hashtable<String, Integer>) ht.clone();
	String[] vs= new String[Math.min(ht.size(),N)];

	for (int k=0; k<vs.length; k++)  {
	    Enumeration e= h.keys();
	    String wmx=(String) e.nextElement();
	    int max= ht.get(wmx).intValue();

	    while ( e.hasMoreElements() )  {
		String w=(String) e.nextElement();
                int emax= ht.get(w).intValue();
		if ( emax > max )  {
		    max= emax;
		    wmx= w;
		}
	    }

	    vs[k]= new String(wmx);
	    h.remove(wmx);
	    if ( h.size() < 1 )  break;
	}

	return vs;
    }


    /**
     * Devolve um array com as N melhores Strings.
     * @depercated
     */
    public static String[] rankNDouble(Hashtable<String, Integer> ht, int N)
    {
	if ( ht == null || ht.size() < 1 )  return null;

	Hashtable<String, Integer> h= (Hashtable<String, Integer>) ht.clone();
	String[] vs= new String[Math.min(ht.size(),N)];

	for (int k=0; k<vs.length; k++)  {
	    Enumeration e= h.keys();
	    String wmx=(String) e.nextElement();
	    double max= ht.get(wmx).doubleValue();

	    while ( e.hasMoreElements() )  {
		String w=(String) e.nextElement();
                double emax= ht.get(w).doubleValue();
		if ( emax > max )  {
		    max= emax;
		    wmx= w;
		}
	    }

	    vs[k]= new String(wmx);
	    h.remove(wmx);
	    if ( h.size() < 1 )  break;
	}

	return vs;
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
	SortedSet sset= tree.tailSet((String) tree.first());
	Iterator iter= sset.iterator();
        
	for (int i=0; iter.hasNext(); i++)  {
	    String skey=(String) iter.next();
	    if ( sa != null  &&  skey.compareTo(sa) < 0 )  continue;
	    if ( sb != null  &&  skey.compareTo(sb) > 0 )  break;

	    Integer I=(Integer) get(skey);
	    System.out.printf("%4d   %10d   [%s]\n", i, I.intValue(), skey);
	}
    }


    /**
     * Similar to the method with two arguments. A more general version of it,
     * by considering all tokens, instead of only verbs.
     * @param filename
     * @return
     */
    public boolean processFile(String filename) {
        return processFile(filename, false, false);
    }


    /**
     * Read the text from a file and incrementally setup the hashtable.
     * @param filename
     * @param only_words
     * @return
     */
    public boolean processFile(String filename, boolean only_words, boolean case_sensitive)
    {
	try  {
            InputStream in = new FileInputStream(filename);
	    BufferedReader br= new BufferedReader(new InputStreamReader(in,this.ENCODE));

	    for(;;)  {
		String line= br.readLine();
		if ( line == null )  break;
                
                if ( !case_sensitive )  line= line.toLowerCase();
                String[] words= line.split(" ");
                for (String w : words) {
                    int N= w.length();
                    if ( N < 1 ) continue;
                    char c0= w.charAt(0);
                    char cN= w.charAt(N-1);
                    if ( !only_words || Character.isLetter(c0) && Character.isLetter(cN) )
                        increment(w, 1);
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


    /**
     * Carrega a Hashtable, a partir de uma outra
     * tabela de Hash.
     */
    public void load(Hashtable ht)
    {
	this.clear();

	Enumeration keys= ht.keys();
	while ( keys.hasMoreElements() )  {
	    String skey= (String)keys.nextElement();
	    Integer I= (Integer)ht.get(skey);
	    this.increment(skey,I.intValue());
	}
    }


    /**
     * Carrega a Hashtable, a partir do ficheiro
     * "fname".
     */
    public boolean load(String fname)
    {
	try  {
	    FileInputStream istream= new FileInputStream(fname);
	    ObjectInputStream ois= new ObjectInputStream(istream);

	    HashStr hstr=(HashStr) ois.readObject();
	    this.load(hstr);
	    ois.close();
	}
	catch (Exception e)  {
	    System.out.println("ERROR - "+e);
	    return false;
	}

	return true;
    }


    /**
     * Grava a Hashtable num ficheiro com o nome "fname".
     */
    public boolean save(String fname)
    {
	try  {
	    FileOutputStream ostream= new FileOutputStream(fname);
	    ObjectOutputStream oos= new ObjectOutputStream(ostream);

	    oos.writeObject(this);
	    oos.flush();
	    oos.close();
	}
	catch (Exception e)  {
	    System.out.println("ERROR - "+e);
	    return false;
	}

	return true;
    }


    /**
     *  M A I N
     *  -------
     */
    public static void main(String[] args)
    {
	HashStr hasht= new HashStr();
        hasht.setEncode("ISO-8859-1");
	String command;

	while ( true )  {
	  System.out.print("HashStr> ");
	  command= Toolkit.readLn().trim();
	  if ( command.startsWith("exit") ) {
                break;
            }

	  parse(command,hasht);
	}
    }


    private static boolean parse(String command, HashStr ht)
    {
	if ( command.equals("help")  ||  command.equals("?") )  {
	    System.out.println("\tprint - imprime a tabela de hash");
	    System.out.println("\tread <filename> - redefinir a hash. com base no ficheiro");
	    System.out.println("\tload <filename> - carregar a hasht. com base no ficheiro");
	    System.out.println("\tsave <filename> - gravar a hasht. no ficheiro");
	    System.out.println("\texit - sair");
	    return true;
	}

	if ( command.startsWith("print") )  {
	    ht.print();
	    return true;
	}

	if ( command.startsWith("read") )  {
	    String filename= command.substring(4).trim();
	    System.out.println("\nLOADING FILE - ["+filename+"]");
	    System.out.println("-----------------------------");
	    boolean bool= ht.processFile(filename, true, false);
	    System.out.println("\n-----------");
	    System.out.printf("FILE LOADED  (#:%d)\n", ht.size());
	    return bool;
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
            System.out.printf("$(%s) = %d\n", w, ht.freq(w));
            return true;
        }
        
        if ( command.startsWith("best") ) {
            String sn= command.substring(4).trim();
            int n= Integer.parseInt(sn);
            String[] vbest= ht.rankBestN(n);
            for (String w : vbest) {
                int fw= ht.freq(w);
                System.out.printf("%25s  --->  %d\n", w, fw);
            }
            return true;
        }

	return false;
    }
}



