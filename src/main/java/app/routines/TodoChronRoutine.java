package app.routines;

import java.time.LocalDate;

import app.DiscordNotifier;

public class TodoChronRoutine implements ChronRoutine {
    private static final String TODO_LIST_URL = "https://www.notion.so/To-do-39e54ae725f84e30b2086225f83a9398?source=copy_link";
    private final DiscordNotifier notifier;

    public TodoChronRoutine(DiscordNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void run(LocalDate day) {
      notifier.send("Good morning Max! Kind reminder to check your to-do list for today: " + TODO_LIST_URL);
      return;
    }
}
