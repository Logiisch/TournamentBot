package helperCore;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import util.STATIC;

import java.util.ArrayList;
import java.util.Objects;

public class UnconfirmedResult {

    int nid;
    User winner;
    Guild g;
    User hasToReact;
    public UnconfirmedResult(int NID, User wnnr,Guild guild,User htr) {
        nid =  NID;
        winner=wnnr;
        g=guild;
        hasToReact =htr;
    }
    public int getId() {
        return nid;
    }

    public void confirm() {

        try {
            Logic.logresult(winner,true,g);
        } catch (Exception e) {
            String msg = "";
            ArrayList<User> usrs = Logic.nodes.get(nid).players;
            for (User u: usrs) {
                try {
                    Member m = g.getMember(u);
                    msg+=m.getAsMention()+" ";

                } catch (Exception e2) {
                    msg += u.getName()+" ";
                }
            }
            try {
                Member m = g.getMemberById(STATIC.OWNERID);
                msg+=m.getAsMention()+" ";

            } catch (Exception e3) {
                msg += Objects.requireNonNull(g.getJDA().getUserById(STATIC.OWNERID)).getName()+" ";
            }
            msg +=": "+e.getMessage();
            Objects.requireNonNull(g.getTextChannelById(STATIC.CHANNEL_ALLGEMEIN)).sendMessage(msg).queue();
            e.printStackTrace();
        }

    }
    public void deny() {
        String msg = "";
        ArrayList<User> usrs = Logic.nodes.get(nid).players;
        for (User u: usrs) {
            try {
                Member m = g.getMember(u);
                msg+=m.getAsMention()+" ";

            } catch (Exception e) {
                msg += u.getName()+" ";
            }
        }
        try {
            Role r = g.getRoleById(STATIC.ROLE_HELPER);
            msg+=r.getAsMention()+" ";

        } catch (Exception e) {
            msg += Objects.requireNonNull(g.getJDA().getUserById(STATIC.OWNERID)).getName()+" ";
        }
        msg +=": Es gibt Probleme bei der Abstimmung. Bitte schlichten! Dann mit `t!set [User als @Erwähnung] [winner/looser] korrigieren!";
        Objects.requireNonNull(g.getTextChannelById(STATIC.CHANNEL_ALLGEMEIN)).sendMessage(msg).queue();
    }


}
