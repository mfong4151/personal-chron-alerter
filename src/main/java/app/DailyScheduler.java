package app;

import java.time.*;
import java.util.Set;
import java.util.concurrent.*;

public final class DailyScheduler {

  private final ScheduledExecutorService exec;

  private static final Set<DayOfWeek> WEEKEND_DAYS = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  private static final Set<LocalDate> HOLIDAYS = Set.of(
      LocalDate.of(2026, 1, 1),   // New Year's Day
      LocalDate.of(2026, 1, 19),  // Martin Luther King Jr. Day
      LocalDate.of(2026, 2, 16),  // Presidents' Day
      LocalDate.of(2026, 4, 3),   // Good Friday
      LocalDate.of(2026, 5, 25),  // Memorial Day
      LocalDate.of(2026, 6, 19),  // Juneteenth
      LocalDate.of(2026, 7, 3),   // Independence Day (observed)
      LocalDate.of(2026, 9, 7),   // Labor Day
      LocalDate.of(2026, 11, 26), // Thanksgiving Day
      LocalDate.of(2026, 12, 25)  // Christmas
  );

  public DailyScheduler(ScheduledExecutorService exec) {
    this.exec = exec;
  }

  /**
   * Runs the given task once per day at runAt in zoneId. Excluded days are
   * skipped.
   */
  public void scheduleDaily(LocalTime runAt, ZoneId zoneId, Runnable task) {
    scheduleNext(runAt, zoneId, task);
  }

  private static boolean isExcluded(LocalDate date) {
    return WEEKEND_DAYS.contains(date.getDayOfWeek()) || HOLIDAYS.contains(date);
  }

  private void scheduleNext(LocalTime runAt, ZoneId zoneId, Runnable task) {
    ZonedDateTime now = ZonedDateTime.now(zoneId);
    ZonedDateTime next = computeNextDailyRun(now, runAt);

    long delayMs = Duration.between(now, next).toMillis();

    System.out.println("Next run scheduled for " + next + " (" + next.getDayOfWeek() + ")");

    exec.schedule(() -> {
      try {
        LocalDate today = LocalDate.now(zoneId);

        if (isExcluded(today)) {
          System.out.println("Skipping routine on excluded day: " + today + " (" + today.getDayOfWeek() + ")");
          return; // <- key change: we still reschedule in finally
        }

        System.out.println("RUN START " + ZonedDateTime.now(zoneId));
        task.run();
        System.out.println("RUN OK " + ZonedDateTime.now(zoneId));

      } catch (Throwable t) {
        System.err.println("RUN FAIL " + ZonedDateTime.now(zoneId) + " err=" + t);
        t.printStackTrace(System.err);

      } finally {
        // Always schedule the next calendar-day run
        scheduleNext(runAt, zoneId, task);
      }
    }, delayMs, TimeUnit.MILLISECONDS);
  }

  /** Next calendar-day occurrence of runAt in the same zone. */
  private static ZonedDateTime computeNextDailyRun(ZonedDateTime now, LocalTime runAt) {
    ZonedDateTime candidate = now.with(runAt);
    if (!candidate.isAfter(now)) {
      candidate = candidate.plusDays(1);
    }
    return candidate;
  }
}
