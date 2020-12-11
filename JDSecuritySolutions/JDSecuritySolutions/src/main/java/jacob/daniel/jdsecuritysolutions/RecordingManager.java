package jacob.daniel.jdsecuritysolutions;

//Course: CENG319
//Team: JD Security Solutions
//Author: Jacob Arsenault N01244276

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.EditText;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RecordingManager implements Callable {

    EditText room;
    Context context;
    SurfaceView screen;
    public static boolean allowRecord;
    public static boolean startRecord;

    RecordingManager(Context context, SurfaceView screen, EditText room, boolean allow){
        this.context = context;
        this.room = room;
        this.allowRecord = allow;
        this.screen = screen;
    }

    @Override
    public Object call() {
        this.startRecord = true;
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Callable<Boolean> recorder = new Recorder(context, screen, room);
        Log.println(Log.INFO, "Manager", "Starting Recorder Thread");
        Future<Boolean> future = executor.submit(recorder);
        return null;
    }

    public void setAllowRecord(boolean allow){
        this.allowRecord = allow;
    }
}
