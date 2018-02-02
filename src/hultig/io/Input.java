package hultig.io;


import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;



/**
 * <b>NOT YET WELL COMMENTED</b>.
 * <b>DESCRIPTION: Tratamento das leituras de dados a partir do "Standard Input"</b>
 * @date 13:45:57 30/Set/2008
 * @author J. P. Cordeiro
 */
public class Input 
{
    private BufferedReader BR;
    private boolean LAST_READ_OK;
    
    
    public Input() {
        LAST_READ_OK= true;
        try {
            BR= new BufferedReader(new InputStreamReader(System.in));
        }
        catch(Exception exc) {
            BR= null;
        }
    }
    
    
    /**
     * Está bem definido, i.e, pronto para ler
     * dados do standard input.
     * @return true ou false
     */
    public boolean isDefined() {
        return BR != null;
    }
    
    
    public boolean lastReadOK() {
        return LAST_READ_OK;
    }
    
    
    /**
     * Leitura de uma String. Caso exista erro na leitura,
     * é possível saber à posteriori, através do método:
     * <pre>lastReadOK()</pre>
     * 
     * @return A String lida ou então null.
     */
    public String readLn() {
        LAST_READ_OK= true;
        try { return BR.readLine(); }
        catch (Exception exc) {
            LAST_READ_OK= false;
            return null;
        }
    }

    
    /**
     * Leitura de um inteiro. Caso exista erro na leitura,
     * é possível saber à posteriori, através do método:
     * <pre>lastReadOK()</pre>
     * 
     * @return O inteiro lido (zero em caso de erro).
     */    
    public int readInt() {
        LAST_READ_OK= true;
        try {
            return Integer.parseInt(readLn());
        }
        catch(Exception exc) {
            LAST_READ_OK= false;
            return 0;
        }
    }
    
    
    /**
     * Leitura de um número real. Caso exista erro na leitura,
     * é possível saber à posteriori, através do método:
     * <pre>lastReadOK()</pre>
     * 
     * @return O inteiro lido (0.0 em caso de erro).
     */    
    public Double readReal() {
        LAST_READ_OK= true;
        try {
            return Double.parseDouble(readLn());
        }
        catch(Exception exc) {
            LAST_READ_OK= false;
            return 0.0;
        }
    }


    /**
     * MAIN - For testing.
     * @param args
     */
    public static void main(String[] args) {
        PrintStream o= System.out;
        Input in= new Input();
        
        o.print("\n[INPUT TESTER]\n\n");
        for (;;) {
            o.print("input string> ");
            String line= in.readLn();
            if ( line == null || line.length() < 1 ) break;
            
            o.printf("input: [%s]   READ OK: %s\n\n", line, in.lastReadOK());
            
            o.print("input integer> ");
            o.printf("input: [%d]   READ OK: %s\n\n", in.readInt(), in.lastReadOK());
            if ( !in.lastReadOK() ) break;
            
            o.print("input real> ");
            o.printf("input: [%f]   READ OK: %s\n\n", in.readReal(), in.lastReadOK());
            if ( !in.lastReadOK() ) break;
        }
    }
}
