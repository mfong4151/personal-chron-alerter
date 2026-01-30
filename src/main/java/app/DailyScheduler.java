package app;

import java.time.*;
import java.util.Set;
import java.util.concurrent.*;

public final class DailyScheduler {

  private final ScheduledExecutorService exec;

  private static final Set<DayOfWeek> WEEKEND_DAYS = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  /**
   * TODO Add more market holidays 
   */
  private static final Set<LocalDate> HOLIDAYS = Set.of(
      LocalDate.of(2026, 1, 1), // New Year's Day
      LocalDate.of(2026, 12, 25) // Christmas
  );

  public DailyScheduler(ScheduledExecutorService exec) {
    this.exec = exec;
  }

  /**
   * Runs task once per business day at localTime in zoneId.
   * Weekends and holidays are skipped.
   */
  public void scheduleDaily(LocalTime localTime, ZoneId zoneId, Runnable task) {
    scheduleNext(localTime, zoneId, task);
  }

  private void scheduleNext(LocalTime localTime, ZoneId zoneId, Runnable task) {
    ZonedDateTime now = ZonedDateTime.now(zoneId);
    ZonedDateTime next = computeNextRun(now, localTime);

    long delayMs = Duration.between(now, next).toMillis();

    // Strongly recommended: visible in journalctl
    System.out.println(
        "Next run scheduled for " + next +
            " (" + next.getDayOfWeek() + ")");

    exec.schedule(() -> {
      try {
        task.run();
        System.out.println("Task completed successfully at " + ZonedDateTime.now(zoneId));
      }  catch (Exception e) {
        System.err.println("Task failed at " + ZonedDateTime.now(zoneId) + ": " + e.getMessage());
        e.printStackTrace();
      
      } finally {
        // Always reschedule after completion
        scheduleNext(localTime, zoneId, task);
      }
    }, delayMs, TimeUnit.MILLISECONDS);
  }

  private ZonedDateTime computeNextRun(ZonedDateTime now, LocalTime runAt) {
    ZonedDateTime candidate = now.with(runAt);

    // If today's run time already passed, move to next day
    if (!candidate.isAfter(now)) {
      candidate = candidate.plusDays(1);
    }

    LocalDate localDate = candidate.toLocalDate();

    Boolean shouldScheduleRoutine = !WEEKEND_DAYS.contains(localDate.getDayOfWeek())
        && !HOLIDAYS.contains(localDate);
    while (!shouldScheduleRoutine) {
      candidate = candidate.plusDays(1);
    }

    return candidate;
  }
}
