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

public class CreateCreationController {

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Parent mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
    }

    @FXML
    public void handleNextButton(ActionEvent event) throws IOException {
        Parent audioParent = FXMLLoader.load(getClass().getResource("audio.fxml"));
        Scene audioCreation = new Scene(audioParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(audioCreation);
        window.show();
    }
}
