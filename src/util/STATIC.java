package util;

/*import Threads.HiskiGiveawayThread;
import Threads.TimerThread;
import commands.cmdGetPoints;
import listeners.activityListener;*/

import helperCore.GuildSettings;
import helperCore.Logic;
import helperCore.TournamentNode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class STATIC {
    private static HashMap<String, GuildSettings> settings = new HashMap<>();
    public static ArrayList<String> GuildsWithTeamMode = new ArrayList<>();
    public static String ACTIVITY = "keine große Rolle";
    public static String PREFIX = "t!";
    public static String ADDLINK = "https://discordapp.com/oauth2/authorize?client_id=705567211380801598&scope=bot&permissions=268692560";
    public static String CODELINK = "https://github.com/Logiisch/TournamentBot";
    public static String OWNERID = "318457868917407745";
    public static String EMAIL = "tournamentbot@logii.de";






    /*public static String ROLE_TOT = "678319461610029056";
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
    public static String ROLE_TURNIERTEILNHEMER = "707266470371131412";

    public static String CHANNEL_RESULTS = "705557075106988144";
    public static String CHANNEL_ALLGEMEIN = "676029060136042536";*/

    public static String TESTGUILD = "676002000835444736";

    public static boolean dryRun = false;

    public static boolean SOMMERZEIT = false;

    public static void setSettings(Guild g, String key, String value) {
        setSettings(g.getId(),key,value);
    }
    public static void setSettings(String guildid, String key, String value) {
        GuildSettings gs = settings.getOrDefault(guildid,new GuildSettings());
        gs.setString(key,value);
        settings.put(guildid,gs);
        saveSettings();
    }
    public static String getSettings(Guild g, String key) {
        return getSettings(g.getId(),key);
    }
    public static String getSettings(String guildid, String key) {
        GuildSettings gs = settings.getOrDefault(guildid,new GuildSettings());
        return gs.getString(key);
    }
    public static void setNextTournament(Guild g,OffsetDateTime nT) {
        setNextTournament(g.getId(),nT);
    }
    public static void setNextTournament(String guildid,OffsetDateTime nT) {
        GuildSettings gs = settings.getOrDefault(guildid,new GuildSettings());
        gs.setNextTournament(nT);
        settings.put(guildid,gs);
        saveSettings();
    }
    public static OffsetDateTime getNextTournament(Guild g) {
        return getNextTournament(g.getId());
    }
    public static OffsetDateTime getNextTournament(String guildid) {
        GuildSettings gs = settings.getOrDefault(guildid,new GuildSettings());
        return gs.getNextTournament();
    }
    public static boolean canStartTournament(Guild g) {
        return canStartTournament(g.getId());
    }

    public static boolean canStartTournament(String guildid) {
        if (!settings.containsKey(guildid)) return false;
        GuildSettings gs = settings.get(guildid);
        return gs.isFullySet();
    }

    public static Set<String> getAllKeys(String guildid) {
        return settings.getOrDefault(guildid,new GuildSettings()).keys();
    }
    public static Set<String> getAllKeys(Guild g) {
        return getAllKeys(g.getId());
    }

    public static void saveSettings() {
        for (String id: settings.keySet()) {
            ArrayList<String> in = saveSettings(settings.get(id));
            try {
                printOutTxtFile.Write("data/guilds/"+id+"/settings.txt",in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static ArrayList<String> saveSettings(GuildSettings gs) {
        ArrayList<String> out = new ArrayList<>();


        for (String s:gs.keys()) {


            String temp = s.toUpperCase() + ":" + gs.getString(s.toUpperCase());
            out.add(temp);

        }
        OffsetDateTime nt = gs.getNextTournament();
        String time = nt.getDayOfMonth()+" "+nt.getMonthValue()+" "+nt.getYear()+" "+nt.getHour()+" "+nt.getMinute();
        out.add("NEXT_TOURNAMENT:"+time);
        return out;
    }
    public static GuildSettings loadSettings(ArrayList<String> in) {
        OffsetDateTime nt = OffsetDateTime.now();
        HashMap<String,String> out = new HashMap<>();
        for (String part:in) {
            String[] sp = part.split(":");
            String key = sp[0];
            if (sp.length<2) {
                out.put(key,"");
                continue;
            }
            String value = sp[1];
            if (key.equalsIgnoreCase("NEXT_TOURNAMENT")) {
                String[] ts = value.split(" ");
                int[] time = new int[ts.length];
                for (int i=0;i<ts.length;i++) {
                    time[i] = Integer.parseInt(ts[i]);
                }
                nt = OffsetDateTime.of(time[2],time[1],time[0],time[3],time[4],0,0,nt.getOffset());
            } else out.put(key,value);
        }
        return new GuildSettings(out,nt);
    }
    public static void loadSettings() {
        String path = "data/guilds/";
        File f = new File(path);
        if (!f.exists()) return;
        File[] files = f.listFiles();
        for (File fc:files) {
            String pathnew = path+fc.getName()+"/settings.txt";
            File fnew = new File(pathnew);
            if (!fnew.exists()) continue;
            ArrayList<String> in= new ArrayList<>();
            try {
                in = readInTxtFile.Read(pathnew);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            GuildSettings gs =loadSettings(in);
            settings.put(fc.getName(),gs);

        }
    }


}

