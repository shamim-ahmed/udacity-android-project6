package com.example.android.sunshine.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.SunshineWatchFaceApplication;
import com.example.android.sunshine.app.util.DateFormatUtil;
import com.example.android.sunshine.app.util.WearableConstants;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by shamim on 5/23/16.
 */
public class SunshineWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = SunshineWatchFaceService.class.getSimpleName();

    // update rate in ms for interactive mode
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    // Handler message id for updating the calendar periodically in interactive mode.
    private static final int MSG_UPDATE_TIME = 0;

    private Typeface WATCH_TEXT_TYPEFACE = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

    @Override
    public Engine onCreateEngine() {
        return new WatchFaceEngine();
    }

    private class EngineHandler extends Handler {
        private final WeakReference<WatchFaceEngine> engineWeakReference;

        public EngineHandler(WatchFaceEngine engine) {
            engineWeakReference = new WeakReference<>(engine);
        }

        @Override
        public void handleMessage(Message message) {
            WatchFaceEngine engine = engineWeakReference.get();

            if (engine != null && message.what == MSG_UPDATE_TIME) {
                engine.handleUpdateTimeMessage();
            }
        }
    }

    private class WatchFaceEngine extends Engine {
        private final Handler updateTimeHandler = new EngineHandler(this);
        private boolean registeredTimeZoneReceiver = false;
        private boolean lowBitAmbient;
        private Calendar calendar;

        private int backgroundColor;
        private int timeColor;
        private int dateColor;
        private int highTemperatureColor;
        private int lowTemperatureColor;

        private Paint backgroundPaint;
        private Paint timePaint;
        private Paint datePaint;
        private Paint highTemperaturePaint;
        private Paint lowTemperaturePaint;
        private Paint iconPaint;

        private float xOffset;
        private float yOffset;

        private final BroadcastReceiver timeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                calendar = Calendar.getInstance(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            // initialize all paint objects
            Context appContext = getApplicationContext();
            backgroundColor = ContextCompat.getColor(appContext, R.color.background);
            backgroundPaint = new Paint();
            backgroundPaint.setColor(backgroundColor);

            timeColor = ContextCompat.getColor(appContext, R.color.time_color);
            dateColor = ContextCompat.getColor(appContext, R.color.date_color);
            highTemperatureColor = ContextCompat.getColor(appContext, R.color.high_temperature_color);
            lowTemperatureColor = ContextCompat.getColor(appContext, R.color.low_temperature_color);

            timePaint = createTextPaint(timeColor, R.dimen.time_text_size);
            datePaint = createTextPaint(dateColor, R.dimen.date_text_size);
            highTemperaturePaint = createTextPaint(highTemperatureColor, R.dimen.temperature_text_size);
            lowTemperaturePaint = createTextPaint(lowTemperatureColor, R.dimen.temperature_text_size);

            iconPaint = new Paint();
            iconPaint.setAntiAlias(true);

            // initialize the calendar with default timezone
            calendar = Calendar.getInstance();
        }

        @Override
        public void onDestroy() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (inAmbientMode) {
                backgroundPaint.setColor(Color.BLACK);
                timePaint.setColor(Color.WHITE);
                datePaint.setColor(Color.WHITE);
                highTemperaturePaint.setColor(Color.BLACK);
                lowTemperaturePaint.setColor(Color.BLACK);

                ColorFilter filter = new LightingColorFilter(Color.BLACK, 0);
                iconPaint.setColorFilter(filter);
            } else {
                backgroundPaint.setColor(backgroundColor);
                timePaint.setColor(timeColor);
                datePaint.setColor(dateColor);
                highTemperaturePaint.setColor(highTemperatureColor);
                lowTemperaturePaint.setColor(lowTemperatureColor);

                ColorFilter filter = new LightingColorFilter(Color.WHITE, 0);
                iconPaint.setColorFilter(filter);
            }

            if (lowBitAmbient) {
                timePaint.setAntiAlias(!inAmbientMode);
                datePaint.setAntiAlias(!inAmbientMode);
                highTemperaturePaint.setAntiAlias(!inAmbientMode);
                lowTemperaturePaint.setAntiAlias(!inAmbientMode);
                iconPaint.setAntiAlias(!inAmbientMode);
            }

            invalidate();
            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            Resources resources = getResources();

            yOffset = resources.getDimension(R.dimen.y_offset);

            if (insets.isRound()) {
                xOffset = resources.getDimension(R.dimen.x_offset_round);
            } else {
                xOffset = resources.getDimension(R.dimen.x_offset_square);
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            Log.i(TAG, "onDraw is invoked");

            calendar.setTime(new Date());

            // draw the background
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);

            drawTimeAndDate(canvas);
            drawForecastInfo(canvas);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                calendar.setTimeZone(TimeZone.getDefault());
                calendar.setTime(new Date());
            } else {
                unregisterReceiver();
            }

            updateTimer();
        }

        public void handleUpdateTimeMessage() {
            invalidate();

            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                updateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        private void drawTimeAndDate(Canvas canvas) {
            String timeText = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            String dateText = DateFormatUtil.generateDateString(calendar.getTime());
            canvas.drawText(timeText, xOffset, yOffset, timePaint);

            Resources resources = getResources();
            int dateDelX = (int) resources.getDimension(R.dimen.date_del_x);
            int dateDelY = (int) resources.getDimension(R.dimen.date_del_y);
            canvas.drawText(dateText, xOffset - dateDelX, yOffset + dateDelY, datePaint);
        }

        private void drawForecastInfo(Canvas canvas) {
            SunshineWatchFaceApplication application = (SunshineWatchFaceApplication) getApplication();
            Map<String, Object> forecastDataMap = application.getForecastDataMap();
            Bitmap bitmap = (Bitmap) forecastDataMap.get(WearableConstants.ICON_KEY);
            String highTempStr = (String) forecastDataMap.get(WearableConstants.TEMPERATURE_HIGH_KEY);
            String lowTempStr = (String) forecastDataMap.get(WearableConstants.TEMPERATURE_LOW_KEY);

            Resources resources = getResources();

            // draw the separator
            int separatorStartDelX = (int) resources.getDimension(R.dimen.separator_start_del_x);
            int separatorEndDelX = (int) resources.getDimension(R.dimen.separator_end_del_x);
            int separatorDelY = (int) resources.getDimension(R.dimen.separator_del_y);
            canvas.drawLine(xOffset + separatorStartDelX, yOffset + separatorDelY, xOffset + separatorEndDelX, yOffset + separatorDelY, lowTemperaturePaint);

            if (bitmap != null) {
                // draw the provided icon
                Log.i(TAG, "drawing the icon");
                int iconDelX = (int) resources.getDimension(R.dimen.icon_del_x);
                int iconDelY = (int) resources.getDimension(R.dimen.icon_del_y);
                Rect rect = new Rect();
                int x = (int) xOffset + iconDelX;
                int y = (int) yOffset + iconDelY;

                int iconSize = (int) resources.getDimension(R.dimen.icon_size);

                rect.set(x, y, x + iconSize, y + iconSize);
                canvas.drawBitmap(bitmap, null, rect, iconPaint);

                int tempDelY = (int) resources.getDimension(R.dimen.temp_del_y);
                int highTempDelX = (int) resources.getDimension(R.dimen.high_temp_del_x);
                int lowTempDelX = (int) resources.getDimension(R.dimen.low_temp_del_x);

                // draw temperature info
                canvas.drawText(highTempStr, x + highTempDelX, y + tempDelY, highTemperaturePaint);
                canvas.drawText(lowTempStr, x + lowTempDelX, y + tempDelY, lowTemperaturePaint);
            }
        }

        private void registerReceiver() {
            if (registeredTimeZoneReceiver) {
                return;
            }

            registeredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatchFaceService.this.registerReceiver(timeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!registeredTimeZoneReceiver) {
                return;
            }

            registeredTimeZoneReceiver = false;
            SunshineWatchFaceService.this.unregisterReceiver(timeZoneReceiver);
        }

        private void updateTimer() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME);

            if (shouldTimerBeRunning()) {
                updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private Paint createTextPaint(int textColor, int dimension) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(WATCH_TEXT_TYPEFACE);
            paint.setAntiAlias(true);
            paint.setTextSize(getResources().getDimension(dimension));

            return paint;
        }
    }
}
