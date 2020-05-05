package commands;

import helperCore.SimpleString;
import listeners.commandListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import threads.statusCycleThread;
import util.STATIC;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class cmdUpdateStatus implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role admin = event.getGuild().getRoleById(STATIC.ROLE_HELPER);
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Das kann nur ein Helfer machen").queue();
            return;
        }
        /*if (!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage("Ne, da habe ich jetzt keine Lust drauf!").queue();
            return;
        }*/
        if (args.length<1) {
            event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status [modus/set/add/speed/list/type/help]`").queue();
            return;
        }
        switch (args[0].toLowerCase()) {
            case "modus":
            case "mode":
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
            case "list":
                list(event,args,prefix);
                break;
            case "type":
                type(event,args,prefix);
                break;
            case "help":
                help(event,args,prefix);
                break;
                default:
                    event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status [modus/set/add/speed/list/type/help]`").queue();
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
            event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status modus [static/cycle]`").queue();
            return;
        }
        if (args[1].equalsIgnoreCase("static")) {
            statusCycleThread.cycle = false;
            event.getJDA().getPresence().setActivity(statusCycleThread.getActivity(statusCycleThread.replaceString(statusCycleThread.cycleList.get(0))));
            STATIC.ACTIVITY = statusCycleThread.cycleList.get(0);
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
        event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status modus [static/cycle]`").queue();
    }
    private void set(MessageReceivedEvent event, String[] args,String prefix){
        if (args.length<2) {
            event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status set [Status,bei cycle getrennt durch §]`").queue();
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
            event.getJDA().getPresence().setActivity(statusCycleThread.getActivity(statusCycleThread.replaceString(STATIC.ACTIVITY)));
            event.getTextChannel().sendMessage("Na gut, überredet!").queue();
        }
    }
    private void add(MessageReceivedEvent event, String[] args,String prefix){
            if (args.length<2) {
                event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status add [Status]`").queue();
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
    private void speed(MessageReceivedEvent event, @NotNull String[] args, String prefix){
        if (args.length<2) {
            event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status apeed [Sekundenzahl]`").queue();
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
    private void list(MessageReceivedEvent event, @NotNull String[] args, String prefix){
        String out = "Verfügbare Ersetzungen:\n";
        for (String s:statusCycleThread.replacements.keySet()) {
            out +=s+"\n";
        }
        event.getTextChannel().sendMessage(out).queue();
    }
    private void type(MessageReceivedEvent event, @NotNull String[] args, String prefix){
        if (args.length<2) {
            event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status type [playing/streaming/listening/watching]`").queue();
            return;
        }
        switch (args[1].toLowerCase()) {
            case "playing":
                statusCycleThread.at = Activity.ActivityType.DEFAULT;
                break;
            case "streaming":
                statusCycleThread.at = Activity.ActivityType.STREAMING;
                break;
            case "listening":
                statusCycleThread.at = Activity.ActivityType.LISTENING;
                break;
            case "watching":
                statusCycleThread.at = Activity.ActivityType.WATCHING;
                break;
                default:
                    event.getTextChannel().sendMessage("Du musst mir schon genau sagen, was ich machen soll!\n`"+prefix+"status type [playing/streaming/listening/watching]`").queue();
                    return;
        }
        if (!statusCycleThread.cycle) {
            event.getJDA().getPresence().setActivity(statusCycleThread.getActivity(statusCycleThread.replaceString(STATIC.ACTIVITY)));
        }
        event.getTextChannel().sendMessage("Ab und zu muss man ja auch mal was anderes machen..").queue();

    }

    private void help(MessageReceivedEvent event, @NotNull String[] args, String prefix){
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.blue).setTitle("Bot Status").setAuthor("Subcommands");
        eb.addField("modus","Wechsle zwischen einem wechselnden Status(Cycle-Modus) und einem festen Status",false);
        eb.addField("set","Setze den Text, der angezeigt werden soll. Wenn der cycle-Mode an ist, kannst du mit § verschiedene Anzeigen trennen",false);
        eb.addField("add","Füge im Cycle-Modus einen oder mehrere Statusse (getrennt durch §) hinzu",false);
        eb.addField("speed","Wie viele Sekunden soll jede Nachricht im Cycle-Modus angezeigt werden?",false);
        eb.addField("list","Zeige alle Befehlsersetzungen(für den Cycle-Mode)",false);
        eb.addField("type","Wechsle zwischen \"Spielt\",\"Schaut\" und \"Hört ... zu\"",false);
        eb.addField("help","Muss ich das wirklich erklären?",false);
        eb.setDescription("Jeder Helfer hat vollen Zugriff auf die Status-Funktion. Wenn ich merke, dass jemand damit Müll anstellt, ist der Zugriff darauf sofort wieder weg!");
        eb.setFooter("Um Daten zu sparen, bitte ich euch, dass, wenn ihr nur eine Nachricht habt, ihr den Bot im Static-Mode verwendet ("+prefix+"status modus static)");
        event.getTextChannel().sendMessage(eb.build()).queue();
    }
    public static void load() {
        SimpleString seksToTourn = new SimpleString() {
            @Override
            public String getString() {
                OffsetDateTime nT = STATIC.NextTournament;
                OffsetDateTime now = OffsetDateTime.now();
                long secs =now.until(nT, ChronoUnit.SECONDS);
                return secs + "";
            }
        };
        statusCycleThread.replacements.put("%SEKUNDEN%",seksToTourn);
        SimpleString minundsek = new SimpleString() {
            @Override
            public String getString() {
                OffsetDateTime nT = STATIC.NextTournament;
                OffsetDateTime now = OffsetDateTime.now();
                if(nT.isBefore(now)) return "wenige Sekunden";
                long secs =now.until(nT, ChronoUnit.SECONDS);
                long min = (secs-(secs%60))/60;
                secs = secs%60;
                return min +"min "+secs +"sek";
            }
        };
        statusCycleThread.replacements.put("%SECSANDMINS%",minundsek);
        SimpleString hoursundminundsek = new SimpleString() {
            @Override
            public String getString() {
                OffsetDateTime nT = STATIC.NextTournament;
                OffsetDateTime now = OffsetDateTime.now();
                if(nT.isBefore(now)) return "wenige Sekunden";
                long secs =now.until(nT, ChronoUnit.SECONDS);
                long min = (secs-(secs%60))/60;
                secs = secs%60;
                long hour = (min-(min%60))/60;
                min = min%60;
                return hour+"h "+min +"min "+secs +"sek";
            }
        };
        statusCycleThread.replacements.put("%SECSANDMINSANDHOURS%",hoursundminundsek);

        SimpleString hoursundminundseksundtage = new SimpleString() {
            @Override
            public String getString() {
                OffsetDateTime nT = STATIC.NextTournament;
                OffsetDateTime now = OffsetDateTime.now();
                if(!nT.isAfter(now)) return "wenige Sekunden";
                long secs =now.until(nT, ChronoUnit.SECONDS);
                long min = (secs-(secs%60))/60;
                secs = secs%60;
                long hour = (min-(min%60))/60;
                min = min%60;
                long days = (hour-(hour%24))/24;
                hour=hour%24;
                String out ="";
                if (days!=0) out += days+"d ";
                if (hour!=0) out += hour+"h ";
                if (min!=0) out += min+"min ";
                if (secs!=0) out += secs+"s ";


                return out;
            }
        };
        statusCycleThread.replacements.put("%TIMETOTOURNAMENT%",hoursundminundseksundtage);
    }

}
