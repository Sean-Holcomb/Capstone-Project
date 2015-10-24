package com.seantholcomb.goalgetter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by seanholcomb on 10/11/15.
 */
public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoAdapterViewHolder> {
    private ArrayList<ContentValues> mCVArrayList;
    private Cursor mCursor;
    final private Context mContext;

    private static final String sUpdateSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_TASK + " = ? ";




    /**
     * Cache of the children views for a forecast list item.
     */
    public class TodoAdapterViewHolder extends RecyclerView.ViewHolder {
        public final CheckBox mBox;
        public final TextView mTitleView;


        public TodoAdapterViewHolder(View view) {
            super(view);
            mTitleView = (TextView) view.findViewById(R.id.todo_title);
            mBox = (CheckBox) view.findViewById(R.id.check_box);

        }

    }


    public TodoAdapter(Context context) {
        mContext = context;
        mCVArrayList = new ArrayList<ContentValues>();

        //mEmptyView = emptyView;
    }

    /*
        This takes advantage of the fact that the viewGroup passed to onCreateViewHolder is the
        RecyclerView that will be used to contain the view, so that it can get the current
        ItemSelectionManager from the view.

        One could implement this pattern without modifying RecyclerView by taking advantage
        of the view tag to store the ItemChoiceManager.
     */
    @Override
    public TodoAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.item_todo;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new TodoAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(TodoAdapterViewHolder todoAdapterViewHolder, final int position) {
        checkOnTrack(todoAdapterViewHolder, position);
        String title = mCVArrayList.get(position).getAsString(GoalContract.GoalEntry.COLUMN_TASK);
        todoAdapterViewHolder.mTitleView.setText(title);
        todoAdapterViewHolder.mBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    int plus = mCVArrayList.get(position).getAsInteger(GoalContract.GoalEntry.COLUMN_TASKS_DONE) + 1;
                    int minus = mCVArrayList.get(position).getAsInteger(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING) - 1;
                    mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, plus);
                    mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, minus);
                    Log.e("EEE", "changed");
                } else {
                    int minus = mCVArrayList.get(position).getAsInteger(GoalContract.GoalEntry.COLUMN_TASKS_DONE) - 1;
                    int plus = mCVArrayList.get(position).getAsInteger(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING) + 1;
                    mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, minus);
                    mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, plus);
                }

                mCursor.moveToPosition(position);
                String selectionArgs =  mCVArrayList.get(position).getAsString(GoalContract.GoalEntry.COLUMN_TASK);
                int updated = mContext.getContentResolver().update(GoalContract.BASE_CONTENT_URI,
                        mCVArrayList.get(position),
                        sUpdateSelection,
                        new String[] {selectionArgs});
                Log.e("EEEE", updated + "");
            }
        });
    }


    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    public void onSaveInstanceState(Bundle outState) {
    }


    @Override
    public int getItemCount() {
        if (null == mCVArrayList) return 0;
        return mCVArrayList.size();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        makeValue(newCursor);
        notifyDataSetChanged();
        //mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }


    public Cursor getCursor() {
        return mCursor;
    }

    public void makeValue(Cursor cursor) {
        mCVArrayList.clear();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            contentValues.put(GoalContract.GoalEntry.COLUMN_ID, cursor.getString(DashboardFragment.COL_GOAL_ID));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TYPE, cursor.getString(DashboardFragment.COL_TYPE));
            contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, cursor.getString(DashboardFragment.COL_NAME));
            contentValues.put(GoalContract.GoalEntry.COLUMN_START_DATE, cursor.getDouble(DashboardFragment.COL_START_DATE));
            contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, cursor.getDouble(DashboardFragment.COL_DUE_DATE));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, cursor.getString(DashboardFragment.COL_TASK));
            contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, cursor.getInt(DashboardFragment.COL_FREQUENCY));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, cursor.getInt(DashboardFragment.COL_TOTAL_TASKS));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, cursor.getInt(DashboardFragment.COL_DONE_TASK));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, cursor.getInt(DashboardFragment.COL_MISSED_TASKS));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, cursor.getInt(DashboardFragment.COL_REMAINING_TASKS));
            contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, cursor.getString(DashboardFragment.COL_STATUS));
            if (contentValues.getAsLong(GoalContract.GoalEntry.COLUMN_DUE_DATE)
                    < GoalContract.normalizeDate(Calendar.getInstance().getTimeInMillis())){
                contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, cursor.getInt(DashboardFragment.COL_DONE_TASK)
                        + cursor.getInt(DashboardFragment.COL_REMAINING_TASKS));
                contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, 0);
                contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.COMPLETE);
                String selectionArgs = mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_TASK);
                int updated = mContext.getContentResolver().update(GoalContract.BASE_CONTENT_URI,
                        mCVArrayList.get(i),
                        sUpdateSelection,
                        new String[] {selectionArgs});

            }else if (cursor.getInt(DashboardFragment.COL_FREQUENCY) != 0) {
                mCVArrayList.add(contentValues);
            }

        }
    }

    public void checkOnTrack(TodoAdapterViewHolder todoAdapterViewHolder, int position) {
        int done = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TASKS_DONE);
        int missed = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TASKS_MISSED);
        int remaining = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING);
        double start = (double) GoalContract.normalizeDate(Calendar.getInstance().getTimeInMillis());

        double end = mCVArrayList.get(position).getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE);
        int freq = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_FREQUENCY);


        int total = (int) mCVArrayList.get(position).get(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS);
        double dif = start - end;
        dif = dif / (1000 * 60 * 60 * 24);
        int difDays = (int) dif;
        difDays = difDays / freq;
        difDays = total - done - missed - difDays;
        if (difDays > 0) {
            missed += difDays;
            remaining -= difDays;
            mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, missed);
            mCVArrayList.get(position).put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, remaining);
            mCursor.moveToPosition(position);
            String selectionArgs = mCVArrayList.get(position).getAsString(GoalContract.GoalEntry.COLUMN_TASK);
            int updated = mContext.getContentResolver().update(GoalContract.BASE_CONTENT_URI,
                    mCVArrayList.get(position),
                    sUpdateSelection,
                    new String[] {selectionArgs});
            todoAdapterViewHolder.mBox.setChecked(false);
        } else if (difDays < 0) {

            todoAdapterViewHolder.mBox.setChecked(true);

        }



    }


}
