package commands;


import helperCore.LangManager;
import helperCore.PermissionLevel;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.util.Set;

public class cmdLang implements Command{
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        if (args.length<1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdLangUsage").replace("%PREFIX%",prefix)).queue();
            return;
        }
        String lang = args[0].toUpperCase();
        Set<String> av = LangManager.getPossible();
        if (!av.contains(lang)) {
            StringBuilder langs = new StringBuilder();
            for (String s:av) {
                langs.append(",").append(s);
            }
            langs = new StringBuilder(langs.toString().replaceFirst(",", ""));
            String out = LangManager.get(event.getGuild(),"cmdLangLangDidntExist")+"\n"+LangManager.get(event.getGuild(),"cmdLangPossibleLangs").replace("%MSG%", langs.toString());
            event.getTextChannel().sendMessage(out).queue();
            return;
        }
        STATIC.setSettings(event.getGuild(),"LANGUAGE",lang);
        event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdLangSuccess")).queue();
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
    public PermissionLevel PermLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public String Def(String prefix, Guild g) {
        return LangManager.get(g,"cmdLangDef");
    }
}
