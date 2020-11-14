package jacob.daniel.jdsecuritysolutions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Registration extends AppCompatActivity {

    private String errorMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
    }

    public void submitInfo(View v){
        int status = validateInfo();
        if(status == 0){
            Intent intent = new Intent(Registration.this, LoginAndRegister.class);
            startActivity(intent);
        }
        else{

        }
    }

    private int validateInfo(){
        int status = 0;

        //TODO validate info, set error message on appropriate editviews

        return status;
    }
}
