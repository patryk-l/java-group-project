package group.file_loaders;

public class CSVRow {
    String fileName;
    String[] tagNames;

    CSVRow(String fN, String[] tN) {
        fileName = fN;
        tagNames = tN;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String[] getTagNames() {
        return tagNames;
    }

    public void setTagNames(String[] tagNames) {
        this.tagNames = tagNames;
    }

    public CSVRow stripWhitespace(){
        for(int i=0;i< getTagNames().length;i++)
            tagNames[i] = tagNames[i].strip();
        return this;
    }

    public CSVRow lowercase(){
        for(int i=0;i< getTagNames().length;i++)
            tagNames[i] = tagNames[i].toLowerCase();
        return this;
    }
}
