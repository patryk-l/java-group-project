package group;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import javax.imageio.ImageIO;

public class SecondaryController<event> {
    public Button exportButton;
    public ComboBox tagsComboBox;
    public ImageView exampleImageView;
    public Text numberOfImagesText;
    public TextField maxNumberOfImagesTF;
    public TextField numberOfImagesTrainingSetTF;
    public TextField numberOfImagesValidationSetTF;
    public TextField numberOfImagesTestSetTF;
    public Text errorText;
    public List<Integer> imageIds;
    public Button leftArrow;
    public Button rightArrow;
    public String trainingDirPath = "";
    public String validationDirPath = "";
    public String testDirPath = "";
    public int imageIdsIndex = 0;
    public List<TagRow> tags;
    public int numberOfImages;
    public int maxNumberOfImages;
    public Text trainingSetPathText;
    public Text validationSetPathText;
    public Text testSetPathText;
    public ProgressBar progressBar;
    private Random randomGenerator = new Random();


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
                if (newValue.isEmpty() != true && Integer.parseInt(newValue) > numberOfImages) {
                        maxNumberOfImagesTF.setText(newValue.replaceAll("[\\d]", "0"));
                }

                if (newValue.isEmpty() != true) {
                    maxNumberOfImages = Integer.parseInt(newValue);
                }
                if (newValue.isEmpty() || numberOfImagesTrainingSetTF.getText().isEmpty() || numberOfImagesValidationSetTF.getText().isEmpty() || numberOfImagesTestSetTF.getText().isEmpty() ||
                        ( Integer.parseInt(newValue) < (Integer.parseInt(numberOfImagesTrainingSetTF.getText()) + Integer.parseInt(numberOfImagesValidationSetTF.getText()) +
                                Integer.parseInt(numberOfImagesTestSetTF.getText()) ) )) {
                    errorText.setText("Błędna liczba obrazków w setach");
                } else {
                    errorText.setText("");
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
                 if (maxNumberOfImagesTF.getText().isEmpty() || newValue.isEmpty() || numberOfImagesValidationSetTF.getText().isEmpty() || numberOfImagesTestSetTF.getText().isEmpty() ||
                         ( Integer.parseInt(maxNumberOfImagesTF.getText()) < (Integer.parseInt(newValue) + Integer.parseInt(numberOfImagesValidationSetTF.getText()) +
                          Integer.parseInt(numberOfImagesTestSetTF.getText()) ) )) {
                     errorText.setText("Błędna liczba obrazków w setach");
                 } else {
                     errorText.setText("");
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
                if (maxNumberOfImagesTF.getText().isEmpty() || numberOfImagesTrainingSetTF.getText().isEmpty() || newValue.isEmpty() || numberOfImagesTestSetTF.getText().isEmpty() ||
                        ( Integer.parseInt(maxNumberOfImagesTF.getText()) < (Integer.parseInt(numberOfImagesTrainingSetTF.getText()) + Integer.parseInt(newValue) +
                                Integer.parseInt(numberOfImagesTestSetTF.getText()) ) )) {
                    errorText.setText("Błędna liczba obrazków w setach");
                } else {
                    errorText.setText("");
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
                if (maxNumberOfImagesTF.getText().isEmpty() || numberOfImagesTrainingSetTF.getText().isEmpty() || numberOfImagesValidationSetTF.getText().isEmpty() || newValue.isEmpty() ||
                        ( Integer.parseInt(maxNumberOfImagesTF.getText()) < (Integer.parseInt(numberOfImagesTrainingSetTF.getText()) + Integer.parseInt(numberOfImagesValidationSetTF.getText()) +
                                Integer.parseInt(newValue) ) )) {
                    errorText.setText("Błędna liczba obrazków w setach");
                } else {
                    errorText.setText("");
                }
            }
        });
        tagsComboBox.setOnAction((event) -> {
            String selectedTagName = (String) tagsComboBox.getValue();
            TagRow selectedTagRow = tags.stream().filter(tag -> selectedTagName.equals(tag.getName())).findFirst().orElse(null);
            try {
                imageIds = DBConnect.getImageIdsByTag(selectedTagRow.getId());
                if (!imageIds.isEmpty()) {
                    numberOfImages = imageIds.size();
                    numberOfImagesText.setText("Ilość obrazków z wybranym tagiem: " + String.valueOf(numberOfImages));
                    setExampleImageView(imageIds.get(imageIdsIndex));
                    rightArrow.setDisable(false);
                } else {
                    numberOfImagesText.setText("Ilość obrazków z wybranym tagiem: 0");
                }


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        leftArrow.setDisable(true);
        rightArrow.setDisable(true);
    }

    public void setExampleImageView(int imageId) throws SQLException {
        ImageRow exampleImageRow = DBConnect.getImage(imageId);
        exampleImageView.setImage(new Image(exampleImageRow.getImage()));
    }

    public void setComboBoxData() throws SQLException {
        tags = new ArrayList<TagRow>(DBConnect.getAllTags());
        List<String> nameList = tags.stream().map(TagRow::getName).collect(Collectors.toList());
        tagsComboBox.getItems().addAll(nameList);
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

    public String openDirDialog() {
        String path;
        DirectoryChooser directoryChooser=new DirectoryChooser();
        File tempDirectory=directoryChooser.showDialog(null);
        if(tempDirectory!=null){
            path = tempDirectory.getPath() + "/";
        }else {
            path="";
        }
        return path;
    }

    public void onLeftArrowClick(ActionEvent actionEvent) throws SQLException {
        if (imageIdsIndex != 0) {
            rightArrow.setDisable(false);
            imageIdsIndex--;
            if (imageIdsIndex == 0) {
                leftArrow.setDisable(true);
            }
            setExampleImageView(imageIds.get(imageIdsIndex));
        }
    }

    public void onRightArrowClick(ActionEvent actionEvent) throws SQLException {
        if (imageIdsIndex != (imageIds.size() - 1)) {
            leftArrow.setDisable(false);
            imageIdsIndex++;
            if (imageIdsIndex == (imageIds.size() - 1)) {
                rightArrow.setDisable(true);
            }
            setExampleImageView(imageIds.get(imageIdsIndex));
        }
    }

    public void openTrainingDirDialog(ActionEvent actionEvent) {
        trainingDirPath = openDirDialog();
        trainingSetPathText.setText(trainingDirPath);
        if (trainingDirPath == "" || validationDirPath == "" || testDirPath == "") {
            exportButton.setDisable(true);
        } else {
            exportButton.setDisable(false);
        }
    }

    public void openValidationDirDialog(ActionEvent actionEvent) {
        validationDirPath = openDirDialog();
        validationSetPathText.setText(validationDirPath);
        if (trainingDirPath == "" || validationDirPath == "" || testDirPath == "") {
            exportButton.setDisable(true);
        } else {
            exportButton.setDisable(false);
        }
    }

    public void openTestDirDialog(ActionEvent actionEvent) {
        testDirPath = openDirDialog();
        testSetPathText.setText(testDirPath);
        if (trainingDirPath == "" || validationDirPath == "" || testDirPath == "") {
            exportButton.setDisable(true);
        } else {
            exportButton.setDisable(false);
        }
    }

    public void exportImages(MouseEvent mouseEvent) {
        List<Integer> imageIds2 = new ArrayList<Integer>(imageIds);
        int numberOfImagesTrainingSet = Integer.parseInt(numberOfImagesTrainingSetTF.getText());
        int numberOfImagesValidationSet = Integer.parseInt(numberOfImagesValidationSetTF.getText());
        int numberOfImagesTestSet = Integer.parseInt(numberOfImagesTestSetTF.getText());
        progressBar.setStyle("-fx-accent: green;");
        exportButton.setDisable(true);

        saveRandomImages(numberOfImagesTrainingSet, imageIds2, trainingDirPath);
        saveRandomImages(numberOfImagesValidationSet, imageIds2, validationDirPath);
        saveRandomImages(numberOfImagesTestSet, imageIds2, testDirPath);
        exportButton.setDisable(false);
        progressBar.setProgress(1);
    }

    public void saveRandomImages(int numberOfImages, List<Integer> ids, String dirPath){
        for (int i = 0; i < numberOfImages; i++) {
            int index = randomGenerator.nextInt(ids.size());
            int randomId = ids.get(index);
            saveImageByIdToDirectory(randomId, dirPath);
            ids.remove(index);
        }
    }

    public void saveImageByIdToDirectory(int randomId, String directoryPath){
        try {
            ImageRow temporary=DBConnect.getImage(randomId);
            File tempFile=new File(directoryPath+temporary.getId()+".png");
            BufferedImage bufferedImage=ImageIO.read(temporary.getImage());
            ImageIO.write(bufferedImage,"png",tempFile);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}