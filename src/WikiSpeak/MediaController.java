package WikiSpeak;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaController implements Initializable{

    @FXML
    private Slider volumeSlider;

    @FXML
    private BorderPane parentPane;

    private MediaPlayer player;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        MediaView mediaView = (MediaView) parentPane.lookup("#mediaView");
        player = mediaView.getMediaPlayer();

        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (volumeSlider.isValueChanging()) {
                    player.setVolume(volumeSlider.getValue());
                }

            }
        });

    }

    public void handlePlayButton(ActionEvent actionEvent) {
        if(player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
        } else {
            player.play();
        }


    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        Parent mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
    }


}
