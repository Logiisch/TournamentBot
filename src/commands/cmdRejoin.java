package commands;

import helperCore.Logic;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.Objects;

public class cmdRejoin implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role admin = event.getGuild().getRoleById(STATIC.ROLE_ADMIN);
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Das kann nur ein Admin machen!").queue();
            return;
        }
        if (!Logic.nodes.isEmpty()) {
            event.getTextChannel().sendMessage("Das Turnier hat schon gestartet, Aus technischen Gründen ist es daher nicht möglich, den User wieder hinzuzufüügen!").queue();
            return;
        }
        if (args.length<1) {
            event.getTextChannel().sendMessage("Usage: `"+prefix+"rejojn [Spieler als @Erwähnung/all]").queue();
            return;
        }
        if (args[0].equalsIgnoreCase("all")) {
            STATIC.rejoinAll();
            event.getTextChannel().sendMessage("Alle User dürfen nun wieder mitspielen!").queue();
            return;
        }
        if(event.getMessage().getMentionedUsers().isEmpty()) {
            event.getTextChannel().sendMessage("Usage: `"+prefix+"rejojn [Spieler als @Erwähnung/all]").queue();
            return;
        }
        for (User u: event.getMessage().getMentionedUsers()) {
            STATIC.rejoinUser(u);
        }
        event.getTextChannel().sendMessage("Es dürfen/darf nun "+event.getMessage().getMentionedUsers().size()+" Spieler wieder mitspielen!").queue();
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
    public String Def(String prefix) {
        return "Hole bereits gekickte Personen zurück ins Turnier!";
    }
}
