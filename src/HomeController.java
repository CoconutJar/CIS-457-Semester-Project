import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML private String username, password;
    @FXML private User user;

    @FXML private MenuButton unLabel;
    @FXML private MenuItem logoutBtn;
    @FXML private ListView<String> onlineFriendsListView;
    @FXML private ListView<String> offlineFriendsListView;
    @FXML private Button searchFriendBtn, newChatBtn;
    @FXML private Label hubLabel;

    // TESTING
    private ArrayList<User> onlineFriendsList = new ArrayList<>();
    private ArrayList<User> offlineFriendsList = new ArrayList<>();
    // ^^TESTING^^

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
        // TESTING
        onlineFriendsList.add(new User("johndoe", "pw1234"));
        onlineFriendsList.add(new User("myuser", "password"));
        onlineFriendsList.add(new User("thisguy", "testpw"));
        onlineFriendsList.add(new User("abcde", "123456"));

        offlineFriendsList.add(new User("offlineguy", "olpassword"));
        offlineFriendsList.add(new User("thisdude", "pwpwpw"));
        offlineFriendsList.add(new User("testuser", "petsname"));
        offlineFriendsList.add(new User("testname", "testpass"));
        // ^^TESTING^^

        // Populates GUI ListView with usernames of all Users in ArrayList<User> onlineFriendsList
        for (User onlineFriend : onlineFriendsList) {
            onlineFriendsListView.getItems().add(onlineFriend.userName);
        }
        // Populates GUI ListView with usernames of all Users in ArrayList<User> offlineFriendsList
        for (User offlineFriend : offlineFriendsList) {
            offlineFriendsListView.getItems().add(offlineFriend.userName);
        }

        // Allows user to select multiple Users to begin group chat with or send message to
        onlineFriendsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Creates "Right Click" menu options for online friends in GUI ListView
        MenuItem mi1 = new MenuItem("Start Chat");
        mi1.setOnAction(e -> startChat());
        MenuItem mil2 = new MenuItem("Send File");
        mil2.setOnAction(e -> sendFile());
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(mi1);
        onlineFriendsListView.setContextMenu(menu);

    }

    // Called on Right Click > Start Chat of Online Friend User
    private void startChat(){
        ObservableList<String> onFriends;
        // Gets list of all Online Friends selected
        onFriends = onlineFriendsListView.getSelectionModel().getSelectedItems();

        for(String m : onFriends){
            System.out.println("Selected friend: " + m);
        }
    }
    // Called on Right Click > Send File of Online Friend User
    private void sendFile(){
        ObservableList<String> onFriends;
        // Gets list of all Online Friends selected
        onFriends = onlineFriendsListView.getSelectionModel().getSelectedItems();

        for(String m : onFriends){
            System.out.println("Start chat: " + m);
        }
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
