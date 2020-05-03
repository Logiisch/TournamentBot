package commands;

import listeners.commandListener;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import threads.statusCycleThread;
import util.STATIC;

import java.util.Arrays;

public class cmdUpdateStatus implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        if (!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Ne, da habe ich jetzt keine Lust drauf!").queue();
            return;
        }
        if (args.length<1) {
            event.getTextChannel().sendMessage("Du musts mir schon genau sagen, was ich machen soll!\n`"+prefix+"status [modus/set/add/speed]`").queue();
            return;
        }
        switch (args[0].toLowerCase()) {
            case "modus":
                modus(event, args,prefix);
                break;
            case "set":
                set(event,args,prefix);
                break;
            case "add":
                add(event,args,prefix);
                break;
            case "speed":
                speed(event,args,prefix);
                break;
                default:
                    event.getTextChannel().sendMessage("Du musts mir schon genau sagen, was ich machen soll!\n`"+prefix+"status [modus/set/add/speed]`").queue();
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
        return true;
    }

    @Override
    public String Def(String prefix) {
        return "Ändere meinen Status!";
    }
    private void modus(MessageReceivedEvent event, String[] args,String prefix){
        if (args.length<2) {
            event.getTextChannel().sendMessage("Du musts mir schon genau sagen, was ich machen soll!\n`"+prefix+"status modus [static/cycle]`").queue();
            return;
        }
        if (args[1].equalsIgnoreCase("static")) {
            statusCycleThread.cycle = false;
            event.getJDA().getPresence().setActivity(Activity.playing(statusCycleThread.cycleList.get(0)));
            statusCycleThread.cycleList.clear();
            event.getTextChannel().sendMessage("Na gut, bin ich eben jetzt ruhiger...").queue();
            return;
        }
        if (args[1].equalsIgnoreCase("cycle")) {
            statusCycleThread.cycle = true;
            statusCycleThread.cycleList.add(STATIC.ACTIVITY);
            Thread t = new Thread(new statusCycleThread(event.getJDA()));
            t.start();
            event.getTextChannel().sendMessage("Yay, Abwechslung!").queue();
            return;
        }
        event.getTextChannel().sendMessage("Du musts mir schon genau sagen, was ich machen soll!\n`"+prefix+"status modus [static/cycle]`").queue();
    }
    private void set(MessageReceivedEvent event, String[] args,String prefix){
        if (args.length<2) {
            event.getTextChannel().sendMessage("Du musts mir schon genau sagen, was ich machen soll!\n`"+prefix+"status set [Status,bei cycle getrennt durch §]`").queue();
            return;
        }
        StringBuilder out = new StringBuilder();
        for (int i=1;i<args.length;i++) {
            String s = args[i];
            out.append(" ").append(s);
        }
        out = new StringBuilder(out.toString().replaceFirst(" ", ""));
        if (statusCycleThread.cycle) {
            String[] split = out.toString().split("§");
            statusCycleThread.cycleList.clear();
            statusCycleThread.cycleList.addAll(Arrays.asList(split));
            statusCycleThread.reset();
            event.getTextChannel().sendMessage("Dann lasse ich jetzt das im Kreis laufen!").queue();
        } else {
            STATIC.ACTIVITY = out.toString();
            event.getJDA().getPresence().setActivity(Activity.playing(out.toString()));
            event.getTextChannel().sendMessage("Na gut, überredet!").queue();
        }
    }
    private void add(MessageReceivedEvent event, String[] args,String prefix){
            if (args.length<2) {
                event.getTextChannel().sendMessage("Du musts mir schon genau sagen, was ich machen soll!\n`"+prefix+"status add [Status]`").queue();
                return;
            }
            StringBuilder out = new StringBuilder();
            for (int i=1;i<args.length;i++) {
                String s = args[i];
                out.append(" ").append(s);
            }
            out = new StringBuilder(out.toString().replaceFirst(" ", ""));
            if (statusCycleThread.cycle) {
                statusCycleThread.cycleList.add(out.toString());
                event.getTextChannel().sendMessage("Das auch noch? Na gut, ob ich mir das alles merken kann..").queue();
            } else {
                statusCycleThread.cycle = true;
                statusCycleThread.cycleList.add(STATIC.ACTIVITY);
                statusCycleThread.cycleList.add(out.toString());
                Thread t = new Thread(new statusCycleThread(event.getJDA()));
                t.start();
                event.getTextChannel().sendMessage("Ich gehe dann mal zum Wechseln über, ja?").queue();
            }
    }
    private void speed(MessageReceivedEvent event, String[] args,String prefix){
        if (args.length<2) {
            event.getTextChannel().sendMessage("Du musts mir schon genau sagen, was ich machen soll!\n`"+prefix+"status apeed [Sekundenzahl]`").queue();
            return;
        }
        int sek;
        try {
            sek = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            event.getTextChannel().sendMessage("Du nuschelst. ich hab die Zahl nicht verstanden!").queue();
            return;
        }
        statusCycleThread.seksShowing = sek;
        if (statusCycleThread.cycle) {
            event.getTextChannel().sendMessage("Uff, da muss ich mich ja umgewöhnen...Na gut...").queue();
        } else {
            event.getTextChannel().sendMessage("Wenn du mir jetzt noch was zum zwischendurch wechseln gibst, wäre ich glücklich. Aber ich merke mir es Mal!").queue();
        }
    }
}
