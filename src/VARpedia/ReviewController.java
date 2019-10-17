package VARpedia;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ReviewController {
    public ImageView star1;
    public ImageView star2;
    public ImageView star3;
    public ImageView star4;
    public ImageView star5;

    public void handlePlayButton(ActionEvent actionEvent) {
    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        Parent mainParent = FXMLLoader.load(getClass().getResource("mainMenu.fxml"));
        Scene mainMenu = new Scene(mainParent);

        Stage window = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        window.setScene(mainMenu);
        window.show();
    }

    public void handleClick1(MouseEvent mouseEvent) {
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-48.png"));
        star3.setImage(new Image("Images/icons8-star-48.png"));
        star4.setImage(new Image("Images/icons8-star-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }

    public void handleEnter1(MouseEvent mouseEvent) {
    }

    public void handleExit1(MouseEvent mouseEvent) {
    }

    public void handleClick2(MouseEvent mouseEvent) {
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-48.png"));
        star4.setImage(new Image("Images/icons8-star-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }

    public void handleEnter2(MouseEvent mouseEvent) {
    }

    public void handleExit2(MouseEvent mouseEvent) {
    }

    public void handleClick3(MouseEvent mouseEvent) {
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-filled-48.png"));
        star4.setImage(new Image("Images/icons8-star-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }

    public void handleEnter3(MouseEvent mouseEvent) {
    }

    public void handleExit3(MouseEvent mouseEvent) {
    }

    public void handleClick4(MouseEvent mouseEvent) {
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-filled-48.png"));
        star4.setImage(new Image("Images/icons8-star-filled-48.png"));
        star5.setImage(new Image("Images/icons8-star-48.png"));
    }

    public void handleEnter4(MouseEvent mouseEvent) {
    }

    public void handleExit4(MouseEvent mouseEvent) {
    }

    public void handleClick5(MouseEvent mouseEvent) {
        star1.setImage(new Image("Images/icons8-star-filled-48.png"));
        star2.setImage(new Image("Images/icons8-star-filled-48.png"));
        star3.setImage(new Image("Images/icons8-star-filled-48.png"));
        star4.setImage(new Image("Images/icons8-star-filled-48.png"));
        star5.setImage(new Image("Images/icons8-star-filled-48.png"));
    }

    public void handleEnter5(MouseEvent mouseEvent) {
    }

    public void handleExit5(MouseEvent mouseEvent) {
    }

}
