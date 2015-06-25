package io.jeb.posture;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;

public class DimReceiver extends BroadcastReceiver {
    private Activity activity;
    private View mainView;

    public DimReceiver() { }

    public DimReceiver(Activity activity) {
        this.activity = activity;
        mainView = activity.findViewById(R.id.main);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int brightness = intent.getIntExtra(DimService.INTENT_EXTRA_BRIGHTNESS, 1);
        android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 1);
        mainView.setBackgroundColor(brightness);
    }
}
