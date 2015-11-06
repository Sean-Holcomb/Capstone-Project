package com.seantholcomb.goalgetter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by seanholcomb on 10/25/15.
 */
public class AlarmReceiver  extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, GoalAlarm.class));
        }

}
