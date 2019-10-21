package VARpedia;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.ResourceBundle;

public class MediaPreviewController implements Initializable{

    @FXML
    private Slider volumeSlider;
    private MediaPlayer player;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*
        Setup values for the volume slider
         */
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(1);

        /*
        Update player volume when the user changes the volume slider
         */
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                player.setVolume(newValue.doubleValue());
            }
        });
    }

    /*
    Play/pause the video when the plau/pause button is pressed
     */
    public void handlePlayButton(ActionEvent actionEvent) {
        if(player.getStatus() == MediaPlayer.Status.PAUSED) {
            player.play();
        }
    }

    public void setPlayer(MediaPlayer mediaPlayer) {
        player = mediaPlayer;
    }

    public void handlePauseButton(ActionEvent actionEvent) {
        if(player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
        }
    }
}
