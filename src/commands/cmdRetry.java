package commands;

import helperCore.retryOnDemand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class cmdRetry implements Command {
    public static HashMap<User , retryOnDemand> retryLater =  new HashMap<>();
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event)  {
        if (!retryLater.containsKey(event.getAuthor())) {
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Aktuell gibt es nichts, was zu tun ist!").queue();
            return;
        }
        boolean res = retryLater.get(event.getAuthor()).tryRun(event.getAuthor());
        if (res) {
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Erfolgreich!").queue();
            retryLater.remove(event.getAuthor());
        } else {
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Hm, das scheint nicht geklappt zu haben...\n"+retryLater.get(event.getAuthor()).whatiscurrentlywrong()).queue();
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public boolean isPrivate() {
        return true;
    }

    @Override
    public String Def() {
        return "Befehl, um bei unzustellbaren Nachrichten die Zustellung erneut zu probieren. ";
    }
}
