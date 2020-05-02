package listeners;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class privateMessageListener extends ListenerAdapter {
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) return;
        event.getChannel().sendMessage("Wenn du entwas an mich richten willst, nutze bitte einen Channel auf dem Turnier-Server dafür").queue();
    }
}
