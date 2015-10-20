package com.seantholcomb.goalgetter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by seanholcomb on 10/9/15.
 */
public class Utility {

    /*Method to calculate the percentages used in the graph item
    * @param total number of tasks, the amount of tasks done, the amount of tasks missed
    * @return int[] {percent tasks done, percent tasks missed, remaining percent}
    */
    public static int[] getPercents(int total, int done, int miss){
        int[] percents = new int[3];
        double value = (double)total/100;
        double dDone = done/value;
        double dMiss = miss/value;
        int iDone = (int) dDone;
        int iMiss = (int) dMiss;

        if (total==miss+done) {
            percents[0] = 100-iMiss;
            percents[1] = iMiss;
            percents[2] = 0;
        }else {
            if (iDone == 0) iDone=1;
            percents[0] = iDone;
            percents[1] = iMiss;
            percents[2] = 100 - iMiss - iDone;
        }
        return percents;
    }

    public static String getDate(long milliSeconds)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static Double getDateDouble(String formattedDate)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = formatter.parse(formattedDate);
            return (double) date.getTime();
        }catch(ParseException p){

        }
        return (double) -1;


    }

}
