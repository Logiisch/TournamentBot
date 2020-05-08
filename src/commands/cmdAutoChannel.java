package commands;

import helperCore.PermissionLevel;
import listeners.autoChannelListener;
import listeners.commandListener;
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
            event.getTextChannel().sendMessage("Das kann nur ein Helfer machen!").queue();
            return;
        }
        if (args.length<1) {
            if (autoChannelListener.VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) {
                event.getTextChannel().sendMessage("AutoChannel ist aktuell aus, es ist noch kein Channel festgelegt. lege einen mit `"+prefix+"autochannel  set [ChannelID]` fest!").queue();
            } else {
                String out = "AutoChannel ist aktuell "+(autoChannelListener.isWorking?"an":"aus")+"!\n";
                VoiceChannel vc = event.getGuild().getVoiceChannelById(autoChannelListener.VC_BASE_AUTOCHANNEL);
                if (vc==null) {
                    autoChannelListener.VC_BASE_AUTOCHANNEL = "";
                    autoChannelListener.isWorking = false;
                    event.getTextChannel().sendMessage("beim laden des AutoChannels ist ein Fehler aufgetreten. Bitte lege einen neuen Channel mit `"+prefix+"autochannel set [ChannelID]` fest!").queue();
                    return;
                }
                out += "Der aktuelle AutoChannel ist `"+vc.getName()+" (ID="+vc.getId()+")`";
                event.getTextChannel().sendMessage(out).queue();
            }
            event.getTextChannel().sendMessage("Usage: `"+prefix+"autochannel [enable/on/disable/off/set]`").queue();
            return;
        }
        switch (args[0]) {
            case "enable":
            case "on":
            case "an":
                if (autoChannelListener.VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) {
                    event.getTextChannel().sendMessage("Bitte setze zuerst einen Channel mit`"+prefix+"autochannel set [ChannelID]`!").queue();
                    return;
                }
                autoChannelListener.isWorking=true;
                event.getTextChannel().sendMessage("Der AutoChannel ist nun an!").queue();
                return;
            case "disable":
            case "off":
            case "aus":
                if (autoChannelListener.VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) {
                    event.getTextChannel().sendMessage("Bitte setze zuerst einen Channel mit`"+prefix+"autochannel set [ChannelID]`!").queue();
                    return;
                }
                autoChannelListener.isWorking=false;
                event.getTextChannel().sendMessage("Der AutoChannel ist nun aus!").queue();
                return;
            case "set":
                if (args.length<2) {
                    event.getTextChannel().sendMessage("Usage: `"+prefix+"autochannel set [ChannelID]`").queue();
                    return;
                }
                VoiceChannel vc = event.getGuild().getVoiceChannelById(args[1]);
                if (vc==null) {
                    event.getTextChannel().sendMessage("Hast du dich bei der ID vertippt? Bitte probiere es erneut!").queue();
                    return;
                }
                autoChannelListener.VC_BASE_AUTOCHANNEL = args[1];
                event.getTextChannel().sendMessage("AutoChannel auf `"+vc.getName()+"(ID="+vc.getId()+")` gesetzt!").queue();
                return;

        }
        event.getTextChannel().sendMessage("Usage: `"+prefix+"autochannel [enable/on/disable/off/set]`").queue();

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
        return "Erstelle oder lösche einen AutoChannel";
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.HELPER;
    }
}
