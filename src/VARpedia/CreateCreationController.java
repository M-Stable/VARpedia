package VARpedia;

import Tasks.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CreateCreationController implements Initializable {
    @FXML
    public ImageView deleteAllAudioButton;
    public VBox creationVbox;
    public HBox audioHbox;
    public Pane pane;
    public ImageView backButton;
    public ImageView playAudio;
    public Text numberOfImages;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private TextField searchField, textCreationName;
    @FXML
    private TextArea textArea;
    @FXML
    private ComboBox comboBox, musicDropdown;
    @FXML
    private ListView<String> listForCreation = new ListView<>();
    @FXML
    private Button previewButton, previewCreationButton, searchButton, saveAudioButton, createButton, selectImagesButton;
    @FXML
    private ProgressBar progressBar;

    private File audioCreationDir = new File("audioCreation/");
    private File imagesDir = new File("images/");

    private String highlightedText = "";
    private String searchTextFinal = "";

    private PlayAudioTask playAudioTask;
    private PreviewAudioTask previewAudio;
    public List<File> images = new ArrayList<File>();
    private ObservableList<String> audioCreationList = FXCollections.observableArrayList();
    ObservableList<Creation> creationObservableList = FXCollections.observableArrayList();

    public void initData(ObservableList<Creation> creationObservableList){
        this.creationObservableList = creationObservableList;
    }

    @FXML
    public void handleBackButton(MouseEvent event) throws IOException {
        //Switch scene back to the main menu
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("mainMenu.fxml"));
        Parent mainParent = loader.load();

        Scene mainMenu = new Scene(mainParent);

        MainMenuController mainMenuController = loader.getController();
        mainMenuController.initData(creationObservableList);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
        window.setHeight(437);
        window.setWidth(640);
    }

    @FXML
    public void handleSearchButton(ActionEvent actionEvent) {

        //If there is a previous search term, restart audio list to prevent mixing of audio clips.
        if (!searchTextFinal.equals("")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Delete audio files?");
            alert.setContentText("Searching another term results in deleting all saved audio files");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                for (File file : audioCreationDir.listFiles()) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }
                listForCreation.getItems().clear();
                listForCreation.setItems(audioCreationList);
                previewCreationButton.setDisable(true);
                createButton.setDisable(true);
            } else {
                return;
            }
        }

        //Disable some UI elements
        disableNodes(true);

        //Check if search field is not empty
        String searchText = searchField.getText();
        try {
            //Use wikit to return text from wikipedia based on search term
            WikitTask wikit = new WikitTask(searchField, textArea);
            executorService.submit(wikit);
            progressBar.setVisible(true);
            wikit.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {

                    //Check if the wikit search result was valid

                    if (wikit.getValue().equals(searchField.getText() + " not found :^(")) {

                        //Display alert to user before deleting unnecessary file, clearing the text field and
                        //disabling UI elements if no text is already present from a previous search

                        Alert alert = new Alert(Alert.AlertType.ERROR, "Result not found");
                        alert.show();
                        File file = new File("./" + searchField.getText() + ".txt");
                        file.delete();
                        searchField.clear();
                        progressBar.setVisible(false);
                        String isEmpty = textArea.getText();
                        if (isEmpty.equals("")) {
                            disableNodes(true);
                            textArea.setDisable(true);
                        } else {
                            disableNodes(false);
                        }
                    } else {

                        searchTextFinal = searchText;
                        //Enable UI elements again, remove progress bar and display wikit result text
                        if (comboBox.getValue() != null) {
                            disableNodes(false);
                        } else {
                            disableNodes(true);
                        }
                        selectImagesButton.setDisable(false);
                        textArea.setDisable(false);
                        textArea.setText(wikit.getValue());
                        progressBar.setVisible(false);
                    }
                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @FXML
    public void handlePreviewButton(ActionEvent actionEvent) throws IOException {
        if (previewButton.getText().equals("Preview")) {
            previewButton.setText("Stop");
            highlightedText = textArea.getSelectedText();
            String[] words = highlightedText.split("\\s+");

            //Check if the user has entered a valid amount of text and selected a speech synthesizer

            if (words.length > 40) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Highlighted text too large");
                alert.show();
                return;
            } else if (highlightedText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text");
                alert.show();
                return;
            }
            String comboBoxValue = comboBox.getValue().toString();

            //Play the selected text using the selected speech synthesizer
            previewAudio = new PreviewAudioTask(comboBoxValue, highlightedText);
            previewAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    try {
                        if (previewAudio.getValue().equals("done")) {
                            previewButton.setText("Preview");
                        }
                    } catch (Exception ignored) {
                        //stop button was pressed
                    }
                }
            });
            executorService.submit(previewAudio);
        } else {
            previewButton.setText("Preview");
            previewAudio.stopAudio();
        }
    }

    public void handlePreviewCreationButton(ActionEvent actionEvent) {
        listForCreation.setItems(audioCreationList);

        //Check if no audio files were selected for creation
        if (!checkValidCreation()) {
            return;
        }

        //Show progress and disable other UI elements while the preview creation process is occurring
        progressBar.setVisible(true);
        createButton.setDisable(true);
        previewCreationButton.setDisable(true);

        //Create preview creation for the user. Merges audio files, then gets images from Flickr, and then combines
        //these into the final creation before playing the preview for the user.
        MergeAudioTask merge = new MergeAudioTask(audioCreationList);
        executorService.submit(merge);
        merge.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {

                double audioDuration = getAudioDuration();

                String music = (String) musicDropdown.getSelectionModel().getSelectedItem();
                VideoCreationTask videoCreationTask = new VideoCreationTask(images, audioDuration, "tempfile1", searchTextFinal, music);
                executorService.submit(videoCreationTask);
                videoCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {

                        //Remove progress bar and re-enable UI elements as well as deleting unnecessary files

                        progressBar.setVisible(false);
                        createButton.setDisable(false);
                        previewCreationButton.setDisable(false);

                        new File("creations/out.mp3").delete();
                        new File("creations/out.mp4").delete();
                        new File("creations/merged.wav").delete();

                        //Create and setup Media, MediaPlayer and MediaView before creating preview popup window
                        File videoFile = new File("creations/tempfile1.mp4");
                        Media video = new Media(videoFile.toURI().toString());
                        MediaPlayer player = new MediaPlayer(video);
                        player.setAutoPlay(true);
                        MediaView mediaView = new MediaView(player);

                        mediaView.setFitHeight(360);

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("mediaPreview.fxml"));
                        BorderPane root = null;
                        try {
                            root = (BorderPane) loader.load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        root.setCenter(mediaView);
                        MediaPreviewController mediaPreviewController = loader.getController();
                        mediaPreviewController.setPlayer(player);
                        Stage stage = new Stage();
                        stage.setTitle("Preview");
                        stage.setScene(new Scene(root));
                        stage.show();
                        stage.setOnCloseRequest(e -> {
                            new File("creations/tempfile1.mp4").delete();
                        });

                    }
                });
            }
        });
    }

    @FXML
    public void handleCreateButton(ActionEvent actionEvent) {
        String creationName = textCreationName.getText();

        //Check if no audio files have been selected, if no creation name has been given and if a creation with the same
        //name already exists
        if (!checkValidCreation()) {
            return;
        }

        File tmpDir = new File("creations/" + creationName + ".mp4");
        boolean exists = tmpDir.exists();
        if (exists) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("File name already exists");
            alert.setContentText("Would you like to overwrite?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                tmpDir.delete();
            } else {
                return;
            }
        }

        //Show progress and disable other UI elements while the preview creation process is occurring
        progressBar.setVisible(true);
        disableNodes(true);
        createButton.setDisable(true);
        previewCreationButton.setDisable(true);
        searchButton.setDisable(true);

        //Create users creation. Merge selected audio files, then get Flickr images and then combine these into the
        //final creation before prompting the user to return to the main menu
        MergeAudioTask merge = new MergeAudioTask(audioCreationList);
        executorService.submit(merge);
        merge.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent workerStateEvent) {

                double audioDuration = getAudioDuration();

                String music = (String) musicDropdown.getSelectionModel().getSelectedItem();
                VideoCreationTask videoCreationTask = new VideoCreationTask(images, audioDuration, creationName, searchTextFinal, music);
                executorService.submit(videoCreationTask);
                videoCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        Creation creation = new Creation(creationName, 0, "N/A");
                        creationObservableList.add(creation);

                        //Add updated list to stored data
                        try {
                            FileOutputStream fos = new FileOutputStream("data.tmp");
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(new ArrayList<Creation>(creationObservableList));
                            oos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        progressBar.setVisible(false);
                        disableNodes(true);
                        cleanUp();
                        clearText();
                        listForCreation.getItems().clear();
                        searchButton.setDisable(false);
                        previewCreationButton.setDisable(true);
                        createButton.setDisable(true);
                        textArea.setDisable(true);

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setHeaderText("Successfully created");
                        alert.setContentText("Would you like to return to the menu?");
                        searchTextFinal = "";

                        Optional<ButtonType> result = alert.showAndWait();

                        if (result.get() == ButtonType.OK) {
                            try {
                                goHome(actionEvent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Window window = createButton.getScene().getWindow();
                            window.setHeight(506);
                            window.setWidth(647);
                        }
                    }
                });
            }
        });
    }

    @FXML
    public void handleSaveAudioButton(ActionEvent actionEvent) {
        highlightedText = textArea.getSelectedText();
        String[] words = highlightedText.split("\\s+");

        //Check if the user has entered a valid amount of text and selected a speech synthesizer
        if (words.length > 40) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Highlighted text too large");
            alert.setContentText("You exceeded by: " + (words.length - 40) + "words");
            alert.show();
            return;
        } else if (highlightedText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text");
            alert.show();
            return;
        }
        //Disable some UI elements and show the progress bar
        progressBar.setVisible(true);
        disableNodes(true);

        //Create the specified text-to-speech audio file and update audio file list
        String comboValue = comboBox.getValue().toString();
        if (comboValue.equals("Deep Voice")) {
            comboValue = "DeepVoice";
        } else {
            comboValue = "LightVoice";
        }

        //Save audio task
        AudioTask audioTask = new AudioTask(textArea.getSelectedText(), comboValue , searchTextFinal);
        executorService.submit(audioTask);
        audioTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                audioCreationList.add(audioTask.getValue());
                initialiseTable();
                //Remove the progress bar and re-enable UI elements
                progressBar.setVisible(false);
                disableNodes(false);
                //Expand window to show rest of creation step
                Window window = saveAudioButton.getScene().getWindow();
                window.setHeight(800);
                audioHbox.setVisible(true);
                creationVbox.setVisible(true);
                if ((musicDropdown.getValue() != null)) {
                    previewCreationButton.setDisable(false);
                }
                if (!textCreationName.getText().isEmpty() && (musicDropdown.getValue() != null)) {
                    createButton.setDisable(false);
                }
            }
        });
    }

    @FXML
    public void handleMoveUp(ActionEvent actionEvent) {
        int i = listForCreation.getSelectionModel().getSelectedIndex();
        if (i > 0) {
            Collections.swap(audioCreationList, i, i-1);
            listForCreation.getSelectionModel().select(i-1);
            initialiseTable();
        }
    }

    @FXML
    public void handleMoveDown(ActionEvent actionEvent) {
        int i = listForCreation.getSelectionModel().getSelectedIndex();
        if (i >= 0 && i < listForCreation.getItems().size() - 1) {
            Collections.swap(audioCreationList, i, i+1);
            listForCreation.getSelectionModel().select(i+1);
            initialiseTable();
        }
    }

    //Display a confirmation prompt to the user, confirming deletion of all audio files, before deleting all audio files
    @FXML
    public void handleDeleteAllAudioButton(MouseEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete all audio files?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            for (File file : audioCreationDir.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
            listForCreation.getItems().clear();
            listForCreation.setItems(audioCreationList);
            previewCreationButton.setDisable(true);
            createButton.setDisable(true);
        }
    }


    //Delete the selected audio files
    @FXML
    public void handleDeleteAudioButton(MouseEvent actionEvent) {
        for (String word : listForCreation.getSelectionModel().getSelectedItems()) {
            File file = new File("audioCreation/" + word + ".wav");
            file.delete();
        }
        audioCreationList.remove(listForCreation.getSelectionModel().getSelectedItem());
        initialiseTable();

        if (audioCreationList.isEmpty()) {
            previewCreationButton.setDisable(true);
            createButton.setDisable(true);
        }
    }

    @FXML
    public void handleSelectImagesButton(ActionEvent actionEvent) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("selectImages.fxml"));

        SelectImagesController selectImagesController = new SelectImagesController(this, searchTextFinal);
        loader.setController(selectImagesController);

        Stage secondaryStage = new Stage();
        secondaryStage.initModality(Modality.APPLICATION_MODAL);
        secondaryStage.initOwner((Stage) ((Node) actionEvent.getSource()).getScene().getWindow());

        AnchorPane root = null;
        try {
            root = (AnchorPane) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        secondaryStage.setTitle("Select Images");
        secondaryStage.setScene(new Scene(root, 1080, 510));
        secondaryStage.show();

        secondaryStage.setOnHiding(e -> {
            numberOfImages.setText(String.valueOf(images.size()));
        });
    }

    @FXML
    public void handlePlayAudio(MouseEvent mouseEvent){
        if (playAudio.getImage().getUrl().contains("play.png")) {
            playAudio.setImage(new Image("Images/stop.png"));
            String filePath = "audioCreation/" + listForCreation.getSelectionModel().getSelectedItem() + ".wav";
            playAudioTask = new PlayAudioTask(filePath);
            playAudioTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    try {
                        if (playAudioTask.getValue().equals("done")) {
                            playAudio.setImage(new Image("Images/play.png"));
                        }
                    } catch (Exception ignored) {
                        //stop button was pressed
                    }
                }
            });
            executorService.submit(playAudioTask);
        } else {
            playAudioTask.stopAudio();
            playAudio.setImage(new Image("Images/play.png"));
        }
    }

    //Set initial settings for UI elements and enable the user of the enter key instead of some buttons
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        musicDropdown.getItems().setAll("None", "Transmutation");
        comboBox.getItems().setAll("Deep Voice", "Light Voice");
        textArea.setWrapText(true);
        textArea.setDisable(true);
        searchButton.setDisable(true);
        previewCreationButton.setDisable(true);
        selectImagesButton.setDisable(true);
        createButton.setDisable(true);
        progressBar.setVisible(false);
        playAudio.setDisable(true);
        cleanUp();
        disableNodes(true);

        //enable play button once user clicks on an audio file
        listForCreation.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                playAudio.setDisable(false);
            }
        });

        //Check if a change to the creation name text field contains invalid characters, and if it does remove them
        textCreationName.textProperty().addListener((observable, oldValue, newValue) -> {
            if ((newValue.contains("/"))
                    || (newValue.contains("\0"))
                    || (newValue.contains("?"))
                    || (newValue.contains("%"))
                    || (newValue.contains(":"))
                    || (newValue.contains("|"))
                    || (newValue.contains("\\"))
                    || (newValue.contains("<"))
                    || (newValue.contains(">"))
                    || (newValue.contains(" "))
                    || (newValue.contains("("))
                    || (newValue.contains(")"))
                    || (newValue.contains("*"))) {
                textCreationName.setText(oldValue);
            }
            if (textCreationName.getText().trim().isEmpty()) {
                createButton.setDisable(true);
            }
            if (!listForCreation.getItems().isEmpty() && musicDropdown.getValue() != null) {
                createButton.setDisable(false);
            }
        });

        //enable search button only once user types something in search
        searchField.textProperty().addListener(observable -> {
            searchButton.setDisable(false);
            if (searchField.getText().trim().isEmpty()) {
                searchButton.setDisable(true);
            }
        });

        /*
        Allow pressing of the enter key instead of clicking the search button
         */
        searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    searchButton.fire();
                }
            }
        });

        /*
        Allow pressing of the enter key instead of clicking the create button
         */
        textCreationName.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    createButton.fire();
                }
            }
        });
    }

    /*
    Helper method to disable some UI elements
     */
    private void disableNodes(boolean b) {
        previewButton.setDisable(b);
        saveAudioButton.setDisable(b);
    }

    /*
    Helper method to remove text from UI elements
     */
    private void clearText() {
        searchField.clear();
        textArea.clear();
        textCreationName.clear();
    }

    /*
    Helper method to delete all unnecessary files
     */
    private void cleanUp() {
        audioCreationList.clear();
        for (File file : imagesDir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }

        new File("creations/out.mp3").delete();
        for (File file : audioCreationDir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }

        new File("creations/out.mp4").delete();
        new File("creations/merged.wav").delete();
    }

    /*
    Helper method to populate the audioList table
     */
    private void initialiseTable() {
        listForCreation.setItems(audioCreationList);
    }


    public void handleComboBox(ActionEvent actionEvent) {
        if (!searchField.getText().isEmpty()) {
            saveAudioButton.setDisable(false);
            previewButton.setDisable(false);
        }
    }

    public void handleMusicComboBox(ActionEvent actionEvent) {
        if (!textCreationName.getText().isEmpty() && !listForCreation.getItems().isEmpty()) {
            createButton.setDisable(false);
        }
        if (!listForCreation.getItems().isEmpty()) {
            previewCreationButton.setDisable(false);
        }
    }

    public boolean checkValidCreation() {
        if (audioCreationList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please transfer at least 1 audio file for creation");
            alert.show();
            return false;
        } else if (images.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select at least 1 image for creation");
            alert.show();
            return false;
        }
        return true;
    }

    public double getAudioDuration(){
        File audioFile = new File("creations/merged.wav");
        double audioDuration = 0;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat audioFormat = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            audioDuration = frames / audioFormat.getFrameRate();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioDuration;
    }

    public void goHome(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("mainMenu.fxml"));
        Parent mainParent = loader.load();

        Scene mainMenu = new Scene(mainParent);

        MainMenuController mainMenuController = loader.getController();
        mainMenuController.initData(creationObservableList);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
        window.setHeight(437);
        window.setWidth(640);
    }
}
