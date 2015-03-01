package io.jeb.posture;

import android.app.Activity;

public class DimService implements Runnable {
    private Activity activity;

    public DimService(Activity activity) {
        this.activity = activity;
    }

    public void run() {
        android.provider.Settings.System.putInt(activity.getApplicationContext().getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, 1);
    }
}
