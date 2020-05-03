package commands;

import helperCore.Logic;
import listeners.ConfirmReactListener;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class cmdReset implements Command {
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
        if (args.length<1) {
            event.getTextChannel().sendMessage("Nutze zum Bestätigen `"+prefix+"delete confirm`").queue();
            return;
        }
        if(!args[0].equalsIgnoreCase("confirm")) {
            event.getTextChannel().sendMessage("Nutze zum Bestätigen `"+prefix+"delete confirm`").queue();
            return;
        }
        Logic.nodes.clear();
        ConfirmReactListener.toConfirmResult.clear();
        File f = new File ("data/nodes.txt");
        if (f.exists()) f.delete();
        TextChannel tc =event.getGuild().getTextChannelById(STATIC.CHANNEL_RESULTS);
        assert tc != null;
        try {
            tc.deleteMessages(tc.getHistoryFromBeginning(100).complete().getRetrievedHistory()).queue();
        } catch (Exception e) {
            event.getTextChannel().sendMessage("Anscheinend gab es beim Löschen der Nachrichten einen Fehler: "+e.getMessage()).queue();
        }
        event.getTextChannel().sendMessage("Es werden nun noch die Chatverläufe der Privathchats geleert. Dies kann einen Moment dauern. Bitte starte in der zeit noch kein Turnier, da sonst das Risiko besteht, dass neue Nachrichten gelöscht werden").queue();
        for (Member m:event.getGuild().getMembers()) {
            removeRoles(m);
            removeMemberConversation(m);
        }

        event.getTextChannel().sendMessage("Das Turnier wurde erfolgreich gelöscht. Starte es wieder mit `"+prefix+"start`!").queue();
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
        return "Setzte das Turnier zurück!";
    }

    private void removeRoles(Member m) {
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(m.getGuild().getRoleById(STATIC.ROLE_TOT));
        roles.add(m.getGuild().getRoleById(STATIC.ROLE_FINALE));
        roles.add(m.getGuild().getRoleById(STATIC.ROLE_HALBFINALE));
        roles.add(m.getGuild().getRoleById(STATIC.ROLE_VIERTELFINALE));
        roles.add(m.getGuild().getRoleById(STATIC.ROLE_ACHTELFINALE));
        roles.add(m.getGuild().getRoleById(STATIC.ROLE_VORRUNDE2));
        roles.add(m.getGuild().getRoleById(STATIC.ROLE_VORRUNDE1));
        for (Role r:roles) {
            //if (m.getRoles().contains(r))
                m.getGuild().removeRoleFromMember(m,r).queue();
        }

    }
    private void removeMemberConversation(Member m) {
        if (m.getUser().getId().equalsIgnoreCase(m.getJDA().getSelfUser().getId()))return;
        PrivateChannel pc =m.getUser().openPrivateChannel().complete();
        MessageHistory mh =pc.getHistoryFromBeginning(100).complete();
        if (mh.size()==0) return;
        for (Message ms:mh.getRetrievedHistory()) {
            if (!ms.getAuthor().getId().equalsIgnoreCase(m.getJDA().getSelfUser().getId())) continue;
            pc.deleteMessageById(ms.getId()).queue();
        }
    }
}
