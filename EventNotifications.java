package pe.domenica.viajecusco;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

final class EventNotifications {
 static final String PREFS="itinerary_notifications";
 static final String ACTION="pe.domenica.viajecusco.ITINERARY_EVENT";
 static final String CHANNEL="itinerary_events";
 private static final ZoneId LIMA=ZoneId.of("America/Lima");
 private EventNotifications() {}

 static void update(Context c,String json,String month) {
  var prefs=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
  cancelList(c,prefs.getString("events","[]"));
  prefs.edit().putString("events",json).putString("month",month).apply();
  schedule(c);
 }
 static void schedule(Context c) {
  if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
  var prefs=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
  String json=prefs.getString("events","[]"),month=prefs.getString("month","2026-12");
  try {
   JSONArray events=new JSONArray(json);
   String[] ym=month.split("-");int year=Integer.parseInt(ym[0]),m=Integer.parseInt(ym[1]);
   ZonedDateTime now=ZonedDateTime.now(LIMA);
   AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(am==null)return;
   for(int i=0;i<events.length();i++) {
    JSONObject e=events.optJSONObject(i);if(e==null)continue;
    String id=String.valueOf(e.opt("id"));String title=e.optString("title","").trim();String time=e.optString("time","");
    if(title.isEmpty()||time.isEmpty())continue;
    int day=e.optInt("day",0);LocalDate date;
    try{date=LocalDate.of(year,m,day);}catch(Exception invalid){continue;}
    ZonedDateTime when=date.atTime(LocalTime.parse(time)).atZone(LIMA);
    if(!when.isAfter(now))continue;
    PendingIntent pi=alarmIntent(c,id,title,e.optString("place",""),when.toInstant().toEpochMilli());
    try {
     if(Build.VERSION.SDK_INT>=31&&am.canScheduleExactAlarms())am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when.toInstant().toEpochMilli(),pi);
     else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when.toInstant().toEpochMilli(),pi);
    } catch(SecurityException ignored) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when.toInstant().toEpochMilli(),pi); }
   }
  } catch(Exception ignored) {}
 }
 private static PendingIntent alarmIntent(Context c,String id,String title,String place,long at) {
  Intent intent=new Intent(c,EventNotificationReceiver.class).setAction(ACTION).setData(Uri.parse("trip-event://"+Uri.encode(id)));
  intent.putExtra("id",id).putExtra("title",title).putExtra("place",place).putExtra("at",at);
  return PendingIntent.getBroadcast(c,id.hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 }
 private static void cancelList(Context c,String json) {
  try {JSONArray old=new JSONArray(json);AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(am==null)return;
   for(int i=0;i<old.length();i++){JSONObject e=old.optJSONObject(i);if(e==null)continue;String id=String.valueOf(e.opt("id"));Intent intent=new Intent(c,EventNotificationReceiver.class).setAction(ACTION).setData(Uri.parse("trip-event://"+Uri.encode(id)));PendingIntent pi=PendingIntent.getBroadcast(c,id.hashCode(),intent,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(pi!=null){am.cancel(pi);pi.cancel();}}
  } catch(Exception ignored) {}
 }
 static void show(Context c,Intent source) {
  if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
  String title=source.getStringExtra("title");String place=source.getStringExtra("place");if(title==null||title.trim().isEmpty())return;
  String text=place==null||place.isBlank()?title:title+" · "+place;
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);if(nm==null)return;
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Avisos del itinerario",NotificationManager.IMPORTANCE_HIGH));
  Intent open=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent content=PendingIntent.getActivity(c,Math.abs(String.valueOf(source.getStringExtra("id")).hashCode()),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  int notificationId=source.getStringExtra("id")==null?2001:source.getStringExtra("id").hashCode();
  nm.notify(notificationId,new android.app.Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Es hora: "+title).setContentText(text).setStyle(new android.app.Notification.BigTextStyle().bigText(text)).setContentIntent(content).setAutoCancel(true).build());
 }
}
