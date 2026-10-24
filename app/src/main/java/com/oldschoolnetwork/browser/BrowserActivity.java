package com.oldschoolnetwork.browser;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.net.*;
import android.net.http.SslError;
import android.os.*;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.inputmethod.*;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** One live WebView; inactive tabs retain bounded navigation state, not renderers. */
public final class BrowserActivity extends Activity {
    private final ArrayList<Bundle> tabs=new ArrayList<Bundle>();
    private final ArrayList<String> titles=new ArrayList<String>();
    private int active=-1;private WebView web;private EditText address;private ProgressBar progress;private TextView status;
    private String normalAgent;private String[] pendingDownload;
    private android.content.SharedPreferences settings;
    public void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.browser);settings=PreferenceManager.getDefaultSharedPreferences(this);
        address=(EditText)findViewById(R.id.address);progress=(ProgressBar)findViewById(R.id.progress);status=(TextView)findViewById(R.id.status);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener(){public void onClick(View v){goBack();}});
        findViewById(R.id.tabs).setOnClickListener(new View.OnClickListener(){public void onClick(View v){showTabs();}});
        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener(){public void onClick(View v){showMenu(v);}});
        address.setOnEditorActionListener(new TextView.OnEditorActionListener(){public boolean onEditorAction(TextView v,int action,KeyEvent e){if(action==EditorInfo.IME_ACTION_GO || (e!=null&&e.getKeyCode()==KeyEvent.KEYCODE_ENTER)){navigate(address.getText().toString());((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);web.requestFocus();return true;}return false;}});
        if(state!=null){int count=Math.min(state.getInt("count",0),6);for(int i=0;i<count;i++){tabs.add(state.getBundle("tab"+i));titles.add(state.getString("title"+i,"New tab"));}if(count>0)switchTab(Math.max(0,Math.min(state.getInt("active",0),count-1)));}
        if(web==null)newTab(getIntent().getDataString()==null?"https://oldschoolnetwork.online/":getIntent().getDataString());
    }
    protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);if(intent.getDataString()!=null)newTab(intent.getDataString());}
    private void configure(){
        WebSettings s=web.getSettings();if(normalAgent==null)normalAgent=s.getUserAgentString();
        s.setJavaScriptEnabled(settings.getBoolean("javascript",true));s.setLoadsImagesAutomatically(settings.getBoolean("images",true));s.setBlockNetworkImage(!settings.getBoolean("images",true));
        s.setDomStorageEnabled(settings.getBoolean("storage",true));s.setDatabaseEnabled(false);s.setGeolocationEnabled(false);
        s.setAllowFileAccess(false);s.setSupportMultipleWindows(false);s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setBuiltInZoomControls(true);s.setDisplayZoomControls(false);s.setUseWideViewPort(true);s.setLoadWithOverviewMode(true);
        s.setUserAgentString(settings.getBoolean("desktop",false)?normalAgent.replace("Mobile","").replace("Android","Linux"):normalAgent);
        CookieManager.getInstance().setAcceptCookie(settings.getBoolean("cookies",true));
        if(Build.VERSION.SDK_INT>=21)Api21.configure(web,settings.getBoolean("third_party",false));
        if(Build.VERSION.SDK_INT>=16)Api16.configure(s);
    }
    private static final class Api16 {static void configure(WebSettings s){s.setAllowFileAccessFromFileURLs(false);s.setAllowUniversalAccessFromFileURLs(false);}}
    private static final class Api21 {static void configure(WebView v,boolean cookies){CookieManager.getInstance().setAcceptThirdPartyCookies(v,cookies);v.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);}}
    private static final class Api23 {static boolean storage(Activity a){return a.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")==android.content.pm.PackageManager.PERMISSION_GRANTED;}static void ask(Activity a){a.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},42);}}
    protected void onResume(){super.onResume();if(web!=null){configure();web.onResume();}}
    protected void onPause(){if(web!=null)web.onPause();super.onPause();}
    private void saveTab(){if(web!=null&&active>=0){Bundle b=new Bundle();web.saveState(b);tabs.set(active,b);titles.set(active,web.getTitle()==null?"New tab":web.getTitle());}}
    private void switchTab(int index){
        saveTab();if(web!=null){((FrameLayout)findViewById(R.id.page)).removeView(web);web.stopLoading();web.destroy();}
        active=index;web=(WebView)getLayoutInflater().inflate(R.layout.browser_page,(FrameLayout)findViewById(R.id.page),false);
        ((FrameLayout)findViewById(R.id.page)).addView(web);configure();
        web.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView v,String url){if(isWeb(url))return false;notice("This link uses an unsupported address type.");return true;}
            public void onPageStarted(WebView v,String url,Bitmap icon){address.setText(url);status.setVisibility(View.GONE);progress.setVisibility(View.VISIBLE);}
            public void onPageFinished(WebView v,String url){progress.setVisibility(View.GONE);titles.set(active,v.getTitle()==null?url:v.getTitle());if(settings.getBoolean("history",true)&&isWeb(url))remember("history",url,titles.get(active),100);}
            public void onReceivedError(WebView v,int code,String text,String url){notice("Page could not load. "+text);progress.setVisibility(View.GONE);}
            public void onReceivedSslError(WebView v,SslErrorHandler handler,SslError error){handler.cancel();notice("The secure connection could not be verified. Check the device date and certificate trust.");}
        });
        web.setWebChromeClient(new WebChromeClient(){public void onProgressChanged(WebView v,int n){progress.setProgress(n);}});
        web.setDownloadListener(new DownloadListener(){public void onDownloadStart(String url,String agent,String disposition,String mime,long size){download(url,agent,disposition,mime);}});
        Bundle b=tabs.get(index);if(b==null||web.restoreState(b)==null)web.loadUrl("https://oldschoolnetwork.online/");
        ((Button)findViewById(R.id.tabs)).setText(String.valueOf(tabs.size()));
    }
    private void newTab(String url){if(tabs.size()>=6){notice("Close a tab before opening another. Up to 6 tabs are available.");return;}saveTab();tabs.add(null);titles.add("New tab");switchTab(tabs.size()-1);navigate(url);}
    private static boolean isWeb(String url){return url!=null&&(url.startsWith("https://")||url.startsWith("http://"));}
    private void navigate(String text){
        String url=text.trim();if(url.length()==0)return;
        if(!isWeb(url)){
            if(url.indexOf(' ')<0&&url.indexOf('.')>0&&url.indexOf(':')<0)url="https://"+url;
            else if(url.indexOf(':')>=0){notice("Only HTTP and HTTPS addresses can be opened.");return;}
            else url="https://duckduckgo.com/?q="+Uri.encode(url);
        }web.loadUrl(url);
    }
    private void notice(String text){status.setText(text);status.setVisibility(View.VISIBLE);}
    private void goBack(){if(web!=null&&web.canGoBack())web.goBack();else if(tabs.size()>1)closeTab();else finish();}
    public void onBackPressed(){goBack();}
    private void closeTab(){if(tabs.size()==1){web.loadUrl("https://oldschoolnetwork.online/");return;}int previous=active;saveTab();tabs.remove(previous);titles.remove(previous);active=-1;switchTab(Math.min(previous,tabs.size()-1));}
    private void showTabs(){saveTab();new AlertDialog.Builder(this).setTitle("Tabs").setItems(titles.toArray(new String[titles.size()]),new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int n){switchTab(n);}}).setPositiveButton("New tab",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int n){newTab("https://oldschoolnetwork.online/");}}).setNeutralButton("Close current",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int n){closeTab();}}).setNegativeButton("Cancel",null).show();}
    private void showMenu(View anchor){PopupMenu menu=new PopupMenu(this,anchor);String[] items={"Forward","Reload","Add bookmark","Bookmarks","History","Downloads","Settings","Page information"};for(int i=0;i<items.length;i++)menu.getMenu().add(0,i,i,items[i]);menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){public boolean onMenuItemClick(MenuItem item){switch(item.getItemId()){
        case 0:if(web.canGoForward())web.goForward();break;case 1:web.reload();break;
        case 2:if(isWeb(web.getUrl())){remember("bookmarks",web.getUrl(),web.getTitle(),100);Toast.makeText(BrowserActivity.this,"Bookmark saved",Toast.LENGTH_SHORT).show();}break;
        case 3:showSaved("bookmarks");break;case 4:showSaved("history");break;
        case 5:try{startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));}catch(ActivityNotFoundException e){notice("The system download list is unavailable.");}break;
        case 6:startActivity(new Intent(BrowserActivity.this,BrowserSettingsActivity.class));break;
        case 7:new AlertDialog.Builder(BrowserActivity.this).setTitle("Page information").setMessage("Address: "+web.getUrl()+"\n\nRenderer: system WebView\n"+normalAgent+"\n\nPage capabilities depend on this device's engine.").setPositiveButton("Close",null).show();break;
    }return true;}});menu.show();}
    private JSONArray saved(String key){try{return new JSONArray(getSharedPreferences("browser_data",0).getString(key,"[]"));}catch(JSONException e){return new JSONArray();}}
    private void remember(String key,String url,String title,int limit){try{JSONArray old=saved(key),list=new JSONArray();list.put(new JSONObject().put("url",url).put("title",title==null?url:title));for(int i=0;i<old.length()&&list.length()<limit;i++)if(!url.equals(old.getJSONObject(i).optString("url")))list.put(old.getJSONObject(i));getSharedPreferences("browser_data",0).edit().putString(key,list.toString()).apply();}catch(JSONException ignored){}}
    private void showSaved(String key){final JSONArray list=saved(key);String[] names=new String[list.length()];for(int i=0;i<names.length;i++)names[i]=list.optJSONObject(i).optString("title");new AlertDialog.Builder(this).setTitle(key.equals("history")?"History":"Bookmarks").setItems(names,new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int n){navigate(list.optJSONObject(n).optString("url"));}}).setNegativeButton("Close",null).show();}
    private void download(final String url,String agent,String disposition,String mime){
        if(!isWeb(url)){notice("Only HTTP and HTTPS downloads are supported.");return;}
        if(Build.VERSION.SDK_INT>=23&&!Api23.storage(this)){pendingDownload=new String[]{url,agent,disposition,mime};Api23.ask(this);return;}
        try{DownloadManager.Request r=new DownloadManager.Request(Uri.parse(url));String name=URLUtil.guessFileName(url,disposition,mime).replaceAll("[^A-Za-z0-9._ -]","_");if(name.length()>100)name=name.substring(name.length()-100);r.setTitle(name);if(mime!=null)r.setMimeType(mime);String cookie=CookieManager.getInstance().getCookie(url);if(cookie!=null)r.addRequestHeader("Cookie",cookie);r.addRequestHeader("User-Agent",agent==null?normalAgent:agent);r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,name);r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);Toast.makeText(this,"Download started",Toast.LENGTH_SHORT).show();}catch(Exception e){notice("Download could not start. "+e.getMessage());}
    }
    public void onRequestPermissionsResult(int request,String[] permissions,int[] results){if(request==42&&pendingDownload!=null){String[] d=pendingDownload;pendingDownload=null;if(results.length>0&&results[0]==android.content.pm.PackageManager.PERMISSION_GRANTED)download(d[0],d[1],d[2],d[3]);else notice("Storage permission is required to save downloads.");}}
    protected void onSaveInstanceState(Bundle out){saveTab();out.putInt("count",tabs.size());out.putInt("active",active);for(int i=0;i<tabs.size();i++){out.putBundle("tab"+i,tabs.get(i));out.putString("title"+i,titles.get(i));}super.onSaveInstanceState(out);}
    protected void onDestroy(){if(web!=null){web.stopLoading();((FrameLayout)findViewById(R.id.page)).removeView(web);web.destroy();web=null;}super.onDestroy();}
}
