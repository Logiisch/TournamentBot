package commands;

import core.commandHandler;
import listeners.commandListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Objects;

public class cmdHelp implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event)  {
        String prefix = commandListener.getPrefix(event.getGuild());
        EmbedBuilder eb = new EmbedBuilder().setColor(getRandomColor()).setTitle("Turnierhilfe");
        for (String s: commandHandler.commands.keySet()) {
            Command cmd = commandHandler.commands.get(s);
            if (cmd.isPrivate())continue;
            String def = cmd.Def(prefix);
            if (def==null) def = "<Keine Beschreibung verfügbar!>";
            eb.addField(s,def,false);
        }
        eb.setFooter("Angefordert von "+ Objects.requireNonNull(event.getMember()).getEffectiveName());
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
        return "Erhalte Hilfe zu allen Befehlen.";
    }

    private Color getRandomColor() {
        int red = (int)Math.round(255*Math.random());
        int green = (int)Math.round(255*Math.random());
        int blue = (int)Math.round(255*Math.random());
        return new Color(red,green,blue);
    }
}
