package com.seantholcomb.goalgetter;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FocusTimerFragment extends Fragment {
    private EditText minuteInput;
    private Button restartButton;
    private Button stopButton;
    private Spinner alarmPicker;
    private int mMinutes = 0;
    private TextView minuteOutput;

    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;


    public FocusTimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_focus_timer, container, false);
        minuteInput = (EditText) rootView.findViewById(R.id.minuteInput);
        restartButton = (Button) rootView.findViewById(R.id.restart);
        stopButton = (Button) rootView.findViewById(R.id.stop);
        alarmPicker = (Spinner) rootView.findViewById(R.id.alarmSelector);
        minuteOutput = (TextView) rootView.findViewById(R.id.minuteOutput);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        minuteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    int minutes = Integer.parseInt(s.toString());
                    if (minutes >= 0 && minutes <= 90) {
                        mMinutes = mMinutes;
                    } else {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.timer_toast), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return rootView;
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;

            secs = secs % 60;

            int milliseconds = (int) (updatedTime % 1000);

            minuteOutput.setText("" + mins + ":"
                            + String.format("%02d", secs) + ":"
                            + String.format("%03d", milliseconds));

            customHandler.postDelayed(this, 0);

        }

    };

}
