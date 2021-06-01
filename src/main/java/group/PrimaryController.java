package group;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import group.file_loaders.Loader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

import javax.imageio.ImageIO;

public class PrimaryController {

    public Button testButton;
    public TextArea textFieldTest;
    public ImageView imageView;
    DirectoryChooser chooser = new DirectoryChooser();
    File chosenDirectory  = null;

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    public void doSomething(ActionEvent actionEvent) {
        List<Path> paths = null;
        textFieldTest.clear();
        if(chosenDirectory==null)
            return;
        try {
            paths = Loader.listImages(chosenDirectory.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        for(Path path : paths)
            textFieldTest.setText(path.toString()+'\n'+textFieldTest.getText());

        //imageView.setImage(Loader.loadToGraphics2D(paths.get(0)));
        try {
            Image image = new Image(Files.newInputStream(paths.get(0)));
            imageView.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDirDialog(ActionEvent actionEvent) {
        chosenDirectory = chooser.showDialog(null);
        /*if(chosenDirectory!=null)
            System.out.println(chosenDirectory.getPath());*/
    }

    public void printFormats(ActionEvent actionEvent) {
        textFieldTest.clear();
        String[] suffixes= ImageIO.getReaderFileSuffixes();
        for(String suffix  : suffixes)
            textFieldTest.setText(suffix+'\n'+textFieldTest.getText());
    }
    public void ConnectToDB() {
        System.out.println("test");
        try {
            DBConnect.connectToDB();
            System.out.println("Connected with database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void InsertImage(){
        List<Path> paths = null;
        if(chosenDirectory==null)
            return;
        try {
            paths = Loader.listImages(chosenDirectory.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //imageView.setImage(Loader.loadToGraphics2D(paths.get(0)));
        try {
            ImageRow imageRow=new ImageRow(new File(String.valueOf(paths.get(0))));
            DBConnect.insertImage(imageRow);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }
    public void showImage() {
        ImageRow imageRow = null;
        try {
            imageRow = DBConnect.getImage(2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(imageRow==null) return;
        Image image;
        image = new Image(imageRow.getImage());
        imageView.setImage(image);
        System.out.println(imageRow.getSize());
    }
}
