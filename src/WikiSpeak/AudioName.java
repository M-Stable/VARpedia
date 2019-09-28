package WikiSpeak;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AudioName {
    public static String display() {
        Stage alert = new Stage();

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Audio");
        alert.setMinWidth(400);

        TextField textField = new TextField();


        Label label = new Label("What would you like to name your audio?");
        Button btnYes = new Button("confirm");
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    btnYes.fire();
                }
            }
        });
        btnYes.setOnAction(e -> alert.close());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10,10,10,10));
        layout.getChildren().addAll(label, textField, btnYes);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        alert.setScene(scene);
        alert.showAndWait();

        return textField.getText();
    }
}
