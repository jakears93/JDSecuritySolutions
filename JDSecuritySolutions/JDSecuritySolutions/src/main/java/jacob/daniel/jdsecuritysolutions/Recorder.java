package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.content.Context;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class Recorder implements Callable<Boolean> {

    long maxFileSize;
    String fileID;
    String lastFileId;
    String filePath;
    String lastFilePath;
    File lastFile;
    EditText room;
    Context context;
    SurfaceView screen;
    SurfaceHolder surfaceHolder;
    MediaRecorder recorder;
    File fp;
    boolean end = false;
    SharedPreferences userInfo;
    String username;
    FirebaseStorage fbInstance;

    //set up object. create room directory if it doesnt exist
    Recorder(Context context, SurfaceView screen, EditText room){
        this.maxFileSize = getMaxFileSize();
        this.room = room;
        this.context = context;
        this.screen = screen;

        userInfo = context.getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        username = userInfo.getString("User", "username");

        String roomName = room.getText().toString().replaceAll("\\s+", "");
        File dir =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName + File.separator);
        if(dir.mkdirs()){
            Log.println(Log.INFO, "Directory", "Successfully Created Root File Directory");
        }
        else{
            Log.println(Log.INFO, "Directory", "Failed to Create Root File Directory");
        }
    }

    @Override
    public Boolean call(){
        prepare();
        record();
        return true;
    }

    //prepare mediarecorder by initializing settings
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
            //Once next file has started being recorded to, upload last file
            if(i==MediaRecorder.MEDIA_RECORDER_INFO_NEXT_OUTPUT_FILE_STARTED){
                if(lastFilePath!=null && !end){
                    lastFile = new File(lastFilePath);
                    Log.println(Log.INFO, "Firebase", "Uploading last saved video: "+lastFilePath);
                    uploadVideoToFirebase(lastFile);
                }
            }
            //set next file output once limit is approaching
            else if(i==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING){
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

    //start the recording and continue until recording manager stops it
    public void record(){
        Log.println(Log.INFO, "MediaRecorder", "Started Recording "+fp.toString());
        recorder.start();

        createRoomReference();

        while(!end){
            if(!RecordingManager.allowRecord){
                recorder.stop();
                recorder.release();
                this.end = true;
                lastFileId = fileID;
                lastFilePath = fp.getPath();
                uploadVideoToFirebase(fp);
            }
        }
    }

    //get the file path for older api levels
    public synchronized String getFilePath(){
        String roomName = room.getText().toString().replaceAll("\\s+", "");

        lastFilePath = filePath;

        fileID = getFileId();
        filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName + File.separator + fileID +".mp4";

        File fp = new File(filePath);
        try {
            if(!fp.createNewFile()){
                end=true;
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return filePath;
    }

    //set file path for api level 29/30
    public synchronized File getFilePath2(){
        String roomName = room.getText().toString().replaceAll("\\s+", "");

        lastFilePath = filePath;
        lastFileId = fileID;

        fileID = getFileId();
        filePath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName + File.separator + fileID +".mp4";

        File fp = new File(filePath);

        try {
            if(!fp.createNewFile()){
                end=true;
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return fp;
    }

    //Get max filesize out of shared prefs from settings
    //implemented in next update
    private long getMaxFileSize(){
        return 3000000;
    }

    //Get file id
    private String getFileId(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", context.getResources().getConfiguration().locale);
        return sdf.format(date);
    }

    //create empty file for room.
    //need to create file so viewer can locate which rooms exist as it doesnt detect directories
    public void createRoomReference(){
        fbInstance = FirebaseStorage.getInstance();
        final String roomName = room.getText().toString().replaceAll("\\s+", "");
        Log.println(Log.INFO, "RoomReference", "Creating "+roomName);
        StorageReference storageRef = fbInstance.getReference();
        String fbPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName+".txt";
        final File tempFile = new File(fbPath);
        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Uri upload = Uri.fromFile(tempFile);
        final StorageReference newFile = storageRef.child(username+File.separator+roomName);

        newFile.putFile(upload)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.println(Log.INFO, "Firebase", "Upload Successful: "+upload.toString());
                        tempFile.delete();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.println(Log.INFO, "Firebase", "Upload Failed: "+roomName);
                        tempFile.delete();
                    }
                });
    }

    //upload last video to firebase
    public void uploadVideoToFirebase(File video){
        fbInstance = FirebaseStorage.getInstance();
        String roomName = room.getText().toString().replaceAll("\\s+", "");
        StorageReference storageRef = fbInstance.getReference();
        String fbPath = username+File.separator+roomName+File.separator+lastFileId;
        final StorageReference newFile = storageRef.child(fbPath);
        Uri file = Uri.fromFile(video);


        newFile.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.println(Log.INFO, "Firebase", "Upload Successful: "+lastFilePath);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.println(Log.INFO, "Firebase", "Upload Failed: "+lastFilePath);
                    }
                });

    }

}//end of record class