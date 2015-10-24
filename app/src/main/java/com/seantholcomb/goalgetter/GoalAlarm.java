package com.seantholcomb.goalgetter;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.ArrayList;

/**
 * Created by seanholcomb on 10/24/15.
 * This Class is use to update the database and generate notifications
 * It should run daily at midnight.
 */
public class GoalAlarm extends IntentService implements CursorLoader.OnLoadCompleteListener<Cursor> {

    String sortOrder = GoalContract.GoalEntry.COLUMN_ID + " ASC";

    private static final String sDelete =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_ID + " = ? ";

    private static final String[] Goal_COLUMNS = {

            GoalContract.GoalEntry.TABLE_NAME + "." + GoalContract.GoalEntry._ID,
            GoalContract.GoalEntry.COLUMN_ID,
            GoalContract.GoalEntry.COLUMN_TYPE,
            GoalContract.GoalEntry.COLUMN_NAME,
            GoalContract.GoalEntry.COLUMN_START_DATE,
            GoalContract.GoalEntry.COLUMN_DUE_DATE,
            GoalContract.GoalEntry.COLUMN_TASK,
            GoalContract.GoalEntry.COLUMN_FREQUENCY,
            GoalContract.GoalEntry.COLUMN_TOTAL_TASKS,
            GoalContract.GoalEntry.COLUMN_TASKS_DONE,
            GoalContract.GoalEntry.COLUMN_TASKS_MISSED,
            GoalContract.GoalEntry.COLUMN_TASKS_REMAINING,
            GoalContract.GoalEntry.COLUMN_STATUS
    };

    static final int COL_ID = 0;
    static final int COL_GOAL_ID = 1;
    static final int COL_TYPE = 2;
    static final int COL_NAME = 3;
    static final int COL_START_DATE = 4;
    static final int COL_DUE_DATE = 5;
    static final int COL_TASK = 6;
    static final int COL_FREQUENCY = 7;
    static final int COL_TOTAL_TASKS = 8;
    static final int COL_DONE_TASK = 9;
    static final int COL_MISSED_TASKS = 10;
    static final int COL_REMAINING_TASKS = 11;
    static final int COL_STATUS = 12;

    private CursorLoader mCursorLoader;
    private ArrayList<ContentValues> mCVArrayList;

    public GoalAlarm() {
        super("GoalAlarm");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mCVArrayList= new ArrayList<>();
        mCursorLoader = new CursorLoader(this, GoalContract.GoalEntry.GOAL_URI, Goal_COLUMNS, null, null, sortOrder);
        mCursorLoader.registerListener(0, this);
        mCursorLoader.startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        // Bind data to UI, etc
        makeValue(data);
        deleteStrayMilestones();
        sendNotification(this, data);
        updateTasks(data);

    }

    private void sendNotification(Context context, Cursor cursor) {
        for cursor.getCount()
        Intent notificationIntent = new Intent(context, Splash.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.icon, "Test1", System.currentTimeMillis());
        notification.setLatestEventInfo(context, "Test2", "Test3", contentIntent);
        notificationMgr.notify(0, notification);
    }

    private void updateTasks(){
        String id;
        for (int i = 0; i< mCVArrayList.size();i++ ){
            if (i==0) {
                id = cursor.getString(COL_GOAL_ID);
            }
        }
    }

    //This method will delete milestones that are not associated with a Goal
    private void deleteStrayMilestones(){
        ArrayList<String> IdArrayList= new ArrayList<>();
        for (int i = 0; i < mCVArrayList.size(); i++){
            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_TYPE).equals(GoalContract.GoalEntry.GOAL)){
                IdArrayList.add(mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_ID));
            }
        }

        for (int i = 0; i < mCVArrayList.size(); i++){
            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_TYPE).equals(GoalContract.GoalEntry.MILESTONE)) {
                boolean isStray = true;
                for (int j = 0; j < IdArrayList.size(); j++){
                    if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_ID).equals(IdArrayList.get(j))){
                        isStray=false;
                    }
                }
                if (isStray){
                    String[] selectionArgs = new String[] {mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_ID)};
                    this.getContentResolver().delete(GoalContract.GoalEntry.GOAL_URI, sDelete, selectionArgs);
                    mCVArrayList.remove();
                }
            }

        }
    }

    public void makeValue(Cursor cursor) {
        mCVArrayList.clear();
        ContentValues contentValues = new ContentValues();
        if(cursor != null)
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                contentValues.put(GoalContract.GoalEntry.COLUMN_ID, cursor.getString(COL_GOAL_ID));
                contentValues.put(GoalContract.GoalEntry.COLUMN_TYPE, cursor.getString(COL_TYPE));
                contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, cursor.getString(COL_NAME));
                contentValues.put(GoalContract.GoalEntry.COLUMN_START_DATE, cursor.getDouble(COL_START_DATE));
                contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, cursor.getDouble(COL_DUE_DATE));
                contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, cursor.getString(COL_TASK));
                contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, cursor.getInt(COL_FREQUENCY));
                contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, cursor.getInt(COL_TOTAL_TASKS));
                contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, cursor.getInt(COL_DONE_TASK));
                contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, cursor.getInt(COL_MISSED_TASKS));
                contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, cursor.getInt(COL_REMAINING_TASKS));
                contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, cursor.getString(COL_STATUS));
                mCVArrayList.add(contentValues);
            }
    }



    @Override
    public void onDestroy() {

        // Stop the cursor loader
        if (mCursorLoader != null) {
            mCursorLoader.unregisterListener(this);
            mCursorLoader.cancelLoad();
            mCursorLoader.stopLoading();
        }
    }
}


