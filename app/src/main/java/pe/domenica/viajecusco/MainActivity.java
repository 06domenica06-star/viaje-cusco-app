package pe.domenica.viajecusco;

import android.app.Activity;
import android.app.AlarmManager;
import android.provider.Settings;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
 private WebView web;
 private FrameLayout root;
 private View splashView;
 private long splashStartedAt;
 private ValueCallback<Uri[]> fileCallback;
 private String pendingText;
 private static final int IMPORT_FILE=40, EXPORT_FILE=41, NOTIFICATION_PERMISSION=42;
 private static final String HOST="appassets.androidplatform.net";
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  splashStartedAt=System.currentTimeMillis();
  root=new FrameLayout(this);
  web=new WebView(this);root.addView(web,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));web.setBackgroundColor(Color.rgb(247,248,243));
  splashView=createSplash();root.addView(splashView,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));setContentView(root);
  web.setOnApplyWindowInsetsListener((v,insets)->{
   if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}
   else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
   return insets;
  });
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
  settings.setAllowFileAccess(false);settings.setAllowContentAccess(true);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  settings.setSupportMultipleWindows(true);settings.setJavaScriptCanOpenWindowsAutomatically(false);
  WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return loader.shouldInterceptRequest(request.getUrl());}
   @Override public void onPageFinished(WebView view,String url){super.onPageFinished(view,url);hideSplashWhenReady();}
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri u=request.getUrl();if(isLocal(u))return false;openExternal(u);return true;}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onShowFileChooser(WebView view,ValueCallback<Uri[]> cb,FileChooserParams params){
    if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=cb;
    Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/json");
    try{startActivityForResult(intent,IMPORT_FILE);return true;}catch(Exception e){fileCallback=null;cb.onReceiveValue(null);return false;}
   }
   @Override public boolean onCreateWindow(WebView view,boolean dialog,boolean gesture,android.os.Message result){
    if(!gesture)return false;
    WebView popup=new WebView(MainActivity.this);
    popup.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(isLocal(u))web.loadUrl(u.toString());else openExternal(u);popup.destroy();return true;}});
    ((WebView.WebViewTransport)result.obj).setWebView(popup);result.sendToTarget();return true;
   }
  });
  web.addJavascriptInterface(new ExportBridge(),"NativeFiles");
  DailyCountdown.schedule(this);
  EventNotifications.schedule(this);
  web.loadUrl("https://"+HOST+"/assets/web/index.html");
 }
 private View createSplash(){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setBackgroundColor(Color.rgb(247,248,243));
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.logo);logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
  LinearLayout.LayoutParams logoParams=new LinearLayout.LayoutParams(dp(184),dp(184));logoParams.bottomMargin=dp(24);box.addView(logo,logoParams);
  RotateAnimation spin=new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,.5f,Animation.RELATIVE_TO_SELF,.5f);spin.setDuration(2200);spin.setRepeatCount(Animation.INFINITE);spin.setInterpolator(new LinearInterpolator());logo.startAnimation(spin);
  ProgressBar progress=new ProgressBar(this);progress.setIndeterminate(true);progress.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.rgb(37,75,64)));LinearLayout.LayoutParams progressParams=new LinearLayout.LayoutParams(dp(28),dp(28));box.addView(progress,progressParams);
  return box;
 }
 private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
 private void hideSplashWhenReady(){if(splashView==null)return;long wait=Math.max(0,650-(System.currentTimeMillis()-splashStartedAt));new Handler(Looper.getMainLooper()).postDelayed(()->{if(splashView==null)return;View old=splashView;splashView=null;old.animate().alpha(0f).setDuration(240).withEndAction(()->root.removeView(old)).start();},wait);}
 private boolean isLocal(Uri u){return "https".equals(u.getScheme())&&HOST.equals(u.getHost());}
 private void openExternal(Uri u){if(!"https".equals(u.getScheme())&&!"http".equals(u.getScheme()))return;try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){Toast.makeText(this,"No se pudo abrir el enlace",Toast.LENGTH_SHORT).show();}}
 private class ExportBridge {
  @JavascriptInterface public void setCountdown(boolean enabled,String date,String time){runOnUiThread(()->{if(!isLocal(Uri.parse(web.getUrl())))return;try{DailyCountdown.save(MainActivity.this,enabled,date,time);}catch(Exception ignored){}});}
  @JavascriptInterface public void setEvents(String json,String month){runOnUiThread(()->{if(!isLocal(Uri.parse(web.getUrl())))return;EventNotifications.update(MainActivity.this,json,month);});}
  @JavascriptInterface public void requestNotificationPermission(){runOnUiThread(()->{if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATION_PERMISSION);else{requestExactAlarmAccess();DailyCountdown.schedule(MainActivity.this);EventNotifications.schedule(MainActivity.this);Toast.makeText(MainActivity.this,"Avisos del viaje activados",Toast.LENGTH_SHORT).show();}});}
  @JavascriptInterface public void saveText(String text,String name,String type){runOnUiThread(()->{
   if(!isLocal(Uri.parse(web.getUrl())))return;
   if(pendingText!=null){Toast.makeText(MainActivity.this,"Termina de guardar el archivo anterior",Toast.LENGTH_SHORT).show();return;}
   pendingText=text;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType(type);intent.putExtra(Intent.EXTRA_TITLE,name);
   try{startActivityForResult(intent,EXPORT_FILE);}catch(Exception e){pendingText=null;Toast.makeText(MainActivity.this,"No se pudo abrir el selector de archivos",Toast.LENGTH_SHORT).show();}
  });}
 }
 private void requestExactAlarmAccess(){if(Build.VERSION.SDK_INT>=31){AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);if(am!=null&&!am.canScheduleExactAlarms()){try{Toast.makeText(this,"Permite Alarmas y recordatorios para avisos a la hora indicada",Toast.LENGTH_LONG).show();startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){}}}}
 @Override protected void onResume(){super.onResume();DailyCountdown.schedule(this);EventNotifications.schedule(this);}
 @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(request==NOTIFICATION_PERMISSION){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED){requestExactAlarmAccess();DailyCountdown.schedule(this);EventNotifications.schedule(this);Toast.makeText(this,"Avisos del viaje activados",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"Activa las notificaciones en Ajustes para recibir el aviso diario",Toast.LENGTH_LONG).show();}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);
  if(request==IMPORT_FILE&&fileCallback!=null){fileCallback.onReceiveValue(result==RESULT_OK&&data!=null?new Uri[]{data.getData()}:null);fileCallback=null;}
  if(request==EXPORT_FILE){if(result==RESULT_OK&&data!=null&&pendingText!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(pendingText.getBytes(StandardCharsets.UTF_8));Toast.makeText(this,"Archivo guardado",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"No se pudo guardar el archivo",Toast.LENGTH_LONG).show();}}pendingText=null;}
 }
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){if(fileCallback!=null)fileCallback.onReceiveValue(null);web.removeJavascriptInterface("NativeFiles");web.destroy();super.onDestroy();}
}
