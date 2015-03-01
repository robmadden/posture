package io.jeb.posture;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity implements SensorEventListener {
    private DimService service;

    private ContentResolver cResolver;
    private Window window;
    private SensorManager mSensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private float[] mGeomagnetic;
    private float[] mGravity;

    private static final float MAX_BRIGHTNESS = 1;
    private static final float BEST_ANGLE = 90;
    private static final float MULTIPLICATION_FACTOR = MAX_BRIGHTNESS / BEST_ANGLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cResolver = getContentResolver();
        window = getWindow();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Don't do anything
    }

    /*
     * Take the pitch as a value in degrees and map it to a value between 0 and 1
     * which are the allowable values for brightness
     */
    private float mapPitchToBrightness(float pitch) {
        // Calculate theta which is the angle of the pitch
        // 0: phone is horizontal to the ground
        // 90: phone is perpendicular to the ground
        float theta = (-pitch*360) / (2 * (float)Math.PI);
        float brightness = theta * MULTIPLICATION_FACTOR;

        Log.d("THETA: ", String.valueOf(theta));
        Log.d("BRIGHTNESS: ", String.valueOf(brightness));

        return brightness;
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

                float brightness = mapPitchToBrightness(pitch);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.screenBrightness = brightness;
                getWindow().setAttributes(lp);
            }
        }
    }
}
