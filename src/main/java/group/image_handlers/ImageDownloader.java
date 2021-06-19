package group.image_handlers;

import group.DBConnect;
import group.ImageRow;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ImageDownloader extends Task<Void> {

    Map<Integer, List<String>> idTagMap;
    Map<String, Integer> directorySplits;
    final String csvName;
    final String format;
    final String delimiter;

    public ImageDownloader(Map<Integer, List<String>> idTagMap, String csvName, String format, String delimiter, Map<String, Integer> directorySplits) {
        this.idTagMap = idTagMap;
        this.directorySplits = directorySplits;
        this.csvName = csvName;
        this.format = format;
        this.delimiter = delimiter;
    }

    public ImageDownloader(Map<Integer, List<String>> idTagMap, String csvName, String format, String delimiter, List<String> directories, Integer... splits) {
        this.idTagMap = idTagMap;
        this.format = format;
        this.delimiter = delimiter;
        this.csvName = csvName;
        this.directorySplits = new HashMap<>();
        try {
            for (int i = 0; i < directories.size(); i++)
                directorySplits.put(directories.get(i), splits[i]);
        } catch (Exception e) {
            System.err.println("Image downloader initialization failed, directories probably don't match splits");
        }
    }

    @Override
    protected Void call() throws Exception {
        List<Integer> keys = new ArrayList<>(idTagMap.keySet().stream().toList());
        long workDone = 0;
        long workMax = keys.size();
        updateProgress(workDone, workMax);
        try {
            Collections.shuffle(keys);
            for (String directory : directorySplits.keySet()) {

                String csvFileName = csvName.lastIndexOf('.') == -1 ? csvName + ".csv" : csvName.substring(csvName.lastIndexOf('.')) + "csv";
                boolean appending = Files.exists(Path.of(directory + csvFileName));
                FileOutputStream csvOutput = new FileOutputStream(directory + csvFileName, true);
                if (appending)
                    csvOutput.write('\n');

                Integer numberOfImages = directorySplits.get(directory);
                for (int i = 0; i < numberOfImages; i++) {
                    Integer id = keys.remove(0);
                    List<String> tags = idTagMap.remove(id);
                    ImageRow temporary = DBConnect.getImage(id);
                    File tempFile = new File(directory + temporary.getId() + "." + format);
                    try (InputStream stream = temporary.getImage()) {
                        BufferedImage bufferedImage = ImageIO.read(stream);
                        ImageIO.write(bufferedImage, format, tempFile);
                        csvOutput.write((temporary.getId()+"."+format
                                + tags.stream().reduce("", (s, s2) -> s + delimiter + s2))
                                .getBytes(Charset.defaultCharset()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateProgress(workDone++, workMax);
                }
                csvOutput.close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
