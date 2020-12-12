package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;


public class LoginAndRegister extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private SharedPreferences userInfo;
    private EditText usernameField;
    private EditText passwordField;
    CheckBox check;
    private boolean remember = false;
    private SharedPreferences.Editor editor;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton gsi;
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
        attemptAutoLogin();

        gsi = findViewById(R.id.sign_in_button);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        gsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.println(Log.INFO,"GoogleSignIn","Started");
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
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
        usernameField = (EditText) findViewById(R.id.addUser);
        passwordField = (EditText) findViewById(R.id.addPass);
        check = findViewById(R.id.remember);
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
    }

    public void login(int loginCode){
        if (loginCode == 0){
            Intent intent = new Intent(LoginAndRegister.this, ChooseConfig.class);
            startActivity(intent);
        }
        else if (loginCode == 1){
            Intent intent = new Intent(LoginAndRegister.this, ViewerMenu.class);
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
        if(check.isChecked()){
            remember = true;
            editor.putBoolean("LoggedIn", remember);
            editor.commit();
        }
        else{
            remember = false;
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
            check.setChecked(true);
            this.remember = true;
            authenticate(findViewById(R.id.submitButton));
        }
    }

    private void storeUserLocally(){
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();
            //add all values to userInfo
            editor.putString("User", username);
            editor.putString("Pass", password);
            editor.putBoolean("LoggedIn", remember);
            editor.commit();
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
                if(checkUser.username.equals(checkUser.username) && !checkUser.username.equals("")){
                    if(checkUser.password.equals(user.password) && !user.password.equals("")){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.

            User googleUser = new User(account.getDisplayName());
            Registration register = new Registration();
            register.checkIfUserExists(findViewById(R.id.sign_in_button), googleUser);
            editor.putString("User", googleUser.username);
            editor.putString("Pass", googleUser.password);
            editor.commit();
            Intent intent = new Intent(LoginAndRegister.this, ChooseConfig.class);
            startActivity(intent);
            finish();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.println(Log.INFO, "GoogleSignInFailure", ""+e.getStatusCode());
        }
    }

}
