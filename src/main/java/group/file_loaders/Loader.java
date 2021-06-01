package group.file_loaders;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Loader {

    public static BufferedImage loadToBufferedImage(Path path) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(path.toFile());
        //InputStream stream = Files.newInputStream(path);
        return bufferedImage;
    }

    public static byte[] getByteArray(BufferedImage image){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image,"bmp",outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Unfinished
    public static BufferedImage getBufferedImageFromByteArray(byte[] bytes){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return null;
    }

    public static List<Path> listImages(String path) throws URISyntaxException {
        List<Path> list = new ArrayList<>();
        Path directoryPath = Path.of(path);
        String[] suffixes = ImageIO.getReaderFileSuffixes();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            for (Path file: stream) {
                if(Arrays.stream(suffixes).anyMatch(s -> s.equals(file.toString().substring(file.toString().lastIndexOf('.')+1))))
                    list.add(file);
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }
        return list;
    }

    public static Path getCSVPath(String path) throws URISyntaxException {
        Path csvPath = null;
        Path directoryPath = Path.of(path);
        String csvSuffix = "csv";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            for (Path file: stream) {
                if(csvSuffix.equals(file.toString().substring(file.toString().lastIndexOf('.')+1)))
                    csvPath = file;
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }
        return csvPath;
    }

    public static List<CSVRow> convertCSV(String path) throws URISyntaxException {
        List<CSVRow> csvRows = new ArrayList<>();
        String FieldDelimiter = ";";
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(path));
            String line;

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(FieldDelimiter, -1);
                csvRows.add(new CSVRow(fields[0], Arrays.copyOfRange(fields, 1, fields.length)));
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return csvRows;
    }
}

