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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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
    public void handleBackButton(ActionEvent event) throws IOException {
        Parent mainParent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
    }

    @FXML
    public void handleSearchButton(ActionEvent actionEvent) {
        textArea.setWrapText(true);
        //get search text
        String searchText = searchField.getText();
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
    private void clearText() {
        searchField.clear();
        textArea.clear();
    }

    @FXML
    public void handlePreviewButton(ActionEvent actionEvent) throws IOException {
        String highlightedText = textArea.getSelectedText();
        if (highlightedText.isEmpty()) {
            AlertBox.display("Error", "Please select some text", "FF6347");
        }
        try {
            if (comboBox.getValue().equals("Festival")) {
                String command = "echo \"" + highlightedText + "\" | festival --tts";
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
                pb.start();
            } else if (comboBox.getValue().equals("eSpeak")) {

            }
        } catch (Exception e) {
            AlertBox.display("Error", "Please select a synthesizer", "FF6347");
        }
    }

    @FXML
    public void handleCreateButton(ActionEvent actionEvent) {
        //check if field is empty and if it already exists
        if (!textCreationName.getText().isEmpty()) {

        } else {
            AlertBox.display("ERROR", "Please type in creation name", "FF6347");
        }

    }

    @FXML
    public void handleAddButton(ActionEvent actionEvent) {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboBox.getItems().setAll("Festival", "eSpeak");
    }
}
