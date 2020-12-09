package jacob.daniel.jdsecuritysolutions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class ViewerMenu extends BottomNavigationInflater {
    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;
    TextView title;
    int height;
    int width;
    String username;
    ScrollView scrollView;
    BottomNavigationView bottomNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
        username = userInfo.getString("User", "Username");
        username+="'s "+getResources().getString(R.string.ViewerMenuTitle);

        Configuration orientation = getResources().getConfiguration();
        onConfigurationChanged(orientation);

    }

    private String[] getRooms(){
        //TODO get room names from firebase
        String[] rooms = {"DefaultRoom", "LivingRoom", "BedRoom", "BathRoom", "ParentsRoom", "FrontYard", "Backyard", "Closet"};
        return rooms;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void createGrid(int orientation){
        title=findViewById(R.id.ChooseRoomTitle);
        title.setText(username);

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


        bottomNavigationView = findViewById(R.id.navigationView);
        int navHeight = bottomNavigationView.getHeight();
        int textHeight = title.getHeight();

        String[] rooms = getRooms();
        GridLayout gridLayout = findViewById(R.id.buttonGrid);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        if (orientation==0){
            gridLayout.setColumnCount(2);
            gridLayout.setRowCount(rooms.length/2 + rooms.length%2);
        }
        else {
            gridLayout.setColumnCount(3);
            gridLayout.setRowCount(rooms.length/3 + 1);
        }


        for (int i = 0; i<rooms.length; i++) {
            Button button = new Button(this);
            if (orientation==0){
                button.setHeight(width/2);
                button.setWidth(width/2);
            }
            else {
                button.setHeight((height-70)/2);
                button.setWidth((height-70)/2);
            }

            button.setText(rooms[i]);
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
            createGrid(0);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.viewer_menu_landscape);
            super.createNavListener();
            createGrid(1);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ViewerMenu.this, ChooseConfig.class);
        startActivity(intent);
        finish();
    }
}
