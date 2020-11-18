package jacob.daniel.jdsecuritysolutions;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

//TODO change video orientation
//TODO add surfaceview to layout
//TODO reorganize code structure
//TODO change vidcount to value from preferences

public class CameraDevice extends AppCompatActivity {

    private static final int MAX_PREVIEW_WIDTH = 1280;
    private static final int MAX_PREVIEW_HEIGHT = 720;
    private boolean allowRecord = false;
    private MediaRecorder recorder;
    SurfaceTexture sft = new SurfaceTexture(0);
    Surface sf = new Surface(sft);
    EditText room;
    SwitchCompat toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_device);
        room = findViewById(R.id.RoomName);
        toggle = findViewById(R.id.toggle);
    }

    public void flippedSwitch(View v) {
        if(toggle.isChecked()){
            checkPermissions();
            if(allowRecord) {
                Toast toast = Toast.makeText(getApplicationContext(), "Recording", Toast.LENGTH_SHORT);
                toast.show();
                Thread recording = new Thread(new StartRecording(recorder));
                recording.start();
                try {
                    recording.join();
                    toast = Toast.makeText(getApplicationContext(), "Done Recording", Toast.LENGTH_SHORT);
                    toast.show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
                Toast toast = Toast.makeText(getApplicationContext(), "FAILED GAINING PERMISSIONS", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public class StartRecording implements Runnable{
        private MediaRecorder recorder;

        StartRecording(MediaRecorder r){
            this.recorder = r;
        }

        public void record(){
            recorder = new MediaRecorder();
            final String fileName = getFilePath();
            recorder.setPreviewDisplay(sf);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOrientationHint(90);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setOutputFile(fileName);
            recorder.setVideoFrameRate(10);
            recorder.setMaxDuration(3000);
            recorder.setVideoSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            try {
                recorder.prepare();
                recorder.start();
            }catch(Exception ex){
                ex.printStackTrace();
                Toast toast=Toast.makeText(getApplicationContext(),"Failed To Start Recorder",Toast.LENGTH_SHORT);
                toast.show();
            }

/*            final CountDownLatch latch = new CountDownLatch(10);
            int delay = 1000;
            int period = 3000;

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    latch.countDown();
                }
            }, delay, period);
            try {
                latch.await();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            timer.cancel();
            Toast toast=Toast.makeText(getApplicationContext(),"DONE",Toast.LENGTH_SHORT);
            toast.show();
            recorder.stop();
            recorder.reset();   // You can reuse the object by going back to setAudioSource() step
            recorder.release(); // Now the object cannot be reused*/
            /*VideoView screen = findViewById(R.id.video);
            screen.setVideoPath(fileName);
            screen.start();*/
        }

        public String getFilePath(){
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            Date custDate = new Date();
            String roomName = room.getText().toString();

            String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+sdf.format(custDate)+".mp4";
            Toast toast=Toast.makeText(getApplicationContext(),filePath,Toast.LENGTH_LONG);
            toast.show();
            return filePath;
        }

        public void storeVideoToFirebase(File video){

        }

        public void run(){
            Looper.prepare();
            record();
        }
    }//end of record class
}//end of parent class