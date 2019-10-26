package VARpedia;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaController implements Initializable{


    private  String source;
    @FXML
    private Slider volumeSlider;

    @FXML
    private AnchorPane parentPane;

    @FXML
    private Slider timeSlider;

    @FXML
    private Button backButton;

    @FXML
    private ImageView backImage;

    private MediaPlayer player;

    private boolean preview;

    private boolean atEndOfMedia = false;

    public MediaController(MediaPlayer player, boolean preview, String source) {
        this.player = player;
        this.preview = preview;
        this.source = source;
    }
    ObservableList<Creation> creationObservableList = FXCollections.observableArrayList();

    public void initData(ObservableList<Creation> creationObservableList){
        this.creationObservableList = creationObservableList;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*
        Setup the values for the volume slider and time slider
         */
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(1);

        timeSlider.setMin(0);
        timeSlider.setMax(player.getMedia().getDuration().toSeconds());
        timeSlider.setValue(0);

        /*
        When the video reaches the end, set atEndOfMedia to true
         */
        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                atEndOfMedia = true;
            }
        });

        /*
        When the user adjusts the volume slider, change the volume of the audio player
         */
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                player.setVolume(newValue.doubleValue());
            }
        });

        /*
        When the user adjusts the time slider, change the current time of the video
         */
        ChangeListener timeSliderListener = new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                player.seek(Duration.seconds(newValue));
            }
        };

        timeSlider.valueProperty().addListener(timeSliderListener);

        /*
        Adjust the time slider as the video plays
         */
        player.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                timeSlider.valueProperty().removeListener(timeSliderListener);
                timeSlider.setValue(player.getCurrentTime().toSeconds());
                timeSlider.valueProperty().addListener(timeSliderListener);
            }
        });

        if(preview) {
            parentPane.getChildren().removeAll(backButton, backImage, timeSlider);
        }

    }

    /*
    Play/pause the video. If the video is at the end of playback, returns to the start of the video
     */
    public void handlePlayButton(ActionEvent actionEvent) {
        MediaPlayer.Status status = player.getStatus();

        if(atEndOfMedia) {
            player.seek(player.getStartTime());
            atEndOfMedia = false;
        } else if(status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
            player.play();
        }
    }

    public void handlePauseButton(ActionEvent actionEvent) {
        MediaPlayer.Status status = player.getStatus();

        if(status == MediaPlayer.Status.PLAYING) {
            player.pause();
        }
    }

    /*
    Return to the main menu scene
     */
    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        player.stop();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(source));
        Parent mainParent = loader.load();

        Scene mainMenu = new Scene(mainParent);

        try {
            ReviewController reviewController = loader.getController();
            reviewController.initData(creationObservableList);
        } catch (ClassCastException ignored) {

        }

        Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
        window.setHeight(437);
        window.setWidth(640);

    }

}
