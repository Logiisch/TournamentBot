package commands;

import helperCore.Logic;
import helperCore.PermissionLevel;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.Objects;

public class cmdRevert implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.HELPER;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role helper = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_HELPER"));
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(helper)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Das kann nur ein Helfer machen!").queue();
            return;
        }
        if (args.length<1||event.getMessage().getMentionedUsers().size()==0) {
            event.getTextChannel().sendMessage("Usage: `"+prefix+"revert [User als @Erwähnung]`").queue();
            return;
        }
        if (event.getMessage().getMentionedUsers().size()>1) {
            event.getTextChannel().sendMessage("Bitte immer nur für alle user einzeln durchführen!").queue();
            return;
        }
        User u = event.getMessage().getMentionedUsers().get(0);
        if (!event.getMessage().getContentDisplay().toLowerCase().contains("confirm")) {
            event.getTextChannel().sendMessage("Bist du dir sicher, den User "+u.getName()+" eine Stufe zurück zu setzen? Dann wiederhole den Befehl bitte und füge `confirm` am Ende an!").queue();
            return;
        }
        try {
            Logic.revert(u,event.getGuild());
            event.getTextChannel().sendMessage("User erfolgreich zurückgesetzt!").queue();
        } catch (Exception e) {
            event.getTextChannel().sendMessage("Anscheinend gab es beim Zurücksetzen eine Fehler "+e.getMessage()).queue();
            e.printStackTrace();
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
    public String Def(String prefix) {
        return "Setze einen Spieler eine Stufe zurück!";
    }
}
