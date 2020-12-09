package jacob.daniel.jdsecuritysolutions;

import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

//TODO uncomment for working version
public class Recorder implements Callable<Boolean> {

    private Boolean doneRecording;
    String fileName;
    long maxFileSize;
    String fileID;
    EditText room;
    Context context;
    SurfaceView screen;
    SurfaceHolder surfaceHolder;
    MediaRecorder recorder;
    File fp;
    boolean end = false;


    Recorder(Context context, SurfaceView screen, EditText room){
        this.maxFileSize = getMaxFileSize();
        this.room = room;
        this.context = context;
        this.screen = screen;
        String roomName = room.getText().toString().replaceAll("\\s+", "");
        File dir =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName + File.separator);
        dir.mkdirs();
        this.doneRecording = false;
    }

    @Override
    public Boolean call() throws Exception {
        prepare();
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

        recorder.setMaxFileSize(maxFileSize);

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
            if(i==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING){
                Log.println(Log.INFO, "MediaRecorder", "Approaching MaxFileSize");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    Log.println(Log.INFO, "FileType", "Updated");
                    fileID = getFileId();
                    fp = getFilePath2();
                    Log.println(Log.INFO, "FileName", fp.toString());
                    try {
                        recorder.setNextOutputFile(fp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Older Api creates new object and starts recording again.
                else{
                    recorder.stop();
                    recorder.release();
                    prepare();
                    record();
                }
            }
        }

    }

    public void record(){
        Log.println(Log.INFO, "MediaRecorder", "Started Recording "+fp.toString());
        recorder.start();

        while(!end){
            if(RecordingManager.allowRecord == false){
                recorder.stop();
                recorder.release();
                this.end = true;
                fp.delete();
                Log.println(Log.INFO, "Deleted Unfinished File", fp.toString());
            }
            else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized String getFilePath(){
        String roomName = room.getText().toString().replaceAll("\\s+", "");

        fileID = getFileId();
        String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName + File.separator + fileID +".mp4";

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

        fileID = getFileId();
        String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName + File.separator + fileID +".mp4";

        File fp = new File(filePath);

        try {
            fp.createNewFile();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return fp;
    }

    //Get max filesize out of shared prefs from settings
    private long getMaxFileSize(){
        return 2000000;
    }

    //Get file id
    private String getFileId(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String id = sdf.format(date);
        return id;
    }

    //TODO implement db storage
    public void storeVideoToFirebase(File video){

    }

}//end of record class