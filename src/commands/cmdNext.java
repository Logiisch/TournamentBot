package commands;

import helperCore.LangManager;
import helperCore.Logic;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
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
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextNotSet")).queue();
                } else {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextAlreadyRunning")).queue();
                }
                return;
            }
            String day = digitadd(tn.getDayOfMonth(),2);
            String month = digitadd(tn.getMonthValue(),2);
            String year = digitadd(tn.getYear(),4);
            String hour = digitadd(tn.getHour(),2);
            String minute = digitadd(tn.getMinute(),2);
            String out =LangManager.get(event.getGuild(),"cmdNextGet").replace("%DAY%",day).replace("%MONTH%",month).replace("%YEAR%",year).replace("%HOUR%",hour).replace("%MINUTE%",minute);
            event.getTextChannel().sendMessage(out).queue();
            return;
        }
        if (args.length<1) {
            String out ="";
            OffsetDateTime tn = STATIC.getNextTournament(event.getGuild());
            if(OffsetDateTime.now().isAfter(tn)) {
                if(Logic.nodes.isEmpty()) {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextNotSet")).queue();
                } else {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextAlreadyRunning")).queue();
                }
            } else {
                String day = digitadd(tn.getDayOfMonth(),2);
                String month = digitadd(tn.getMonthValue(),2);
                String year = digitadd(tn.getYear(),4);
                String hour = digitadd(tn.getHour(),2);
                String minute = digitadd(tn.getMinute(),2);
                out += LangManager.get(event.getGuild(),"cmdNextGet").replace("%DAY%",day).replace("%MONTH%",month).replace("%YEAR%",year).replace("%HOUR%",hour).replace("%MINUTE%",minute);
            }
            out += "\n"+LangManager.get(event.getGuild(),"cmdNextUsage").replace("%PREFIX%",prefix);
            event.getTextChannel().sendMessage(out).queue();
            return;
        }
         if(args.length<5) {
             String out = LangManager.get(event.getGuild(),"cmdNextUsage").replace("%PREFIX%",prefix);
             event.getTextChannel().sendMessage(out).queue();
             return;
         }
         ArrayList<Integer> in = new ArrayList<>();
         for (String s:args) {
             try {
                 in.add(Integer.parseInt(s));
             } catch (Exception e) {
                 event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextError").replace("%MSG%",e.getMessage())).queue();
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
             event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextNEDay")).queue();
             return;
         }
        if (month>12||month<1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextNEMonth")).queue();
            return;
        }
        if (hour>23||hour<0) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextNEHour")).queue();
            return;
        }
        if (minute>59||minute<0) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextNEMinute")).queue();
            return;
        }
        OffsetDateTime next = OffsetDateTime.now();
        try {
            next = OffsetDateTime.of(year,month,day,hour,minute,0,0,OffsetDateTime.now().getOffset());
        } catch (Exception e) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextParseError").replace("%MSG%",e.getMessage())).queue();
            e.printStackTrace();
            return;
        }
        if (next.isBefore(OffsetDateTime.now())) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextPastError")).queue();
            return;
        }
        STATIC.setNextTournament(event.getGuild(),next);
        OffsetDateTime tn = next;
        String out =LangManager.get(event.getGuild(),"cmdNextGet").replace("%DAY%",day+"").replace("%MONTH%",month+"").replace("%YEAR%",year+"").replace("%HOUR%",hour+"").replace("%MINUTE%",minute+"");
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
    public String Def(String prefix, Guild g) {
        return LangManager.get(g,"cmdNextDef");
    }

    private String digitadd(int numbertoadd, int howmanydigits) {
        StringBuilder out = new StringBuilder("" + numbertoadd);

        while (out.length()<howmanydigits) {
            out.insert(0, "0");
        }
        return out.toString();
    }
}
