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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private File audioDir = new File("audio/");
    private File audioCreationDir = new File("audioCreation/");

    private String highlightedText="";
    private String searchText= "";

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
        textArea.setDisable(false);
        //get search text
        searchText = searchField.getText();
        //check if empty
        if ((searchText != null && !searchText.isEmpty())) {
            try {
                //helper thread to do process
                WikitTask wikit = new WikitTask(searchField, textArea);
                executorService.submit(wikit);
                wikit.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    //check if it is a valid search
                    public void handle(WorkerStateEvent workerStateEvent) {
                        if (wikit.getValue().equals(searchField.getText() + " not found :^(")) {
                            AlertBox.display("ERROR", "No result found", "FF6347");
                            File file = new File("./" + searchField.getText() + ".txt");
                            file.delete();
                            clearText();
                            textArea.setDisable(true);

                            return;
                        }else {
                            textArea.setText(wikit.getValue());
                        }

                    }
                });
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            AlertBox.display("Error", "Please enter a search term", "FF6347");
        }
    }

    @FXML
    public void handlePreviewButton(ActionEvent actionEvent) throws IOException {
        highlightedText = textArea.getSelectedText();
        String[] words = highlightedText.split("\\s+");
        if (words.length > 40) {
            AlertBox.display("Error", "Highlighted text too large", "FF6347");

            return;
        }
        if (highlightedText.isEmpty()) {
            AlertBox.display("Error", "Please select some text", "FF6347");
        }
        try {
            if (comboBox.getValue().equals("Festival")) {
                String command = "echo \"" + highlightedText + "\" | festival --tts";
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
                pb.start();
            } else if (comboBox.getValue().equals("eSpeak")) {
                String command = "espeak \"" + highlightedText + "\"";
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
                pb.start();
            }
        } catch (Exception e) {
            AlertBox.display("Error", "Please select a synthesizer", "FF6347");
        }
    }

    @FXML
    public void handleCreateButton(ActionEvent actionEvent) {
        //check if field is empty and if it already exists
        if (!textCreationName.getText().isEmpty()) {
            List audioFiles = listForCreation.getItems();

        } else {
            AlertBox.display("ERROR", "Please type in creation name", "FF6347");
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
            File file = new File("audio/" + word);
            file.delete();
        }
        initialiseTable();
    }

    @FXML
    public void handleSaveAudioButton(ActionEvent actionEvent) {
        highlightedText = textArea.getSelectedText();
        String[] words = highlightedText.split("\\s+");
        if (words.length > 40) {
            AlertBox.display("Error", "Highlighted text too large", "FF6347");
            return;
        }
        if (highlightedText.isEmpty()) {
            AlertBox.display("Error", "Please select some text", "FF6347");
        }
        if (comboBox.getValue() == null) {
            AlertBox.display("Error", "Please select a synthesizer", "FF6347");
            return;
        }
        String audioName = AudioName.display();
        AudioTask audioTask = new AudioTask(textArea.getSelectedText(), comboBox.getValue().toString(), audioName);
        executorService.submit(audioTask);
        audioTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                initialiseTable();
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
        initialiseTable();
    }

    private void clearText() {
        searchField.clear();
        textArea.clear();
    }

    private void initialiseTable(){
        File[] creations = audioDir.listFiles();

        listAudio.getItems().clear();

        for(File creation : creations) {
            if(creation.getName().contains(".wav")) {
                listAudio.getItems().add(creation.getName().replace(".wav", ""));
            }
        }

        File[] creation1 = audioCreationDir.listFiles();

        listForCreation.getItems().clear();

        for(File creation2 : creation1) {
            if(creation2.getName().contains(".wav")) {
                listForCreation.getItems().add(creation2.getName().replace(".wav", ""));
            }
        }
    }
}
