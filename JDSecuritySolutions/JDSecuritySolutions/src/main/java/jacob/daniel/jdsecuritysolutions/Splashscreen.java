package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                    Intent intent = new Intent(Splashscreen.this, LoginAndRegister.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                }
            }
        };
        // start thread
        background.start();
    }
}
