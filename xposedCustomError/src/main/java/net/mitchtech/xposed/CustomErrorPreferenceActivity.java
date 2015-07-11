
package net.mitchtech.xposed;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.tsengvn.typekit.TypekitContextWrapper;

import net.mitchtech.xposed.customerror.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomErrorPreferenceActivity extends AppCompatActivity {

    private static final String TAG = CustomErrorPreferenceActivity.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.customerror";
    private static final int REQUEST_CODE = 6384; // onActivityResult request

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
//        addPreferencesFromResource(R.xml.settings);

        try {
            String path = Environment.getExternalStorageDirectory() + "/soundfx";
            File dir = new File(path);
            if (dir.mkdirs() || dir.isDirectory()) {
                CopyRAWtoSDCard(R.raw.hes_dead_jim, path + File.separator + "hes_dead_jim.wav");
                CopyRAWtoSDCard(R.raw.ah_ah_ah, path + File.separator + "ah_ah_ah.mp3");
                CopyRAWtoSDCard(R.raw.sorry_dave, path + File.separator + "sorry_dave.wav");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }


    public class SettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.settings);

            findPreference("prefSoundFile").setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            showChooser();
                            return false;
                        }
                    });
            setSoundFxPreferenceSummary();

            findPreference("prefTestSound").setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent("net.mitchtech.xposed.ERROR");
                            sendBroadcast(intent);
                            return false;
                        }
                    });

            findPreference("prefTestCrash").setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent("net.mitchtech.xposed.ERROR");
                            sendBroadcast(intent);
                            // throw new RuntimeException("This is a crash");
                            int x = 2 / 0;
                            return false;
                        }
                    });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.contentEquals("prefSoundFile")) {
                setSoundFxPreferenceSummary();
            }
        }

        private void setSoundFxPreferenceSummary() {
            Preference soundFile = findPreference("prefSoundFile");
            soundFile.setSummary(getPreferenceScreen()
                    .getSharedPreferences().getString("prefSoundFile", "< select audio file >"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_exit:
                this.finish();
                return true;
        }

        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        target.setType(FileUtils.MIME_TYPE_AUDIO);
        Intent intent = Intent.createChooser(target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        // Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("prefSoundFile", path);
                            editor.commit();
//                            SettingsFragment.setSoundFxPreferenceSummary();
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getVersion(Context context) {
        String version = "1.0";
        try {
            PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }

    private void CopyRAWtoSDCard(int id, String path) throws IOException {
        InputStream in = getResources().openRawResource(id);
        FileOutputStream out = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }
}
