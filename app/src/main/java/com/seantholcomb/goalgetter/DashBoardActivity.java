package com.seantholcomb.goalgetter;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;


//todo move settings and titles to fragments
//Todo softkeyboard closes when not in use
//todo make ui nicer
//todo widget ui too
//todo add content descriptions
//todo publish app
//todo add fade transitions

public class DashBoardActivity extends AppCompatActivity
        implements DashboardFragment.Callback, DetailFragment.Callback,NavigationView.OnNavigationItemSelectedListener  {


    private CharSequence mTitle;
    private int menuLayout;
    private Fragment mfragment;
    SettingsFragment mSettingsFragment;
    private final String TITLE_KEY= "title";
    private final String DUE_DATE_KEY = "due_date";
    private static final String NAV_ITEM_ID = "navItemId";
    private static final String MENU_ID = "menuId";
    private static final long DRAWER_CLOSE_DELAY_MS = 250;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    //private InterstitialAd mInterstitialAd;

    private final Handler mDrawerActionHandler = new Handler();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mNavItemId;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        mTitle = getTitle();

        mSettingsFragment = new SettingsFragment();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);

        if (null == savedInstanceState) {
            mNavItemId = R.id.drawer_item_dashboard;
            menuLayout=R.menu.dash_board;
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
            menuLayout = savedInstanceState.getInt(MENU_ID);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().findItem(mNavItemId).setChecked(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (savedInstanceState == null)
        navigate(mNavItemId);


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

    private void navigate(final int itemId) {

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        getFragmentManager().beginTransaction().remove(mSettingsFragment).commit();
        switch (itemId){


            case R.id.drawer_item_detail:
                mTitle = getString(R.string.title_section2);
                menuLayout = R.menu.detail;
                mfragment = new DetailFragment();
                break;
            case R.id.drawer_item_focus:
                mfragment  = new FocusTimerFragment();
                mTitle = getString(R.string.title_section3);
                menuLayout = R.menu.focus_timer;
                //if (mInterstitialAd.isLoaded()) {
                //    mInterstitialAd.show();
                //}
                break;
            case R.id.drawer_item_settings:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, mSettingsFragment)
                        .commit();

                return;
            default:
                mTitle = getString(R.string.title_section1);
                menuLayout = R.menu.dash_board;
                mfragment= new DashboardFragment();

        }
        toolbar.setTitle(mTitle);
        invalidateOptionsMenu();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mfragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // update highlighted item in the navigation menu
        menuItem.setChecked(true);
        mNavItemId = menuItem.getItemId();

        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(menuItem.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }


/*
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }*/



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
        mNavItemId = R.id.drawer_item_detail;
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


    public void restoretoolar() {
        //Toolbar toolbar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //toolbar.setDisplayShowTitleEnabled(true);
        //toolbar.setTitle(mTitle);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menuLayout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.support.v7.appcompat.R.id.home) {
            toolbar.setTitle(R.string.app_name);
            return mDrawerToggle.onOptionsItemSelected(item);
        }
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


        return super.onOptionsItemSelected(item);
    }

    public void openDrawer(){
        if (mDrawerLayout != null){
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            invalidateOptionsMenu();
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
        outState.putInt(MENU_ID, menuLayout);
    }

}


