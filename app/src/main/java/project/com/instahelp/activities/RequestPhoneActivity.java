package project.com.instahelp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import project.com.instahelp.R;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;


public class RequestPhoneActivity extends AppCompatActivity {

    private Button nextButton;
    private EditText phoneNumber;
    private String user_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle params = getIntent().getExtras();

        user_id = params.getString("user_id");

        setContentView(R.layout.activity_request_phone);

        initUI();
    }

    private void initUI(){
        nextButton = (Button) findViewById(R.id.next_button);
        phoneNumber = (EditText) findViewById(R.id.phone_number);


        /**
         * Set listener for the next button
         */
        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                /**
                 * Add phone number to database.
                 */
                ServerComm comm = null;
                try{
                    comm = ServerComm.getInstance();
                }catch (ServerComm.AppNotInitializedException ignored){}

                SharedPreferences pref = getSharedPreferences("current_user", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                Gson gson = new Gson();
                String currentID = pref.getString("current_id", null);
                User user = gson.fromJson(pref.getString(currentID, null), User.class);
                user.setPhoneNumber(phoneNumber.getText().toString());
                String json = gson.toJson(user);
                editor.putString(user.getID(), json);
                editor.putString("firstLogin", "true");
                editor.apply();
                comm.updatePhoneNumber(user_id, phoneNumber.getText().toString());

                startActivity( new Intent( getApplicationContext(), MainActivity.class ) );
                finish();
            }
        });


        /**
         * Set listeners for phone number edit text
         */

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phone_number = s.toString();
                boolean is_valid = checkValidity(phone_number);
                if(is_valid){
                    nextButton.setEnabled(true);
                }
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancel_button);

        if(cancelButton != null){
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity( new Intent( getApplicationContext(), LoginActivity.class ) );
                    finish();
                }
            });
        }


    }

    private boolean checkValidity(String phone_number){
        if(!isNumeral(phone_number)){
            phoneNumber.setError("Phone number must only contain digits");
            return false;
        }
        if(phone_number.length() != 10){
            phoneNumber.setError("Phone number must be 10 digits");
        }
        return true;
    }

    private boolean isNumeral(String str){
        char[] char_arr = str.toCharArray();
        for(char c : char_arr){
            if(!Character.isDigit(c)) return false;
        }
        return true;
    }
}
