package jacob.daniel.jdsecuritysolutions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

//TODO fix record video
//TODO landscape views
//TODO french translation
//TODO logout
//TODO bottomnav
//TODO shared pref on login (remember me button)
//TODO login auth
//TODO folder for video storage


public class LoginAndRegister extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private SharedPreferences userInfo;
    private EditText usernameField;
    private EditText passwordField;
    private boolean remember = false;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
        usernameField = (EditText) findViewById(R.id.addUser);
        passwordField = (EditText) findViewById(R.id.addPass);
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
        attemptAutoLogin();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_login_and_register);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_login_and_register_landscape);
        }
    }

    public void login(int loginCode){

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

    public void rememberMe(View v){
        CheckBox check = findViewById(R.id.remember);
        if(check.isChecked()){
            remember = true;
            editor.putBoolean("LoggedIn", remember);
            editor.commit();
        }
    }

    public void attemptAutoLogin(){
        //check if login remembered
        String username = userInfo.getString("User", "");
        String password = userInfo.getString("Pass", "");
        boolean remembered =  userInfo.getBoolean("LoggedIn", false);
        if(remembered && !password.equals("") && !username.equals("")){
            usernameField.setText(username, EditText.BufferType.EDITABLE);
            passwordField.setText(password, EditText.BufferType.EDITABLE);
            authenticate(findViewById(R.id.submitButton));
        }
    }

    private void storeUserLocally(){
        if(remember) {
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();
            //add all values to userInfo
            editor.putString("User", username);
            editor.putString("Pass", password);
            editor.putBoolean("LoggedIn", remember);
            editor.commit();
        }
    }


    public void authenticate(View v){
        //Check user/pass in database. for each failed check, decrement status.  if any fail, login fails.
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        User user = new User(username, password);
        storeUserLocally();

        //Check firebase
        checkDbLogin(user);
    }

    public void checkDbLogin(final User user){
        DatabaseReference rootRef = database.getReference();
        final DatabaseReference userNameRef = rootRef.child("Usernames/"+user.username);

        readData(userNameRef, new LoginAndRegister.OnGetDataListener() {
            int status;
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                User checkUser = new User();
                try {
                    HashMap<String, Object> temp = (HashMap<String, Object>) dataSnapshot.getValue();
                    for (String key : temp.keySet()) {
                        String tempStr = String.valueOf(temp.get(key));
                        if(key.equals("name")){
                            checkUser.name=tempStr;
                        }
                        else if(key.equals("email")){
                            checkUser.email=tempStr;
                        }
                        else if(key.equals("password")){
                            checkUser.password=tempStr;
                        }
                        else if(key.equals("username")){
                            checkUser.username=tempStr;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if(checkUser.username.equals(user.username) && !checkUser.username.equals("")){
                    if(checkUser.password.equals(user.password)){
                        //status = userInfo.getInt("Device", 0);
                        status = 0;
                    }
                    else {
                        status = -1;
                    }
                }
                else{
                    status = -1;
                }
                login(status);
            }
            @Override
            public void onStart() {
            }

            @Override
            public void onFailure() {
            }
        });
    }

    public void readData(final DatabaseReference ref, final LoginAndRegister.OnGetDataListener listener) {
        final boolean hasFinished = false;
        listener.onStart();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure();
                ref.removeEventListener(this);
            }
        });
    }

    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(R.string.alert_message)

                .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .setNegativeButton(R.string.alert_cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


}
