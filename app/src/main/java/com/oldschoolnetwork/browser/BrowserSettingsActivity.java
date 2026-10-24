package com.oldschoolnetwork.browser;
import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.webkit.*;

public final class BrowserSettingsActivity extends PreferenceActivity {
    public void onCreate(Bundle state){
        super.onCreate(state);addPreferencesFromResource(R.xml.settings);
        findPreference("third_party").setEnabled(Build.VERSION.SDK_INT>=21);
        findPreference("clear_history").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){public boolean onPreferenceClick(Preference p){getSharedPreferences("browser_data",0).edit().remove("history").apply();return true;}});
        findPreference("clear_data").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){public boolean onPreferenceClick(Preference p){
            new AlertDialog.Builder(BrowserSettingsActivity.this).setTitle("Clear website data?").setMessage("This signs you out of websites. Bookmarks are kept.").setNegativeButton("Cancel",null).setPositiveButton("Clear",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int which){
                WebView view=new WebView(BrowserSettingsActivity.this);view.clearCache(true);view.clearFormData();view.destroy();CookieManager.getInstance().removeAllCookie();WebStorage.getInstance().deleteAllData();
            }}).show();return true;
        }});
        findPreference("help").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){public boolean onPreferenceClick(Preference p){startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://oldschoolnetwork.online/downloads/docs/")));return true;}});
    }
}
