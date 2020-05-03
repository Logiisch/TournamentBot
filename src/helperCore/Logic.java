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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Logic {

    public static HashMap<Integer,TournamentNode> nodes = new HashMap<>();

    public static String start(MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        nodes.clear();

        List<Member> mbrs =event.getGuild().getMembers();


        TournamentNode finale = new TournamentNode(1,0,6);
        int nid = 2;
        ArrayList<TournamentNode> temp = new ArrayList<>();
        temp.add(finale);

        while (!temp.isEmpty()) {
            TournamentNode tn = temp.get(0);
            if (tn.getRunde()>1) {
                TournamentNode a = new TournamentNode(nid++,tn.NID,tn.getRunde()-1);
                TournamentNode b = new TournamentNode(nid++,tn.NID,tn.getRunde()-1);

                tn.promoteFrom.add(a.NID);
                tn.promoteFrom.add(b.NID);
                temp.add(a);
                temp.add(b);
            }
            temp.remove(0);
            tn.update();
        }

        ArrayList<User> teilnehmer = new ArrayList<>();

        for (Member m:mbrs) {

            if (!STATIC.getNotIncluded().contains(m.getUser().getId())&&!m.getUser().isBot()) teilnehmer.add(m.getUser());

        }

        if (teilnehmer.size()>64) {
            return "Es können maximal 64 Leute teilnehmen, bitte enferne Spieler mit `"+prefix+"kick [Anzahl/User als @Mention]`!";
        }
        while (teilnehmer.size()<64) {
            teilnehmer.add(event.getJDA().getSelfUser());
        }
        ;
        for (int i:nodes.keySet()) {
            TournamentNode node = nodes.get(i);
            if (node.getRunde()==1) {
                int tnnbr =(int)Math.round(Math.random()*teilnehmer.size());
                if (tnnbr==teilnehmer.size()) tnnbr--;
                User a = teilnehmer.get(tnnbr);
                teilnehmer.remove(tnnbr);

                 tnnbr =(int)Math.round(Math.random()*teilnehmer.size());
                if (tnnbr==teilnehmer.size()) tnnbr--;
                User b = teilnehmer.get(tnnbr);
                teilnehmer.remove(tnnbr);

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

    public static void refreshTournamnet(MessageReceivedEvent event, boolean exitOnHalfway) {
        boolean changedsomething = false;
        for (int nid: nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (node.players.size()<2) continue;;
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
            String senda = "Dein Gegner ist nun "+b.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit ✅, dass du anwesend bist. Du hast dafür 15min Zeit!";
            String sendb = "Dein Gegner ist nun "+a.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit ✅, dass du anwesend bist. Du hast dafür 15min Zeit!";
            Message msga=STATIC.trysend(a,senda);
            Message msgb=STATIC.trysend(b,sendb);
            assert msga!=null;
            assert msgb!=null;
            msga.addReaction("U+2705").queue();
            msgb.addReaction("U+2705").queue();
            RoundTime rt = new RoundTime(node.NID,15,event.getGuild());
            ConfirmReactListener.rtimes.put(msga.getId(),rt);
            ConfirmReactListener.rtimes.put(msgb.getId(),rt);

        }


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
             throw new Exception("Du kannst noch kein Ergebnis eintragen, da du noch kein gegner zugewiesen wurde");
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
        if(tn.getRunde()==6) {
            Objects.requireNonNull(g.getTextChannelById(STATIC.CHANNEL_ALLGEMEIN)).sendMessage("Das Turnier ist beendet, der Gewinnner steht fest: "+u.getName()+" hat gewonnen! Glückwunsch!").queue();
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


            String senda = "Dein Gegner ist nun "+plb.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit ✅, dass du anwesend bist. Du hast dafür 15min Zeit!";
            String sendb = "Dein Gegner ist nun "+pla.getName()+"! Bitte verständigt euch selbstständig, wann ihr das Spiel spielt. Wenn ihr fertig seid, gib bitte `"+prefix+"res [win/loose]` ein, abhängig davon, ob du gewonnen oder verloren hast! Bitte bestätige mit ✅, dass du anwesend bist. Du hast dafür 15min Zeit!";
            Message msga=STATIC.trysend(pla,senda);
            Message msgb=STATIC.trysend(plb,sendb);
            assert msga!=null;
            assert msgb!=null;
            msga.addReaction("U+2705").queue();
            msgb.addReaction("U+2705").queue();
            RoundTime rt = new RoundTime(promto.NID,15,g);
            ConfirmReactListener.rtimes.put(msga.getId(),rt);
            ConfirmReactListener.rtimes.put(msgb.getId(),rt);
        } else {
            STATIC.trysend(winner,"Dein Gegner ist noch nicht fertig. Warte noch einen Moment.");
        }
        refreshRoles(g.getMember(winner),false,promto.getRunde());
        User looser = null;
        for (User us:tn.players) {
            if (us.getId().equalsIgnoreCase(u.getId())) continue;
            looser= us;
        }

        if (looser==null) {
            throw new Exception("Interner Fehler:looser in line 191 still null");
        }
        refreshRoles(g.getMember(looser),true,tn.getRunde());
        tn.update();
        promto.update();
        TextChannel tc=g.getTextChannelById(STATIC.CHANNEL_RESULTS);
        EmbedBuilder eb = new EmbedBuilder().setColor(Color.green).setTitle("Matchergebnisse").setAuthor(STATIC.getRoundname(tn.getRunde()));
        if (tn.players.contains(g.getSelfMember().getUser())) {
            User named = null;
            if (tn.players.get(0).getId().equalsIgnoreCase(g.getJDA().getSelfUser().getId())) {
                named = tn.players.get(1);
            } else {
                named = tn.players.get(0);
            }
            eb.setDescription("Da das Turnier nicht voll war, konnte "+named.getName()+" direkt in die Runde \""+STATIC.getRoundname(promto.getRunde())+"\" aufsteigen!");
        } else {
            eb.setDescription("Das Match zwischen " + tn.players.get(0).getName() + " und " + tn.players.get(1).getName() + " wurde ausgetragen. Der Gewinner ist " + tn.winner.getName() + " ! Dieser Spieler ist nun i" + ((promto.getRunde() < 3) ? "n " : "m ") + STATIC.getRoundname(promto.getRunde()) + "!");
        }
        assert tc != null;
        tc.sendMessage(eb.build()).queue();
    }
    public static void refreshRoles(Member m, boolean isdead, int round) throws Exception {
        if (m==null){
            throw new Exception("Interner Fehler:m is null in Line 199");
        }
        Role role = null;
        switch (round) {
            case 1:
                role = m.getGuild().getRoleById(STATIC.ROLE_VORRUNDE1);
                break;
            case 2:
                role = m.getGuild().getRoleById(STATIC.ROLE_VORRUNDE2);
                break;
            case 3:
                role = m.getGuild().getRoleById(STATIC.ROLE_ACHTELFINALE);
                break;
            case 4:
                role = m.getGuild().getRoleById(STATIC.ROLE_VIERTELFINALE);
                break;
            case 5:
                role = m.getGuild().getRoleById(STATIC.ROLE_HALBFINALE);
                break;
            case 6:
                role = m.getGuild().getRoleById(STATIC.ROLE_FINALE);
                break;
                default:
                    throw new Exception("Interner Fehler: Runde ="+round+ " in line 222");
        }
        assert role != null;
        m.getGuild().addRoleToMember(m,role).queue();
        if (isdead) {
            Role dead = m.getGuild().getRoleById(STATIC.ROLE_TOT);
            assert dead != null;
            m.getGuild().addRoleToMember(m,dead).queue();
        }

    }
    public static void trylog(MessageReceivedEvent event,boolean haswon) {
        boolean found =false;
        for (int nid: nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (node.winner==null&&node.players.contains(event.getAuthor())&&node.players.size()==2) {
                User tosend = null;
                if (ConfirmReactListener.alreadyLogged(nid)) {
                    event.getTextChannel().sendMessage(event.getMember().getAsMention()+": Für dieses Match wurde schon ein Ergebnis eingegeben!").queue();
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
            UnconfirmedResult ur = new UnconfirmedResult(nid,playerthathaswon,playerthathaswon.getJDA().getGuildById(STATIC.GUILDID),playerToNotify);
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
    public static void revert(User u, TournamentNode revto,TournamentNode revfrom) throws Exception {
        revto.winner =null;
        revfrom.players.remove(u);
        if (!revfrom.players.isEmpty()) {
            STATIC.trysend(revfrom.players.get(0),"Das Ergebnis des vorherigen Spieles deines Gegners wurde zurückgesetzt: Bitte warte, bis eine Entscheidung getroffen ist!");
        }
        revfrom.update();
        revto.update();
        Role role = null;
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

    public static void save() {
        if (nodes.isEmpty()) return;
        ArrayList<String> out = new ArrayList<>();
        for (int i:nodes.keySet()) {
            out.add(save(i));
        }
        File f = new File("data/");
        if (!f.exists()) f.mkdirs();
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
        out.append(node.getRunde()+"").append(",");
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

        return out.toString();
    }

    /*
    Serialisation:
    nid, promtonid, winner(id,0 wenn nicht gesetzt),runde,promotefrom[0](0 wenn nicht gsetzt),promotefrom[1](0 wenn nicht gsetzt),player[0](id,0 wenn nicht gsetzt),player[1](id,0 wenn nicht gsetzt)
    Seperator: ,

    */

    private static void load (String s, JDA jda) {
        String[] split = s.split(",");
        long[] sp = new long[8];
        if (split.length!=8) {
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
            plyr.add(jda.getUserById(plyra));
        }
        if (plyrb!=0) {
            plyr.add(jda.getUserById(plyrb));
        }
        TournamentNode tn = new TournamentNode(nid,promtoid,winner,runde,promfrom,plyr);
        tn.update(false);
    }
    public static boolean loadNodes(JDA jda) {
        ArrayList<String> in = new ArrayList<>();
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
