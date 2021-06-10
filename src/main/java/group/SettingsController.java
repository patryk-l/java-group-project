package group;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;


import java.io.*;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsController  {

    public TextField connectionString;
    public TextField username;
    public PasswordField password;
    public Label status;

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
    public void initialize(){
        FileInputStream inputStream=null;
        try {
            inputStream=new FileInputStream(".config");
        } catch (FileNotFoundException e) {
            connectionString.setText("localhost");
            username.setText("root");
            if(save()){
                initialize();
            }
            return;
        }
        Properties properties=new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionString.setText(properties.getProperty("db.url"));
        username.setText(properties.getProperty("db.user"));
        String pass=properties.getProperty("db.password");
        byte[] decodedBytes = Base64.getDecoder().decode(pass);
        password.setText(new String(decodedBytes));
    }
    public boolean save(){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(".config");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        Properties properties=new Properties();
        properties.setProperty("db.url", connectionString.getText());
        properties.setProperty("db.user", username.getText());
        properties.setProperty("db.password", Base64.getEncoder().encodeToString(password.getText().getBytes()));
        try {
            properties.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void CheckConnectionToDB() {
        Properties properties=new Properties();
        properties.setProperty("db.url", "jdbc:mysql://"+connectionString.getText());
        properties.setProperty("db.user", username.getText());
        properties.setProperty("db.password", password.getText());

        try {
            DBConnect.connectToDB(properties);
            status.setVisible(true);
        } catch (SQLException e) {
            status.setText("Błąd połączenia. Sprawdź poprawność wprowadzonych danych");
            status.setTextFill(Color.RED);
            return;
        }
        status.setText("Połączenie nawiązane");
        status.setTextFill(Color.GREEN);
        try {
            DBConnect.disconnectDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void PrepareDB() throws SQLException {
        Properties properties=new Properties();
        properties.setProperty("db.url", "jdbc:mysql://"+connectionString.getText());
        properties.setProperty("db.user", username.getText());
        properties.setProperty("db.password", password.getText());
        DBConnect.connectToDB(properties);
        DBConnect.executeJDML("drop schema MachineLearning;");
        DBConnect.executeJDML("create schema MachineLearning;" );
        DBConnect.disconnectDB();
        properties.setProperty("db.url", "jdbc:mysql://"+connectionString.getText()+"/MachineLearning");
        DBConnect.connectToDB(properties);
        DBConnect.executeJDML("create table images(" +
                "id int AUTO_INCREMENT," +
                "file longblob not null, " +
                "file_size int not null," +
                "image_width int not null," +
                "image_height int not null, " +
                "primary key(id));");
        DBConnect.executeJDML("create table tags(" +
                "id int auto_increment," +
                "name varchar(15)," +
                "primary key(id));");
        DBConnect.executeJDML("create table images_tags(" +
                "image_id int not null," +
                "tag_id int not null," +
                "constraint fk_image_id foreign key(image_id) references images(id)," +
                "constraint fk_tag_id foreign key(tag_id) references tags(id));");
    }
}
