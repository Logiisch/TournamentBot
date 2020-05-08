package commands;

import core.commandHandler;
import helperCore.LangManager;
import listeners.commandListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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
        EmbedBuilder eb = new EmbedBuilder().setColor(getRandomColor()).setTitle(LangManager.get(event.getGuild(),"cmdHelpTitle"));
        for (String s: commandHandler.commands.keySet()) {
            Command cmd = commandHandler.commands.get(s);
            if (cmd.isPrivate())continue;
            String def = cmd.Def(prefix,event.getGuild());
            if (def==null) def = LangManager.get(event.getGuild(),"cmdHelpNoDef");
            eb.addField(s,def,false);
        }
        eb.setFooter(LangManager.get(event.getGuild(),"cmdHelpFooter").replace("%NAME%",Objects.requireNonNull(event.getMember()).getEffectiveName()));
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
    public String Def(String prefix, Guild g) {
        return LangManager.get(g,"cmdHelpDef");
    }

    private Color getRandomColor() {
        int red = (int)Math.round(255*Math.random());
        int green = (int)Math.round(255*Math.random());
        int blue = (int)Math.round(255*Math.random());
        return new Color(red,green,blue);
    }
}
