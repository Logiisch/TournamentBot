package commands;

import com.iwebpp.crypto.TweetNaclFast;
import helperCore.LangManager;
import helperCore.PermissionLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class cmdTeamMixup implements Command {
    public static HashMap<String,ArrayList<ArrayList<String>>> guildTeams = new HashMap<>();
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (!STATIC.GuildsWithTeamMode.contains(event.getGuild().getId())) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdMixupNoTeammode")).queue();
            STATIC.GuildsWithTeamMode.add(event.getGuild().getId());
        }
        Role tlnhmr = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_TURNIERTEILNEHMER"));
        ArrayList<Member> tn = new ArrayList<>();
        for (Member m: event.getGuild().getMembers()) {
            if (m.getRoles().contains(tlnhmr)) tn.add(m);
        }
        Collections.shuffle(tn);
        if (tn.size()%2==1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdMixupOddPlayers")).queue();
            return;
        }
        if (tn.size()==0) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdMixupNoPlayers")).queue();
            return;
        }
        ArrayList<ArrayList<String>> teams = new ArrayList<>();
        while (tn.size()>0) {
            ArrayList<String> part = new ArrayList<>();
            Member a = tn.get(0);
            Member b = tn.get(1);
            part.add(a.getUser().getId());
            tn.remove(a);
            part.add(b.getUser().getId());
            tn.remove(b);
            teams.add(part);
        }
        guildTeams.put(event.getGuild().getId(),teams);
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.BLUE).setTitle(LangManager.get(event.getGuild(),"cmdMixupEbTeams"));
        eb.setDescription(LangManager.get(event.getGuild(),"cmdMixupEbTeamList")+"\n");
        for (ArrayList<String> part:teams) {
            eb.appendDescription("\n"+event.getGuild().getMemberById(part.get(0)).getEffectiveName()+" "+LangManager.get(event.getGuild(),"cmdMixupEbAnd")+" "+event.getGuild().getMemberById(part.get(1)).getEffectiveName());
        }
        eb.setFooter(LangManager.get(event.getGuild(),"cmdMixupEbFooter"));
        event.getTextChannel().sendMessage(eb.build()).queue();
        Role ld = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_TEAMLEADER"));
        for (ArrayList<String> part:teams) {
            Member leader = event.getGuild().getMemberById(part.get(0));
            Member partner = event.getGuild().getMemberById(part.get(1));
            if (!event.getMessage().getContentDisplay().contains("silent")) {
                leader.getUser().openPrivateChannel().complete().sendMessage(LangManager.get(event.getGuild(), "cmdMixupPartner").replace("%NAME%", partner.getEffectiveName()) + "\n" + LangManager.get(event.getGuild(), "cmdMixupTeamLeaderYou")).queue();
                partner.getUser().openPrivateChannel().complete().sendMessage(LangManager.get(event.getGuild(), "cmdMixupPartner").replace("%NAME%", leader.getEffectiveName()) + "\n" + LangManager.get(event.getGuild(), "cmdMixupTeamLeaderPartner")).queue();
                assert ld != null;
                event.getGuild().addRoleToMember(leader,ld).queue();
            }

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
        return false;
    }

    @Override
    public String Def(String prefix, Guild g) {
        return null;
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.ADMIN;
    }
}
