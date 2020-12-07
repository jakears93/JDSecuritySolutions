package jacob.daniel.jdsecuritysolutions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class ViewerDevice extends BottomNavigationInflater {

    BottomNavigationView bottomNav;
    String fileName;
    String roomName;
    int vidIndex = 0;
    int vidCount = 25;
    VideoView screen;
    SeekBar seek;
    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
        super.createNavListener();
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
        seek = (SeekBar) findViewById(R.id.videoProgress);
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


        roomName = getResources().getString(R.string.DefaultRoom).replaceAll("\\s+", "");
        screen = findViewById(R.id.recordedVideo);

        setVideo();
        vidIndex++;

        screen.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                if(vidIndex < vidCount){
                    setVideo();
                    vidIndex++;
                }
                else{
                    int progress = (vidIndex*100/vidCount);
                    seek.setProgress(progress);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.viewer_device);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.viewer_device_landscape);
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
        int progress = (vidIndex * 100 / vidCount);
        seek.setProgress(progress);
        fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+ File.separator+ vidIndex+".mp4";
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

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_logout_title)
                .setMessage(R.string.alert_logout_message)

                .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.remove("User");
                        editor.remove("Pass");
                        editor.remove("LoggedIn");
                        editor.commit();
                        Intent intent = new Intent(ViewerDevice.this, LoginAndRegister.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.alert_cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}