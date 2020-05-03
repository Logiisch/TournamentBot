package commands;

import listeners.commandListener;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

public class cmdLeave implements Command{
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        if(args.length<1) {
            event.getTextChannel().sendMessage("Bitte bestätige mit `"+prefix+"leave confirm`, dass du das Turnier verlassen willst.Das kannst du nicht mehr rückgängig machen!").queue();
            return;
        }
        if (!args[0].equalsIgnoreCase("confirm")) {
            event.getTextChannel().sendMessage("Bitte bestätige mit `"+prefix+"kick confirm`, dass du das Turnier verlassen willst.Das kannst du nicht mehr rückgängig machen!").queue();
            return;
        }
        STATIC.kickUser(event.getAuthor());
        event.getTextChannel().sendMessage("Du hast das Turnier verlassen!").queue();
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
        return "Verlasse das Turnier vorzeitig. Achtung: Unumkehrbar";
    }
}
