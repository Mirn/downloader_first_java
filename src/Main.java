/**
 * Created by Easy on 2016.11.15.
 */
import java.io.*;
import java.util.*;

public class Main {

    public static void main (String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.out.println("HTML files multi thread downloader");
            System.out.println("USAGE: downloader <link_list> <threads count>");
            System.out.println();
            System.exit(1);
        }

        String list_fname = args[0];
        System.out.println("Load task list from: " + list_fname);

        List<Task_item> task_list = Collections.synchronizedList(new LinkedList<Task_item>());
        List<String> loaded_list = Misc_Utils.load_list(list_fname);
        Misc_Utils.task_list_parse(loaded_list, task_list);

        if (task_list.size() <= 0)
        {
            System.err.println("FATAL ERROR: nothing to do");
            System.exit(1);
        }
        else
            System.out.println("Load task list done\n");

        //System.exit(1);
        //Misc_Utils.load_file("http://78.47.239.56:62978/test/Mikuni.pk", "test_file.dat");

        Download_thread[] workers = new Download_thread[4];

        for (int pos = 0; pos < workers.length; pos++)
            workers[pos] = new Download_thread(task_list, "T" + pos);

        for (int pos = 0; pos < workers.length; pos++)
            workers[pos].start();

        int workers_runned = workers.length;
        while (workers_runned > 0)
        {
            workers_runned = 0;
            for (int pos = 0; pos < workers.length; pos++)
                if (workers[pos].isAlive())
                    workers_runned++;

            Thread.sleep(1000);
        }

        System.out.println("");
        System.out.println("ALL DONE");
    }
}