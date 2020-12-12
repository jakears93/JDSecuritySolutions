package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraDevice extends BottomNavigationInflater {

    private boolean allowRecord = false;
    EditText room;
    SwitchCompat toggle;
    SurfaceView screen;
    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;
    RecordingManager recordManager;
    private NotificationManagerCompat notificationManager;
    private String PRIMARY_CHANNEL_ID;

    //Change layout based on orientation
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.camera_device);
            super.createNavListener();
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.camera_device_landscape);
            super.createNavListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
        room = findViewById(R.id.RoomName);
        toggle = findViewById(R.id.toggle);
        screen = findViewById(R.id.video);

        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
        //Create channel id and initialize notification manager
        PRIMARY_CHANNEL_ID = getResources().getString(R.string.channel_id);
        notificationManager = NotificationManagerCompat.from(this);
    }

    public void flippedSwitch(View v) {
        if(toggle.isChecked()){
            checkPermissions();
            if(allowRecord) {
                //create callable, exit function
                ExecutorService executor = Executors.newFixedThreadPool(1);
                recordManager = new RecordingManager(getApplicationContext(), screen, room, allowRecord);
                executor.submit(recordManager);
                //Turn on notification
                createMyNotificationChannel();
                Notification notif = new NotificationCompat.Builder(CameraDevice.this, PRIMARY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.jd_logo_small)
                        .setContentTitle(getResources().getString(R.string.channel_name))
                        .setContentText(getResources().getString(R.string.notif_start))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setOngoing(true)
                        .build();

                notificationManager.notify(1,notif);
            }
        }
        else if(!toggle.isChecked()){
            recordManager.setAllowRecord(false);
            //Turn off notification
            Notification notif2 = new NotificationCompat.Builder(CameraDevice.this, PRIMARY_CHANNEL_ID)
                    .setSmallIcon(R.drawable.jd_logo_small)
                    .setContentTitle(getResources().getString(R.string.channel_name))
                    .setContentText(getResources().getString(R.string.notif_end))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(1,notif2);
        }
    }

    public void checkPermissions() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
        int approvedPermissions = 0;
        for(int i=0; i<permissions.length; i++){
            if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 10);
            }
            if (ActivityCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                approvedPermissions++;
            }
        }
        if(approvedPermissions == 4){
            allowRecord = true;
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                allowRecord = true;
            }else{
                Toast toast = Toast.makeText(getApplicationContext(),getResources().getString(R.string.FailedPermission), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onBackPressed(){
        if(recordManager!= null){
            recordManager.setAllowRecord(false);
        }
        notificationManager.cancelAll();
        Intent intent = new Intent(CameraDevice.this, ChooseConfig.class);
        startActivity(intent);
        finish();
    }

    private void createMyNotificationChannel(){
        //Create a notification channel in order to show notification on Android Oreo and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(PRIMARY_CHANNEL_ID, getResources().getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription(getResources().getString(R.string.channel_description));
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}//end of parent class