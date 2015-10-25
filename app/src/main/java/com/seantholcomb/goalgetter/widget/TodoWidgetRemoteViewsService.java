package com.seantholcomb.goalgetter.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.text.format.Time;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.seantholcomb.goalgetter.R;
import com.seantholcomb.goalgetter.data.GoalContract;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by seanholcomb on 10/24/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodoWidgetRemoteViewsService extends RemoteViewsService {
        private static final String sortOrder = GoalContract.GoalEntry.COLUMN_DUE_DATE + " ASC";


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

                private Cursor data = null;

                @Override
                public void onCreate() {
                    // Nothing to do
                }

                @Override
                public void onDataSetChanged() {
                    if (data != null) {
                        data.close();
                    }
                    // This method is called by the app hosting the widget (e.g., the launcher)
                    // However, our ContentProvider is not exported so it doesn't have access to the
                    // data. Therefore we need to clear (and finally restore) the calling identity so
                    // that calls use our process and permission
                    final long identityToken = Binder.clearCallingIdentity();
                    // Get today's data from the ContentProvider

                    Uri TodoUri = DatabaseContract.scores_table.buildScoreWithDate();
                    data = getContentResolver().query(
                            TodayGameUri,
                            DATABASE_COLUMNS,
                            selection,
                            dateStrings,
                            DatabaseContract.scores_table.DATE_COL + " ASC");
                    if (data!=null) {
                        String ee = data.getCount()+" ";
                        Log.e("EEEEEEEEE", ee);
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
                    return data == null ? 0 : data.getCount();
                }

                @Override
                public RemoteViews getViewAt(int position) {
                    RemoteViews views = new RemoteViews(getPackageName(),
                            R.layout.widget_item);
                    if (position == AdapterView.INVALID_POSITION ||
                            data == null || !data.moveToPosition(position)) {
                        return views;
                    }

                    int gameId = data.getInt(INDEX_MATCH_ID);
                    String homeName = data.getString(INDEX_HOME);
                    String awayName = data.getString(INDEX_AWAY);
                    int homeCrest = Utilies.getTeamCrestByTeamName(homeName, getApplicationContext());
                    int awayCrest = Utilies.getTeamCrestByTeamName(awayName, getApplicationContext());
                    String score = Utilies.getScores(data.getInt(INDEX_HOME_GOALS), data.getInt(INDEX_AWAY_GOALS));
                    String date = data.getString(INDEX_MATCHTIME);
                    String day = data.getString(INDEX_DATE);
                    day = getDayName(day);

                    views.setTextViewText(R.id.day_text, day);
                    views.setTextViewText(R.id.home_name, homeName);
                    views.setTextViewText(R.id.away_name, awayName);
                    views.setTextViewText(R.id.score_textview, score);
                    views.setTextViewText(R.id.data_textview, date);
                    views.setImageViewResource(R.id.home_crest, homeCrest);
                    views.setImageViewResource(R.id.away_crest, awayCrest);

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
                        return data.getLong(INDEX_MATCH_ID);
                    return position;
                }

                @Override
                public boolean hasStableIds() {
                    return true;
                }
            };
        }
    }
