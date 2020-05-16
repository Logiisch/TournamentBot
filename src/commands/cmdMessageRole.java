package commands;

import helperCore.AutoMessageRole;
import helperCore.PermissionLevel;
import listeners.MessageRoleListener;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.List;
import java.util.Objects;

public class cmdMessageRole implements Command {
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
        String prefix = commandListener.getPrefix(event.getGuild());
        Role helper = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_HELPER"));
        if (!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)&&!Objects.requireNonNull(event.getMember()).getRoles().contains(helper))  {
            //event.getTextChannel().sendMessage("Du darfst das nicht!").queue();
            return;}
        if (args.length<4) {
            event.getTextChannel().sendMessage("Usage: `"+prefix+"messagetorole [add/rem/auto/off] [TextChannelID] [MessageID] [RoleID] [ReactEmoji]`").queue();
            return;
        }
        Message msg = Objects.requireNonNull(event.getGuild().getTextChannelById(args[1])).getHistoryAround(args[2],5).complete().getMessageById(args[2]);
        if (msg==null) {
            event.getTextChannel().sendMessage("Nachricht nicht gefunden!").queue();
            return;
        }
        List<MessageReaction> msrs =msg.getReactions();
        MessageReaction mr = null;
        for (MessageReaction mer:msrs) {
            if (mer.getReactionEmote().getEmoji().equalsIgnoreCase(args[4])) mr = mer;
        }
        if (mr==null) {
            event.getTextChannel().sendMessage("Reaction nicht gefunden!").queue();
            return;
        }
        List<User> usrs =mr.retrieveUsers().complete();
        Role r = event.getGuild().getRoleById(args[3]);
        if (r==null) {
            event.getTextChannel().sendMessage("Rolle nicht gefunden!").queue();
            return;
        }
        if (args[0].equalsIgnoreCase("auto")) {
            AutoMessageRole amr = new AutoMessageRole(msg.getId(),msg.getTextChannel().getId(),r.getId(),event.getGuild().getId(),args[4]);
            MessageRoleListener.amrs.add(amr);
            event.getTextChannel().sendMessage("Rollen werden nun automatisch verwaltet. Nutze `"+prefix+"messagetorole off "+amr.getTextChannelID()+" "+msg.getId()+" "+amr.getRoleID()+" "+amr.getEmoji()+"`, um die automatische Rollenvergabe zu beenden!").queue();
            return;
        }
        if (args[0].equalsIgnoreCase("off")) {
            AutoMessageRole rem = null;
            for (AutoMessageRole amr:MessageRoleListener.amrs) {
                if (!amr.getGuildID().equalsIgnoreCase(event.getGuild().getId())) continue;
                if (!amr.getTextChannelID().equalsIgnoreCase(msg.getTextChannel().getId())) continue;
                if (!amr.getMessageID().equalsIgnoreCase(msg.getId())) continue;
                if (!amr.getEmoji().equalsIgnoreCase(args[4])) continue;
                rem =amr;
            }
            if (rem==null) {
                event.getTextChannel().sendMessage("Diese Kombination aus IDs konnte nicht gefunden werden!").queue();
                return;
            }
            MessageRoleListener.amrs.remove(rem);
            event.getTextChannel().sendMessage("Erfolgreich entfernt!").queue();
            return;
        }


        for (User u:usrs) {
            if (event.getGuild().isMember(u)) {
                Member m =event.getGuild().getMember(u);
                assert m !=null;
                if (args[0].equalsIgnoreCase("add")) {
                    event.getGuild().addRoleToMember(m,r).queue();
                    event.getTextChannel().sendMessage("Erfolgreich "+usrs.size()+" Nutzer hinzugefügt!").queue();
                }
                if (args[0].equalsIgnoreCase("rem")) {
                    event.getGuild().removeRoleFromMember(m,r).queue();
                    event.getTextChannel().sendMessage("Erfolgreich "+usrs.size()+" Nutzer entfernt!").queue();
                }
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
        return true;
    }

    @Override
    public String Def(String prefix,Guild g) {
        return "Administrationscommand";
    }
}
