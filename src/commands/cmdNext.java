package commands;

import helperCore.Logic;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class cmdNext implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role admin = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_ADMIN"));
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            OffsetDateTime tn = STATIC.getNextTournament(event.getGuild());
            if(OffsetDateTime.now().isAfter(tn)) {
                if(Logic.nodes.isEmpty()) {
                    event.getTextChannel().sendMessage("Der Termin für's nächste Turnier ist noch nicht bekannt!").queue();
                } else {
                    event.getTextChannel().sendMessage("Das Turnier läuft bereits!!").queue();
                }
                return;
            }
            String out ="Das nächste Turnier findet am "+digitadd(tn.getDayOfMonth(),2)+". "+digitadd(tn.getMonthValue(),2)+". "+tn.getYear()+" gegen "+digitadd(tn.getHour(),2)+":"+digitadd(tn.getMinute(),2)+" Uhr statt!";
            event.getTextChannel().sendMessage(out).queue();
            return;
        }
        if (args.length<1) {
            String out ="";
            OffsetDateTime tn = STATIC.getNextTournament(event.getGuild());
            if(OffsetDateTime.now().isAfter(tn)) {
                if(Logic.nodes.isEmpty()) {
                    event.getTextChannel().sendMessage("Der Termin für's nächste Turnier ist noch nicht eingestellt!").queue();
                } else {
                    event.getTextChannel().sendMessage("Das Turnier läuft bereits!!").queue();
                }
            } else {

                out += "Das nächste Turnier findet am " + digitadd(tn.getDayOfMonth(), 2) + ". " + digitadd(tn.getMonthValue(), 2) + ". " + tn.getYear() + " gegen " + digitadd(tn.getHour(), 2) + ":" + digitadd(tn.getMinute(), 2) + " Uhr statt!";
            }
            out += "\nWenn du das Datum ändern/setzen willst, nutze `"+prefix+"next [Tag] [Monat] [Jahr] [Stunde] [Minute]`";
            event.getTextChannel().sendMessage(out).queue();
            return;
        }
         if(args.length<5) {
             String out = "Wenn du das Datum ändern willst, nutze `"+prefix+"next [Tag] [Monat] [Jahr] [Stunde] [Minute]`";
             event.getTextChannel().sendMessage(out).queue();
             return;
         }
         ArrayList<Integer> in = new ArrayList<>();
         for (String s:args) {
             try {
                 in.add(Integer.parseInt(s));
             } catch (Exception e) {
                 event.getTextChannel().sendMessage("Fehler bei der Verarbeitung der Eingabe: "+e.getMessage()).queue();
                 e.printStackTrace();
                 return;
             }

         }
         int day = in.get(0);
         int month = in.get(1);
         int year = in.get(2);
         int hour = in.get(3);
         int minute = in.get(4);

         if (day>31||day<1) {
             event.getTextChannel().sendMessage("Diesen Tag gibt es nicht!").queue();
             return;
         }
        if (month>12||month<1) {
            event.getTextChannel().sendMessage("Diesen Monat gibt es nicht!").queue();
            return;
        }
        if (hour>23||hour<0) {
            event.getTextChannel().sendMessage("Diese Stunde gibt es nicht!").queue();
            return;
        }
        if (minute>59||minute<0) {
            event.getTextChannel().sendMessage("Diese Minute gibt es nicht!").queue();
            return;
        }
        OffsetDateTime next = OffsetDateTime.now();
        try {
            next = OffsetDateTime.of(year,month,day,hour,minute,0,0,OffsetDateTime.now().getOffset());
        } catch (Exception e) {
            event.getTextChannel().sendMessage("Fehler beim Parsen des Zeitpunkts: "+e.getMessage()).queue();
            e.printStackTrace();
            return;
        }
        if (next.isBefore(OffsetDateTime.now())) {
            event.getTextChannel().sendMessage("Dieser Zeitpunkt liegt in der Vergangenheit!").queue();
            return;
        }
        STATIC.setNextTournament(event.getGuild(),next);
        OffsetDateTime tn = next;
        String out ="Das nächste Turnier findet am "+digitadd(tn.getDayOfMonth(),2)+". "+digitadd(tn.getMonthValue(),2)+". "+tn.getYear()+" gegen "+digitadd(tn.getHour(),2)+":"+digitadd(tn.getMinute(),2)+" Uhr statt!";
        event.getTextChannel().sendMessage(out).queue();
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
        return "Erfahre den Zeitpunkt des nächsten Turnieres!";
    }

    private String digitadd(int numbertoadd, int howmanydigits) {
        StringBuilder out = new StringBuilder("" + numbertoadd);

        while (out.length()<howmanydigits) {
            out.insert(0, "0");
        }
        return out.toString();
    }
}
