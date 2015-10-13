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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.Calendar;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
    static final int COL_STATUS = 11;

    private GoalAdapter mGoalAdapter;
    private RecyclerView mMilestoneGraph;
    private RecyclerView mMilestoneList;
    private EditText titleView;
    private TextView duedateView;
    private Button addMilestoneButton;
    private Button saveButton;
    private Button cancelButton;

    private final String GOAL_KEY = "goal_key";
    private final String DATE_KEY = "date_key";
    private String titleString;
    private String dateString;
    private Boolean isNew;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(GOAL_KEY)) {

            titleString = args.getString(GOAL_KEY);
            dateString = getString(R.string.due_date);
            args.getDouble(DATE_KEY);
            isNew = false;

        } else {
            isNew = true;
            titleString = getString(R.string.title);
            dateString = getString(R.string.due_date);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        titleView = (EditText) rootView.findViewById(R.id.goal_title);
        duedateView = (TextView) rootView.findViewById(R.id.goal_date);
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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNew = false;
                setEditable(false);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditable(false);
                if (isNew){
                    //getActivity().getSupportFragmentManager().beginTransaction()
                    //        .replace(R.id.container, new DashboardFragment())
                    //        .commit();
                    ((DashBoardActivity) getActivity()).openDrawer();
                }
            }
        });

        addMilestoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDummyData();
            }
        });
        mMilestoneGraph.setAdapter(mGoalAdapter);
        setEditable(isNew);
        return rootView;
    }

    public void setEditable(Boolean editable) {
        if (editable) {
            cancelButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            addMilestoneButton.setVisibility(View.VISIBLE);
            if (duedateView.hasOnClickListeners()) {
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
        contentValues.put(GoalContract.GoalEntry.COLUMN_STATUS, GoalContract.GoalEntry.ACTIVE) ;
        getContext().getContentResolver().insert(GoalContract.GoalEntry.CONTENT_URI, contentValues);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (isNew) {
            return null;
        }
        String sortOrder = GoalContract.GoalEntry.COLUMN_DUE_DATE + " ASC";


        Uri goalUri = GoalContract.GoalEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                goalUri,
                Goal_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGoalAdapter.swapCursor(data);


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

        public TextView textView;

        public DatePickerFragment(){
            super();
        }

        public DatePickerFragment(View v){
            super();
            textView= (TextView) v;
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
            if (textView != null) {
                String s = month + "/" + day + "/" + year;
                textView.setText(s);
            }
        }
    }


}

