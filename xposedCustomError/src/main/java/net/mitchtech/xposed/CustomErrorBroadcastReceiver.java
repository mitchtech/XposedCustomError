
package net.mitchtech.xposed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CustomErrorBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = CustomErrorBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        intent = new Intent(context, CustomErrorIntentService.class);
        context.startService(intent);
    }
    
}
