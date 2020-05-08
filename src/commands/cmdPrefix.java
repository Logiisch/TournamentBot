package commands;

import helperCore.LangManager;
import helperCore.PermissionLevel;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.Objects;

public class cmdPrefix implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());

        if (args.length<1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdPrefixUsage").replace("%PREFIX%",prefix)).queue();
            return;
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            "abc".replaceFirst(args[0],"");
        } catch (Exception e) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdPrefixNotAllowed")).queue();
            return;

        }
        STATIC.setSettings(event.getGuild().getId(),"PREFIX",args[0]);
        event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdPrefixSuccess").replace("%PREFIX%",STATIC.getSettings(event.getGuild(),"PREFIX"))).queue();
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
        return LangManager.get(g,"cmdPrefixDef");
    }
}
