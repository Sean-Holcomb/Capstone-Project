package com.seantholcomb.goalgetter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.Calendar;

/**
 * Created by seanholcomb on 10/9/15.
 */
public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalAdapterViewHolder> {

    private static final int VIEW_TYPE_COMPLETE = 0;
    private static final int VIEW_TYPE_CURRENT = 1;


    private Cursor mCursor;
    final private Context mContext;
    final private GoalAdapterOnClickHandler mClickHandler;
    private static final String sUpdateGoalSelection =
            GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_ID + " = ? AND " +
                    GoalContract.GoalEntry.TABLE_NAME +
                    "." + GoalContract.GoalEntry.COLUMN_TYPE + " = ? ";

    /**
     * Cache of the children views for a forecast list item.
     */
    public class GoalAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mRedBar;
        public final View mClearBar;
        public final View mGreenBar;
        public final TextView mTitleView;
        public final TextView mPercentView;
        public final TextView mMinusPercentView;
        public long due_date;

        public GoalAdapterViewHolder(View view) {
            super(view);
            mClearBar= view.findViewById(R.id.clear_bar);
            mRedBar = view.findViewById(R.id.red_bar);
            mGreenBar = view.findViewById(R.id.green_bar);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mPercentView = (TextView) view.findViewById(R.id.percent);
            mMinusPercentView = (TextView) view.findViewById(R.id.minus_precent);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            mCursor.moveToPosition(adapterPosition);
            int idColumnIndex = mCursor.getColumnIndex(GoalContract.GoalEntry.COLUMN_ID);
            mClickHandler.onClick(mCursor.getString(idColumnIndex), this);
            //mICM.onClick(this);
        }
    }

    public static interface GoalAdapterOnClickHandler {
        void onClick(String id, GoalAdapterViewHolder vh);
    }

    public GoalAdapter(Context context, GoalAdapterOnClickHandler dh) {
        mContext = context;
        mClickHandler = dh;
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
    public GoalAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = R.layout.item_graph;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new GoalAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(GoalAdapterViewHolder goalAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        goalAdapterViewHolder.due_date=mCursor.getLong(DashboardFragment.COL_DUE_DATE);
        checkDueDate(goalAdapterViewHolder, position);

        int total_tasks = mCursor.getInt(DashboardFragment.COL_TOTAL_TASKS);
        int tasks_done = mCursor.getInt(DashboardFragment.COL_DONE_TASK);
        int tasks_missed = mCursor.getInt(DashboardFragment.COL_MISSED_TASKS);
        String title = mCursor.getString(DashboardFragment.COL_NAME);
        goalAdapterViewHolder.mTitleView.setText(title);
        int[] percents =Utility.getPercents(total_tasks,tasks_done,tasks_missed);
        goalAdapterViewHolder.mPercentView.setText(percents[0]+"%");
        goalAdapterViewHolder.mMinusPercentView.setText("-" + percents[1] + "%");
        if (mCursor.getString(DashboardFragment.COL_STATUS).equals(GoalContract.GoalEntry.COMPLETE)) {
            goalAdapterViewHolder.mGreenBar.setBackgroundColor(mContext.getResources().getColor(R.color.gold));
            percents[0]=percents[0]+percents[2];
            percents[2]=0;
        }
        setBarWeight(goalAdapterViewHolder.mGreenBar, percents[0]);
        setBarWeight(goalAdapterViewHolder.mRedBar, percents[1]);
        setBarWeight(goalAdapterViewHolder.mClearBar, percents[2]);



    }
    public void checkDueDate(GoalAdapterViewHolder goalAdapterViewHolder, int position ){
        mCursor.moveToPosition(position);
        if (goalAdapterViewHolder.due_date
                < GoalContract.normalizeDate(Calendar.getInstance().getTimeInMillis())
                && !mCursor.getString(DashboardFragment.COL_STATUS).equals(GoalContract.GoalEntry.COMPLETE)) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(GoalContract.GoalEntry.COLUMN_ID, mCursor.getString(DashboardFragment.COL_GOAL_ID));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TYPE, mCursor.getString(DashboardFragment.COL_TYPE));
            contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, mCursor.getString(DashboardFragment.COL_NAME));
            contentValues.put(GoalContract.GoalEntry.COLUMN_START_DATE, mCursor.getDouble(DashboardFragment.COL_START_DATE));
            contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, mCursor.getDouble(DashboardFragment.COL_DUE_DATE));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, mCursor.getString(DashboardFragment.COL_TASK));
            contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, mCursor.getInt(DashboardFragment.COL_FREQUENCY));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, mCursor.getInt(DashboardFragment.COL_TOTAL_TASKS));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, mCursor.getInt(DashboardFragment.COL_DONE_TASK)
                    + mCursor.getInt(DashboardFragment.COL_REMAINING_TASKS));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, mCursor.getInt(DashboardFragment.COL_MISSED_TASKS));
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, 0);
            contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.COMPLETE);
            String selectionArgs = contentValues.getAsString(GoalContract.GoalEntry.COLUMN_ID);
            mContext.getContentResolver().update(GoalContract.BASE_CONTENT_URI,
                    contentValues,
                    sUpdateGoalSelection,
                    new String[]{selectionArgs, GoalContract.GoalEntry.GOAL});
        }
    }

    public void setBarWeight(View view, int weight){
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) view.getLayoutParams();

        linearParams.weight = weight;
        view.setLayoutParams(linearParams);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        //mICM.onSaveInstanceState(outState);
    }


//    public int getSelectedItemPosition() {
        //return mICM.getSelectedItemPosition();
//    }

    @Override
    public int getItemViewType(int position) {
        return position;
        //return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
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

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof GoalAdapterViewHolder ) {
            GoalAdapterViewHolder vfh = (GoalAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }


}