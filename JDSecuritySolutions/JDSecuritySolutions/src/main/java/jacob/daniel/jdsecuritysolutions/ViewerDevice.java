package jacob.daniel.jdsecuritysolutions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class ViewerDevice extends AppCompatActivity {

    BottomNavigationView bottomNav;
    String fileName;
    String roomName;
    int vidIndex = 0;
    int vidCount = 5;
    VideoView screen;
    SeekBar seek;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer_device);

        seek = (SeekBar) findViewById(R.id.videoProgress);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seek.getProgress();
                Log.println(Log.INFO, "Seek", "Got Progress");
                setVideoFromProgress(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

            }
        });

        roomName = getResources().getString(R.string.DefaultRoom).replaceAll("\\s+", "");
        ;
/*        bottomNav = findViewById(R.id.navigation);
        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);*/
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

    //TODO add in finer seek detail, find file via progress chunk, find time via progresschunk %  screen.seekTo();
    public void setVideoFromProgress(int progress){
        screen.stopPlayback();
        Log.println(Log.INFO, "Prelim Progress", String.valueOf(progress));

        if(progress == 0){
            vidIndex = 0;
        }
        else {
            vidIndex = (vidCount * progress)/100;
        }
        Log.println(Log.INFO, "index", String.valueOf(vidIndex));
        setVideo();
    }

    public void setVideo() {
        int progress = (vidIndex * 100 / vidCount);
        Log.println(Log.INFO, "Progress level", String.valueOf(progress));
        seek.setProgress(progress);
        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + roomName + vidIndex + ".mp4";
        screen.setVideoPath(fileName);
        screen.start();
    }
}