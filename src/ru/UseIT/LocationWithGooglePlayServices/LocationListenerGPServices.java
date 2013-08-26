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
        if (locationRunnable != null)
            locationRunnable.locationUpdate(location);
    }

    @Override
    public void onConnected(final Bundle bundle)
    {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
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
        mLocationClient.connect();
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
