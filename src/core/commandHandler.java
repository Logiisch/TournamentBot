package core;

import commands.Command;

import java.io.IOException;
import java.util.HashMap;

//import commands.cmdServerSettings;

public class commandHandler {

    public static final commandParser parser = new commandParser();
    public static HashMap<String, Command> commands = new HashMap<>();

    public static void handleCommand(commandParser.commandContainer cmd) throws IOException {

        if (commands.containsKey(cmd.invoke)) {
            if (commands.get(cmd.invoke).blockedServerIDs() != null) {
                if (commands.get(cmd.invoke).blockedServerIDs().contains(cmd.event.getGuild().getId())) {
                    cmd.event.getTextChannel().sendMessage("Dieser Befehl ist auf dem Server deaktiviert!").queue();
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

}