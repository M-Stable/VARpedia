package VARpedia;

import Tasks.FlickrTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectImagesController implements Initializable {

    @FXML
    private TilePane tilePane;
    @FXML
    private ImageView loading;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private List<File> images;
    private String imageSearchTerm;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<File> selectedImages = new ArrayList<File>();
    private CreateCreationController createCreationController;

    public SelectImagesController(CreateCreationController createCreationController, String imageSearchTerm) {
        this.imageSearchTerm = imageSearchTerm;
        this.createCreationController = createCreationController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.close();
            }
        });
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if (selectedImages.size() == 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Please select at least 1 image before saving");
                    alert.show();
                } else {
                    File[] imageFiles = new File("images/").listFiles();
                    for (File image : imageFiles) {
                        boolean contains = false;

                        for (File selectedImage : selectedImages) {
                            if (selectedImage.getPath().replace("file:", "").equals(image.getAbsolutePath())) {
                                contains = true;
                            }
                        }
                        if (!contains) {
                            image.delete();
                        }
                    }

                    createCreationController.images = selectedImages;
                    Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                    stage.close();
                }


            }
        });
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        loading.setVisible(true);
        FlickrTask flickrTask = new FlickrTask(10, imageSearchTerm);
        executorService.submit(flickrTask);
        flickrTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {

                loading.setVisible(false);

                    images = flickrTask.getImages();

                    for (File imageFile : images) {
                        Image image = new Image(String.valueOf(imageFile.toURI()), 200, 200, false, false);

                        RadioButton radioButton = new RadioButton();
                        ImageView imageView = new ImageView(image);
                        Pane pane = new Pane(imageView, radioButton);

                        imageView.setPreserveRatio(true);
                        imageView.setFitHeight(200);
                        imageView.setFitWidth(200);

                        radioButton.setMinSize(10, 10);
                        radioButton.setMaxSize(10, 10);
                        radioButton.setLayoutX(95);
                        radioButton.setLayoutY(207.5);
                        radioButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                radioButton.setSelected(!radioButton.isSelected());
                                pane.fireEvent(mouseEvent);
                            }
                        });
                        pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                               boolean set = radioButton.isSelected();
                                String fileURL = imageView.getImage().getUrl();

                                File selectedImage = new File(fileURL);

                                if (!set) {
                                    selectedImages.add(selectedImage);
                                    radioButton.setSelected(true);
                                } else {
                                    selectedImages.remove(selectedImage);
                                    radioButton.setSelected(false);
                                }
                            }
                        });
                        
                        tilePane.getChildren().add(pane);
                    }
                }


        });

    }

    public List<File> getImages() {
        return images;
    }
}
