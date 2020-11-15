package jacob.daniel.jdsecuritysolutions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseConfig extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_config);
    }

    public void clickedViewer(View v){
        Intent intent = new Intent(ChooseConfig.this, ViewerDevice.class);
        startActivity(intent);
    }

    public void clickedCamera(View v){
        Intent intent = new Intent(ChooseConfig.this, CameraDevice.class);
        startActivity(intent);
    }
}
