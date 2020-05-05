package helperCore;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;

public class TournamentNode {

    public int NID;
    public int promoteToNID;
    public User winner=null;
    private int runde;
    ArrayList<Integer> promoteFrom = new ArrayList<>();
    public ArrayList<User> players = new ArrayList<>();
    private int bracketNodeNbr;
    private ArrayList<Integer> bracketSubNotes = new ArrayList<>();

    TournamentNode(int nid, int ptnid, int rnd,int bracket) {
        NID = nid;
        promoteToNID = ptnid;
        runde = rnd;
        bracketNodeNbr = bracket;
    }
    /*TournamentNode(int nid, int ptnid, int rnd, int bracket, int suba, int subb) {
        this(nid,ptnid,rnd,bracket);
        bracketSubNotes.add(suba);
        bracketSubNotes.add(subb);
    }*/
    TournamentNode(int nid, int promotetonid, User wnner, int rnde, ArrayList<Integer> promFrom, ArrayList<User> plyrs,int brckt,ArrayList<Integer> subbrckt) {
        NID = nid;
        promoteToNID= promotetonid;
        winner =wnner;
        promoteFrom= promFrom;
        players = plyrs;
        runde= rnde;
        bracketNodeNbr = brckt;
        bracketSubNotes = subbrckt;
    }
    public int getBracketNbr() {
        return bracketNodeNbr;
    }
    public ArrayList<Integer> getBracketSub() {
        return bracketSubNotes;
    }
    public void addSub(int sub) {
        bracketSubNotes.add(sub);
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
