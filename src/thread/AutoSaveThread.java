package thread;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import util.FileUtil;

/** Saves the draft being typed to a file at regular intervals (Unit II: Runnable). */
public class AutoSaveThread implements Runnable {
    private final Supplier<String> draftSource;
    private final Consumer<String> statusCallback;
    private final File draftFile;
    private final int intervalSeconds;
    private volatile boolean running = true;

    public AutoSaveThread(Supplier<String> draftSource, Consumer<String> statusCallback,
                          File draftFile, int intervalSeconds) {
        this.draftSource = draftSource;
        this.statusCallback = statusCallback;
        this.draftFile = draftFile;
        this.intervalSeconds = intervalSeconds;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(intervalSeconds * 1000L);
            } catch (InterruptedException ex) {
                return;
            }
            if (!running) {
                return;
            }
            try {
                String text = draftSource.get();
                if (text != null && !text.trim().isEmpty()) {
                    FileUtil.saveDraft(text, draftFile);
                    final String msg = "Draft auto-saved at "
                            + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    SwingUtilities.invokeLater(() -> statusCallback.accept(msg));
                }
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> statusCallback.accept("Auto-save failed"));
            }
        }
    }
}
