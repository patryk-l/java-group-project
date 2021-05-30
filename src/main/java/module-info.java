module group {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;

    opens group to javafx.fxml;
    exports group;
}
