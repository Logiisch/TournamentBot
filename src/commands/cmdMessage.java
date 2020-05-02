package commands;

import jdk.nashorn.internal.runtime.ECMAException;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
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
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role helper = event.getGuild().getRoleById(STATIC.ROLE_HELPER);
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(helper)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Das kann nur ein Helfer machen!").queue();
            return;
        }
        ArrayList<Member> member = new ArrayList<>();
         for (Member m:event.getGuild().getMembers()) {
             if (STATIC.getNotIncluded().contains(m.getUser().getId())) continue;
             if (m.getUser().isBot()) continue;
             member.add(m);
         }
         ArrayList<Member> notReachable = new ArrayList<>();
         String Message = "";
         for (String s:args) {
             Message+= " "+s;
         }
         Message = Message.replaceFirst(" ","");
         for (Member m:member) {
             try {
                 m.getUser().openPrivateChannel().complete().sendMessage(Message).queue();
             } catch (Exception e) {
                 notReachable.add(m);
             }
         }
         if (notReachable.isEmpty()) return;
         String membs = "";
         for (Member m: notReachable) {
             membs += m.getAsMention()+"\n";
         }
         Objects.requireNonNull(event.getGuild().getTextChannelById(STATIC.CHANNEL_ALLGEMEIN)).sendMessage(membs+"\n"+Message).queue();
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
    public String Def() {
        return "Sende eine Nachricht an alle Teilnehmer!";
    }
}
