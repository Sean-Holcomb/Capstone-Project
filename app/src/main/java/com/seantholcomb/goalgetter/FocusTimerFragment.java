package com.seantholcomb.goalgetter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//Todo stop alarm from going off when fragment opens
//Todo save choosen alarm and open to it
//Todo keep active timer through life cycle changes.
public class FocusTimerFragment extends Fragment {
    private EditText minuteInput;
    private Button restartButton;
    private Button stopButton;
    private Spinner alarmPicker;
    private TextView minuteOutput;
    private long[] vibratePatern = {500, 3000, 500, 3000};

    private final int MINS_TO_MILLIS= 60000;
    private int mMinutes = 0;
    private long mMillis = 0;
    private long mTimerCount = 0;


    private CountDownTimer countDownTimer;
    private Vibrator vibrator;
    private RingtoneManager ringtoneManager;
    private Cursor mCursor;
    private Ringtone mRingtone;


    public FocusTimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.focus_timer, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

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

            newFragment.show(getActivity().getSupportFragmentManager(), "aboutFocus");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(getString(R.string.title_section3));
        vibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        ringtoneManager = new RingtoneManager(getActivity());
        mCursor = ringtoneManager.getCursor();


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

        SimpleCursorAdapter sca=new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                mCursor,
                new String[]{mCursor.getColumnName(RingtoneManager.TITLE_COLUMN_INDEX)},
                new int[] {android.R.id.text1},
                0);
        sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmPicker.setAdapter(sca);
        alarmPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mRingtone = ringtoneManager.getRingtone(position);
                mRingtone.play();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRingtone.isPlaying()){
                    mRingtone.stop();
                }
                if (countDownTimer != null){
                    countDownTimer.cancel();
                    vibrator.cancel();
                    displayTime();
                }


            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.cancel();
                if (mRingtone.isPlaying()){
                    mRingtone.stop();
                }

                if (mMinutes !=0) {
                    countDownTimer = new CountDownTimer(mMillis, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            mTimerCount = mMillis - millisUntilFinished;

                        }

                        @Override
                        public void onFinish() {
                            mTimerCount = mMillis;
                            displayTime();
                            mRingtone.play();
                            vibrator.vibrate(vibratePatern, 3);
                        }
                    };
                    countDownTimer.start();
                    Toast.makeText(getActivity(), getString(R.string.start_toast), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), getString(R.string.minutes_toast), Toast.LENGTH_SHORT).show();
                }

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
                        mMinutes = minutes;
                        mMillis = mMinutes * MINS_TO_MILLIS;
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


    private void displayTime(){
        int secs = (int) (mTimerCount / 1000);
        int mins = secs / 60;
        int hours = mins /60;
        secs = secs % 60;
        mins = mins%60;

        if (hours == 0){
            minuteOutput.setText("" + mins + ":"
                    + String.format("%02d", secs));
        }else{

            minuteOutput.setText("" + hours + ":"
                    + String.format("%02d", mins) + ":"
                    + String.format("%02d", secs));
        }
    }



}
