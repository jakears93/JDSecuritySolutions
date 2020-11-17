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
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.List;

//TODO change video orientation
//TODO add surfaceview to layout
//TODO reorganize code structure

public class CameraDevice extends AppCompatActivity {

    private static final int MAX_PREVIEW_WIDTH = 1280;
    private static final int MAX_PREVIEW_HEIGHT = 720;
    private boolean allowRecord = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_device);
        checkPermissions();
        if(allowRecord) {
            try {
                dispatchTakeVideoIntent();
            } catch (IOException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "IOEXCEPTION", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "Cannot Record", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void dispatchTakeVideoIntent() throws IOException {
            final String fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "test.mp4";
            final MediaRecorder recorder = new MediaRecorder();

            SurfaceTexture sft = new SurfaceTexture(0);
            Surface sf = new Surface(sft);

            recorder.setPreviewDisplay(sf);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(fileName);
            recorder.setVideoFrameRate(60);
            recorder.setVideoSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


            recorder.prepare();
            try {
                recorder.start();
                new CountDownTimer(10000, 1000) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        Toast toast=Toast.makeText(getApplicationContext(),"DONE",Toast.LENGTH_SHORT);
                        toast.show();
                        recorder.stop();
                        recorder.reset();   // You can reuse the object by going back to setAudioSource() step
                        recorder.release(); // Now the object cannot be reused
                        VideoView screen = findViewById(R.id.video);
                        screen.setVideoURI(Uri.parse(fileName));
                    }
                }.start();
            }catch(Exception ex){
                ex.printStackTrace();
                Toast toast=Toast.makeText(getApplicationContext(),"Failed To Start Recorder",Toast.LENGTH_SHORT);
                toast.show();
            }
    }

    public void flippedSwitch(View v) {

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

}
