package commands;

import helperCore.LangManager;
import helperCore.Logic;
import helperCore.PermissionLevel;
import helperCore.TournamentNode;
import listeners.ConfirmReactListener;
import listeners.commandListener;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import util.STATIC;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class cmdReset implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public PermissionLevel PermLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String prefix = commandListener.getPrefix(event.getGuild());
        Role admin = event.getGuild().getRoleById(STATIC.getSettings(event.getGuild(),"ROLE_ADMIN"));;
        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(admin)&&!event.getAuthor().getId().equalsIgnoreCase(STATIC.OWNERID)) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdGeneralOnlyAdmin")).queue();
            return;
        }
        if (args.length<1) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdResetConfirm").replace("%PREFIX",prefix)).queue();
            return;
        }
        if(!args[0].equalsIgnoreCase("confirm")) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdresetConfirm").replace("%PREFIX",prefix)).queue();
            return;
        }
        HashMap<Integer, TournamentNode> nodes = Logic.getNodes(event.getGuild());
        nodes.clear();
        Logic.setNodes(event.getGuild(),nodes);
        ConfirmReactListener.toConfirmResult.clear();
        File f = new File ("data/guilds/"+event.getGuild().getId()+"/nodes.txt");
        if (f.exists()) //noinspection ResultOfMethodCallIgnored
            f.delete();
        TextChannel tc =event.getGuild().getTextChannelById(STATIC.getSettings(event.getGuild(),"CHANNEL_RESULTS"));
        assert tc != null;
        try {
            tc.deleteMessages(tc.getHistoryFromBeginning(100).complete().getRetrievedHistory()).queue();
        } catch (Exception e) {
            event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdResetError").replace("%MSG%",e.getMessage())).queue();
        }
        event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdResetMsgClear")).queue();
        for (Member m:event.getGuild().getMembers()) {
            removeRoles(m);
            removeMemberConversation(m);
        }

        event.getTextChannel().sendMessage(LangManager.get(event.getGuild(),"cmdResetSuccess").replace("%PREFIX%",prefix)).queue();
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public boolean isPrivate() {
        return true;
    }

    @Override
    public String Def(String prefix,Guild g) {
        return LangManager.get(g,"cmdResetDef");
    }

    private void removeRoles(Member m) {
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_TOT")));
        roles.add(m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_FINALE")));
        roles.add(m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_HALBFINALE")));
        roles.add(m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_VIERTELFINALE")));
        roles.add(m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_ACHTELFINALE")));
        roles.add(m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_VORRUNDE2")));
        roles.add(m.getGuild().getRoleById(STATIC.getSettings(m.getGuild().getId(),"ROLE_VORRUNDE1")));
        for (Role r:roles) {
            //if (m.getRoles().contains(r))
            try {
                m.getGuild().removeRoleFromMember(m,r).queue();
            } catch (ErrorResponseException e) {
                System.out.println("Fehler beim Löschen! "+e.getMeaning());
            }

        }

    }
    private void removeMemberConversation(Member m) {
        if (m.getUser().getId().equalsIgnoreCase(m.getJDA().getSelfUser().getId()))return;
        PrivateChannel pc =m.getUser().openPrivateChannel().complete();
        MessageHistory mh =pc.getHistoryFromBeginning(100).complete();
        if (mh.size()==0) return;
        for (Message ms:mh.getRetrievedHistory()) {
            if (!ms.getAuthor().getId().equalsIgnoreCase(m.getJDA().getSelfUser().getId())) continue;
            pc.deleteMessageById(ms.getId()).queue();
        }
    }
}
