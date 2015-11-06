package com.seantholcomb.goalgetter;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Calendar;


//todo remove depricated methods
//todo setting returns properly
//todo clean up code
//todo make ui nicer
public class DashBoardActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        DashboardFragment.Callback, DetailFragment.Callback {
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
    private InterstitialAd mInterstitialAd;


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
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to start at midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        // set the alarm to repeat daily but not to go off until the device is woken up
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                alarmIntent);

        //todo fix interstitial ad
        //admob ad setup
        //mInterstitialAd = new InterstitialAd(this);
        //mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
/*
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mfragment  = new FocusTimerFragment();
                mTitle = getString(R.string.title_section3);
                menuLayout = R.menu.focus_timer;

            }
        });

        //requestNewInterstitial();
*/

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position){
            case 1:
                mTitle = getString(R.string.title_section2);
                menuLayout = R.menu.detail;
                mfragment = new DetailFragment();
                break;
            case 2:
                mfragment  = new FocusTimerFragment();
                mTitle = getString(R.string.title_section3);
                menuLayout = R.menu.focus_timer;
                //if (mInterstitialAd.isLoaded()) {
                //    mInterstitialAd.show();
                //}
                break;
            default:
                mTitle = getString(R.string.title_section1);
                menuLayout = R.menu.dash_board;
                mfragment= new DashboardFragment();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, mfragment)
                .addToBackStack(null)
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
                .addToBackStack(null)
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
                DialogFragment newFragment = new DialogFragment(){
                    @Override
                    public Dialog onCreateDialog (Bundle savedInstanceState){
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(inflater.inflate(R.layout.dialog_about_focus_timer, null));
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Nothing needed here
                            }
                        });

                        return builder.create();
                    }
                };

                newFragment.show(getSupportFragmentManager(), "aboutFocus");
            }
        }

        if (menuLayout == R.menu.detail){
            if (id == R.id.action_about_detail){
                DialogFragment newFragment = new DialogFragment(){
                    @Override
                    public Dialog onCreateDialog (Bundle savedInstanceState){
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
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDrawer(){
        if (mNavigationDrawerFragment != null){
            mNavigationDrawerFragment.openDrawer();
        }
    }

}


