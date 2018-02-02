package hultig.io;

import java.io.*;

import java.util.Formatter;
import java.util.Locale;


/**
 * <b>NOT YET WELL COMMENTED</b>.
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: UBI/HULTIG</p>
 *
 * @author JPC
 * @version 1.0
 */
public class FileOUT extends File
{
    private PrintWriter writer;
    protected String encode;

    /**
     * File base name - example: for file "fxyz.dat", "fxyz" would be
     * its base name, and ".dat" its extension token.
     */
    protected String base;
    protected String ext;


    public FileOUT(String fname) {
        super(fname);
        writer= null;
        parseBaseExt(fname);
    }

    /**
     * The default encoding is: UTF-8
     * @param string String
     */
    public FileOUT(String fname, String encode) {
        super(fname);
        parseBaseExt(fname);
        open(encode);
    }


    private boolean setBaseExt(String base, String ext) {
        if ( base == null || base.length() < 1 ) return false;
        this.base= base;

        if ( ext == null || ext.length() < 1 )
            this.ext= "";
        else if ( ext.charAt(0) == '.' )
            this.ext= ext;
        else
            this.ext= '.' + ext;

        return true;
    }


    private void parseBaseExt(String fname) {
        if ( fname == null ) {
            System.err.println("ERROR: parsing fname in method parseBaseExt(.)\n");
            return;
        }

        String[] vfn= fname.split("\\.");
        if ( vfn.length < 2 )
            setBaseExt(vfn[0], "");
        else
            setBaseExt(vfn[0], vfn[1]);
    }


    public String getBase() {
        return this.base;
    }


    public String getExt() {
        return this.ext;
    }


    public String getBaseExt() {
        return base+ext;
    }


    /**
     * Close this file.
     */
    public void close() {
        if ( writer != null ) {
            writer.flush();
            writer.close();
        }
    }


    /**
     * Open the defined output file, with UTF-8 encoding.
     * @return
     */
    public boolean open() {
        return open(null);
    }


    public void flush() {
        if ( writer != null )  writer.flush();
    }


    /**
     * Redefine the output character encoding
     * @param encoding
     * @return
     */
    public boolean open(String encoding) {
        close();
        if ( encoding != null )
            this.encode= encoding;
        else
            this.encode= "UTF-8";
        try {
            FileOutputStream fos= new FileOutputStream(this.getAbsolutePath());
            OutputStreamWriter osw= new OutputStreamWriter(fos, this.encode);
            writer= new PrintWriter(osw);
        }
        catch(Exception exc) {
            System.err.println(exc);
            writer= null;
            return false;
        }

        return true;
    }
    

    /**
     * Return the current encoding;
     * @return String
     */
    public String getEncoding() {
        return this.encode;
    }


    /**
     * A shortcut for standard print.
     * @param s String
     */
    public void print(String s) {
        if ( writer == null ) return;
        writer.print(s);
        writer.flush();
    }


    /**
     *  shortcut for standard println.
     * @param s String
     */
    public void println(String s) {
        if ( writer == null ) return;
        writer.println(s);
        writer.flush();
    }


    /**
     * My <b>sprintf</b> function, similar to the C function.
     * @param format String
     * @param args Object[]
     * @return String
     */
    public String sprintf(String format, Object ... args) {
        return new Formatter().format(Locale.US, format, args).toString();
    }


    /**
     * My <b>printf</b> function, similar to the C function.
     * @param format String
     * @param args Object[]
     */
    public void printf(String format, Object ... args) {
        if ( writer == null ) return;
        writer.print(new Formatter().format(Locale.US, format, args).toString());
        writer.flush();
    }


    /**
     * My <b>printf</b> function, similar to the C function.
     * @param format String
     * @param args Object[]
     */
    public void printfPT(String format, Object ... args) {
        if ( writer == null ) return;
        writer.print(new Formatter().format(format, args).toString());
        writer.flush();
    }


    /**
     * M A I N - For testing ...
     * @param args
     */
    public static void main(String[] args) {
        String osname= System.getProperty("os.name");
        String osarch= System.getProperty("os.arch");
        System.out.printf("[OS NAME]: %s   [ARCH]: %s\n\n", osname, osarch);

        FileOUT fo= null;
        if ( osname.contains("Windows") )
            fo= new FileOUT("C:/tmp/FileOUT-test.txt", "ISO-8859-1");
        else
            fo= new FileOUT("/tmp/FileOUT-test.txt", "ISO-8859-1");

        fo.printf("João Caçador %d\n", 3);
        fo.printf("John Rambsy %d\n", 4);
        fo.close();
    }
}
