package jacob.daniel.jdsecuritysolutions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseConfig extends AppCompatActivity {

    private SharedPreferences userInfo;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_config);
        userInfo = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        editor = userInfo.edit();
    }

    public void clickedViewer(View v){
        Intent intent = new Intent(ChooseConfig.this, ViewerDevice.class);
        //Saved device type to shared pref (1 being viewer)
        editor.putInt("Device", 1);
        editor.commit();
        startActivity(intent);
    }

    public void clickedCamera(View v){
        Intent intent = new Intent(ChooseConfig.this, CameraDevice.class);
        //Saved device type to shared pref (2 being camera)
        editor.putInt("Device", 2);
        editor.commit();
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_logout_title)
                .setMessage(R.string.alert_logout_message)

                .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ChooseConfig.super.onBackPressed();
                        editor.clear();
                        editor.commit();
                    }
                })
                .setNegativeButton(R.string.alert_cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
