package listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import util.STATIC;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Objects;

public class autoChannelListener extends ListenerAdapter {
    public static boolean isWorking = false;
    public static String VC_BASE_AUTOCHANNEL= "";
    public static ArrayList<String> VC_AUTOCHANNEL_CHILDS = new ArrayList<>();

    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        if (VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) return;
        testJoin(event.getChannelJoined(),event.getMember());
    }

    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        if (VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) return;
        testJoin(event.getChannelJoined(),event.getMember());
        testLeave(event.getChannelLeft());
    }

    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (VC_BASE_AUTOCHANNEL.equalsIgnoreCase("")) return;
        testLeave(event.getChannelLeft());
    }
    private void testJoin(VoiceChannel vc, Member m) {
        if (!vc.getId().equalsIgnoreCase(VC_BASE_AUTOCHANNEL)) return;
        if (!isWorking) return;
        try {
            VoiceChannel vcnew =vc.createCopy().setName(m.getEffectiveName()+"'s-Channel").reason("AutoChannel create").complete();
            vc.getGuild().moveVoiceMember(m,vcnew).queue();
            VC_AUTOCHANNEL_CHILDS.add(vcnew.getId());
        } catch (Exception e) {
            Objects.requireNonNull(vc.getGuild().getTextChannelById(STATIC.CHANNEL_ALLGEMEIN)).sendMessage("Aufgrund eines Fehlers konnte kein AutoChannel erstellt werden: "+e.getMessage()).queue();
            e.printStackTrace();
        }

    }
    private void testLeave(VoiceChannel vc) {
        if (!VC_AUTOCHANNEL_CHILDS.contains(vc.getId())) return;
        try {
            VC_AUTOCHANNEL_CHILDS.remove(vc.getId());
            if (vc.getMembers().isEmpty()) vc.delete().reason("AutoChannel remove").queue();
        } catch (Exception e) {
            Objects.requireNonNull(vc.getGuild().getTextChannelById(STATIC.CHANNEL_ALLGEMEIN)).sendMessage("Aufgrund eines Fehlers konnte ein AutoChannel nicht gelöscht werden: "+e.getMessage()).queue();
            e.printStackTrace();
        }
    }


}
