package WikiSpeak;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class WikiSpeakController implements Initializable {

    @FXML
    public Button newCreationButton;
    @FXML
    private ListView creationsList;

    @FXML
    private Text welcomeText;

    private File creationsDir;

    private File audioDir;

    private File imagesDir;

    private File audioCreationsDir;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    protected void handlePlayButton(ActionEvent event) throws IOException {

        if(creationsList.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No Creation selected");
            alert.show();
        } else {
            String fileName = creationsList.getSelectionModel().getSelectedItem().toString() + ".mp4";
            File videoFile = new File("creations/" + fileName);
            Media video = new Media(videoFile.toURI().toString());
            MediaPlayer player = new MediaPlayer(video);
            player.setAutoPlay(true);
            player.setOnReady(new Runnable() {
                @Override
                public void run() {
                    MediaView mediaView = new MediaView(player);

                    mediaView.setFitHeight(360);


                    FXMLLoader loader = new FXMLLoader(getClass().getResource("media.fxml"));

                    // MediaController mediaController = loader.getController();
                    MediaController mediaController = new MediaController(player);
                    loader.setController(mediaController);
                    BorderPane root = null;
                    try {
                        root = (BorderPane) loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    root.setCenter(mediaView);
                    //  mediaController.setVideoLength(video.getDuration());
                    //mediaController.setPlayer(player);

                    Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
                    window.setScene(new Scene(root));
                    window.show();
                }
            });

        }
    }

    @FXML
    public void handleNewCreationButton(ActionEvent event) throws IOException {
        Parent creationParent = FXMLLoader.load(getClass().getResource("creator.fxml"));
        Scene newCreationScene = new Scene(creationParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(newCreationScene);
        window.show();

    }

    public void updateCreationsList() {
        File[] creations = creationsDir.listFiles();

        if(toggleButton.isSelected()) {
            Arrays.sort(creations, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return Long.valueOf(file.lastModified()).compareTo(t1.lastModified());
                }
            });
        } else {
            Arrays.sort(creations, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    return String.CASE_INSENSITIVE_ORDER.compare(t.toString(), t1.toString());
                }
            });
        }

        creationsList.getItems().clear();

        for(File creation : creations) {
            if(creation.getName().contains(".mp4")) {
                creationsList.getItems().add(creation.getName().replace(".mp4", ""));
            }
        }
    }

    @FXML
    public void handleDeleteButton(ActionEvent event) {
        if(creationsList.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No Creation selected");
            alert.show();
        } else {
            String fileName = creationsList.getSelectionModel().getSelectedItem().toString() + ".mp4";
            String filePath = "creations/" + fileName;
            File selectedCreation = new File(filePath);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText("Are you sure you want to delete " + selectedCreation.getName() + "?");

            Optional<ButtonType> result = alert.showAndWait();

            if(result.get() == ButtonType.OK) {
                selectedCreation.delete();
                updateCreationsList();
            }
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        creationsDir = new File("creations/");
        audioDir = new File("audio/");
        imagesDir = new File("images/");
        audioCreationsDir = new File("audioCreation/");
        creationsDir.mkdir();
        audioDir.mkdir();
        imagesDir.mkdir();
        audioCreationsDir.mkdir();
        updateCreationsList();
        welcomeText.setFont(Font.font("veranda", FontWeight.BOLD, FontPosture.REGULAR, 20));
        welcomeText.setFill(Color.DARKSLATEBLUE);
        newCreationButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: #FFFAF0;");
    }
}
