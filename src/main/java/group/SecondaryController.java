package group;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import group.image_handlers.ImageDownloader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    public ListView tagsListView;
    public ComboBox fileFormatComboBox;
    public TextField delimiterTextField;
    public Button addTagButton;
    public Button deleteTagButton;
    private Random randomGenerator = new Random();
    private int loopCounter;


    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    public void initialize() throws SQLException {
        //ConnectToDB();
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
                    errorText.setText("B????dna liczba obrazk??w w setach");
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
                     errorText.setText("B????dna liczba obrazk??w w setach");
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
                    errorText.setText("B????dna liczba obrazk??w w setach");
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
                    errorText.setText("B????dna liczba obrazk??w w setach");
                } else {
                    errorText.setText("");
                }
            }
        });
        tagsListView.getItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                List<String> selectedTagNames = tagsListView.getItems();
                List<Integer> selectedTagIds = new ArrayList<Integer>();
                selectedTagNames.forEach((String tagName) -> {
                    selectedTagIds.add(tags.stream().filter(tag -> tagName.equals(tag.getName())).findFirst().orElse(null).getId());
                });
                try {
                    imageIds = DBConnect.getImageIdsByTags(selectedTagIds);
                    if (!imageIds.isEmpty()) {
                        numberOfImages = imageIds.size();
                        numberOfImagesText.setText("Ilo???? obrazk??w z wybranymi tagami: " + String.valueOf(numberOfImages));
                        imageIdsIndex = 0;
                        setExampleImageView(imageIds.get(imageIdsIndex));
                        if (imageIds.size() == 1) {
                            rightArrow.setDisable(true);
                        } else {
                            rightArrow.setDisable(false);
                        }
                        cleanForm();
                    } else {
                        numberOfImagesText.setText("Ilo???? obrazk??w z wybranym tagiem: 0");
                    }


                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        });
        tagsComboBox.setOnAction((event) -> {
            Object selectedItem = tagsComboBox.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                addTagButton.setDisable(true);
            } else {
                addTagButton.setDisable(false);
            }
        });
        leftArrow.setDisable(true);
        addTagButton.setDisable(true);
        rightArrow.setDisable(true);
        deleteTagButton.disableProperty()
                .bind(tagsListView.getSelectionModel().selectedItemProperty().isNull());
    }

    public void cleanForm() {
        maxNumberOfImagesTF.setText("0");
        numberOfImagesTrainingSetTF.setText("0");
        numberOfImagesValidationSetTF.setText("0");
        numberOfImagesTestSetTF.setText("0");
        trainingDirPath = "";
        trainingSetPathText.setText(trainingDirPath);
        validationDirPath = "";
        validationSetPathText.setText(validationDirPath);
        testDirPath = "";
        testSetPathText.setText(testDirPath);
        progressBar.setProgress(0);
    }

    public void setExampleImageView(int imageId) throws SQLException {
        ImageRow exampleImageRow = DBConnect.getImage(imageId);
        exampleImageView.setImage(new Image(exampleImageRow.getImage()));
    }

    public void setComboBoxData() throws SQLException {
        tags = new ArrayList<TagRow>(DBConnect.getAllTags());
        List<String> nameList = tags.stream().map(TagRow::getName).collect(Collectors.toList());
        tagsComboBox.getItems().addAll(nameList);
        fileFormatComboBox.setItems(FXCollections.observableList(
                Arrays.stream(ImageIO.getWriterFileSuffixes()).filter(s -> !s.equals("wbmp")).collect(Collectors.toList())));
        fileFormatComboBox.setValue("png");
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
        if (checkStuff()) {
            exportButton.setDisable(true);
        } else {
            exportButton.setDisable(false);
        }
    }

    public void openValidationDirDialog(ActionEvent actionEvent) {
        validationDirPath = openDirDialog();
        validationSetPathText.setText(validationDirPath);
        if (checkStuff()) {
            exportButton.setDisable(true);
        } else {
            exportButton.setDisable(false);
        }
    }

    public void openTestDirDialog(ActionEvent actionEvent) {
        testDirPath = openDirDialog();
        testSetPathText.setText(testDirPath);
        if (checkStuff()) {
            exportButton.setDisable(true);
        } else {
            exportButton.setDisable(false);
        }
    }

    public void exportImages(MouseEvent mouseEvent) {
        List<Integer> imageIds2 = new ArrayList<Integer>(imageIds);
        loopCounter=0;
        progressBar.setProgress(0);
        int numberOfImagesTrainingSet = Integer.parseInt(numberOfImagesTrainingSetTF.getText());
        int numberOfImagesValidationSet = Integer.parseInt(numberOfImagesValidationSetTF.getText());
        int numberOfImagesTestSet = Integer.parseInt(numberOfImagesTestSetTF.getText());
        progressBar.setStyle("-fx-accent: green;");
        exportButton.setDisable(true);

        List<String> directoryList = List.of(trainingDirPath,validationDirPath,testDirPath).
                stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
        if(directoryList.stream().distinct().count()!=
            directoryList.stream().count()){
            errorText.setText("Foldery musz?? by?? r????ne");
            return;
        }

        //just a workaround
        Integer[] varargs = new Integer[3];
        if(!trainingDirPath.isBlank()){
            varargs[0] = numberOfImagesTrainingSet;
            if(!validationDirPath.isBlank()){
                varargs[1] = numberOfImagesValidationSet;
                varargs[2] = numberOfImagesTestSet;
            }else
                varargs[1] = numberOfImagesTestSet;
        }else{
            if(!validationDirPath.isBlank()){
                varargs[0]=numberOfImagesValidationSet;
                varargs[1]=numberOfImagesTestSet;
            }else
                varargs[0]=numberOfImagesTestSet;
        }

        ImageDownloader downloader = new ImageDownloader(
                DBConnect.mapImagesToStringMap(selectedTagsToStringList(),imageIds2),
                "tags",getFormatFromComboBox(),getDelimiterFromTextBox(),
                directoryList,varargs
                );


        /*ImageDownloader downloader = new ImageDownloader(
                DBConnect.mapImagesToStringMap(selectedTagsToStringList(),imageIds2),
                "tags",getFormatFromComboBox(),getDelimiterFromTextBox(),
                directoryList,
                numberOfImagesTrainingSet,numberOfImagesValidationSet,numberOfImagesTestSet);*/

        progressBar.progressProperty().bind(downloader.progressProperty());

        downloader.setOnSucceeded(workerStateEvent -> {progressBar.progressProperty().unbind(); progressBar.setProgress(1);exportButton.setDisable(false);});
        downloader.setOnScheduled(workerStateEvent -> {});
        downloader.setOnFailed(workerStateEvent -> {progressBar.progressProperty().unbind(); progressBar.setStyle("-fx-accent: red;");exportButton.setDisable(false);});
        Thread th = new Thread(downloader);
        th.setDaemon(false);
        th.start();

        /*saveRandomImages(numberOfImagesTrainingSet, imageIds2, trainingDirPath);
        saveRandomImages(numberOfImagesValidationSet, imageIds2, validationDirPath);
        saveRandomImages(numberOfImagesTestSet, imageIds2, testDirPath);*/
        //exportButton.setDisable(false);
        //progressBar.setProgress(1);
    }

    public void saveRandomImages(int numberOfImages, List<Integer> ids, String dirPath){
        for (int i = 0; i < numberOfImages; i++) {
            moveProgressBar();
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

    private void moveProgressBar(){
        loopCounter++;
        progressBar.setProgress((double)loopCounter/maxNumberOfImages);
    }

    public void addTag(ActionEvent actionEvent) {
        tagsListView.getItems().add(tagsComboBox.getValue());
        tagsComboBox.getItems().remove(tagsComboBox.getValue());
    }

    public void removeTagFromList(MouseEvent mouseEvent) {
        tagsComboBox.getItems().add(tagsListView.getSelectionModel().getSelectedItem());
        tagsListView.getItems().remove(tagsListView.getSelectionModel().getSelectedItem());
        if (tagsListView.getItems().size() == 0) {
            numberOfImagesText.setText("Ilo???? obrazk??w z wybranymi tagami: 0");
            exampleImageView.setImage(null);
            leftArrow.setDisable(true);
            rightArrow.setDisable(true);
        }
    }

    public Map<String,String> tagsToStringMap(){
        List<String> list = tagsListView.getItems();
        return list.stream().collect(Collectors.toConcurrentMap(Function.identity(),Function.identity()));
    }

    public List<String> selectedTagsToStringList(){
        return tagsListView.getItems();
    }

    public String getFormatFromComboBox(){
        return (String)fileFormatComboBox.getSelectionModel().getSelectedItem();
    }

    public String getDelimiterFromTextBox(){
        return delimiterTextField.getText();
    }

    public boolean checkStuff(){
        return ((trainingDirPath.equals("") && validationDirPath.equals("") && testDirPath.equals(""))
                ||(trainingDirPath.equals("")&&Integer.parseInt(numberOfImagesTrainingSetTF.getText()) != 0)
                ||(validationDirPath.equals("")&&Integer.parseInt(numberOfImagesValidationSetTF.getText()) != 0)
                ||(testDirPath.equals("")&&Integer.parseInt(numberOfImagesTestSetTF.getText()) != 0));
    }

    //use at your own risk
    public void deleteImages(ActionEvent actionEvent) {
        List<String> tags = selectedTagsToStringList();
        Integer deletedImages = 0;
        for(String tag : tags){
            try {
                deletedImages+=DBConnect.deleteByTag(tag);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        tagsListView.getItems().clear();
        exampleImageView.setImage(null);
        leftArrow.setDisable(true);
        rightArrow.setDisable(true);
        numberOfImagesText.setText("Ilo???? obrazk??w z wybranymi tagami: 0");
        errorText.setText("Deleted " + deletedImages);
        try {
            tagsComboBox.getItems().clear();
            setComboBoxData();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}