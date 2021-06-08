package group;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import group.file_loaders.Loader;
import group.image_handlers.ImageUploader;
import group.file_loaders.CSVRow;
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
    public Button primaryButton;
    public Button dirButton;
    public Button saveButton;
    DirectoryChooser chooser = new DirectoryChooser();
    File chosenDirectory = null;
    List<Path> imagePaths = null;
    Path csvPath = null;
    String csvDelimiter = ";";
    boolean tagsToLowercase = true;

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    public void initialize(){
        ConnectToDB();
    }
    public void doSomething() throws URISyntaxException {
        textFieldTest.setVisible(false);
        imageView.setVisible(true);
        List<Path> paths = null;
        //Path csvPath = null;
        if (chosenDirectory == null)
            return;
        try {
            paths = Loader.listImages(chosenDirectory.getPath());
           // csvPath = Loader.getCSVPath(chosenDirectory.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //imageView.setImage(Loader.loadToGraphics2D(paths.get(0)));
        try {
            Image image = new Image(Files.newInputStream(paths.get(0)));
            imageView.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //List<CSVRow> csvRows = Loader.convertCSV(csvPath.toString());
//        for(Path path : paths)
//            textFieldTest.setText(path.toString()+'\n'+textFieldTest.getText());
//
//        //imageView.setImage(Loader.loadToGraphics2D(paths.get(0)));
//        try {
//            Image image = new Image(Files.newInputStream(paths.get(0)));
//            imageView.setImage(image);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void openDirDialog(ActionEvent actionEvent) throws URISyntaxException {
        chosenDirectory = chooser.showDialog(null);
        if(chosenDirectory!=null){
            try {
                imagePaths = Loader.listImages(chosenDirectory.getPath());
                csvPath = Loader.getCSVPath(chosenDirectory.getPath());
                //pathShower.setText(chosenDirectory.getPath() + " " + imagePaths.size());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            this.doSomething();
        }

    }

    public void printFormats(ActionEvent actionEvent) {
        imageView.setVisible(false);
        textFieldTest.setVisible(true);
        textFieldTest.clear();
        String[] suffixes = ImageIO.getReaderFileSuffixes();
        for (String suffix : suffixes)
            textFieldTest.setText(suffix + '\n' + textFieldTest.getText());
    }

    public void ConnectToDB() {
        try {
            DBConnect.connectToDB();
            System.out.println("Connected with database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void InsertImage() {
        List<Path> paths = null;
        if (chosenDirectory == null)
            return;
        try {
            paths = Loader.listImages(chosenDirectory.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //imageView.setImage(Loader.loadToGraphics2D(paths.get(0)));
        try {
            ImageRow imageRow = new ImageRow(new File(String.valueOf(paths.get(0))));
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
        if (imageRow == null) return;
        Image image;
        image = new Image(imageRow.getImage());
        imageView.setImage(image);
    }

    public void uploadImages(ActionEvent actionEvent) {
        Map<File, List<Integer>> map = new HashMap<>();
        if (imagePaths == null)
            return;
        try {
            try {
               // Map<String, List<Integer>> initialMap = DBConnect.updateTagsAndGetIds(Loader.readCSV(csvPath.toString(), csvDelimiter, tagsToLowercase));
                Map<String, List<Integer>> initialMap = DBConnect.updateTagsAndGetIds(Loader.readCSV(chosenDirectory.getPath()+ File.separator +"1.csv", csvDelimiter, tagsToLowercase));
                for (Map.Entry<String, List<Integer>> entry : initialMap.entrySet()) {
                    if (entry.getKey().contains(File.separator))
                        map.put(new File(entry.getKey()), entry.getValue());
                    else
                        map.put(new File(chosenDirectory + File.separator + entry.getKey()), entry.getValue());
                }
            } catch (SQLException e) {
                System.err.println("Error during tag updating");
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error during path processing");
                e.printStackTrace();
            }

            //Loader.convertToFileList(imagePaths).forEach(file -> map.put(file,null));
            ImageUploader uploader = new ImageUploader(map);
            progressBar.progressProperty().bind(uploader.progressProperty());
            progressBar.setStyle("-fx-accent: green;");

            uploader.setOnSucceeded(workerStateEvent -> {progressBar.progressProperty().unbind(); progressBar.setProgress(1);restoreButtons();});
            uploader.setOnScheduled(workerStateEvent -> blockButtons());
            uploader.setOnFailed(workerStateEvent -> {progressBar.progressProperty().unbind(); progressBar.setProgress(0);restoreButtons();});

            Thread th = new Thread(uploader);
            th.setDaemon(true);
            th.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When this is invoked make buttons unclickable
     **/
    public void blockButtons() {
        primaryButton.setDisable(true);
        testButton.setDisable(true);
        dirButton.setDisable(true);
        saveButton.setDisable(true);
    }

    public void restoreButtons() {
        primaryButton.setDisable(false);
        testButton.setDisable(false);
        dirButton.setDisable(false);
        saveButton.setDisable(false);
    }

    public void goToTest(ActionEvent actionEvent) {
        try {
            App.setRoot("testLayout");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
