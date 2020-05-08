package commands;

import helperCore.LangManager;
import helperCore.PermissionLevel;
import listeners.autoChannelListener;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.Objects;

public class cmdAutoChannel implements Command{
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role helper = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_HELPER"));
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(helper)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdGeneralOnlyHelper")).queue();
            return;
        }
        if (args.length<1) {
            if (autoChannelListener.VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) {
                event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelOffNotSet").replace("%PREFIX%",prefix)).queue();
            } else {
                String out = LangManager.get(event.getGuild(),"cmdAutoChannelCurrently"+(autoChannelListener.isWorking?"On":"Off"));
                VoiceChannel vc = event.getGuild().getVoiceChannelById(autoChannelListener.VC_BASE_AUTOCHANNEL);
                if (vc==null) {
                    autoChannelListener.VC_BASE_AUTOCHANNEL = "";
                    autoChannelListener.isWorking = false;
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelLoadingError").replace("%PREFIX%",prefix)).queue();
                    return;
                }
                out += LangManager.get(event.getGuild(),"cmdAutoChannelCurrentChannel").replace("%NAME%",vc.getName()).replace("%ID%",vc.getId());
                event.getTextChannel().sendMessage(out).queue();
            }
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelUsage").replace("%PREFIX%",prefix)).queue();
            return;
        }
        switch (args[0]) {
            case "enable":
            case "on":
            case "an":
                if (autoChannelListener.VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelSetFirst").replace("%PREFIX%",prefix)).queue();
                    return;
                }
                autoChannelListener.isWorking=true;
                event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelNowOn")).queue();
                return;
            case "disable":
            case "off":
            case "aus":
                if (autoChannelListener.VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelSetFirst").replace("%PREFIX%",prefix)).queue();
                    return;
                }
                autoChannelListener.isWorking=false;
                event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelNowOff")).queue();
                return;
            case "set":
                if (args.length<2) {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelUsageSet").replace("%PREFIX%",prefix)).queue();
                    return;
                }
                VoiceChannel vc = event.getGuild().getVoiceChannelById(args[1]);
                if (vc==null) {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelInvalidID")).queue();
                    return;
                }
                autoChannelListener.VC_BASE_AUTOCHANNEL = args[1];
                event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelChannelNow".replace("%NAME%",vc.getName()).replace("%ID%",vc.getId()))).queue();
                return;

        }
        event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdAutoChannelUsage").replace("%PREFIX%",prefix)).queue();

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
        return LangManager.get(g,"cmdAutoChannelDef");
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.HELPER;
    }
}
