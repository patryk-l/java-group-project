package group;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import group.file_loaders.Loader;
import group.file_loaders.CSVRow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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

    public void doSomething() throws URISyntaxException {
        List<Path> paths = null;
        Path csvPath = null;
        if(chosenDirectory==null)
            return;
        try {
            paths = Loader.listImages(chosenDirectory.getPath());
            csvPath = Loader.getCSVPath(chosenDirectory.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        List<CSVRow> csvRows = Loader.convertCSV(csvPath.toString());


//        for(Path path : paths)
////            textFieldTest.setText(path.toString()+'\n'+textFieldTest.getText());
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
        this.doSomething();

        /*if(chosenDirectory!=null)
            System.out.println(chosenDirectory.getPath());*/
    }

    public void printFormats(ActionEvent actionEvent) {
        textFieldTest.clear();
        String[] suffixes= ImageIO.getReaderFileSuffixes();
        for(String suffix  : suffixes)
            textFieldTest.setText(suffix+'\n'+textFieldTest.getText());
    }
}
