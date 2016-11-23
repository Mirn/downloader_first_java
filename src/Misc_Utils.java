import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

final public class Misc_Utils {
    public static List<String> load_list(String file_name)
    {
        File f = null;
        BufferedReader in = null;
        FileReader fr = null;
        List<String> list = new ArrayList<String>();

        try
        {
            f = new File(file_name);
            if (!f.exists())
            {
                System.err.println("ERROR: " + file_name + " not found");
                return list;
            }

            fr = new FileReader(f);
            in = new BufferedReader(fr);

            String str = "";
            while ((str = in.readLine()) != null)
                list.add(str);
        }

        catch (IOException e)
        {
            System.err.println("ERROR: Can't load " + file_name);
        }

        catch (Exception e)
        {
            System.err.println("ERROR: unknow exception when load " + file_name);
            e.printStackTrace();
        }

        finally {
            try {
                if (in != null) in.close();
                if (fr != null) fr.close();
            } catch (IOException e) {
            }
        }

        return list;
    }

    public static int task_list_parse(List<String> strings_list, List<Task_item> task_list, String subdir)
    {
        for (int line_num = 0; line_num < strings_list.size(); line_num++)
        {
            String str = strings_list.get(line_num);
            Task_item task_item = Task_item.parse_string(str, subdir, line_num + 1);

            if (task_item == null)
                continue;

            task_list.add(task_item);
            //System.out.println("OK: " + task_item.to_string());
        }

        return task_list.size();
    }

    public static boolean load_file(String url_link, String file_name, AtomicLong stat_rx, Speed_limitter limitter) throws InterruptedException
    {
        URL url = null;
        InputStream is = null;
        BufferedInputStream inputStream = null;
        FileOutputStream file_out = null;

        int BLOCK_SIZE = 512;
        byte[] buf = new byte[BLOCK_SIZE];

        try
        {
            try
            {
                file_out = new FileOutputStream(file_name);
            }
            catch (IOException ioe)
            {

                System.err.println("ERROR: can't create file " + file_name + " for save content");
                return false;
            }

            url = new URL(url_link);
            is = url.openStream();
            inputStream = new BufferedInputStream(is);

            int cnt = 0;
            while ((cnt = inputStream.read(buf)) >= 0)
            {
                if (cnt == 0)
                    continue;

                if (limitter != null)
                    while (!limitter.check_ready(cnt))
                        Thread.sleep(1);

                stat_rx.addAndGet(cnt);

                file_out.write(buf, 0, cnt);
                //System.out.print(".");
                //System.out.println("rd:\t" + cnt + "\tinputStream.available():\t" + inputStream.available());
            }
            return true;
        }

        catch (MalformedURLException mue)
        {
            System.err.println("ERROR: can't download" + url_link);
            return false;
        }

        catch (IOException ioe)
        {
            System.err.println("ERROR: io error on link:" + url_link + " and file:" + file_name);
            return false;
        }

        finally
        {
            try
            {
                if (is != null) is.close();
                if (file_out != null) file_out.close();
            }

            catch (IOException ioe)
            {
            }
        }
    }
}
