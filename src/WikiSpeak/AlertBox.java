package WikiSpeak;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox {

    public static void display(String title, String message, String colour) {
        Stage alert = new Stage();

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        alert.setMinWidth(400);

        Label label = new Label(message);
        Button btnYes = new Button("OK");
        btnYes.setOnAction(e -> alert.close());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: " + colour + ";");
        layout.setPadding(new Insets(10,10,10,10));
        layout.getChildren().addAll(label, btnYes);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        alert.setScene(scene);
        alert.showAndWait();
    }
}
