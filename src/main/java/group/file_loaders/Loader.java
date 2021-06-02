package group.file_loaders;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

}
