package util;

import java.io.IOException;
import java.util.ArrayList;

public class SECRETS {
    private static String TOKEN ="";
    private static String CLEVERBOT_USERAPI = "";
    private static String CLEVERBOT_KEYAPI = "";
    private static String MUSIXMATCH_KEY ="";
    private static String SENDGRID_KEY="";
    private static String OWM_TOKEN = "";

    public static String getTOKEN() {
        return TOKEN;
    }

    public static String getCleverbotUserapi() {
        return CLEVERBOT_USERAPI;
    }

    public static String getCleverbotKeyapi() {
        return CLEVERBOT_KEYAPI;
    }

    public static String getMusixmatchKey() {
        return MUSIXMATCH_KEY;
    }

    public static String getSendgridKey() {
        return SENDGRID_KEY;
    }

    public static String getOwmToken() {
        return OWM_TOKEN;
    }
    /*
    true wenn Einlesen Erfolgreich
    false wenn Fehler beim Einlesen
     */
    public static boolean loadTokens() {
        //Hier Speicherort einer Datei, die alle benötigten Keys enthält, angeben
        //String fileSource="C:\\Users\\Privat\\Documents\\Tokens_Discord_Bot.txt";
        String fileSource="Tokens_Discord_Bot.txt";
        //Zeilen, die mit Doppelslash beginnen, werden ignoriert

        /*
        Reinfolge:
        Discord-Token
        Cleverbot-UserKey
        Cleverbot-ApiKey
        Musixmatch-ApiKey
        Sendgrid-ApiKey
        Open-Weather-Map-ApiKey
         */
        ArrayList<String> in = new ArrayList<>();
        try {
            in = readInTxtFile.Read(fileSource);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fehler beim Einlesen der Tokens!");
            return false;
        }
        ArrayList<String> commentfree = new ArrayList<>();
        for (String s:in) {
            if (!s.startsWith("//")) commentfree.add(s);
        }
        if (commentfree.size()!=6) {
            System.out.println("Falsche Anzahl an Keys, erwartet: 6, erhalten: "+commentfree.size());
            return false;
        }
        TOKEN = commentfree.get(0);
        CLEVERBOT_USERAPI = commentfree.get(1);
        CLEVERBOT_KEYAPI = commentfree.get(2);
        MUSIXMATCH_KEY = commentfree.get(3);
        SENDGRID_KEY = commentfree.get(4);
        OWM_TOKEN = commentfree.get(5);

        return true;
    }
}
