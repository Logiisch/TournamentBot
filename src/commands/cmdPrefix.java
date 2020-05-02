package commands;

import listeners.commandListener;
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
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role helper = event.getGuild().getRoleById(STATIC.ROLE_HELPER);
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(helper)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Das kann nur ein Helfer machen!").queue();
            return;
        }
        if (args.length<1) {
            event.getTextChannel().sendMessage("Usage: `"+prefix+"`prefix [Neues Prefix]`").queue();
            return;
        }
        STATIC.changePrefix(args[0]);
        event.getTextChannel().sendMessage("Prefix erfogreich geändert!").queue();
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
        return "Ändere das Prefix!";
    }
}
