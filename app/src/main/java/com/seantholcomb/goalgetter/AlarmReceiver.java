package com.seantholcomb.goalgetter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by seanholcomb on 10/25/15.
 */
public class AlarmReceiver  extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("JJJ", "Recieved");
            context.startService(new Intent(context, GoalAlarm.class));
        }

}
