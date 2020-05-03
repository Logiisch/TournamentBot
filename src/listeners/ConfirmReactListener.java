package listeners;

import helperCore.RoundTime;
import helperCore.UnconfirmedResult;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Objects;

public class ConfirmReactListener extends ListenerAdapter {
    public static HashMap<String, UnconfirmedResult> toConfirmResult = new HashMap<>();
    public static HashMap<String, RoundTime> rtimes = new HashMap<>();

    public void onPrivateMessageReactionAdd(@Nonnull PrivateMessageReactionAddEvent event) {
        if (!toConfirmResult.containsKey(event.getMessageId()) &&!rtimes.containsKey(event.getMessageId())) return;
        if (Objects.requireNonNull(event.getUser()).getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) return;
        if (event.getReaction().retrieveUsers().complete().size() < 2) return;
        if (toConfirmResult.containsKey(event.getMessageId())) {
            UnconfirmedResult ur = toConfirmResult.get(event.getMessageId());
            boolean correctReact = false;
            //confirm
            if (event.getReactionEmote().getEmoji().equalsIgnoreCase("✅")) {
                ur.confirm();
                correctReact = true;
            }
            //deny
            if (event.getReactionEmote().getEmoji().equalsIgnoreCase("❎")) {
                ur.deny();
                correctReact = true;
            }
            if (correctReact) {
                toConfirmResult.remove(event.getMessageId());
            } else {
                System.out.println(event.getReactionEmote().getEmoji());
            }
        }
        if (rtimes.containsKey(event.getMessageId())) {
            if (!event.getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDC4D")) return;
            RoundTime rt = rtimes.get(event.getMessageId());
            rt.userreact(event.getUser());
            rtimes.put(event.getMessageId(),rt);
            event.getChannel().sendMessage("Danke, du hast deine Anwesenheit bestätigt. Bitte erinner auch deinen Gegner, die zu tun!").queue();
        }




    }
    public  void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (!toConfirmResult.containsKey(event.getMessageId())) return;
        UnconfirmedResult ur = toConfirmResult.get(event.getMessageId());
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
            toConfirmResult.remove(event.getMessageId());
        } else {
            System.out.println(event.getReactionEmote().getEmoji());
        }

    }
    public static boolean alreadyLogged(int nid) {
        for (String s : toConfirmResult.keySet()) {
            UnconfirmedResult ur = toConfirmResult.get(s);
            if (ur.getId() == nid) return true;
        }
        return false;
    }
}

