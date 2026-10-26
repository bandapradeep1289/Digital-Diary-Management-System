package ui;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import model.User;

/** Main window. Uses CardLayout to switch between login, register and dashboard. */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;

    public MainFrame() {
        super("Digital Diary Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        loginPanel = new LoginPanel(this);
        root.add(loginPanel, "login");
        root.add(new RegisterPanel(this), "register");
        add(root);
        showLogin();
    }

    public void showLogin() {
        if (dashboardPanel != null) {
            dashboardPanel.stopThreads();
            root.remove(dashboardPanel);
            dashboardPanel = null;
        }
        cards.show(root, "login");
    }

    public void showRegister() {
        cards.show(root, "register");
    }

    public void showDashboard(User user) {
        if (dashboardPanel != null) {
            dashboardPanel.stopThreads();
            root.remove(dashboardPanel);
        }
        dashboardPanel = new DashboardPanel(this, user);
        root.add(dashboardPanel, "dashboard");
        cards.show(root, "dashboard");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default look and feel
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
