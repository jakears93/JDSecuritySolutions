package jacob.daniel.jdsecuritysolutions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginAndRegister extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();

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
            Toast toast=Toast.makeText(getApplicationContext(),getResources().getString(R.string.LoginFail),Toast.LENGTH_SHORT);
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
        int status = -1;
        EditText usernameField = (EditText) findViewById(R.id.addUser);
        EditText passwordField = (EditText) findViewById(R.id.addPass);
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        //TODO validate login info with DB
        //TODO check current device config
        //TODO set status and errormsg
        //Status=0 when new device, 1=viewer, 2=camera

        //DEBUG
        if(username.equals("jacob")){
            if(password.equals("daniel")){
                status = 0;
            }
        }
        else{
            status = 0;
        }
        //ENDOFDEBUG

        return status;
    }



}
