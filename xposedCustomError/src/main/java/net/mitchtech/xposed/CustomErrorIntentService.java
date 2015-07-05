package net.mitchtech.xposed;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

public class CustomErrorIntentService extends IntentService {

    private static final String TAG = CustomErrorIntentService.class.getSimpleName();
    private SharedPreferences mPrefs;
        
    public CustomErrorIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());  
        String prefSoundFile = mPrefs.getString("prefSoundFile", "prefSoundFile");
        Log.i(TAG, prefSoundFile);
        playSound(prefSoundFile);
    }
    
    public void playSound(String path) {
        File mp3File = new File(path);
        Uri mp3Uri = Uri.fromFile(mp3File);
        MediaPlayer mediaPlayer = MediaPlayer.create(this, mp3Uri);
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                
            }
        });
        mediaPlayer.start();
    }

}
