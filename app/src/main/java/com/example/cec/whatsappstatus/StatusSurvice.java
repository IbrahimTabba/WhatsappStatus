package com.example.cec.whatsappstatus;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by CEC on 18-Sep-18.
 */

public class StatusSurvice extends Service {
    String StatusDirectory , WhastappStatuesDirectory;
    File WhastappStatuesFile , StatusFile ;
    boolean Flag = true;
    FileObserver Listener ;
    SharedPreferences SP;
    SharedPreferences.Editor ED;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("Survice ","Created");
        SP = getSharedPreferences("Ibrahim",MODE_PRIVATE);
        ED = SP.edit();
        StatusDirectory = Environment.getExternalStorageDirectory().toString()+"/WhatsApp/Media/.Statuses";
        WhastappStatuesDirectory = Environment.getExternalStorageDirectory().toString()+"/WhatsappStatues";
        WhastappStatuesFile = new File(WhastappStatuesDirectory);
        StatusFile = new File(StatusDirectory);
        if(!WhastappStatuesFile.exists())
        {
            WhastappStatuesFile.mkdir();
        }
        if(!WhastappStatuesFile.exists() || !StatusFile.exists() )
            Flag = false;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Toast.makeText(this,"Survice Started",Toast.LENGTH_SHORT).show();
        if(Flag)
        {
            Listener = new FileObserver(StatusDirectory) {
                @Override
                public void onEvent(int i, String s) {
                    //Log.e("Type :",i + " " + s);
                    if(i == FileObserver.CLOSE_WRITE || i==FileObserver.CREATE || i == FileObserver.MODIFY || i==FileObserver.MOVED_TO)
                    {
                        if(!SP.getString("names","").contains(s) && CheckExtenction(Extention(s)))
                        try
                        {
                            Log.e("File : ",s);
                            ED.putString("names",SP.getString("names","")+s);
                            ED.commit();
                            int ID = SP.getInt("ID",0);
                            ED.putInt("ID",ID+1);
                            ED.commit();
                            InputStream in = new FileInputStream(StatusDirectory + "/" + s);
                            OutputStream out = new FileOutputStream(WhastappStatuesDirectory + "/" + ID + Extention(s) );
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            in.close();
                            out.close();
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(new File(WhastappStatuesDirectory + "/" + ID  + Extention(s)));
                            mediaScanIntent.setData(contentUri);
                            getApplicationContext().sendBroadcast(mediaScanIntent);
                        }
                        catch (Exception e)
                        {

                        }
                    }
                }
            };
            Listener.startWatching();

        }
        return super.onStartCommand(intent, flags, startId);
    }
    public String Extention(String name)
    {
        String Res = "";
        int dotidx = -1;
        for(int i = name.length()-1 ; i >=0 ; i--)
        {
            if(name.charAt(i)=='.')
            {
                dotidx = i;
                break;
            }
        }
        if(dotidx==-1)
            return null;
        for(int i = dotidx ; i < name.length() ; i++)
            Res+=name.charAt(i);
        return  Res;
    }
    public boolean CheckExtenction(String EX)
    {
        return EX!=null && (EX.toLowerCase().equals(".jpg") || EX.toLowerCase().equals(".mp4"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //sendBroadcast(new Intent("android.intent.action.BOOT_COMPLETED"));
    }
}
