package commands;

import helperCore.LangManager;
import helperCore.retryOnDemand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": "+ LangManager.get(event.getGuild(),"cmdRetryNothingToDo")).queue();
            return;
        }
        boolean res = retryLater.get(event.getAuthor()).tryRun(event.getAuthor());
        if (res) {
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": "+LangManager.get(event.getGuild(),"cmdRetrySuccess")).queue();
            retryLater.remove(event.getAuthor());
        } else {
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+":" +LangManager.get(event.getGuild(),"cmdRetryFailure").replace("%MSG%",retryLater.get(event.getAuthor()).whatiscurrentlywrong(event.getGuild()))).queue();
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
    public String Def(String prefix, Guild g) {
        return LangManager.get(g,"cmdRetryDef");
    }
}
