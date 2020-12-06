package jacob.daniel.jdsecuritysolutions;

import android.content.Context;
import android.widget.EditText;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RecordingManager implements Callable {

    EditText room;
    Context context;
    public boolean allowRecord;

    RecordingManager(Context context, EditText room, boolean allow){
        this.context = context;
        this.room = room;
        this.allowRecord = allow;
    }

    @Override
    public Object call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Callable<Integer> recorder = new Recorder(context, room);
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
        return null;
    }

    public void setAllowRecord(boolean allow){
        this.allowRecord = allow;
    }
}
