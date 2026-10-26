package dao;

import java.sql.*;
import exception.DuplicateUserException;
import exception.InvalidLoginException;
import model.User;
import util.DBConnection;

public class UserDAO {

    public void register(String username, String password)
            throws SQLException, DuplicateUserException {
        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty.");
        }
        String check = "SELECT user_id FROM users WHERE username = ?";
        String insert = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(check)) {
                ps.setString(1, username.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new DuplicateUserException("Username '" + username + "' is already taken.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setString(1, username.trim());
                ps.setString(2, password);
                ps.executeUpdate();
            }
        }
    }

    public User login(String username, String password)
            throws SQLException, InvalidLoginException {
        String sql = "SELECT user_id, username, password FROM users WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("username"),
                                    rs.getString("password"));
                }
            }
        }
        throw new InvalidLoginException("Wrong username or password.");
    }
}
