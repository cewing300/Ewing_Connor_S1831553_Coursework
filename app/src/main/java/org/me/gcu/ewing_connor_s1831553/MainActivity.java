package org.me.gcu.ewing_connor_s1831553;

// Name                 Connor Ewing
// Student ID           S1831553
//Course                Computing Bsc(Hons)

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStreamReader;
import java.text.DateFormat;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView locationTitle;
    private Button startButton;
    private Button btnNext, btnPrevious;
    private String urlSource;
    private String todayUrlSource;
    private List<String> locations;
    private List<String> locationCodes;
    private List<LatLng> locationLatLngs = new ArrayList<>();
    private int currentIndex = 0;
    private TextView dateAndTime;

    // Handler for running delayed tasks
    private final Handler updateHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            startProgress(); // Starts the process of updating data

            // Re-post itself to run again with a calculated delay
            updateHandler.postDelayed(this, calculateDelayUntilNextUpdate());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        locationTitle = findViewById(R.id.locationTitle);
        startButton = findViewById(R.id.startButton);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        dateAndTime = findViewById(R.id.dateAndTime);

        // Setting click listeners
        startButton.setOnClickListener(this);
        btnNext.setOnClickListener(v -> navigateLocations(true));
        btnPrevious.setOnClickListener(v -> navigateLocations(false));

        // Initialize location data
        initLocationData();

        // Setting up fragments for displaying weather forecasts
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.todayForecastContainer, new TodayFragment())
                .commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.threeDayForecastContainer, new ThreeDayFragment())
                .commit();


        updateDateTime();

        // Start periodic updates for location data and forecasts
        updateHandler.post(updateRunnable);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }
        initLocationData();

        if (!locations.isEmpty()) {
            locationTitle.setText(locations.get(currentIndex));
            updateMapFragment(locationLatLngs.get(currentIndex));
        }
    }

    // Constants for determining update times
    private static final int DEFAULT_UPDATE_HOUR_1 = 8; // 8 AM
    private static final int DEFAULT_UPDATE_HOUR_2 = 20; // 8 PM
    private void updateDateTime() {
        // Updates the date and time display with the current date and time
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        dateAndTime.setText(currentDateTimeString);
    }


    private void initLocationData() {
        locations = new ArrayList<>();
        locations.add("Glasgow");
        locations.add("London");
        locations.add("New York");
        locations.add("Oman");
        locations.add("Mauritius");
        locations.add("Bangladesh");

        locationCodes = new ArrayList<>();
        locationCodes.add("2648579"); // Glasgow
        locationCodes.add("2643743"); // London
        locationCodes.add("5128581"); // New York
        locationCodes.add("287286"); // Oman
        locationCodes.add("934154"); // Mauritius
        locationCodes.add("1185241"); // Bangladesh

        locationLatLngs.add(new LatLng(55.8642, -4.2518)); // Glasgow
        locationLatLngs.add(new LatLng(51.5074, -0.1278)); // London
        locationLatLngs.add(new LatLng(40.7128, -74.0060)); // New York
        locationLatLngs.add(new LatLng(23.6150, 58.5400)); // Oman
        locationLatLngs.add(new LatLng(-20.3484, 57.5522)); // Mauritius
        locationLatLngs.add(new LatLng(23.6850, 90.3563)); // Bangladesh
    }

    // Sets the URL for getting weather data based on the location code
    private void setLocationCode(String locationCode) {
        urlSource = "https://weather-broker-cdn.api.bbci.co.uk/en/forecast/rss/3day/" + locationCode;
        todayUrlSource = "https://weather-broker-cdn.api.bbci.co.uk/en/observation/rss/" + locationCode;
    }

    // Navigate through locations in the list based buttons at top
    private void navigateLocations(boolean isNext) {
        currentIndex = isNext ? (currentIndex + 1) % locations.size() : (currentIndex - 1 + locations.size()) % locations.size();
        locationTitle.setText(locations.get(currentIndex));
        setLocationCode(locationCodes.get(currentIndex));
        updateMapFragment(locationLatLngs.get(currentIndex));
        clearFragmentData();
        clearWeatherIcon();
    }

    // Clears the displayed weather icon in the TodayFragment
    private void clearWeatherIcon() {
        TodayFragment fragment = (TodayFragment) getSupportFragmentManager().findFragmentById(R.id.todayForecastContainer);
        if (fragment != null) {
            fragment.clearIcon();
        }
    }


    private void clearFragmentData() {
        Fragment todayFragment = getSupportFragmentManager().findFragmentById(R.id.todayForecastContainer);
        if (todayFragment instanceof TodayFragment) {
            ((TodayFragment) todayFragment).clearData();
        }

        Fragment threeDayFragment = getSupportFragmentManager().findFragmentById(R.id.threeDayForecastContainer);
        if (threeDayFragment instanceof ThreeDayFragment) {
            ((ThreeDayFragment) threeDayFragment).clearData();
        }
    }

    // Updates the displayed map location in the MapFragment
    private void updateMapFragment(LatLng latLng) {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment == null || !(mapFragment instanceof MapFragment)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, MapFragment.newInstance(latLng))
                    .commit();
        } else {
            mapFragment.updateLocation(latLng);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == startButton) {
            startProgress();
        }
    }

    // Initiates getting and updating weather forecasts
    private void startProgress() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<List<WeatherForecast>> futureThreeDay = executor.submit(new ForecastTask(urlSource, true));
        Future<List<WeatherForecast>> futureToday = executor.submit(new ForecastTask(todayUrlSource, false));

        try {
            updateUI(futureThreeDay.get(), true);
            updateUI(futureToday.get(), false);
        } catch (Exception e) {
            Log.e("MyTag", "Error processing forecast tasks", e);
        } finally {
            executor.shutdown();
        }
    }

    // Calculates the delay until the next update based on the current time
    private long calculateDelayUntilNextUpdate() {
        Calendar currentTime = Calendar.getInstance();
        int hourOfDay = currentTime.get(Calendar.HOUR_OF_DAY);

        int nextUpdateHour = (hourOfDay < DEFAULT_UPDATE_HOUR_2) ? DEFAULT_UPDATE_HOUR_2 : DEFAULT_UPDATE_HOUR_1;
        currentTime.set(Calendar.HOUR_OF_DAY, nextUpdateHour);
        currentTime.set(Calendar.MINUTE, 0);
        currentTime.set(Calendar.SECOND, 0);
        long delayMillis = currentTime.getTimeInMillis() - System.currentTimeMillis();

        if (delayMillis < 0) {
            delayMillis += TimeUnit.HOURS.toMillis(24);
        }

        return delayMillis;
    }


    private static class ForecastTask implements java.util.concurrent.Callable<List<WeatherForecast>> {
        private String url;
        private boolean isThreeDay;

        public ForecastTask(String url, boolean isThreeDay) {
            this.url = url;
            this.isThreeDay = isThreeDay;
        }

        @Override
        public List<WeatherForecast> call() throws Exception {
            List<WeatherForecast> forecasts = new ArrayList<>();
            try {
                URL aUrl = new URL(url);
                URLConnection yc = aUrl.openConnection();
                yc.connect();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(yc.getInputStream()));

                WeatherForecast forecast = null;
                String tagContent = null;

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if ("item".equalsIgnoreCase(tagName)) {
                                forecast = new WeatherForecast();
                            }
                            break;
                        case XmlPullParser.TEXT:
                            tagContent = parser.getText();
                            break;
                        case XmlPullParser.END_TAG:
                            if ("item".equalsIgnoreCase(tagName)) {
                                forecasts.add(forecast);
                            } else if (forecast != null) {
                                if ("title".equalsIgnoreCase(tagName)) {
                                    forecast.setTitle(tagContent);
                                } else if ("description".equalsIgnoreCase(tagName)) {
                                    forecast.setDescription(tagContent);
                                }
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                Log.e("MyTag", "Exception in network or parsing", e);
            }
            return forecasts;
        }
    }

    // Updates the UI with new data received from weather forecasts
    private void updateUI(List<WeatherForecast> forecasts, boolean isThreeDay) {
        runOnUiThread(() -> {
            if (isThreeDay) {
                ThreeDayFragment fragment = (ThreeDayFragment) getSupportFragmentManager().findFragmentById(R.id.threeDayForecastContainer);
                if (fragment != null) {
                    fragment.updateWeather(forecasts);
                }
            } else {
                TodayFragment fragment = (TodayFragment) getSupportFragmentManager().findFragmentById(R.id.todayForecastContainer);
                if (fragment != null) {
                    fragment.updateWeather(forecasts);
                }
            }
        });
    }
}
