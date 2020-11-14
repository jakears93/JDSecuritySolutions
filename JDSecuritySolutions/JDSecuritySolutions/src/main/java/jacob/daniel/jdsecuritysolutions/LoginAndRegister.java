package jacob.daniel.jdsecuritysolutions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginAndRegister extends AppCompatActivity {
    private String errorMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_register);
    }

    public void login(View v){
        int loginCode = authenticate();
        if (loginCode == 0){
            Intent intent = new Intent(LoginAndRegister.this, ChooseConfig.class);
            startActivity(intent);
        }
        else if (loginCode == 1){
            Intent intent = new Intent(LoginAndRegister.this, ViewerDevice.class);
            startActivity(intent);
        }
        else if (loginCode == 2){
            Intent intent = new Intent(LoginAndRegister.this, CameraDevice.class);
            startActivity(intent);
        }
        else if (loginCode < 0){
            Toast toast=Toast.makeText(getApplicationContext(),errorMsg,Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void register(View v){
        Intent intent = new Intent(LoginAndRegister.this, Registration.class);
        startActivity(intent);
    }

    public void forgotPassword(View v){
        Intent intent = new Intent(LoginAndRegister.this, ForgotPassword.class);
        startActivity(intent);
    }

    private int authenticate(){
        //Check user/pass in database. for each failed check, decrement status.  if any fail, login fails.
        int status = 0;
        EditText user = (EditText) findViewById(R.id.addUser);
        EditText pass = (EditText) findViewById(R.id.addPass);

        //TODO validate login info with DB
        //TODO check current device config
        //TODO set status and errormsg
        //Status=0 when new device, 1=viewer, 2=camera

        return status;
    }
}
