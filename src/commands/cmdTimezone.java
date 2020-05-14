package commands;

import helperCore.LangManager;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import util.SECRETS;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class cmdTimezone implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        if (args.length<1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdTZUsage").replace("%PREFIX%",prefix)).queue();
            return;
        }
        ArrayList<String> all = new ArrayList<>();
        try {
            all = getAll();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdTZError")+e.getMessage()).queue();
            e.printStackTrace();
            return;
        }
        if (all.size()==0) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdTZError")+"all.size() == 0").queue();
            return;
        }
        boolean explicit = false;
        if (args[0].length()==2&&args[0].toUpperCase().equals(args[0])) explicit = true;
        ArrayList<String> sorted = sort(all,args[0],explicit);
         if(sorted.size()==0) {
             event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdTZNoMatches")).queue();
             return;
         }
         event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdTZResults")).queue();
         String out = "";
         for (String s:sorted) {
             if (s.length()+out.length()+1>2000) {
                 event.getTextChannel().sendMessage(out).queue();
                 out = "";
             }
             out += s+"\n";
         }
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
        return LangManager.get(g,"cmdTZDef");
    }

    private static ArrayList<String> getAll() throws IOException, ParserConfigurationException, SAXException {
        ArrayList<String> out = new ArrayList<>();

        String in = "http://api.timezonedb.com/v2.1/list-time-zone?key="+SECRETS.getTimeDBKey()+"&format=xml";
        String html = "";
        try {
            html = Jsoup.connect(in).get().html();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Load the input XML document, parse it and return an instance of the
        // Document class.
        ArrayList<String> outal = new ArrayList<>();
        outal.add(html);

        util.printOutTxtFile.Write("timezones.txt",outal);
        Document document = builder.parse("timezones.txt");

        NodeList nodeList = document.getDocumentElement().getChildNodes();
        boolean hasworked = true;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;

                if (elem.getTagName().equalsIgnoreCase("status")) {
                    String status = elem.getChildNodes().item(0).getNodeValue().replace("\n","").replace(" ","");
                    if (!status.equalsIgnoreCase("OK")) {
                        hasworked = false;
                        System.out.println("status="+status);
                    }
                    continue;
                }
                if (elem.getTagName().equalsIgnoreCase("message")&&!hasworked) {
                    String ret = elem.getChildNodes().item(0).getNodeValue().replace("\n","");
                    throw new NumberFormatException(ret);
                }
                if (!elem.getTagName().equalsIgnoreCase("zones")) continue;

                NodeList zones =elem.getChildNodes();
                for (int j = 0; j < zones.getLength(); j++) {
                    Node zonen = zones.item(j);
                    if (zonen.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element zone = (Element) zonen;
                    String CC = zone.getElementsByTagName("countryCode")
                            .item(0).getChildNodes().item(0).getNodeValue();

                    // Get the value of all sub-elements.
                    String CN = zone.getElementsByTagName("countryName")
                            .item(0).getChildNodes().item(0).getNodeValue();

                    String ZN = zone.getElementsByTagName("zoneName").item(0)
                            .getChildNodes().item(0).getNodeValue();

                    String build = "`"+ZN.replace(" ","") + "` in "+CN.replace(" ","")+" ("+CC.replace(" ","")+")";
                    build = build.replace("\n","");
                    out.add(build);
                }


            }
        }

        return out;
    }
    private static  ArrayList<String> sort (ArrayList<String> in, String key,boolean explicitCC) {
        ArrayList<String> out = new ArrayList<>();
        for (String s:in) {
            if (s.toUpperCase().contains(key.toUpperCase())&&!explicitCC) out.add(s); else {
                if (s.endsWith("("+key+")")) out.add(s);
            }
        }

        return out;
    }
}
