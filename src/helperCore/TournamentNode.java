package helperCore;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;

public class TournamentNode {

    public int NID;
    public int promoteToNID;
    public User winner=null;
    private int runde;
    public ArrayList<Integer> promoteFrom = new ArrayList<>();
    public ArrayList<User> players = new ArrayList<>();

    public TournamentNode (int nid, int ptnid,int rnd) {
        NID = nid;
        promoteToNID = ptnid;
        runde = rnd;
    }
    public TournamentNode (int nid, int promotetonid,User wnner, int rnde, ArrayList<Integer> promFrom, ArrayList<User> plyrs) {
        NID = nid;
        promoteToNID= promotetonid;
        winner =wnner;
        promoteFrom= promFrom;
        players = plyrs;
        runde= rnde;
    }
    public void update() {
        update(true);
    }
    public void update(boolean save ) {
        Logic.nodes.put(NID,this);
        if(save) Logic.save();
    }
    public void updateRunde(int lvl) {
        runde = lvl;
    }
    public int getRunde() {
        return runde;
    }
}
