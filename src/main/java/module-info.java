module com.colonygenesis.colonygenesis {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.colonygenesis.core to javafx.fxml;
    exports com.colonygenesis.core;
}