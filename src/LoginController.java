import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable{
    @FXML private Label closeBtn;
    @FXML private TextField unTextfield, pwTextfield;
    @FXML private Button loginBtn;

    private double x, y;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    // Handles closing of window
    public void closeBtnAction() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        System.out.println("Application closed.");
        stage.close();
        System.exit(1);
    }

    // Prints user input to system
    private void printConnectInput() {
        System.out.println("Username: " + unTextfield.getText() +
                            "\nPassword: " + pwTextfield.getText());
    }

    // Handles "Connect" Button. Submits connection/user information.
    // Changes window to FileTable window.
    public void loginBtnPushed(ActionEvent event) throws IOException {
        printConnectInput();
        User user = new User(unTextfield.getText(), pwTextfield.getText());

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("home.fxml"));
        Parent fileTableParent = loader.load();

        // Setup for window (stage) change.
        Scene fileTableScene = new Scene(fileTableParent);

        HomeController controller = loader.getController();
        controller.initData(user, unTextfield.getText(), pwTextfield.getText());

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(fileTableScene);

        // Handles "click and drag" functionality of window
        fileTableParent.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });
        fileTableParent.setOnMouseDragged(e -> {
            window.setX(e.getScreenX() - x);
            window.setY(e.getScreenY() - y);
        });

        window.show();
    }
}

