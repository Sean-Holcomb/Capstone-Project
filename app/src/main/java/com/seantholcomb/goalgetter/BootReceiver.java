package com.seantholcomb.goalgetter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by seanholcomb on 10/24/15.
 */
public class BootReceiver extends BroadcastReceiver{
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Set Alarm for database incrementation and notifications
            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent pendIntent = new Intent(context, GoalAlarm.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, pendIntent, 0);

            // Set the alarm to start at midnight
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 0);

            // set the alarm to repeat daily but not to go off until the device is woken up
            alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public class NotifyAlarm extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            GoalAlarm goalAlarm = new GoalAlarm();
            goalAlarm.onHandleIntent(intent);
        }
    }
}