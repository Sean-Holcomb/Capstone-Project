package com.seantholcomb.goalgetter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.seantholcomb.goalgetter.data.GoalContract;

/**
 * Created by seanholcomb on 10/11/15.
 */
public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoAdapterViewHolder> {

        private Cursor mCursor;
        final private Context mContext;
        final private TodoAdapterOnClickHandler mClickHandler;
        //final private ItemChoiceManager mICM;

        /**
         * Cache of the children views for a forecast list item.
         */
        public class TodoAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public final CheckBox mBox;
            public final TextView mTitleView;


            public TodoAdapterViewHolder(View view) {
                super(view);
                mTitleView = (TextView) view.findViewById(R.id.todo_title);
                mBox = (CheckBox) view.findViewById(R.id.check_box);
                mBox.setOnClickListener(this);
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

        public static interface TodoAdapterOnClickHandler {
            void onClick(String id, TodoAdapterViewHolder vh);
        }

        public TodoAdapter(Context context, TodoAdapterOnClickHandler dh) {
            mContext = context;
            mClickHandler = dh;
            //mEmptyView = emptyView;
            //mICM = new ItemChoiceManager(this);
            //mICM.setChoiceMode(choiceMode);
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
            if ( viewGroup instanceof RecyclerView ) {
                int layoutId = R.layout.item_todo;
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
                view.setFocusable(true);
                return new TodoAdapterViewHolder(view);
            } else {
                throw new RuntimeException("Not bound to RecyclerView");
            }
        }

        @Override
        public void onBindViewHolder(TodoAdapterViewHolder TodoAdapterViewHolder, int position) {
            mCursor.moveToPosition(position);

            String title = mCursor.getString(DashboardFragment.COL_TASK);
            TodoAdapterViewHolder.mTitleView.setText(title);

            //mICM.onBindViewHolder(GoalAdapterViewHolder, position);
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
        public int getItemCount() {
            if ( null == mCursor ) return 0;
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
            if ( viewHolder instanceof TodoAdapterViewHolder ) {
                TodoAdapterViewHolder vfh = (TodoAdapterViewHolder)viewHolder;
                vfh.onClick(vfh.itemView);
            }
        }


    }
