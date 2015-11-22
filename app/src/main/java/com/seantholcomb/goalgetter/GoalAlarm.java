package com.seantholcomb.goalgetter;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.seantholcomb.goalgetter.data.GoalContract;
import com.seantholcomb.goalgetter.data.GoalProvider;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by seanholcomb on 10/24/15.
 * This Class is used to update the database and generate notifications
 * It is run daily at midnight be an alarm.
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
    private ArrayList<String> mIdArrayList;

    private boolean notificationsEnabled;
    private final String GROUP_TODO = "group_todo";
    private final String GROUP_COMPLETE = "group_complete";

    public GoalAlarm() {
        super("GoalAlarm");
    }

    /**
     * Start loaders to get full database
     * @param intent sent by AlarmReceiver
     */
    @Override
    public void onHandleIntent(Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        notificationsEnabled=settings.getBoolean(getString(R.string.pref_noti_key), true);
        mCVArrayList = new ArrayList<>();
        mIdArrayList = new ArrayList<>();
        mCursorLoader = new CursorLoader(this, GoalContract.GoalEntry.GOAL_URI, Goal_COLUMNS, null, null, sortOrder);
        mCursorLoader.registerListener(0, this);
        mCursorLoader.startLoading();

    }

    /**
     * run methods when db is availible
     * @param loader
     * @param data db
     */
    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        makeValue(data);
        deleteStrayMilestones();
        sendNotification();
        updateTasks();



    }

    /**
     * sends notifications about tasks and completed milestones/goals
     */
    private void sendNotification() {

        NotificationCompat.Builder mBuilder;
        Intent resultIntent = new Intent(this, DashBoardActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DashBoardActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Calendar calendar = Calendar.getInstance();
        boolean showNotification;
        for (int i = 0; i < mCVArrayList.size(); i++) {

            if (notificationsEnabled && mCVArrayList.get(i).getAsLong(GoalContract.GoalEntry.COLUMN_DUE_DATE) <= calendar.getTimeInMillis()
                    && !mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.COMPLETE)) {
                mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.COMPLETE);
                //create unique id for each notification
                String name = mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_NAME);
                String idString = Utility.makeValidId(name);
                if (idString.length()>9){
                    idString = idString.substring(0,8);
                }
                while (idString.charAt(0)=='0'){
                    idString = idString.substring(1);
                }
                int id = Integer.valueOf(idString);
                mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_drawer)
                                .setContentTitle(getString(R.string.congratulations))
                                .setContentText(name + getString(R.string.completed))
                                .setGroup(GROUP_COMPLETE)
                                .setAutoCancel(true)
                                .setContentIntent(resultPendingIntent);

                mNotificationManager.notify(id, mBuilder.build());


            }
            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.ACTIVE)
                    && mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_TYPE).equals(GoalContract.GoalEntry.MILESTONE)) {
                showNotification = checkOnTrack(i);
                if (notificationsEnabled && showNotification) {
                    //create unique id for notifications
                    String task = mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_TASK);
                    String idString = Utility.makeValidId(task);
                    if (idString.length()>9){
                        idString = idString.substring(0,8);
                    }
                    while (idString.charAt(0)=='0'){
                        idString = idString.substring(1);
                    }
                    int id = Integer.valueOf(idString);
                    mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_drawer)
                                    .setContentTitle(getString(R.string.task_due))
                                    .setContentText(task)
                                    .setGroup(GROUP_TODO)
                                    .setAutoCancel(true)
                                    .setContentIntent(resultPendingIntent);
                    mNotificationManager.notify(id, mBuilder.build());
                }
            }
        }


    }

    /**
     * updates goals task fields to be equal to the sum of its milestones respective fields
     */
    private void updateTasks() {
        String id;
        int remaining;
        int done;
        int missed;
        int goalIndex;
        for (int i = 0; i < mIdArrayList.size(); i++) {
            id = mIdArrayList.get(i);
            goalIndex = -1;
            remaining = 0;
            done = 0;
            missed = 0;
            for (int j = 0; j < mCVArrayList.size(); j++) {
                if (mCVArrayList.get(j).getAsString(GoalContract.GoalEntry.COLUMN_ID).equals(id)) {
                    if (mCVArrayList.get(j).getAsString(GoalContract.GoalEntry.COLUMN_TYPE).equals(GoalContract.GoalEntry.GOAL)) {
                        goalIndex = j;
                    } else {
                        remaining += mCVArrayList.get(j).getAsInteger(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING);
                        done += mCVArrayList.get(j).getAsInteger(GoalContract.GoalEntry.COLUMN_TASKS_DONE);
                        missed += mCVArrayList.get(j).getAsInteger(GoalContract.GoalEntry.COLUMN_TASKS_MISSED);
                    }
                }
            }
            if (goalIndex != -1) {
                mCVArrayList.get(goalIndex).put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, remaining);
                mCVArrayList.get(goalIndex).put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, done);
                mCVArrayList.get(goalIndex).put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, missed);

            }
        }

        ContentValues[] CVArray = mCVArrayList.toArray(new ContentValues[mCVArrayList.size()]);
        this.getContentResolver().delete(GoalContract.GoalEntry.GOAL_URI, GoalProvider.DELETE_ALL, null);
        this.getContentResolver().bulkInsert(GoalContract.GoalEntry.GOAL_URI, CVArray);
    }

    /**
     * This method will delete milestones that are not associated with a Goal
     */
    private void deleteStrayMilestones() {

        for (int i = 0; i < mCVArrayList.size(); i++) {
            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_TYPE).equals(GoalContract.GoalEntry.MILESTONE)) {
                boolean isStray = true;
                for (int j = 0; j < mIdArrayList.size(); j++) {
                    if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_ID).equals(mIdArrayList.get(j))) {
                        isStray = false;
                    }
                }
                if (isStray) {

                    mCVArrayList.remove(i);
                }
            }

        }
    }

    /**
     * If a milestone is active. Algorithm checks amount of tasks complete against
     * those it calculates to be remaining.
     * if the remaining amount cant be completed at the rate specified by the frequency
     * then a task is marked as missed
     * @param position
     * @return
     */
    public boolean checkOnTrack(int position) {
        int done = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TASKS_DONE);
        int missed = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TASKS_MISSED);
        int remaining = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING);
        double start = (double) GoalContract.normalizeDate(Calendar.getInstance().getTimeInMillis());

        double end = mCVArrayList.get(position).getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE);
        int freq = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_FREQUENCY);
        if (freq == 0) return false;

        int total = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS);
        double dif = end-start;
        dif = dif / (1000 * 60 * 60 * 24);
        dif = dif / 7 * freq;
        int difDays = (int) dif;
        difDays = total - done - missed - difDays;
        if (difDays > 0) {
            missed += difDays;
            remaining -= difDays;
            mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, missed);
            mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, remaining);

            return true;

        } else if (difDays < 0) {
            return false;


        }
        return true;


    }

    /**
     * populates arraylists from cursor
     * @param cursor database
     */
    public void makeValue(Cursor cursor) {
        mIdArrayList.clear();
        mCVArrayList.clear();

        if (cursor != null)
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                if (cursor.getString(COL_TYPE).equals(GoalContract.GoalEntry.GOAL)) {
                    mIdArrayList.add(cursor.getString(COL_GOAL_ID));
                }
                ContentValues contentValues = new ContentValues();
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


