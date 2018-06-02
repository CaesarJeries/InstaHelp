package project.com.instahelp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Status;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import project.com.instahelp.R;
import project.com.instahelp.utils.Defaults;
import project.com.instahelp.utils.ServerComm;
import project.com.instahelp.utils.User;
import utils.GoogleClient.GoogleConnection;
import utils.GoogleClient.State;


// facebook
// Google


public class LoginActivity extends AppCompatActivity implements
        Observer {


    @Override
    public void update(Observable observable, Object data) {
        if (observable != googleConnection) {
            return;
        }

        switch ((State) data) {
            case CREATED:
                dialog.dismiss();
                break;
            case OPENING:
                dialog.show();
                break;
            case OPENED:
                dialog.dismiss();

                break;
            case CLOSED:
                dialog.dismiss();
                break;
        }
    }
    private ServerComm comm = null;

    // google
    private GoogleConnection googleConnection;
    private AlertDialog dialog;
    private SignInButton googleButton;
    private static final int RC_SIGN_IN = 9001;


    // facebook
    private CallbackManager callbackManager;
    private LoginButton facebookButton;

    private User current_user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Login activity may be called from child activity (MainActivity), with
        // parameter "logout". This happens if the users pressed the logout button in the action bar.

        Bundle params = getIntent().getExtras();
        if(params != null){
            boolean logout_requested = params.getBoolean("logout", false);
            if(logout_requested){
                SharedPreferences prefs = getSharedPreferences("current_user", MODE_PRIVATE);
                String currentUserID = prefs.getString("current_id", null);
                Gson gson = new Gson();
                User u = gson.fromJson(prefs.getString(currentUserID, null),User.class);
                logout(u.getAccountType());
            }
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Defaults.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        googleConnection = GoogleConnection.getInstance(this, gso);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        ServerComm.init(this);
        callbackManager = CallbackManager.Factory.create();

        initUI();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.dismiss();

        try{
            comm = ServerComm.getInstance();
        }catch(ServerComm.AppNotInitializedException e){
            Log.d("Exception", e.getMessage());
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        // check if facebook user is still connected.
        if(accessToken != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleConnection.deleteObserver(this);
        googleConnection.disconnect();
    }

    public void onAuthenticated(User user){

        saveUserData(user);
        current_user = user;
        Log.d( "onAuthenticated", "Authentication successful" );
        String phoneNumber = current_user.getPhoneNumber();


        Intent intent;
        if(phoneNumber == null){
            intent = new Intent(this, RequestPhoneActivity.class);
        }else{
            intent = new Intent(this, MainActivity.class);
        }
        Bundle args = new Bundle();
        args.putString("user_id", current_user.getID());
        intent.putExtras(args);

        startActivity(intent);
        finish();
    }


    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ){
        super.onActivityResult( requestCode, resultCode, data );
        if(resultCode != RESULT_CANCELED) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
                return;
            }
            // forward result to facebook call back manager.
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        Toast
                .makeText(LoginActivity.this, "Login failed: Request cancelled", Toast.LENGTH_LONG)
                .show();
        hideProgressBar();

    }


    public void onConnectionFailed(ConnectionResult result){
        Toast.makeText(LoginActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
    }


    /**
     * Private helper methods
     */

    private void initUI(){
        googleButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        facebookButton = (LoginButton) findViewById(R.id.facebook_sign_in_button);

        hideProgressBar();

        googleButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view ){
                showProgressBar();
                googleSignIn();
            }
        });

        facebookButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showProgressBar();
                facebookSignIn();
            }
        });

        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>(){

            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                comm.authenticateSignIn(ServerComm.SignInMode.FACEBOOK, accessToken);

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "facebook login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                showLoginButtons();
                showNoInternetDialog();
            }
        });
    }


    private void googleSignIn(){
        googleConnection.connect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleConnection.getClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



    private class RequestToken extends AsyncTask<Void, Void, String>{

        private GoogleSignInResult result;
        private String token = null;
        private String errorMessage = null;

        RequestToken(GoogleSignInResult result){
            this.result = result;
        }


        @Override
        protected String doInBackground(Void... params) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String email = acct.getEmail();
            String scopes = "oauth2:profile email";
            try{
                token = GoogleAuthUtil.getToken(getApplicationContext(), email, scopes);
            }catch(GoogleAuthException | IOException e){
                Toast.makeText(LoginActivity.this, "GoogleAuthException : " +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (token != null) {
                /* Successfully got OAuth token, now login with Google */
                comm.addGoogleSignInResult(result);
                comm.authenticateSignIn(ServerComm.SignInMode.GOOGLE, token);
            } else if (errorMessage != null) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result){
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            new RequestToken(result).execute();

        } else {
            Status status = result.getStatus();
            Toast.makeText(LoginActivity.this, "Error:" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void facebookSignIn(){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    private void logout(User.AccountType mode){
        switch(mode){
            case Facebook:
                LoginManager.getInstance().logOut();
                break;
            case Google:
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Defaults.GOOGLE_CLIENT_ID)
                        .requestEmail()
                        .build();
                GoogleConnection.getInstance(this, gso).disconnect();

        }
    }

    private void saveUserData(User user){
        SharedPreferences pref = getSharedPreferences("current_user", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();

        editor.putString("current_id", user.getID());
        String firstLogin = pref.getString("firstLogin", null);
        if(firstLogin != null){
            if(firstLogin.equals("true")){
                editor.putString("firstLogin", "false");
            }
        }

        String userJson = pref.getString(user.getID(), null);
        if(userJson != null){
            User currentUser = gson.fromJson(userJson, User.class);
            if(currentUser != null){
                if(currentUser.getPhoneNumber() != null){
                    if(currentUser.getID().equals(user.getID())){
                        user.setPhoneNumber(currentUser.getPhoneNumber());
                    }
                }
            }
        }

        String json = gson.toJson(user);
        editor.putString(user.getID(), json);
        editor.apply();
    }

    private void showProgressBar(){
        hideLoginButtons();
        ProgressBar bar = (ProgressBar) findViewById(R.id.login_progress_bar);
        bar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        ProgressBar bar = (ProgressBar) findViewById(R.id.login_progress_bar);
        bar.setVisibility(View.INVISIBLE);
        showLoginButtons();
    }

    private void showLoginButtons(){
        facebookButton.setVisibility(View.VISIBLE);
        googleButton.setVisibility(View.VISIBLE);
    }

    private void hideLoginButtons(){
        facebookButton.setVisibility(View.INVISIBLE);
        googleButton.setVisibility(View.INVISIBLE);
    }

    private void showNoInternetDialog(){
        String noInternetAlertTitle = getResources().getString(R.string.no_internet_alert_title);
        String noInternetAlertContent = getResources().getString(R.string.no_internet_alert_content);


        new AlertDialog.Builder(getApplicationContext())
                .setTitle(noInternetAlertTitle)
                .setMessage(noInternetAlertContent)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

