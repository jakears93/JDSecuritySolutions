package jacob.daniel.jdsecuritysolutions;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    EditText info;
    int status = 0;
    boolean exists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);
    }

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

    //TODO move checkIfUserExists method to user class

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

                if(checkUser.username.equals(user.username)){
                    info.setError(getResources().getString(R.string.usernameErrorMsg));
                }
                else{
                    //Submit info to firebase
                    rootRef.child("Usernames").child(user.username).setValue(user);
                    returnToLogin(v);
                }

            }
            @Override
            public void onStart() {
                Toast toast=Toast.makeText(getApplicationContext(),"starting",Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onFailure() {
            }
        });
    }

    public void returnToLogin(View v){
        Intent intent = new Intent(Registration.this, LoginAndRegister.class);
        startActivity(intent);
    }

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

    public void readData(final DatabaseReference ref, final OnGetDataListener listener) {
        final boolean hasFinished = false;
        listener.onStart();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast toast=Toast.makeText(getApplicationContext(),getResources().getString(R.string.LoginFail),Toast.LENGTH_SHORT);
                toast.show();
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
