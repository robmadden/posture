package io.jeb.posture;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DimReceiver extends BroadcastReceiver {
    private Activity activity;

    public DimReceiver() { }

    public DimReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int brightness = intent.getIntExtra(DimService.INTENT_EXTRA_BRIGHTNESS, 1);
        Log.d("BRIGHTNESS: ", String.valueOf(brightness));
        android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
    }
}
