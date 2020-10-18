package helperCore;

import commands.cmdRetry;
import commands.cmdTeamMixup;
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

    private static HashMap<String,HashMap<Integer,TournamentNode>> allNodes = new HashMap<>();

    public static HashMap<Integer, TournamentNode> getNodes(Guild g) {
        return allNodes.getOrDefault(g.getId(),new HashMap<>());
    }
    public static void setNodes(Guild g, HashMap<Integer, TournamentNode> map) {
        allNodes.put(g.getId(),map);
    }

    public static String start(MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());


        List<Member> mbrs =event.getGuild().getMembers();



        ArrayList<User> teilnehmer = new ArrayList<>();
        if (!STATIC.GuildsWithTeamMode.contains(event.getGuild().getId())) {

            Role tt = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_TURNIERTEILNEHMER"));
            for (Member m : mbrs) {
                if (getNotIncluded().contains(m.getUser().getId())) continue;
                if (!m.getRoles().contains(tt)) continue;
                teilnehmer.add(m.getUser());


            }
        } else {
            ArrayList<ArrayList<String>> teams = cmdTeamMixup.guildTeams.get(event.getGuild().getId());
            for (ArrayList<String> part:teams) {
                teilnehmer.add(event.getJDA().getUserById(part.get(0)));
            }
        }
        StringBuilder out = new StringBuilder(LangManager.get(event.getGuild(),"LogicParticipants")+"\n");
        for (User u:teilnehmer) {
            if (u.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) continue;
            out.append(u.getName()).append("\n");
        }
        event.getTextChannel().sendMessage(out.toString()).queue();

        if (teilnehmer.size()>64) {
            return LangManager.get(event.getGuild(),"logicMax64".replace("%PREFIX%",prefix));
        }
        int playersFull = 64;
        if (teilnehmer.size()<33) playersFull= 32;
        if (teilnehmer.size()<17) playersFull= 16;
        if (teilnehmer.size()<9) playersFull= 8;
        if (teilnehmer.size()<5) playersFull= 4;
        while (teilnehmer.size()<playersFull) {
            teilnehmer.add(event.getJDA().getSelfUser());
        }

        TournamentNode finale = new TournamentNode(1,0,6,127,event.getGuild());
        int nid = 2;
        ArrayList<TournamentNode> temp = new ArrayList<>();
        temp.add(finale);

        int minrunde = 1;
        if (teilnehmer.size()<33) minrunde=2;
        if (teilnehmer.size()<17) minrunde=3;
        if (teilnehmer.size()<9) minrunde=4;
        if (teilnehmer.size()<5) minrunde=5;

        while (!temp.isEmpty()) {
            TournamentNode tn = temp.get(0);
            if (tn.getRunde()>minrunde) {
                TournamentNode a = new TournamentNode(nid++,tn.NID,tn.getRunde()-1,getSubs(tn.getBracketNbr()).get(0),event.getGuild());
                TournamentNode b = new TournamentNode(nid++,tn.NID,tn.getRunde()-1,getSubs(tn.getBracketNbr()).get(1),event.getGuild());

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

        Collections.shuffle(teilnehmer);
        HashMap<Integer,TournamentNode> nodes = getNodes(event.getGuild());
        for (int i:nodes.keySet()) {
            TournamentNode node = nodes.get(i);
            if (node.getRunde()==minrunde) {
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
                Role vr1 = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_VORRUNDE1"));
                if (minrunde==2) vr1 = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_VORRUNDE2"));
                if (minrunde==3) vr1 = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_ACHTELFINALE"));
                if (minrunde==4) vr1 = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_VIERTELFINALE"));
                if (minrunde==5) vr1 = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_HALBFINALE"));
                assert vr1 != null;
                event.getGuild().addRoleToMember(mema,vr1).queue();
                event.getGuild().addRoleToMember(memb,vr1).queue();

            }
            node.update();
        }
        setNodes(event.getGuild(),nodes);
        refreshTournamnet(event,false);

        if (STATIC.dryRun) {
            return LangManager.get(event.getGuild(),"LogicTLS");
        }
        return LangManager.get(event.getGuild(),"LogicTS");
    }

    private static void refreshTournamnet(MessageReceivedEvent event, boolean exitOnHalfway) {
        boolean changedsomething = false;
        HashMap<Integer,TournamentNode> nodes = getNodes(event.getGuild());
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
        setNodes(event.getGuild(),nodes);
        if (changedsomething) refreshTournamnet(event,true);
        if(exitOnHalfway) return;
        String prefix = commandListener.getPrefix(event.getGuild());
        for (int nid: nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (node.players.size()<2||node.winner!=null) continue;
            User a = node.players.get(0);
            User b = node.players.get(1);
            String senda = LangManager.get(event.getGuild(),"LogicEnemyNow").replace("%NAME%",b.getName()).replace("%PREFIX%",prefix);
            String sendb = LangManager.get(event.getGuild(),"LogicEnemyNow").replace("%NAME%",a.getName()).replace("%PREFIX%",prefix);
            Message msga=trysend(a,senda,event.getGuild());
            Message msgb=trysend(b,sendb,event.getGuild());
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
        HashMap<Integer,TournamentNode> nodes = getNodes(g);
        for(int i:nodes.keySet()) {
            TournamentNode node = nodes.get(i);
            if (node.winner==null&&node.players.contains(u)) tn = node;
        }
        if (tn==null) {
            throw new Exception(LangManager.get(g,"LogicNoOpenGame"));
        }
         if(tn.players.size()<2) {
             throw new Exception(LangManager.get(g,"LogicNoEnemyYet").replace("%ID%",tn.NID+""));
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
            throw new Exception("Internal Error:winner in line 158 still null");
        }
        tn.winner=winner;
        tn.update();
        if(tn.getRunde()==6) {
            Objects.requireNonNull(g.getTextChannelById(STATIC.getSettings(g,"CHANNEL_ALLGEMEIN"))).sendMessage(LangManager.get(g,"LogicEndGeneral").replace("%NAME%",winner.getName())).queue();
            g.addRoleToMember(Objects.requireNonNull(g.getMember(u)), Objects.requireNonNull(g.getRoleById(STATIC.getSettings(g,"ROLE_WINNER")))).queue();
            EmbedBuilder eb = new EmbedBuilder().setColor(roleOfRound(6,g).getColor()).setTitle(LangManager.get(g,"LogicFinalRes"));
            eb.setDescription(LangManager.get(g,"LogicEndResults").replace("%WINNER%",winner.getName()).replace("%LB%","\n")+(tn.players.get(0).getId().equalsIgnoreCase(winner.getId())?tn.players.get(1).getName():tn.players.get(0).getName()));
            Objects.requireNonNull(g.getTextChannelById(STATIC.getSettings(g,"CHANNEL_RESULTS"))).sendMessage(eb.build()).queue();
            setNodes(g,nodes);
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

            String senda = LangManager.get(g,"LogicEnemyNow").replace("%NAME%",plb.getName()).replace("%PREFIX%",prefix);
            String sendb = LangManager.get(g,"LogicEnemyNow").replace("%NAME%",pla.getName()).replace("%PREFIX%",prefix);
            Message msga=trysend(pla,senda,g);
            Message msgb=trysend(plb,sendb,g);
            assert msga!=null;
            assert msgb!=null;
            msga.addReaction("U+1F44D").queue();
            msgb.addReaction("U+1F44D").queue();
            RoundTime rt = new RoundTime(promto.NID,15,g);
            ConfirmReactListener.rtimes.put(msga.getId(),rt);
            ConfirmReactListener.rtimes.put(msgb.getId(),rt);
        } else {
            trysend(winner,LangManager.get(g,"LogicEnemyNotReadyYet"),g);
            //return;
        }
        refreshRoles(g.getMember(winner),false,promto.getRunde());
        User looser = null;
        for (User us:tn.players) {
            if (us.getId().equalsIgnoreCase(winner.getId())) continue;
            looser= us;
        }
        if (looser==null) {
            throw new Exception("Internal Error:looser in line 191 still null");
        }
        if(winner.getId().equalsIgnoreCase(looser.getId())) {
            throw new Exception("Internal Error:looser.id == winner.id");
        }


        refreshRoles(g.getMember(looser),true,tn.getRunde());
        tn.update();
        promto.update();
        TextChannel tc=g.getTextChannelById(STATIC.getSettings(g,"CHANNEL_RESULTS"));
        EmbedBuilder eb = new EmbedBuilder().setTitle(LangManager.get(g,"LogicMatchResult")).setAuthor(getRoundname(tn.getRunde(),g));

        eb.setDescription(LangManager.get(g,"LogicResult").replace("%PLAYERA%",tn.players.get(0).getName()).replace("%PLAYERB%",tn.players.get(1).getName()).replace("%WINNER%",tn.winner.getName()).replace("%ROUND%",getRoundname(promto.getRunde(),g)));

        if (tn.players.contains(g.getSelfMember().getUser())) {
            User named;
            if (tn.players.get(0).getId().equalsIgnoreCase(g.getJDA().getSelfUser().getId())) {
                named = tn.players.get(1);
            } else {
                named = tn.players.get(0);
            }
            eb.setDescription(LangManager.get(g,"LogicDirectPromote").replace("%NAME%",named.getName()).replace("%ROUND%",getRoundname(promto.getRunde(),g)));
        }
        Role role = roleOfRound(tn.getRunde(),g);

        assert role != null;
        eb.setColor(role.getColor());
        assert tc != null;
        tc.sendMessage(eb.build()).queue();
        setNodes(g,nodes);
    }
    private static void refreshRoles(Member m, boolean isdead, int round) throws Exception {
        if (m==null){
            throw new Exception("Internal Error:m is null in Line 540");
        }
        Role role = roleOfRound(round,m.getGuild());

        assert role != null;
        m.getGuild().addRoleToMember(m,role).queue();
        if (isdead) {
            Role dead = m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_TOT"));
            assert dead != null;
            m.getGuild().addRoleToMember(m,dead).queue();
        }

    }
    public static Role roleOfRound(int runde,Guild g) throws Exception {
        Role role;
        switch (runde) {
            case 1:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_VORRUNDE1"));
                break;
            case 2:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_VORRUNDE2"));
                break;
            case 3:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_ACHTELFINALE"));
                break;
            case 4:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_VIERTELFINALE"));
                break;
            case 5:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_HALBFINALE"));
                break;
            case 6:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_FINALE"));
                break;
            default:
                throw new Exception("Internal Error: Runde ="+runde+ " in line 575");
        }
        return role;
    }
    public static void trylog(MessageReceivedEvent event,boolean haswon) {
        boolean found =false;
        HashMap<Integer,TournamentNode> nodes = getNodes(event.getGuild());
        for (int nid: nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (node.winner==null&&node.players.contains(event.getAuthor())&&node.players.size()==2) {
                User tosend;
                if (ConfirmReactListener.alreadyLogged(nid)) {
                    event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": "+LangManager.get(event.getGuild(),"LogicResultAlready")).queue();
                    return;
                }
                if (node.players.get(0).getId().equalsIgnoreCase(event.getAuthor().getId())) tosend=node.players.get(1); else tosend=node.players.get(0);
                if (haswon) {
                    sendConfirmationMessage(nid,tosend,event.getAuthor(),event.getTextChannel(),event.getMember());
                } else {
                    sendConfirmationMessage(nid,tosend,tosend,event.getTextChannel(),event.getMember());
                }
                event.getTextChannel().sendMessage(Objects.requireNonNull(event.getMember()).getAsMention()+": "+LangManager.get(event.getGuild(),"LogicWaitingForEnemyResponse")).queue();
                found = true;
            }
        }
        if (!found) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"LogicNoCurrentMatch")).queue();
        }

    }
    private static void sendConfirmationMessage(int nid, User playerToNotify, User playerthathaswon,TextChannel tc,Member author) {
        HashMap<Integer,TournamentNode> nodes = getNodes(tc.getGuild());
        TournamentNode tourni = nodes.get(nid);
        String enemy = tourni.players.get(0).getId().equalsIgnoreCase(playerToNotify.getId())?tourni.players.get(1).getName():tourni.players.get(0).getName();
        String msgs = LangManager.get(tc.getGuild(),(playerthathaswon.getId().equalsIgnoreCase(playerToNotify.getId())?"LogicNotifyYouHaveWon":"LogicNotifyEnemyHasWon").replace("%ENEMY%",enemy));
        msgs += "\n"+LangManager.get(tc.getGuild(),"LogicPleaseVerify");
        try {
            Message msg =playerToNotify.openPrivateChannel().complete().sendMessage(msgs).complete();
            msg.addReaction("U+2705").queue();
            msg.addReaction("U+274E").queue();
            UnconfirmedResult ur = new UnconfirmedResult(nid,playerthathaswon,tc.getGuild());
            ConfirmReactListener.toConfirmResult.put(msg.getId(),ur);
            tc.sendMessage(author.getAsMention()+":"+LangManager.get(tc.getGuild(),"LogicEnemyWasNotified")).queue();

        } catch (Exception e) {
            //TextChannel tc= Objects.requireNonNull(playerthathaswon.getJDA().getGuildById(STATIC.GUILDID)).getTextChannelById(STATIC.CHANNEL_ALLGEMEIN);
            tc.sendMessage(Objects.requireNonNull(tc.getGuild().getMember(playerToNotify)).getAsMention()+": "+LangManager.get(tc.getGuild(),"LogicPleaseOpenPNs").replace("%PREFIX%",commandListener.getPrefix(tc.getGuild()))).queue();
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
        setNodes(tc.getGuild(),nodes);
    }

    public static void revert(User u,Guild g) throws Exception{
        HashMap<Integer,TournamentNode> nodes = getNodes(g);
        for (int nid : nodes.keySet()) {
            TournamentNode node = nodes.get(nid);
            if (!node.players.contains(u)) continue;
            if (node.winner!=null) continue;
            if (node.getRunde()==1) throw new Exception(LangManager.get(g,"LogicPlayerStillPre1"));
            ArrayList<Integer> from = node.promoteFrom;
            TournamentNode a = nodes.get(from.get(0));
            TournamentNode b = nodes.get(from.get(1));
            if (a.players.contains(u)) {
                revert(u,a,node,g);
                return;
            }
            if (b.players.contains(u)) {
                revert(u,b,node,g);
                return;
            }
        }
        throw new Exception(LangManager.get(g,"LogicIsntParticipant"));
    }
    private static void revert(User u, TournamentNode revto, TournamentNode revfrom,Guild g) throws Exception {
        HashMap<Integer,TournamentNode> nodes = getNodes(g);
        revto.winner =null;
        revfrom.players.remove(u);
        if (!revfrom.players.isEmpty()) {
            trysend(revfrom.players.get(0),LangManager.get(g,"LogicRevertedEnemy"),g);
        }
        revfrom.update();
        revto.update();
        Role role;
        switch (revfrom.getRunde()) {
            case 1:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_VORRUNDE1"));
                break;
            case 2:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_VORRUNDE2"));
                break;
            case 3:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_ACHTELFINALE"));
                break;
            case 4:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_VIERTELFINALE"));
                break;
            case 5:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_HALBFINALE"));
                break;
            case 6:
                role = g.getRoleById(STATIC.getSettings(g,"ROLE_FINALE"));
                break;
            default:
                throw new Exception("Internal Error: Runde ="+revfrom.getRunde()+ " in line 222");
        }
        assert role != null;
        Objects.requireNonNull(g).removeRoleFromMember(Objects.requireNonNull(Objects.requireNonNull(g).getMember(u)), role).queue();
        Role ded = g.getRoleById(STATIC.getSettings(g.getId(),"ROLE_TOT"));
        for (User us: revto.players) {
            if (us.getId().equalsIgnoreCase(u.getId())) continue;
            assert ded != null;
            g.removeRoleFromMember(Objects.requireNonNull(g.getMember(us)),ded).queue();
        }

    }

    static void save(Guild g) {
        HashMap<Integer,TournamentNode> nodes = getNodes(g);
        if (nodes.isEmpty()) return;
        ArrayList<String> out = new ArrayList<>();
        for (int i:nodes.keySet()) {
            out.add(save(i,g));
        }
        File f = new File("data/guilds/"+g.getId()+"/");
        if (!f.exists()) //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
        try {
            printOutTxtFile.Write("data/"+g.getId()+"/nodes.txt",out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static String save(int nid,Guild g) {
        HashMap<Integer,TournamentNode> nodes = getNodes(g);
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
    nid, promtonid, winner(id,0 wenn nicht gesetzt),runde,promotefrom[0](0 wenn nicht gsetzt),promotefrom[1](0 wenn nicht gsetzt),player[0](id,0 wenn nicht gsetzt),player[1](id,0 wenn nicht gsetzt),bracketnode,bracketsub1,bracketsub2,guildid
    Seperator: ,

    */

    private static boolean load (String s, JDA jda,String gid) {
        String[] split = s.split(",");
        long[] sp = new long[11];
        if (split.length!=11) {
            System.out.println("split lenght wrong: expectet 8, got"+split.length);
            return false;
        }
        try {
            for (int i=0;i<split.length;i++) {
                sp[i] = Long.parseLong(split[i]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
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
        Guild g = jda.getGuildById(gid);
        if (g==null) return false;
        TournamentNode tn = new TournamentNode(nid,promtoid,winner,runde,promfrom,plyr,brckt,subs,g);
        tn.update(false);
        return true;
    }
    public static boolean loadNodes(JDA jda) {
        ArrayList<String> in;
        File f = new File("data/guilds/");
        if (!f.exists()) return false;
        File[] subs = f.listFiles();
        File fe;
        for (File fl:subs) {
            fe = new File(fl.getAbsolutePath()+"/nodes.txt");
            if (!fe.exists()) continue;
            try {
                in =readInTxtFile.Read(fe.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            for (String s:in) {
                load(s,jda,fl.getName());
            }
        }


        return true;
    }
    public static TournamentNode getCurrentNode(User u,Guild g) {
        HashMap<Integer, TournamentNode> nodes = getNodes(g);
        for (int nid:nodes.keySet()) {
            TournamentNode tn = nodes.get(nid);
            if (tn.players.contains(u)&&tn.winner==null) return tn;
        }
        return null;
    }

    public static void kickUser(User u,Guild g) {
        HashMap<Integer, TournamentNode> nodes = getNodes(g);
        notincluded.add(u.getId());
        if (!nodes.isEmpty()) {
            for (int nid:nodes.keySet()) {
                TournamentNode tn = nodes.get(nid);

                if (tn.players.contains(u)) {
                    tn.players.remove(u);
                    u.openPrivateChannel().complete().sendMessage(LangManager.get(g,"LogicYouGotRemoved")).queue();
                    User other = null;
                    if (!tn.players.isEmpty()) {
                        other = tn.players.get(0);
                    }
                    tn.players.add(u.getJDA().getSelfUser());
                    if (tn.players.size()==2&&tn.winner==null) try {
                        Logic.logresult(u.getJDA().getSelfUser(),false,g);
                    } catch (Exception e) {
                        if (other!=null) other.openPrivateChannel().complete().sendMessage(LangManager.get(g,"LogicRoundUp")+"\n"+e.getMessage()).queue();
                        e.printStackTrace();
                    }
                }
            }
        }
        setNodes(g,nodes);
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

    public static ArrayList<String> notincluded = new ArrayList<>();

    public static ArrayList<String> getNotIncluded() {
        return notincluded;
    }



    public static Message trysend (User u, String msg,Guild g) {
        String SELFID = "705567211380801598";
        if (u.getId().equalsIgnoreCase(SELFID)||u.getJDA().getSelfUser().getId().equalsIgnoreCase(u.getId())) return null;
        try {
            return u.openPrivateChannel().complete().sendMessage(msg).complete();
        } catch (Exception e) {

            if (g.isMember(u)) {
                return Objects.requireNonNull(u.getJDA().getTextChannelById(STATIC.getSettings(g,"CHANNEL_ALLGEMEIN"))).sendMessage(Objects.requireNonNull(g.getMember(u)).getAsMention()+":"+msg+"\nFür das Turnier öffne bite deine Privatnachrichten, da nicht alle Nachrichten über diesen Channel gesendet werden können!").complete();} else {return null;}
        }
    }
    public static String getRoundname(int runde,Guild g) {
        switch (runde) {
            case 1:
                return LangManager.get(g,"RoundNameVR1");
            case 2:
                return LangManager.get(g,"RoundNameVR2");
            case 3:
                return LangManager.get(g,"RoundNameAF");
            case 4:
                return LangManager.get(g,"RoundNameVF");
            case 5:
                return LangManager.get(g,"RoundNameHF");
            case 6:
                return LangManager.get(g,"RoundNameF");
            default:
                return "undefined";
        }
    }

    public static void saveNotIncluded() {
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
