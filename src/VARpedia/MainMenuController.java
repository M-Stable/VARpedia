package VARpedia;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class MainMenuController implements Initializable {

    @FXML
    public Button newCreationButton;

    private File creationsDir;
    private File imagesDir;
    private File audioCreationsDir;
    private ObservableList<Creation> creationObservableList = FXCollections.observableArrayList();

    public void initData(ObservableList<Creation> creationObservableList){
        this.creationObservableList = creationObservableList;
    }

    @FXML
    public void handleNewCreationButton(ActionEvent event) throws IOException {
        /*
        Switch scene to the new creation scene
         */
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("createCreation.fxml"));
        Parent mainParent = loader.load();

        Scene newCreationScene = new Scene(mainParent);

        CreateCreationController createCreationController = loader.getController();
        createCreationController.initData(creationObservableList);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setResizable(false);
        window.setScene(newCreationScene);
        window.show();
        window.setHeight(506);
        window.setWidth(647);
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

        //if this is the first time user opening this program, creating a file to store creation data else read from data file to set up list
        File file = new File("data.tmp");
        if (!file.exists()) {
            File[] creations = creationsDir.listFiles();
            for (File creation : creations) {
                if (creation.getName().contains(".mp4")) {
                    creationObservableList.add(new Creation(creation.getName().replace(".mp4", ""), 0 ,"N/A"));
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream("data.tmp");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(new ArrayList<Creation>(creationObservableList));
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileInputStream fis = new FileInputStream("data.tmp");
                ObjectInputStream ois = new ObjectInputStream(fis);
                List<Creation> list = (List<Creation>) ois.readObject();
                creationObservableList = FXCollections.observableList(list);
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleReviewButton(ActionEvent event) throws IOException {
        //Switch to review scene
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("review.fxml"));
        Parent mainParent = loader.load();

        Scene newCreationScene = new Scene(mainParent);

        ReviewController reviewController = loader.getController();
        reviewController.initData(creationObservableList);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setResizable(false);
        window.setScene(newCreationScene);
        window.show();
        window.setHeight(437);
        window.setWidth(640);
    }

    public void handleCreditsButton(ActionEvent event) throws IOException {
        //Switch to credits scene
        Parent creationParent = FXMLLoader.load(getClass().getResource("credits.fxml"));
        Scene newCreationScene = new Scene(creationParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setResizable(false);
        window.setScene(newCreationScene);
        window.show();
        window.setHeight(429);
        window.setWidth(640);
    }
}
