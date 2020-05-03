package commands;

import helperCore.Logic;
import listeners.commandListener;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class cmdResult implements Command {
    private static String[] winIndicators = {"w","win","g","gewonnen"};
    private static String[] looseIndicators = {"l","lose","v","verloren"};
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event)  {
        String prefix = commandListener.getPrefix(event.getGuild());
        if (Logic.nodes.size()==0) {
            event.getTextChannel().sendMessage("Immer ruhig. Das Turnier hat noch nicht einmal begonnen!").queue();
            return;
        }
        if (args.length<1) {
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Bitte gib an, ob du gewonnen (`"+ prefix+"res [w/win/g/gewonnen]`) oder verloren (`"+prefix+"res [l/lose/v/verloren]`) hast!").queue();
            return;
        }
        boolean found = false;
         for (String s:winIndicators) {
             if (args[0].equalsIgnoreCase(s)) {
                 found=true;
                 Logic.trylog(event,true);
             }
         }
        for (String s:looseIndicators) {
            if (args[0].equalsIgnoreCase(s)) {
                found=true;
                Logic.trylog(event,false);
            }
        }
        if (!found) {
            event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Bitte gib an, ob du gewonnen (`"+ prefix+"res [w/win/g/gewonnen]`) oder verloren (`"+prefix+"res [l/lose/v/verloren]`) hast!").queue();
        }

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
        return "Trage das Ergebnis eines Matches ein. Nutze \""+prefix+"res win\", wenn du gewonnen hast oder \""+prefix+"res lose\", wenn du verloren hast!";
    }
}
