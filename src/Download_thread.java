import java.util.List;

/**
 * Created by Easy on 2016.11.21.
 */
public class Download_thread extends Thread {
    private List<Task_item> task_list;

    public Download_thread(List<Task_item> tlist, String name)
    {
        super(name);
        task_list = tlist;
        System.out.println("Create thread: " + this.getName());
    };

    public void run()
    {
        try
        {
            System.out.println("Start thread: " + this.getName());
            while (task_list.size() > 0)
            {
                Task_item task = task_list.remove(0);
                System.out.println("Thread " + this.getName() + "; New task: " + task.to_string());
                Misc_Utils.load_file(task.url_link, task.file_name);
                Thread.sleep(1);
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread" + this.getName() + " interrupted");
        }
        catch (IndexOutOfBoundsException e)
        {
            //for case if tasks.size() = 0, but another thread delete last item between tasks.size and tasks.remove
            return;
        }
    }
}
