package com.example.cec.whatsappstatus;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    boolean Flag = true;
    TextView info , Title;
    SharedPreferences SP;
    SharedPreferences.Editor ED;
    String StatusDirectory , WhastappStatuesDirectory;
    File WhastappStatuesFile , StatusFile ;
    private static final int REQUEST_ID_READ_PERMISSION = 100 , REQUEST_ID_WRITE_PERMISSION = 200 ;
    ImageView loader ;
    ProgressBar PB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SP = getSharedPreferences("Ibrahim",MODE_PRIVATE);
        ED = SP.edit();
        askPermission(REQUEST_ID_WRITE_PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        askPermission(REQUEST_ID_READ_PERMISSION,
                Manifest.permission.READ_EXTERNAL_STORAGE);
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
    public void CopyonStartUp()
    {
        if(Flag)
        {
            final File[] Media = StatusFile.listFiles();
            if(Media!=null)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PB.setMax(Media.length);
                    }
                });
                for(int i = 0 ; i < Media.length ; i++)
                {
                    try
                    {
                        Log.e("File is Nani :  ",Media[i].getName());
                        Log.e("Files Areee : ", "Fuck" +  SP.getString("names",""));
                        final  int I = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                info.setText("Copying : "+ '\n' + Media[I].getName());
                            }
                        });
                        if( !SP.getString("names","").contains(Media[i].getName()) && CheckExtenction(Extention(Media[i].getName())) )
                        //if(!new File(WhastappStatuesDirectory + "/" + Media[i].getName()).exists() && CheckExtenction(Extention(Media[i].getName())))
                        {
                            ED.putString("names",SP.getString("names","")+Media[i].getName());
                            ED.commit();
                            int ID = SP.getInt("ID",0);
                            ED.putInt("ID",ID+1);
                            ED.commit();
                            InputStream in = new FileInputStream(StatusDirectory + "/" + Media[i].getName());
                            OutputStream out = new FileOutputStream(WhastappStatuesDirectory + "/" + ID  + Extention(Media[i].getName()));
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            in.close();
                            out.close();
                            //if(i == Media.length - 1 ){
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(new File(WhastappStatuesDirectory + "/" + ID  + Extention(Media[i].getName())));
                            mediaScanIntent.setData(contentUri);
                            getApplicationContext().sendBroadcast(mediaScanIntent);//}
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                PB.setProgress(I+1);
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.e("Ex ","Some Fucking Exeption");
                    }
                }
            }
        }
    }
    public boolean checkServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ( (this.getApplicationContext().getPackageName()+".StatusSurvice")
                    .equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Activity"," Destroid");
            Log.e("Starting"," Service");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        if(checkServiceRunning())
                            stopService(new Intent(MainActivity.this,StatusSurvice.class));
                        startService(new Intent(MainActivity.this,StatusSurvice.class));
                }
            });
    }
    public void PermissionGaranteed()
    {
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
        PB = (ProgressBar) findViewById(R.id.progressBar);
        PB.getProgressDrawable().setColorFilter(
                Color.rgb(26,111,60), android.graphics.PorterDuff.Mode.SRC_IN);
        info = (TextView) findViewById(R.id.info);
        Title = (TextView) findViewById(R.id.Title);
        if(Flag){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CopyonStartUp();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loader.setScaleX(1.0f);
                        Animation anim = AnimationUtils.loadAnimation(MainActivity.this,R.anim.scale);
                        anim.setInterpolator(new LinearInterpolator());
                        loader.setAnimation(anim);
                        Title.setText("Your Statues Are Up to Date ✓ ");
                        info.setText("Done !");
                    }
                });
            }
        }).start();
        if(!checkServiceRunning())
        {
            Log.e("Note : ","Survice Started From Activity");
            //Intent in = new Intent(MainActivity.this,StatusSurvice.class);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    startService(new Intent(MainActivity.this,StatusSurvice.class));
                }
            });
        }
        loader = (ImageView) findViewById(R.id.loader);
        Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
        anim.setInterpolator(new LinearInterpolator());
        loader.startAnimation(anim);}
        else
        {
            Title.setText("Couldn't Find WhatsApp Statues File");
            info.setText("Error");
        }
    }
    private boolean askPermission(int requestId, String permissionName) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            // Check if we have permission
            int permission = ActivityCompat.checkSelfPermission(this, permissionName);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{permissionName},
                        requestId
                );
                return false;
            }
        }
        PermissionGaranteed();
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED || (
                grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ))
        {
            PermissionGaranteed();
        }
    }
}
