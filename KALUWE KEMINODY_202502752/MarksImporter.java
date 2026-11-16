import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * ICT261 Unit 4 Lab - Exception Handling and Error Management.
 *
 * A small console importer for student marks.
 *
 * RECOVERY STRATEGY (decision for Activity B / C):
 *   - A bad RECORD (wrong number of fields, non-numeric mark, mark outside 0..100,
 *     or a repeated student number) is SKIPPED. It is reported on the console and
 *     written to the log, and the import CONTINUES with the next line.
 *   - A bad FILE (missing or unreadable) STOPS the import, because there is
 *     nothing safe to continue with.
 *
 * Usage:  java MarksImporter            (reads marks.txt)
 *         java MarksImporter other.txt  (reads another file - used for tests T1-T5)
 */
public class MarksImporter {

    // Technical details go to the logger (file import.log), NOT to the user's console.
    private static final Logger LOGGER = Logger.getLogger(MarksImporter.class.getName());

    /** Sets up the logger so technical details are written to import.log only. */
    private static void setupLogger() {
        // Compact one-line log format; the stack trace (if any) is appended by %6$s.
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$s %2$s - %5$s%6$s%n");
        try {
            FileHandler fileHandler = new FileHandler("import.log", true); // true = append
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false); // keep technical logs off the console
        } catch (IOException | SecurityException e) {
            // If the log file cannot be created, fall back to the default console logger.
            System.out.println("Note: could not create import.log; technical logs will use the console.");
        }
    }

    /** Closes the log file so that import.log.lck is removed. */
    private static void closeLogger() {
        for (java.util.logging.Handler handler : LOGGER.getHandlers()) {
            handler.close();
        }
    }

    /**
     * Parses a mark and validates the range 0..100.
     *
     * @throws NumberFormatException    (unchecked) thrown by Integer.parseInt when text is not a number
     * @throws IllegalArgumentException (unchecked) thrown by us when the number is outside 0..100
     */
    static int parseMark(String text) {
        int mark = Integer.parseInt(text.trim());           // may throw NumberFormatException
        if (mark < 0 || mark > 100) {
            throw new IllegalArgumentException(
                    "Mark must be between 0 and 100 (found " + mark + ")");   // 'throw' = raise it now
        }
        return mark;
    }

    /**
     * Reads the marks file line by line.
     * 'throws IOException' declares that the CHECKED exceptions of file reading
     * (FileNotFoundException is a subclass of IOException) are passed on to the caller.
     */
    static void importMarks(String filename) throws IOException {
        Set<String> seen = new HashSet<>();
        int accepted = 0;
        int skipped = 0;
        int lineNumber = 0;

        // try-with-resources: the BufferedReader (and the FileReader inside it) implements
        // AutoCloseable, so Java automatically calls br.close() when the try block ends -
        // whether it ends normally OR because an exception was thrown. No finally is needed.
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // ignore blank lines
                }
                String[] parts = line.split(",");
                try {
                    // TODO 1: check there are exactly 2 fields
                    if (parts.length != 2) {
                        throw new IllegalArgumentException(
                                "Expected 2 fields (student number, mark) but found " + parts.length);
                    }
                    String studentId = parts[0].trim();
                    if (studentId.isEmpty()) {
                        throw new IllegalArgumentException("Student number is missing");
                    }

                    // TODO 2: parse mark and validate range 0..100
                    int mark = parseMark(parts[1]);

                    // TODO 3: detect repeated student ID using seen
                    if (!seen.add(studentId)) {          // add() returns false if already present
                        throw new DuplicateStudentException(studentId);
                    }

                    // TODO 4: display accepted record
                    System.out.println("Accepted: " + studentId + " -> " + mark);
                    accepted++;

                } catch (NumberFormatException e) {
                    // Must come BEFORE IllegalArgumentException (it is a subclass of it).
                    // parts[1] is safe here: the field count was checked before parsing.
                    skipped++;
                    System.out.println("Line " + lineNumber + " skipped: the mark '"
                            + parts[1].trim() + "' is not a valid number.");
                    LOGGER.log(Level.WARNING, "Line " + lineNumber + " [" + line + "] "
                            + e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    skipped++;
                    System.out.println("Line " + lineNumber + " skipped: " + e.getMessage() + ".");
                    LOGGER.log(Level.WARNING, "Line " + lineNumber + " [" + line + "] "
                            + e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (DuplicateStudentException e) {
                    skipped++;
                    System.out.println("Line " + lineNumber + " skipped: student number "
                            + e.getStudentId() + " has already been imported (duplicate).");
                    LOGGER.log(Level.WARNING, "Line " + lineNumber + " [" + line + "] "
                            + e.getClass().getName() + ": " + e.getMessage(), e);
                }
            }
        }
        System.out.println("Summary: " + accepted + " accepted, " + skipped + " skipped.");
    }

    public static void main(String[] args) {
        setupLogger();
        // Default file is marks.txt; an optional argument lets us test other files (T1-T5).
        String filename = (args.length > 0) ? args[0] : "marks.txt";
        try {
            importMarks(filename);

        } catch (FileNotFoundException e) {
            // Specific checked exception FIRST (it is a subclass of IOException).
            System.out.println("Marks file not found; check the file name ('" + filename + "').");
            LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);

        } catch (IOException e) {
            // General checked exception AFTER the specific one.
            System.out.println("The marks file could not be read. Please try again or contact support.");
            LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);

        } catch (DuplicateStudentException e) {
            // Safety net: with the SKIP strategy importMarks() already handles duplicates per
            // record, so this is not normally reached. It keeps main() safe if the importer
            // is ever changed to stop on the first bad record.
            System.out.println("Import stopped: duplicate student number " + e.getStudentId() + ".");
            LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);

        } catch (IllegalArgumentException e) {
            // Safety net for invalid data (also covers NumberFormatException), same reason as above.
            System.out.println("Import stopped: invalid data found (" + e.getMessage() + ").");
            LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);

        } finally {
            // finally ALWAYS runs: after success, after a handled exception, or on file-not-found.
            System.out.println("Import attempt finished");
            closeLogger();
        }
    }
}

/**
 * Custom UNCHECKED exception (extends RuntimeException): thrown when the same student
 * number appears more than once in the marks file.
 */
class DuplicateStudentException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String studentId;

    public DuplicateStudentException(String studentId) {
        super("Duplicate student number: " + studentId);
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }
}
