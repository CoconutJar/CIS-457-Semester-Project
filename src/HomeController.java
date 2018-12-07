import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class HomeController implements Initializable {
    @FXML private String username, password;
    @FXML private User user;

    @FXML private MenuButton unLabel;
    @FXML private MenuItem logoutBtn;
    @FXML private ListView<String> onlineFriendsListView;
    @FXML private ListView<String> offlineFriendsListView;
    @FXML private ListView<String> chatListView;
    @FXML private Button searchFriendBtn, newChatBtn, assignGrpNameBtn, sendPostBtn;
    @FXML private Label hubLabel;
    @FXML private TextField groupNameTextField;
    @FXML private TextArea mainTextArea, mainTextField;
    @FXML private AnchorPane assignGrpNamePane, friendPane, chatPane;

    // TESTING
    private ArrayList<User> onlineFriendsList = new ArrayList<>();
    private ArrayList<User> offlineFriendsList = new ArrayList<>();
    private ArrayList<Group> chatList = new ArrayList<>();
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

        chatList.add(new Group("group1", onlineFriendsList));
        chatList.add(new Group("group2", offlineFriendsList));
        // ^^TESTING^^

        // Populates GUI ListView with usernames of all Users in ArrayList<User> onlineFriendsList
        for (User onlineFriend : onlineFriendsList) {
            onlineFriendsListView.getItems().add(onlineFriend.userName);
        }
        // Populates GUI ListView with usernames of all Users in ArrayList<User> offlineFriendsList
        for (User offlineFriend : offlineFriendsList) {
            offlineFriendsListView.getItems().add(offlineFriend.userName);
        }
        for (Group group : chatList) {
            chatListView.getItems().add(group.name);
        }

        // Allows user to select multiple Users to begin group chat with or send message to
        onlineFriendsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        chatListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Creates "Right Click" menu options for online friends in GUI ListView
        MenuItem mi1 = new MenuItem("Start Chat");
        mi1.setOnAction(e -> startChat());
        MenuItem mil2 = new MenuItem("Send File");
        mil2.setOnAction(e -> sendFile());
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(mi1);
        menu.getItems().add(mil2);
        onlineFriendsListView.setContextMenu(menu);
        // "Right Click" to view chat
        MenuItem mil3 = new MenuItem("View");
        mil3.setOnAction(e -> viewChat());
        ContextMenu menuChat = new ContextMenu();
        menuChat.getItems().add(mil3);
        chatListView.setContextMenu(menuChat);

        mainTextField.setWrapText(true);
        sendPostBtn.setOnAction(event -> sendPostBtnClicked());
    }

    private void sendPostBtnClicked() {
        String textarea = mainTextArea.getText();
        String textfield = mainTextField.getText();

        if(textfield != null) {
            mainTextArea.setText(textarea + "\n\n" + username +": " + textfield);
        }
        mainTextField.setText("");
    }

    // Called on Right Click > View of Chat List Group name
    private void viewChat() {
        ObservableList<String> chats;
        chats = chatListView.getSelectionModel().getSelectedItems();

        // For all groupname that was selected..
        for(String n : chats){
            // For all the Groups in the chatList ArrayList..
            for(Group group : chatList){
                // If the groupname selected equals a Group's name, print that Group's members.
                if(n.equals(group.name)){
                    // Calls Group class for printMembers()
                    group.printMembers();
                }
            }
        }

    }

    // Called on Right Click > Start Chat of Online Friend User
    private void startChat(){
        ObservableList<String> onFriends;
        // Gets list of all Online Friends selected
        onFriends = onlineFriendsListView.getSelectionModel().getSelectedItems();

        AtomicReference<String> groupName = new AtomicReference<>("");
        ArrayList<User> members = new ArrayList<>();

        // Able/Disable Panes
        friendPane.setDisable(true);
        chatPane.setDisable(true);
        assignGrpNamePane.setVisible(true);

        // On 'Assign' button click
        assignGrpNameBtn.setOnAction(event -> {
            Group group;

            groupName.set(groupNameTextField.getText());
            assignGrpNamePane.setVisible(false);
            friendPane.setDisable(false);
            chatPane.setDisable(false);

            // Adds Users to ArrayList based on username string search
            for(String m : onFriends){
//                System.out.println("Start chat: " + m);
                for(User user : onlineFriendsList){
                    if(m.equals(user.userName)){
                        members.add(user);
                    }
                }
            }
            group = new Group(groupName.toString(), members);

            addGroupToChatList(group);
        });


    }

    // Adds new group to Chat ListView in GUI
    // Adds new Group object to ArrayList<Group> chatList
    private void addGroupToChatList(Group group) {
        chatListView.getItems().add(group.name);
        chatList.add(group);
        chatListView.refresh();
    }

    // Called on Right Click > Send File of Online Friend User
    private void sendFile(){
        ObservableList<String> onFriends;
        // Gets list of all Online Friends selected
        onFriends = onlineFriendsListView.getSelectionModel().getSelectedItems();

        for(String m : onFriends){
            System.out.println("Send file: " + m);
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
