package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.Entry;

/** File handling: text export/import and serialization backup/restore (Unit III). */
public class FileUtil {

    private FileUtil() { }

    /** Writes one entry to a text file. The text is built with a StringBuffer. */
    public static void exportEntry(Entry e, File file) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("Date   : ").append(e.getEntryDate()).append("\n");
        sb.append("Type   : ").append(e.getEntryType()).append("\n");
        sb.append("Title  : ").append(e.getTitle()).append("\n");
        sb.append("Mood   : ").append(e.getMood()).append("\n");
        sb.append("------------------------------------\n");
        sb.append(e.getContent()).append("\n");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(sb.toString());
        }
    }

    /** Reads a whole text file and returns its contents. */
    public static String importText(File file) throws IOException {
        StringBuffer sb = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /** Saves a draft (used by AutoSaveThread). */
    public static void saveDraft(String text, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(text);
        }
    }

    /** Backup: serialize the whole list of entries. */
    public static void backup(List<Entry> entries, File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new ArrayList<Entry>(entries));
        }
    }

    /** Restore: read the serialized list back. */
    @SuppressWarnings("unchecked")
    public static List<Entry> restore(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Entry>) in.readObject();
        }
    }
}
