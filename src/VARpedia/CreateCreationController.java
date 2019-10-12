package VARpedia;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CreateCreationController implements Initializable {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private TextField searchField;

    @FXML
    private TextArea textArea;

    @FXML
    private TextField textCreationName;

    @FXML
    private ComboBox comboBox;

    @FXML
    private Spinner spinner;

    @FXML
    private ListView<String> listForCreation = new ListView<>();

    @FXML
    private Button previewButton;
    @FXML
    private Button previewCreationButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button saveAudioButton;
    @FXML
    private Button createButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ComboBox musicDropdown;
    @FXML
    private Button selectImagesButton;

    private File audioDir = new File("audio/");
    private File audioCreationDir = new File("audioCreation/");
    private File imagesDir = new File("images/");


    private String highlightedText = "";
    private String searchTextFinal = "";

    private ObservableList<String> audioCreationList = FXCollections.observableArrayList();

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        //Switch scene back to the main menu

        Parent mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
    }

    @FXML
    public void handleSearchButton(ActionEvent actionEvent) {

        //Disable some UI elements

        disableNodes(true);

        //Check if search field is not empty

        String searchText = searchField.getText();
        if ((searchText != null && !searchText.isEmpty())) {
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

                            previewCreationButton.setDisable(false);
                            createButton.setDisable(false);
                            textArea.setDisable(false);
                            textArea.setText(wikit.getValue());
                            progressBar.setVisible(false);
                            disableNodes(false);
                        }

                    }
                });
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a search term");
            alert.show();
        }
    }

    @FXML
    public void handlePreviewButton(ActionEvent actionEvent) throws IOException {
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
        } else if (comboBox.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a synthesizer");
            alert.show();

            return;
        }

        String comboBoxValue = comboBox.getValue().toString();


        //Disable some UI elements
        previewButton.setDisable(true);
        saveAudioButton.setDisable(true);


        //Play the selected text using the selected speech synthesizer
        PreviewAudio previewAudio = new PreviewAudio(comboBoxValue, highlightedText);
        executorService.submit(previewAudio);
        previewAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {

                //re-enable UI elements
                previewButton.setDisable(false);
                saveAudioButton.setDisable(false);
            }
        });

    }


    public void handlePreviewCreationButton(ActionEvent actionEvent) {
        String creationName = textCreationName.getText();

        //Check if no audio files were selected for creation
        if (audioCreationList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please transfer at least 1 audio file for creation");
            alert.show();
            return;
        }


        //Show progress and disable other UI elements while the preview creation process is occurring
        progressBar.setVisible(true);
        createButton.setDisable(true);
        previewCreationButton.setDisable(true);

        //Create preview creation for the user. Merges audio files, then gets images from Flickr, and then combines
        //these into the final creation before playing the preview for the user.

        MergeAudio merge = new MergeAudio(audioCreationList);
        executorService.submit(merge);
        merge.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                FlickrTask flickrTask = new FlickrTask((Integer) spinner.getValue(), searchTextFinal);
                executorService.submit(flickrTask);
                flickrTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {

                        //Check if no images were found

                        if (flickrTask.getValue().equals("fail")) {
                            new File("creations/merged.wav").delete();
                            progressBar.setVisible(false);
                            Alert alert = new Alert(Alert.AlertType.ERROR, "No images found. Please enter a different search term");
                            alert.show();
                            return;
                        } else {
                            List<File> images = flickrTask.getImages();
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

                                    for (File file : imagesDir.listFiles()) {
                                        if (!file.isDirectory()) {
                                            file.delete();
                                        }
                                    }
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

        if (audioCreationList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please transfer at least 1 audio file for creation");
            alert.show();
            return;
        }
        if (!creationName.isEmpty()) {
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
            MergeAudio merge = new MergeAudio(audioCreationList);
            executorService.submit(merge);
            merge.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    FlickrTask flickrTask = new FlickrTask((Integer) 10, searchTextFinal);
                    executorService.submit(flickrTask);
                    flickrTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            /*
                            Check if no images were found
                             */
                            if (flickrTask.getValue().equals("fail")) {
                                new File("creations/merged.wav").delete();
                                progressBar.setVisible(false);
                                Alert alert = new Alert(Alert.AlertType.ERROR, "No images found. Please enter a different search term");
                                alert.show();
                                return;
                            } else {
                                List<File> images = flickrTask.getImages();
                                File audioFile = new File("creations/merged.wav");
                                double audioDuration = 0;
                                try {
                                    /*
                                    Get the length of the merged audio for the video creation
                                     */
                                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                                    AudioFormat audioFormat = audioInputStream.getFormat();
                                    long frames = audioInputStream.getFrameLength();
                                    audioDuration = frames / audioFormat.getFrameRate();
                                } catch (UnsupportedAudioFileException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String music = (String) musicDropdown.getSelectionModel().getSelectedItem();
                                VideoCreationTask videoCreationTask = new VideoCreationTask(images, audioDuration, creationName, searchTextFinal, music);
                                executorService.submit(videoCreationTask);
                                videoCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                                    @Override
                                    public void handle(WorkerStateEvent workerStateEvent) {
                                        progressBar.setVisible(false);
                                        disableNodes(true);
                                       // spinner.getValueFactory().setValue(1);
                                        cleanUp();
                                        initialiseTable();
                                        searchButton.setDisable(false);
                                        clearText();
                                        previewCreationButton.setDisable(true);
                                        createButton.setDisable(true);
                                        textArea.setDisable(true);
                                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                        alert.setHeaderText("Successfully created");
                                        alert.setContentText("Would you like to return to the menu?");

                                        Optional<ButtonType> result = alert.showAndWait();

                                        if (result.get() == ButtonType.OK) {
                                            Parent mainParent = null;
                                            try {
                                                mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            Scene mainMenu = new Scene(mainParent);

                                            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                                            window.setScene(mainMenu);
                                            window.show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please type in creation name");
            alert.show();
        }

    }

    /*
    Display a confirmation prompt to the user, confirming deletion of all audio files, before deleting all audio files
     */
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
            initialiseTable();
        }
    }

    /*
    Delete the selected audio files
     */
    @FXML
    public void handleDeleteAudioButton(MouseEvent actionEvent) {
        for (String word : listForCreation.getSelectionModel().getSelectedItems()) {
            File file = new File("audioCreation/" + word + ".wav");
            file.delete();
        }
        initialiseTable();
    }

    @FXML
    public void handleSaveAudioButton(ActionEvent actionEvent) {
        highlightedText = textArea.getSelectedText();
        String[] words = highlightedText.split("\\s+");
        /*
        Check if the user has entered a valid amount of text and selected a speech synthesizer
         */
        if (words.length > 40) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Highlighted text too large");
            alert.show();
            return;
        } else if (highlightedText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text");
            alert.show();
            return;
        } else if (comboBox.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a synthesizer");
            alert.show();
            return;
        }
        /*
        Disable some UI elements and show the progress bar
         */
        progressBar.setVisible(true);
        disableNodes(true);

        /*
        Create the specified text-to-speech audio file and update audio file list
         */
        AudioTask audioTask = new AudioTask(textArea.getSelectedText(), comboBox.getValue().toString(), searchTextFinal);
        executorService.submit(audioTask);
        audioTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                if (audioTask.getValue().equals("yes")) {
                    initialiseTable();
                }
                /*
                Remove the progress bar and re-enable UI elements
                 */
                progressBar.setVisible(false);
                disableNodes(false);
            }
        });
    }

    /*
    Set initial settings for UI elements and enable the user of the enter key instead of some buttons
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        musicDropdown.getItems().setAll("None", "Transmutation", "", "", "");
        comboBox.getItems().setAll("Festival", "eSpeak");
        textArea.setDisable(true);
        textArea.setWrapText(true);
        previewCreationButton.setDisable(true);
        createButton.setDisable(true);
        listForCreation.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      //  SpinnerValueFactory<Integer> imagesValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10);
        //this.spinner.setValueFactory(imagesValueFactory);
        progressBar.setVisible(false);
        cleanUp();
        initialiseTable();
        disableNodes(true);
        createButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: #FFFAF0;");

        /*
         Check if a change to the creation name text field contains invalid characters, and if it does remove them
         */
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
        File[] creations = audioCreationDir.listFiles();

        Arrays.sort(creations, (f1, f2) -> f1.compareTo(f2));
        listForCreation.getItems().clear();

        for (File creation : creations) {
            if (creation.getName().contains(".wav")) {
                listForCreation.getItems().add(creation.getName().replace(".wav", ""));
            }
        }

        listForCreation.setItems(audioCreationList);
    }

    public void handleSelectImagesButton(ActionEvent actionEvent) {
    }

    public void handlePlayAudio(MouseEvent mouseEvent) {
    }
}
