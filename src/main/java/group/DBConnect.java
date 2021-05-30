package group;

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
            System.out.println("Zamknięto połączenie z bazą danych studenci");
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
    public static ResultSet getData(String sql) throws SQLException {
        connectToDB();
        Statement statement=null;
        ResultSet resultSet=null;
        statement=connection.createStatement();
        resultSet=statement.executeQuery(sql);
        return resultSet;
    }
}
