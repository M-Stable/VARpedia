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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CreateCreationController implements Initializable {
    @FXML
    public ImageView deleteAllAudioButton;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private TextField searchField, textCreationName;
    @FXML
    private TextArea textArea;
    @FXML
    private ComboBox comboBox, musicDropdown;
    @FXML
    private Spinner spinner;
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

    public List<File> images = new ArrayList<File>();


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

        if (!searchTextFinal.equals("")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Delete audio files?");
            alert.setContentText("Searching another term results in deleting all saved audio files");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
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
            } else {
                return;
            }
        }

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
        listForCreation.setItems(audioCreationList);

        //Check if no audio files were selected for creation
        if (audioCreationList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please transfer at least 1 audio file for creation");
            alert.show();
            return;
        } else if (images.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select at least 1 image for creation");
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
                            cleanUp();
                            listForCreation.getItems().clear();
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
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please type in creation name");
            alert.show();
        }

    }

    @FXML
    public void handleSaveAudioButton(ActionEvent actionEvent) {
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
    }

    @FXML
    public void handlePlayAudio(MouseEvent mouseEvent){
        String filePath = "audioCreation/" + listForCreation.getSelectionModel().getSelectedItem() + ".wav";
        PlayAudio play = new PlayAudio(filePath);
        play.start();

    }


    //Set initial settings for UI elements and enable the user of the enter key instead of some buttons
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        musicDropdown.getItems().setAll("None", "Transmutation");
        comboBox.getItems().setAll("Deep Voice", "Light Voice");
        textArea.setDisable(true);
        textArea.setWrapText(true);
        previewCreationButton.setDisable(true);
        selectImagesButton.setDisable(true);
        createButton.setDisable(true);
        progressBar.setVisible(false);
        cleanUp();
        disableNodes(true);
        createButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: #FFFAF0;");

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
            if (textCreationName.getText().equals("")) {
                createButton.setDisable(true);
            }
            if (!listForCreation.getItems().isEmpty() && musicDropdown.getValue() != null) {
                createButton.setDisable(false);
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
        previewButton.setDisable(false);
        saveAudioButton.setDisable(false);
    }

    public void handleMusicComboBox(ActionEvent actionEvent) {
        if (!textCreationName.getText().isEmpty() && !listForCreation.getItems().isEmpty()) {
            createButton.setDisable(false);
        }
        if (!listForCreation.getItems().isEmpty()) {
            previewCreationButton.setDisable(false);
        }
    }
}
