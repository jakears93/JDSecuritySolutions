package jacob.daniel.jdsecuritysolutions;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPassword extends AppCompatActivity {

    private String errorMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.forgot_password);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.forgot_password_landscape);
        }
    }

    public void recoverPassword(View v){
        int status = validateEmail();
        if(status == 0){
            Intent intent = new Intent(ForgotPassword.this, LoginAndRegister.class);
            startActivity(intent);
        }
    }

    private int validateEmail(){
        int status = 0;
        EditText emailInput = (EditText) findViewById(R.id.emailInput);
        String input = emailInput.getText().toString();

        //TODO Check Email Format
        //TODO Check DB contains email
        //TODO Set status

        if(status == -1){
            errorMsg = getResources().getString(R.string.WrongEmailFormat);
            emailInput.setError(errorMsg);
        }
        else if(status == -2){
            errorMsg = getResources().getString(R.string.NoEmailFound);
            emailInput.setError(errorMsg);
        }
        return status;
    }
}
