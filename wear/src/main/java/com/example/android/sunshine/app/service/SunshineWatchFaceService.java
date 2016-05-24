package com.example.android.sunshine.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.SunshineWatchFaceApplication;
import com.example.android.sunshine.app.util.DateFormatUtil;
import com.example.android.sunshine.app.util.WearableConstants;

import java.lang.ref.WeakReference;
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

    // Handler message id for updating the displayTime periodically in interactive mode.
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


    private class WatchFaceEngine extends CustomWatchFaceService.Engine {
        private final Handler updateTimeHandler = new EngineHandler(this);
        private boolean registeredTimeZoneReceiver = false;
        private boolean ambient;
        private boolean lowBitAmbient;
        private Time displayTime;
        private int tapCount;

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
                displayTime.clear(intent.getStringExtra("displayTime-zone"));
                displayTime.setToNow();
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
            Resources resources = getResources();
            backgroundPaint = new Paint();
            backgroundPaint.setColor(resources.getColor(R.color.background));

            int timeColor = resources.getColor(R.color.time_color);
            int dateColor = resources.getColor(R.color.date_color);
            int highTemperatureColor = resources.getColor(R.color.high_temperature_color);
            int lowTemperatureColor = resources.getColor(R.color.low_temperature_color);

            timePaint = createTextPaint(timeColor, R.dimen.time_text_size);
            datePaint = createTextPaint(dateColor, R.dimen.date_text_size);
            highTemperaturePaint = createTextPaint(highTemperatureColor, R.dimen.temperature_text_size);
            lowTemperaturePaint = createTextPaint(lowTemperatureColor, R.dimen.temperature_text_size);

            iconPaint = new Paint();
            iconPaint.setAntiAlias(true);

            // initialize the time
            displayTime = new Time();
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

            if (ambient != inAmbientMode) {
                ambient = inAmbientMode;

                if (lowBitAmbient) {
                    timePaint.setAntiAlias(!inAmbientMode);
                    datePaint.setAntiAlias(!inAmbientMode);
                    highTemperaturePaint.setAntiAlias(!inAmbientMode);
                    lowTemperaturePaint.setAntiAlias(!inAmbientMode);
                }

                invalidate();
            }

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

            displayTime.setToNow();

            // draw the background
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);

            drawTimeAndDate(canvas);
            drawForecastInfo(canvas);
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = SunshineWatchFaceService.this.getResources();

            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP: {
                    // The user has completed the tap gesture.
                    tapCount++;
                    int resultColor = tapCount % 2 == 0 ? R.color.alternate_background : R.color.background;
                    backgroundPaint.setColor(resources.getColor(resultColor));
                    break;
                }
            }

            invalidate();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                displayTime.clear(TimeZone.getDefault().getID());
                displayTime.setToNow();
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
            String timeText = displayTime.hour + ":" + String.format("%02d", displayTime.minute);
            String dateText = DateFormatUtil.generateDateString();
            canvas.drawText(timeText, xOffset, yOffset, timePaint);
            canvas.drawText(dateText, xOffset - 15, yOffset + 35, datePaint);
        }

        private void drawForecastInfo(Canvas canvas) {
            SunshineWatchFaceApplication application = (SunshineWatchFaceApplication) getApplication();
            Map<String, Object> forecastDataMap = application.getForecastDataMap();
            Bitmap bitmap = (Bitmap) forecastDataMap.get(WearableConstants.ICON_KEY);
            String highTempStr = (String) forecastDataMap.get(WearableConstants.TEMPERATURE_HIGH_KEY);
            String lowTempStr = (String) forecastDataMap.get(WearableConstants.TEMPERATURE_LOW_KEY);

            // draw the separator
            canvas.drawLine(xOffset + 50, yOffset + 60, xOffset + 100, yOffset + 60, lowTemperaturePaint);

            if (bitmap == null) {
                // TODO draw a default icon
                Log.i(TAG, "no icon found");
            } else {
                // draw the provided icon
                Log.i(TAG, "drawing the icon");
                Rect rect = new Rect();
                int x = (int) xOffset;
                int y = (int) yOffset + 75;

                rect.set(x, y, x + 40, y + 40);
                canvas.drawBitmap(bitmap, null, rect, iconPaint);

                // draw temperature info
                canvas.drawText(highTempStr, x + 50, y + 30, highTemperaturePaint);
                canvas.drawText(lowTempStr, x + 105, y + 30, lowTemperaturePaint);
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
