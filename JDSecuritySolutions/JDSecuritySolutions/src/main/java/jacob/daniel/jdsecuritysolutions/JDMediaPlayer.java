package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;


//TODO update progress bar based on how many videos in list and what index
public class JDMediaPlayer implements Callable<Boolean> {

    Context context;
    VideoView screen;
    String username;
    String roomname;
    File video;
    FirebaseStorage fbInstance;
    StorageReference storageRef;
    StorageReference roomRef;
    StorageReference fileRef;
    int vidIndex;
    int attempts;
    ArrayList files;
    boolean readyToPlay = false;
    boolean value;

    JDMediaPlayer(Context context, VideoView screen, String username, String roomname){
        this.context = context;
        this.screen = screen;
        this.username = username;
        this.roomname = roomname;
    }

    @Override
    public Boolean call(){
        Log.println(Log.INFO, "JDMediaPlayer", "MediaPlayer Started");

        screen.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                playFirebaseVideo();
            }
        });

        listFirebaseVideos();
        playFirebaseVideo();

        while(!value){}
        Log.println(Log.INFO, "JDMediaPlayer", "MediaPlayer Ended");
        return value;
    }

    public void playFirebaseVideo(){
        while(files.size()<=vidIndex){
            if(attempts > 100){
                value = true;
                return;
            }
            Log.println(Log.INFO, "JDMediaPlayer", "Attempt "+attempts);
            attempts++;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String filePath = files.get(vidIndex).toString();
        String[] fileComponents = filePath.split("/",6);
        filePath=fileComponents[fileComponents.length-1];

        fileRef = roomRef.child(filePath);

        try{
            video = File.createTempFile("JDPLAYVIDEO", "mp4");
        }catch (IOException ex){
            ex.printStackTrace();
        }

        fileRef.getFile(video).addOnSuccessListener(
                new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess (FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.println(Log.INFO, "Firebase", "Download Success: "+video.toString());
                        vidIndex++;
                        attempts = 0;
                        screen.setVideoURI(Uri.fromFile(video));
                        screen.start();
                    }

                }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.println(Log.INFO, "Firebase", "Download Failed: "+video.toString());
            }
        });
    }

    public void listFirebaseVideos(){
        fbInstance = FirebaseStorage.getInstance();
        storageRef = fbInstance.getReference();
        roomRef = storageRef.child(username+File.separator+roomname);
        files = new ArrayList();
        Log.println(Log.INFO, "Firebase", "References Made");


        roomRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            files.add(item);
                            Log.println(Log.INFO, "Files", item.toString());
                        }
                        readyToPlay = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.INFO, "Firebase", "Directory Retrieval Failure");
                    }
                });

    }

}
