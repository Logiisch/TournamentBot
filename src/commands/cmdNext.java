package commands;

import helperCore.LangManager;
import helperCore.Logic;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import util.SECRETS;
import util.STATIC;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
            String tzs = STATIC.SOMMERZEIT?"CEST":"CET";
            if (args.length==1) {
                String ret = "";
                try {
                    ret = convert(args[0].toUpperCase(),tn);
                } catch (NumberFormatException e) {
                    event.getTextChannel().sendMessage("ERROR: "+e.getMessage()).queue();
                    return;
                }catch (IOException | SAXException | ParserConfigurationException e) {
                    event.getTextChannel().sendMessage("An Error occured. Please try again later").queue();
                    e.printStackTrace();
                    return;
                }
                long offset = Long.parseLong(ret);
                tn =tn.withOffsetSameInstant(ZoneOffset.ofTotalSeconds((int) offset));
                tzs = args[0].toUpperCase();
            }
            String day = digitadd(tn.getDayOfMonth(),2);
            String month = digitadd(tn.getMonthValue(),2);
            String year = digitadd(tn.getYear(),4);
            String hour = digitadd(tn.getHour(),2);
            String minute = digitadd(tn.getMinute(),2);
            String out =LangManager.get(event.getGuild(),"cmdNextGet").replace("%DAY%",day).replace("%MONTH%",month).replace("%YEAR%",year).replace("%HOUR%",hour).replace("%MINUTE%",minute).replace("%TIMEZONE%",tzs);
            event.getTextChannel().sendMessage(out).queue();
            return;
        }
        if (args.length<2) {
            String out ="";
            OffsetDateTime tn = STATIC.getNextTournament(event.getGuild());
            if(OffsetDateTime.now().isAfter(tn)) {
                if(Logic.nodes.isEmpty()) {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextNotSet")).queue();
                } else {
                    event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdNextAlreadyRunning")).queue();
                }
            } else {
                String tzs = STATIC.SOMMERZEIT?"CEST":"CET";
                if (args.length>0) {
                    String ret = "";
                    try {
                        ret = convert(args[0].toUpperCase(),tn);
                    } catch (NumberFormatException e) {
                        event.getTextChannel().sendMessage("ERROR: "+e.getMessage()).queue();
                        return;
                    }catch (IOException | SAXException | ParserConfigurationException e) {
                        event.getTextChannel().sendMessage("An Error occured. Please try again later").queue();
                        e.printStackTrace();
                        return;
                    }
                    long offset = Long.parseLong(ret);
                    tn =tn.withOffsetSameInstant(ZoneOffset.ofTotalSeconds((int) offset));
                    tzs = args[0];
                }


                String day = digitadd(tn.getDayOfMonth(),2);
                String month = digitadd(tn.getMonthValue(),2);
                String year = digitadd(tn.getYear(),4);
                String hour = digitadd(tn.getHour(),2);
                String minute = digitadd(tn.getMinute(),2);
                out += LangManager.get(event.getGuild(),"cmdNextGet").replace("%DAY%",day).replace("%MONTH%",month).replace("%YEAR%",year).replace("%HOUR%",hour).replace("%MINUTE%",minute).replace("%TIMEZONE%",tzs);
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
        return LangManager.get(g,"cmdNextDef").replace("%PREFIX%",prefix);
    }

    private String digitadd(int numbertoadd, int howmanydigits) {
        StringBuilder out = new StringBuilder("" + numbertoadd);

        while (out.length()<howmanydigits) {
            out.insert(0, "0");
        }
        return out.toString();
    }

    private static String convert(String tz, OffsetDateTime odt) throws IOException, SAXException, ParserConfigurationException, NumberFormatException {
        long unix =odt.toEpochSecond();
        String from = //STATIC.SOMMERZEIT?"CEST":"CET";
                "UTC";

        String in = "http://api.timezonedb.com/v2.1/convert-time-zone?key="+ SECRETS.getTimeDBKey() +"&format=xml&from="+from+"&to="+tz+"&time="+unix;
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

        util.printOutTxtFile.Write("xml.txt",outal);
        Document document = builder.parse("xml.txt");

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
                }
                if (elem.getTagName().equalsIgnoreCase("message")&&!hasworked) {
                    String ret = elem.getChildNodes().item(0).getNodeValue().replace("\n","");
                    throw new NumberFormatException(ret);
                }
                if (elem.getTagName().equalsIgnoreCase("offset")) {
                    return  elem.getChildNodes().item(0).getNodeValue().replace("\n","").replace(" ","");
                }


                // Get the value of all sub-elements.
                /*String status = elem.getElementsByTagName("status").item(0).getChildNodes().item(0).getNodeValue();
                String ret ="";

                if (!status.equalsIgnoreCase("OK")) {
                    ret = elem.getElementsByTagName("message").item(0).getChildNodes().item(0).getNodeValue();
                    throw new NumberFormatException(ret);
                } else {
                    ret = elem.getElementsByTagName("offset").item(0).getChildNodes().item(0).getNodeValue();
                }
                return ret;*/

            }
        }
        throw new NumberFormatException("ERROR whilst connecting to API!");
    }
}
