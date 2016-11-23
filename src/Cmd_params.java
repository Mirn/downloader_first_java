import java.io.File;

/**
 * Created by Easy on 2016.11.23.
 */
final public class Cmd_params {
    private int threads_count;
    private int speed_limit_kbs;
    private String task_list_fname;
    private String results_dir;

    private Cmd_params()
    {
        this(1, Integer.MAX_VALUE, "", "");
    }

    private Cmd_params(int threads, int limit, String task_list, String res_dir)
    {
        threads_count = threads;
        speed_limit_kbs = limit;
        task_list_fname = task_list;
        results_dir = res_dir;
    }


    public int getThreads_count() {
        return threads_count;
    }

    public int getSpeed_limit_kbs() {
        return speed_limit_kbs;
    }

    public String getTask_list_fname() {
        return task_list_fname;
    }

    public String getResults_dir() {
        return results_dir;
    }

    public boolean valid()
    {
        boolean task_norm = (task_list_fname.length() > 0);
        boolean threads_norm = (threads_count > 0) && (threads_count < 0x8000);
        boolean speed_norm = (speed_limit_kbs > 0);

        boolean all_valid = task_norm && threads_norm && speed_norm;
        return all_valid;
    };

    public static void print_help()
    {
        System.out.println("Использование:");
        System.out.println("\t-n количество одновременно качающих потоков (1,2,3,4....)");
        System.out.println("\t-l общее ограничение на скорость скачивания, для всех потоков, размерность - байт/секунда, можно использовать суффиксы k,m (k=1024, m=1024*1024)");
        System.out.println("\t-f путь к файлу со списком ссылок");
        System.out.println("\t-o имя папки, куда складывать скачанные файлы");
        System.out.println();

        System.out.println("Формат файла со ссылками:\n" +
                "\n" +
                "<HTTP ссылка><пробел><имя файла, под которым его надо сохранить>\n" +
                "пример:\n" +
                "\n" +
                "http://example.com/archive.zip my_archive.zip\n" +
                "http://example.com/image.jpg picture.jpg");
        System.out.println();
        System.out.println("В HTTP ссылке нет пробелов, нет encoded символов и прочей ерунды - это всегда обычные ссылки с английскими символами без специальных символов в именах файлов и прочее. Короче - ссылкам можно не делать decode. Ссылки без авторизации, не HTTPS/FTP - всегда только HTTP-протокол.");
        System.out.println();
    }

    public void print_all()
    {
        System.out.println("Params:");
        System.out.println("\t.Threads_count   : " + threads_count);
        System.out.println("\t.speed_limit_kbs : " + speed_limit_kbs);
        System.out.println("\t.task_list_fname : " + task_list_fname);
        System.out.println("\t.results_dir     : " + results_dir);
        System.out.println();
    }

    static Cmd_params parse_args(String[] args)
    {
        Cmd_params params = new Cmd_params();

        int pos = 0;
        while ((pos + 1) < args.length)
        {
            String key   = args[pos + 0];
            String value = args[pos + 1];
            pos += 2;

            if (key.equals("-f"))
            {
                params.task_list_fname = value;
                continue;
            }

            if (key.equals("-o"))
            {
                if (value.charAt(value.length()-1) != File.separatorChar)
                        value = value + File.separatorChar;

                params.results_dir = value;
                continue;
            }

            if (key.equals("-n"))
            {
                try
                {
                    params.threads_count = Integer.parseInt(value);
                }
                catch (NumberFormatException e)
                {
                    System.err.println("ERROR: threads_count invalid number: " + value);
                }
                continue;
            }

            if (key.equals("-l"))
            {
                char suffix = value.charAt(value.length()-1);
                long multiplier = 1;
                if (Character.toUpperCase(suffix) == 'K') multiplier = 1024;
                if (Character.toUpperCase(suffix) == 'M') multiplier = 1024*1024;
                if ((multiplier != 1) && (value.length() > 1))
                    value = value.substring(0, value.length()-1);

                try
                {
                    params.speed_limit_kbs = (int)((Long.parseLong(value) * multiplier) / 1000);
                }
                catch (NumberFormatException e)
                {
                    System.err.println("ERROR: speed_limit invalid number: " + value);
                }
                continue;
            }

            System.err.println("ERROR: unknown parameter : " + key);
            pos--;
        };

        params.print_all();
        return params;
    }
}
