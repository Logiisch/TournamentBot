package helperCore;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public interface retryOnDemand {

    boolean tryRun(User u);
    default String whatiscurrentlywrong(Guild g) {
        return LangManager.get(g,"RODOpenDMs");
    }
}
