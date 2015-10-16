package com.seantholcomb.goalgetter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.Calendar;

/**
 * Created by seanholcomb on 10/14/15.
 */
public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.MilestoneAdapterViewHolder> {


    private Cursor mCursor;
    final private Context mContext;



    /**
     * Cache of the children views for a forecast list item.
     */
    public class MilestoneAdapterViewHolder extends RecyclerView.ViewHolder{
        public final EditText mTitle;
        public final EditText mTask;
        public final EditText mFrequency;
        public final TextView mDueDate;
        public final CheckBox mConcurrent;
        public final Button mDelete;
        public ContentValues mContentValues;
        public double due_date;

        public MilestoneAdapterViewHolder(View view) {
            super(view);
            mTitle=  (EditText) view.findViewById(R.id.milestone_title);
            mTask = (EditText) view.findViewById(R.id.todo_item);
            mFrequency = (EditText) view.findViewById(R.id.frequency);
            mDueDate = (TextView) view.findViewById(R.id.milesstone_duedate);
            mConcurrent = (CheckBox) view.findViewById(R.id.concurrent);
            mDelete = (Button) view.findViewById(R.id.delete_milestone);


        }

    }




    public MilestoneAdapter(Context context) {
        mContext = context;
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
    public MilestoneAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = R.layout.item_milestone;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new MilestoneAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(MilestoneAdapterViewHolder milestoneAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        milestoneAdapterViewHolder.mContentValues= makeValue(mCursor);
        milestoneAdapterViewHolder.due_date=mCursor.getDouble(DashboardFragment.COL_DUE_DATE);
        milestoneAdapterViewHolder.mDueDate.setText(Utility.getDate((long) milestoneAdapterViewHolder.due_date));
        milestoneAdapterViewHolder.mTitle.setText(mCursor.getString(DashboardFragment.COL_NAME));
        milestoneAdapterViewHolder.mFrequency.setText(String.valueOf(mCursor.getInt(DashboardFragment.COL_FREQUENCY)));
        milestoneAdapterViewHolder.mTask.setText(mCursor.getString(DashboardFragment.COL_TASK));
        milestoneAdapterViewHolder.mConcurrent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked &&
                        !milestoneAdapterViewHolder.mContentValues
                                .get(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.COMPLETE)){
                    milestoneAdapterViewHolder.mContentValues
                            .put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.ACTIVE);
                }else if (position > 0){

                }
            }
        });

        milestoneAdapterViewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        milestoneAdapterViewHolder.mDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public ContentValues makeValue(Cursor cursor){
        ContentValues contentValues = new ContentValues();

        contentValues.put(GoalContract.GoalEntry.COLUMN_ID, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_TYPE, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_START_DATE, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, cursor.getString(DashboardFragment.COL_ID));
        contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.COMPLETE) ;
        //mContext.getContentResolver().insert(GoalContract.GoalEntry.GOAL_URI, contentValues);
        return contentValues;
    }

    public ContentValues updateTasks(ContentValues contentValues){
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTimeInMillis();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        //mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

}
