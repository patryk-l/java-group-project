module group {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens group to javafx.fxml;
    exports group;
}
