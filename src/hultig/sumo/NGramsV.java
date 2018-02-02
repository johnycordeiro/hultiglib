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

import java.util.*;
import hultig.util.HashStr;



class Node {
    int i;
    int k;
    int df;
}


/**
 * <b>NOT YET WELL COMMENTED</b>.
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @author João Paulo Cordeiro (Feb, 2006)
 * @version 1.0
 */
public class NGramsV
{
    public HashStr hstng; //---------> hashtable with all n-grams
    public int[] corpus= null; //----> the corpus
    public int N; //-----------------> corpus size
    public int[] suf; //-------------> suffix array
    public int[] lcp; //-------------> lcp array

    private int MAXN; //-------------> biggest n, for all n-grams

    public Stack<Node> stack;
    public int[] doclink;
    public int[] docid;
    public int s;


    public NGramsV(int[] v)
    {
        corpus= v.clone();
        N= corpus.length;
        hstng= new HashStr();
        suf= null;
        lcp= null;
        MAXN= 0;

        stack= new Stack<Node>();
    }


    public int num_ngrams(int n)
    {
        return num_ngrams(n, corpus.length);
    }


    public static int num_ngrams(int n, int m)
    {
        if ( n > m ) return 0;
        else return m - n + 1;
    }


    /**
     * Compute suffix array.
     */
    private void suffix_array()
    {
        if ( corpus == null )  return;

        Integer[] vI= new Integer[N];
        for (int i=0; i<N; i++)  vI[i]= new Integer(i);

        OrdVSuffixes osuff= new OrdVSuffixes(corpus);
        Arrays.sort(vI, osuff);

        suf= new int[vI.length];
        for (int i=0; i<vI.length; i++)  {
            suf[i] = vI[i].intValue();
            //System.out.printf("suffix(%d) = %d\n", i, suf[i]);
        }
    }


    private int complcp(int i)
    {
        int a= suf[i];
        int b= suf[i-1];

        int j= 0;
        for (j=0; j<Math.min(N-a, N-b); j++)
            if ( corpus[a+j] != corpus[b+j] )  break;

        return j;
    }


    /**
     * Compute the lcp array.
     */
    private void lcp_array()
    {
        if ( suf == null ) return;

        lcp= new int[N+1];
        for(int i=1; i<N; i++)  lcp[i] = complcp(i);

        lcp[0] = lcp[N] = 0;
    }


    private void output_hst(int i, int j, int k)
    {
        int LBL = (lcp[i] > lcp[j+1]) ? lcp[i] : lcp[j+1];
        int SIL = lcp[k];
        int tf, r;
        char ch;

        if( i ==j ) {
            String lgram= "1<"+corpus[suf[i]]+">";
            if ( MAXN < 1 )  MAXN= 1;
            hstng.increment(lgram, 1);

            //System.out.printf("1-gram:... %s\n", lgram);
        }
        else if(LBL < SIL) {
            tf= j-i+1;
            int[] v= subvector(corpus, suf[k], suf[k]+SIL);
            String ngram= v.length + vectstr(v);
            if ( MAXN < v.length )  MAXN= v.length;
            hstng.put(ngram, new Integer(tf));
            //System.out.printf("n-gram:................. %s\n", ngram);
            /*
            System.out.printf("\t\t nontrival <%d, %d>, rep=%d, tf=%d  s=[%s]\n",
                              i, j, k, tf, corpus.substring(suf[k],suf[k]+SIL));
            */
        }
    }


    private void output_hst(int i, int j, int k, int df)
    {
        int LBL = (lcp[i] > lcp[j+1]) ? lcp[i] : lcp[j+1];
        int SIL = lcp[k];
        int tf, r;
        char ch;

        if( i ==j ) {
            System.out.printf("trivial <%d,%d>, tf=1\n", i, j);
            //System.out.printf("1-gram:... %s\n", lgram);
        }
        else if(LBL < SIL) {
            tf= j-i+1;
            System.out.printf("\t\t nontrival <%d, %d>, rep=%d, tf=%d  df=%d\n",
                              i, j, k, tf, df);//, corpus.substring(suf[k],suf[k]+SIL));
        }
    }


    public static int[] subvector(int[] v, int a)
    {
        if ( v == null ) return null;
        if ( a >= v.length ) return null;
        int b= v.length;
        int[] u= new int[b-a];
        for (int i=a; i<b; i++)  u[i-a]= v[i];
        return u;
    }


    public static int[] subvector(int[] v, int a, int b)
    {
        if ( v == null ) return null;
        if ( a>b ) { int x=a; a=b; b=x; }
        if ( a >= v.length ) return null;
        if ( b > v.length ) b= v.length;
        int[] u= new int[b-a];
        for (int i=a; i<b; i++)  u[i-a]= v[i];
        return u;
    }

    public static String vectstr(int[] v)
    {
        String str="<";
        for (int i=0; i<v.length-1; i++)  str+= v[i] + ",";
        str+= v[v.length-1] + ">";
        return str;
    }

    public static int[] strvect(String s)
    {
        if ( s == null ) return null;

        int a=0, b=s.length();
        if ( s.charAt(0) == '<' ) a++;
        if ( s.charAt(b-1) == '>' ) b--;

        StringTokenizer st= new StringTokenizer(s.substring(a,b),", ;");
        //System.out.printf("a:%d  b:%d\n", a , b);
        int[] u= new int[st.countTokens()];
        int k=0;
        while ( st.hasMoreElements() )
            u[k++]= Integer.parseInt(st.nextToken());

        return u;
    }


    private int print_LDIs_hst(int i, int k)
    {
        int j = k;
        output_hst(k,k,0); /* trivial intervals */
        while(lcp[k] <= lcp[j+1] && j+1 < N)  j = print_LDIs_hst(k, j+1);
        output_hst(i,j,k); /* non-trivial intervals */
        return j;
    }


    private int doc(int i)
    {
        return docid[suf[i]];
    }


    private void print_LDIs_hst_with_df(int N, int D)
    {
        int i, j, df;

        doclink= new int[D];
        for (i=0; i<D; i++) doclink[i]= -1;

        push(0,0,1);
        for (j=0; j<N; j++) {
            output_hst(j,j,0,1);
            if ( doclink[doc(j)] != -1 ) dec_df(doc(j));
            doclink[doc(j)]= j;

            df= 1;
            while ( lcp[j+1] < lcp[stack.peek().k] ) {
                df= stack.peek().df + df;
                output_hst(stack.peek().i, j, stack.peek().k, stack.peek().df);
                pop();
            }

            push(stack.peek().k, j+1, df);
        }
    }


    private void push(int i, int k, int df)
    {
        Node node= new Node();
        node.i= i;
        node.k= k;
        node.df= df;
        stack.push(node);
    }


    public void pop()
    {
        stack.pop();
    }


    private void dec_df(int docid)
    {
        int beg=0, end=stack.size(), mid= end/2;
        while ( beg != end ) {
            stack.get(mid);
            if ( doclink[docid] >= stack.get(mid).i ) beg= mid;
            else end= mid;
            mid= (beg+end)/2;
        }
        Node n= stack.get(mid);  n.df--;
        stack.set(mid,n);
    }



    /**
     * Compute all n-grams
     */
    public void compute()
    {
        if ( corpus == null )  return;

        suffix_array();
        lcp_array();
        print_LDIs_hst(0,0);
        //populateList();
    }


    private int[] doc_table(int[] corpus, int[] docnum)
    {
        int[] doc_id = new int[N];
        int id = 0;
        for(int i = 0; i < N; i++){
            doc_id[i] = id;
            // -1001 is the end document token.
            if ( corpus[i] == -1001 ) id++;
        }
        docnum[0] = id;
        return doc_id;
    }


    public void compute_df()
    {
        if ( corpus == null ) return;

        N = corpus.length;
        suffix_array();

        System.out.print("     i = ");
        for(int i = 0; i < N; i++) System.out.printf("%2d ", i);
        System.out.println();

        System.out.printf("cor[i] = ");
        for(int i = 0; i < N; i++) System.out.printf(" %d ", corpus[i]);
        System.out.println();

        System.out.printf("s[i]   = ");
        for(int i = 0; i < N; i++) System.out.printf("%2d ", suf[i]);
        System.out.println();

        lcp_array();
        System.out.printf("lcp[i] = ");
        for(int i = 0; i < N+1; i++) System.out.printf("%2d ", lcp[i]);
        System.out.println();


        int[] D= new int[1];
        docid = doc_table(corpus, D); System.out.printf("doc[i] = ");
        for(int i = 0; i < N; i++) System.out.printf("%2d ", docid[i]);
        System.out.println();

        print_LDIs_hst_with_df(N, D[0]);
    }


    public int maxN()
    {
        return MAXN;
    }


    public void print()
    {
        System.out.printf("\nCORPUS\n\t%s\n\n", vectstr(corpus));
        System.out.print("\nSUFFIX ARRAY  AND  LCP's\n");
        for (int i=0; i<suf.length; i++)
            System.out.printf("\tlcp(%2d) = %3d    suffix(%2d) = %s\n",
                              i, lcp[i], i, vectstr(subvector(corpus,suf[i])) );

        System.out.print("\nN-GRAMS COMPUTED\n");
        hstng.print();
    }



    /**
     * MAIN - Tests
     * @param args String[]
     */
    public static void main(String[] args)
    {
        int[] v= {6,7,8,4,3,-1001,5,7,8,4,9,-1001};
        //int[] v= {0,1,2,3,4,-1,0,1,2,3,4};

        NGramsV ng= new NGramsV(v);
        ng.compute();
        //ng.compute_df();
        ng.print();
    }
}


class NGram
{
    private int[] v;
    private int freq;

    public NGram(int[] u)
    {
        v= u.clone();
        freq= -1;
    }

    public NGram(int[] u, int f)
    {
        v= u.clone();
        freq= f;
    }

    public int n()
    {
        return v.length;
    }

    public int gram(int i)
    {
        return v[i];
    }

    public int compareTo(NGram ngram)
    {
        int m= Math.min(this.n(), ngram.n());
        for (int i=0; i<m; i++) {
            if (this.gram(i) < ngram.gram(i)) return -1;
            if (this.gram(i) > ngram.gram(i)) return  1;
        }
        if ( this.n() < m ) return -1;
        if ( this.n() > m ) return  1;
        return 0;
    }
}


class OrdVSuffixes implements Comparator<Integer>
{
    int[] v;

    public OrdVSuffixes(int[] vcorp)
    {
        v= vcorp;
    }

    public int compare(Integer I, Integer J)
    {
        int i= I.intValue();
        int j= J.intValue();

        while ( i<v.length && j<v.length ) {
            if ( v[i] < v[j] )  return -1;
            if ( v[i] > v[j] )  return  1;
            i++;
            j++;
        }

        if ( i < v.length )  return -1;
        if ( j < v.length )  return  1;

        return 0;
    }
}


