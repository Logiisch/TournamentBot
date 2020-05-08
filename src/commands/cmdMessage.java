package commands;

import helperCore.Logic;
import helperCore.PermissionLevel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.ArrayList;
import java.util.Objects;

public class cmdMessage implements Command {
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
        ArrayList<Member> member = new ArrayList<>();
         for (Member m:event.getGuild().getMembers()) {
             if (Logic.getNotIncluded().contains(m.getUser().getId())) continue;
             if (m.getUser().isBot()) continue;
             member.add(m);
         }
         ArrayList<Member> notReachable = new ArrayList<>();
         StringBuilder Message = new StringBuilder();
         for (String s:args) {
             Message.append(" ").append(s);
         }
         Message = new StringBuilder(Message.toString().replaceFirst(" ", ""));
         for (Member m:member) {
             try {
                 m.getUser().openPrivateChannel().complete().sendMessage(Message.toString()).complete();
             } catch (Exception e) {
                 notReachable.add(m);
             }
         }
         if (notReachable.isEmpty()) return;
         StringBuilder membs = new StringBuilder();
         for (Member m: notReachable) {
             membs.append(m.getAsMention()).append("\n");
         }
         Objects.requireNonNull(event.getGuild().getTextChannelById(STATIC.getSettings(event.getGuild(),"CHANNEL_ALLGEMEIN"))).sendMessage(membs+"\n"+Message).queue();
         event.getTextChannel().sendMessage("Die Message wurde an alle Teilnehmer zugestellt!").queue();

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
        return "Sende eine Nachricht an alle Teilnehmer!";
    }
}
