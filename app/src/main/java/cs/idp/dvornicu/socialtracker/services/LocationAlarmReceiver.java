package cs.idp.dvornicu.socialtracker.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class LocationAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        context.stopService(new Intent(context, LocationService.class));
        startWakefulService(context, new Intent(context, LocationService.class));
    }
}
