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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seantholcomb.goalgetter.data.GoalContract;


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

    public interface Callback {
        public void onItemSelected(Uri GoalUri, GoalAdapter.GoalAdapterViewHolder vh);
    }


    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dash_board, container, false);
        mCurrentList = (RecyclerView) rootView.findViewById(R.id.current_list);
        mTodoList = (RecyclerView) rootView.findViewById(R.id.todo_list);
        mPastList = (RecyclerView) rootView.findViewById(R.id.past_list);
        mCurrentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPastList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTodoList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mGoalAdapter = new GoalAdapter(getActivity(), new GoalAdapter.GoalAdapterOnClickHandler() {
            @Override
            public void onClick(String id, GoalAdapter.GoalAdapterViewHolder vh) {
                ((Callback) getActivity())
                        .onItemSelected(GoalContract.GoalEntry.CONTENT_URI,
                                vh
                        );
            }
        });

        mPastAdapter = new GoalAdapter(getActivity(), new GoalAdapter.GoalAdapterOnClickHandler() {
            @Override
            public void onClick(String id, GoalAdapter.GoalAdapterViewHolder vh) {
                ((Callback) getActivity())
                        .onItemSelected(GoalContract.GoalEntry.CONTENT_URI,
                                vh
                        );
            }
        });

        mTodoAdapter = new TodoAdapter(getActivity(), new TodoAdapter.TodoAdapterOnClickHandler() {
            @Override
            public void onClick(String id, TodoAdapter.TodoAdapterViewHolder vh) {

            }
        });
        mCurrentList.setAdapter(mGoalAdapter);
        mPastList.setAdapter(mPastAdapter);
        mTodoList.setAdapter(mTodoAdapter);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

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
        mGoalAdapter.notifyDataSetChanged();
        mCurrentList.setAdapter(mGoalAdapter);
        Log.e("EEEEEE", "MMMMMMMM");

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCurrentList) {
            mCurrentList.clearOnScrollListeners();
            mPastList.clearOnScrollListeners();
            mTodoList.clearOnScrollListeners();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mGoalAdapter.swapCursor(null);
    }



}
