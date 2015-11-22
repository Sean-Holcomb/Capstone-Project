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
 * Custom content provider for this application
 * database queries are made through here
 * Created by seanholcomb on 10/8/15.
 */
public class GoalProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private GoalDbHelper mOpenHelper;
    public static final String DELETE_ALL="delete";
    static final int GOAL = 99;
    static final int PAST = 100;
    static final int GOAL_WITH_MILESTONES = 101;
    static final int CURRENT = 102;
    static final int TODO = 103;


    private static final SQLiteQueryBuilder sGoalQueryBuilder;

    static{
        sGoalQueryBuilder = new SQLiteQueryBuilder();

        sGoalQueryBuilder.setTables(GoalContract.GoalEntry.TABLE_NAME);
    }


    //Different selections arguments used across the app
    private static final String sGoalSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_TYPE + " = ? ";



    private static final String sGoalAndMilestoneSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_ID + " = ? ";

    private static final String sTodoSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_STATUS + " = ? ";

    private static final String sCurrentSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_STATUS + " = ? AND " +
                    GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_TYPE + " = ? ";

    private static final String sPastSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_STATUS + " = ? AND " +
                    GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_TYPE + " = ? ";

    //returns entire database
    private Cursor getGoal(Uri uri, String[] projection, String sortOrder) {
        return sGoalQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    //returns goals and milestones with a given id
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

    //returns goals that are marked as complete
    private Cursor getPastGoals(
            Uri uri, String[] projection, String[] selectionArgs, String sortOrder) {


        return sGoalQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sPastSelection,
                new String[] {GoalContract.GoalEntry.COMPLETE, GoalContract.GoalEntry.GOAL},
                null,
                null,
                sortOrder
        );
    }

    //returns current goals
    private Cursor getCurrentGoal(
            Uri uri, String[] projection, String[] selectionArgs, String sortOrder) {

        return sGoalQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCurrentSelection,
                new String[] {GoalContract.GoalEntry.ACTIVE, GoalContract.GoalEntry.GOAL},
                null,
                null,
                sortOrder
        );
    }

    //returns current goals and milestones
    private Cursor getTodo(
            Uri uri, String[] projection, String[] selectionArgs, String sortOrder) {


        return sGoalQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sTodoSelection,
                new String[] {GoalContract.GoalEntry.ACTIVE},
                null,
                null,
                sortOrder
        );
    }

    /**
     * Build uri matcher to sort requests
     * @return said urimatcher
     */
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = GoalContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, GoalContract.PATH_PAST, PAST);
        matcher.addURI(authority, GoalContract.PATH_CURRENT, CURRENT);
        matcher.addURI(authority, GoalContract.PATH_TODO, TODO);
        matcher.addURI(authority, GoalContract.PATH_GOAL_MILESTONE, GOAL_WITH_MILESTONES);
        matcher.addURI(authority, GoalContract.PATH_GOAL, GOAL);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new GoalDbHelper(getContext());
        return true;
    }

    /**
     * match the uri to content type, all are the same
     * @param uri
     * @return
     */
    @Override
    public String getType(Uri uri) {


        final int match = sUriMatcher.match(uri);

        switch (match) {

            case GOAL:
                return GoalContract.GoalEntry.CONTENT_TYPE;
            case PAST:
                return GoalContract.GoalEntry.CONTENT_TYPE;
            case CURRENT:
                return GoalContract.GoalEntry.CONTENT_TYPE;
            case TODO:
                return GoalContract.GoalEntry.CONTENT_TYPE;
            case GOAL_WITH_MILESTONES:
                return GoalContract.GoalEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * matches uri and runs proper query method
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return cursor with appropriate data
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {




            case PAST:
                retCursor = getPastGoals(uri, projection, selectionArgs, sortOrder);
                break;


            case GOAL_WITH_MILESTONES:
                retCursor = getGoalAndMilestones(uri, projection, selectionArgs, sortOrder);
                break;

            case CURRENT:
                retCursor = getCurrentGoal(uri, projection, selectionArgs, sortOrder);
                break;

            case TODO:
                retCursor = getTodo(uri, projection, selectionArgs, sortOrder);
                break;

            case GOAL:
                retCursor = getGoal(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case GOAL: {
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
        if (selection.equals(DELETE_ALL)) selection = null;
        switch (match) {
            case GOAL:
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
            case GOAL:
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

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}