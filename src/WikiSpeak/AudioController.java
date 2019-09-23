package WikiSpeak;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;


public class AudioController {

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Parent creatorParent = FXMLLoader.load(getClass().getResource("creator.fxml"));
        Scene creationScene = new Scene(creatorParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(creationScene);
        window.show();
    }
}
