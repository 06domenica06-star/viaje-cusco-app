package pe.domenica.viajecusco;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

final class DailyCountdown {
 static final String PREFS="daily_countdown";
 static final String ACTION="pe.domenica.viajecusco.DAILY_COUNTDOWN";
 static final String CHANNEL="trip_countdown";
 static final ZoneId LIMA=ZoneId.of("America/Lima");
 private DailyCountdown() {}
 static void save(Context c, boolean enabled, String date, String time) {
  c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putBoolean("enabled",enabled).putString("date",date).putString("time",time).apply();
  schedule(c);
 }
 static void schedule(Context c) {
  var p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
  if(!p.getBoolean("enabled",false)) { cancel(c); return; }
  if(Build.VERSION.SDK_INT>=33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return;
  String[] hm=p.getString("time","08:00").split(":");
  int hour=Integer.parseInt(hm[0]), minute=Integer.parseInt(hm[1]);
  ZonedDateTime now=ZonedDateTime.now(LIMA);
  LocalDate tripDate=LocalDate.parse(p.getString("date","2026-12-15"));
  if(now.toLocalDate().isAfter(tripDate)) { cancel(c); return; }
  ZonedDateTime next=now.toLocalDate().atTime(hour,minute).atZone(LIMA);
  if(now.toLocalDate().equals(tripDate)&&!next.isAfter(now)) { cancel(c); return; }
  if(!next.isAfter(now)) next=next.plusDays(1);
  Intent intent=new Intent(c,DailyCountdownReceiver.class).setAction(ACTION);
  PendingIntent pi=PendingIntent.getBroadcast(c,1,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  if(am!=null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.toInstant().toEpochMilli(),pi);
 }
 static void cancel(Context c) {
  Intent intent=new Intent(c,DailyCountdownReceiver.class).setAction(ACTION);
  PendingIntent pi=PendingIntent.getBroadcast(c,1,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE); if(am!=null)am.cancel(pi);
 }
 static void show(Context c) {
  if(Build.VERSION.SDK_INT>=33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
  var p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
  LocalDate start=LocalDate.parse(p.getString("date","2026-12-15"));
  long days=java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(LIMA),start);
  String text=days>1?"Faltan "+days+" días para tu viaje a Cusco.":days==1?"Mañana empieza tu viaje a Cusco.":days==0?"¡Hoy empieza tu viaje a Cusco!":"Tu viaje a Cusco ya comenzó.";
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(nm==null)return;
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Aviso diario del viaje",NotificationManager.IMPORTANCE_DEFAULT));
  Intent open=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent content=PendingIntent.getActivity(c,2,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  nm.notify(1001,new android.app.Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Viaje a Cusco").setContentText(text).setStyle(new android.app.Notification.BigTextStyle().bigText(text)).setContentIntent(content).setAutoCancel(true).build());
 }
}
