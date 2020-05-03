package threads;

import helperCore.RoundTime;
import listeners.ConfirmReactListener;

import java.time.OffsetDateTime;
import java.util.ArrayList;

public class AutoKickThread implements Runnable {
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            synchronized (this) {
                ArrayList<Integer> nidsToRem = new ArrayList<>();
                for (String s : ConfirmReactListener.rtimes.keySet()) {
                    RoundTime rt = ConfirmReactListener.rtimes.get(s);
                    if (rt.fireOn().isBefore(OffsetDateTime.now())) {
                        boolean remove = rt.OnTimeRunOut();
                        if (remove) nidsToRem.add(rt.getNid());
                    }
                }
                ArrayList<String> msgidsTorem = new ArrayList<>();
                for (String s : ConfirmReactListener.rtimes.keySet()) {
                    RoundTime rt = ConfirmReactListener.rtimes.get(s);
                    if (nidsToRem.contains(rt.getNid())) msgidsTorem.add(s);
                }
                for (String msg : msgidsTorem) {
                    ConfirmReactListener.rtimes.remove(msg);
                }
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
