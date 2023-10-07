package se233.javaproject.view;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import se233.javaproject.Data;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainView implements Initializable {
    private Stage stage;
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    private Scene scene;
    private Parent root;
    @FXML
    private Button Resize;
    @FXML
    private ListView filesListView;
    Data data = Data.getInstance();
    @FXML
    public void switchSceneToResize(ActionEvent event) throws IOException {
        try {
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(MainView.class.getResource("/se233/javaproject/Re-size.fxml"));
            root = loader.load();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void switchSceneToWaterMask(javafx.event.ActionEvent event) throws IOException {
        try {
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(MainView.class.getResource("/se233/javaproject/Water-Mask.fxml"));
            root = loader.load();
            WaterMask waterMaskController = loader.getController();
            waterMaskController.setSelectedFiles(data.getFiles());
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void HandleDragOver(DragEvent event){
        if(event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.ANY);
    }
    private boolean handleZipFile(File zipFile, List<File> droppedFiles) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    // Process each file entry within the zip file
                    String entryName = entry.getName();
                    // Check if the entry is a PNG or JPG file
                    if (entryName.endsWith(".png") || entryName.endsWith(".jpg")) {
                        System.out.println("Extracting and handling " + entryName + " from the zip file");

                        // Read the contents of the entry into a byte array
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        byte[] entryData = outputStream.toByteArray();
                        // Convert the byte array to a BufferedImage
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(entryData);
                        BufferedImage image = ImageIO.read(inputStream);
                        // save the image as a PNG file
                        File temp = new File(entryName);
                        ImageIO.write(image, "PNG", temp);
                        // Add the extracted file to the list
                        droppedFiles.add(temp);
                        filesListView.getItems().add(temp.getName());
                        temp.deleteOnExit();
                    } else {
                        System.out.println("Skipping " + entryName + " as it is not a PNG or JPG file");
                    }
                }
            }
            return true; // Handling was successful
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Handling failed
        }
    }
    public void HandleDrop(DragEvent event) {
        List<File> droppedFiles = event.getDragboard().getFiles();
        List<File> filesToAdd = new ArrayList<>();

        if (droppedFiles != null && !droppedFiles.isEmpty()) {
            for (File file : droppedFiles) {
                if (file.getName().endsWith(".zip")) {
                    handleZipFile(file, filesToAdd); // Modify handleZipFile to add extracted files to filesToAdd
                } else {
                    filesToAdd.add(file);
                    for (int i = 0; i < droppedFiles.size(); i++) {
                        File f = droppedFiles.get(i);
                        filesListView.getItems().add(f.getName());
                    }
                }
            }
            List<File> existingFiles = data.getFiles();
            if (existingFiles == null) {
                data.setFiles(new ArrayList<>(filesToAdd));
            } else {
                existingFiles.addAll(filesToAdd);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourcesBundle) {
        if (filesListView != null) {
            List<File> savedFiles = data.getFiles();
            if (savedFiles != null && !savedFiles.isEmpty()) {
                for (int i = 0; i < savedFiles.size(); i++) {
                    File f = savedFiles.get(i);
                    filesListView.getItems().add(f.getName());
                }
                data.setFiles(savedFiles);
            } else {
                System.out.println("File is empty");
            }
        }
        Resize.setOnAction(event -> {
            try {
                switchSceneToResize(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}