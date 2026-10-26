package ui;

import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import dao.UserDAO;
import exception.DuplicateUserException;

public class RegisterPanel extends JPanel {
    private final JTextField userField = new JTextField(18);
    private final JPasswordField passField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);
    private final UserDAO userDAO = new UserDAO();

    public RegisterPanel(MainFrame frame) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel("Register New User");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(title, c);

        c.gridwidth = 1; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        add(userField, c);

        c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        add(passField, c);

        c.gridx = 0; c.gridy = 3; c.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirm password:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        add(confirmField, c);

        JButton regBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");
        JPanel btns = new JPanel();
        btns.add(regBtn);
        btns.add(backBtn);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        add(btns, c);

        regBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            String cp = new String(confirmField.getPassword());
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password are required.");
                return;
            }
            if (!p.equals(cp)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }
            try {
                userDAO.register(u, p);
                JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
                clear();
                frame.showLogin();
            } catch (DuplicateUserException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Duplicate User",
                        JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        backBtn.addActionListener(e -> { clear(); frame.showLogin(); });
    }

    private void clear() {
        userField.setText("");
        passField.setText("");
        confirmField.setText("");
    }
}
