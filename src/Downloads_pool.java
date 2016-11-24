import java.util.List;

/**
 * Created by Easy on 2016.11.24.
 */
final public class Downloads_pool {
    private Download_thread[] workers;

    public Downloads_pool(int count, List<Task_item> task_list, List<Task_item> done_list, Speed_limitter limitter)
    {
        workers = new Download_thread[count];

        for (int pos = 0; pos < workers.length; pos++)
            workers[pos] = new Download_thread("T" + pos, task_list, done_list, limitter);
    };

    public void start_all() {
        for (Download_thread i: workers)
            i.start();
    }

    public void run_all() {
        for (Download_thread i: workers)
            i.run();
    }

    public void join_all() throws InterruptedException {
        for (Download_thread i: workers)
            i.join();
    }

    public int isAlive_count() {
        int count = 0;
        for (Download_thread i: workers)
            if (i.isAlive())
                count++;
        return count;
    }

    public boolean run_second()
    {
        long time_old = System.currentTimeMillis();
        int pos = 0;
        while (System.currentTimeMillis() - time_old < 1000)
        {
            if (this.isAlive_count() == 0)
                return true;

            try
            {
                workers[pos].join(1000);
                pos = (pos + 1) % workers.length;
            }
            catch (InterruptedException e)
            {
                return true;
            }
        }
        return false;
    }

    public String stat_rxed_title() {
        String result = "";
        for (Download_thread i: workers) {
            result += i.getName() + "\t";;
        }
        return result;
    }

    public String stat_rxed_string() {
        String result = "";
        for (Download_thread i: workers)
            result += i.stat_total_RXed() + "\t";;
        return result;
    }

    public long all_total_rxed() {
        long sum = 0;
        for (Download_thread i: workers)
            sum += i.stat_total_RXed();
        return sum;
    }

    public long all_files_copied() {
        int sum = 0;
        for (Download_thread i: workers)
            sum += i.stat_files_copied();
        return sum;
    }

    public long all_files_downloaded() {
        int sum = 0;
        for (Download_thread i: workers)
            sum += i.stat_files_downloaded();
        return sum;
    }

    public long all_files_failed() {
        int sum = 0;
        for (Download_thread i: workers)
            sum += i.stat_files_failed();
        return sum;
    }

    public long all_files_ignored() {
        int sum = 0;
        for (Download_thread i: workers)
            sum += i.stat_files_ignored();
        return sum;
    }
}