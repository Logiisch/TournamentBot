package helperCore;

import net.dv8tion.jda.api.entities.Guild;
import util.STATIC;
import util.readInTxtFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LangManager {
    private static final String seperator = "#";

    private static HashMap<String,HashMap<String,String>> langs = new HashMap<>();

    public static String get(Guild g, String toTranslate) {
        String lang = STATIC.getSettings(g,"LANGUAGE");
        String prefix = STATIC.getSettings(g,"PREFIX");
        return get(lang,toTranslate,prefix);

    }
    public static String get(String lang, String toTranslate,String prefix) {
        if (!langs.containsKey(lang)) {
            return "LANGUAGE NOT SUPPORTED! PLEASE USE \""+prefix+"setup LANGUAGE ENG\" to switch back to English!";
        }
        HashMap<String,String> choosenLang = langs.get(lang);
        if (!choosenLang.containsKey(toTranslate)) {
            return "KEY "+toTranslate+" IN LANGUAGE "+lang+" NOT SET!";
        }
        return choosenLang.get(toTranslate);
    }

    public static void load() {
        File parent = new File("data/lang/");
        if (!parent.exists()) parent.mkdirs();
        for (File f:parent.listFiles()) {
            load(f.getAbsolutePath());
        }
    }
    private static void load(String path) {
        ArrayList<String> in;

        try {
            in = readInTxtFile.Read(path);
        } catch (IOException e) {
            System.out.println("Path "+path+" could not be loaded!");
            e.printStackTrace();
            return;
        }
        ArrayList<String> cmtfree = new ArrayList<>();
        for (String s:in) {
            if (s.equalsIgnoreCase("")) continue;
            if (!s.startsWith("//")) {
                String[] split = s.split("//");
                cmtfree.add(split[0]);
            }
        }
        String lang = new File(path).getName().replace(".txt","").toUpperCase();
        HashMap<String,String> out = new HashMap<>();
        String preshort = "";
        for (String s:cmtfree) {
            String[] split = s.split(seperator);
            if (split.length<2) {
                System.out.println("Error: \""+s+"\" contains "+split.length+" "+seperator+"'s in lang "+lang);
                continue;
            }
            String value="";
            if (split.length>2) {
                String temp ="";
                for (int i=1;i<split.length;i++) temp+= seperator+split[i];
                value=temp.replaceFirst(seperator,"");

            } else value=split[1];

            if(split[0].equalsIgnoreCase("-")) {
                preshort=value;
                continue;
            }
            if (split[0].startsWith("-")) split[0] = split[0].replaceFirst("-",preshort);
            out.put(split[0],value);
        }
        langs.put(lang,out);
        System.out.println("Successfully loaded lang "+lang+"!");
    }

}
