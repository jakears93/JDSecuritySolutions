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

    RecordingManager(Context context, EditText room){
        this.context = context;
        this.room = room;
    }

    @Override
    public Object call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Callable<Integer> recorder = new Recorder(context, room);
        executor.submit(recorder);
        for(int i=0; i<5; i++){
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
}
