package com.seantholcomb.goalgetter.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.seantholcomb.goalgetter.R;
import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by seanholcomb on 10/24/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodoWidgetRemoteViewsService extends RemoteViewsService {
        private static final String sortOrder = GoalContract.GoalEntry.COLUMN_DUE_DATE + " ASC";

    //todo fix widget adaptor
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
    static final int COL_REMAINING_TASKS= 11;
    static final int COL_STATUS = 12;

        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
            return new RemoteViewsFactory() {
                private ArrayList<String> mTodoArrayList;

                private Cursor data = null;

                @Override
                public void onCreate() {
                    // Nothing to do
                    mTodoArrayList = new ArrayList<String>();

                }

                @Override
                public void onDataSetChanged() {
                    if (data != null) {
                        data.close();
                    }

                    final long identityToken = Binder.clearCallingIdentity();


                    Uri TodoUri = GoalContract.GoalEntry.TODO_URI;
                    data = getContentResolver().query(
                            TodoUri,
                            Goal_COLUMNS,
                            null,
                            null,
                            sortOrder);
                    if (data!=null) {
                        makeValue(data);

                    }
                    Binder.restoreCallingIdentity(identityToken);
                }

                @Override
                public void onDestroy() {
                    if (data != null) {
                        data.close();
                        data = null;
                    }
                }

                @Override
                public int getCount() {
                    return mTodoArrayList == null ? 0 : mTodoArrayList.size();
                }

                @Override
                public RemoteViews getViewAt(int position) {
                    RemoteViews views = new RemoteViews(getPackageName(),
                            R.layout.widget_item);
                    if (position == AdapterView.INVALID_POSITION ||
                            data == null || !data.moveToPosition(position)) {
                        return views;
                    }
                    views.setTextViewText(R.id.todo_title, mTodoArrayList.get(position));

                    return views;
                }

                @Override
                public RemoteViews getLoadingView() {
                    return new RemoteViews(getPackageName(), R.layout.widget_item);
                }

                @Override
                public int getViewTypeCount() {
                    return 1;
                }

                @Override
                public long getItemId(int position) {
                    if (data.moveToPosition(position))
                        return data.getLong(COL_ID);
                    return position;
                }

                @Override
                public boolean hasStableIds() {
                    return true;
                }

                public void makeValue(Cursor cursor) {
                    mTodoArrayList.clear();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        if (cursor.getString(COL_TYPE).equals(GoalContract.GoalEntry.GOAL)
                                || cursor.getInt(COL_FREQUENCY) == 0
                                || checkOnTrack(cursor, i)){
                            continue;
                        }

                        mTodoArrayList.add(cursor.getString(COL_TASK));

                    }
                }

                public boolean checkOnTrack(Cursor cursor, int position) {
                    cursor.moveToPosition(position);
                    int done = cursor.getInt(COL_DONE_TASK);
                    int missed = cursor.getInt(COL_MISSED_TASKS);
                    double start = (double) GoalContract.normalizeDate(Calendar.getInstance().getTimeInMillis());
                    double end = cursor.getDouble(COL_DUE_DATE);
                    int freq = cursor.getInt(COL_FREQUENCY);


                    int total = cursor.getInt(COL_TOTAL_TASKS);

                    double dif = end - start;
                    dif = dif / (1000 * 60 * 60 * 24);
                    dif = dif / 7 * freq;
                    int difDays = (int) dif;
                    difDays = total - done - missed - difDays;
                    if (difDays >= 0) {
                        return false;
                    } else {
                       return true;
                    }



                }
            };
        }
    }
