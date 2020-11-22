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


//TODO add surfaceview to layout
//TODO reorganize code structure
//TODO change vidcount to value from preferences

public class CameraDevice extends BottomNavigationInflater {

    private boolean allowRecord = false;
    EditText room;
    SwitchCompat toggle;
    String fileName;
    VideoView screen;
    int vidCount = 0;
    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;


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

    //TODO look into AsyncTask
    public void flippedSwitch(View v) {
        if(toggle.isChecked()){
            checkPermissions();
            while(allowRecord && vidCount < 5) {
                Thread recording = new Thread(new StartRecording());
                recording.start();
                   try {
                       recording.join();
                   } catch (InterruptedException ex) {
                       ex.printStackTrace();
                   }
            }
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

    public class StartRecording implements Runnable{
        StartRecording(){}

        //TODO enable preview
        public void record(){
            SurfaceTexture sft = new SurfaceTexture(0);
            Surface sf = new Surface(sft);
            MediaRecorder recorder = new MediaRecorder();
            fileName = getFilePath();
            File fp = getFilePath2();
            vidCount++;
            recorder.setPreviewDisplay(sf);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOrientationHint(90);
            recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Log.println(Log.INFO, "FileType", "Updated");
                recorder.setOutputFile(fp);
            }
            else{
                Log.println(Log.INFO, "FileType", "Legacy");
                recorder.setOutputFile(fileName);
            }
            recorder.setMaxDuration(3000);

            //TODO explore max file size instead of max duration?
 /*           recorder.setMaxFileSize(1000000);
            recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING ) {
                        Log.println(Log.INFO, "recorder", "stopping");
                        mr.stop();
                        mr.release();
                    }
                }
            });*/

            try {
                recorder.prepare();
                recorder.start();

            }catch(Exception ex){
                ex.printStackTrace();
                Toast toast=Toast.makeText(getApplicationContext(),getResources().getString(R.string.RecordFail),Toast.LENGTH_SHORT);
                toast.show();
            }
            try {
                Thread.sleep(3000);
            }
            catch(InterruptedException ex){
                ex.printStackTrace();
            }

        }

        public String getFilePath(){
            String roomName = room.getText().toString().replaceAll("\\s+", "");

            String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+vidCount+".mp4";

            File fp = new File(filePath);
            return filePath;
        }

        public File getFilePath2(){
            String roomName = room.getText().toString().replaceAll("\\s+", "");

            String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+vidCount+".mp4";

            File fp = new File(filePath);
            return fp;
        }

        //TODO implement db storage
        public void storeVideoToFirebase(File video){

        }

        public void run(){
            Looper.prepare();
            record();
        }

    }//end of record class

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