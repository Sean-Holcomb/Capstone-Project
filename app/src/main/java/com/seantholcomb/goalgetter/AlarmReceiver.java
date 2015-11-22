package com.seantholcomb.goalgetter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by seanholcomb on 10/25/15.
 * Broadcast Receiver to catch Intents from alarm manager and start the GoalAlarm intent service.
 */
public class AlarmReceiver  extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, GoalAlarm.class));
        }

}
