package group;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DBConnect {
    private static Connection connection=null;
    public static void connectToDB() throws SQLException {
        String url="jdbc:mysql://localhost/MachineLearning";
        Properties properties = new Properties();
        properties.setProperty("user","root");
        properties.setProperty("password","1234567890");
        try {
            connection = DriverManager.getConnection(url,properties);
        } catch (SQLException e) {
            System.out.println("Error when connecting to database");
            e.printStackTrace();
            throw e;
        }
    }

    public static void disconnectDB() throws SQLException {
        try {
            connection.close();
            connection=null;
            System.out.println("Disconnected from database");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

    }
    public static void executeJDML(String sql) throws SQLException {
        Statement statement= null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            if(!statement.isClosed()){
                statement.close();
            }
        }
    }
    public static void insertImage(ImageRow imageRow) throws SQLException, FileNotFoundException {
        Statement statement = null;
        InputStream fin =imageRow.getImage();
        statement = connection.createStatement();
        PreparedStatement pre= connection.prepareStatement("insert into images(file, file_size, image_width, image_height) values(?,?,?,?)");
        pre.setBinaryStream(1,fin);
        pre.setInt(2,imageRow.getSize());
        pre.setInt(3,imageRow.getWidth());
        pre.setInt(4,imageRow.getHeight());
        pre.executeUpdate();
        pre.close();
    }
    public static ImageRow getImage(int id) throws SQLException {
        Statement statement=connection.createStatement();
        ResultSet resultSet=statement.executeQuery("select * from images where id="+id+";");
        ImageRow imageRow=new ImageRow();
        if(resultSet.next()){
            imageRow.setId(resultSet.getInt("id"));
            imageRow.setImage(resultSet.getBinaryStream("file"));
            imageRow.setSize(resultSet.getInt("file_size"));
            imageRow.setWidth(resultSet.getInt("image_width"));
            imageRow.setHeight(resultSet.getInt("image_height"));
        }else return null;
        return imageRow;
    }
    public static ResultSet getData(String sql) throws SQLException {
        connectToDB();
        Statement statement=null;
        ResultSet resultSet=null;
        statement=connection.createStatement();
        resultSet=statement.executeQuery(sql);
        return resultSet;
    }
}
