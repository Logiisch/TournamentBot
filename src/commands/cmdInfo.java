package commands;

import helperCore.Logic;
import helperCore.TournamentNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.awt.*;

public class cmdInfo implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        User u =null;
        TournamentNode tn = null;
        if (event.getMessage().getMentionedMembers().size()==0) {
             u = event.getAuthor();
        } else {
            u= event.getMessage().getMentionedMembers().get(0).getUser();
        }
        tn = Logic.getCurrentNode(u);
        if (tn==null) {
            event.getTextChannel().sendMessage("Kein offenes Match gefunden!").queue();
            return;
        }
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.green);
        eb.setTitle(u.getName()).setAuthor(STATIC.getRoundname(tn.getRunde()));
        String geg = "";
        if (tn.players.size()==2) {
            User gegner = null;
            if (tn.players.get(0).getId().equalsIgnoreCase(u.getId())) {
                gegner = tn.players.get(1);
            } else {
                gegner = tn.players.get(0);
            }
            geg = gegner.getName();
        } else {
            geg = "Steht noch nicht fest!";
        }
        eb.addField("Aktueller Gegner",geg,false);
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
    public String Def(String prefix) {
        return "Zeige deine Aktuelle Position und deinen aktuellen Gegner an";
    }
}
