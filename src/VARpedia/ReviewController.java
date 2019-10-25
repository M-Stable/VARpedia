package VARpedia;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReviewController implements Initializable{
    public ImageView star1;
    public ImageView star2;
    public ImageView star3;
    public ImageView star4;
    public ImageView star5;
    public TableColumn<Creation, String> tableName;
    public TableColumn<Creation, Integer> tableRating;
    public TableColumn<Creation, String> tableViewed;
    public TableView table;
    public Text creationName;
    public Text lastViewed;
    public HBox starsHbox;
    public Text ratingText;
    ObservableList<Creation> creationObservableList = FXCollections.observableArrayList();

    public void initData(ObservableList<Creation> creationObservableList){
        this.creationObservableList = creationObservableList;
        setTable();
    }

    public void handlePlayButton(MouseEvent event) {
        if (table.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No Creation selected");
            alert.show();
        } else {
            /*
              Create and setup Media, MediaPlayer and MediaView before switching scene
             */
            Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
            String fileName = creation.getName() + ".mp4";
            File videoFile = new File("creations/" + fileName);

            //Get time of play back
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            creation.setViewTime(dtf.format(now));

            Media video = new Media(videoFile.toURI().toString());
            MediaPlayer player = new MediaPlayer(video);
            player.setAutoPlay(true);
            player.setOnReady(new Runnable() {
                @Override
                public void run() {

                    MediaView mediaView = new MediaView(player);

                    mediaView.setFitHeight(360);

                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("../FXML/media.fxml"));

                    MediaController mediaController = new MediaController(player, false, "../FXML/review.fxml");
                    mediaController.initData(creationObservableList);
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
                    window.setWidth(640);
                    window.setHeight(477);
                }
            });

        }
    }

    @FXML
    public void handleDeleteButton(MouseEvent event) {
        if (table.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No Creation selected");
            alert.show();
        } else {
            Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
            String fileName = creation.getName() + ".mp4";
            String filePath = "creations/" + fileName;
            File selectedCreation = new File(filePath);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Confirm Deletion");
            alert.setContentText("Are you sure you want to delete " + selectedCreation.getName() + "?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                creationObservableList.remove(creation);
                selectedCreation.delete();
                setTable();
            }
        }
    }

    public void handleBackButton(MouseEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../FXML/mainMenu.fxml"));
        Parent mainParent = loader.load();

        Scene mainMenu = new Scene(mainParent);

        MainMenuController mainMenuController = loader.getController();
        mainMenuController.initData(creationObservableList);

        Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
        window.setHeight(437);
        window.setWidth(640);
    }

    public void handleClick1(MouseEvent mouseEvent) {
        setStar1();
        Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
        creation.setConfidenceRating(1);
        setTable();
    }

    public void handleClick2(MouseEvent mouseEvent) {
        setStar2();
        Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
        creation.setConfidenceRating(2);
        setTable();
    }

    public void handleClick3(MouseEvent mouseEvent) {
        setStar3();
        Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
        creation.setConfidenceRating(3);
        setTable();
    }

    public void handleClick4(MouseEvent mouseEvent) {
        setStar4();
        Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
        creation.setConfidenceRating(4);
        setTable();
    }


    public void handleClick5(MouseEvent mouseEvent) {
        setStar5();
        Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
        creation.setConfidenceRating(5);
        setTable();
    }

    public void setTable() {
        try {
            FileOutputStream fos = new FileOutputStream("data.tmp");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new ArrayList<Creation>(creationObservableList));
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableRating.setCellValueFactory(new PropertyValueFactory<>("confidenceRating"));
        tableViewed.setCellValueFactory(new PropertyValueFactory<>("viewTime"));
        table.setItems(creationObservableList);
        table.refresh();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        starsHbox.setVisible(false);
        starsHbox.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                starsHbox.setVisible(true);
                starsHbox.setDisable(false);
                Creation creation = (Creation) table.getSelectionModel().getSelectedItem();
                creationName.setText(creation.getName());
                if (creation.getConfidenceRating() == 0) {
                    setStar0();
                }
                if (creation.getConfidenceRating() == 1) {
                    setStar1();
                }
                if (creation.getConfidenceRating() == 2) {
                    setStar2();
                }
                if (creation.getConfidenceRating() == 3) {
                    setStar3();
                }
                if (creation.getConfidenceRating() == 4) {
                    setStar4();
                }
                if (creation.getConfidenceRating() == 5) {
                    setStar5();
                }
                if (creation.getViewTime().equals("N/A")) {
                    lastViewed.setText("Not Viewed");
                } else {
                    lastViewed.setText(creation.getViewTime());
                }
            }
        });
    }

    public void setStar0() {
        ratingText.setText("Rate Me!");
        star1.setImage(new Image("Images/icons8-star-48.png"));
        star2.setImage(new Image("Images/icons8-star-48.png"));
        star3.setImage(new Image("Images/icons8-star-48.png"));
        star4.setImage(new Image("Images/icons8-star-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }

    public void setStar1() {
        ratingText.setText("Need Reviewing");
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-48.png"));
        star3.setImage(new Image("Images/icons8-star-48.png"));
        star4.setImage(new Image("Images/icons8-star-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }
    public void setStar2() {
        ratingText.setText("Getting there");
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-48.png"));
        star4.setImage(new Image("Images/icons8-star-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }
    public void setStar3() {
        ratingText.setText("Pretty Confident");
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-filled-48.png"));
        star4.setImage(new Image("Images/icons8-star-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }
    public void setStar4() {
        ratingText.setText("Confident");
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-filled-48.png"));
        star4.setImage(new Image("Images/icons8-star-filled-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }
    public void setStar5() {
        ratingText.setText("Very Confident!");
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-filled-48.png"));
        star4.setImage(new Image("Images/icons8-star-filled-48.png"));
        star5.setImage(new Image("Images/icons8-star-filled-48.png"));
    }
}
