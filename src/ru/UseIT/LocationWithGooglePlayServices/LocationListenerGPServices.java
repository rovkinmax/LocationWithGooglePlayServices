package ru.UseIT.LocationWithGooglePlayServices;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Created UseIT for  LocationWithGooglePlayServices
 * User: maxrovkin
 * Date: 25.08.13
 * Time: 13:41
 * <p/>
 * ===============================================================================================================
 * В данной реализации я использую singleton. Но его можно и переписать под обычный класс
 * ===============================================================================================================
 */
public final class LocationListenerGPServices extends ILocationListener implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener
{


    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private Location mLocation = null;
    private FindLocation findLocation;

    private static volatile LocationListenerGPServices instance;

    private LocationListenerGPServices(final Context context)
    {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        mLocationClient = new LocationClient(context, this, this);

        log("constructor");
    }

    public static LocationListenerGPServices getInstance(final Context context)
    {
        if (instance == null)
        {
            synchronized (LocationListenerGPServices.class)
            {
                if (instance == null)
                    instance = new LocationListenerGPServices(context);
            }
        }
        return instance;
    }

    @Override
    public void onLocationChanged(final Location location)
    {
        mLocation = location;
    }

    @Override
    public void onConnected(final Bundle bundle)
    {
        if (!useCurrentLocation())
        {

            mLocationClient.requestLocationUpdates(mLocationRequest, this);
            if (findLocation != null && !findLocation.isCancelled())
                findLocation.cancel(true);
            findLocation = new FindLocation();
            findLocation.execute();

        }
    }

    @Override
    public void onDisconnected()
    {}

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult)
    {

    }

    /**
     * Проверяет, можно ли подключиться к типу  определения местоположения
     *
     * @param context контекст приложения для доступа
     *
     * @return возвращает true в случае успеха
     */
    public static boolean servicesConnected(final Context context)
    {
        //проверим, можем ли мы использовать сервисы
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        return ConnectionResult.SUCCESS == resultCode;
    }


    /**
     * Включает определение месоположения
     */
    @Override
    public void enableMyLocation()
    {
        log("enableMyLocation");
        mLocation = null;
        mLocationClient.connect();
    }


    /**
     * Определяет, подходит ли нам последнее изветное местопложение. Интервал 30 секунд
     *
     * @return возвращает true, в случае использования
     */
    private boolean useCurrentLocation()
    {
        final Location location = mLocationClient.getLastLocation();
        if (System.currentTimeMillis() - location.getTime() < HALF_MINUTE)
        {
            log("is useCurrentLocation");
            disableMyLocation();
            if (locationRunnable != null)
                locationRunnable.locationUpdate(location);
            return true;
        }

        return false;
    }

    /**
     * Прекращает поиск либо по времени либо по нахождения местоположения.
     *
     * @return возвращает местоположение, если оно не было найдено, то будет null
     */
    private Location endFind()
    {
        long sec = System.currentTimeMillis();
        //time out 20 sec
        while (this.mLocation == null && System.currentTimeMillis() - sec < TIME_OUT)
        {
        }
        log("endFind");
        return this.mLocation;
    }

    private class FindLocation extends AsyncTask<Void, Void, Location>
    {

        @Override
        protected Location doInBackground(final Void... params)
        {
            return endFind();
        }

        @Override
        protected void onPostExecute(final Location location)
        {
            if (locationRunnable != null)
                locationRunnable.locationUpdate(location);
            disableMyLocation();
        }
    }

    /**
     * Прекращает определение местоположения
     */
    @Override
    public void disableMyLocation()
    {

        if (mLocationClient.isConnected())
            mLocationClient.removeLocationUpdates(this);

        mLocationClient.disconnect();

    }
}
