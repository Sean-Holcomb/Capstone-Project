package com.seantholcomb.goalgetter;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seantholcomb.goalgetter.data.GoalContract;

/**
 * Fragment for displaying information about the user's current goals
 * displays three recycler views. One with a to-do list of tasks to do that day
 * and two displaying current and past goals progress using GoalAdapters
 */
public class DashboardFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private GoalAdapter mGoalAdapter;
    private TodoAdapter mTodoAdapter;
    private GoalAdapter mPastAdapter;
    private RecyclerView mCurrentList;
    private RecyclerView mPastList;
    private RecyclerView mTodoList;

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
    static final int COL_REMAINING_TASKS= 11;
    static final int COL_STATUS = 12;

    final int CURRENT_LOADER = 0;
    final int PAST_LOADER = 1;
    final int TODO_LOADER = 2;

    //Interface for when a goalAdapter item is selected
    public interface Callback {
        void onItemSelected(GoalAdapter.GoalAdapterViewHolder vh);
    }


    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * called when fragment is created
     * starts three loader to get content for recycler views
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_section1));
        getLoaderManager().initLoader(CURRENT_LOADER, null, this);
        getLoaderManager().initLoader(PAST_LOADER, null, this);
        getLoaderManager().initLoader(TODO_LOADER, null, this);
    }
    /**
     * Initialize and set listeners and adapters for views
     */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dash_board, container, false);
        mCurrentList = (RecyclerView) rootView.findViewById(R.id.current_list);
        mTodoList = (RecyclerView) rootView.findViewById(R.id.todo_list);
        mPastList = (RecyclerView) rootView.findViewById(R.id.past_list);
        mCurrentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPastList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTodoList.setLayoutManager(new LinearLayoutManager(getActivity()));

        //selecting items opens detail fragment
        mGoalAdapter = new GoalAdapter(getActivity(), new GoalAdapter.GoalAdapterOnClickHandler() {
            @Override
            public void onClick(String id, GoalAdapter.GoalAdapterViewHolder vh) {
                ((Callback) getActivity()).onItemSelected(vh);
            }
        });

        //selecting items opens detail fragment
        mPastAdapter = new GoalAdapter(getActivity(), new GoalAdapter.GoalAdapterOnClickHandler() {
            @Override
            public void onClick(String id, GoalAdapter.GoalAdapterViewHolder vh) {
                ((Callback) getActivity()).onItemSelected(vh);
            }
        });

        mTodoAdapter = new TodoAdapter(getActivity());
        mCurrentList.setAdapter(mGoalAdapter);
        mPastList.setAdapter(mPastAdapter);
        mTodoList.setAdapter(mTodoAdapter);

        return rootView;
    }


    /**
     * Chooses correct uri and starts loader
     * @param id selects which kind of cursor will be returned should be: CURRENT_LOADER, PAST_LOADER or TODO_LOADER
     * @param bundle
     * @return returns cursor loader with proper URI for the content provider
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String sortOrder = GoalContract.GoalEntry.COLUMN_DUE_DATE + " ASC";
        Uri goalUri;
        switch (id) {
            case CURRENT_LOADER:
                goalUri = GoalContract.GoalEntry.CURRENT_URI;
                break;
            case PAST_LOADER:
                goalUri = GoalContract.GoalEntry.PAST_URI;
                break;
            case TODO_LOADER:
                goalUri = GoalContract.GoalEntry.TODO_URI;
                break;
            default:
                goalUri = GoalContract.GoalEntry.GOAL_URI;
        }


        return new CursorLoader(getActivity(),
                goalUri,
                Goal_COLUMNS,
                null,
                null,
                sortOrder);
    }

    /**
     * applies cursor to the appropriate adapter
     * @param loader of the cursor
     * @param data the data to be displayed by the adapter
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case CURRENT_LOADER:
                mGoalAdapter.swapCursor(data);
                mGoalAdapter.notifyDataSetChanged();
                break;
            case PAST_LOADER:
                mPastAdapter.swapCursor(data);
                mPastAdapter.notifyDataSetChanged();
                break;
            case TODO_LOADER:
                mTodoAdapter.swapCursor(data);
                mTodoAdapter.notifyDataSetChanged();
                break;
        }

    }

    /**
     * remove on scroll listeners from recyclerviews
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCurrentList) {
            mCurrentList.clearOnScrollListeners();
            mPastList.clearOnScrollListeners();
            mTodoList.clearOnScrollListeners();
        }
    }

    //required method for loader call backs
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mGoalAdapter.swapCursor(null);
    }


}
