package org.me.gcu.ewing_connor_s1831553;
// Name                 Connor Ewing
// Student ID           S1831553

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import java.util.List;
import android.widget.ImageView;

public class TodayFragment extends Fragment {
    private TextView titleTextView;
    private TextView descriptionTextView;
    private ImageView weatherIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);
        titleTextView = view.findViewById(R.id.textViewToday);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        weatherIcon = view.findViewById(R.id.weatherIcon);

        descriptionTextView.setVisibility(View.GONE);  // description is hidden

        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (descriptionTextView.getVisibility() == View.VISIBLE) {
                    descriptionTextView.setVisibility(View.GONE);
                } else {
                    descriptionTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    public void updateWeather(List<org.me.gcu.ewing_connor_s1831553.WeatherForecast> forecasts) {
        if (!forecasts.isEmpty()) {
            org.me.gcu.ewing_connor_s1831553.WeatherForecast forecast = forecasts.get(0);
            titleTextView.setText(forecast.getTitle());
            descriptionTextView.setText(forecast.getDescription());
            updateWeatherIcon(forecast.getTitle());
            weatherIcon.setVisibility(View.VISIBLE);
        } else {
            clearData();
        }
    }

    private void updateWeatherIcon(String title) {
        String titleLower = title.toLowerCase();

        if (titleLower.contains("sunny")) {
            weatherIcon.setImageResource(R.drawable.day_clear);
        } else if (titleLower.contains("rain")) {
            weatherIcon.setImageResource(R.drawable.rain);
        } else if (titleLower.contains("cloudy") && !titleLower.contains("partly cloudy")) {
            weatherIcon.setImageResource(R.drawable.cloudy);
        } else if (titleLower.contains("light rain")) {
            weatherIcon.setImageResource(R.drawable.day_rain);
        } else if (titleLower.contains("partly cloudy")) {
            weatherIcon.setImageResource(R.drawable.day_partial_cloud);
        } else {
            weatherIcon.setImageResource(R.drawable.unavailable);
        }
    }

    public void clearData() {
        if (titleTextView != null) {
            titleTextView.setText("");
        }
        if (descriptionTextView != null) {
            descriptionTextView.setText("");
            descriptionTextView.setVisibility(View.GONE);
        }
        if (weatherIcon != null) {
            weatherIcon.setVisibility(View.GONE);  // Hides Icon
        }
    }
    public void clearIcon() {
        if (weatherIcon != null) {
            weatherIcon.setVisibility(View.GONE);
        }
    }


}
