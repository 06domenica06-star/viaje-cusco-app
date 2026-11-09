package pe.domenica.viajecusco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DailyCountdownReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context, Intent intent) {
  if(DailyCountdown.ACTION.equals(intent.getAction())) DailyCountdown.show(context);
  DailyCountdown.schedule(context);
 }
}
