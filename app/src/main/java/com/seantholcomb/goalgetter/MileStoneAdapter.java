package com.seantholcomb.goalgetter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by seanholcomb on 10/14/15.
 */
public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.MilestoneAdapterViewHolder> {


    private Cursor mCursor;
    final private Context mContext;
    private final ArrayList<ContentValues> mCVArrayList;
    private boolean mIsEditable = false;
    private String mID;
    private double mDueDate;




    /**
     * Cache of the children views for a forecast list item.
     */
    public class MilestoneAdapterViewHolder extends RecyclerView.ViewHolder {
        public final EditText mTitle;
        public final EditText mTask;
        public final EditText mFrequency;
        public final EditText mDueDate;
        public final CheckBox mConcurrent;
        public final Button mDelete;
        public double due_date;


        public MilestoneAdapterViewHolder(View view) {
            super(view);
            mTitle = (EditText) view.findViewById(R.id.milestone_title);
            mTask = (EditText) view.findViewById(R.id.todo_item);
            mFrequency = (EditText) view.findViewById(R.id.frequency);
            mDueDate = (EditText) view.findViewById(R.id.milesstone_duedate);
            mConcurrent = (CheckBox) view.findViewById(R.id.concurrent);
            mDelete = (Button) view.findViewById(R.id.delete_milestone);


        }

    }


    public MilestoneAdapter(Context context) {
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
    public MilestoneAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.item_milestone;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new MilestoneAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final MilestoneAdapterViewHolder milestoneAdapterViewHolder, final int position) {
        final ContentValues contentValues = mCVArrayList.get(position);

        milestoneAdapterViewHolder.due_date = contentValues.getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE);
        milestoneAdapterViewHolder.mDueDate.setText(Utility.getDate((long) milestoneAdapterViewHolder.due_date));
        milestoneAdapterViewHolder.mTitle.setText(contentValues.getAsString(GoalContract.GoalEntry.COLUMN_NAME));
        milestoneAdapterViewHolder.mFrequency.setText(String.valueOf(contentValues.get(GoalContract.GoalEntry.COLUMN_FREQUENCY)));
        milestoneAdapterViewHolder.mTask.setText(contentValues.getAsString(GoalContract.GoalEntry.COLUMN_TASK));
        if (contentValues.getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.ACTIVE)
                && position != 0
                && !mCVArrayList.get(position - 1).getAsString(GoalContract.GoalEntry.COLUMN_STATUS)
                .equals(GoalContract.GoalEntry.COMPLETE)) {
            milestoneAdapterViewHolder.mConcurrent.setChecked(true);
            contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.CONCURRENT);

        }

        //Checks if views should be editable or not. If not listeners are not initalized
        milestoneAdapterViewHolder.mFrequency.setEnabled(mIsEditable);
        milestoneAdapterViewHolder.mTitle.setEnabled(mIsEditable);
        milestoneAdapterViewHolder.mTask.setEnabled(mIsEditable);
        if (!mIsEditable) {
            milestoneAdapterViewHolder.mConcurrent.setVisibility(View.GONE);
            milestoneAdapterViewHolder.mDelete.setVisibility(View.GONE);
            milestoneAdapterViewHolder.mDueDate.setOnClickListener(null);
            return;
        }
        milestoneAdapterViewHolder.mConcurrent.setVisibility(View.VISIBLE);
        milestoneAdapterViewHolder.mDelete.setVisibility(View.VISIBLE);


        milestoneAdapterViewHolder.mConcurrent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked
                        && !contentValues.getAsString(GoalContract.GoalEntry.COLUMN_STATUS)
                        .equals(GoalContract.GoalEntry.COMPLETE)) {
                    contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.CONCURRENT);
                } else {
                    contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.PENDING);
                }
            }
        });


        milestoneAdapterViewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCVArrayList.remove(position);
                notifyDataSetChanged();

            }
        });

        milestoneAdapterViewHolder.mFrequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    int freq = Integer.parseInt(s.toString());
                    if (freq >= 0 && freq <= 7) {
                        contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, freq);
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.freq_toast), Toast.LENGTH_SHORT).show();
                        contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, 0);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        milestoneAdapterViewHolder.mTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        milestoneAdapterViewHolder.mTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        milestoneAdapterViewHolder.mDueDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double duedate = Utility.getDateDouble(s.toString());
                if (duedate<= mDueDate){
                    contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, duedate);
                }else{
                    Toast.makeText(mContext, mContext.getString(R.string.adapter_date_prompt), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        milestoneAdapterViewHolder.mDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DetailFragment.DatePickerFragment(v);
                newFragment.show(((FragmentActivity)mContext).getSupportFragmentManager(), "datePicker");
            }
        });


    }

    public void setmID(String id) {
        mID = id;
        for (int i = 0; i < mCVArrayList.size(); i++) {
            mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_ID, id);
        }
    }

    public void setDueDate(Double dueDate) {
        mDueDate = dueDate;
    }


    public void makeValue(Cursor cursor) {
        mCVArrayList.clear();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            if (cursor.getString(DashboardFragment.COL_TYPE).equals(GoalContract.GoalEntry.GOAL)) {
                mDueDate = cursor.getDouble(DashboardFragment.COL_DUE_DATE);
                mID= cursor.getString(DashboardFragment.COL_ID);
                continue;
            }
            contentValues.put(GoalContract.GoalEntry.COLUMN_ID, cursor.getString(DashboardFragment.COL_ID));
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
            mCVArrayList.add(contentValues);
            //mContext.getContentResolver().insert(GoalContract.GoalEntry.GOAL_URI, contentValues);
        }
    }

    public void addNew() {
        if (mID == null || mDueDate == 0) {
            Toast.makeText(mContext, mContext.getString(R.string.milestone_toast), Toast.LENGTH_SHORT).show();
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(GoalContract.GoalEntry.COLUMN_ID, mID);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TYPE, GoalContract.GoalEntry.MILESTONE);
            contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, mContext.getString(R.string.new_milestone));
            contentValues.put(GoalContract.GoalEntry.COLUMN_START_DATE, 0);
            contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, mDueDate);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, mContext.getString(R.string.task_prompt));
            contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, 0);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, 0);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, 0);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, 0);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, 0);
            contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.PENDING);
            mCVArrayList.add(0, contentValues);

            notifyDataSetChanged();
        }
    }


    public ArrayList<ContentValues> save() {
        arrangeForSave();
        return mCVArrayList;
    }

    public void cancel() {
        makeValue(mCursor);
        notifyDataSetChanged();
    }

    public void makeEditable(Boolean isEditable) {
        mIsEditable = isEditable;
        notifyDataSetChanged();
    }

    public void updateTasks(ContentValues contentValues) {
        int done = (int) contentValues.get(GoalContract.GoalEntry.COLUMN_TASKS_DONE);
        int missed = (int) contentValues.get(GoalContract.GoalEntry.COLUMN_TASKS_MISSED);
        double start = contentValues.getAsDouble(GoalContract.GoalEntry.COLUMN_START_DATE);
        double end = contentValues.getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE);
        int freq = (int) contentValues.get(GoalContract.GoalEntry.COLUMN_FREQUENCY);
        if (freq == 0) {
            contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, done + missed);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, 0);
        } else {

            double dif = start - end;
            dif = dif / (1000 * 60 * 60 * 24);
            int difDays = (int) dif;
            difDays = difDays / freq;
            contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, difDays);
            contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, difDays - done - missed);

        }

    }

    public void arrangeForSave(){
        ContentValues contentValues1;
        ContentValues contentValues2;
        for (int i = 0; i < mCVArrayList.size(); i++){
            for (int j = i+1; j<mCVArrayList.size(); j++){
                contentValues1=mCVArrayList.get(i);
                contentValues2=mCVArrayList.get(j);
                if(contentValues1.getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE)<
                        contentValues2.getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE)) {
                    mCVArrayList.remove(j);
                    mCVArrayList.add(i, contentValues2);
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        if (mCVArrayList.size()!=0) {
            if (!mCVArrayList.get(0).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.COMPLETE)) {
                mCVArrayList.get(0).put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.ACTIVE);
            }
            if (mCVArrayList.get(0).getAsDouble(GoalContract.GoalEntry.COLUMN_START_DATE) == 0) {
                mCVArrayList.get(0).put(GoalContract.GoalEntry.COLUMN_START_DATE, (double) calendar.getTimeInMillis());
                updateTasks(mCVArrayList.get(0));
            }
        }
        for (int i = 1; i < mCVArrayList.size(); i++){
            if (mCVArrayList.get(i).getAsDouble(GoalContract.GoalEntry.COLUMN_START_DATE)
                    < (double) calendar.getTimeInMillis()){
                mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.COMPLETE);
            }

            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.COMPLETE)){
                continue;
            }


            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.PENDING)){
                if (mCVArrayList.get(i-1).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.COMPLETE)){
                    mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.ACTIVE);
                }else {
                    mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_START_DATE,
                            mCVArrayList.get(i - 1).getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE));
                    mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, 0);
                    mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, 0);
                }
            }

            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.CONCURRENT)){
                mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.ACTIVE);
            }

            if (mCVArrayList.get(i).getAsString(GoalContract.GoalEntry.COLUMN_STATUS).equals(GoalContract.GoalEntry.ACTIVE)) {
                if (mCVArrayList.get(i).getAsDouble(GoalContract.GoalEntry.COLUMN_START_DATE) == 0
                        || mCVArrayList.get(i).getAsDouble(GoalContract.GoalEntry.COLUMN_START_DATE)
                        > (double) calendar.getTimeInMillis()) {
                    mCVArrayList.get(i).put(GoalContract.GoalEntry.COLUMN_START_DATE, (double) calendar.getTimeInMillis());
                }
            }
            updateTasks(mCVArrayList.get(i));

        }
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return mCVArrayList.size();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        makeValue(mCursor);
        notifyDataSetChanged();
        //mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

}
