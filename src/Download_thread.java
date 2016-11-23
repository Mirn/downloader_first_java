import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by Easy on 2016.11.21.
 */
final public class Download_thread extends Thread {
    private List<Task_item> done_list;
    private List<Task_item> task_list;
    private AtomicLong stat_rxed;
    private AtomicInteger stat_files_copied;
    private AtomicInteger stat_files_downloaded;
    private AtomicInteger stat_files_failed;
    private AtomicInteger stat_files_ignored;
    private Speed_limitter limitter;

    public Download_thread(String name, List<Task_item> tlist, List<Task_item> dlist, Speed_limitter lim)
    {
        super(name);
        stat_rxed = new AtomicLong(0);
        stat_files_copied = new AtomicInteger(0);
        stat_files_downloaded = new AtomicInteger(0);
        stat_files_failed = new AtomicInteger(0);
        stat_files_ignored = new AtomicInteger(0);

        task_list = tlist;
        done_list = dlist;
        limitter = lim;
        //System.out.println("Create thread: " + this.getName());
    };

    public long stat_total_RXed(){return stat_rxed.get(); }
    public long stat_files_copied(){return stat_files_copied.get(); }
    public long stat_files_downloaded(){return stat_files_downloaded.get(); }
    public long stat_files_failed(){return stat_files_failed.get(); }
    public long stat_files_ignored(){return stat_files_ignored.get(); }

    private boolean file_copy(String from, String to)
    {
        InputStream src = null;
        OutputStream dst = null;

        try
        {
            src = new FileInputStream(from);
            dst = new FileOutputStream(to);

            byte[] buf = new byte[4096];
            int cnt;

            while ((cnt = src.read(buf)) > 0)
                dst.write(buf, 0, cnt);

            //System.out.println("Copy done: " + from + " to " + to);
            return true;
        }
        catch (IOException e)
        {
            System.err.println("ERROR: Can't copy file: " + from + " to file: " + to);
            return false;
        }

        finally
        {
            try
            {
                if (src != null) src.close();
                if (dst != null) dst.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    private boolean find_and_copy(Task_item task)
    {
        int pos = 0;
        while (pos < done_list.size())
        //for(Task_item item : done_list) //commented for avoid ConcurrentModificationException
        {
            Task_item item = done_list.get(pos);
            pos++;

            if (item.getUrl_link().equals(task.getUrl_link()))
            {
                if (item.getFile_name().equals(task.getFile_name()))
                    stat_files_ignored.incrementAndGet();
                else {
                    if (file_copy(item.getFile_name(), task.getFile_name()))
                        stat_files_copied.incrementAndGet();
                    else
                        stat_files_failed.incrementAndGet();
                }
                return true;
            }
        }
        return false;
    }

    public void run()
    {
        try
        {
            boolean task_not_empty = task_list.size() > 0;
            //System.out.println("Start thread: " + this.getName());
            while (task_not_empty)
            {
                Task_item task = null;
                synchronized (task_list) {
                    task_not_empty = task_list.size() > 0;
                    if (!task_not_empty) break;
                    task = task_list.remove(0);
                }

                //System.out.println("Thread " + this.getName() + "; New task: " + task.to_string());

                if (!find_and_copy(task)){
                    if (Misc_Utils.load_file(task.getUrl_link(), task.getFile_name(), stat_rxed, limitter))
                    {
                        done_list.add(task);
                        stat_files_downloaded.incrementAndGet();
                    }
                    else
                        stat_files_failed.incrementAndGet();
                }

                Thread.sleep(0);
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread " + this.getName() + " interrupted");
        }
        catch (IndexOutOfBoundsException e)
        {
            //for case if tasks.size() = 0, but another thread delete last item between tasks.size and tasks.remove
            //fixed
            return;
        }
    }
}
