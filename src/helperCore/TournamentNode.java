package helperCore;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;

public class TournamentNode {

    int NID;
    int promoteToNID;
    public User winner=null;
    private int runde;
    ArrayList<Integer> promoteFrom = new ArrayList<>();
    public ArrayList<User> players = new ArrayList<>();

    TournamentNode(int nid, int ptnid, int rnd) {
        NID = nid;
        promoteToNID = ptnid;
        runde = rnd;
    }
    TournamentNode(int nid, int promotetonid, User wnner, int rnde, ArrayList<Integer> promFrom, ArrayList<User> plyrs) {
        NID = nid;
        promoteToNID= promotetonid;
        winner =wnner;
        promoteFrom= promFrom;
        players = plyrs;
        runde= rnde;
    }
    void update() {
        update(true);
    }
    void update(boolean save) {
        Logic.nodes.put(NID,this);
        if(save) Logic.save();
    }

    public int getRunde() {
        return runde;
    }
}
