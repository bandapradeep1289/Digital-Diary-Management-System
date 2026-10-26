package model;

import java.time.LocalDate;

public class TravelEntry extends Entry {
    private static final long serialVersionUID = 1L;

    public TravelEntry(int entryId, int userId, LocalDate entryDate,
                  String title, String content, String mood) {
        super(entryId, userId, entryDate, title, content, mood);
    }

    @Override
    public String getEntryType() {
        return "Travel";
    }
}
