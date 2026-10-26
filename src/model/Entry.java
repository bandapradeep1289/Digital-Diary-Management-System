package model;

import java.io.Serializable;
import java.time.LocalDate;

/** Abstract base class for all diary entries (Unit I: abstract class, inheritance). */
public abstract class Entry implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int entryId;
    protected int userId;
    protected LocalDate entryDate;
    protected String title;
    protected String content;
    protected String mood;

    public Entry(int entryId, int userId, LocalDate entryDate,
                 String title, String content, String mood) {
        this.entryId = entryId;
        this.userId = userId;
        this.entryDate = entryDate;
        this.title = title;
        this.content = content;
        this.mood = mood;
    }

    /** Each subclass returns its own type name. */
    public abstract String getEntryType();

    /** Factory method that builds the right subclass from a type name. */
    public static Entry create(String type, int entryId, int userId, LocalDate date,
                               String title, String content, String mood) {
        if ("Work".equalsIgnoreCase(type)) {
            return new WorkEntry(entryId, userId, date, title, content, mood);
        } else if ("Travel".equalsIgnoreCase(type)) {
            return new TravelEntry(entryId, userId, date, title, content, mood);
        }
        return new PersonalEntry(entryId, userId, date, title, content, mood);
    }

    public int getEntryId()          { return entryId; }
    public void setEntryId(int id)   { this.entryId = id; }
    public int getUserId()           { return userId; }
    public LocalDate getEntryDate()  { return entryDate; }
    public String getTitle()         { return title; }
    public String getContent()       { return content; }
    public String getMood()          { return mood; }

    public void setEntryDate(LocalDate d) { this.entryDate = d; }
    public void setTitle(String t)        { this.title = t; }
    public void setContent(String c)      { this.content = c; }
    public void setMood(String m)         { this.mood = m; }

    @Override
    public String toString() {
        return entryDate + " | " + getEntryType() + " | " + title;
    }
}
