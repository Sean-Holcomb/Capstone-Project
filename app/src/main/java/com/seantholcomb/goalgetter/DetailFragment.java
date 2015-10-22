package com.seantholcomb.goalgetter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.ArrayList;
import java.util.Calendar;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
    static final int COL_REMAINING = 11;
    static final int COL_STATUS = 12;

    private GoalAdapter mGoalAdapter;
    private MilestoneAdapter mMilestoneAdapter;
    private RecyclerView mMilestoneGraph;
    private RecyclerView mMilestoneList;
    private EditText titleView;
    private EditText duedateView;
    private Button addMilestoneButton;
    private Button saveButton;
    private Button cancelButton;
    private ContentValues GoalValue;
    private final String TITLE_KEY = "title";
    private final String DATE_KEY = "due_date";
    private String titleString;
    private String dateString;
    private Boolean isNew;

    public DetailFragment() {
        // Required empty public constructor
    }

    public interface Callback {
        public void onSave(Bundle args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(TITLE_KEY)) {
            isNew = false;
            titleString = args.getString(TITLE_KEY);
            double dateDouble = args.getDouble(DATE_KEY);
            dateString =  Utility.getDate((long) dateDouble);
            GoalValue=newGoal();
            getLoaderManager().initLoader(0, args, this);

        } else {
            isNew = true;
            titleString = getString(R.string.title);
            dateString = getString(R.string.due_date);
            GoalValue=newGoal();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        titleView = (EditText) rootView.findViewById(R.id.goal_title);
        duedateView = (EditText) rootView.findViewById(R.id.goal_date);
        addMilestoneButton = (Button) rootView.findViewById(R.id.add_button);
        saveButton = (Button) rootView.findViewById(R.id.save_button);
        cancelButton = (Button) rootView.findViewById(R.id.cancel_button);
        mMilestoneList = (RecyclerView) rootView.findViewById(R.id.milestone_list);
        mMilestoneGraph = (RecyclerView) rootView.findViewById(R.id.milestone_graph);

        titleView.setText(titleString);
        duedateView.setText(dateString);

        mMilestoneGraph.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMilestoneList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGoalAdapter = new GoalAdapter(getActivity(), new GoalAdapter.GoalAdapterOnClickHandler() {
            @Override
            public void onClick(String id, GoalAdapter.GoalAdapterViewHolder vh) {
                //This does not need to do anything
            }
        });

        mMilestoneAdapter = new MilestoneAdapter(getActivity());
        if (!isNew){
            mMilestoneAdapter.setmID(titleString);
            mMilestoneAdapter.setDueDate(Utility.getDateDouble(dateString));
        }
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNew = false;
                ArrayList<ContentValues> CVAL = mMilestoneAdapter.save();
                Log.e("EEEEEEE", titleString);
                getContext().getContentResolver().delete(GoalContract.GoalEntry.GOAL_URI, sDelete, new String[]{titleString});
                titleString = GoalValue.getAsString(GoalContract.GoalEntry.COLUMN_ID);
                double date = GoalValue.getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE);
                dateString = Utility.getDate((long) date);
                setGoalTasks(CVAL);
                CVAL.add(0, GoalValue);
                ContentValues[] CVArray = CVAL.toArray(new ContentValues[CVAL.size()]);
                getContext().getContentResolver().bulkInsert(GoalContract.GoalEntry.GOAL_URI, CVArray);
                Bundle args = new Bundle();
                args.putString(TITLE_KEY, titleString);
                args.putDouble(DATE_KEY, date);
                ((Callback) getActivity()).onSave(args);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditable(false);
                mMilestoneAdapter.cancel();
                titleView.setText(titleString);
                duedateView.setText(dateString);
                if (isNew){
                    ((DashBoardActivity) getActivity()).openDrawer();
                }
            }
        });

        addMilestoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMilestoneAdapter.addNew();
            }
        });

        titleView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMilestoneAdapter.setmID(s.toString());
                GoalValue.put(GoalContract.GoalEntry.COLUMN_ID, s.toString());
                GoalValue.put(GoalContract.GoalEntry.COLUMN_NAME, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        duedateView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double duedate = Utility.getDateDouble(s.toString());
                if (duedate>= GoalValue.getAsDouble(GoalContract.GoalEntry.COLUMN_START_DATE)){
                    mMilestoneAdapter.setDueDate(duedate);
                    GoalValue.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, duedate);
                }else if (duedate != -1.0){
                    Toast.makeText(getActivity(), getString(R.string.date_prompt), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        mMilestoneGraph.setAdapter(mGoalAdapter);
        mMilestoneList.setAdapter(mMilestoneAdapter);
        setEditable(isNew);
        return rootView;
    }

    public void setEditable(Boolean editable) {
        if (editable) {
            cancelButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            addMilestoneButton.setVisibility(View.VISIBLE);
            if (!duedateView.hasOnClickListeners()) {
                duedateView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new DatePickerFragment(v);
                        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");

                    }
                });
            }
        } else {
            cancelButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            addMilestoneButton.setVisibility(View.GONE);
            duedateView.setOnClickListener(null);
        }
        titleView.setEnabled(editable);
        mMilestoneAdapter.makeEditable(editable);


    }

    public void addDummyData() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(GoalContract.GoalEntry.COLUMN_ID,"Learn toRead");
        contentValues.put(GoalContract.GoalEntry.COLUMN_TYPE, GoalContract.GoalEntry.GOAL);
        contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, "Learn toRead");
        contentValues.put(GoalContract.GoalEntry.COLUMN_START_DATE, 2);
        contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, 1);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, "read good)");
        contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, 3);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, 200);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, 100);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, 50);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, 50);
        contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.COMPLETE) ;
        //getContext().getContentResolver().insert(GoalContract.GoalEntry.GOAL_URI, contentValues);
    }

    public void deleteGoal() {
        getContext().getContentResolver().delete(GoalContract.GoalEntry.GOAL_URI, sDelete, new String[]{titleString});
        Bundle args = new Bundle();
        ((Callback) getActivity()).onSave(args);
    }


    public void getGoal(Cursor cursor){
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            if (cursor.getString(DashboardFragment.COL_TYPE).equals(GoalContract.GoalEntry.GOAL)) {
                GoalValue.put(GoalContract.GoalEntry.COLUMN_ID, cursor.getString(DashboardFragment.COL_GOAL_ID));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TYPE, cursor.getString(DashboardFragment.COL_TYPE));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_NAME, cursor.getString(DashboardFragment.COL_NAME));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_START_DATE, cursor.getDouble(DashboardFragment.COL_START_DATE));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, cursor.getDouble(DashboardFragment.COL_DUE_DATE));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASK, cursor.getString(DashboardFragment.COL_TASK));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, cursor.getInt(DashboardFragment.COL_FREQUENCY));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, cursor.getInt(DashboardFragment.COL_TOTAL_TASKS));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, cursor.getInt(DashboardFragment.COL_DONE_TASK));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, cursor.getInt(DashboardFragment.COL_MISSED_TASKS));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, cursor.getInt(DashboardFragment.COL_REMAINING_TASKS));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_STATUS, cursor.getString(DashboardFragment.COL_STATUS));
                return;
            }
        }
    }

    public ContentValues newGoal(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(GoalContract.GoalEntry.COLUMN_ID, "");
        contentValues.put(GoalContract.GoalEntry.COLUMN_TYPE, GoalContract.GoalEntry.GOAL);
        contentValues.put(GoalContract.GoalEntry.COLUMN_NAME, "");
        contentValues.put(GoalContract.GoalEntry.COLUMN_START_DATE, (double) GoalContract.normalizeDate(Calendar.getInstance().getTimeInMillis()));
        contentValues.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, 0);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASK, "");
        contentValues.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, 0);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, 0);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, 0);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, 0);
        contentValues.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, 0);
        contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.ACTIVE);
        return contentValues;
    }

    public void setGoalTasks(ArrayList<ContentValues> CVAL){
        int total=0;
        int done = 0;
        int missed = 0;
        int remaining = 0;
        for (int i =0; i<CVAL.size();i++){
            total += (int) CVAL.get(i).get(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS);
            done += (int) CVAL.get(i).get(GoalContract.GoalEntry.COLUMN_TASKS_DONE);
            missed += (int) CVAL.get(i).get(GoalContract.GoalEntry.COLUMN_TASKS_MISSED);
            remaining += (int) CVAL.get(i).get(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING);
        }
        GoalValue.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, total);
        GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, done);
        GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, missed);
        GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, remaining);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String sortOrder = GoalContract.GoalEntry.COLUMN_DUE_DATE + " ASC";
        String id = bundle.getString(TITLE_KEY);

        Uri goalUri = GoalContract.GoalEntry.GOAL_MILESTONE_URI;

        return new CursorLoader(getActivity(),
                goalUri,
                Goal_COLUMNS,
                null,
                new String[] {id},
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        getGoal(data);
        mGoalAdapter.swapCursor(data);
        mGoalAdapter.notifyDataSetChanged();
        mMilestoneAdapter.swapCursor(data);
        mMilestoneAdapter.notifyDataSetChanged();


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mMilestoneList) {
            mMilestoneList.clearOnScrollListeners();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mGoalAdapter.swapCursor(null);
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public EditText dateView;

        public DatePickerFragment(){
            super();
        }

        public DatePickerFragment(View v){
            super();
            dateView= (EditText) v;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);


            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (dateView != null) {
                Calendar c = Calendar.getInstance();
                c.set(year, month, day);
                String s = Utility.getDate(GoalContract.normalizeDate(c.getTimeInMillis()));
                dateView.setText(s);
            }

        }
    }


}

