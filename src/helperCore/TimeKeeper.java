package helperCore;

import java.time.OffsetDateTime;

public interface TimeKeeper {
    OffsetDateTime fireOn();
    //If true, remove it from any list, if false, keep it
    boolean OnTimeRunOut();
}
