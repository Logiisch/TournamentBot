package listeners;

import helperCore.Logic;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import util.STATIC;

import javax.annotation.Nonnull;

public class readyListener extends ListenerAdapter {

    public void onReady(@Nonnull ReadyEvent event) {
       if(STATIC.loadNotIncluded()) {
           System.out.println("notIncluded geladen!");
       } else {
           System.out.println("notIncluded konnte nicht geladen werden!");
       }
        if(Logic.loadNodes(event.getJDA())) {
            System.out.println("Nodes geladen!");
        } else {
            System.out.println("Nodes konnte nicht geladen werden!");
        }
    }
}