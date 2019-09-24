package WikiSpeak;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class WikiSpeakController {

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
            MediaView mediaView = new MediaView(player);
            mediaView.setId("mediaView");

            BorderPane creationParent = FXMLLoader.load(getClass().getResource("media.fxml"));
            Scene newCreationScene = new Scene(creationParent);

            creationParent.setCenter(mediaView);

            //creationParent.getChildren().add(mediaView);

            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(newCreationScene);
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

        File testvid = new File("test.mp4");
        creationsList.getItems().add(testvid);

    }
}
