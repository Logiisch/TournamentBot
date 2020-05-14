package helperCore;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import util.STATIC;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class RoundTime  implements TimeKeeper{
    private OffsetDateTime end;
    private int nid;
    private ArrayList<User> hasReacted= new ArrayList<>();
    private Guild guild;

    RoundTime(int nID, int durInMin, Guild g) {
        nid = nID;
        end = OffsetDateTime.now().plusMinutes(durInMin);
        guild=g;
    }

    public Guild getGuild() {
        return guild;
    }

    @Override
    public OffsetDateTime fireOn() {
        return end;
    }

    @Override
    public boolean OnTimeRunOut() {
        if (hasReacted.size()==2) return true;
        TournamentNode tn = Logic.getNodes(guild).get(nid);
        if (tn.winner!=null) return true;
        if (hasReacted.size()==0) {
            for (User u:tn.players) {
                Logic.trysend(u,LangManager.get(guild,"RoundTimeNobody"),guild);
            }
            end =end.plusMinutes(15);
            Objects.requireNonNull(guild.getTextChannelById(STATIC.getSettings(guild,"CHANNEL_ALLGEMEIN"))).sendMessage(LangManager.get(guild,"RoundTimeWarning").replace("%USERA%",tn.players.get(0).getName()).replace("%USERB%",tn.players.get(1).getName())).queue();
            return true;
        }
        if(hasReacted.size()==1) {
            User hR = hasReacted.get(0);
            User missingReaction;
            if (tn.players.get(0).getId().equalsIgnoreCase(hR.getId())) missingReaction=tn.players.get(1); else missingReaction=tn.players.get(0);
            hR.openPrivateChannel().complete().sendMessage(LangManager.get(guild,"RoundTimeWon")).queue();
            try {
                Logic.logresult(hR,true,guild);
            } catch (Exception e) {
                hR.openPrivateChannel().complete().sendMessage(LangManager.get(guild,"RoundTimeError").replace("%MSG",e.getMessage()).replace("%BR%","\n").replace("%EMAIL%",STATIC.EMAIL)).queue();
                e.printStackTrace();
            }
            missingReaction.openPrivateChannel().complete().sendMessage(LangManager.get(guild,"RoundTimeLost")).queue();
            return true;
        }
        return true;
    }
    public void userreact(User u) {
        if(!hasReacted.contains(u))
        hasReacted.add(u);
    }
    public int getNid() {
        return nid;
    }

}
