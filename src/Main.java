/**
 * Created by Easy on 2016.11.15.
 */
import java.io.*;
import java.util.*;

public class Main {

    public static void main (String[] args)
    {
        if (args.length < 2) {
            System.out.println("HTML files multi thread downloader");
            System.out.println("USAGE: downloader <link_list> <threads_count>");
            System.out.println();
            System.exit(1);
        }

        String arg_list_fname = args[0];
        String arg_threads_count = args[1];

        int threads_count = 1;
        try
        {
            threads_count = Integer.parseInt(arg_threads_count);
        }
        catch (NumberFormatException e)
        {
            System.err.println("ERROR: threads_count invalid number (param #2), string:" + arg_threads_count);
            System.exit(1);
        }

        System.out.println("Load task list from: " + arg_list_fname);

        List<Task_item> task_list = Collections.synchronizedList(new LinkedList<Task_item>());
        List<String> loaded_list = Misc_Utils.load_list(arg_list_fname);
        Misc_Utils.task_list_parse(loaded_list, task_list);

        if (task_list.size() <= 0)
        {
            System.err.println("FATAL ERROR: nothing to do");
            System.exit(1);
        }
        else
            System.out.println("Load task list done\n");

        Download_thread[] workers = new Download_thread[threads_count];
        Speed_limitter limitter = new Speed_limitter("Limitter", 850);

        for (int pos = 0; pos < workers.length; pos++)
            workers[pos] = new Download_thread("T" + pos, task_list, limitter);

        for (int pos = 0; pos < workers.length; pos++)
            System.out.print(workers[pos].getName() + "\t");;
        System.out.println("\tSpeed");

        for (int pos = 0; pos < workers.length; pos++)
            workers[pos].start();

        long rxed_old = 0;
        int workers_runned = workers.length;
        while (workers_runned > 0)
        {
            workers_runned = 0;
            for (int pos = 0; pos < workers.length; pos++)
                if (workers[pos].isAlive())
                    workers_runned++;

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                System.err.println("Interrupted");
                System.exit(1);
            }

            long rxed_new = 0;
            for (int pos = 0; pos < workers.length; pos++)
            {
                long current = workers[pos].RXed_get();
                rxed_new += current;
                System.out.print(current + "\t");
            }

            long speed = rxed_new - rxed_old;
            rxed_old = rxed_new;

            System.out.println("\t" + speed);
        }

        System.out.println("");
        System.out.println("ALL DONE");
        System.exit(0);
    }
}