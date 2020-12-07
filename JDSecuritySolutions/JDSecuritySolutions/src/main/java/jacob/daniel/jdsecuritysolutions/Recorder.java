package jacob.daniel.jdsecuritysolutions;

import jacob.daniel.jdsecuritysolutions.R;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

//TODO uncomment for working version
public class Recorder implements Callable<Boolean> {

    private Boolean doneRecording;
    String fileName;
    static int vidCount = 0;
    EditText room;
    Context context;
    SurfaceView screen;
    SurfaceHolder surfaceHolder;
    MediaRecorder recorder;
    File fp;
    static boolean end = false;


    Recorder(Context context, SurfaceView screen, EditText room){
        this.room = room;
        this.context = context;
        this.screen = screen;
        String roomName = room.getText().toString().replaceAll("\\s+", "");
        File dir =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName);
        dir.mkdirs();
    }

    @Override
    public Boolean call() throws Exception {
        this.doneRecording = false;
        prepare();
        while(RecordingManager.startRecord == false){
            Thread.sleep(10);
        }
        RecordingManager.startRecord = false;
        this.end = false;
        record();
        return this.doneRecording;
    }

    public void prepare(){
        recorder = new MediaRecorder();
        surfaceHolder = screen.getHolder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setOrientationHint(90);
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        recorder.setPreviewDisplay(surfaceHolder.getSurface());


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.println(Log.INFO, "FileType", "Updated");
            fp = getFilePath2();
            Log.println(Log.INFO, "FileName", fp.toString());
            recorder.setOutputFile(fp);
        }
        else{
            Log.println(Log.INFO, "FileType", "Legacy");
            String fileName = getFilePath();
            Log.println(Log.INFO, "FileName", fileName);
            recorder.setOutputFile(fileName);
        }

        //recorder.setMaxDuration(2000);

        recorder.setMaxFileSize(3000000);
        MyListener listener = new MyListener();
        recorder.setOnInfoListener(listener);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class MyListener implements MediaRecorder.OnInfoListener{
        @Override
        public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
            if(RecordingManager.allowRecord == false){
                recorder.stop();
                recorder.release();
                Recorder.end = true;
                fp.delete();
                Log.println(Log.INFO, "Deleted Unfinished File", fp.toString());
                vidCount--;
                return;
            }
            if(i==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    Log.println(Log.INFO, "FileType", "Updated");
                    fp = getFilePath2();
                    Log.println(Log.INFO, "FileName", fp.toString());
                    try {
                        recorder.setNextOutputFile(fp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void record(){
        Log.println(Log.INFO, "MediaRecorder", "Started Recording "+fp.toString());
        recorder.start();

        while(!end){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

/*        try {
            Thread.sleep(2000);
            Log.println(Log.INFO, "MediaRecorder", "Stopping Recording"+fp.toString());
            recorder.stop();
            recorder.release();
        }
        catch(InterruptedException ex){
            ex.printStackTrace();
        }
        doneRecording = true;
        RecordingManager.startRecord = true;*/

    }

    public synchronized String getFilePath(){
        String roomName = room.getText().toString().replaceAll("\\s+", "");

        String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+ File.separator+ vidCount+".mp4";
        vidCount++;

        File fp = new File(filePath);
        try {
            fp.createNewFile();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return filePath;
    }

    public synchronized File getFilePath2(){
        String roomName = room.getText().toString().replaceAll("\\s+", "");

        String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+ File.separator+ vidCount+".mp4";
        vidCount++;

        File fp = new File(filePath);

        try {
            fp.createNewFile();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return fp;
    }

    //TODO implement db storage
    public void storeVideoToFirebase(File video){

    }

}//end of record class