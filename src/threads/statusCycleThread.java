package threads;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import java.util.ArrayList;

public class statusCycleThread implements Runnable {

    public static ArrayList<String> cycleList = new ArrayList<>();
    private static int count=-1;
    public static int seksShowing=10;
    public static boolean cycle = false;
    public static JDA jda;
    public statusCycleThread(JDA jd) {
        jda = jd;
        cycle= true;
    }
    @Override
    public void run() {
        while (cycle) {
            count++;
            if (count>=cycleList.size()) count=0;
            String stat = cycleList.get(count);
            jda.getPresence().setActivity(Activity.playing(stat));
            try {
                Thread.sleep(seksShowing*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void reset() {
        count=-1;
    }
}
