package helperCore;

import net.dv8tion.jda.api.entities.User;

public interface retryOnDemand {

    boolean tryRun(User u);
    default String whatiscurrentlywrong() {
        return "Bitte öffne deine DM's!";
    }
}
