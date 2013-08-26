package ru.UseIT.LocationWithGooglePlayServices;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity implements ILocationListener.LocationRunnable
{
    private LinearLayout content;
    private ILocationListener locationListener;
    private LatLng[] latLngs = new LatLng[]{new LatLng(56.836155, 60.599564), new LatLng(56.826753, 60.603116),
                                            new LatLng(56.847483, 60.599843), new LatLng(56.858153, 60.600676),
                                            new LatLng(56.878815, 60.611351), new LatLng(56.888589, 60.613908),
                                            new LatLng(56.901334, 60.613995)};//массив координат
    private String[] names = new String[]{"Площадь 1905 года", "Геологическая", "Динамо", "Уральская",
                                          "Машиностроителей", "Уралмаш", "Проспект Космонавтов"};//массив названий
    private SortStation sortStation;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        content = (LinearLayout) findViewById(R.id.content);
        locationListener = LocationListenerGPServices.getInstance(this);
        locationListener.setLocationRunnable(this);
        findViewById(R.id.starFind).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                startFind();
            }
        });
    }

    @Override
    public void onPause()
    {
        locationListener.disableMyLocation();
        super.onPause();
    }

    /**
     * Начинает искать новое местоположение
     */
    private void startFind()
    {
        if (sortStation == null)
        {
            locationListener.enableMyLocation();
            content.removeAllViews();
            final TextView textView = new TextView(MainActivity.this);
            textView.setText("Поиск...");
            textView.setPadding(5, 10, 5, 10);
            content.addView(textView);

        }
    }


    private class SortStation extends AsyncTask<Location, Void, Integer[]>
    {
        private double mLat = 0;
        private double mLon = 0;
        private Integer[] indexStops;

        //определяет расстояние от пользователя до станции
        private double getPointInRadius(final double lat, final double lon)
        {
            int radius = 6371; // Километры, радиус земли
            double dLat = Math.toRadians(mLat - lat);
            double dLong = Math.toRadians(mLon - lon);
            double lat1 = Math.toRadians(mLat);
            double lat2 = Math.toRadians(lat);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(
                    dLong / 2) * Math.sin(dLong / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return c * radius;
        }

        //алгоритм быстрой сортировки
        public void qSort(int low, int high)
        {
            if (low >= 0 && high < indexStops.length)
            {
                int i = low;
                int j = high;
                int x = indexStops[(low + high) / 2];  // x - опорный элемент посредине между low и high
                final LatLng latLng_x = latLngs[indexStops[x]];

                do
                {
                    LatLng latLng_i = latLngs[indexStops[i]];
                    while (getPointInRadius(latLng_i.latitude, latLng_i.longitude) < getPointInRadius(latLng_x.latitude, latLng_x.longitude) && i <= high) // поиск элемента для переноса в старшую часть
                    {
                        ++i;
                        latLng_i = latLngs[indexStops[i]];
                    }

                    LatLng latLng_j = latLngs[indexStops[j]];

                    while (getPointInRadius(latLng_j.latitude, latLng_j.longitude) < getPointInRadius(latLng_x.latitude, latLng_x.longitude) && j > low)   // поиск элемента для переноса в младшую часть
                    {
                        --j;
                        latLng_j = latLngs[indexStops[j]];
                    }
                    if (i <= j)
                    {
                        // обмен элементов местами:
                        int temp = indexStops[i];
                        indexStops[i] = indexStops[j];
                        indexStops[j] = temp;
                        // переход к следующим элементам:
                        i++;
                        j--;
                    }
                } while (i < j);
                if (low < j)
                {
                    qSort(low, j);
                }
                if (i < high)
                {
                    qSort(i, high);
                }
            }
        }

        @Override
        protected Integer[] doInBackground(final Location... params)
        {
            if (params.length > 0 && params[0] != null)
            {
                if (indexStops == null)
                {
                    indexStops = new Integer[latLngs.length];
                    final int sz = indexStops.length;
                    for (int i = 0; i < sz; i++)
                        indexStops[i] = i;
                }

                qSort(0, indexStops.length - 1);
                return indexStops;
            }

            return null;
        }

        protected void onPostExecute(Integer[] indexs)
        {
            if (indexs != null)
            {
                content.removeAllViews();
                for (int i = 0; i < indexs.length; i++)
                {
                    final TextView textView = new TextView(MainActivity.this);
                    textView.setText(names[indexs[i]]);
                    textView.setPadding(5, 10, 5, 10);
                    content.addView(textView);
                }

            }

            sortStation = null;
        }
    }

    /**
     * Вызывается, когда местоположение пользователя определяется
     *
     * @param location местоположение пользователя
     */
    @Override
    public void locationUpdate(final Location location)
    {
        locationListener.disableMyLocation();
        sortStation = new SortStation();
        sortStation.execute(location);
    }

}
