package world.sake.laspock.internal;

import java.sql.Timestamp;

import javax.annotation.Resource;

import org.dbflute.hook.AccessContext;
import org.lastaflute.core.time.TimeManager;

/**
 * @author neginuki
 */
public class AccessContextHelper {

    @Resource
    private TimeManager timeManager;

    public void prepare() {
        AccessContext context = new AccessContext();
        context.setAccessLocalDate(timeManager.currentDate());
        context.setAccessLocalDateTime(timeManager.currentDateTime());
        context.setAccessTimestamp(new Timestamp(timeManager.currentMillis()));
        context.setAccessDate(timeManager.currentUtilDate());
        context.setAccessUser(Thread.currentThread().getName());
        context.setAccessProcess(getClass().getSimpleName());
        context.setAccessModule(getClass().getSimpleName());
        AccessContext.setAccessContextOnThread(context);
    }

    public void clear() {
        AccessContext.clearAccessContextOnThread();
    }
}
