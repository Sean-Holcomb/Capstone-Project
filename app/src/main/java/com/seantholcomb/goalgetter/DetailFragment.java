package com.seantholcomb.goalgetter;

import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.seantholcomb.goalgetter.data.GoalContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//todo add loading spinner for saving
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
    static final int COL_REMAINING_TASKS = 11;
    static final int COL_STATUS = 12;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;

    private GoalAdapter mGoalAdapter;
    private MilestoneAdapter mMilestoneAdapter;
    private RecyclerView mMilestoneGraph;
    private RecyclerView mMilestoneList;
    private EditText titleView;
    private EditText duedateView;
    private Button addMilestoneButton;
    private Button saveButton;
    private Button cancelButton;
    private CheckBox addCalendar;
    private ContentValues GoalValue;
    private final String TITLE_KEY = "title";
    private final String DATE_KEY = "due_date";
    private String titleString;
    private String dateString;
    private boolean isNew;
    private boolean hasCalendar = false;
    private boolean calendarActive = false;

    private GoogleAccountCredential mCredential;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    public DetailFragment() {
        // Required empty public constructor
    }

    public interface Callback {
        public void onSave(Bundle args);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about_detail) {
            DialogFragment newFragment = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setView(inflater.inflate(R.layout.dialog_about_detail, null));
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Nothing needed here
                        }
                    });

                    return builder.create();
                }
            };
            newFragment.show(getActivity().getSupportFragmentManager(), "aboutDetail");
            return true;
        }
        if (id == R.id.action_edit) {
            setEditable(true);
            return true;
        }

        if (id == R.id.action_delete) {
            deleteGoal();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_section2));
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        if (args != null && args.containsKey(TITLE_KEY)) {
            isNew = false;
            titleString = args.getString(TITLE_KEY);
            double dateDouble = args.getDouble(DATE_KEY);
            dateString = Utility.getDate((long) dateDouble);
            GoalValue = newGoal();
            getLoaderManager().initLoader(0, args, this);
            hasCalendar = settings.contains(Utility.makeValidId(titleString));
            calendarActive = hasCalendar;
        } else {
            isNew = true;
            titleString = getString(R.string.title);
            dateString = getString(R.string.due_date);
            GoalValue = newGoal();
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
        addCalendar = (CheckBox) rootView.findViewById(R.id.add_calendar);
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
        if (!isNew) {
            mMilestoneAdapter.setmID(titleString);
            mMilestoneAdapter.setDueDate(Utility.getDateDouble(dateString));
        }
        addCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
                    if (!settings.contains(PREF_ACCOUNT_NAME)) {
                        chooseAccount();
                    }
                }
                calendarActive=isChecked;
            }
        });
        addCalendar.setChecked(calendarActive);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ContentValues> CVAL = mMilestoneAdapter.arrangeForSave();
                setGoalTasks(CVAL);
                CVAL.add(0, GoalValue);
                if (calendarActive) {
                    addEvents(CVAL);
                } else if (hasCalendar) {
                    new MakeRequestTask(mCredential, CVAL).execute();
                } else {
                    onSaveButton(CVAL);
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditable(false);
                mMilestoneAdapter.cancel();
                titleView.setText(titleString);
                duedateView.setText(dateString);
                if (isNew) {
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
                if (duedate >= GoalValue.getAsDouble(GoalContract.GoalEntry.COLUMN_START_DATE)) {
                    mMilestoneAdapter.setDueDate(duedate);
                    GoalValue.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, duedate);
                } else if (duedate != -1.0) {
                    Toast.makeText(getActivity(), getString(R.string.date_prompt), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rootView.findViewById(R.id.linear_detail).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });


        mMilestoneGraph.setAdapter(mGoalAdapter);
        mMilestoneList.setAdapter(mMilestoneAdapter);
        setEditable(isNew);
        return rootView;
    }

    public void onSaveButton(ArrayList<ContentValues> CVAL) {
        isNew = false;
        getContext().getContentResolver().delete(GoalContract.GoalEntry.GOAL_URI, sDelete, new String[]{titleString});
        titleString = GoalValue.getAsString(GoalContract.GoalEntry.COLUMN_ID);
        double date = GoalValue.getAsDouble(GoalContract.GoalEntry.COLUMN_DUE_DATE);
        dateString = Utility.getDate((long) date);
        ContentValues[] CVArray = CVAL.toArray(new ContentValues[CVAL.size()]);
        getContext().getContentResolver().bulkInsert(GoalContract.GoalEntry.GOAL_URI, CVArray);
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, titleString);
        args.putDouble(DATE_KEY, date);
        ((Callback) getActivity()).onSave(args);
    }

    public void setEditable(Boolean editable) {
        if (editable) {
            cancelButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            addMilestoneButton.setVisibility(View.VISIBLE);
            addCalendar.setVisibility(View.VISIBLE);
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
            addCalendar.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            addMilestoneButton.setVisibility(View.GONE);
            duedateView.setOnClickListener(null);
        }
        titleView.setEnabled(editable);
        mMilestoneAdapter.makeEditable(editable);


    }

    public void deleteGoal() {
        if (hasCalendar){
           new MakeRequestTask(mCredential).execute();
        }

        getContext().getContentResolver().delete(GoalContract.GoalEntry.GOAL_URI, sDelete, new String[]{titleString});
        Bundle args = new Bundle();
        ((Callback) getActivity()).onSave(args);
    }


    public void getGoal(Cursor cursor) {
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            if (cursor.getString(COL_TYPE).equals(GoalContract.GoalEntry.GOAL)) {
                GoalValue.put(GoalContract.GoalEntry.COLUMN_ID, cursor.getString(COL_GOAL_ID));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TYPE, cursor.getString(COL_TYPE));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_NAME, cursor.getString(COL_NAME));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_START_DATE, cursor.getDouble(COL_START_DATE));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_DUE_DATE, cursor.getDouble(COL_DUE_DATE));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASK, cursor.getString(COL_TASK));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_FREQUENCY, cursor.getInt(COL_FREQUENCY));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TOTAL_TASKS, cursor.getInt(COL_TOTAL_TASKS));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_DONE, cursor.getInt(COL_DONE_TASK));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_MISSED, cursor.getInt(COL_MISSED_TASKS));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_TASKS_REMAINING, cursor.getInt(COL_REMAINING_TASKS));
                GoalValue.put(GoalContract.GoalEntry.COLUMN_STATUS, cursor.getString(COL_STATUS));
                return;
            }
        }
    }

    public ContentValues newGoal() {
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

    public void setGoalTasks(ArrayList<ContentValues> CVAL) {
        int total = 0;
        int done = 0;
        int missed = 0;
        int remaining = 0;
        for (int i = 0; i < CVAL.size(); i++) {
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

    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
                        mCredential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void addEvents(ArrayList<ContentValues> CVAL) {
        String baseId = "goalgetterevent";
        ArrayList<Event> events = new ArrayList<>();

        for (int i = 0; i < CVAL.size(); i++) {
            String summary = CVAL.get(i).getAsString(GoalContract.GoalEntry.COLUMN_NAME);
            long dueDate = CVAL.get(i).getAsLong(GoalContract.GoalEntry.COLUMN_DUE_DATE);


            DateTime dateTime = new DateTime(new Date(dueDate));
            EventDateTime start = new EventDateTime();
            DateTime endTime = new DateTime(new Date(dueDate + 24 * 60 * 60 * 1000));
            EventDateTime end = new EventDateTime();
            end.setDateTime(endTime);
            start.setDateTime(dateTime);
            //todo make events nicer ie. all day, availible, no notifications
            Event event = new Event()
                    .setSummary(summary)
                    .setStart(start)
                    .setEnd(end);

            events.add(event);
        }
        MakeRequestTask task = new MakeRequestTask(mCredential, events, CVAL);
        task.execute();

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
                new String[]{id},
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

        public DatePickerFragment() {
            super();
        }

        public DatePickerFragment(View v) {
            super();
            dateView = (EditText) v;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            if(dateView!=null){
                double setDate = Utility.getDateDouble(dateView.getText().toString());
                if (setDate != 0){
                    c.setTimeInMillis((long) setDate);
                }
            }
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

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private ArrayList<Event> mEvents = null;
        private Exception mLastError = null;
        private ArrayList<ContentValues> mCVAL;
        private String mSummary;
        private String mId;
        private String oldId;
        private String mCalendarId = "";
        private boolean delete;


        public MakeRequestTask(GoogleAccountCredential credential, ArrayList<Event> events, ArrayList<ContentValues> CVAL) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mEvents = events;
            mCVAL = CVAL;
            mSummary = CVAL.get(0).getAsString(GoalContract.GoalEntry.COLUMN_ID);
            mId = Utility.makeValidId(mSummary);
            oldId = Utility.makeValidId(titleString);
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Goal Getter")
                    .build();
            delete=false;
        }
        public MakeRequestTask(GoogleAccountCredential credential){
            oldId=Utility.makeValidId(titleString);
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Goal Getter")
                    .build();
            delete=true;
        }

        public MakeRequestTask(GoogleAccountCredential credential, ArrayList<ContentValues> CVAL){
            oldId=Utility.makeValidId(titleString);
            mCVAL = CVAL;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Goal Getter")
                    .build();
            delete=true;
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            com.google.api.services.calendar.model.Calendar calendar;
            SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            try {
                if (settings.contains(oldId)) {
                    mCalendarId = settings.getString(oldId, "");
                    editor.remove(oldId).apply();
                    CalendarList calendarList = mService.calendarList().list().execute();
                    int length = calendarList.getItems().size();
                    for (int i = 0; i < length; i++) {
                        String tmpId = calendarList.getItems().get(i).getId();
                        if (mCalendarId.equals(tmpId)) {
                            mService.calendars().delete(mCalendarId).execute();
                            break;
                        }
                    }
                }
                if(!delete) {
                    calendar = makeNewCalendar();
                    mCalendarId = calendar.getId();
                    editor.putString(mId, mCalendarId).apply();


                    for (Event event : mEvents) {
                        mService.events().insert(mCalendarId, event).execute();

                    }
                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);

            }

            return null;
        }

        private com.google.api.services.calendar.model.Calendar makeNewCalendar() {
            com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
            calendar.setSummary(mSummary);
            try {
                calendar = mService.calendars().insert(calendar).execute();
            } catch (Exception e) {
            }
            return calendar;
        }
        @Override
        protected void onPostExecute(List<String> output) {
            if (mCVAL != null) {
                onSaveButton(mCVAL);
            }
        }

        @Override
        protected void onCancelled() {

            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);

                }
            }
        }

    }


}

