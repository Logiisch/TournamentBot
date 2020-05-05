package helperCore;

import commands.cmdRetry;
import listeners.ConfirmReactListener;
import listeners.commandListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import util.STATIC;
import util.printOutTxtFile;
import util.readInTxtFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Logic {

    public static HashMap<Integer,TournamentNode> nodes = new HashMap<>();

    public static String start(MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        nodes.clear();

        List<Member> mbrs =event.getGuild().getMembers();


        TournamentNode finale = new TournamentNode(1,0,6,127);
        int nid = 2;
        ArrayList<TournamentNode> temp = new ArrayList<>();
        temp.add(finale);

        while (!temp.isEmpty()) {
            TournamentNode tn = temp.get(0);
            if (tn.getRunde()>1) {
                TournamentNode a = new TournamentNode(nid++,tn.NID,tn.getRunde()-1,getSubs(tn.getBracketNbr()).get(0));
                TournamentNode b = new TournamentNode(nid++,tn.NID,tn.getRunde()-1,getSubs(tn.getBracketNbr()).get(1));

                tn.promoteFrom.add(a.NID);
                tn.promoteFrom.add(b.NID);
                temp.add(a);
                temp.add(b);
            } else {
                for (int i:getSubs(tn.getBracketNbr())) {
                    tn.addSub(i);
                }
            }
            temp.remove(0);
            tn.update();
        }

        ArrayList<User> teilnehmer = new ArrayList<>();
        Role tlt = event.getGuild().getRoleById(STATIC.ROLE_TESTLAUFTEILNEHMER);
        for (Member m:mbrs) {
            if (!STATIC.getNotIncluded().contains(m.getUser().getId())&&!m.getUser().isBot()&&((!STATIC.dryRun&&m.getRoles().contains(event.getGuild().getRoleById(STATIC.ROLE_TURNIERTEILNHEMER)))||m.getRoles().contains(tlt))) teilnehmer.add(m.getUser());

        }
        StringBuilder out = new StringBuilder("Folgende Nutzer spielen mit:\n");
        for (User u:teilnehmer) {
            if (u.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) continue;
            out.append(u.getName()).append("\n");
        }
        event.getTextChannel().sendMessage(out.toString()).queue();

        if (teilnehmer.size()>64) {
            return "Es können maximal 64 Leute teilnehmen, bitte enferne Spieler mit `"+prefix+"kick [Anzahl/User als @Mention]`!";
        }
        while (teilnehmer.size()<64) {
            teilnehmer.add(event.getJDA().getSelfUser());
        }
        Collections.shuffle(teilnehmer);
        for (int i:nodes.keySet()) {
            TournamentNode node = nodes.get(i);
            if (node.getRunde()==1) {
                User a = teilnehmer.get(0);
                teilnehmer.remove(0);

                User b = teilnehmer.get(0);
                teilnehmer.remove(0);

                node.players.add(a);
                node.players.add(b);

                Member mema =event.getGuild().getMember(a);
                Member memb =event.getGuild().getMember(b);
                assert mema!=null;
                assert memb!=null;
                Role vr1 = event.getGuild().getRoleById(STATIC.ROLE_VORRUNDE1);
                assert vr1 != null;
                event.getGuild().addRoleToMember(mema,vr1).queue();
                event.getGuild().addRoleToMember(memb,vr1).queue();

            }
            node.update();
        }
        refreshTournamnet(event,false);

        if (STATIC.dryRun) {
            return "Der Testlauf wurde gestartet! Alle, die michmacheen wollen, können es tun. Folgt dazu den Anweisungen, die ihr erhalten werdet/habt!";
        }
        return "Das Turnier wurde gestartet!";
    }

    private static void refreshTournamnet(MessageReceivedEvent event, boolean exitOnHalfway) {
        boolean changedsomething = false;
        for (int nid: nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (node.players.size()<2) continue;
            if (node.players.contains(event.getJDA().getSelfUser())&&node.winner==null) {
                changedsomething= true;
                if (node.players.get(0).getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
                    node.winner= node.players.get(1);
                    int promid = node.promoteToNID;
                    TournamentNode promto = nodes.get(promid);
                    promto.players.add(node.players.get(1));
                    if (!node.players.get(1).getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) node.players.get(1).openPrivateChannel().complete().sendMessage("Du hast Glück, da nicht genügend Spieler da sind, um das Turnier zu füllen, oder jemand das Turnier verlassen hat, bist du direkt eine Runde weitergekommen!").queue();
                    node.update();
                    promto.update();
                } else {
                    node.winner= node.players.get(0);
                    int promid = node.promoteToNID;
                    TournamentNode promto = nodes.get(promid);
                    promto.players.add(node.players.get(0));
                    if (!node.players.get(0).getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) node.players.get(0).openPrivateChannel().complete().sendMessage("Du hast Glück, da nicht genügend Spieler da sind, um das Turnier zu füllen oder jemand das Turnier verlassen hat, bist du direkt eine Runde weitergekommen!").queue();
                    node.update();
                    promto.update();
                }
            }

        }
        if (changedsomething) refreshTournamnet(event,true);
        if(exitOnHalfway) return;
        String prefix = commandListener.getPrefix(event.getGuild());
        for (int nid: nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (node.players.size()<2||node.winner!=null) continue;
            User a = node.players.get(0);
            User b = node.players.get(1);
            String senda = "Dein Gegner ist nun "+b.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit \uD83D\uDC4D, dass du anwesend bist. Du hast dafür 15min Zeit!";
            String sendb = "Dein Gegner ist nun "+a.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit \uD83D\uDC4D, dass du anwesend bist. Du hast dafür 15min Zeit!";
            Message msga=STATIC.trysend(a,senda);
            Message msgb=STATIC.trysend(b,sendb);
            assert msga!=null;
            assert msgb!=null;
            msga.addReaction("U+1F44D").queue();
            msgb.addReaction("U+1F44D").queue();
            RoundTime rt = new RoundTime(node.NID,15,event.getGuild());
            ConfirmReactListener.rtimes.put(msga.getId(),rt);
            ConfirmReactListener.rtimes.put(msgb.getId(),rt);

        }


    }

    private static ArrayList<Integer> getSubs(int brckid) {
        ArrayList<Integer> ret = new ArrayList<>();
        switch (brckid) {
            case 65:
                ret.add(1);
                ret.add(2);
                break;
            case 66:
                ret.add(3);
                ret.add(4);
                break;
            case 67:
                ret.add(5);
                ret.add(6);
                break;
            case 68:
                ret.add(7);
                ret.add(8);
                break;
            case 69:
                ret.add(9);
                ret.add(10);
                break;
            case 70:
                ret.add(11);
                ret.add(12);
                break;
            case 71:
                ret.add(13);
                ret.add(14);
                break;
            case 72:
                ret.add(15);
                ret.add(16);
                break;
            case 73:
                ret.add(17);
                ret.add(18);
                break;
            case 74:
                ret.add(19);
                ret.add(20);
                break;
            case 75:
                ret.add(21);
                ret.add(22);
                break;
            case 76:
                ret.add(23);
                ret.add(24);
                break;
            case 77:
                ret.add(25);
                ret.add(26);
                break;
            case 78:
                ret.add(27);
                ret.add(28);
                break;
            case 79:
                ret.add(29);
                ret.add(30);
                break;
            case 80:
                ret.add(31);
                ret.add(32);
                break;

            case 81:
                ret.add(33);
                ret.add(34);
                break;
            case 82:
                ret.add(35);
                ret.add(36);
                break;
            case 83:
                ret.add(37);
                ret.add(38);
                break;
            case 84:
                ret.add(39);
                ret.add(40);
                break;
            case 85:
                ret.add(41);
                ret.add(42);
                break;
            case 86:
                ret.add(43);
                ret.add(44);
                break;
            case 87:
                ret.add(45);
                ret.add(46);
                break;
            case 88:
                ret.add(47);
                ret.add(48);
                break;
            case 89:
                ret.add(49);
                ret.add(50);
                break;
            case 90:
                ret.add(51);
                ret.add(52);
                break;
            case 91:
                ret.add(53);
                ret.add(54);
                break;
            case 92:
                ret.add(55);
                ret.add(56);
                break;
            case 93:
                ret.add(57);
                ret.add(58);
                break;
            case 94:
                ret.add(59);
                ret.add(60);
                break;
            case 95:
                ret.add(61);
                ret.add(62);
                break;
            case 96:
                ret.add(63);
                ret.add(64);
                break;

            case 97:
                ret.add(65);
                ret.add(66);
                break;
            case 98:
                ret.add(67);
                ret.add(68);
                break;
            case 99:
                ret.add(69);
                ret.add(70);
                break;
            case 100:
                ret.add(71);
                ret.add(72);
                break;
            case 101:
                ret.add(73);
                ret.add(74);
                break;
            case 102:
                ret.add(75);
                ret.add(76);
                break;
            case 103:
                ret.add(77);
                ret.add(78);
                break;
            case 104:
                ret.add(79);
                ret.add(80);
                break;

            case 105:
                ret.add(81);
                ret.add(82);
                break;
            case 106:
                ret.add(83);
                ret.add(84);
                break;
            case 107:
                ret.add(85);
                ret.add(86);
                break;
            case 108:
                ret.add(87);
                ret.add(88);
                break;
            case 109:
                ret.add(89);
                ret.add(90);
                break;
            case 110:
                ret.add(91);
                ret.add(92);
                break;
            case 111:
                ret.add(93);
                ret.add(94);
                break;
            case 112:
                ret.add(95);
                ret.add(96);
                break;

            case 113:
                ret.add(97);
                ret.add(98);
                break;
            case 114:
                ret.add(99);
                ret.add(100);
                break;
            case 115:
                ret.add(101);
                ret.add(102);
                break;
            case 116:
                ret.add(103);
                ret.add(104);
                break;

            case 117:
                ret.add(105);
                ret.add(106);
                break;
            case 118:
                ret.add(107);
                ret.add(108);
                break;
            case 119:
                ret.add(109);
                ret.add(110);
                break;
            case 120:
                ret.add(111);
                ret.add(112);
                break;

            case 121:
                ret.add(113);
                ret.add(114);
                break;
            case 122:
                ret.add(115);
                ret.add(116);
                break;

            case 123:
                ret.add(117);
                ret.add(118);
                break;
            case 124:
                ret.add(119);
                ret.add(120);
                break;

            case 125:
                ret.add(121);
                ret.add(122);
                break;

            case 126:
                ret.add(123);
                ret.add(124);
                break;

            case 127:
                ret.add(125);
                ret.add(126);
                break;



        }
        return ret;

    }

    public static void logresult(User u, boolean haswon, Guild g) throws Exception{
        TournamentNode tn = null;

        for(int i:nodes.keySet()) {
            TournamentNode node = nodes.get(i);
            if (node.winner==null&&node.players.contains(u)) tn = node;
        }
        if (tn==null) {
            throw new Exception("Es konnte kein offenes Spiel passend zu dem Spieler gefunden werden!");
        }
         if(tn.players.size()<2) {
             throw new Exception("Du kannst noch kein Ergebnis eintragen, da du noch kein Gegner zugewiesen wurde! (matchID="+tn.NID+")");
         }
        User winner = null;
        if(haswon) {
            winner = u;
        } else {
            for (User us:tn.players) {
                if (us.getId().equalsIgnoreCase(u.getId())) continue;
                winner= us;
            }
        }
        if (winner==null) {
            throw new Exception("Interner Fehler:winner in line 158 still null");
        }
        tn.winner=winner;
        tn.update();
        if(tn.getRunde()==6) {
            Objects.requireNonNull(g.getTextChannelById(STATIC.CHANNEL_ALLGEMEIN)).sendMessage("Das Turnier ist beendet, der Gewinnner steht fest: "+winner.getName()+" hat gewonnen! Glückwunsch!").queue();
            g.addRoleToMember(Objects.requireNonNull(g.getMember(u)), Objects.requireNonNull(g.getRoleById(STATIC.ROLE_WINNER))).queue();
            EmbedBuilder eb = new EmbedBuilder().setColor(roleOfRound(6,g).getColor()).setTitle("Endergebnis");
            eb.setDescription("Das Turnier gewonnen hat: "+ winner.getName()+"\nHerzlichen Glückwunsch!\n\nAuf Platz zwei ist "+(tn.players.get(0).getId().equalsIgnoreCase(winner.getId())?tn.players.get(1).getName():tn.players.get(0).getName()));
            Objects.requireNonNull(g.getTextChannelById(STATIC.CHANNEL_RESULTS)).sendMessage(eb.build()).queue();
            return;
        }

        tn.winner=winner;
        TournamentNode promto = nodes.get(tn.promoteToNID);
        promto.players.add(winner);

        if (promto.players.size()==2) {
            if (promto.players.contains(g.getSelfMember().getUser())) {
                tn.update();
                promto.update();
                logresult(winner, true,g);
                return;
            }
            User pla = promto.players.get(0);
            User plb = promto.players.get(1);
            String prefix = commandListener.getPrefix(g);


            String senda = "Dein Gegner ist nun "+plb.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit  \uD83D\uDC4D, dass du anwesend bist. Du hast dafür 15min Zeit!";
            String sendb = "Dein Gegner ist nun "+pla.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit  \uD83D\uDC4D, dass du anwesend bist. Du hast dafür 15min Zeit!";
            Message msga=STATIC.trysend(pla,senda);
            Message msgb=STATIC.trysend(plb,sendb);
            assert msga!=null;
            assert msgb!=null;
            msga.addReaction("U+1F44D").queue();
            msgb.addReaction("U+1F44D").queue();
            RoundTime rt = new RoundTime(promto.NID,15,g);
            ConfirmReactListener.rtimes.put(msga.getId(),rt);
            ConfirmReactListener.rtimes.put(msgb.getId(),rt);
        } else {
            STATIC.trysend(winner,"Dein Gegner ist noch nicht fertig. Warte noch einen Moment.");
            return;
        }
        refreshRoles(g.getMember(winner),false,promto.getRunde());
        User looser = null;
        for (User us:tn.players) {
            if (us.getId().equalsIgnoreCase(winner.getId())) continue;
            looser= us;
        }
        if (looser==null) {
            throw new Exception("Interner Fehler:looser in line 191 still null");
        }
        if(winner.getId().equalsIgnoreCase(looser.getId())) {
            throw new Exception("Interner Fehler:looser.id == winner.id");
        }


        refreshRoles(g.getMember(looser),true,tn.getRunde());
        tn.update();
        promto.update();
        TextChannel tc=g.getTextChannelById(STATIC.CHANNEL_RESULTS);
        EmbedBuilder eb = new EmbedBuilder().setTitle("Matchergebnisse").setAuthor(STATIC.getRoundname(tn.getRunde()));
        if (tn.players.contains(g.getSelfMember().getUser())) {
            User named;
            if (tn.players.get(0).getId().equalsIgnoreCase(g.getJDA().getSelfUser().getId())) {
                named = tn.players.get(1);
            } else {
                named = tn.players.get(0);
            }
            eb.setDescription("Da das Turnier nicht voll war, konnte "+named.getName()+" direkt in die Runde \""+STATIC.getRoundname(promto.getRunde())+"\" aufsteigen!");
        } else {
            eb.setDescription("Das Match zwischen " + tn.players.get(0).getName() + " und " + tn.players.get(1).getName() + " wurde ausgetragen. Der Gewinner ist " + tn.winner.getName() + " ! Dieser Spieler ist nun i" + ((promto.getRunde() < 3) ? "n " : "m ") + STATIC.getRoundname(promto.getRunde()) + "!");
        }
        Role role = roleOfRound(tn.getRunde(),g);

        assert role != null;
        eb.setColor(role.getColor());
        assert tc != null;
        tc.sendMessage(eb.build()).queue();
    }
    private static void refreshRoles(Member m, boolean isdead, int round) throws Exception {
        if (m==null){
            throw new Exception("Interner Fehler:m is null in Line 552");
        }
        Role role = roleOfRound(round,m.getGuild());

        assert role != null;
        m.getGuild().addRoleToMember(m,role).queue();
        if (isdead) {
            Role dead = m.getGuild().getRoleById(STATIC.ROLE_TOT);
            assert dead != null;
            m.getGuild().addRoleToMember(m,dead).queue();
        }

    }
    public static Role roleOfRound(int runde,Guild g) throws Exception {
        Role role;
        switch (runde) {
            case 1:
                role = g.getRoleById(STATIC.ROLE_VORRUNDE1);
                break;
            case 2:
                role = g.getRoleById(STATIC.ROLE_VORRUNDE2);
                break;
            case 3:
                role = g.getRoleById(STATIC.ROLE_ACHTELFINALE);
                break;
            case 4:
                role = g.getRoleById(STATIC.ROLE_VIERTELFINALE);
                break;
            case 5:
                role = g.getRoleById(STATIC.ROLE_HALBFINALE);
                break;
            case 6:
                role = g.getRoleById(STATIC.ROLE_FINALE);
                break;
            default:
                throw new Exception("Interner Fehler: Runde ="+runde+ " in line 611");
        }
        return role;
    }
    public static void trylog(MessageReceivedEvent event,boolean haswon) {
        boolean found =false;
        for (int nid: nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (node.winner==null&&node.players.contains(event.getAuthor())&&node.players.size()==2) {
                User tosend;
                if (ConfirmReactListener.alreadyLogged(nid)) {
                    event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Für dieses Match wurde schon ein Ergebnis eingegeben!").queue();
                    return;
                }
                if (node.players.get(0).getId().equalsIgnoreCase(event.getAuthor().getId())) tosend=node.players.get(1); else tosend=node.players.get(0);
                if (haswon) {
                    sendConfirmationMessage(nid,tosend,event.getAuthor(),event.getTextChannel(),event.getMember());
                    event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Es wird noch auf die Bestätigung durch deinen Gegner gewartet, bevor das Ergebnis eingetragen wird!!").queue();
                } else {
                    sendConfirmationMessage(nid,tosend,tosend,event.getTextChannel(),event.getMember());
                    event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": Es wird noch auf die Bestätigung durch deinen Gegner gewartet, bevor das Ergebnis eingetragen wird!!").queue();
                }
                found = true;
            }
        }
        if (!found) {
            event.getTextChannel().sendMessage("Es wurde kein ausstehendes Match gefunden, für das du schon eine Bewertung abgeben könntest!").queue();
        }
    }
    private static void sendConfirmationMessage(int nid, User playerToNotify, User playerthathaswon,TextChannel tc,Member author) {
        String msgs = "Bitte bestätige das Ergebnis des Matches: "+(playerToNotify.getId().equalsIgnoreCase(playerthathaswon.getId())?"Du hast":playerthathaswon.getName()+"hat")+"gewonnen!";
        msgs += "\nBitte bestätige mit ✅ oder protestiere mit ❎!";
        try {
            Message msg =playerToNotify.openPrivateChannel().complete().sendMessage(msgs).complete();
            msg.addReaction("U+2705").queue();
            msg.addReaction("U+274E").queue();
            UnconfirmedResult ur = new UnconfirmedResult(nid,playerthathaswon,playerthathaswon.getJDA().getGuildById(STATIC.GUILDID));
            ConfirmReactListener.toConfirmResult.put(msg.getId(),ur);
            tc.sendMessage(author.getAsMention()+": Dein Gegner wurde benachrichtigt!").queue();

        } catch (Exception e) {
            //TextChannel tc= Objects.requireNonNull(playerthathaswon.getJDA().getGuildById(STATIC.GUILDID)).getTextChannelById(STATIC.CHANNEL_ALLGEMEIN);
            assert tc != null;
            tc.sendMessage(Objects.requireNonNull(tc.getGuild().getMember(playerToNotify)).getAsMention()+": Bitte öffne deine PN's. Nutze dann den Befehl `"+ commandListener.getPrefix(tc.getGuild())+"retry`, damit dir die Nachricht zugestellt wird! ").queue();
            String finalMsgs = msgs;
            retryOnDemand rod = u -> {
                try {
                    Message mes =u.openPrivateChannel().complete().sendMessage(finalMsgs).complete();
                    mes.addReaction("U+2705").queue();
                    mes.addReaction("U+274E").queue();
                    return true;
                } catch (Exception e1) {
                    return false;
                }
            };
            cmdRetry.retryLater.put(playerToNotify,rod);
        }
    }

    public static void revert(User u) throws Exception{
        for (int nid : nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (!node.players.contains(u)) continue;
            if (node.winner!=null) continue;
            if (node.getRunde()==1) throw new Exception("Dieser Spieler ist noch in der Vorrunde 1!");
            ArrayList<Integer> from = node.promoteFrom;
            TournamentNode a = nodes.get(from.get(0));
            TournamentNode b = nodes.get(from.get(1));
            if (a.players.contains(u)) {
                revert(u,a,node);
                return;
            }
            if (b.players.contains(u)) {
                revert(u,b,node);
                return;
            }
        }
        throw new Exception("Nimmt dieser Spieler überhaupt teil?");
    }
    private static void revert(User u, TournamentNode revto, TournamentNode revfrom) throws Exception {
        revto.winner =null;
        revfrom.players.remove(u);
        if (!revfrom.players.isEmpty()) {
            STATIC.trysend(revfrom.players.get(0),"Das Ergebnis des vorherigen Spieles deines Gegners wurde zurückgesetzt: Bitte warte, bis eine Entscheidung getroffen ist!");
        }
        revfrom.update();
        revto.update();
        Role role;
        switch (revfrom.getRunde()) {
            case 1:
                role = Objects.requireNonNull(u.getJDA().getGuildById(STATIC.GUILDID)).getRoleById(STATIC.ROLE_VORRUNDE1);
                break;
            case 2:
                role = Objects.requireNonNull(u.getJDA().getGuildById(STATIC.GUILDID)).getRoleById(STATIC.ROLE_VORRUNDE2);
                break;
            case 3:
                role = Objects.requireNonNull(u.getJDA().getGuildById(STATIC.GUILDID)).getRoleById(STATIC.ROLE_ACHTELFINALE);
                break;
            case 4:
                role = Objects.requireNonNull(u.getJDA().getGuildById(STATIC.GUILDID)).getRoleById(STATIC.ROLE_VIERTELFINALE);
                break;
            case 5:
                role = Objects.requireNonNull(u.getJDA().getGuildById(STATIC.GUILDID)).getRoleById(STATIC.ROLE_HALBFINALE);
                break;
            case 6:
                role = Objects.requireNonNull(u.getJDA().getGuildById(STATIC.GUILDID)).getRoleById(STATIC.ROLE_FINALE);
                break;
            default:
                throw new Exception("Interner Fehler: Runde ="+revfrom.getRunde()+ " in line 222");
        }
        assert role != null;
        Guild g = u.getJDA().getGuildById(STATIC.GUILDID);
        Objects.requireNonNull(g).removeRoleFromMember(Objects.requireNonNull(Objects.requireNonNull(g).getMember(u)), role).queue();
        Role ded = Objects.requireNonNull(u.getJDA().getGuildById(STATIC.GUILDID)).getRoleById(STATIC.ROLE_TOT);
        for (User us: revto.players) {
            if (us.getId().equalsIgnoreCase(u.getId())) continue;
            assert ded != null;
            g.removeRoleFromMember(Objects.requireNonNull(g.getMember(us)),ded).queue();
        }

    }

    static void save() {
        if (nodes.isEmpty()) return;
        ArrayList<String> out = new ArrayList<>();
        for (int i:nodes.keySet()) {
            out.add(save(i));
        }
        File f = new File("data/");
        if (!f.exists()) //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
        try {
            printOutTxtFile.Write("data/nodes.txt",out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static String save(int nid) {
        StringBuilder out = new StringBuilder();
        TournamentNode node = nodes.get(nid);
        out.append(node.NID).append(",");
        out.append(node.promoteToNID).append(",");
        if (node.winner==null) {
            out.append("0").append(",");
        } else {
            out.append(node.winner.getId()).append(",");
        }
        out.append(node.getRunde()).append(",");
        if(node.promoteFrom.size()>0) {
            if (node.promoteFrom.size()>1) {
                out.append(node.promoteFrom.get(0)).append(",");
                out.append(node.promoteFrom.get(1)).append(",");
            } else {
                out.append(node.promoteFrom.get(0)).append(",");
                out.append("0").append(",");
            }
        } else {
            out.append("0,0").append(",");
        }
        if(node.players.size()>0) {
            if (node.players.size()>1) {
                out.append(node.players.get(0).getId()).append(",");
                out.append(node.players.get(1).getId());
            } else {
                out.append(node.players.get(0).getId()).append(",");
                out.append("0");
            }


        } else {
            out.append("0,0");
        }
        out.append(",").append(node.getBracketNbr()).append(",");
        if(node.getBracketSub().isEmpty()) {
            out.append("0,0");
        } else {
            out.append(node.getBracketSub().get(0)).append(",").append(node.getBracketSub().get(1));
        }
        return out.toString();
    }

    /*
    Serialisation:
    nid, promtonid, winner(id,0 wenn nicht gesetzt),runde,promotefrom[0](0 wenn nicht gsetzt),promotefrom[1](0 wenn nicht gsetzt),player[0](id,0 wenn nicht gsetzt),player[1](id,0 wenn nicht gsetzt),bracketnode,bracketsub1,bracketsub2
    Seperator: ,

    */

    private static void load (String s, JDA jda) {
        String[] split = s.split(",");
        long[] sp = new long[11];
        if (split.length!=11) {
            System.out.println("split lenght wrong: expectet 8, got"+split.length);
            return;
        }
        try {
            for (int i=0;i<split.length;i++) {
                sp[i] = Long.parseLong(split[i]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        int nid = (int)sp[0];
        int promtoid = (int)sp[1];
        User winner = null;
        if (sp[2]!=0) {
            winner =jda.getUserById(sp[2]);
        }
        int runde = (int)sp[3];
        int promfroma = (int)sp[4];
        int promfromb = (int)sp[5];
        ArrayList<Integer> promfrom = new ArrayList<>();
        if (promfroma!=0) {
            promfrom.add(promfroma);
        }
        if (promfromb!=0) {
            promfrom.add(promfromb);
        }
        long plyra = sp[6];
        long plyrb = sp[7];
        ArrayList<User> plyr = new ArrayList<>();
        if (plyra!=0) {
            User usera = jda.getUserById(plyra);
            if (usera != null) plyr.add(usera); else plyr.add(jda.getSelfUser());
        }
        if (plyrb!=0) {
            User userb = jda.getUserById(plyrb);
            if (userb != null) plyr.add(userb); else plyr.add(jda.getSelfUser());
        }
        int brckt = (int)sp[8];
        int suba = (int)sp[9];
        int subb = (int)sp[10];
        ArrayList<Integer> subs = new ArrayList<>();
        if (suba!=0) {
            subs.add(suba);
            subs.add(subb);
        }
        TournamentNode tn = new TournamentNode(nid,promtoid,winner,runde,promfrom,plyr,brckt,subs);
        tn.update(false);
    }
    public static boolean loadNodes(JDA jda) {
        ArrayList<String> in;
        File f = new File("data/nodes.txt");
        if (!f.exists()) return false;
        try {
            in =readInTxtFile.Read("data/nodes.txt");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        for (String s:in) {
            load(s,jda);
        }

        return true;
    }
    public static TournamentNode getCurrentNode(User u) {
        for (int nid:nodes.keySet()) {
            TournamentNode tn = nodes.get(nid);
            if (tn.players.contains(u)&&tn.winner==null) return tn;
        }
        return null;
    }



}
