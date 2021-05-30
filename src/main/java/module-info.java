module group {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires org.mariadb.jdbc;

    opens group to javafx.fxml;
    exports group;
}
