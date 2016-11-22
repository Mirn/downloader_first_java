import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Easy on 2016.11.21.
 */
public class Download_thread extends Thread {
    private List<Task_item> task_list;
    private AtomicLong stat_rx;
    private Speed_limitter limitter;

    public Download_thread(String name, List<Task_item> tlist, Speed_limitter lim)
    {
        super(name);
        stat_rx = new AtomicLong(0);
        task_list = tlist;
        limitter = lim;
        //System.out.println("Create thread: " + this.getName());
    };

    public long RXed_get()
    {
        return stat_rx.get();
    }

    public void run()
    {
        try
        {
            //System.out.println("Start thread: " + this.getName());
            while (task_list.size() > 0)
            {
                Task_item task = task_list.remove(0);
                //System.out.println("Thread " + this.getName() + "; New task: " + task.to_string());
                Misc_Utils.load_file(task.url_link, task.file_name, stat_rx, limitter);
                Thread.sleep(1);
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread " + this.getName() + " interrupted");
        }
        catch (IndexOutOfBoundsException e)
        {
            //for case if tasks.size() = 0, but another thread delete last item between tasks.size and tasks.remove
            return;
        }
    }
}
