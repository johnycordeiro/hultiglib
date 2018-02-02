package hultig.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * <p>
 * The JPrimes class ...
 * </p>
 *
 * <p>
 * <b>University of Beira Interior</b> (UBI)<br />
 * Centre For Human Language Technology and Bioinformatics (HULTIG)
 * </p>
 *
 * @date 18:30:39 16/Mai/2013
 * @author J. P. Cordeiro
 */
public class JPrimes 
{
    private long[] seqPrimes;    
    
    
    public JPrimes() {
        seqPrimes= null;
    }
    
    
    public void computeSequence(int limit) {
        int lastPosition= 1;
        if ( seqPrimes == null ) {
            if ( limit < 10 )  limit= 10;
            seqPrimes= new long[limit];
            seqPrimes[0]= 2L;
            seqPrimes[1]= 3L;
            lastPosition= 1;
        }
        else if ( seqPrimes.length < limit ) {
            long[] v= new long[limit];
            System.arraycopy(seqPrimes, 0, v, 0, seqPrimes.length);
            lastPosition= seqPrimes.length-1;
            seqPrimes= v;
        }
        
        for(int i=lastPosition+1; i<seqPrimes.length; i++) {
            for(long n= seqPrimes[i-1];;) { 
                n+= 2L;
                long rn= (long) Math.sqrt(n)+1L;
                boolean divide= false;
                for (int j = 0; j < i && seqPrimes[j]<rn; j++) {
                    if ( n%seqPrimes[j] == 0 ) {
                        divide= true;
                        break;
                    }
                }
                if ( !divide ) {
                    seqPrimes[i]= n;
                    //System.out.printf("%6d  ---->   %d    %d\n", i, n, n*n);
                    break;
                }
            }
        }
    }
    
    
    public int size() {
        return seqPrimes == null ? 0 : seqPrimes.length;
    }
    
    
    public long get(int i) {
        if ( i<0 || i>size() ) return -1;
        return seqPrimes[i];
    }

    
    public static boolean isPrime(long n) {
        if ( n%2 == 0 ) return false;
        long sqrtn= (long) Math.sqrt(n) +1L;
        for (long k= 3L; k<sqrtn; k++)
            if ( n%k == 0 ) return false;
        return true;
    }  
    
    
    public boolean saveSequence(String fileName) {
        try {
            new ObjectOutputStream(new FileOutputStream(fileName)).writeObject(seqPrimes);
        } catch (Exception ex) {
            return false;
        }
        
        return true;
    }
    
    
    public boolean loadSequence(String fileName) {
        try {
            FileInputStream fis= new FileInputStream(fileName);
            ObjectInputStream ois= new ObjectInputStream(fis);
            seqPrimes= (long[]) ois.readObject();
            
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR: file not found: "+fileName);
            return false;
        } catch (IOException ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }
    
    
    public void print(int a, int b) {
        if ( seqPrimes == null ) return;
        if ( a>b ) {
            int aux= a;
            a= b;
            b= aux;
            if ( b > seqPrimes.length-1 )
                b= seqPrimes.length-1;
        }
        for (int i = a; i <=b; i++) {
            if ( i%5 == 0 ) {
                String s= String.format("prime[%d]:", i+1);
                System.out.printf("\n%20s", s);
            }
            System.out.printf("   %10d", seqPrimes[i]);
        }
        System.out.println("\n");
    }
    
    

    /**
     * @param args
     */
    public static void main(String[] args) {
        args= new String[]{
            "-limit",    "200000",
            "-load",     "/a/primes1.2E6.object",
            //"compute",
            "print"
        };
        OSArguments osa= new OSArguments(args);
        int limit= Integer.parseInt(osa.get("limit"));
        JPrimes primes= new JPrimes();
        if ( osa.contains("load") ) {
            primes.loadSequence(osa.get("load"));
            //primes.print();
        }
        
        String dts="";
        if ( osa.contains("compute") ) {
            CronoSensor dt= new CronoSensor();
            primes.computeSequence(limit);
            dts= dt.dts();
            primes.saveSequence("/a/primes.object");
        }
        
        if ( osa.contains("print") ) {
            primes.print(199000, 200000);
        }
        
        System.out.printf("limit: %d   dt: %s\n\n", limit, dts);
    }
}
