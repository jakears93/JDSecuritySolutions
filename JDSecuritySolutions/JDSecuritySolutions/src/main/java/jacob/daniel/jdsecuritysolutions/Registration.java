package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class Registration extends AppCompatActivity {

    EditText info;
    int status = 0;
    boolean activeScreen;

    public Registration(){
        this.activeScreen = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
        activeScreen = true;
    }

    //listener for submit button
    public void submitInfo(View v){
        User user = validateInfo();
        if(status==0) {
            checkIfUserExists(v, user);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.registration);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.registration_landscape);
        }
    }

    //check if user exists already, if not, add user to firebase and go back to log in
    public void checkIfUserExists(final View v, final User user){
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        final DatabaseReference rootRef = database.getReference();
        final DatabaseReference userNameRef = rootRef.child("Usernames/"+user.username);

        readData(userNameRef, new OnGetDataListener() {
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

                if(checkUser.username.equals(user.username) && activeScreen){
                    info.setError(getResources().getString(R.string.usernameErrorMsg));
                }
                else{
                    //Submit info to firebase
                    rootRef.child("Usernames").child(user.username).setValue(user);
                    if(activeScreen){
                        returnToLogin(v);
                    }
                }

            }
            @Override
            public void onStart() {
            }

            @Override
            public void onFailure() {
            }
        });
    }

    public void returnToLogin(View v){
        Intent intent = new Intent(Registration.this, LoginAndRegister.class);
        startActivity(intent);
        finish();
    }

    //make sure all information inputted is valid before checking firebase for exisiting user
    private User validateInfo(){
        status = 0;

        User user = new User();

        info=findViewById(R.id.fullNameInput);
        user.name = info.getText().toString();
        if(user.name.length() < 2){
            info.setError(getResources().getString(R.string.nameErrorMsg));
            status = -1;
        }

        info=findViewById(R.id.passwordInput);
        user.password = info.getText().toString();
        if(user.password.length() < 8){
            //set error
            info.setError(getResources().getString(R.string.passwordErrorMsg));
            status = -1;
        }

        info=findViewById(R.id.confirmPasswordInput);
        String confirmPass = info.getText().toString();
        if(!confirmPass.equals(user.password) || confirmPass.length()==0){
            //set error
            info.setError(getResources().getString(R.string.confirmPasswordErrorMsg));
            status = -1;
        }

        info=findViewById(R.id.emailInput);
        user.email = info.getText().toString();
        //TODO validate email format
        if(user.email.length() < 5){
            info.setError(getResources().getString(R.string.emailErrorMsg));
            status = -1;
        }

        info=findViewById(R.id.userNameInput);
        user.username = info.getText().toString();
        if(user.username.length()<2){
            info.setError(getResources().getString(R.string.usernameErrorMsg));
            status = -1;
        }
        return user;
    }

    //read firebase data
    public void readData(final DatabaseReference ref, final OnGetDataListener listener) {
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
}
