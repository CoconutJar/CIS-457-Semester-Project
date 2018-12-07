import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML private String username, password;
    @FXML private User user;

    @FXML private MenuButton unLabel;
    @FXML private MenuItem logoutBtn;
    @FXML private ListView onlineFriendsListView, offlineFriendsListView, chatsListView;
    @FXML private Button searchFriendBtn, newChatBtn;
    @FXML private Label hubLabel;
    

    // Data received from ConnectController
    public void initData(User user, String username, String password) {
        this.username = username;
        this.password = password;

        this.user = user;

        // Set connection session attributes banner
        sessionAttributes();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void sessionAttributes(){
        unLabel.setText(username);
    }

    public void searchFriendBtnPushed(){
        System.out.println("Search Friend Button Pressed.");
    }

    public void newChatBtnPushed(){
        System.out.println("New Chat Button Pressed.");
    }

    public void logoutBtnPushed(){
        Stage stage = (Stage) hubLabel.getScene().getWindow();
        System.out.println("Application closed.");
        stage.close();
        System.exit(1);
    }
}
