package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.util.ArrayList;

public class ViewerMenu extends BottomNavigationInflater {
    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;
    TextView title;
    int height;
    int width;
    String username;
    String titleString;
    FirebaseStorage fbInstance;
    StorageReference storageRef;
    StorageReference roomRef;
    GridLayout gridLayout;
    ArrayList files = new ArrayList();
    int orientation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
        username = userInfo.getString("User", "Username");
        titleString = username+"'s "+getResources().getString(R.string.ViewerMenuTitle);

        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);

        gridLayout = findViewById(R.id.buttonGrid);
        gridLayout.removeAllViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRooms();
    }

    @Override
    protected void onPostResume() {
        gridLayout.removeAllViews();
        super.onPostResume();
    }

    private void getRooms(){
        fbInstance = FirebaseStorage.getInstance();
        storageRef = fbInstance.getReference();
        roomRef = storageRef.child(username+File.separator);
        files = new ArrayList();
        Log.println(Log.INFO, "Firebase", "References Made for "+username);

        roomRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        Log.println(Log.INFO, "Firebase", "OnSuccess");
                        for (StorageReference item : listResult.getItems()) {
                            files.add(item.getName());
                            Log.println(Log.INFO, "Rooms", item.getName());
                        }
                        Log.println(Log.INFO, "ViewerMenu", "Remaking Grid");
                        createMyGrid(orientation);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.INFO, "Firebase", "Directory Retrieval Failure");
                    }
                });
    }


    public void createMyGrid(int orientation){
        title=findViewById(R.id.ChooseRoomTitle);
        title.setText(titleString);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
            WindowMetrics wm = getWindowManager().getCurrentWindowMetrics();
            Rect bounds = wm.getBounds();
            height = bounds.height();
            width = bounds.width();
        }
        else{
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            height = displayMetrics.heightPixels;
            width = displayMetrics.widthPixels;
        }

        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        if (orientation==0){
            gridLayout.setColumnCount(2);
            gridLayout.setRowCount(files.size()/2 + files.size()%2);
        }
        else {
            gridLayout.setColumnCount(3);
            gridLayout.setRowCount(files.size()/3 + 1);
        }


        for (int i = 0; i<files.size(); i++) {
            Button button = new Button(this);
            if (orientation==0){
                button.setHeight(width/2);
                button.setWidth(width/2);
            }
            else {
                button.setHeight((height-70)/2);
                button.setWidth((height-70)/2);
            }

            button.setText(files.get(i).toString());
            addListener(button, button.getText().toString());
            Drawable img = getResources().getDrawable(R.drawable.room_clipart, getTheme());
            button.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
            button.setBackgroundColor(getColor(R.color.mainBackground));

            gridLayout.addView(button);
        }
    }

    public void addListener(Button button, final String text){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ViewerDevice.class);
                intent.putExtra("roomname", text);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.viewer_menu);
            super.createNavListener();
            orientation = 0;
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.viewer_menu_landscape);
            super.createNavListener();
            orientation = 1;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ViewerMenu.this, ChooseConfig.class);
        startActivity(intent);
        finish();
    }
}
