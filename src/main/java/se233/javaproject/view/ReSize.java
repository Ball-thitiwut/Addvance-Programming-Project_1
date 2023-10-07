package se233.javaproject.view;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import net.coobird.thumbnailator.Thumbnails;
import se233.javaproject.Data;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReSize extends MainView implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;
    Data data = Data.getInstance();
    @FXML
    private Slider HeightSlider;
    @FXML
    private Slider WidthSlider;
    @FXML
    private Slider PercentageSlider;
    @FXML
    private MenuButton Format;
    @FXML
    public void confirmButtonClicked() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File selectedDirectory = directoryChooser.showDialog(HeightSlider.getScene().getWindow());
        String outputPath = selectedDirectory.getAbsolutePath();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        if (selectedDirectory != null) {
            if (PercentageSlider.isVisible()) {
                for (int i = 0; i < data.getFiles().size(); i++) {
                    int finalI = i;
                    executor.submit(() -> {
                        try {
                            File file = data.getFiles().get(finalI);
                            // file setup
                            String toPath = outputPath + File.separator + "resized_" + Nosurfile(file) + "." + Format.getText();
                            System.out.println("writing resized file to : " + toPath);
                            // write
                            Thumbnails.of(file)
                                    .scale(PercentageSlider.getValue() * .01)
                                    .outputFormat(Format.getText())
                                    .toFile(new File(toPath));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
        }else if (HeightSlider.isVisible()) {
            for (int i = 0; i < data.getFiles().size(); i++) {
                int finalI = i;
                executor.submit(() -> {
                    try {
                        File file = data.getFiles().get(finalI);
                        System.out.println(file);
                        // file setup
                        String toPath = outputPath + File.separator + "resized_" + Nosurfile(file) + "." + Format.getText();
                        System.out.println("writing resized file to : " + toPath);
                        Image img;
                        try {
                            img = new Image(new FileInputStream(file));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        // write
                        int OriginW = (int) img.getWidth();
                        int OriginH = (int) img.getHeight();
                        int Width = (int) (OriginW * (HeightSlider.getValue() / OriginH));
                        int Height = (int) HeightSlider.getValue();
                        Thumbnails.of(file)
                                .size(Width, Height)
                                .outputFormat(Format.getText())
                                .toFile(new File(toPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }else if (WidthSlider.isVisible()) {
                for (int i = 0; i < data.getFiles().size(); i++) {
                    int finalI = i;
                    executor.submit(() -> {
                        try {
                            File file = data.getFiles().get(finalI);
                            // file setup
                            String toPath = outputPath + File.separator + "resized_" + Nosurfile(file) + "." + Format.getText();
                            System.out.println("writing resized file to : " + toPath);
                            Image img;
                            try {
                                img = new Image(new FileInputStream(file));
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            // write
                            int OriginW = (int) img.getWidth();
                            int OriginH = (int) img.getHeight();
                            int Width = (int) WidthSlider.getValue();
                            int Height = (int) (OriginH * (WidthSlider.getValue() / OriginW));
                            Thumbnails.of(file)
                                    .size(Width, Height)
                                    .outputFormat(Format.getText())
                                    .toFile(new File(toPath));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private String Nosurfile(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return fileName;  // return the original filename if there's no dot
        }
        return fileName.substring(0, lastDot);
    }
    public void switchSceneToBackMainView(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/se233/javaproject/Main-view.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    void handleHeightButton() {
        if (HeightSlider.isVisible()) {
            HeightSlider.setVisible(false);
        } else {
            HeightSlider.setVisible(true);
            WidthSlider.setVisible(false);
            PercentageSlider.setVisible(false);
        }
    }

    @FXML
    void handleWidthButton() {
        if (WidthSlider.isVisible()) {
            WidthSlider.setVisible(false);
        } else {
            WidthSlider.setVisible(true);
            HeightSlider.setVisible(false);
            PercentageSlider.setVisible(false);
        }
    }

    @FXML
    void handlePercentageButton() {
        if (PercentageSlider.isVisible()) {
            PercentageSlider.setVisible(false);
        } else {
            PercentageSlider.setVisible(true);
            HeightSlider.setVisible(false);
            WidthSlider.setVisible(false);
        }
    }
    @FXML
    private void MenuButtonUpdate(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        Format.setText(menuItem.getText());
    }
    int percentageHeight;
    int percentageWidth;
    int percentage;
    Image newimage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Data data = new Data();
        List<File> fileList = data.getFiles();
        if (fileList != null && !fileList.isEmpty()) {
            File file = fileList.get(0);
            newimage = new Image(file.toURI().toString());
        } else {
            newimage = null;
            System.out.println("File list is empty or null!");
        }
        HeightSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            percentageHeight = newValue.intValue();
        }));
        WidthSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            percentageWidth = newValue.intValue();
        }));
        PercentageSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            percentage = newValue.intValue();
        }));
        if (newimage != null) {
            resizeImage(percentageHeight, newimage, percentageWidth);
            resizePercentage(newimage, percentage);
        } else {
            System.out.println("Image could not be initialized!");
        }
    }

    private Image resizeImage(double Height, Image image, double Width) {
        try {
            int newHeight = Height != 0 ? Integer.parseInt(String.valueOf(Height)) : (int) image.getHeight();
            int newWidth = Width != 0 ? Integer.parseInt(String.valueOf(Width)) : (int) image.getWidth();
            return new Image(image.getUrl(), newWidth, newHeight, true, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Image resizePercentage(Image image, double percentage) {
        try {
            double width = image.getWidth() * (1 + (percentage / 100.0));
            double height = image.getHeight() * (1 + (percentage / 100.0));
            return new Image(image.getUrl(), width, height, true, true);
        } catch (Exception e) {
            e.printStackTrace();  // Handle the exception appropriately
            return null;  // Return null or a default image in case of an error
        }
    }

}
