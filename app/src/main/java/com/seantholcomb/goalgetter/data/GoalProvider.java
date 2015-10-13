package com.seantholcomb.goalgetter.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by seanholcomb on 10/8/15.
 */
public class GoalProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private GoalDbHelper mOpenHelper;

    static final int GOALS = 100;
    static final int GOAL_WITH_MILESTONES = 101;


    private static final SQLiteQueryBuilder sGoalQueryBuilder;

    static{
        sGoalQueryBuilder = new SQLiteQueryBuilder();

        sGoalQueryBuilder.setTables(GoalContract.GoalEntry.TABLE_NAME);
    }



    private static final String sGoalSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_TYPE + " = ? ";


    private static final String sGoalAndMilestoneSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_ID + " = ? ";

    private Cursor getGoal(Uri uri, String[] projection, String sortOrder) {
        return sGoalQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sGoalSelection,
                new String[] {GoalContract.GoalEntry.GOAL},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getGoalAndMilestones(
            Uri uri, String[] projection, String[] selectionArgs, String sortOrder) {


        return sGoalQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sGoalAndMilestoneSelection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = GoalContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, GoalContract.PATH_GOAL, GOALS);
        matcher.addURI(authority, GoalContract.PATH_GOAL + "/*", GOAL_WITH_MILESTONES);

        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new GoalDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case GOALS:
                return GoalContract.GoalEntry.CONTENT_TYPE;
            case GOAL_WITH_MILESTONES:
                return GoalContract.GoalEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "goal"
            case GOALS: {
                retCursor = getGoal(uri, projection, sortOrder);
                break;
            }
            // "goal/*"
            case GOAL_WITH_MILESTONES: {
                retCursor = getGoalAndMilestones(uri, projection, selectionArgs, sortOrder);
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case GOALS: {
                normalizeDate(values);
                long _id = db.insert(GoalContract.GoalEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = GoalContract.GoalEntry.buildGoalUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case GOALS:
                rowsDeleted = db.delete(
                        GoalContract.GoalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(GoalContract.GoalEntry.COLUMN_START_DATE)) {
            long dateValue = values.getAsLong(GoalContract.GoalEntry.COLUMN_START_DATE);
            values.put(GoalContract.GoalEntry.COLUMN_START_DATE, GoalContract.normalizeDate(dateValue));
        }
        if (values.containsKey(GoalContract.GoalEntry.COLUMN_DUE_DATE)) {
            long dateValue = values.getAsLong(GoalContract.GoalEntry.COLUMN_DUE_DATE);
            values.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, GoalContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rowsUpdated;

        normalizeDate(values);
        rowsUpdated = db.update(GoalContract.GoalEntry.TABLE_NAME, values, selection,
                selectionArgs);


    if(rowsUpdated!=0)
    {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    return rowsUpdated;
}

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GOALS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(GoalContract.GoalEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}