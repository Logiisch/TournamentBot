package helperCore;

import listeners.commandListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import util.STATIC;

import java.util.ArrayList;
import java.util.Objects;

public class UnconfirmedResult {

    private int nid;
    private User winner;
    private Guild g;

    UnconfirmedResult(int NID, User wnnr, Guild guild) {
        nid =  NID;
        winner=wnnr;
        g=guild;
    }
    public int getId() {
        return nid;
    }

    public void confirm() {

        try {
            Logic.logresult(winner,true,g);
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder();
            ArrayList<User> usrs = Logic.nodes.get(nid).players;
            for (User u: usrs) {
                try {
                    Member m = g.getMember(u);
                    assert m != null;
                    msg.append(m.getAsMention()).append(" ");

                } catch (Exception e2) {
                    msg.append(u.getName()).append(" ");
                }
            }
            try {
                Member m = g.getMemberById(STATIC.OWNERID);
                msg.append(Objects.requireNonNull(m).getAsMention()).append(" ");

            } catch (Exception e3) {
                msg.append(Objects.requireNonNull(g.getJDA().getUserById(STATIC.OWNERID)).getName()).append(" ");
                e.printStackTrace();
            }
            msg.append(": ").append(e.getMessage());
            Objects.requireNonNull(g.getTextChannelById(STATIC.getSettings(g, "CHANNEL_ALLGEMEIN"))).sendMessage(msg.toString()).queue();

        }

    }
    public void deny() {
        StringBuilder msg = new StringBuilder();
        ArrayList<User> usrs = Logic.nodes.get(nid).players;
        for (User u: usrs) {
            try {
                Member m = g.getMember(u);
                assert m != null;
                msg.append(m.getAsMention()).append(" ");

            } catch (Exception e) {
                msg.append(u.getName()).append(" ");
            }
        }
        try {
            Role r = g.getRoleById(STATIC.getSettings(g,"ROLE_HELPER"));
            assert r != null;
            msg.append(r.getAsMention()).append(" ");

        } catch (Exception e) {
            msg.append(Objects.requireNonNull(g.getJDA().getUserById(STATIC.OWNERID)).getName()).append(" ");
        }
        msg.append(": ").append(LangManager.get(g, "UCRDeny").replace("%PREFIX%", commandListener.getPrefix(g)));
        Objects.requireNonNull(g.getTextChannelById(STATIC.getSettings(g, "CHANNEL_ALLGEMEIN"))).sendMessage(msg.toString()).queue();
    }


}
