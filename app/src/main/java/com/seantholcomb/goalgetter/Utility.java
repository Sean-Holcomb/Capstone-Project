package com.seantholcomb.goalgetter;

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
            percents[0] = iDone;
            percents[1] = iMiss;
            percents[2] = 100 - iMiss - iDone;
        }
        return percents;
    }

}
