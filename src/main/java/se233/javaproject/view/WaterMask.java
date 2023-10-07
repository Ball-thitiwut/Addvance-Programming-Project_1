package se233.javaproject.view;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.effect.GaussianBlur;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import se233.javaproject.Data;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class WaterMask extends MainView {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Image originalImage;
    private Image watermarkedImage;
    private Image backgroundChangedImage;
    Data data = Data.getInstance();
    @FXML
    private Slider Visibility;
    @FXML
    private Slider ImageQuality;
    @FXML
    private TextArea textArea;
    @FXML
    private Text waterMaskText;
    @FXML
    private ImageView imageView;
    @FXML
    private ChoiceBox<String> changeFormat;
    @FXML
    private ColorPicker changeBackgroundColor;
    @FXML
    private MenuButton Format;
    @Override
    public void initialize(URL url,ResourceBundle resource){
        waterMaskText.opacityProperty().bind(Visibility.valueProperty().divide(100));
        ImageQuality.setValue(50);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            waterMaskText.setText(newValue);
            updateWatermarkOnImage();
        });
        Visibility.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateWatermarkOnImage();
        });
        ImageQuality.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateImageQuality();
        });
        List<String> availableFonts = Font.getFamilies();
        changeFormat.getItems().addAll(availableFonts);
        changeFormat.setValue("Arial");
        changeFormat.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateWatermarkFont(newValue);
        });
    }
    public void switchSceneToBackMainView(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/se233/javaproject/Main-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    private List<File> selectedFiles;
    public void setSelectedFiles(List<File> files) {
        this.selectedFiles = files;
        if (files != null && !files.isEmpty()) {
            loadImage(files.get(0));
        }
    }
    private void loadImage(File file) {
        originalImage = new Image(file.toURI().toString());
        if (backgroundChangedImage != null) {
            imageView.setImage(backgroundChangedImage);
        } else if (watermarkedImage != null) {
            imageView.setImage(watermarkedImage);
        } else {
            imageView.setImage(originalImage);
        }
    }
    public void removeWaterMark() {
        watermarkedImage = null;
        if (backgroundChangedImage != null) {
            imageView.setImage(backgroundChangedImage);
        } else {
            imageView.setImage(originalImage);
        }
        textArea.setText("");
    }
    private Image addWatermarkToImage(Image baseImage) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = originalImage.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setArgb(x, y, pixelReader.getArgb(x, y));
            }
        }
        Text watermark = new Text(waterMaskText.getText());
        watermark.setFont(Font.font(changeFormat.getValue(), FontWeight.BOLD, 100));
        watermark.setOpacity(Visibility.getValue() / 100.0);
        watermark.setWrappingWidth(width);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Image watermarkImage = watermark.snapshot(parameters, null);
        int startX = width - (int) watermarkImage.getWidth() - 10;
        int startY = height - (int) watermarkImage.getHeight() - 10;
        startX = Math.max(startX, 0);
        startY = Math.max(startY, 0);

        for (int y = 0; y < watermarkImage.getHeight() && startY + y < height; y++) {
            for (int x = 0; x < watermarkImage.getWidth() && startX + x < width; x++) {
                if (watermarkImage.getPixelReader().getArgb(x, y) != 0) {
                    pixelWriter.setArgb(startX + x, startY + y, watermarkImage.getPixelReader().getArgb(x, y));
                }
            }
        }
        return writableImage;
    }
    private void updateWatermarkOnImage() {
        watermarkedImage = addWatermarkToImage(originalImage);
        if (backgroundChangedImage != null) {
            backgroundChangedImage = changeBackground(watermarkedImage, changeBackgroundColor.getValue());
            imageView.setImage(backgroundChangedImage);
        } else {
            imageView.setImage(watermarkedImage);
        }
    }
    private void updateImageQuality() {
        double blurValue = (100 - ImageQuality.getValue()) / 25;
        GaussianBlur blurEffect = new GaussianBlur(blurValue);
        imageView.setEffect(blurEffect);
        if (backgroundChangedImage != null) {
            imageView.setImage(backgroundChangedImage);
        } else if (watermarkedImage != null) {
            imageView.setImage(watermarkedImage);
        } else {
            imageView.setImage(originalImage);
        }
    }
    private void updateWatermarkFont(String fontName) {
        double fontSize = imageView.getImage().getWidth() * 0.1;
        if (fontSize > 100) fontSize = 100;
        if (fontSize < 20) fontSize = 20;
        updateWatermarkOnImage();
    }
    public void changeImageBackgroundColor() {
        Color selectedColor = changeBackgroundColor.getValue();
        if (watermarkedImage != null) {
            backgroundChangedImage = changeBackground(watermarkedImage, selectedColor);
        } else {
            backgroundChangedImage = changeBackground(originalImage, selectedColor);
        }
        imageView.setImage(backgroundChangedImage);
    }
    private Image changeBackground(Image baseImage, Color newColor) {
        if (baseImage == null) return null;

        int width = (int) baseImage.getWidth();
        int height = (int) baseImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = baseImage.getPixelReader();

        double threshold = 0.05;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = pixelReader.getColor(x, y);

                if (Math.abs(pixelColor.getRed() - Color.WHITE.getRed()) < threshold &&
                        Math.abs(pixelColor.getGreen() - Color.WHITE.getGreen()) < threshold &&
                        Math.abs(pixelColor.getBlue() - Color.WHITE.getBlue()) < threshold) {
                    pixelWriter.setColor(x, y, newColor);
                } else {
                    pixelWriter.setColor(x, y, pixelColor);
                }
            }
        }
        return writableImage;
    }
    @FXML
    private void MenuButtonUpdate(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        Format.setText(menuItem.getText());
    }
    @FXML
    public void confirmButtonClicked() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File selectedDirectory = directoryChooser.showDialog(Visibility.getScene().getWindow());
        if (selectedDirectory != null) {
            String outputPath = selectedDirectory.getAbsolutePath();
            BufferedImage watermark = stringToImage(textArea.getText(),
                    new java.awt.Font((String) changeFormat.getValue(), java.awt.Font.PLAIN, 100)
            );
            ExecutorService executor = Executors.newFixedThreadPool(4);
            executor.submit(() -> {
                try {
                    for (int i = 0; i < data.getFiles().size(); i++) {

                        File file = data.getFiles().get(i);
                        Image image = new Image(file.toURI().toString());
                        Color selectedColor = changeBackgroundColor.getValue();
                        Image coloredImage = changeBackground(image, selectedColor);
                        BufferedImage bufferedColoredImage = SwingFXUtils.fromFXImage(coloredImage, null);

                        // file setup
                        String toPath = outputPath + File.separator + "WaterMask" + Nosurfile(file) + "." + Format.getText();
                        System.out.println("writing watermask file to : " + toPath);

                        // write
                        Thumbnails.of(bufferedColoredImage) // Use bufferedColoredImage here
                                .scale(1)
                                .watermark(Positions.BOTTOM_LEFT, watermark, (float) Visibility.getValue() / 100.f)
                                .outputFormat(Format.getText())
                                .toFile(new File(toPath));
                        }
                    }catch(IOException e){
                    e.printStackTrace();
                }
            });
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
    private static BufferedImage stringToImage(String text, java.awt.Font font) {
        // Create a dummy image to get the text dimensions
        BufferedImage dummyImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = dummyImg.createGraphics();
        graphics2D.setFont(font);
        FontMetrics fm = graphics2D.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        graphics2D.dispose();
        // Create the actual image with the correct dimensions
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics2D = img.createGraphics();
        // set font
        graphics2D.setFont(font);
        graphics2D.setColor(new java.awt.Color(0,0,0,0));
        graphics2D.fillRect(0, 0, width, height);
        graphics2D.setColor(new java.awt.Color(0,0,0,255));
        graphics2D.drawString(text, 0, fm.getAscent());
        // Check for negative or zero dimensions
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid dimensions for text image: width=" + width + ", height=" + height);
        }
        graphics2D.dispose();
        return img;
    }
}
