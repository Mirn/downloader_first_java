/**
 * Created by Easy on 2016.11.15.
 */
import java.io.*;
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

        List<Task_item> done_list = Collections.synchronizedList(new LinkedList<Task_item>());
        List<Task_item> task_list = task_list_load(params.getTask_list_fname(), params.getResults_dir());

        Download_thread[] workers = new Download_thread[params.getThreads_count()];
        Speed_limitter limitter = new Speed_limitter("Limitter", params.getSpeed_limit_kbs());

        for (int pos = 0; pos < workers.length; pos++)
            workers[pos] = new Download_thread("T" + pos, task_list, done_list, limitter);

        for (int pos = 0; pos < workers.length; pos++)
            System.out.print(workers[pos].getName() + "\t");;
        System.out.println("\tSpeed");

        for (int pos = 0; pos < workers.length; pos++)
            workers[pos].start();

        long rxed_old = 0;
        int workers_runned = workers.length;
        long time_old = System.currentTimeMillis();
        long time_period = 0;
        while (workers_runned > 0)
        {
            try
            {
                while (System.currentTimeMillis() - time_old < 1000)
                {
                    workers_runned = 0;
                    for (int pos = 0; pos < workers.length; pos++)
                        if (workers[pos].isAlive())
                            workers_runned++;

                    if (workers_runned == 0)
                        break;

                    Thread.sleep(1);
                }
                time_period = System.currentTimeMillis() - time_old;
                time_old = System.currentTimeMillis();
            }
            catch (InterruptedException e)
            {
                System.err.println("Interrupted");
                System.exit(1);
            }

            long rxed_new = 0;
            for (int pos = 0; pos < workers.length; pos++)
            {
                long current = workers[pos].stat_total_RXed();
                rxed_new += current;
                System.out.print(current + "\t");
            }

            long speed = ((rxed_new - rxed_old) * 1000) / time_period;
            rxed_old = rxed_new;

            System.out.println("\t" + speed);
        }
        System.out.println("ALL DONE");
        System.out.println();

        long work_time_ms = System.currentTimeMillis() - time_start;
        print_statistics(workers, work_time_ms);

        System.exit(0);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static List<Task_item> task_list_load(String fname, String subdir)
    {
        List<Task_item> task_list = Collections.synchronizedList(new LinkedList<Task_item>());
        List<String> loaded_list = Misc_Utils.load_list(fname);
        Misc_Utils.task_list_parse(loaded_list, task_list, subdir);

        if (task_list.size() <= 0)
        {
            System.err.println("FATAL ERROR: nothing to do");
            System.exit(1);
        }
        else
            System.out.println("Load task list from: " + fname + " done\n");

        return task_list;
    };

    static void print_statistics(Download_thread[] workers, long work_time_ms)
    {
        long stat_rxed = 0;
        int stat_copied = 0;
        int stat_downloaded = 0;
        int stat_failed = 0;
        int stat_ignored = 0;
        double stat_time_work = (work_time_ms) / 1000.0;

        for (int pos = 0; pos < workers.length; pos++)
        {
            stat_rxed       += workers[pos].stat_total_RXed();
            stat_copied     += workers[pos].stat_files_copied();
            stat_downloaded += workers[pos].stat_files_downloaded();
            stat_failed     += workers[pos].stat_files_failed();
            stat_ignored    += workers[pos].stat_files_ignored();
        }

        System.out.println("Statistics:");
        System.out.println("files copied     : " + stat_copied);
        System.out.println("files downloaded : " + stat_downloaded);
        System.out.println("files failed     : " + stat_failed);
        System.out.println("files ignored    : " + stat_ignored);
        System.out.println("received bytes   : " + stat_rxed);
        System.out.println("work time        : " + stat_time_work + " sec");
        System.out.println("Download speed   : " + (int)(stat_rxed / stat_time_work) + " bytes/sec");
    }
}

/*
TODO
1 sys exit убрать
2 this(1, Integer.MAX_VALUE, "", "");
3 toString
4 arraylist hash
5 try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
6 убрать continue cmd_params
7 while (task_list.size() > 0) & remove atomic
8 while (!limitter.check_ready(cnt))   Thread.sleep(1); -->wait
9 try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
10 паблик поля убрать
 */