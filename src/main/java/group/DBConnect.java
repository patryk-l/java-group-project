package group;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DBConnect {
    private static Connection connection = null;

    public static void connectToDB() throws SQLException {
        String url = "jdbc:mysql://localhost/MachineLearning";
        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "");
        try {
            connection = DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            System.out.println("Error when connecting to database");
            e.printStackTrace();
            throw e;
        }
    }

    public static void connectToDB(Properties properties) throws SQLException {
        //String url = "jdbc:mysql://localhost/MachineLearning";
        try {
            //connection = DriverManager.getConnection(url, properties);
            connection = DriverManager.getConnection(properties.getProperty("db.url"),properties.getProperty("db.user"),properties.getProperty("db.password"));
        } catch (SQLException e) {
            System.out.println("Error when connecting to database");
            e.printStackTrace();
            throw e;
        }
    }

    public static void disconnectDB() throws SQLException {
        try {
            connection.close();
            connection = null;
            System.out.println("Disconnected from database");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

    }

    public static void executeJDML(String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (!statement.isClosed()) {
                statement.close();
            }
        }
    }

    public static void insertImage(ImageRow imageRow) throws SQLException, FileNotFoundException {
        Statement statement = null;
        InputStream fin = imageRow.getImage();
        statement = connection.createStatement();
        PreparedStatement pre = connection.prepareStatement("insert into images(file, file_size, image_width, image_height) values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        pre.setBinaryStream(1, fin);
        pre.setInt(2, imageRow.getSize());
        pre.setInt(3, imageRow.getWidth());
        pre.setInt(4, imageRow.getHeight());
        pre.executeUpdate();
        ResultSet set = pre.getGeneratedKeys();
        if (!set.next())
            throw new SQLException();
        imageRow.setId(set.getInt(1));
        pre.close();
    }

    public static ImageRow getImage(int id) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from images where id=" + id + ";");
        ImageRow imageRow = new ImageRow();
        if (resultSet.next()) {
            imageRow.setId(resultSet.getInt("id"));
            imageRow.setImage(resultSet.getBinaryStream("file"));
            imageRow.setSize(resultSet.getInt("file_size"));
            imageRow.setWidth(resultSet.getInt("image_width"));
            imageRow.setHeight(resultSet.getInt("image_height"));
        } else return null;
        return imageRow;
    }

    public static ResultSet getData(String sql) throws SQLException {
        connectToDB();
        Statement statement = null;
        ResultSet resultSet = null;
        statement = connection.createStatement();
        resultSet = statement.executeQuery(sql);
        return resultSet;
    }

    public static Map<String, List<Integer>> updateTagsAndGetIds(Map<String, List<String>> fileTagMap) throws SQLException {
        Set<String> tagSet = fileTagMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from tags");
            //Map<String, Integer> tagMap = new HashMap<>(resultSet.getInt("count(*)"));
            Map<String, Integer> tagMap = new HashMap<>();
            while (resultSet.next()) {
                String tag = resultSet.getString("name");
                Integer id = resultSet.getInt("id");
                if (tagSet.remove(tag)){
                    tagMap.put(tag, id);
                }

            }
            for (String tag : tagSet) {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into tags(name) values (?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, tag);
                preparedStatement.execute();
                ResultSet rsID = preparedStatement.getGeneratedKeys();
                if (!rsID.next())
                    throw new SQLException();
                tagMap.put(tag, rsID.getInt(1));
                preparedStatement.close();
            }
            Map<String, List<Integer>> fileTagIdMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : fileTagMap.entrySet()) {
                fileTagIdMap.put(entry.getKey(), entry.getValue().stream().map(s -> tagMap.get(s)).collect(Collectors.toList()));
            }
            return fileTagIdMap;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw throwables;
        }
    }

    public static void insertImageTag(Integer imageId, Integer tagId) throws SQLException {
        String sql = "insert into images_tags(image_id,tag_id) values(?,?)";
        try {
            PreparedStatement pre = connection.prepareStatement(sql);
            pre.setInt(1, imageId);
            pre.setInt(2, tagId);
            pre.execute();
            pre.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error while executing: " + sql);
            throw e;
        }
    }

    public static Integer deleteByTag(String tag) throws SQLException {
        String sql = "select images.id,tags.id " +
                "from images join images_tags it on images.id = it.image_id join tags on it.tag_id = tags.id " +
                "where tags.name = ?";
        Integer images_delete = 0;
        try(PreparedStatement pre = connection.prepareStatement(sql)){
            pre.setString(1,tag);
            ResultSet resultSet = pre.executeQuery();
            while(resultSet.next()){
                Statement statement = connection.createStatement();
                statement.executeBatch();
                statement.addBatch("delete from images where id="+resultSet.getInt(1));
                statement.addBatch("delete from tags where id="+resultSet.getInt(2));
                statement.addBatch("delete from images_tags where image_id="+resultSet.getInt(1));
                images_delete += statement.executeBatch()[0];
                statement.close();
            }
        } catch (SQLException e) {
            System.err.println("Error while attempting to delete by tags");
            e.printStackTrace();
            throw e;
        }

        return images_delete;
    }

    public static List<Integer> queryImageIdsByTag(String tag) throws SQLException {
        String sql = "select images.id" +
                " from images join images_tags on images.id = images_tags.image_id join tags on images_tags.tag_id = tags.id " +
                "where tags.name = ";
        try(PreparedStatement pre = connection.prepareStatement(sql+"\""+tag+"\"")){
            ResultSet resultSet = pre.executeQuery();
            List<Integer> list = new ArrayList<>(resultSet.getInt("count(*)"));
            while(resultSet.next())
                list.add(resultSet.getInt(1));
            return list;
        } catch (SQLException e) {
            System.err.println("Error while getting images by tag");
            e.printStackTrace();
            throw e;
        }
    }

    public static List<Integer> getImageIdsByTags(List<Integer> selectedTagIds) throws SQLException {
        String sql = "select images.id" +
                " from images join images_tags on images.id = images_tags.image_id join tags on images_tags.tag_id = tags.id ";

        for (int i = 0; i < selectedTagIds.size(); i++) {
            if (i == 0) {
                sql += "where tags.id = " + selectedTagIds.get(i);
            } else {
                sql += " or tags.id = " + selectedTagIds.get(i);
            }
        }
        sql += ";";
        try(PreparedStatement pre = connection.prepareStatement(sql)){
            ResultSet resultSet = pre.executeQuery();
            List<Integer> list = new ArrayList<>();
            while(resultSet.next())
                list.add(resultSet.getInt(1));

            return list.stream().distinct().collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Error while getting images by tag");
            e.printStackTrace();
            throw e;
        }
    }

    public static List<TagRow> getAllTags() throws SQLException {
        String sql = "select * from tags";
        try(PreparedStatement pre = connection.prepareStatement(sql)){
            ResultSet resultSet = pre.executeQuery();
            List<TagRow> list = new ArrayList<>();
            while (resultSet.next()) {
                TagRow tagRow = new TagRow();
                tagRow.setId(resultSet.getInt("id"));
                tagRow.setName(resultSet.getString("name"));
                list.add(tagRow);
            }
            return list;
        } catch (SQLException e) {
            System.err.println("Error while getting all tags");
            e.printStackTrace();
            throw e;
        }
    }

    public static Map<Integer,List<String>> mapImagesToStringMap(List<String> tagList, List<Integer> imageIds){
        if(tagList==null || tagList.isEmpty())
            throw new NullPointerException("List of tags null or empty");
        String sql = "select images.id, tags.name " +
                "from images join images_tags on images.id = images_tags.image_id join tags on images_tags.tag_id = tags.id " +
                "where tags.name in (" + tagList.stream().reduce("",(s, s2) -> s+",\""+s2 +"\"").substring(1) +")";

        Map<Integer,List<String>> map = new ConcurrentHashMap<>();
        Map<String,Integer> listIndexMap = new HashMap<>();
        for(Integer id : imageIds)
            map.put(id, new ArrayList<>());
        for(int i = 0; i < tagList.size(); i++)
            listIndexMap.put(tagList.get(i),i);
        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try (ResultSet resultSet = statement.executeQuery(sql)){
            while(resultSet.next()){
                Integer resultImageId = resultSet.getInt(1);
                String resultTagName = resultSet.getString(2);
                if(map.containsKey(resultImageId))
                    map.get(resultImageId).add(tagList.get(listIndexMap.get(resultTagName)));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }
}
