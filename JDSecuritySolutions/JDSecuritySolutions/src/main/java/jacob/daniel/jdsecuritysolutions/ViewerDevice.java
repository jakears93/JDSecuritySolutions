package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ViewerDevice extends BottomNavigationInflater {

    String fileName;
    String roomName;
    int vidIndex = 0;
    int vidCount = 25;
    VideoView screen;
    SeekBar seek;
    SharedPreferences userInfo;
    String username;
    TextView roomTitle;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.roomName = getIntent().getExtras().getString("roomname");
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);

        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        username = userInfo.getString("User", "username");
        roomTitle = findViewById(R.id.RoomNameLabel);

        roomTitle.setText(this.roomName);


        seek = findViewById(R.id.videoProgress);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seek.getProgress();
                setVideoFromProgress(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

            }
        });

        screen = findViewById(R.id.recordedVideo);

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Callable<Boolean> mediaPlayer = new JDMediaPlayer(this, screen, username, roomName);
        Log.println(Log.INFO, "ViewerDevice", "Starting MediaPlayer Thread");
        Future<Boolean> future = executor.submit(mediaPlayer);
        if(future.isDone()){
            Log.println(Log.INFO, "JDMediaPlayer", "No More Content to Play");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.viewer_device);
            super.createNavListener();
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.viewer_device_landscape);
            super.createNavListener();
        }
    }

    //TODO add in finer seek detail, find file via progress chunk, find time via progresschunk %  screen.seekTo();
    public void setVideoFromProgress(int progress){
        screen.stopPlayback();
        if(progress == 0){
            vidIndex = 0;
        }
        else {
            vidIndex = (vidCount * progress)/100;
        }
        setVideo();
    }

    public void setVideo() {
        //TODO set video from firebase
        String path =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "JDSecurity" + File.separator + roomName + File.separator;
        String[] dirListing;
        File dir = new File(path);
        dirListing = dir.list();
        if(dirListing!=null){
            Arrays.sort(dirListing);
        }

        int progress = (vidIndex * 100 / vidCount);
        seek.setProgress(progress);

        if(vidIndex < dirListing.length){
            fileName =  path+dirListing[vidIndex];
            File fp = new File(fileName);
            if(fp.exists()){
                screen.setVideoPath(fileName);
                screen.start();
                Log.println(Log.INFO, "Video Viewer", fileName);
            }
            else{
                Toast toast = Toast.makeText(getApplicationContext(),getResources().getString(R.string.NoMoreVideos), Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),getResources().getString(R.string.NoMoreVideos), Toast.LENGTH_LONG);
            toast.show();
        }

    }


}