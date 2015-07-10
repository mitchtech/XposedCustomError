
package net.mitchtech.xposed;

import android.content.Context;
import android.content.Intent;
import android.content.res.XResources;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

public class XposedCustomError implements IXposedHookZygoteInit {

    private static final String TAG = XposedCustomError.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.customerror";
    
    private static final String AERR_DEFAULT = "Unfortunately, %1$s has stopped.";
    private static final String ANR_DEFAULT = "%1$s isn\'t responding.\n\nDo you want to close it?";
    
    private static String MODULE_PATH = null;

    private XSharedPreferences prefs;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        loadPrefs();
        
        // set custom error message if enabled
        if (isEnabled("prefEnableAppError")) {
            // String prefAppErrorMsg = "He's dead, Jim! \n\n (%1$s)";
            // String prefAppErrorMsg = "I'm sorry Dave, I'm afraid I can't do that."
            // String prefAppErrorMsg = "Ah ah ah you didn't say the magic word!";
            
            String prefAppErrorMsg = prefs.getString("prefAppErrorMsg", AERR_DEFAULT);
            // XposedBridge.log(TAG + " prefAppErrorMsg: " + prefAppErrorMsg);

            // com.android.internal.R.string.aerr_application
            XResources.setSystemWideReplacement("android", "string", "aerr_application", prefAppErrorMsg);
    
            // com.android.internal.R.string.aerr_process
            XResources.setSystemWideReplacement("android", "string", "aerr_process", prefAppErrorMsg);
        }
        
        // set custom anr message if enabled
        if (isEnabled("prefEnableAnrError")) {
            String prefAnrErrorMsg = prefs.getString("prefAnrErrorMsg", ANR_DEFAULT);
            // XposedBridge.log(TAG + " prefAnrErrorMsg: " + prefAnrErrorMsg);
            
            // com.android.internal.R.string.anr_activity_application
            XResources.setSystemWideReplacement("android", "string", "anr_activity_application", prefAnrErrorMsg);
            
            // com.android.internal.R.string.anr_activity_process
            XResources.setSystemWideReplacement("android", "string", "anr_activity_process", prefAnrErrorMsg);
            
            // com.android.internal.R.string.anr_application_process
            XResources.setSystemWideReplacement("android", "string", "anr_application_process", prefAnrErrorMsg);
            
            // com.android.internal.R.string.anr_process
            XResources.setSystemWideReplacement("android", "string", "anr_process", prefAnrErrorMsg);
        }
        
        // hook anr & error dialog constructors if sound effects enabled
        if (isEnabled("prefEnableSoundFx")) {
            
            // AppErrorDialog(Context context, ActivityManagerService service,
            // AppErrorResult result, ProcessRecord app);
            findAndHookConstructor("com.android.server.am.AppErrorDialog", null, Context.class, 
                    "com.android.server.am.ActivityManagerService", 
                    "com.android.server.am.AppErrorResult", 
                    "com.android.server.am.ProcessRecord", 
                    new XC_MethodHook() {
                
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    Context context = (Context) methodHookParam.args[0];
                    // XposedBridge.log(TAG + ": " + context.getPackageName());
                    Intent intent = new Intent("net.mitchtech.xposed.ERROR");
                    context.sendBroadcast(intent);          
                }
            });
            
            // AppNotRespondingDialog(ActivityManagerService service, Context context,
            //        ProcessRecord app, ActivityRecord activity, boolean aboveSystem)
            findAndHookConstructor("com.android.server.am.AppNotRespondingDialog", null, 
                    "com.android.server.am.ActivityManagerService", 
                    Context.class, 
                    "com.android.server.am.ProcessRecord", 
                    "com.android.server.am.ActivityRecord", 
                    boolean.class,
                    new XC_MethodHook() {
                
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    Context context = (Context) methodHookParam.args[1];
                    // XposedBridge.log(TAG + ": " + context.getPackageName());
                    Intent intent = new Intent("net.mitchtech.xposed.ERROR");
                    context.sendBroadcast(intent);          
                }
            });
        }
    }

    private boolean isEnabled(String pkgName) {
        prefs.reload();
        return prefs.getBoolean(pkgName, false);
    }

    private void loadPrefs() {
        prefs = new XSharedPreferences(PKG_NAME);
        prefs.makeWorldReadable();
        XposedBridge.log(TAG + ": prefs loaded.");
    }
    
}
