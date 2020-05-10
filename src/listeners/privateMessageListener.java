package listeners;

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class privateMessageListener extends ListenerAdapter {
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if (event.getAuthor().getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) return;
        event.getChannel().sendMessage("If you want to say something to me, please use a channel on a guild where I am also running!").queue();
    }
}
