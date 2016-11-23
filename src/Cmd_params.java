import java.io.File;

/**
 * Created by Easy on 2016.11.23.
 */
final public class Cmd_params {
    public int threads_count;
    public int speed_limit_kbs;
    public String task_list_fname;
    public String results_dir;

    public Cmd_params()
    {
        threads_count = 1;
        speed_limit_kbs = Integer.MAX_VALUE;
        task_list_fname = "";
        results_dir = "";
    }

    public Cmd_params(int threads, int limit, String task_list, String res_dir)
    {
        threads_count = threads;
        speed_limit_kbs = limit;
        task_list_fname = task_list;
        results_dir = res_dir;
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

    public static void error_help_and_exit(String err_msg)
    {
        System.err.println(err_msg);
        print_help();
        System.exit(1);
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
                    error_help_and_exit("ERROR: threads_count invalid number: " + value);
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
                    error_help_and_exit("ERROR: speed_limit invalid number: " + value);
                }
                continue;
            }

            System.err.println("ERROR: unknown parameter : " + key);
            pos--;
        };

        if (!params.valid())
            error_help_and_exit("FATAL ERROR: parameters not valid");

        params.print_all();
        return params;
    }
}
