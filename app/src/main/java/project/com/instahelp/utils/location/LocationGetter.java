package utils.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

public class LocationGetter {
    private final Context context;
    private Location location = null;
    private final Object gotLocationLock = new Object();
    private final LocationResolver.LocationResult locationResult = new LocationResolver.LocationResult() {
        @Override
        public void onResult(Location location) {
            synchronized (gotLocationLock) {
                LocationGetter.this.location = location;
                gotLocationLock.notifyAll();
                Looper.myLooper().quit();
            }
        }
    };



    public LocationGetter(Context context) {
        if (context == null)
            throw new IllegalArgumentException("context == null");

        this.context = context;
    }

    /**
     *
     * @param maxWaitingTime - waiting time in milliseconds.
     * @param updateTimeout - timeout period in milliseconds.
     * @return A Coordinates instance, initialized to the best known location of the current
     *          device, or Coordinates.UNDEFINED in case of an error.
     */

    public synchronized Location getLocation(int maxWaitingTime, int updateTimeout) {
        try {
            final int updateTimeoutPar = updateTimeout;
            synchronized (gotLocationLock) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        LocationResolver locationResolver = new LocationResolver(context);
                        locationResolver.prepare();
                        locationResolver.getLocation(context, locationResult, updateTimeoutPar);
                        Looper.loop();
                    }
                }.start();

                gotLocationLock.wait(maxWaitingTime);
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        return location;
    }
}
