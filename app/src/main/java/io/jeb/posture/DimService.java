package io.jeb.posture;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;

public class DimService extends IntentService implements SensorEventListener {
    private ContentResolver cResolver;
    private SensorManager mSensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private float[] mGeomagnetic;
    private float[] mGravity;

    public static final String INTENT_EXTRA_BRIGHTNESS = "brightness";
    private static final float MAX_BRIGHTNESS = 255;
    private static final float BEST_ANGLE = 90;
    private static final float MULTIPLICATION_FACTOR = MAX_BRIGHTNESS / BEST_ANGLE;

    public DimService() {
        super("DimService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        cResolver = getContentResolver();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (magnetometer == null || accelerometer == null) {
            Log.i("NO_ACC", "Sorry, your device does not have a magnetometer or accelerometer, you cannot use this app");
        }

        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Settings.System.putInt(cResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

    }

    /*
     * Take the pitch as a value in degrees and map it to a value between 0 and 1
     * which are the allowable values for brightness
     */
    private int mapPitchToBrightness(float pitch) {
        // Calculate theta which is the angle of the pitch
        // 0: phone is horizontal to the ground
        // 90: phone is perpendicular to the ground
        float theta = (-pitch*360) / (2 * (float)Math.PI);
        int brightness = (int) (theta * MULTIPLICATION_FACTOR);

//        Log.d("THETA: ", String.valueOf(theta));
//        Log.d("BRIGHTNESS: ", String.valueOf(brightness));

        return brightness;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Don't do anything
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
                float pitch = orientation[1]; // orientation contains: azimuth, pitch and roll
                float roll = orientation[2]; // orientation contains: azimuth, pitch and roll

//                Log.d("ORIENTATION: ", "Azimuth: " + azimuth + " Pitch: " + pitch + " Roll: " + roll);

                int brightness = mapPitchToBrightness(pitch);
                Intent i = new Intent();
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(INTENT_EXTRA_BRIGHTNESS, brightness);
                i.setClass(this, DimReceiver.class);
                MainActivity.broadcastManager.sendBroadcast(i);
            }
        }
    }
}
