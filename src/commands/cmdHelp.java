package commands;

import core.commandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class cmdHelp implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event)  {
        EmbedBuilder eb = new EmbedBuilder().setColor(getRandomColor()).setTitle("Xander's Hilfe");
        for (String s: commandHandler.commands.keySet()) {
            Command cmd = commandHandler.commands.get(s);
            if (cmd.isPrivate())continue;
            String def = cmd.Def();
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
    public String Def() {
        return "Erhalte Hilfe zu allen Befehlen.";
    }

    private Color getRandomColor() {
        int red = (int)Math.round(255*Math.random());
        int green = (int)Math.round(255*Math.random());
        int blue = (int)Math.round(255*Math.random());
        return new Color(red,green,blue);
    }
}
