package pe.domenica.viajecusco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventNotificationReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent) { EventNotifications.show(context,intent); }
}
