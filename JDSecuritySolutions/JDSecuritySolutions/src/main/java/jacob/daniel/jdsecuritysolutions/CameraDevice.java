package jacob.daniel.jdsecuritysolutions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


//TODO add surfaceview to layout
//TODO reorganize code structure
//TODO change vidcount to value from preferences

public class CameraDevice extends AppCompatActivity {

    private static final int MAX_PREVIEW_WIDTH = 1280;
    private static final int MAX_PREVIEW_HEIGHT = 720;
    private boolean allowRecord = false;
    SurfaceTexture sft = new SurfaceTexture(0);
    Surface sf = new Surface(sft);
    EditText room;
    SwitchCompat toggle;
    String fileName;
    VideoView screen;
    int vidCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_device);
        room = findViewById(R.id.RoomName);
        toggle = findViewById(R.id.toggle);
        screen = findViewById(R.id.video);
    }

    class MyFileObserver extends FileObserver {

        public MyFileObserver (String path, int mask) {
            super(path, mask);
        }

        public void onEvent(int event, String path) {
            Toast toast = Toast.makeText(getApplicationContext(), "File complete", Toast.LENGTH_SHORT);
            toast.show();
            screen.setVideoPath(fileName);
            screen.start();
            this.stopWatching();
        }
    }


    public void flippedSwitch(View v) {
        if(toggle.isChecked()){
            checkPermissions();
            while(allowRecord && vidCount < 5) {
                Thread recording = new Thread(new StartRecording());
                recording.start();
                try {
                    recording.join();
                    Log.println(Log.INFO, "Thread", "Thread joined");
                }catch(InterruptedException ex){
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
                Toast toast = Toast.makeText(getApplicationContext(), "FAILED GAINING PERMISSIONS", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public class StartRecording implements Runnable{
        private MediaRecorder recorder;

        StartRecording(){

        }

        public void record(){
            recorder = new MediaRecorder();
            fileName = getFilePath();
            recorder = new MediaRecorder();
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
   /*         recorder.setOnInfoListener(new OnInfoListener()
            {

                @Override
                public void onInfo(MediaRecorder mr, int what, int extra)
                {
                    switch (what)
                    {
                        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                            Toast toast=Toast.makeText(getApplicationContext(),"Released",Toast.LENGTH_SHORT);
                            toast.show();
                            mr.stop();
                            mr.reset();
                            mr.release();
                            break;
                    }
                }
            });
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener()
            {

                @Override
                public void onError(MediaRecorder mr, int what, int extra)
                {
                    Toast toast=Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_SHORT);
                    toast.show();
                }
            });*/

            try {
                recorder.prepare();
                recorder.start();

            }catch(Exception ex){
                ex.printStackTrace();
                Toast toast=Toast.makeText(getApplicationContext(),"Failed To Start Recorder",Toast.LENGTH_SHORT);
                toast.show();
            }
            try {
                Thread.sleep(4000);
            }
            catch(InterruptedException ex){
                ex.printStackTrace();
            }
        }

        public String getFilePath(){
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
            Date custDate = new Date();
            String roomName = room.getText().toString();

            //TEMP
            String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+vidCount+".mp4";
            vidCount++;
/*
            String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+sdf.format(custDate)+".mp4";
*/
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