package commands;

import helperCore.LangManager;
import helperCore.PermissionLevel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class cmdReload implements Command{

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        LangManager.load();
        event.getTextChannel().sendMessage("Erfolgreich geladen!").queue();
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
        return false;
    }

    @Override
    public String Def(String prefix, Guild g) {
        return "Lade die LANG-Dateien neu";
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.BOTOWNER;
    }
}
