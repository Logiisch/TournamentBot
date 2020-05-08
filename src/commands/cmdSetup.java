package commands;

import helperCore.PermissionLevel;
import listeners.commandListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.awt.*;

public class cmdSetup implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.GUILDOWNER;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
       if (args.length<2) {
           event.getTextChannel().sendMessage(getEmbed(event.getGuild(),true, commandListener.getPrefix(event.getGuild()))).queue();
           return;
       }
       String key = args[0];
       String value = args[1];

       if (!STATIC.getAllKeys(event.getGuild()).contains(key.toUpperCase())) {
           event.getTextChannel().sendMessage("Thats not a valid key!").queue();
           event.getTextChannel().sendMessage(getEmbed(event.getGuild())).queue();
           return;
       }
       if (!onlyNumber(value)) {
           event.getTextChannel().sendMessage("Please use the ID of the Role or Channel!").queue();
           return;
       }
       STATIC.setSettings(event.getGuild(),key,value);
       event.getTextChannel().sendMessage("Sucessfully set!").queue();
       event.getTextChannel().sendMessage(getEmbed(event.getGuild())).queue();
       if(STATIC.canStartTournament(event.getGuild())) event.getTextChannel().sendMessage("Setup complete! You can now use all functions of the bot!").queue();


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
        return null;
    }

    private static MessageEmbed getEmbed(Guild g,boolean footer,String prefix) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription("All these values have to been set to work with the bot.");
        eb.setTitle("Server Settings").setAuthor(g.getName());
        if (STATIC.canStartTournament(g)) {
            eb.setColor(Color.green);
        } else {
            eb.setColor(Color.gray);
        }
        for (String key: STATIC.getAllKeys(g)) {
            String value = STATIC.getSettings(g,key);
            eb.addField(key,value,false);
        }
        if(footer) eb.setFooter("Use \""+prefix+"setup [key] [new Value]\".Please use the corresponding ID's!");
        return eb.build();
    }
    private static MessageEmbed getEmbed(Guild g) {
        return getEmbed(g,false,commandListener.getPrefix(g));
    }
    private static boolean onlyNumber(String in) {
        for (char c: in.toCharArray()) {
            if (!isNumber(c)) return false;
        }
        return true;
    }
    private static boolean isNumber(char c) {
        if (c=='0') return true;
        if (c=='1') return true;
        if (c=='2') return true;
        if (c=='3') return true;
        if (c=='4') return true;
        if (c=='5') return true;
        if (c=='6') return true;
        if (c=='7') return true;
        if (c=='8') return true;
        if (c=='9') return true;
        return false;

    }
}
