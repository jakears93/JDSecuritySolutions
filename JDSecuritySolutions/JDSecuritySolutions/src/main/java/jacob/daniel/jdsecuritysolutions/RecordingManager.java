package jacob.daniel.jdsecuritysolutions;

import android.content.Context;
import android.view.Surface;
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
    public boolean allowRecord;

    RecordingManager(Context context, SurfaceView screen, EditText room, boolean allow){
        this.context = context;
        this.room = room;
        this.allowRecord = allow;
        this.screen = screen;
    }

    @Override
    public Object call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Callable<Integer> recorder = new Recorder(context, screen, room);
        executor.submit(recorder);
        while(allowRecord){
            Future<Integer> future = executor.submit(recorder);
            while(!future.isDone()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        screen.getHolder().getSurface().release();
        return null;
    }

    public void setAllowRecord(boolean allow){
        this.allowRecord = allow;
    }
}
