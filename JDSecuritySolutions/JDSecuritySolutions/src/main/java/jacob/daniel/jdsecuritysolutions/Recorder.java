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
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class Recorder implements Callable<Integer> {

    String fileName;
    static int vidCount = 0;
    EditText room;
    Context context;

    Recorder(Context context, EditText room){
        this.room = room;
        this.context = context;
    }

    private Integer doneRecording = -1;

    @Override
    public Integer call() throws Exception {
        record();
        return doneRecording;
    }

    boolean vidRecording = true;

    //TODO enable preview
    public void record(){
        SurfaceTexture sft = new SurfaceTexture(0);
        Surface sf = new Surface(sft);
        MediaRecorder recorder = new MediaRecorder();
        File fp = getFilePath2();
        vidCount++;
        recorder.setPreviewDisplay(sf);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setOrientationHint(90);
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.println(Log.INFO, "FileType", "Updated");
            recorder.setOutputFile(fp);
        }
        else{
            Log.println(Log.INFO, "FileType", "Legacy");
            recorder.setOutputFile(fileName);
        }
        recorder.setMaxDuration(1000);

/*
            //TODO explore max file size instead of max duration?
            recorder.setMaxFileSize(1000000);
            recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING ) {
                        Log.println(Log.INFO, "recorder", "stopping");
                        mr.stop();
                        mr.release();
                        vidRecording = false;
                    }
                }
            });
*/

        try {
            recorder.prepare();
            recorder.start();

        }catch(Exception ex){
            ex.printStackTrace();
/*            Toast toast=Toast.makeText(context.getString(R.string.RecordFail),Toast.LENGTH_SHORT);
            toast.show();*/
        }
        try {
            Thread.sleep(1000);
            recorder.stop();
            recorder.release();
            doneRecording = 1;
        }
        catch(InterruptedException ex){
            ex.printStackTrace();
        }

    }

    public String getFilePath(){
        String roomName = room.getText().toString().replaceAll("\\s+", "");

        String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+vidCount+".mp4";

        File fp = new File(filePath);
        try {
            fp.createNewFile();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return filePath;
    }

    public File getFilePath2(){
        String roomName = room.getText().toString().replaceAll("\\s+", "");

        String filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+vidCount+".mp4";

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

    public void run(){
        Looper.prepare();
        record();
    }


}//end of record class