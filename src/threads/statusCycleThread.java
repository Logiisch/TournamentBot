package threads;

import helperCore.SimpleString;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import util.STATIC;

import java.util.ArrayList;
import java.util.HashMap;

public class statusCycleThread implements Runnable {
    public static HashMap<String, SimpleString> replacements= new HashMap<>();
    public static ArrayList<String> cycleList = new ArrayList<>();
    private static int count=-1;
    public static int seksShowing=6;
    public static boolean cycle = false;
    public static JDA jda;
    public static Activity.ActivityType at = Activity.ActivityType.DEFAULT;
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
            jda.getPresence().setActivity(getActivity(replaceString(stat,jda.getGuildById(STATIC.TESTGUILD))));
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

    public static Activity getActivity(String whatToShow) {
        return Activity.of(at,whatToShow);
    }
    public static String replaceString(String in, Guild g) {
        for (String part:replacements.keySet()) {
            if (in.contains(part)) {
                in = in.replace(part,replacements.get(part).getString(g));
            }
        }
        return in;
    }
}
