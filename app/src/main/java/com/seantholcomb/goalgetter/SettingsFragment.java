package com.seantholcomb.goalgetter;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * fragment for setting notification settings
 * Created by seanholcomb on 11/5/15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.action_settings));
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {
            getView().setClickable(true);
            getView().setBackgroundColor(Color.WHITE);
        }
    }
}