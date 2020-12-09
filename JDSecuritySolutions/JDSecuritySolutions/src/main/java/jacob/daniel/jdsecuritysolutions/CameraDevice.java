package jacob.daniel.jdsecuritysolutions;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//TODO reorganize code structure
//TODO change vidcount to value from preferences

public class CameraDevice extends BottomNavigationInflater {

    private boolean allowRecord = false;
    EditText room;
    SwitchCompat toggle;
    SurfaceView screen;
    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;
    RecordingManager recordManager;

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
    }

    public void flippedSwitch(View v) {
        if(toggle.isChecked()){
            checkPermissions();
            if(allowRecord) {
                //create callable, exit function
                ExecutorService executor = Executors.newFixedThreadPool(1);
                recordManager = new RecordingManager(getApplicationContext(), screen, room, allowRecord);
                executor.submit(recordManager);
            }
        }
        else if(!toggle.isChecked()){
            recordManager.setAllowRecord(false);
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
                        finish();
                    }
                })
                .setNegativeButton(R.string.alert_cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}//end of parent class