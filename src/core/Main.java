package core;


import commands.*;
import listeners.*;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import threads.AutoKickThread;
import util.SECRETS;
import util.STATIC;

import javax.security.auth.login.LoginException;

public class Main {
    private static JDABuilder builder = new JDABuilder(AccountType.BOT);

    public static void main(String[] args) {
        // JDABuilder builder = new JDABuilder(AccountType.BOT);
        if (!SECRETS.loadTokens()) return;
        builder.setToken(SECRETS.getTOKEN());
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.ONLINE);
        String Version = STATIC.ACTIVITY;


        builder.setActivity(Activity.playing(Version + ""));
        System.out.println("Starte mit Status " + Version + " ...");
        addListeners();
        addCommands();
        //readInStartValues();

        try {
            JDA jda = builder.build();
            StartThreads(jda);
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addListeners() {

        builder.addEventListeners(new commandListener());
        builder.addEventListeners(new ConfirmReactListener());
        builder.addEventListeners(new readyListener());
        builder.addEventListeners(new privateMessageListener());
        builder.addEventListeners(new autoChannelListener());
    }

    private static void addCommands() {
        commandHandler.commands.put("start", new cmdStart());
        commandHandler.commands.put("res", new cmdResult());
        commandHandler.commands.put("retry", new cmdRetry());
        commandHandler.commands.put("kick", new cmdKick());
        commandHandler.commands.put("rejoin", new cmdRejoin());
        commandHandler.commands.put("revert", new cmdRevert());
        commandHandler.commands.put("help", new cmdHelp());
        commandHandler.commands.put("msg",new cmdMessage());
        commandHandler.commands.put("set", new cmdSet());
        commandHandler.commands.put("delete", new cmdReset());
        commandHandler.commands.put("info", new cmdInfo());
        commandHandler.commands.put("prefix", new cmdPrefix());
        commandHandler.commands.put("leave", new cmdLeave());
        commandHandler.commands.put("messagetorole",new cmdMessageRole());
        commandHandler.commands.put("status", new cmdUpdateStatus());
        commandHandler.commands.put("next", new cmdNext());
        commandHandler.commands.put("bracket", new cmdBracket());
        commandHandler.commands.put("final", new cmdFinalDisplay());
        commandHandler.commands.put("autochannel", new cmdAutoChannel());
        commandHandler.commands.put("setup", new cmdSetup());
        commandHandler.commands.put("reload",new cmdReload());
        commandHandler.commands.put("lang", new cmdLang());
        commandHandler.commands.put("timezone",new cmdTimezone());

    }

    private static void StartThreads(JDA jda) {
        Thread autokick = new Thread(new AutoKickThread());
        autokick.start();
    }

    //TODO: Speicher und Post-offline Abfrage der IDs




}
