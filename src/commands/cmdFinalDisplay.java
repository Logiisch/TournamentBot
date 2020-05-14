package commands;

import helperCore.LangManager;
import helperCore.Logic;
import helperCore.PermissionLevel;
import helperCore.TournamentNode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdGeneralOnlyAdmin")).queue();
            return;
        }
        if (Logic.getNodes(event.getGuild()).isEmpty()) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdGeneralDidntStartYet")).queue();
            return;
        }
        if (Logic.getNodes(event.getGuild()).get(1).winner==null) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdFinalNoWinnerYet")).queue();
            return;
        }
         if (event.getMessage().getMentionedChannels().isEmpty()) {
             event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdFinalNoChannel")).queue();
             return;
         }
        HashMap<Integer, TournamentNode> nodes = Logic.getNodes(event.getGuild());
        User winner = nodes.get(1).winner;
        String wstring = "";
        if (event.getGuild().isMember(winner)) wstring = Objects.requireNonNull(event.getGuild().getMember(winner)).getAsMention(); else wstring = winner.getName();
        ArrayList<String> users = new ArrayList<>();
        for (Integer nid : nodes.keySet()) {
            TournamentNode tn = nodes.get(nid);
            for (User ta:tn.players) {
                if (!users.contains(ta.getId())) users.add(ta.getId());
            }
        }
        users.remove(event.getJDA().getSelfUser().getId());
        try {
            cmdBracket.drawImage(winner,event.getGuild());
        } catch (Exception e) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdFinalFailure").replace("%MSG%",e.getMessage())).queue();
            e.printStackTrace();
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        String out = LangManager.get(event.getGuild(),"cmdFinalOut").replace("%DAY%",now.getDayOfMonth()+"").replace("%MONTH%",now.getMonthValue()+"").replace("%YEAR%",now.getYear()+"").replace("%WINNER%",wstring).replace("%COUNT%",users.size()+"");
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
    public String Def(String prefix, Guild g) {
        return LangManager.get(g,"cmdFinalDef");
    }
}
