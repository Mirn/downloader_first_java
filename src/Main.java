/**
 * Created by Easy on 2016.11.15.
 */
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class Main {

    public static void main (String[] args)
    {
        long time_start = System.currentTimeMillis();

        Cmd_params params = Cmd_params.parse_args(args);
        if ((params == null) || (!params.valid()))
        {
            Cmd_params.print_help();
            System.exit(1);
        }

        List<Task_item> task_list = task_list_load(params.getTask_list_fname(), params.getResults_dir());
        if (task_list.size() <= 0)
        {
            System.err.println("FATAL ERROR: nothing to do");
            System.exit(1);
        }
        else
            System.out.println("Load task list done\n");

        List<Task_item> done_list = Collections.synchronizedList(new LinkedList<Task_item>());

        Speed_limitter limitter = new Speed_limitter("Limitter", params.getSpeed_limit_kbs());
        Downloads_pool downloads_pool = new Downloads_pool(params.getThreads_count(), task_list, done_list, limitter);

        downloads_pool.start_all();

        System.out.println(downloads_pool.stat_rxed_title() + "\tSpeed");

        long rxed_old = 0;
        long time_old = System.currentTimeMillis();
        while (downloads_pool.isAlive_count() > 0)
        {
            downloads_pool.wait_all(1000);

            long time_period = System.currentTimeMillis() - time_old;
            time_old = System.currentTimeMillis();

            long rxed_new = downloads_pool.all_total_rxed();
            long speed = ((rxed_new - rxed_old) * 1000) / Math.max(1, time_period);
            rxed_old = rxed_new;

            System.out.println(downloads_pool.stat_rxed_string() + "\t" + speed); // + "\t\t" + time_period);
        }
        System.out.println("ALL DONE");
        System.out.println();

        long work_time_ms = System.currentTimeMillis() - time_start;
        print_statistics(downloads_pool, work_time_ms);

        System.exit(0);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static List<Task_item> task_list_load(String fname, String subdir)
    {
        List<Task_item> task_list = Collections.synchronizedList(new LinkedList<Task_item>());
        List<String> loaded_list = Misc_Utils.load_list(fname);
        Misc_Utils.task_list_parse(loaded_list, task_list, subdir);

        return task_list;
    };

    static String long_formatter(long v)
    {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');

        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
        return formatter.format(v);
    }

    static void print_statistics(Downloads_pool downloads_pool, long work_time_ms)
    {
        double stat_time_work = (work_time_ms) / 1000.0;

        System.out.println("Statistics:");
        System.out.println("files copied     : " + downloads_pool.all_files_copied());
        System.out.println("files downloaded : " + downloads_pool.all_files_downloaded());
        System.out.println("files failed     : " + downloads_pool.all_files_failed());
        System.out.println("files ignored    : " + downloads_pool.all_files_ignored());
        System.out.println("received total   : " + long_formatter(downloads_pool.all_total_rxed()) + " bytes");
        System.out.println("work time        : " + stat_time_work + " sec");
        System.out.println("Download speed   : " +
                long_formatter((long)(downloads_pool.all_total_rxed() / stat_time_work)) + " bytes/sec");
    }
}

/*
TODO
4 arraylist done_list --> hashset
6 убрать continue cmd_params
9 try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
 */