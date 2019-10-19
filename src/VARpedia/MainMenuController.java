package VARpedia;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class MainMenuController implements Initializable {

    @FXML
    public Button newCreationButton;

    private File creationsDir;
    private File imagesDir;
    private File audioCreationsDir;

    @FXML
    public void handleNewCreationButton(ActionEvent event) throws IOException {
        /*
        Switch scene to the new creation scene
         */
        Parent creationParent = FXMLLoader.load(getClass().getResource("createCreation.fxml"));
        Scene newCreationScene = new Scene(creationParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setResizable(false);
        window.setScene(newCreationScene);
        window.show();

    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*
         Create the folders used by the program at program startup if they do not already exist, as well as populate
         the creations list and style the menu
         */
        creationsDir = new File("creations/");
        imagesDir = new File("images/");
        audioCreationsDir = new File("audioCreation/");
        creationsDir.mkdir();
        imagesDir.mkdir();
        audioCreationsDir.mkdir();

    }



    public void handleReviewButton(ActionEvent event) throws IOException {
        //Switch to review scene
        Parent creationParent = FXMLLoader.load(getClass().getResource("review.fxml"));
        Scene newCreationScene = new Scene(creationParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setResizable(false);
        window.setScene(newCreationScene);
        window.show();
    }

    public void handleListButton(ActionEvent event) throws IOException {

        //Switch to list scene
        Parent creationParent = FXMLLoader.load(getClass().getResource("list.fxml"));
        Scene newCreationScene = new Scene(creationParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setResizable(false);
        window.setScene(newCreationScene);
        window.show();
    }

    public void handleCreditsButton(ActionEvent event) throws IOException {
        //Switch to credits scene
        Parent creationParent = FXMLLoader.load(getClass().getResource("credits.fxml"));
        Scene newCreationScene = new Scene(creationParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setResizable(false);
        window.setScene(newCreationScene);
        window.show();
    }
}
