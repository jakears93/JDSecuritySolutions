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
            Intent intent = new Intent(LoginAndRegister.this, ForgotPassword.class);
            startActivity(intent);
        }
        else if (loginCode < 0){
            Toast toast=Toast.makeText(getApplicationContext(),R.string.LoginFail,Toast.LENGTH_SHORT);
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
        EditText user = (EditText) findViewById(R.id.addUser);
        EditText pass = (EditText) findViewById(R.id.addPass);

        return status;
    }
}
