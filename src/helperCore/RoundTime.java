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

    @Override
    public OffsetDateTime fireOn() {
        return end;
    }

    @Override
    public boolean OnTimeRunOut() {
        if (hasReacted.size()==2) return true;
        TournamentNode tn = Logic.nodes.get(nid);
        if (tn.winner!=null) return true;
        if (hasReacted.size()==0) {
            for (User u:tn.players) {
                Logic.trysend(u,"Da sich keiner innerhalb des zeitlimits gemeldet hat, wurde die Zeit um 15min verlängert!",guild);
            }
            end =end.plusMinutes(15);
            Objects.requireNonNull(guild.getTextChannelById(STATIC.getSettings(guild,"CHANNEL_ALLGEMEIN"))).sendMessage("Sowohl Nutzer "+tn.players.get(0).getName()+" als auch Nutzer "+tn.players.get(1).getName()+" sind nicht aktiv. Es wird empfohlen, sie zu kicken!").queue();
            return true;
        }
        if(hasReacted.size()==1) {
            User hR = hasReacted.get(0);
            User missingReaction;
            if (tn.players.get(0).getId().equalsIgnoreCase(hR.getId())) missingReaction=tn.players.get(1); else missingReaction=tn.players.get(0);
            hR.openPrivateChannel().complete().sendMessage("Da die Zeit deines Gegners abgelaufen ist, hast du automatisch gewonnen!").queue();
            try {
                Logic.logresult(hR,true,guild);
            } catch (Exception e) {
                hR.openPrivateChannel().complete().sendMessage("anscheinend ist beim Eintragen ein fehler aufgetreten: "+e.getMessage()+"\nBitte informiere Logii!").queue();
                e.printStackTrace();
            }
            missingReaction.openPrivateChannel().complete().sendMessage("Deine Zeit ist abgelaufen, du hast damit automatisch verloren!!").queue();
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
