package WikiSpeak;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MediaController implements Initializable{

    @FXML
    private Slider volumeSlider;

    @FXML
    private AnchorPane parentPane;

    @FXML
    private Slider timeSlider;

    private MediaPlayer player;

    private boolean atEndOfMedia = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(1);
        //timeSlider.setMax(player.getMedia().getDuration().toSeconds());

        /*player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                atEndOfMedia = true;
            }
        });*/

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                player.setVolume(newValue.doubleValue());
            }
        });

        /*InvalidationListener timeSliderListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(timeSlider.isValueChanging()) {
                    player.seek(new Duration(timeSlider.getValue()));
                }
            }
        };


        timeSlider.valueProperty().addListener(timeSliderListener);

        player.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                timeSlider.valueProperty().removeListener(timeSliderListener);
                timeSlider.setValue(player.getCurrentTime().toSeconds());
                timeSlider.valueProperty().addListener(timeSliderListener);
            }
        });
        */
    }

    public void handlePlayButton(ActionEvent actionEvent) {

        MediaPlayer.Status status = player.getStatus();

        if(status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
            if(atEndOfMedia) {
                player.seek(player.getStartTime());
                atEndOfMedia = false;
            }
            player.play();
        } else {
            player.pause();
        }
    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        player.stop();

        Parent mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();

    }

    public void setPlayer(MediaPlayer mediaPlayer) {
        player = mediaPlayer;
    }
}
