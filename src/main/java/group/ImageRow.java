package group;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageRow {
    private IntegerProperty id;
    private byte[] image;
    private IntegerProperty size;
    private IntegerProperty width;
    private IntegerProperty height;

    public ImageRow() {
        image=null;
        id=new SimpleIntegerProperty();
        size=new SimpleIntegerProperty();
        width=new SimpleIntegerProperty();
        height=new SimpleIntegerProperty();
        id.setValue(0);
        size.setValue(0);
        width.setValue(0);
        height.setValue(0);
    }

    public ImageRow(File file) throws IOException {
        FileInputStream fileInputStream=new FileInputStream(file);
        image=fileInputStream.readAllBytes();
        id=new SimpleIntegerProperty();
        size=new SimpleIntegerProperty();
        width=new SimpleIntegerProperty();
        height=new SimpleIntegerProperty();
        id.setValue(0);
        size.setValue(file.length() / 1024);
        InputStream targetStream = new ByteArrayInputStream(image);
        BufferedImage bufferedImage = ImageIO.read(targetStream);
        fileInputStream.close();
        width.setValue(bufferedImage.getWidth());
        height.setValue(bufferedImage.getHeight());
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public InputStream getImage() {
        return new ByteArrayInputStream(image);
    }

    public int getSize() {
        return size.get();
    }

    public IntegerProperty sizeProperty() {
        return size;
    }

    public int getWidth() {
        return width.get();
    }

    public IntegerProperty widthProperty() {
        return width;
    }

    public int getHeight() {
        return height.get();
    }

    public IntegerProperty heightProperty() {
        return height;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public void setImage(InputStream image) {
        try {
            this.image = image.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSize(int size) {
        this.size.set(size);
    }

    public void setWidth(int width) {
        this.width.set(width);
    }

    public void setHeight(int height) {
        this.height.set(height);
    }

    public byte[] getImageAsByteArray(){return image;}
}
