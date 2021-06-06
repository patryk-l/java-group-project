package group;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import javax.imageio.ImageIO;

public class SecondaryController<event> {
    public Button downloadButton;
    public ComboBox tagsComboBox;
    public ImageView exampleImageView;
    public Text numberOfImagesText;
    public TextField maxNumberOfImagesTF;
    public TextField numberOfImagesTrainingSetTF;
    public TextField numberOfImagesValidationSetTF;
    public TextField numberOfImagesTestSetTF;
    String path;
    public List<TagRow> tags;
    public int numberOfImages;


    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    public void initialize() throws SQLException {
        ConnectToDB();
        setComboBoxData();
        maxNumberOfImagesTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    maxNumberOfImagesTF.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (newValue.isEmpty() != true) {
                    if (Integer.parseInt(newValue) > numberOfImages) {
                        maxNumberOfImagesTF.setText(newValue.replaceAll("[\\d]", "0"));
                    }
                }
            }
        });
        numberOfImagesTrainingSetTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    numberOfImagesTrainingSetTF.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        numberOfImagesValidationSetTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    numberOfImagesValidationSetTF.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        numberOfImagesTestSetTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    numberOfImagesTestSetTF.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        tagsComboBox.setOnAction((event) -> {
            String selectedTagName = (String) tagsComboBox.getValue();
            TagRow selectedTagRow = tags.stream().filter(tag -> selectedTagName.equals(tag.getName())).findFirst().orElse(null);
            try {
                List<Integer> imageIds = DBConnect.getImageIdsByTag(selectedTagRow.getId());
                if (!imageIds.isEmpty()) {
                    numberOfImages = imageIds.size();
                    numberOfImagesText.setText("Ilość obrazków z wybranym tagiem: " + String.valueOf(numberOfImages));
                    ImageRow exampleImageRow = DBConnect.getImage(imageIds.get(0));
                    exampleImageView.setImage(new Image(exampleImageRow.getImage()));
                } else {
                    numberOfImagesText.setText("Ilość obrazków z wybranym tagiem: 0");
                }


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public void setComboBoxData() throws SQLException {
        tags = new ArrayList<TagRow>(DBConnect.getAllTags());
        List<String> nameList = tags.stream().map(TagRow::getName).collect(Collectors.toList());
        tagsComboBox.getItems().addAll(nameList);
    }

    public void saveListOfImagesByTagsToDirectory(List<Integer> tags){
        for(int i=0;i<tags.size();i++){
            try {
                ImageRow temporary=DBConnect.getImage(tags.get(i));
                File tempFile=new File(path+temporary.getId()+".png");
                BufferedImage bufferedImage=ImageIO.read(temporary.getImage());
                ImageIO.write(bufferedImage,"png",tempFile);
                exampleImageView.setImage(new Image(temporary.getImage()));
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