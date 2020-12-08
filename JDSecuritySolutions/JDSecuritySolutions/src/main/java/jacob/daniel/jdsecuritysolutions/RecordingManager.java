package jacob.daniel.jdsecuritysolutions;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.EditText;

import androidx.camera.core.CameraX;

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

    //prep next worker when other worker is recording.
    //TODO change to 1 recorder thread that continuously writes to new files.
    @Override
    public Object call() throws Exception {
        this.startRecord = true;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> recorder = new Recorder(context, screen, room);
        Log.println(Log.INFO, "Manager", "Starting Thread 1");
        Future<Boolean> future = executor.submit(recorder);

        while(allowRecord){
            if(future.isDone()){
                Log.println(Log.INFO, "Manager", "Starting Thread 1");
                future = executor.submit(recorder);
            }
        }
        return null;
    }

    public void setAllowRecord(boolean allow){
        this.allowRecord = allow;
    }
}
