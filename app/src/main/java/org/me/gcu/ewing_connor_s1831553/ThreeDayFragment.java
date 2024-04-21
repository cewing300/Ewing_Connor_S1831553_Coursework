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

public class ThreeDayFragment extends Fragment {
    private TextView titleTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three_day, container, false);
        titleTextView = view.findViewById(R.id.textViewThreeDay);
        return view;
    }

    public void updateWeather(List<org.me.gcu.ewing_connor_s1831553.WeatherForecast> forecasts) {
        if (!forecasts.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (org.me.gcu.ewing_connor_s1831553.WeatherForecast wf : forecasts) {
                sb.append(wf.getTitle()).append("\n");
            }
            titleTextView.setText(sb.toString());
        }
    }

    public void clearData() {
        if (titleTextView != null) {
            titleTextView.setText("");
        }
    }
}
