package helperCore;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.HashMap;

public class TournamentNode {

    public int NID;
    public int promoteToNID;
    public User winner=null;
    private int runde;
    ArrayList<Integer> promoteFrom = new ArrayList<>();
    public ArrayList<User> players = new ArrayList<>();
    private int bracketNodeNbr;
    private ArrayList<Integer> bracketSubNotes = new ArrayList<>();
    private Guild guild;

    TournamentNode(int nid, int ptnid, int rnd,int bracket,Guild g) {
        NID = nid;
        promoteToNID = ptnid;
        runde = rnd;
        bracketNodeNbr = bracket;
        guild = g;
    }
    /*TournamentNode(int nid, int ptnid, int rnd, int bracket, int suba, int subb) {
        this(nid,ptnid,rnd,bracket);
        bracketSubNotes.add(suba);
        bracketSubNotes.add(subb);
    }*/
    TournamentNode(int nid, int promotetonid, User wnner, int rnde, ArrayList<Integer> promFrom, ArrayList<User> plyrs,int brckt,ArrayList<Integer> subbrckt,Guild g) {
        NID = nid;
        promoteToNID= promotetonid;
        winner =wnner;
        promoteFrom= promFrom;
        players = plyrs;
        runde= rnde;
        bracketNodeNbr = brckt;
        bracketSubNotes = subbrckt;
        guild = g;
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
        HashMap<Integer,TournamentNode> nodes = Logic.getNodes(guild);
        nodes.put(NID,this);
        Logic.setNodes(guild,nodes);
        if(save) Logic.save(guild);
    }

    public int getRunde() {
        return runde;
    }
}
