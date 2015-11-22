package com.seantholcomb.goalgetter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Static methods for use in other classes
 * Created by seanholcomb on 10/9/15.
 */
public class Utility {

    /**
    *Method to calculate the percentages used in the graph item
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

        if (total==0){
            percents[0] = 1;
            percents[1] = 0;
            percents[2] = 99;
        }else if (total==miss+done) {
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

    /**
     * make a formatted string from milliseconds
     * @param milliSeconds date in milliseconds
     * @return formatted date string
     */
    public static String getDate(long milliSeconds)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /**
     * turn formatted string into milliseconds
     * @param formattedDate formatted date string
     * @return date in milliseconds
     */
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

    /**
     * Changes alphabetic string to numerals for notification ids
     * @param id string to be turned into a numeral value
     * @return numeral value that can be converted to a long
     */
    public static String makeValidId(String id){
        id = id.toLowerCase();
        String returnString = "";
        int length = id.length();
        for (int i = 0; i < length; i++){
           switch (id.charAt(i)){
               case 'a' :
                   returnString = returnString + "00";
                   break;
               case 'b' :
                   returnString = returnString + "01";
                   break;
               case 'c' :
                   returnString = returnString + "02";
                   break;
               case 'd' :
                   returnString = returnString + "03";
                   break;
               case 'e' :
                   returnString = returnString + "04";
                   break;
               case 'f' :
                   returnString = returnString + "05";
                   break;
               case 'g' :
                   returnString = returnString + "06";
                   break;
               case 'h' :
                   returnString = returnString + "07";
                   break;
               case 'i' :
                   returnString = returnString + "08";
                   break;
               case 'j' :
                   returnString = returnString + "09";
                   break;
               case 'k' :
                   returnString = returnString + "10";
                   break;
               case 'l' :
                   returnString = returnString + "11";
                   break;
               case 'm' :
                   returnString = returnString + "12";
                   break;
               case 'n' :
                   returnString = returnString + "13";
                   break;
               case 'o' :
                   returnString = returnString + "14";
                   break;
               case 'p' :
                   returnString = returnString + "15";
                   break;
               case 'q' :
                   returnString = returnString + "16";
                   break;
               case 'r' :
                   returnString = returnString + "17";
                   break;
               case 's' :
                   returnString = returnString + "18";
                   break;
               case 't' :
                   returnString = returnString + "19";
                   break;
               case 'u' :
                   returnString = returnString + "20";
                   break;
               case 'v' :
                   returnString = returnString + "21";
                   break;
               case 'w' :
                   returnString = returnString + "22";
                   break;
               case 'x' :
                   returnString = returnString + "23";
                   break;
               case 'y' :
                   returnString = returnString + "24";
                   break;
               case 'z' :
                   returnString = returnString + "25";
                   break;
               default:
                   returnString = returnString + "99";
            }
        }
        return returnString;
    }

}
