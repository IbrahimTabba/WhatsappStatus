package com.example.cec.whatsappstatus;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by CEC on 18-Sep-18.
 */

public class BootReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!checkServiceRunning(context)){
        Intent in = new Intent(context,StatusSurvice.class);
        context.startService(in);}
    }
    public boolean checkServiceRunning(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ((context.getPackageName()+".StatusSurvice")
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}
