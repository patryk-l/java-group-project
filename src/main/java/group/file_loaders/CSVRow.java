package group.file_loaders;

public class CSVRow {
    String fileName;
    String tagNames[];

    CSVRow(String fN, String[] tN) {
        fileName = fN;
        tagNames = tN;
    }
}
