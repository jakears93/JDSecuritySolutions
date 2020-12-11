package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ChooseConfig extends BottomNavigationInflater {

    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;
    private BottomNavigationView bottomNavigationView;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.choose_config);
            super.createNavListener();
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.choose_config_landscape);
            super.createNavListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
    }


    public void clickedViewer(View v){
        if(checkViewerPermissions() ==2){
            Intent intent = new Intent(ChooseConfig.this, ViewerMenu.class);
            //Saved device type to shared pref (1 being viewer)
            editor.putInt("Device", 1);
            editor.commit();
            startActivity(intent);
        }
    }

    public void clickedCamera(View v){
        if(checkRecorderPermissions()==4){
            Intent intent = new Intent(ChooseConfig.this, CameraDevice.class);
            //Saved device type to shared pref (2 being camera)
            editor.putInt("Device", 2);
            editor.commit();
            startActivity(intent);
        }
    }


    public int checkViewerPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
        int approvedPermissions = 0;
        for(int i=0; i<permissions.length; i++){
            if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 10);
            }
            if (ActivityCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                approvedPermissions++;
            }
        }
        return approvedPermissions;
    }

    public int checkRecorderPermissions() {
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
        return approvedPermissions;
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_logout_title)
                .setMessage(R.string.alert_logout_message)

                .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ChooseConfig.super.onBackPressed();
                        editor.clear();
                        editor.commit();
                        Intent intent = new Intent(ChooseConfig.this, LoginAndRegister.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.alert_cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
