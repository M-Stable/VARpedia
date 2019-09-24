package WikiSpeak;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.IOException;

public class MediaController {

    @FXML
    private Slider volumeSlider;

    @FXML
    private MediaView mediaView;

    public void handlePlayButton(ActionEvent actionEvent) {
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        Parent mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
    }
}
