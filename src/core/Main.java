package core;


import commands.*;
import listeners.ConfirmReactListener;
import listeners.commandListener;

import listeners.privateMessageListener;
import listeners.readyListener;
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
    public static JDABuilder builder = new JDABuilder(AccountType.BOT);

    public static void main(String[] args) {
        // JDABuilder builder = new JDABuilder(AccountType.BOT);
        if (!SECRETS.loadTokens()) return;
        builder.setToken(SECRETS.getTOKEN());
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.ONLINE);
        String Version = STATIC.VERSION;


        builder.setActivity(Activity.playing(Version + ""));
        System.out.println("Starte auf " + Version + " ...");
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

    public static void addListeners() {

        builder.addEventListeners(new commandListener());
        builder.addEventListeners(new ConfirmReactListener());
        builder.addEventListeners(new readyListener());
        builder.addEventListeners(new privateMessageListener());
    }

    public static void addCommands() {
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


    }

    public static void StartThreads(JDA jda) {
        Thread autokick = new Thread(new AutoKickThread());
        autokick.start();
    }

    //TODO: Speicher und Post-offline Abfrage der IDs




}
