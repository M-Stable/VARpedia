package WikiSpeak;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


public class WikiSpeakController implements Initializable {

    @FXML
    private ListView creationsList;

    private File creationsDir;

    private File audioDir;

    private File videoDir;

    private File imagesDir;

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
            MediaView mediaView = new MediaView(player);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("media.fxml"));
            BorderPane root = (BorderPane) loader.load();
            root.setCenter(mediaView);
            MediaController mediaController = loader.getController();
            mediaController.setPlayer(player);

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
            window.setScene(new Scene(root));
            window.show();
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

        creationsList.getItems().clear();

        for(File creation : creations) {
            if(creation.getName().contains(".mp4")) {
                creationsList.getItems().add(creation.getName().replace(".mp4", ""));
            }
        }
    }

    @FXML
    public void handleDeleteButton(ActionEvent event) {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        creationsDir = new File("creations/");
        audioDir = new File("audio/");
        videoDir = new File("video/");
        imagesDir = new File("images/");
        creationsDir.mkdir();
        audioDir.mkdir();
        videoDir.mkdir();
        imagesDir.mkdir();
        updateCreationsList();
    }
}
