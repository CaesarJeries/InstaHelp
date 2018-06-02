package utils.location;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import project.com.instahelp.R;

public class LocationResolver {
    private Timer timer;
    private LocationManager locationManager;
    private LocationResult locationResult;
    private boolean gpsEnabled = false;
    private boolean networkEnabled = false;
    private Handler locationTimeoutHandler;

    private Context context;


    public LocationResolver(Context context){
        this.context = context;
    }

    private final Handler.Callback locationTimeoutCallback = new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            locationTimeoutFunc();
            return true;
        }

        private void locationTimeoutFunc() {
            try{
                locationManager.removeUpdates(locationListenerGps);
                locationManager.removeUpdates(locationListenerNetwork);
            } catch(SecurityException e){
                Log.e("LocationResolver", e.getMessage());
            }


            Location networkLocation = null, gpsLocation = null;
            if (gpsEnabled){
                try{
                    gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }catch(SecurityException e){
                    Log.e("LocationResolver", e.getMessage());
                }
            }


            if (networkEnabled){
                try{
                    networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }catch(SecurityException e){
                    Log.e("LocationResolver", e.getMessage());
                }
            }


            // if there are both values use the latest one
            if (gpsLocation != null && networkLocation != null) {
                if (gpsLocation.getTime() > networkLocation.getTime())
                    locationResult.onResult(gpsLocation);
                else
                    locationResult.onResult(networkLocation);
                return;
            }

            if (gpsLocation != null) {
                locationResult.onResult(gpsLocation);
                return;
            }
            if (networkLocation != null) {
                locationResult.onResult(networkLocation);
                return;
            }
            locationResult.onResult(null);
        }
    };
    private final LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.onResult(location);
            try{
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
            } catch(SecurityException e){
                Log.e("LocationResolver", e.getMessage());
            }

        }

        public void onProviderDisabled(String provider) {
            showGPSAlert();
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void showGPSAlert(){

        String gpsAlertTitle = context.getResources().getString(R.string.gps_alert_title);
        String gpsAlertContent = context.getResources().getString(R.string.gps_alert_content);
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(gpsAlertTitle)
                .setMessage(gpsAlertContent)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.onResult(location);
            try{
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
            }catch(SecurityException e){
                Log.e("LocationResolver", e.getMessage());
            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public void prepare() {
        locationTimeoutHandler = new Handler(locationTimeoutCallback);
    }

    public synchronized boolean getLocation(Context context, LocationResult result, int maxMillisToWait) {
        locationResult = result;
        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e("LocationResolver", e.getMessage());
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e("LocationResolver", e.getMessage());
        }

        // don't start listeners if no provider is enabled
        if (!gpsEnabled && !networkEnabled)
            return false;

        if (gpsEnabled){
            try{
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListenerGps, Looper.myLooper());
            }catch(SecurityException e){
                Log.e("LocationResolver", e.getMessage());
            }

        }

        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        } catch (SecurityException e){
            Log.e("LocationResolver", e.getMessage());
        }
        if (networkEnabled){
            try{
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListenerNetwork, Looper.myLooper());
            }catch(SecurityException e){
                Log.e("LocationResolver", e.getMessage());
            }
        }


        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        } catch(SecurityException e){
            Log.e("LocationResolver", e.getMessage());
        }

        timer = new Timer();
        timer.schedule(new GetLastLocationTask(), maxMillisToWait);
        return true;
    }

    private class GetLastLocationTask extends TimerTask {
        @Override
        public void run() {
            locationTimeoutHandler.sendEmptyMessage(0);
        }
    }

    public static abstract class LocationResult {
        public abstract void onResult(Location location);
    }
}