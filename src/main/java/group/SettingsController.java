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
            connectionString.setText("jdbc:mysql://localhost/MachineLearning");
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
        properties.setProperty("db.url", connectionString.getText());
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
}
