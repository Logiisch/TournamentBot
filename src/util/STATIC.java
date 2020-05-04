package util;

/*import Threads.HiskiGiveawayThread;
import Threads.TimerThread;
import commands.cmdGetPoints;
import listeners.activityListener;*/

import helperCore.Logic;
import helperCore.TournamentNode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Objects;

public class STATIC {
    public static String ACTIVITY = "keine große Rolle";
    public static String PREFIX = "t!";
    public static String ADDLINK = "https://discordapp.com/oauth2/authorize?client_id=705567211380801598&scope=bot&permissions=268692560";
    public static String CODELINK = "https://github.com/Logiisch/TournamentBot";
    public static String OWNERID = "318457868917407745";


    public static String GUILDID = "676002000835444736";




    public static String ROLE_TOT = "678319461610029056";
    public static String ROLE_FINALE = "705578400089112576";
    public static String ROLE_HALBFINALE = "705578401179631847";
    public static String ROLE_VIERTELFINALE = "705578402165555292";
    public static String ROLE_ACHTELFINALE = "705578402517745749";
    public static String ROLE_VORRUNDE2 = "705578521652625440";
    public static String ROLE_VORRUNDE1 = "705578521896026224";

    public static String ROLE_WINNER = "706501983368118272";

    public static String ROLE_ADMIN = "676004297560948738";
    public static String ROLE_HELPER = "677254731835637800";

    public static String ROLE_TESTLAUFTEILNEHMER = "706471180369920000";

    public static String CHANNEL_RESULTS = "705557075106988144";
    public static String CHANNEL_ALLGEMEIN = "676029060136042536";

    public static OffsetDateTime NextTournament = OffsetDateTime.of(2020, 5,5,19,30,0,0, OffsetDateTime.now().getOffset());

    public static boolean dryRun = true;


    public static void changePrefix (String prefix){
        PREFIX = prefix;
    }
    //Für User IDs
    private static ArrayList<String> notincluded = new ArrayList<>();

    public static ArrayList<String> getNotIncluded() {
        return notincluded;
    }
    public static void kickUser(User u) {
        notincluded.add(u.getId());
        if (!Logic.nodes.isEmpty()) {
            for (int nid:Logic.nodes.keySet()) {
                TournamentNode tn = Logic.nodes.get(nid);

                if (tn.players.contains(u)) {
                    tn.players.remove(u);
                    User other = null;
                    if (!tn.players.isEmpty()) {
                        other = tn.players.get(0);
                    }
                    tn.players.add(u.getJDA().getSelfUser());
                    if (tn.players.size()==2&&tn.winner==null) try {
                        Logic.logresult(u.getJDA().getSelfUser(),false,u.getJDA().getGuildById(GUILDID));
                    } catch (Exception e) {
                        if (other!=null) other.openPrivateChannel().complete().sendMessage("Du bist eine Runde weiter, anscheinend ist aber ein Fehler aufgetreten, bitte melde dich bei Logii!\n"+e.getMessage()).queue();
                        e.printStackTrace();
                    }
                }
            }
        }
        saveNotIncluded();
    }
    //true wenn User wirklich gekickt war, false wenn er eh schon dabei war
    public static void rejoinUser(User u) {
        if (notincluded.contains(u.getId())) {
            notincluded.remove(u.getId());
            saveNotIncluded();
        }
    }
    public static void rejoinAll() {
        notincluded.clear();
        saveNotIncluded();
    }

    public static Message trysend (User u, String msg) {
        String SELFID = "705567211380801598";
        if (u.getId().equalsIgnoreCase(SELFID)||u.getJDA().getSelfUser().getId().equalsIgnoreCase(u.getId())) return null;
        try {
            return u.openPrivateChannel().complete().sendMessage(msg).complete();
        } catch (Exception e) {
            Guild g = Objects.requireNonNull(u.getJDA().getTextChannelById(CHANNEL_ALLGEMEIN)).getGuild();
            if (g.isMember(u)) {
                return Objects.requireNonNull(u.getJDA().getTextChannelById(CHANNEL_ALLGEMEIN)).sendMessage(Objects.requireNonNull(g.getMember(u)).getAsMention()+":"+msg+"\nFür das Turnier öffne bite deine Privatnachrichten, da nicht alle Nachrichten über diesen Channel gesendet werden können!").complete();} else {return null;}
        }
    }
    public static String getRoundname(int runde) {
        switch (runde) {
            case 1:
                return "Vorrunde 1";
            case 2:
                return "Vorrunde 2";
            case 3:
                return "Achtelfinale";
            case 4:
                return "Viertelfinale";
            case 5:
                return "Halbfinale";
            case 6:
                return "Finale";
                default:
                    return "undefined";
        }
    }

    private static void saveNotIncluded() {
        File f = new File("data/");
        if (!f.exists()) {
            //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
        }


        try {
            printOutTxtFile.Write("data/notInc.txt",notincluded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean loadNotIncluded() {
        File f = new File("data/notInc.txt");
        if (!f.exists()) return false;
        try {
            notincluded= readInTxtFile.Read("data/notInc.txt");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }


}

