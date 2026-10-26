package dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import exception.EmptyEntryException;
import model.Entry;

/** Operations on diary entries (Unit I: interface). */
public interface DiaryOperations {
    void addEntry(Entry e) throws SQLException, EmptyEntryException;
    void updateEntry(Entry e) throws SQLException, EmptyEntryException;
    void deleteEntry(int entryId) throws SQLException;
    List<Entry> getEntriesByUser(int userId) throws SQLException;
    List<Entry> searchByKeyword(int userId, String keyword) throws SQLException;
    List<Entry> searchByDate(int userId, LocalDate date) throws SQLException;
}
