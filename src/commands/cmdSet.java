package commands;

import helperCore.Logic;
import helperCore.PermissionLevel;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.Objects;

public class cmdSet implements Command {
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
        if (Logic.nodes.size()==0) {
            event.getTextChannel().sendMessage("Immer ruhig. Das Turnier hat noch nicht einmal begonnen!").queue();
            return;
        }
        if (args.length<1||(event.getMessage().getMentionedUsers().size()==0)&&event.getMessage().getMentionedMembers().size()==0) {
            event.getTextChannel().sendMessage("Usage: `"+prefix+"set [User als @Erwähnung] [winner/looser]`").queue();
            return;
        }
        if (event.getMessage().getContentDisplay().contains("loser")&&event.getMessage().getContentDisplay().contains("winner")) {
            event.getTextChannel().sendMessage("Du musst dich schon zwischen looser oder winner entscheiden!").queue();
            return;
        }
        if (event.getMessage().getMentionedUsers().size()!=1) {
            event.getTextChannel().sendMessage("Du musst dich schon für genau einen User entscheiden!").queue();
            return;
        }
        User u = event.getMessage().getMentionedUsers().get(0);

        if (event.getMessage().getContentDisplay().contains("winner")) {
            if(u.getId().equalsIgnoreCase(event.getAuthor().getId())) {
                event.getTextChannel().sendMessage("Sir-Mastermind-Sperre: Bitte lasse das von einem anderen Helfer eintragen :)").queue();
                return;
            }

            try {
                Logic.logresult(u,true,event.getGuild());
                event.getTextChannel().sendMessage("Erfolgreich Gewinner festgelgt!!").queue();
            } catch (Exception e) {
                event.getTextChannel().sendMessage("Scheinbar ist ein Fehler aufgetreten\n"+e.getMessage()).queue();
                e.printStackTrace();
            }
            return;
        }
        if (event.getMessage().getContentDisplay().contains("loser")) {
            try {
                Logic.logresult(u,false,event.getGuild());
                event.getTextChannel().sendMessage("Erfolgreich Verlierer festgelgt!!").queue();
            } catch (Exception e) {
                event.getTextChannel().sendMessage("Scheinbar ist ein Fehler aufgetreten\n"+e.getMessage()).queue();
                //e.printStackTrace();
            }
            return;
        }
        event.getTextChannel().sendMessage("Du musst mir schon sagen, ob der Spieler verloren oder gewonnen hat...").queue();
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
        return "Setze das Ergebnis eines noch nicht eingetragenen Matches!";
    }
}
