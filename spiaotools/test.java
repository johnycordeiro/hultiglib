/**
 *  Template demonstrando utilização de argumentos.
 */

import spiaotools.SentParDetector;
import hultig.sumo.Text;


public class test
{
    public static boolean x200703091807(String s)
    {
        System.out.print("<Original Text>\n");
        System.out.printf("   %s\n", s);
        System.out.print("</Original Text>\n");

        Text txt= new Text();
	txt.add(s);
        System.out.print("<sentences>\n");
	for (int i=0; i<txt.size(); i++)
	    System.out.printf("   stc(%d) ---> [%s]\n", i, txt.getSentence(i));
        System.out.print("</sentences>\n");

        System.out.print("\n\n");
	return true;
    }


    public static void main(String[] args)
    {
        /**
         * Este exemplo é díficil, mesmo para o SentParDetector. A expressão 
         * "In Washington, U.S. Defense" acaba por ser dividida em "Defense".
         */
	String sa= "Mr. Petraeus noted some positive developments, saying several insurgent cells have been destroyed and their leaders captured. He said the number of sectarian killings in Baghdad has also gone down. But added that it is \"critical\" for Iraq's political leaders to halt any drift toward sectarian conflict. In Washington, U.S. Defense Secretary Robert Gates Wednesday approved General Petraeus' request for 2,200 more military police to help deal with an expected rise in detainees during the security crackdown. Mr. John are in addition to the 21,000 combat troops and 2,400 support troops being sent to Iraq as part of President Bush's Baghdad security plan.";

        /**
         * Este exemplo rebentava com o SentParDetector original e reportei para o 
         * Scott Piao em Março de 2007
         */
	String sb="Next comes the \"short-list,\" the judges' top six, which was announced with great fanfare in late September.  This year the works included Brian Moore's political thriller \"The Color of Blood\" (E.P.  Dutton, 182 pages, $16.95); Peter Ackroyd's \"Chatterton\" (Grove Press, 240 pages, $17.95);";

	x200703091807(sa);
	x200703091807(sb);

        /*
        experiment(sa);
        experiment(sb);
        */
    }



    public static void experiment(String s) {
	SentParDetector spd= new SentParDetector();
	spd.markTitle(false);
	String st= spd.markupRawText(4, s);
	System.out.println("\n<str>\n"+st+"</str>\n");
    }
}
