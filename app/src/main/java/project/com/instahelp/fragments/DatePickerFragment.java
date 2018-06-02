package project.com.instahelp.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

import project.com.instahelp.R;

public class DatePickerFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {


    private Button datePickerButton;

    private String year;
    private String month;
    private String day;
    private boolean currentDateSelected = false;
    private boolean dateValid = false;

    public void setArgument(View v){
        this.datePickerButton = (Button) v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.year = String.valueOf(year);
        this.month = new DateFormatSymbols().getMonths()[month];
        this.day = String.valueOf(day);

        if(dateIsValid(year, month, day)){
            datePickerButton.setText(String.valueOf(year));
            datePickerButton.append("/ ");
            datePickerButton.append(this.month);
            datePickerButton.append("/ ");
            datePickerButton.append(String.valueOf(day));
            dateValid = true;
        }else{
            String error_msg = getActivity().getResources().getString(R.string.add_post_date_error);
            datePickerButton.setError(error_msg);
            datePickerButton.setText(getResources().getString(R.string.add_post_date));
        }


    }

    public String getDate(){
        if(year == null) return null; // date is null if datepicker is clicked without choosing a date.
        return year + "/" + month + "/" + day;
    }
    public boolean isCurrentDateSelected(){
        return currentDateSelected;
    }

    public boolean isDateValid(){
        return dateValid;
    }

    private boolean dateIsValid(int year, int month, int day){
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        Date selectedDate = cal.getTime();
        if(selectedDate.equals(currentDate)) currentDateSelected = true;
        if(selectedDate.before(currentDate)) return false;
        return true;
    }
}
