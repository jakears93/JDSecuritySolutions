package jacob.daniel.jdsecuritysolutions;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class User{
    public String name;
    public String username;
    public String password;
    public String email;
    public FirebaseDatabase database;
    public DatabaseReference dbRef;


    User(String name, String user, String pass, String email){
        this.name=name;
        this.username=user;
        this.password=pass;
        this.email=email;
    }
    public User(){
        this.name="";
        this.username="";
        this.password="";
        this.email="";
    }

    public boolean setDbReference(){
        if(!this.username.equals("")){
            this.dbRef = database.getReference().child("Usernames/"+this.username);
            return true;
        }
        else{
            return false;
        }
    }

    //TODO finish function to return user from db
    public User retrieveUserInfoFromDb(){

        final User referenceUser = this;
        final User checkUser = new User();

        if(this.setDbReference()){
            readData(this.dbRef, new OnGetDataListener() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if(!referenceUser.username.equals("")){
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
        return checkUser;
    }

    public void readData(DatabaseReference ref, final OnGetDataListener listener) {
        listener.onStart();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure();
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