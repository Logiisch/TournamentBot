package helperCore;

import util.STATIC;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Set;

public  class GuildSettings {

    private HashMap<String,String> values = new HashMap<>();

    private  OffsetDateTime NextTournament = OffsetDateTime.now();

    public GuildSettings(HashMap<String,String> strings, OffsetDateTime next) {


        values = strings;

        NextTournament = next;
    }

    public GuildSettings() {

        values.put("LANGUAGE","ENG");
        values.put("PREFIX", STATIC.PREFIX);
        values.put("ROLE_TOT","");
        values.put("ROLE_FINALE","");
        values.put("ROLE_HALBFINALE","");
        values.put("ROLE_VIERTELFINALE","");
        values.put("ROLE_ACHTELFINALE","");
        values.put("ROLE_VORRUNDE2","");
        values.put("ROLE_VORRUNDE1","");

        values.put("ROLE_WINNER","");
        values.put("ROLE_ADMIN","");
        values.put("ROLE_HELPER","");
        values.put("ROLE_TURNIERTEILNEHMER","");

        values.put("CHANNEL_ALLGEMEIN","");
        values.put("CHANNEL_RESULTS","");

    }

    public void setString(String key, String value) {
        values.put(key.toUpperCase(),value);
    }

    public String getString(String key) {
        return values.getOrDefault(key.toUpperCase(),"");

    }
    public void setNextTournament(OffsetDateTime nt) {
        NextTournament = nt;
    }
    public OffsetDateTime getNextTournament() {
        return NextTournament;
    }
    public boolean isFullySet() {


        if(values.getOrDefault("ROLE_TOT","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_FINALE","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_HALBFINALE","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_VIERTELFINALE","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_ACHTELFINALE","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_VORRUNDE1","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_VORRUNDE2","").equalsIgnoreCase("")) return false;

        if(values.getOrDefault("ROLE_WINNER","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_ADMIN","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_HELPER","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("ROLE_TURNIERTEILNEHMER","").equalsIgnoreCase("")) return false;

        if(values.getOrDefault("CHANNEL_RESULTS","").equalsIgnoreCase("")) return false;
        if(values.getOrDefault("CHANNEL_ALLGEMEIN","").equalsIgnoreCase("")) return false;
        return true;
    }
    public Set<String> keys() {
        return values.keySet();
    }

}
