package utils.GoogleClient;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;

import java.lang.ref.WeakReference;
import java.util.Observable;

public class GoogleConnection extends Observable
        implements ConnectionCallbacks, OnConnectionFailedListener {

    public static final int REQUEST_CODE = 1234;

    public void connect() {
        currentState.connect(this);
    }

    public void disconnect() {
        currentState.disconnect(this);
    }

    public void revokeAccessAndDisconnect() {
        currentState.revokeAccessAndDisconnect(this);
    }

    public static GoogleConnection getInstance(Activity activity, GoogleSignInOptions gso) {
        if (null == sGoogleConnection) {
            sGoogleConnection = new GoogleConnection(activity, gso);
        }

        return sGoogleConnection;
    }

    @Override
    public void onConnected(Bundle hint) {
        changeState(State.OPENED);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        changeState(State.CLOSED);
        connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (currentState.equals(State.CLOSED) && connectionResult.hasResolution()) {
            changeState(State.CREATED);
            this.connectionResult = connectionResult;
        } else {
            connect();
        }
    }

    public GoogleApiClient getClient(){
        return googleApiClient;
    }

    public void onActivityResult(int result) {
        if (result == Activity.RESULT_OK) {
            // If the error resolution was successful we should continue
            // processing errors.
            changeState(State.CREATED);
        } else {
            // If the error resolution was not successful or the user canceled,
            // we should stop processing errors.
            changeState(State.CLOSED);
        }

        // If Google Play services resolved the issue with a dialog then
        // onStart is not called so we need to re-attempt connection here.
        onSignIn();
    }


    protected void onSignIn() {
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    protected void onSignOut() {
        if (googleApiClient.isConnected()) {
            // We clear the default account on sign out so that Google Play
            // services will not return an onConnected callback without user
            // interaction.
            Auth.GoogleSignInApi.signOut(googleApiClient);
            googleApiClient.disconnect();
            //googleApiClient.connect();
            changeState(State.CLOSED);
        }
    }

    protected void onSignUp() {
        // We have an intent which will allow our user to sign in or
        // resolve an error.  For example if the user needs to
        // select an account to sign in with, or if they need to consent
        // to the permissions your app is requesting.

        try {
            // Send the pending intent that we stored on the most recent
            // OnConnectionFailed callback.  This will allow the user to
            // resolve the error currently preventing our connection to
            // Google Play services.
            changeState(State.OPENING);
            connectionResult.startResolutionForResult(activityWeakReference.get(), REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            // The intent was canceled before it was sent.  Attempt to connect to
            // get an updated ConnectionResult.
            changeState(State.CREATED);
            googleApiClient.connect();
        }
    }

    protected void onRevokeAccessAndDisconnect() {
        // After we revoke permissions for the user with a GoogleApiClient
        // instance, we must discard it and create a new one.
        Auth.GoogleSignInApi.revokeAccess(googleApiClient);

        // Our sample has caches no user data from Google+, however we
        // would normally register a callback on revokeAccessAndDisconnect
        // to delete user data so that we comply with Google developer
        // policies.
        googleApiClient = googleApiClientBuilder.build();
        googleApiClient.connect();
        changeState(State.CLOSED);
    }

    private GoogleConnection(Activity activity, GoogleSignInOptions gso) {
        activityWeakReference = new WeakReference<>(activity);

        googleApiClientBuilder =
                new GoogleApiClient.Builder(activityWeakReference.get().getApplicationContext())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .enableAutoManage((FragmentActivity) activity, sGoogleConnection)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .addScope(new Scope("email"));

        googleApiClient = googleApiClientBuilder.build();
        currentState = State.CLOSED;
    }

    private void changeState(State state) {
        currentState = state;
        setChanged();
        notifyObservers(state);
    }

    private static GoogleConnection sGoogleConnection;

    private WeakReference<Activity> activityWeakReference;
    private GoogleApiClient.Builder googleApiClientBuilder;
    private GoogleApiClient googleApiClient;
    private ConnectionResult connectionResult;
    private State currentState;

}
