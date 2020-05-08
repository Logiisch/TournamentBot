package commands;

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

public class cmdRevert implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.HELPER;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role helper = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_HELPER"));
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(helper)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdGeneralOnlyHelper")).queue();
            return;
        }
        if (args.length<1||event.getMessage().getMentionedUsers().size()==0) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRevertUsage").replace("%PREFIX%",prefix)).queue();
            return;
        }
        if (event.getMessage().getMentionedUsers().size()>1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRevertSingleOnly")).queue();
            return;
        }
        User u = event.getMessage().getMentionedUsers().get(0);
        if (!event.getMessage().getContentDisplay().toLowerCase().contains("confirm")) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRevertConfim").replace("%NAME%",u.getName()).replace("%PREFIX%",prefix)).queue();
            return;
        }
        try {
            Logic.revert(u,event.getGuild());
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRevertSuccess")).queue();
        } catch (Exception e) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdRevertError").replace("%MSG%",e.getMessage())).queue();
            e.printStackTrace();
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
    public String Def(String prefix, Guild g) {
        return LangManager.get(g,"cmdRevertDef");
    }
}
