/**
 * Created by Easy on 2016.11.18.
 */
final public class Task_item {
    private String url_link;
    private String file_name;

    public Task_item(String url, String fname)
    {
        this.url_link = url;
        this.file_name = fname;
    }

    public String toString(){return "\"" + url_link + "\"\t\"" + file_name + "\""; }

    public static Task_item parse_string(String str, String subdir, int line_num)
    {
        String url_link;
        String file_name;

        if(!str.contains(" "))
        {
            System.err.println("ERROR: in line #" + line_num + " \"" + str + "\" need two parameters with space separator");
            return null;
        }

        url_link  = str.substring(0, str.indexOf(" "));
        file_name = str.substring(str.indexOf(" "), str.length());
        file_name = file_name.trim();

        if (url_link.length() == 0)
        {
            System.err.println("ERROR: in line #" + line_num + " \"" + str + "\" url link is void");
            return null;
        }

        if (file_name.length() == 0)
        {
            System.err.println("ERROR: in line #" + line_num + " \"" + str + "\" file name void");
            return null;
        }

        return new Task_item(url_link, subdir + file_name);
    }

    public String getUrl_link() {
        return url_link;
    }

    public String getFile_name() {
        return file_name;
    }
}
