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
    static int vidIndex;
    static int vidCount;
    ArrayList files;
    boolean complete;

    //initialize object
    JDMediaPlayer(Context context, VideoView screen, String username, String roomname){
        this.context = context;
        this.screen = screen;
        this.username = username;
        this.roomname = roomname;
        this.vidIndex = 0;
        this.vidCount = 0;
    }

    //entry point for calling function
    @Override
    public Boolean call(){
        Log.println(Log.INFO, "JDMediaPlayer", "MediaPlayer Started");

        //when video complete, move on to next video
        screen.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                playFirebaseVideo();
            }
        });

        //start to play videos if available
        listFirebaseVideos();

        //keep thread alive until told to stop
        //allows for scrubbing through videos
        while(!ViewerDevice.stop){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(complete && vidIndex<vidCount){
                complete = false;
                playFirebaseVideo();
            }
        }

        //stop playback when told to stop
        screen.stopPlayback();

        Log.println(Log.INFO, "JDMediaPlayer", "MediaPlayer Ended");
        return true;
    }

    public void playFirebaseVideo(){
        //only look for video if in range
        if(vidIndex>=vidCount){
            Log.println(Log.INFO, "JDMediaPlayer", "No More Videos");
            complete = true;
            return;
        }
        Log.println(Log.INFO, "JDMediaPlayer", "Starting to play video "+vidIndex);

        //parse video file name
        String filePath = files.get(vidIndex).toString();
        String[] fileComponents = filePath.split("/",10);
        filePath=fileComponents[fileComponents.length-1];

        //create reference to indexed video
        fileRef = roomRef.child(filePath);

        try{
            video = File.createTempFile("JDPLAYVIDEO", "mp4");
        }catch (IOException ex){
            ex.printStackTrace();
        }

        //Once video is retrieved play it on videoview
        fileRef.getFile(video).addOnSuccessListener(
                new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess (FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.println(Log.INFO, "Firebase", "Download Success: "+video.toString());
                        vidIndex++;
                        screen.setVideoURI(Uri.fromFile(video));
                        screen.start();
                        Log.println(Log.INFO, "JDMediaPlayer", "Playing Video "+vidIndex+" of "+vidCount);
                    }

                }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.println(Log.INFO, "Firebase", "Download Failed: "+video.toString());
            }
        });
    }

    //collect list of videos in specific rooms storage.
    //call playfirebasevideo on completion to start playback
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
                        vidCount = files.size();
                        playFirebaseVideo();
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
