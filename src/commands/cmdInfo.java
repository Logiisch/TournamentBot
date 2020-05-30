package commands;

import helperCore.LangManager;
import helperCore.Logic;
import helperCore.TournamentNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.awt.*;
import java.util.ArrayList;

public class cmdInfo implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        User u;
        TournamentNode tn;
        if (event.getMessage().getMentionedMembers().size()==0) {
             u = event.getAuthor();
        } else {
            u= event.getMessage().getMentionedMembers().get(0).getUser();
        }
        tn = Logic.getCurrentNode(u,event.getGuild());
        if (tn==null) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdGeneralNoOpenMatch")).queue();
            return;
        }
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.green);
        eb.setTitle(u.getName()).setAuthor(Logic.getRoundname(tn.getRunde(),event.getGuild()));
        if (STATIC.GuildsWithTeamMode.contains(event.getGuild().getId())) {
            String mate = "";
            ArrayList<ArrayList<String>> teams =cmdTeamMixup.guildTeams.get(event.getGuild().getId());
            for (ArrayList<String> temp:teams) {
                if (!temp.contains(event.getMember().getId())) continue;
                if (temp.get(0).equalsIgnoreCase(event.getMember().getId())) mate = event.getGuild().getMemberById(temp.get(1)).getEffectiveName();
                if (temp.get(1).equalsIgnoreCase(event.getMember().getId())) mate = event.getGuild().getMemberById(temp.get(0)).getEffectiveName();
            }
            eb.addField(LangManager.get(event.getGuild(),"cmdInfoPartner")+":",mate,false);
        }
        String geg;
        if (tn.players.size()==2) {
            User gegner;
            if (tn.players.get(0).getId().equalsIgnoreCase(u.getId())) {
                gegner = tn.players.get(1);
            } else {
                gegner = tn.players.get(0);
            }
            geg = gegner.getName();
            if (STATIC.GuildsWithTeamMode.contains(event.getGuild().getId())) {
                String mate = "";
                ArrayList<ArrayList<String>> teams =cmdTeamMixup.guildTeams.get(event.getGuild().getId());
                String enID = event.getGuild().getMember(gegner).getId();
                for (ArrayList<String> temp:teams) {
                    if (!temp.contains(enID)) continue;
                    if (temp.get(0).equalsIgnoreCase(enID)) mate = event.getGuild().getMemberById(temp.get(1)).getEffectiveName();
                    if (temp.get(1).equalsIgnoreCase(enID)) mate = event.getGuild().getMemberById(temp.get(0)).getEffectiveName();
                }
                geg = geg + " & "+ mate;
            }
        } else {
            geg = LangManager.get(event.getGuild(),"cmdInfoNotSetYet");
        }
        eb.addField(LangManager.get(event.getGuild(),"cmdInfoCurrent"),geg,false);
        event.getTextChannel().sendMessage(eb.build()).queue();
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
        return LangManager.get(g,"cmdInfoDef");
    }
}
