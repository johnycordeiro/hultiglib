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

import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * <p>A time sensor for counting execution times</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: UBI - HULTIG</p>
 *
 * @author João Paulo Cordeiro
 * @version 1.0
 */
public class CronoSensor
{
    /**
     * The zero time - time when object is created or when it is set.
     */
    long t0;

    /**
     * Define the type "milliseconds".
     */
    public static  byte MSECONDS= 1;

    /**
     *	Define the type "seconds"
     */
    public static byte  SECONDS= 2;

    /**
     * Define the type "minutes"
     */
    public static byte  MINUTES= 3;

    /**
     * Define the type "hour"
     */
    public static byte    HOURS= 4;

    /**
     * Define the type "day"
     */
    public static byte     DAYS= 5;


    /**
     * Create current object and set time zero - start time
     */
    public CronoSensor()
    {
        this.set();
    }


    /**
     * Reset the chronometer to time zero for subsequente time measuring.
     */
    public void set()
    {
        t0= new GregorianCalendar(Locale.UK).getTimeInMillis();
    }


    /**
     * Get the elapsed time since the chronometer was set to time zero.
     */
    public long dt()
    {
        Calendar c= new GregorianCalendar(Locale.UK);
        long t1= c.getTimeInMillis();
        return t1-t0;
    }


    /**
     * Get the elapsed time since the chronometer was set to time zero.
     * @param type One of the types defined previously: SECONDS, MINUTES, etc.
     * @return The elapsed time since the chronometer was set to time zero,
     * according to the correct interpretation or type.
     */
    public double dt(byte type)
    {
        long dt= dt();

        if ( type == SECONDS )  return dt/1000.0;
        if ( type == MINUTES )  return dt/(60*1000.0);
        if (   type == HOURS )  return dt/(3600*1000.0);
        if (    type == DAYS )  return dt/(24*3600*1000.0);

        return (double) dt;
    }


    /**
     *  The time in a string format
     */
    public String dts() {
        long dt= dt();
        int H= (int) dt/3600000;
        dt= dt - 3600000*H;
        int M= (int) dt/60000;
        dt= dt - 60000*M;
        int S= (int) dt/1000;

        return new String(Toolkit.sprintf("%02d:%02d:%02d", H, M, S));
    }


    /**
     * Return a string with the current date-time stamp,
     * for example: 20100320171545. The time precision
     * is in the order of a second.
     * @return String with current time stamp.
     */
    public static String nowStamp() {
        Calendar now= Calendar.getInstance();
        int Y= now.get(Calendar.YEAR);
        int M= now.get(Calendar.MONTH)+1;
        int D= now.get(Calendar.DAY_OF_MONTH);
        int h= now.get(Calendar.HOUR_OF_DAY);
        int m= now.get(Calendar.MINUTE);
        int s= now.get(Calendar.SECOND);

        String snow= String.format("%4d%02d%02d%02d%02d%02d", Y, M, D, h, m, s);
        return snow;
    }
}
