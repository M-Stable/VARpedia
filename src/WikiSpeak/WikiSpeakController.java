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
import java.util.ResourceBundle;


public class WikiSpeakController implements Initializable {

    @FXML
    private ListView creationsList;

    @FXML
    protected void handlePlayButton(ActionEvent event) throws IOException {

        if(creationsList.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No Creation selected");
            alert.show();
        } else {
            File videoFile = (File) creationsList.getSelectionModel().getSelectedItem();
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

    @FXML
    public void handleDeleteButton(ActionEvent event) {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File file = new File("test2.mp4");
        creationsList.getItems().add(file);
    }
}
