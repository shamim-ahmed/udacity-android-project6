package com.example.android.sunshine.app.wearable;

/**
 * Created by shamim on 5/28/16.
 */
public class ForecastData {
    private final String highTemperature;
    private final String lowTemperature;
    private final String summary;

    public ForecastData(String highTemperature, String lowTemperature, String summary) {
        if (highTemperature == null || lowTemperature == null || summary == null) {
            throw new IllegalArgumentException("temperature value or summary cannot be null");
        }

        this.highTemperature = highTemperature;
        this.lowTemperature = lowTemperature;
        this.summary = summary;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ForecastData)) {
            return false;
        }

        ForecastData otherForecast = (ForecastData) obj;

        return highTemperature.equals(otherForecast.highTemperature) && lowTemperature.equals(otherForecast.lowTemperature)
                && summary.equals(otherForecast.summary);
    }
}
