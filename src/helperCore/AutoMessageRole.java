package helperCore;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;

public class AutoMessageRole {
    
    private final String MessageID;
    private final String TextChannelID;
    private final String RoleID;
    private final String Emoji;
    private final String GuildID;
    private final ArrayList<User> addedViaAMR = new ArrayList<>();
    
    public AutoMessageRole(String MsgID, String TcID, String RlID,String GuID, String Em) {
        MessageID = MsgID;
        TextChannelID = TcID;
        RoleID = RlID;
        Emoji = Em;
        GuildID = GuID;
    }

    public String getEmoji() {
        return Emoji;
    }

    public String getMessageID() {
        return MessageID;
    }

    public String getRoleID() {
        return RoleID;
    }

    public String getTextChannelID() {
        return TextChannelID;
    }

    public String getGuildID() {
        return GuildID;
    }
    public void addUser(User u) {
        addedViaAMR.add(u);
    }
    public ArrayList<User> overAMRadded() {
        return addedViaAMR;
    }
    public void remUser(User u) {
        addedViaAMR.remove(u);
    }
}
