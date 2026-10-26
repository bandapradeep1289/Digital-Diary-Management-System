package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import exception.EmptyEntryException;
import model.Entry;
import util.DBConnection;

public class EntryDAO implements DiaryOperations {

    private static final String COLS =
            "entry_id, user_id, entry_type, entry_date, title, content, mood";

    private void validate(Entry e) throws EmptyEntryException {
        if (e.getTitle() == null || e.getTitle().trim().isEmpty()
                || e.getContent() == null || e.getContent().trim().isEmpty()) {
            throw new EmptyEntryException("Title and content cannot be empty.");
        }
    }

    @Override
    public void addEntry(Entry e) throws SQLException, EmptyEntryException {
        validate(e);
        String sql = "INSERT INTO entries (user_id, entry_type, entry_date, title, content, mood) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getUserId());
            ps.setString(2, e.getEntryType());
            ps.setDate(3, Date.valueOf(e.getEntryDate()));
            ps.setString(4, e.getTitle().trim());
            ps.setString(5, e.getContent());
            ps.setString(6, e.getMood());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setEntryId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void updateEntry(Entry e) throws SQLException, EmptyEntryException {
        validate(e);
        String sql = "UPDATE entries SET entry_type=?, entry_date=?, title=?, content=?, mood=? "
                   + "WHERE entry_id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getEntryType());
            ps.setDate(2, Date.valueOf(e.getEntryDate()));
            ps.setString(3, e.getTitle().trim());
            ps.setString(4, e.getContent());
            ps.setString(5, e.getMood());
            ps.setInt(6, e.getEntryId());
            ps.setInt(7, e.getUserId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteEntry(int entryId) throws SQLException {
        String sql = "DELETE FROM entries WHERE entry_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, entryId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Entry> getEntriesByUser(int userId) throws SQLException {
        String sql = "SELECT " + COLS + " FROM entries WHERE user_id = ? ORDER BY entry_date DESC, entry_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return readAll(ps);
        }
    }

    @Override
    public List<Entry> searchByKeyword(int userId, String keyword) throws SQLException {
        String sql = "SELECT " + COLS + " FROM entries WHERE user_id = ? "
                   + "AND (LOWER(title) LIKE ? OR LOWER(content) LIKE ?) ORDER BY entry_date DESC";
        String like = "%" + keyword.toLowerCase() + "%";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, like);
            ps.setString(3, like);
            return readAll(ps);
        }
    }

    @Override
    public List<Entry> searchByDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT " + COLS + " FROM entries WHERE user_id = ? AND entry_date = ? "
                   + "ORDER BY entry_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            return readAll(ps);
        }
    }

    private List<Entry> readAll(PreparedStatement ps) throws SQLException {
        List<Entry> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(Entry.create(
                        rs.getString("entry_type"),
                        rs.getInt("entry_id"),
                        rs.getInt("user_id"),
                        rs.getDate("entry_date").toLocalDate(),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("mood")));
            }
        }
        return list;
    }
}
