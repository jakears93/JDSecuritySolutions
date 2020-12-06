package jacob.daniel.jdsecuritysolutions;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


//TODO add surfaceview to layout
//TODO reorganize code structure
//TODO change vidcount to value from preferences

public class CameraDevice extends BottomNavigationInflater {

    private boolean allowRecord = false;
    EditText room;
    SwitchCompat toggle;
    VideoView screen;
    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;
    RecordingManager recordManager;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.camera_device);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.camera_device_landscape);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
        super.createNavListener();
        room = findViewById(R.id.RoomName);
        toggle = findViewById(R.id.toggle);
        screen = findViewById(R.id.video);
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
    }

    private void recordCamera(){

    }

    //TODO look into AsyncTask
    public void flippedSwitch(View v) {
        if(toggle.isChecked()){
            checkPermissions();
            if(allowRecord) {
                //create callable, exit function
                ExecutorService executor = Executors.newFixedThreadPool(1);
                recordManager = new RecordingManager(getApplicationContext(), room, allowRecord);
                executor.submit(recordManager);
            }
        }
        else if(!toggle.isChecked()){
            recordManager.setAllowRecord(false);
        }
    }

    public void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, 10);
        }
        else{
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_logout_title)
                .setMessage(R.string.alert_logout_message)

                .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.remove("User");
                        editor.remove("Pass");
                        editor.remove("LoggedIn");
                        editor.commit();
                        Intent intent = new Intent(CameraDevice.this, LoginAndRegister.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.alert_cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}//end of parent class