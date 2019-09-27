package WikiSpeak;

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
    private Button createButton;
    @FXML
    private Button saveAudioButton;


    private File audioDir = new File("audio/");
    private File audioCreationDir = new File("audioCreation/");
    private File imagesDir = new File("images/");

    private String highlightedText="";
    private String searchText= "";

    @FXML
    private ProgressBar progressBar;

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
        String[] words = highlightedText.split("\\s+");
        if (words.length > 40) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Highlighted text too large");
            alert.show();

            return;
        }
        if (highlightedText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text");
            alert.show();

            return;
        }
        try {
            String command = "";
            if (comboBox.getValue().equals("Festival")) {
                command = "echo \"" + highlightedText + "\" | festival --tts";
            } else if (comboBox.getValue().equals("eSpeak")) {
                command = "espeak \"" + highlightedText + "\"";

            }
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.start();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a synthesizer");
            alert.show();
        }
    }

    @FXML
    public void handleCreateButton(ActionEvent actionEvent) {
        //check if field is empty and if it already exists
        if (!textCreationName.getText().isEmpty()) {
            progressBar.setVisible(true);
            MergeAudio merge = new MergeAudio();
            executorService.submit(merge);
            merge.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    FlickrTask flickrTask = new FlickrTask((Integer) spinner.getValue(), textCreationName.getText());
                    executorService.submit(flickrTask);
                    flickrTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {

                            if(flickrTask.getValue().equals("fail")) {
                                new File("creations/merged.wav").delete();
                                progressBar.setVisible(false);
                                Alert alert = new Alert(Alert.AlertType.ERROR, "No images found. Please enter a different creation name");
                                alert.show();
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

                                VideoCreationTask videoCreationTask = new VideoCreationTask(images, audioDuration, textCreationName.getText(), searchField.getText());
                                executorService.submit(videoCreationTask);
                                videoCreationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                    @Override
                                    public void handle(WorkerStateEvent workerStateEvent) {

                                        cleanUp();
                                        initialiseTable();

                                        progressBar.setVisible(false);

                                        /*try {
                                            handleBackButton(new ActionEvent());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/
                                        //handleBackButton(new ActionEvent());
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

    @FXML
    public void handleSendToCreationButton(ActionEvent actionEvent) throws IOException {
        for (String word : listAudio.getSelectionModel().getSelectedItems()){
           Files.move(Paths.get("audio/" + word + ".wav"),
                            Paths.get("audioCreation/" + word + ".wav"));
        }
        initialiseTable();
    }

    @FXML
    public void handleRemoveAudioButton(ActionEvent actionEvent) throws IOException {
        for (String word : listForCreation.getSelectionModel().getSelectedItems()){
            Files.move(Paths.get("audioCreation/" + word + ".wav"),
                    Paths.get("audio/" + word + ".wav"));
        }
        initialiseTable();
    }

    @FXML
    public void handleDeleteAllAudioButton(ActionEvent actionEvent) {
        for(File file: audioDir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
        initialiseTable();

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
        }
        if (highlightedText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select some text");
            alert.show();
        }
        if (comboBox.getValue() == null) {
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

        new File("video/video.mp4").delete();
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

        File[] creation1 = audioCreationDir.listFiles();
        Arrays.sort(creation1, (f1, f2) -> f1.compareTo(f2));
        listForCreation.getItems().clear();

        for(File creation2 : creation1) {
            if(creation2.getName().contains(".wav")) {
                listForCreation.getItems().add(creation2.getName().replace(".wav", ""));
            }
        }
    }
}
