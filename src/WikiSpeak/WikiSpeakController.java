package WikiSpeak;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;


public class WikiSpeakController implements Initializable {

    @FXML
    public Button newCreationButton;
    @FXML
    private ListView creationsList;

    @FXML
    private Text welcomeText;

    private File creationsDir;

    private File audioDir;

    private File imagesDir;

    private File audioCreationsDir;

    private boolean nameSort = true;

    @FXML
    private Button toggleButton;

    @FXML
    protected void handlePlayButton(ActionEvent event) throws IOException {

        if (creationsList.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No Creation selected");
            alert.show();
        } else {
            /*
              Create and setup Media, MediaPlayer and MediaView before switching scene
             */
            String fileName = creationsList.getSelectionModel().getSelectedItem().toString() + ".mp4";
            File videoFile = new File("creations/" + fileName);
            Media video = new Media(videoFile.toURI().toString());
            MediaPlayer player = new MediaPlayer(video);
            player.setAutoPlay(true);
            player.setOnReady(new Runnable() {
                @Override
                public void run() {
                    MediaView mediaView = new MediaView(player);

                    mediaView.setFitHeight(360);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("media.fxml"));

                    MediaController mediaController = new MediaController(player);
                    loader.setController(mediaController);
                    BorderPane root = null;
                    try {
                        root = (BorderPane) loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    root.setCenter(mediaView);

                    Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    window.setScene(new Scene(root));
                    window.show();
                }
            });

        }
    }

    @FXML
    public void handleNewCreationButton(ActionEvent event) throws IOException {
        /*
        Switch scene to the new creation scene
         */
        Parent creationParent = FXMLLoader.load(getClass().getResource("creator.fxml"));
        Scene newCreationScene = new Scene(creationParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(newCreationScene);
        window.show();

    }

    /*
    Updates the creations list using two different sorting methods depnding on what the user has specified using
    the toggleButton
     */
    public void updateCreationsList() {
        File[] creations = creationsDir.listFiles();

        if (!nameSort) {
            Arrays.sort(creations, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return Long.valueOf(file.lastModified()).compareTo(t1.lastModified());
                }
            });
        } else {
            Arrays.sort(creations, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    return String.CASE_INSENSITIVE_ORDER.compare(t.toString(), t1.toString());
                }
            });
        }

        creationsList.getItems().clear();

        for (File creation : creations) {
            if (creation.getName().contains(".mp4")) {
                creationsList.getItems().add(creation.getName().replace(".mp4", ""));
            }
        }
    }

    /*
    Check if the user has selected a creation, and if they have display a confirmation prompt to confirm deletion
    before deleting the file
    */
    @FXML
    public void handleDeleteButton(ActionEvent event) {
        if (creationsList.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No Creation selected");
            alert.show();
        } else {
            String fileName = creationsList.getSelectionModel().getSelectedItem().toString() + ".mp4";
            String filePath = "creations/" + fileName;
            File selectedCreation = new File(filePath);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText("Are you sure you want to delete " + selectedCreation.getName() + "?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                selectedCreation.delete();
                updateCreationsList();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*
         Create the folders used by the program at program startup if they do not already exist, as well as populate
         the creations list and style the menu
         */
        creationsDir = new File("creations/");
        audioDir = new File("audio/");
        imagesDir = new File("images/");
        audioCreationsDir = new File("audioCreation/");
        creationsDir.mkdir();
        audioDir.mkdir();
        imagesDir.mkdir();
        audioCreationsDir.mkdir();

        updateCreationsList();

        welcomeText.setFont(Font.font("veranda", FontWeight.BOLD, FontPosture.REGULAR, 20));
        welcomeText.setFill(Color.DARKSLATEBLUE);
        newCreationButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: #FFFAF0;");
    }

    /*
    Switch sorting method for the creations list
     */
    public void handleToggleButton(ActionEvent event) {
        nameSort = !nameSort;

        if (nameSort) {
            toggleButton.setText("Sort: name");
        } else {
            toggleButton.setText("Sort: creation time");
        }

        updateCreationsList();
    }
}
