package project.com.instahelp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import project.com.instahelp.R;
import project.com.instahelp.fragments.DatePickerFragment;
import project.com.instahelp.fragments.TimePickerFragment;
import project.com.instahelp.utils.Defaults;
import project.com.instahelp.utils.Post;



public class AddPost extends AppCompatActivity {

    private String userId;
    private String userName;
    private String phoneNumber;
    private EditText titleEditText;
    private EditText contentEditText;

    // Pickers
    TimePickerFragment timePickerFragment;
    DatePickerFragment datePickerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if(bar != null){
            bar.setDisplayHomeAsUpEnabled(true);
        }
        initUserAttributes();

        initUI();
        titleEditText = (EditText) findViewById(R.id.post_title);
        contentEditText = (EditText) findViewById(R.id.post_content);
    }


    private void initUserAttributes(){
        Bundle params = getIntent().getExtras();

        userId = params.getString(Defaults.PreferencesTags.USER_ID);
        userName = params.getString(Defaults.PreferencesTags.USER_NAME);
        phoneNumber = params.getString(Defaults.PreferencesTags.PHONE_NUMBER);
    }

    private void initUI() {
        Button postButton = (Button) findViewById(R.id.add_post_button);

        if(postButton != null){
            postButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(validatePost() ){
                        Post post = getPostFromLayout();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("post", post);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }
            });
        }
    }


    public void showTimePicker(View v){
        timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArgument(v);
        timePickerFragment.setArgument(this);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePicker(View v){
        datePickerFragment = new DatePickerFragment();
        datePickerFragment.setArgument(v);
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }


    private Post getPostFromLayout(){
        CheckBox phonePrivacy = (CheckBox) findViewById(R.id.addPost_phone_checkbox);
        CheckBox locationPrivacy = (CheckBox) findViewById(R.id.addPost_location_checkbox);

        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();

        boolean isLocationPrivate = !locationPrivacy.isChecked();
        boolean isPhonePrivate = !phonePrivacy.isChecked();

        String time;
        if(timePickerFragment != null) {
            time = Post.parseTime(timePickerFragment);
            if(time == null){
                time = getCurrentTime();
            }
        }else{
            time = getCurrentTime();
        }
        String date;
        if(datePickerFragment != null){
            date = Post.parseDate(datePickerFragment);
            if(date == null){
                date = getCurrentDate();
            }
        }else{
            date = getCurrentDate();
        }



        return new Post(userId, title, userName, content, phoneNumber,
                isLocationPrivate, isPhonePrivate, date, time);
    }

    private String getCurrentDate(){
        Calendar cal = Calendar.getInstance();
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)];
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        return year + "/" + month + "/" + day;
    }

    private String getCurrentTime(){
        Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MONTH);
        return pad(currentHour) + ":" + pad(currentMinute);
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    private boolean validatePost(){
        boolean retval = true;

        if(titleEditText.getText().toString().isEmpty()){
            String msg = getResources().getString(R.string.add_post_title_error);
            titleEditText.setError(msg);
            retval = false;
        }

        if(contentEditText.getText().toString().isEmpty()){
            String msg = getResources().getString(R.string.add_post_content_error);
            contentEditText.setError(msg);
            retval = false;
        }

        if(timePickerFragment != null){
            int selectedHour = timePickerFragment.getHour();
            int selectedMinute = timePickerFragment.getMinute();

            if(validSelection(selectedHour, selectedMinute)){
                timePickerFragment.showSelectedTime();
            }else{
                if(datePickerFragment != null){
                    if(datePickerFragment.isCurrentDateSelected()){
                        if(validSelection(selectedHour, selectedMinute)){
                            timePickerFragment.showSelectedTime();
                        }else{
                            timePickerFragment.showError();
                            retval = false;
                        }
                    }else{
                        if(datePickerFragment.isDateValid()){
                            timePickerFragment.showSelectedTime();
                        }else{
                            // do nothing
                        }
                    }
                }else {
                    timePickerFragment.showError();
                    retval = false;
                }
            }
        }

        return retval;
    }

    private boolean validSelection(int selectedHour, int selectedMinute){
        Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MONTH);
        if(selectedHour < currentHour) return false;
        if(selectedHour > currentHour) return true;
        if(selectedMinute < currentMinute) return false;

        return true;
    }



}
