package group;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import group.file_loaders.Loader;
import group.image_handlers.ImageUploader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

import javax.imageio.ImageIO;

public class PrimaryController {

    public Button testButton;
    public TextArea textFieldTest;
    public ImageView imageView;
    public ProgressBar progressBar;
    public TextField pathShower;
    DirectoryChooser chooser = new DirectoryChooser();
    File chosenDirectory  = null;
    List<Path> paths = null;

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
        try {
            paths = Loader.listImages(chosenDirectory.getPath());
            pathShower.setText(chosenDirectory.getPath() + " " + paths.size());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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

    public void uploadImages(ActionEvent actionEvent) {
        Map<File, List<Integer>> map = new HashMap<>();
        if(paths == null)
            return;
        try {
            //List<File> files =
            Loader.convertToFileList(paths).forEach(file -> map.put(file,null));
            ImageUploader uploader = new ImageUploader(map);

            progressBar.progressProperty().bind(uploader.progressProperty());
            progressBar.setStyle("-fx-accent: green;");

            uploader.setOnSucceeded(workerStateEvent -> restoreButtons());
            uploader.setOnScheduled(workerStateEvent -> blockButtons());

            Thread th = new Thread(uploader);
            th.setDaemon(true);
            th.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When this is invoked make buttons unclickable
     * **/
    public void blockButtons(){

    }

    public void restoreButtons(){

    }
}
