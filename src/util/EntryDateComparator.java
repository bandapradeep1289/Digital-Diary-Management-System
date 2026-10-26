package util;

import java.util.Comparator;
import model.Entry;

/** Sorts entries by date (newest first by default). */
public class EntryDateComparator implements Comparator<Entry> {
    private final boolean newestFirst;

    public EntryDateComparator() {
        this(true);
    }

    public EntryDateComparator(boolean newestFirst) {
        this.newestFirst = newestFirst;
    }

    @Override
    public int compare(Entry a, Entry b) {
        int result = a.getEntryDate().compareTo(b.getEntryDate());
        return newestFirst ? -result : result;
    }
}
