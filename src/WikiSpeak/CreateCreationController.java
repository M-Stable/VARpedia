package WikiSpeak;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private ListView<String> listAudio = new ListView<>();

    @FXML
    private ListView<String> listForCreation = new ListView<>();

    @FXML
    private Button previewButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button saveAudioButton;
    @FXML
    private Button createButton;
    @FXML
    private ProgressBar progressBar;

    private File audioDir = new File("audio/");
    private File audioCreationDir = new File("audioCreation/");
    private File imagesDir = new File("images/");

    private String highlightedText="";
    private String searchText= "";

    private ObservableList<String> audioCreationList = FXCollections.observableArrayList();

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        Parent mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
    }

    @FXML
    public void handleSearchButton(ActionEvent actionEvent) {
        disableNodes(true);
        //get search text
        searchText = searchField.getText();
        //check if empty
        if ((searchText != null && !searchText.isEmpty())) {
            try {
                //helper thread to do process
                WikitTask wikit = new WikitTask(searchField, textArea);
                executorService.submit(wikit);
                progressBar.setVisible(true);
                wikit.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    //check if it is a valid search
                    public void handle(WorkerStateEvent workerStateEvent) {
                        if (wikit.getValue().equals(searchField.getText() + " not found :^(")) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Result not found");
                            alert.show();
                            File file = new File("./" + searchField.getText() + ".txt");
                            file.delete();
                            clearText();
                            textArea.setDisable(true);
                            progressBar.setDisable(true);
                            return;
                        }else {
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
        String comboBoxValue = comboBox.getValue().toString();
        String[] words = highlightedText.split("\\s+");
        if (words.length > 40) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Highlighted text too large");
            alert.show();

            return;
        } else if (highlightedText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text");
            alert.show();

            return;
        } else if (comboBoxValue == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a synthesizer");
            alert.show();

            return;
        }
        PreviewAudio previewAudio = new PreviewAudio(comboBoxValue, highlightedText);
        executorService.submit(previewAudio);
        previewButton.setDisable(true);
        saveAudioButton.setDisable(true);
        previewAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                previewButton.setDisable(false);
                saveAudioButton.setDisable(false);
            }
        });

    }


    public void handlePreviewCreationButton(ActionEvent actionEvent) {
        String creationName = textCreationName.getText();
        if (audioCreationList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please transfer at least 1 audio file for creation");
            alert.show();
            return;
        }
        //check if field is empty and if it already exists
        if (!creationName.isEmpty()) {
            if(creationName.contains("\"") || creationName.contains("\'") || creationName.contains("\\")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid creation name. Cannot contain \\, \" or \'");
                alert.show();
                return;
            } else {
                File tmpDir = new File("creations/" + creationName + ".mp4");
                boolean exists = tmpDir.exists();
                if (exists) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText("File name already exists");
                    alert.setContentText("Would you like to overwrite?");

                    Optional<ButtonType> result = alert.showAndWait();

                    if(result.get() == ButtonType.OK) {
                        tmpDir.delete();
                    } else {
                        return;
                    }
                }
                progressBar.setVisible(true);
                MergeAudio merge = new MergeAudio(audioCreationList);
                executorService.submit(merge);
                merge.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        FlickrTask flickrTask = new FlickrTask((Integer) spinner.getValue(), creationName);
                        executorService.submit(flickrTask);
                        flickrTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent workerStateEvent) {

                                if(flickrTask.getValue().equals("fail")) {
                                    new File("creations/merged.wav").delete();
                                    progressBar.setVisible(false);
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "No images found. Please enter a different creation name");
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

                                    VideoCreationTask videoCreationTask = new VideoCreationTask(images, audioDuration, creationName);
                                    executorService.submit(videoCreationTask);
                                    videoCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent workerStateEvent) {
                                            progressBar.setVisible(false);
                                            for(File file: imagesDir.listFiles()) {
                                                if (!file.isDirectory()) {
                                                    file.delete();
                                                }
                                            }
                                            new File("creations/out.mp4").delete();
                                            new File("creations/merged.wav").delete();


                                            File videoFile = new File("creations/" + creationName + ".mp4");
                                            Media video = new Media(videoFile.toURI().toString());
                                            MediaPlayer player = new MediaPlayer(video);
                                            player.setAutoPlay(true);
                                            MediaView mediaView = new MediaView(player);

                                            mediaView.setFitHeight(720);

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


//                                            Stage window = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
//                                            window.setScene(new Scene(root));
//                                            window.show();

                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please type in creation name");
            alert.show();
        }
    }

    @FXML
    public void handleCreateButton(ActionEvent actionEvent) {
        String creationName = textCreationName.getText();
        if (audioCreationList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please transfer at least 1 audio file for creation");
            alert.show();
            return;
        }
        //check if field is empty and if it already exists
        if (!creationName.isEmpty()) {
            if(creationName.contains("\"") || creationName.contains("\'") || creationName.contains("\\")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid creation name. Cannot contain \\, \" or \'");
                alert.show();
                return;
            } else {
                File tmpDir = new File("creations/" + creationName + ".mp4");
                boolean exists = tmpDir.exists();
                if (exists) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText("File name already exists");
                    alert.setContentText("Would you like to overwrite?");

                    Optional<ButtonType> result = alert.showAndWait();

                    if(result.get() == ButtonType.OK) {
                        tmpDir.delete();
                    } else {
                        return;
                    }
                }
                progressBar.setVisible(true);
                disableNodes(true);
                searchButton.setDisable(true);
                MergeAudio merge = new MergeAudio(audioCreationList);
                executorService.submit(merge);
                merge.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        FlickrTask flickrTask = new FlickrTask((Integer) spinner.getValue(), creationName);
                        executorService.submit(flickrTask);
                        flickrTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent workerStateEvent) {

                                if(flickrTask.getValue().equals("fail")) {
                                    new File("creations/merged.wav").delete();
                                    progressBar.setVisible(false);
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "No images found. Please enter a different creation name");
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

                                    VideoCreationTask videoCreationTask = new VideoCreationTask(images, audioDuration, creationName);
                                    executorService.submit(videoCreationTask);
                                    videoCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent workerStateEvent) {
                                            progressBar.setVisible(false);
                                            disableNodes(false);
                                            cleanUp();
                                            initialiseTable();
                                            searchButton.setDisable(false);
                                            clearText();
                                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                            alert.setHeaderText("Successfully created");
                                            alert.setContentText("Would you like to play your creation?");

                                            Optional<ButtonType> result = alert.showAndWait();

                                            if(result.get() == ButtonType.OK) {
                                                File videoFile = new File("creations/" + creationName + ".mp4");
                                                Media video = new Media(videoFile.toURI().toString());
                                                MediaPlayer player = new MediaPlayer(video);
                                                player.setAutoPlay(true);
                                                MediaView mediaView = new MediaView(player);

                                                mediaView.setFitHeight(720);


                                                FXMLLoader loader = new FXMLLoader(getClass().getResource("media.fxml"));
                                                BorderPane root = null;
                                                try {
                                                    root = (BorderPane) loader.load();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                root.setCenter(mediaView);
                                                MediaController mediaController = loader.getController();
                                                mediaController.setPlayer(player);

                                                Stage window = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
                                                window.setScene(new Scene(root));
                                                window.show();


                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please type in creation name");
            alert.show();
        }

    }

    @FXML
    public void handleSendToCreationButton(ActionEvent actionEvent) throws IOException {
        for (String word : listAudio.getSelectionModel().getSelectedItems()){
            Files.move(Paths.get("audio/" + word + ".wav"),
                    Paths.get("audioCreation/" + word + ".wav"));
            audioCreationList.add(word);
        }
        initialiseTable();
    }

    @FXML
    public void handleRemoveAudioButton(ActionEvent actionEvent) throws IOException {
        ObservableList<String> selected = listForCreation.getSelectionModel().getSelectedItems();
        for (String word : selected){
            Files.move(Paths.get("audioCreation/" + word + ".wav"),
                    Paths.get("audio/" + word + ".wav"));
        }
        int size = selected.size();
        String[] temp = new String[size];
        for (int i = 0; i<size; i++) {
            temp[i] = selected.get(i);
        }
        for (int i = 0; i<size; i++) {
            audioCreationList.remove(temp[i]);
        }
        initialiseTable();
    }

    @FXML
    public void handleDeleteAllAudioButton(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete all files?");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.get() == ButtonType.OK) {
            for(File file: audioDir.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
            initialiseTable();
        }


    }

    @FXML
    public void handleDeleteAudioButton(ActionEvent actionEvent) {
        for (String word : listAudio.getSelectionModel().getSelectedItems()){
            File file = new File("audio/" + word + ".wav");
            file.delete();
        }
        initialiseTable();
    }

    @FXML
    public void handleSaveAudioButton(ActionEvent actionEvent) {

        highlightedText = textArea.getSelectedText();
        String[] words = highlightedText.split("\\s+");
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
        String audioName = AudioName.display();
        if (audioName.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Enter a file name");
            alert.show();
            return;
        }
        File tmpDir = new File("audio/" + audioName + "_" + comboBox.getValue().toString() + ".wav");
        boolean exists = tmpDir.exists();
        if (exists) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File with that name already exists");
            alert.show();
            return;
        }
        File tmpDir1 = new File("audioCreation/" + audioName + "_" + comboBox.getValue().toString() + ".wav");
        boolean check = tmpDir1.exists();
        if (check) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File with that name already exists");
            alert.show();
            return;
        }
        AudioTask audioTask = new AudioTask(textArea.getSelectedText(), comboBox.getValue().toString(), audioName);
        executorService.submit(audioTask);
        progressBar.setVisible(true);
        disableNodes(true);
        audioTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                if (audioTask.getValue().equals("yes")) {
                    initialiseTable();
                }
                progressBar.setVisible(false);
                disableNodes(false);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboBox.getItems().setAll("Festival", "eSpeak");
        textArea.setDisable(true);
        textArea.setWrapText(true);
        listAudio.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listForCreation.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SpinnerValueFactory<Integer> imagesValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10);
        this.spinner.setValueFactory(imagesValueFactory);
        progressBar.setVisible(false);
        cleanUp();
        initialiseTable();
        disableNodes(true);
        createButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: #FFFAF0;");

        searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    searchButton.fire();
                }
            }
        });
        textCreationName.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    createButton.fire();
                }
            }
        });
    }

    private void disableNodes(boolean b) {
        previewButton.setDisable(b);
        saveAudioButton.setDisable(b);
    }

    private void clearText() {
        searchField.clear();
        textArea.clear();
        textCreationName.clear();
    }

    private void cleanUp() {
        audioCreationList.clear();
        for(File file: imagesDir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }

        for(File file: audioCreationDir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }

        new File("creations/out.mp4").delete();
        new File("creations/merged.wav").delete();
    }

    private void initialiseTable(){
        File[] creations = audioDir.listFiles();

        Arrays.sort(creations, (f1, f2) -> f1.compareTo(f2));
        listAudio.getItems().clear();

        for(File creation : creations) {
            if(creation.getName().contains(".wav")) {
                listAudio.getItems().add(creation.getName().replace(".wav", ""));
            }
        }

        listForCreation.setItems(audioCreationList);
    }

}
