package listeners;

import helperCore.AutoMessageRole;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class MessageRoleListener extends ListenerAdapter {

    public static ArrayList<AutoMessageRole> amrs = new ArrayList<>();

    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {

        for (AutoMessageRole amr:amrs) {
            if (!amr.getGuildID().equals(event.getGuild().getId())) continue;
            if (!amr.getTextChannelID().equals(event.getChannel().getId())) continue;
            if (!amr.getMessageID().equals(event.getMessageId())) continue;
            if (!amr.getEmoji().equals(event.getReactionEmote().getEmoji())) continue;
            Role role = event.getGuild().getRoleById(amr.getRoleID());
            if (role==null) return;
            event.getGuild().addRoleToMember(event.getMember(),role).queue();
            amr.addUser(event.getUser());
        }

    }

    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {

        for (AutoMessageRole amr:amrs) {
            if (!amr.getGuildID().equals(event.getGuild().getId())) continue;
            if (!amr.getTextChannelID().equals(event.getChannel().getId())) continue;
            if (!amr.getMessageID().equals(event.getMessageId())) continue;
            if (!amr.getEmoji().equals(event.getReactionEmote().getEmoji())) continue;
            Role role = event.getGuild().getRoleById(amr.getRoleID());
            if (role==null) return;
            if (event.getMember()==null) return;
            event.getGuild().removeRoleFromMember(event.getMember(),role).queue();
            amr.remUser(event.getUser());
        }

    }

    public void onGuildMessageReactionRemoveAll(@Nonnull GuildMessageReactionRemoveAllEvent event) {

        for (AutoMessageRole amr:amrs) {
            if (!amr.getTextChannelID().equals(event.getChannel().getId())) continue;
            if (!amr.getMessageID().equals(event.getMessageId())) continue;
            Role role = event.getGuild().getRoleById(amr.getRoleID());
            if (role==null) return;
            for (User u:amr.overAMRadded()) {
                amr.remUser(u);
                if (event.getGuild().isMember(u)) {
                    Member m = event.getGuild().getMember(u);
                    if (m==null) continue;
                    event.getGuild().removeRoleFromMember(m,role).queue();
                }
            }
        }

    }


}
