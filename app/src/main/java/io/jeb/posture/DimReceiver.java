package io.jeb.posture;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

public class DimReceiver extends BroadcastReceiver {
    private Activity activity;

    public DimReceiver() { }

    public DimReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        float brightness = intent.getFloatExtra(DimService.INTENT_EXTRA_BRIGHTNESS, 1);
        Log.d("BRIGHTNESS: ", String.valueOf(brightness));
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        activity.getWindow().setAttributes(lp);
    }
}
