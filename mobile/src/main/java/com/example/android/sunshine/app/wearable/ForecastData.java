package com.example.android.sunshine.app.wearable;

import com.example.android.sunshine.app.util.StringUtils;

/**
 * Created by shamim on 5/28/16.
 */
public class ForecastData {
    private final String highTemperature;
    private final String lowTemperature;
    private final String summary;

    public ForecastData(String highTemperature, String lowTemperature, String summary) {
        if (StringUtils.isBlank(highTemperature) || StringUtils.isBlank(lowTemperature) || StringUtils.isBlank(summary)) {
            throw new IllegalArgumentException("Temperature value or summary cannot be blank");
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
