package group;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import group.file_loaders.Loader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;

import javax.imageio.ImageIO;

public class SecondaryController {
    public Button downloadButton;
    String path;

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    public void initialize(){
        ConnectToDB();
    }

    public void saveListOfImagesByTagsToDirectory(List<Integer> tags){
        for(int i=0;i<tags.size();i++){
            try {
               ImageRow temporary=DBConnect.getImage(tags.get(i));
               File tempFile=new File(path+temporary.getId()+".png");
               BufferedImage bufferedImage=ImageIO.read(temporary.getImage());
               ImageIO.write(bufferedImage,"png",tempFile);
              // Files.copy(temporary.getImage(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        downloadButton.setDisable(true);
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

    public void openDirDialog(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser=new DirectoryChooser();
        File tempFile=directoryChooser.showDialog(null);
        if(tempFile!=null){
            path = tempFile.getPath() + "/";
            downloadButton.setDisable(false);
        }else {
            path="";
            downloadButton.setDisable(true);
        }
    }

    public void testFunction(){
        List<Integer> intList=new ArrayList<>();
        intList.add(2);
        intList.add(3);
        if(!path.equals("")){
            saveListOfImagesByTagsToDirectory(intList);
        }
    }
}