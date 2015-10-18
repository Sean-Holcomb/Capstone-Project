package com.seantholcomb.goalgetter.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by seanholcomb on 10/8/15.
 */
public class GoalContract {


    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.seantholcomb.goalgetter";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.

    public static final String PATH_GOAL= "goal";
    public static final String PATH_TODO= "todo";
    public static final String PATH_PAST = "past";
    public static final String PATH_CURRENT = "current";
    public static final String PATH_GOAL_MILESTONE= "goal_milestone";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the table contents of the location table */
    public static final class GoalEntry implements BaseColumns {

        public static final Uri GOAL_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GOAL).build();
        public static final Uri CURRENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CURRENT).build();
        public static final Uri PAST_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PAST).build();
        public static final Uri TODO_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TODO).build();
        public static final Uri GOAL_MILESTONE_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GOAL_MILESTONE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOAL;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOAL;

        // Table name
        public static final String TABLE_NAME = "goal";
        //ID to group together goals with their milestones
        public static final String COLUMN_ID="id";
        //Type either goal or milestone
        public static final String COLUMN_TYPE="type";
        //title or name of the milestone
        public static final String COLUMN_NAME="name";
        //Start date of item for calculating total tasks
        public static final String COLUMN_START_DATE="start_date";
        //due date of item to be displayed
        public static final String COLUMN_DUE_DATE="due_date";
        //String that defines what task needs to be done for user
        public static final String COLUMN_TASK = "task";
        //how many times a week a task is to be preformed
        public static final String COLUMN_FREQUENCY = "frequency";
        //total number of tasks to be preformed
        public static final String COLUMN_TOTAL_TASKS = "total_tasks";
        //number of tasks alright completed
        public static final String COLUMN_TASKS_DONE= "tasks_done";
        //number of tasks missed
        public static final String COLUMN_TASKS_MISSED="tasks_missed";
        //number of tasks remaining befor due date
        public static final String COLUMN_TASKS_REMAINING="remaining_tasks";
        //the status of the item:waiting,disabled,concurrent,complete
        public static final String COLUMN_STATUS = "status";

        //status column states
        public static final String ACTIVE="active";
        public static final String PENDING = "pending";
        public static final String COMPLETE="complete";
        public static final String CONCURRENT="concurrent";

        //Type column type
        public static final String GOAL = "goal";
        public static final String MILESTONE = "milestone";


        public static Uri buildGoalUri(long id) {
            return ContentUris.withAppendedId(BASE_CONTENT_URI, id);
        }
    }

}
