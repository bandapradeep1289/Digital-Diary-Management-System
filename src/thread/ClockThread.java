package thread;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/** Updates a label with the current date and time every second (Unit II: Thread). */
public class ClockThread extends Thread {
    private final JLabel label;
    private volatile boolean running = true;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss");

    public ClockThread(JLabel label) {
        this.label = label;
        setDaemon(true);
    }

    public void stopClock() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            final String text = LocalDateTime.now().format(FMT);
            SwingUtilities.invokeLater(() -> label.setText(text));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}
