package commands;

import helperCore.Logic;
import helperCore.TournamentNode;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.AttachmentOption;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class cmdBracket implements Command {
    private static final int fontsize = 26;
    private static final String path = "bracket.jpg";
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (Logic.nodes.isEmpty()) {
            event.getTextChannel().sendMessage("Das Turnier hat noch nicht mal angefangen...").queue();
            return;
        }
        event.getTextChannel().sendMessage("Die Bracket wird erstellt. Dies kann ein paar Sekunden dauern...").queue();
        try {
            drawImage(event.getAuthor());
        } catch (Exception e) {
            event.getTextChannel().sendMessage("Anscheinend ist ein Fehler aufgetreten! \n"+e.getMessage()).queue();
            e.printStackTrace();
            return;
        }
        event.getTextChannel().sendFile(new File(path),"Bracket_aktuell.jpg").queue();
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
        return "Schau dir die Bracket an. Fett sind die Rungengewinner, blau bist du!";
    }
    private void drawImage(User author) throws Exception{
        String org = "original.jpg";
        String wove = path;
        File original = new File(org);
        File workingVersion = new File(wove);
        BufferedImage myPicture;

        myPicture = ImageIO.read(original);

        Graphics2D g = (Graphics2D) myPicture.getGraphics();
        g.setStroke(new BasicStroke(3));
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.DIALOG,Font.PLAIN,fontsize));
        for (int nid:Logic.nodes.keySet()) {
            TournamentNode tn = Logic.nodes.get(nid);
            draw(tn, author, g);
        }


        ImageIO.write(myPicture,"JPEG",workingVersion);


    }
    private static void draw(TournamentNode tn, User u, Graphics2D g) {
        int brcktnd = tn.getBracketNbr();
        if (tn.winner!=null) {
            if (tn.winner.getId().equalsIgnoreCase(u.getId())) g.setPaint(Color.blue); else g.setPaint(Color.black);
            if (Logic.nodes.get(tn.promoteToNID).winner.getId().equalsIgnoreCase(tn.winner.getId())) g.setFont(new Font(Font.DIALOG,Font.BOLD,fontsize)); else g.setFont(new Font(Font.DIALOG,Font.PLAIN,fontsize));
            g.drawString(bracketify(tn.winner),getx(brcktnd),gety(brcktnd));
        }
        if(!tn.getBracketSub().isEmpty()) {
            if (tn.players.get(0).getId().equalsIgnoreCase(u.getId())) g.setPaint(Color.blue); else g.setPaint(Color.black);
            if (tn.winner.getId().equalsIgnoreCase(tn.players.get(0).getId())) g.setFont(new Font(Font.DIALOG,Font.BOLD,fontsize)); else g.setFont(new Font(Font.DIALOG,Font.PLAIN,fontsize));
            g.drawString(bracketify(tn.players.get(0)),getx(tn.getBracketSub().get(0)),gety(tn.getBracketSub().get(0)));
            if (tn.players.get(1).getId().equalsIgnoreCase(u.getId())) g.setPaint(Color.blue); else g.setPaint(Color.black);
            if (tn.winner.getId().equalsIgnoreCase(tn.players.get(1).getId())) g.setFont(new Font(Font.DIALOG,Font.BOLD,fontsize)); else g.setFont(new Font(Font.DIALOG,Font.PLAIN,fontsize));
            g.drawString(bracketify(tn.players.get(1)),getx(tn.getBracketSub().get(1)),gety(tn.getBracketSub().get(1)));
        }

    }
    private static String bracketify(User u) {
        if (u.getId().equalsIgnoreCase(u.getJDA().getSelfUser().getId())) return "[Freier Platz]";
        String un = u.getName();
        if (un.length()>15) {
            un = un.substring(0,12)+"...";
        }
        return un;
    }

    private static int getx(int nbr) {
        if (nbr<33) return 210;
        if (nbr<65) return 2945;
        if (nbr<81) return 410;
        if (nbr<97) return 2685;
        if (nbr<105) return 660;
        if (nbr<113) return 2420;
        if (nbr<117) return 935;
        if (nbr<121) return 2155;
        if (nbr<123) return 1205;
        if (nbr<125) return 1890;
        if (nbr==125) return 1460;
        if (nbr==126) return 1535;
        if (nbr==127) return 1530;
        return 500;
    }
    private static int gety(int nbr) {
        switch (nbr) {
            case 1:
            case 33:
                return 284;
            case 2:
            case 34:
                return 357;
            case 3:
            case 35:
                return 415;
            case 4:
            case 36:
                return 482;
            case 5:
            case 37:
                return 537;
            case 6:
            case 38:
                return 610;
            case 7:
            case 39:
                return 668;
            case 8:
            case 40:
                return 741;
            case 9:
            case 41:
                return 793;
            case 10:
            case 42:
                return 866;
            case 11:
            case 43:
                return 924;
            case 12:
            case 44:
                return 997;
            case 13:
            case 45:
                return 1051;
            case 14:
            case 46:
                return 1125;
            case 15:
            case 47:
                return 1182;
            case 16:
            case 48:
                return 1258;
            case 17:
            case 49:
                return 1311;
            case 18:
            case 50:
                return 1389;
            case 19:
            case 51:
                return 1453;
            case 20:
            case 52:
                return 1529;
            case 21:
            case 53:
                return 1572;
            case 22:
            case 54:
                return 1645;
            case 23:
            case 55:
                return 1703;
            case 24:
            case 56:
                return 1776;
            case 25:
            case 57:
                return 1823;
            case 26:
            case 58:
                return 1890;
            case 27:
            case 59:
                return 1948;
            case 28:
            case 60:
                return 2020;
            case 29:
            case 61:
                return 2064;
            case 30:
            case 62:
                return 2143;
            case 31:
            case 63:
                return 2201;
            case 32:
            case 64:
                return 2273;

            case 65:
            case 81:
                return 313;
            case 66:
            case 82:
                return 459;
            case 67:
            case 83:
                return 581;
            case 68:
            case 84:
                return 712;
            case 69:
            case 85:
                return 837;
            case 70:
            case 86:
                return 968;
            case 71:
            case 87:
                return 1095;
            case 72:
            case 88:
                return 1229;
            case 73:
            case 89:
                return 1360;
            case 74:
            case 90:
                return 1497;
            case 75:
            case 91:
                return 1616;
            case 76:
            case 92:
                return 1747;
            case 77:
            case 93:
                return 1860;
            case 78:
            case 94:
                return 1991;
            case 79:
            case 95:
                return 2113;
            case 80:
            case 96:
                return 2244;



            case 97:
            case 105:
                return 386;
            case 98:
            case 106:
                return 639;
            case 99:
            case 107:
                return 895;
            case 100:
            case 108:
                return 1154;
            case 101:
            case 109:
                return 1418;
            case 102:
            case 110:
                return 1674;
            case 103:
            case 111:
                return 1919;
            case 104:
            case 112:
                return 2172;

            case 113:
            case 117:
                return 538;
            case 114:
            case 118:
                return 1052;
            case 115:
            case 119:
                return 1572;
            case 116:
            case 120:
                return 2064;

            case 121:
            case 123:
                return 794;
            case 122:
            case 124:
                return 1823;

            case 125:
                return 1154;
            case 126:
                return 1572;
            case 127:
                return 1418;
        }


        return 500;
    }
}
