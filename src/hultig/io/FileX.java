package hultig.io;

import java.io.File;


/**
 * <b>NOT YET WELL COMMENTED</b>.
 * @date 23:54:11 18/Dez/2008
 * @author J. P. Cordeiro
 */
public class FileX extends File
{
    /**
     * File base name - example: for file "fxyz.dat", "fxyz" would be
     * its base name, and ".dat" its extension token.
     */
    protected String base;
    protected String ext;


    public FileX(String fname) {
        super(fname);
        parseBaseExt(fname);
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

        int p= fname.lastIndexOf(".");
        int q= fname.lastIndexOf("/");

        if ( p < 0 )
            setBaseExt(fname.substring(q+1), "");
        else
            setBaseExt(fname.substring(q+1,p), fname.substring(p));
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
     * MAIN - For testing.
     * @param args
     */
    public static void main(String[] args) {
        FileX fx= new FileX("listbub.dat");
        System.out.printf("FILE:.... %s\n", fx.getAbsoluteFile());
        System.out.printf("FBASE:... %15s   EXT: %s\n", fx.getBase(), fx.getExt());

        fx= new FileX(fx.getBase()+"2"+fx.getExt());
        System.out.printf("FILE:.... %s\n", fx.getAbsoluteFile());
        System.out.printf("FBASE:... %15s   EXT: %s\n", fx.getBase(), fx.getExt());

        fx= new FileX("/Users/john/descent.exe");
        System.out.printf("FILE:.... %s\n", fx.getAbsoluteFile());
        System.out.printf("FBASE:... %15s   EXT: %s\n", fx.getBase(), fx.getExt());
        System.out.printf("FNAME:... %15s   PATH: %s\n", fx.getName(), fx.getPath());
    }
}
