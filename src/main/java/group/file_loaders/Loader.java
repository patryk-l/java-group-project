package group.file_loaders;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Loader {

    public static BufferedImage loadToBufferedImage(Path path) throws IOException {
        return ImageIO.read(path.toFile());
    }

    public static byte[] getByteArray(BufferedImage image){
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image,"bmp",outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage getBufferedImageFromByteArray(byte[] bytes) throws IOException {
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(inputStream);
        }
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

    public static List<File> convertToFileList(List<Path> paths){
        return paths.stream().map(Path::toFile).collect(Collectors.toList());
    }

    public static Map<String, List<String>> readCSV(String path, String delimiter,boolean lowerCase){
        BufferedReader br;
        Map<String,List<String>> map = new HashMap<>();
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null && !(line = line.strip()).equals("")) {
                String[] fields = line.split(delimiter, -1);
                System.out.println(fields[0]+" "+fields[1]);
                List<String> lines=new ArrayList();
                lines.add(fields[0]);
                lines.add(fields[1]);
                //List<String> lines = Arrays.stream(fields).map(String::strip).filter(String::isEmpty).collect(Collectors.toList());
                System.out.println(lines.get(1));
                if(lowerCase)
                    map.put(lines.get(0),lines.subList(1,lines.size()).stream().map(String::toLowerCase).collect(Collectors.toList()));
                else
                    map.put(lines.get(0),lines.subList(1, lines.size()));
            }
            return map;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return map;
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

