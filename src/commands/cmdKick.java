package commands;

import listeners.commandListener;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.ArrayList;
import java.util.Objects;

public class cmdKick implements Command{

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event)  {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role admin = event.getGuild().getRoleById(STATIC.ROLE_HELPER);
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Das kann nur ein Helfer machen").queue();
            return;
        }

        if (args.length<1) {

            event.getTextChannel().sendMessage("Usage: `"+prefix+"kick [Spieler als @Erwähnung/Anzahl zufälliger Nutzer]").queue();
            return;
        }

        ArrayList<User> rem = new ArrayList<>();
        if (event.getMessage().getMentionedUsers().size()>0) {
            for (User u:event.getMessage().getMentionedUsers()) {
                STATIC.kickUser(u);
                rem.add(u);
            }
        } else {
            int tokick;
            try {
                tokick=Integer.parseInt(args[0]);
            } catch (Exception e) {
                event.getTextChannel().sendMessage("Usage: `"+prefix+"kick [Spieler als @Erwähnung/Anzahl zufälliger Nutzer]`").queue();
                return;
            }
            while (tokick>0) {
                ArrayList<User> teilnehmer = new ArrayList<>();

                for (Member m:event.getGuild().getMembers()) {

                    if (!STATIC.getNotIncluded().contains(m.getUser().getId())&&!m.getUser().isBot()) teilnehmer.add(m.getUser());

                }
                int i = (int)Math.round(Math.random()*teilnehmer.size());
                while (i>=teilnehmer.size()) i--;
                User u = teilnehmer.get(i);
                STATIC.kickUser(u);
                rem.add(u);
                tokick--;
            }


        }
        StringBuilder out = new StringBuilder("Folgende(r) User wurde(n) gekickt:\n");
        for (User u: rem) {
            out.append(u.getName()).append("\n");
        }
        event.getTextChannel().sendMessage(out.toString()).queue();

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
        return "Kicke User vom Turnier";
    }
}
