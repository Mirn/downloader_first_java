import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Easy on 2016.11.22.
 */
final public class Speed_limitter extends Thread {
    private long speed_limit;
    private AtomicLong current_limit;

    public Speed_limitter(String name, long limit_kbSec)
    {
        super(name);
        current_limit = new AtomicLong(0);
        speed_limit = limit_kbSec * 1000;
        this.start();
    };

    public boolean check_ready(int block_size)
    {
        if (current_limit.get() < block_size)
            return false;

        current_limit.addAndGet(-block_size);
        return true;
    }

    public void run()
    {
        try
        {
            long time_old = System.currentTimeMillis();

            while (!Thread.interrupted())
            {
                Thread.sleep(1);

                long time_new = System.currentTimeMillis();
                long time_dlt = time_new - time_old;
                time_old = time_new;

                long limit_dlt = time_dlt * (speed_limit / 1000);
                if ((current_limit.get() + limit_dlt) > speed_limit)
                    limit_dlt = speed_limit - current_limit.get();

                current_limit.addAndGet(limit_dlt);
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("Thread " + this.getName() + " interrupted");
        }
    }
}
