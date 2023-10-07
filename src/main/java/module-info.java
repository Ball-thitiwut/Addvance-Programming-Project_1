module se233.javaproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires net.coobird.thumbnailator;
    requires javafx.swing;
    exports  se233.javaproject;
    exports se233.javaproject.view;

    opens se233.javaproject to javafx.fxml;
    opens se233.javaproject.view to javafx.fxml;

}