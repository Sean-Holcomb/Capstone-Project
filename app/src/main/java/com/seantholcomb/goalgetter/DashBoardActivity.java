package com.seantholcomb.goalgetter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;


public class DashBoardActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        DashboardFragment.Callback, DetailFragment.Callback {
    //TODO write method to update Goal tasks
    //TODo write method to clean up stray milestones
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int menuLayout;
    private Fragment mfragment;
    private final String TITLE_KEY= "title";
    private final String DUE_DATE_KEY = "due_date";

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        menuLayout=R.menu.dash_board;
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //Set Alarm for database incrementation and notifications
        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, GoalAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to start at midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        // set the alarm to repeat daily but not to go off until the device is woken up
        alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        switch (position){
            case 1:
                mTitle = getString(R.string.title_section2);
                menuLayout = R.menu.detail;
                fragment = new DetailFragment();
                break;
            case 2:
                fragment  = new FocusTimerFragment();
                mTitle = getString(R.string.title_section3);
                menuLayout = R.menu.focus_timer;

                break;
            default:
                mTitle = getString(R.string.title_section1);
                menuLayout = R.menu.dash_board;
                fragment= new DashboardFragment();
        }
        mfragment=fragment;
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onItemSelected(Uri uri, GoalAdapter.GoalAdapterViewHolder vh){
        Bundle args = new Bundle();
        if (vh != null) {
            String title = String.valueOf(vh.mTitleView.getText());
            double date =vh.due_date;
            if (title != null) {
                args.putString(TITLE_KEY, title);
                args.putDouble(DUE_DATE_KEY, date);
            }
        }
        Fragment fragment = new DetailFragment();
        fragment.setArguments(args);
        mfragment=fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        menuLayout= R.menu.detail;
        invalidateOptionsMenu();
    }

    @Override
    public void onSave(Bundle args){
        Fragment fragment = new DetailFragment();
        fragment.setArguments(args);
        mfragment=fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        menuLayout= R.menu.detail;
        invalidateOptionsMenu();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(menuLayout, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (menuLayout == R.menu.focus_timer){
            if (id == R.id.action_about){
                DialogFragment newFragment = new DialogFragment();
                newFragment.show(getSupportFragmentManager(), "aboutFocus");
            }
        }

        if (menuLayout == R.menu.detail){
            if (id == R.id.action_about_detail){
                DialogFragment newFragment = new DialogFragment();
                newFragment.show(getSupportFragmentManager(), "aboutDetail");
            }
            if (id == R.id.action_edit){
                ((DetailFragment) mfragment).setEditable(true);
            }

            if (id == R.id.action_delete){
                ((DetailFragment) mfragment).deleteGoal();
            }

        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDrawer(){
        if (mNavigationDrawerFragment != null){
            mNavigationDrawerFragment.openDrawer();
        }
    }

}


