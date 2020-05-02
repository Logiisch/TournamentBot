package commands;

import listeners.commandListener;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.io.IOException;
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
        Role admin = event.getGuild().getRoleById(STATIC.ROLE_ADMIN);

        if (args.length<1) {
            if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
                event.getTextChannel().sendMessage("Bitte bestätige mit `"+prefix+"kick confirm`, dass du das Turnier verlassen willst.Das kannst du nicht mehr rückgängig machen!").queue();
            } else {
                event.getTextChannel().sendMessage("Usage: `"+prefix+"kick [Spieler als @Erwähnung/Anzahl zufälliger Nutzer]").queue();
            }
            return;
        }
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            if(args[0].equalsIgnoreCase("confirm")){
                STATIC.kickUser(event.getAuthor());
                event.getTextChannel().sendMessage("Du hast das Turnier verlassen!").queue();
                return;
            } else {
                event.getTextChannel().sendMessage("Bitte bestätige mit `"+prefix+"kick confirm`, dass du das Turnier verlassen willst.Das kannst du nicht mehr rückgängig machen!").queue();
            }
            return;
        }
        ArrayList<User> rem = new ArrayList<>();
        if (event.getMessage().getMentionedUsers().size()>0) {
            for (User u:event.getMessage().getMentionedUsers()) {
                STATIC.kickUser(u);
                rem.add(u);
            }
        } else {
            int tokick=0;
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
        String out = "Folgende(r) User wurde(n) gekickt:\n";
        for (User u: rem) {
            out += u.getName()+"\n";
        }
        event.getTextChannel().sendMessage(out).queue();

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
    public String Def() {
        return "Verlasse das Turnier";
    }
}
