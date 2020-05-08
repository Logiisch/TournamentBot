package commands;

import com.sun.org.apache.bcel.internal.generic.LADD;
import helperCore.LangManager;
import helperCore.Logic;
import helperCore.PermissionLevel;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.Objects;

public class cmdRejoin implements Command {
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
        Role admin = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_ADMIN"));
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdGeneralOnlyAdmin")).queue();
            return;
        }
        if (!Logic.nodes.isEmpty()) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRejoinAlreadyStarted")).queue();
            return;
        }
        if (args.length<1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRejoinUsage").replace("%PREFIX%",prefix)).queue();
            return;
        }
        if (args[0].equalsIgnoreCase("all")) {
            Logic.rejoinAll();
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRejoinAll")).queue();
            return;
        }
        if(event.getMessage().getMentionedUsers().isEmpty()) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRejoinUsage").replace("%PREFIX%",prefix)).queue();
            return;
        }
        for (User u: event.getMessage().getMentionedUsers()) {
            Logic.rejoinUser(u);
        }
        event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRejojnSuccess").replace("%COUNT%",event.getMessage().getMentionedMembers().size()+"")).queue();
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
        return LangManager.get(g,"cmdRejoinDef");
    }
}
