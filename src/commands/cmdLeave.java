package commands;

import helperCore.LangManager;
import helperCore.Logic;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class cmdLeave implements Command{
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        if(args.length<1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdLeaveConfirm").replace("%PREFIX%",prefix)).queue();
            return;
        }
        if (!args[0].equalsIgnoreCase("confirm")) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdLeaveConfirm").replace("%PREFIX%",prefix)).queue();
            return;
        }
        Logic.kickUser(event.getAuthor(),event.getGuild());
        event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdLeaveSuccess")).queue();
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
        return LangManager.get(g,"cmdLeaveDef");
    }
}
