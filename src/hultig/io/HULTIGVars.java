package hultig.io;

import java.io.File;


/**
 * Defines a number of relevant variables, as paths for
 * accessing data.
 * 
 * @author Cordeiro
 * @since 30/01/2014
 */
public class HULTIGVars {
    public static final String PATH_BASE=   "/a/opennlp-tools-1.5.0";
    //public static final String PATH_BASE=   "W:";
    public static final String PATH_A=      "/a";
    public static final String PATH_LIB=    "/lib/java";
    public static final String PATH_OPENLP= "/a/opennlp-tools-1.5.0";

    /**
     * Serves just for testing folder path configuration.
     * @param args 
     */
    public static void main(String[] args) {
        File a= new File(PATH_A);
        if ( a.exists() ) {
            String[] dirA= a.list();
            for (String f : dirA) {
                if ( f.charAt(0) != '.' )
                    System.out.println(f);
            }
        }
        else
            System.err.println("(hultig.io.HULTIGVars): Error in path definition!");
    }
}
