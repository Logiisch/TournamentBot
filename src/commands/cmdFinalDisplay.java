package commands;

import helperCore.Logic;
import helperCore.PermissionLevel;
import helperCore.TournamentNode;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class cmdFinalDisplay implements Command {
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
        Role admin = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_ADMIN"));
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Das kann nur ein Admin machen!").queue();
            return;
        }
        if (Logic.nodes.isEmpty()) {
            event.getTextChannel().sendMessage("Das Turnier hat noch nicht gestartet!").queue();
            return;
        }
        if (Logic.nodes.get(1).winner==null) {
            event.getTextChannel().sendMessage("Der Gewinner steht noch nicht fest!").queue();
            return;
        }
         if (event.getMessage().getMentionedChannels().isEmpty()) {
             event.getTextChannel().sendMessage("Bitte gib einen Channel an, um das Endergebnis dort reinzusenden!!").queue();
             return;
         }
        User winner = Logic.nodes.get(1).winner;
        String wstring = "";
        if (event.getGuild().isMember(winner)) wstring = Objects.requireNonNull(event.getGuild().getMember(winner)).getAsMention(); else wstring = winner.getName();
        ArrayList<String> users = new ArrayList<>();
        for (Integer nid : Logic.nodes.keySet()) {
            TournamentNode tn = Logic.nodes.get(nid);
            for (User ta:tn.players) {
                if (!users.contains(ta.getId())) users.add(ta.getId());
            }
        }
        users.remove(event.getJDA().getSelfUser().getId());
        try {
            cmdBracket.drawImage(winner);
        } catch (Exception e) {
            event.getTextChannel().sendMessage("Leider trat beim versuch ein Fehler auf!"+e.getMessage()).queue();
            e.printStackTrace();
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        String out = "Das Turnier vom "+now.getDayOfMonth()+". "+now.getMonthValue()+". "+now.getYear()+ " hat der Spieler "+wstring+" gewonnen. Insgesamt haben "+users.size()+" Spieler teilgenommen. Wir bedanken uns bei allen Spielern!";
        TextChannel sendto = event.getMessage().getMentionedChannels().get(0);
        sendto.sendMessage(out).queue();
        sendto.sendFile(new File("bracket.jpg")).queue();

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
        return "sende das Endergebnis eines Turniers in einen Channel";
    }
}
