package project.com.instahelp.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

import project.com.instahelp.R;

public class TimePickerFragment extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener{

    private Button timePickerButton;
    private Activity activity;
    private String time;
    private int hour;
    private int minute;


    public void setArgument(View v){
        this.timePickerButton = (Button) v;
    }

    public void setArgument(Activity activity){
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {

        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        time = pad(hourOfDay) + ":" + pad(minute);
        this.hour = hourOfDay;
        this.minute = minute;
        timePickerButton.setText(time);
    }

    public String getTime(){
        return time;
    }

    public int getHour(){
        return hour;
    }

    public int getMinute(){
        return minute;
    }

    public void showSelectedTime(){
        timePickerButton.setText(time);
        timePickerButton.setError(null);
    }

    public void showError(){
        String error_message = activity.getResources().getString(R.string.add_post_time_error);
        timePickerButton.setError(error_message);
        String button_label = activity.getResources().getString(R.string.add_post_time);
        timePickerButton.setText(button_label);
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
