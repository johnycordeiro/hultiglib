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

import java.util.Hashtable;
import java.util.Vector;



/**
 * <b>NOT YET WELL COMMENTED</b>.
 * Command Line arguments handler
 *
 * <p>Description: </p>
 *
 * <p>Copyright: JPC Copyright (c) Sep 2006</p>
 *
 * <p>Company: UBI/HULTIG/SUMO</p>
 *
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public class OSArguments
{
    private String sinput;
    private Hashtable<String,String> hargs;

    private Arg[] varg;

    /**
     * Representação interna de um argumento
     */
    private class Arg
    {
        String lab;
        String str;

        public Arg() { lab= null; str= null; }
        public Arg(String s) { this.lab= null; str= s; }
        public Arg(String lab, String s) { this.lab= lab; str= s; }
    }


    /**
     * Constructor
     * @param args String[]
     */
    public OSArguments(String[] args)
    {
        Vector<Arg> v= new Vector<Arg>();
        sinput= "";
        for (int k=0; k<args.length; k++) {
            if ( args[k].charAt(0) == '-' ) {
                String label= args[k].substring(1);
                String value= args[++k];
                v.add(new Arg(label, value));
                sinput+= '-' + label + ' ' + value + ' ';
            }
            else {
                v.add(new Arg(args[k]));
                sinput+= args[k] + ' ';
            }
        }

        varg= new Arg[v.size()];
        for (int i=0; i<varg.length; i++)  varg[i]= v.get(i);
    }


    /**
     * Test if some argument exists, labeled or not
     * @param label String
     * @return boolean
     */
    public boolean contains(String label)
    {
        for (int i=0; i<varg.length; i++)
            if ( varg[i].lab == null ) {
                if (varg[i].str != null  &&  varg[i].str.equals(label) ) return true;
            }
            else
                if ( varg[i].lab.equals(label) ) return true;

        return false;
    }


    public int index(String label)
    {
        for (int i=0; i<varg.length; i++)
            if ( varg[i].lab != null  &&  varg[i].lab.equals(label) )  return i;

        return -1;
    }


    public String get(String param)
    {
        if ( param == null || param.length()<1 ) return null;
        
        int i= -1;
        if ( param.charAt(0) == '-' )
            i= this.index(param.substring(1));
        else
            i= this.index(param);

        return i < 0 ?  null  :  varg[i].str;
    }

    /**
     * Tries to read an integer parameter.
     * @param param The parameter name.
     * @return The read number or {@code null}.
     */
    public Integer getInteger(String param) {
        String sNumber= get(param);
        if ( sNumber == null ) return null;

        try {
            Integer number = new Integer(sNumber);
            return number;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tries to read am integer parameter, ensuring a default value.
     * @param param The name of the parameter to be read
     * @param defaultValue The default value assumed.
     * @return The parameter or the default value.
     */
    public Integer getInteger(String param, int defaultValue) {
        Integer p= getInteger(param);
        if ( p == null )
            return defaultValue;
        else
            return p;
    }




    public String get(int i)
    {
        if ( i<0 || i>varg.length )  return null;
        return varg[i].str;
    }


    public String getLabel(int i)
    {
        if ( i<0 || i>varg.length )  return null;
        return varg[i].lab;
    }


    /**
     * Test if a set of arguments were defined.
     * @param keys String[]
     * @return boolean: true if yes.
     */
    public boolean allDefined(String[] keys)
    {
        for (int i=0; i<keys.length; i++)
            if ( !contains(keys[i]) )  return false;

        return true;
    }


    /**
     * Intrepreta o i-ésimo argumento como int.
     * @param i int
     * @param a int
     * @return int
     */
    public int interpret(int i, int a)
    {
        try { return Integer.parseInt(varg[i].str); }  catch(Exception x) { return a; }
    }


    /**
     * Intreprea o i-ésimo argumento como long.
     * @param i int
     * @param a long
     * @return long
     */
    public long interpret(int i, long a)
    {
        try { return Long.parseLong(varg[i].str); }  catch(Exception x) { return a; }
    }


    /**
     * Intreprea o i-ésimo argumento como double.
     * @param i int
     * @param a double
     * @return double
     */
    public double interpret(int i, double a)
    {
         try { return Double.parseDouble(varg[i].str); }  catch(Exception x) { return a; }
    }


    /**
     * Total number of arguments.
     * @return int
     */
    public int size()
    {
        return varg.length;
    }


    /**
     * Outputs both argument types: positionals and labeled.
     */
    public void printArgs()
    {
        System.out.println("\nARGUMENTS LIST:\n");
        System.out.println("\t      i      label      value");
        System.out.println("\t----------------------------------");
        for (int i=0; i<this.size(); i++)
            System.out.printf("\t arg(%2d)   %7s   %7s\n", i, "["+getLabel(i)+"]", "["+get(i)+"]");
        System.out.println("\n");
    }

    /**
     * Overloading.
     * @return String
     */
    @Override
    public String toString() {
        return sinput;
    }

    

    /**
     * The main method is used for testing.
     * @param args String[]
     */
    public static void main(String[] args) {
        args= new String[] {
            "-inp", "file.txt",
            "-dt", "1127",
            "-dx", "3.45"
        };
        OSArguments osa= new OSArguments(args);
        osa.printArgs();

        System.out.println("\n\ntoString() method:..... ["+osa+"]\n");

        Integer dt= osa.getInteger("-dt", 0);
        System.out.printf("dt: %s\n", dt);
        dt= null;
        dt=5;
        dt++;
        System.out.println(dt);
    }
}
