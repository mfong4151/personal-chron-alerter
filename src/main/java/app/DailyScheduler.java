package app;

import java.time.*;
import java.util.Set;
import java.util.concurrent.*;

public final class DailyScheduler {
    private final ScheduledExecutorService exec;
    private final static Set<DayOfWeek> EXCLUDED_DAY_OF_WEEKS = Set.of(
      DayOfWeek.SUNDAY, DayOfWeek.SATURDAY
    );

    public DailyScheduler(ScheduledExecutorService exec) {
        this.exec = exec;
    }

    /**
     *  Runs task once per day at localTime in zoneId. 
     */
    public void scheduleDaily(LocalTime localTime, ZoneId zoneId, Runnable task) {

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if (EXCLUDED_DAY_OF_WEEKS.contains(now.getDayOfWeek())){
            return;
        }
        scheduleNext(localTime, zoneId, task);
    }

    private void scheduleNext(LocalTime localTime, ZoneId zoneId, Runnable task) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime next = now.with(localTime);
        if (!next.isAfter(now)) next = next.plusDays(1);
        long delayMs = Duration.between(now, next).toMillis();

        exec.schedule(() -> {
            try {
                task.run();
            } finally {
                // schedule the next run after finishing
                scheduleNext(localTime, zoneId, task);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }
}
