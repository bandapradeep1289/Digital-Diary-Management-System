package ui;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import dao.UserDAO;
import exception.InvalidLoginException;
import model.User;
import thread.ClockThread;

public class LoginPanel extends JPanel {
    private final MainFrame frame;
    private final JTextField userField = new JTextField(18);
    private final JPasswordField passField = new JPasswordField(18);
    private final JLabel clockLabel = new JLabel(" ", SwingConstants.CENTER);
    private final UserDAO userDAO = new UserDAO();

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Digital Diary", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(title);
        top.add(clockLabel);
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Username:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        form.add(userField, c);
        c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Password:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        form.add(passField, c);

        JButton loginBtn = new JButton("Login");
        c.gridx = 1; c.gridy = 2;
        form.add(loginBtn, c);

        // MouseListener: clickable "Register" link with hover effect
        JLabel regLink = new JLabel("New user? Click here to register");
        regLink.setForeground(Color.BLUE);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { frame.showRegister(); }
            @Override public void mouseEntered(MouseEvent e) { regLink.setForeground(Color.RED); }
            @Override public void mouseExited(MouseEvent e)  { regLink.setForeground(Color.BLUE); }
        });
        c.gridy = 3;
        form.add(regLink, c);
        add(form, BorderLayout.CENTER);

        // ActionListener for the button
        loginBtn.addActionListener(e -> doLogin());
        // KeyListener: pressing Enter in the password box logs in
        passField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        new ClockThread(clockLabel).start();
    }

    private void doLogin() {
        try {
            User user = userDAO.login(userField.getText(), new String(passField.getPassword()));
            passField.setText("");
            frame.showDashboard(user);
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
