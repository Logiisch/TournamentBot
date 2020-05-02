package listeners;

import helperCore.UnconfirmedResult;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class ConfirmReactListener extends ListenerAdapter {
    public static HashMap<String, UnconfirmedResult> toReact = new HashMap<>();

    public void onPrivateMessageReactionAdd(@Nonnull PrivateMessageReactionAddEvent event) {
        if (!toReact.containsKey(event.getMessageId())) return;
        if (event.getReaction().retrieveUsers().complete().size()<2) return;
        //TODO: die korrekten Emotes testen
        UnconfirmedResult ur = toReact.get(event.getMessageId());
        boolean correctReact = false;
        //confirm
        if (event.getReactionEmote().getEmoji().equalsIgnoreCase("✅")){
            ur.confirm();
            correctReact = true;
        }
        //deny
        if (event.getReactionEmote().getEmoji().equalsIgnoreCase("❎")){
            ur.deny();
            correctReact=true;
        }
        if (correctReact) {
            toReact.remove(event.getMessageId());
        } else {
            System.out.println(event.getReactionEmote().getEmoji());
        }


    }
    public  void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (!toReact.containsKey(event.getMessageId())) return;
        UnconfirmedResult ur = toReact.get(event.getMessageId());
        boolean correctReact = false;
        //confirm
        if (event.getReactionEmote().getEmoji().equalsIgnoreCase("✅")){
            ur.confirm();
            correctReact = true;
        }
        //deny
        if (event.getReactionEmote().getEmoji().equalsIgnoreCase("❎")){
            ur.deny();
            correctReact=true;
        }
        if (correctReact) {
            toReact.remove(event.getMessageId());
        } else {
            System.out.println(event.getReactionEmote().getEmoji());
        }

    }
    public static boolean alreadyLogged(int nid) {
        for (String s : toReact.keySet()) {
            UnconfirmedResult ur = toReact.get(s);
            if (ur.getId() == nid) return true;
        }
        return false;
    }
}

