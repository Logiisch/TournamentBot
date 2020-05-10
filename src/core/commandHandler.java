package core;

import commands.Command;
import helperCore.LangManager;
import helperCore.PermissionLevel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

//import commands.cmdServerSettings;

public class commandHandler {

    public static final commandParser parser = new commandParser();
    public static HashMap<String, Command> commands = new HashMap<>();

    public static void handleCommand(commandParser.commandContainer cmd) throws IOException {

        if (commands.containsKey(cmd.invoke)) {
            if (commands.get(cmd.invoke).blockedServerIDs() != null) {
                if (commands.get(cmd.invoke).blockedServerIDs().contains(cmd.event.getGuild().getId())) {
                    cmd.event.getTextChannel().sendMessage(LangManager.get(cmd.event.getGuild(),"cmdHandlerDeactivated")).queue();
                    return;
                }
                if (!STATIC.canStartTournament(cmd.event.getGuild())) {
                    if (!cmd.invoke.equalsIgnoreCase("setup")) {
                        cmd.event.getTextChannel().sendMessage("Please set up the bot using `t!setup` first!").queue();
                        return;
                    }
                }

                PermissionLevel perm = commands.get(cmd.invoke).PermLevel();
                if (!hasPermission(perm,cmd.event)) {
                    cmd.event.getTextChannel().sendMessage(LangManager.get(cmd.event.getGuild(),"cmdHandlerPermission").replace("%MSG%",perm.name())).queue();
                    return;
                }

            }
            /*if (!cmdServerSettings.allowedToUseCmd(cmd.invoke,cmd.event.getGuild(),cmd.event.getAuthor())&&!cmd.event.getAuthor().getId().equalsIgnoreCase("318457868917407745")) {
                cmd.event.getTextChannel().sendMessage("Du hast keine Berechtigungen, diesen Befehl zu nutzen!").queue();
                return;
            }*/
            boolean safe = commands.get(cmd.invoke).called(cmd.args, cmd.event);
            if (!safe) {

                commands.get(cmd.invoke).action(cmd.args, cmd.event);
                commands.get(cmd.invoke).executed(safe, cmd.event);
            } else {

                commands.get(cmd.invoke).executed(safe, cmd.event);
            }

        }

    }
    private static boolean hasPermission(PermissionLevel pm, MessageReceivedEvent event)  {
        if (pm.equals(PermissionLevel.GUILDOWNER)&&event.getAuthor().getId().equalsIgnoreCase(Objects.requireNonNull(event.getGuild().getOwner()).getUser().getId())) return true;
        if (event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) return true;
        if (pm.equals(PermissionLevel.BOTOWNER)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) return false;
        Role helper = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_HELPER"));
        if (pm.equals(PermissionLevel.HELPER)&&!Objects.requireNonNull(event.getMember()).getRoles().contains(helper)) return false;
        Role admin = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_ADMIN"));
        if (pm.equals(PermissionLevel.ADMIN)&&!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)) return false;
        return false;
    }

}