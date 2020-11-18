package jacob.daniel.jdsecuritysolutions;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
    int vidCount = 6;
    VideoView screen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer_device);


        roomName = getResources().getString(R.string.DefaultRoom);
        bottomNav = findViewById(R.id.navigation);
        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        screen = findViewById(R.id.recordedVideo);

        setVideo();
        vidIndex++;

        screen.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                if(vidIndex < vidCount){
                    Toast toast = Toast.makeText(getApplicationContext(), "Next", Toast.LENGTH_SHORT);
                    toast.show();
                    setVideo();
                    vidIndex++;
                }
            }
        });
    }


    public void setVideo() {
        fileName =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator +roomName+vidIndex+".mp4";
        screen.setVideoPath(fileName);
        screen.start();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.nav_0:
                             intent = new Intent(ViewerDevice.this, LoginAndRegister.class);
                            startActivity(intent);
                            return true;
                        case R.id.nav_1:
                             intent = new Intent(ViewerDevice.this, Registration.class);
                            startActivity(intent);
                            return true;
                        case R.id.nav_2:
                             intent = new Intent(ViewerDevice.this, ChooseConfig.class);
                            startActivity(intent);
                            return true;
                    }
                    return false;
                }
            };

}