package pe.domenica.viajecusco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemEventsReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context, Intent intent) { DailyCountdown.schedule(context); EventNotifications.schedule(context); }
}
