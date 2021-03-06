import java.awt.TextField;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class HomeController implements Initializable {
	@FXML
	private String username, password;
	@FXML
	private MenuButton unLabel;
	@FXML
	private MenuItem logoutBtn;
	@FXML
	private ListView chatsListView;
	@FXML
	private Button searchFriendBtn, newChatBtn, sendPostBtn;

	@FXML
	private TextArea mainTextArea, mainTextField;

	@FXML
	private Label hubLabel;
	@FXML
	private TextField searchTextField;
	@FXML
	private ListView<String> onlineFriendsListView;
	@FXML
	private ListView<String> offlineFriendsListView;

	private int port;
	private String textarea;

	private Socket newSock;
	private boolean loggedOn = true;
	private DataOutputStream dos;
	private DataInputStream dis;
	private ArrayList<Friend> onlineFriends = new ArrayList<Friend>();
	private ArrayList<Friend> offlineFriends = new ArrayList<>();
	private ArrayList<Status> newsFeed = new ArrayList<Status>();

	// Data received from ConnectController
	public void initData(Socket s, DataInputStream dis, DataOutputStream dos, int port, String username,
			String password) {
		this.username = username;
		this.password = password;
		// Set up input and output stream to send and receive messages.
		InetAddress ip;
		// Connection to server.
		// try {
		// ip = InetAddress.getByName("localhost");
		// newSock = new Socket(ip, 3159);
		this.dis = dis;
		this.dos = dos;
		// this.dis = new DataInputStream(newSock.getInputStream());
		// this.dos = new DataOutputStream(newSock.getOutputStream());
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		startThread();
		// update();
		// Set connection session attributes banner
		sessionAttributes();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		for (Friend onlineFriend : onlineFriends) {
			onlineFriendsListView.getItems().add(onlineFriend.userName);
		}
		// Populates GUI ListView with usernames of all Users in ArrayList<User>
		// offlineFriendsList
		for (Friend offlineFriend : offlineFriends) {
			offlineFriendsListView.getItems().add(offlineFriend.userName);
		}

		// Allows user to select multiple Users to begin group chat with or send message
		// to
		onlineFriendsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// Creates "Right Click" menu options for online friends in GUI ListView
		MenuItem mi1 = new MenuItem("Start Chat");
		mi1.setOnAction(e -> startChat());
		MenuItem mil2 = new MenuItem("Send File");
		mil2.setOnAction(e -> sendFile());
		ContextMenu menu = new ContextMenu();
		menu.getItems().add(mi1);
		onlineFriendsListView.setContextMenu(menu);
		mainTextField.setWrapText(true);
		sendPostBtn.setOnAction(event -> sendPostBtnClicked());

	}

	private void sendPostBtnClicked() {
		String textarea = mainTextArea.getText();
		String textfield = mainTextField.getText();
		if (textfield != null) {
			if (textfield.startsWith("ADD:")) {
				int count = textfield.indexOf(":");
				String friend = textfield.substring(count + 1);
				addFriend(friend);
			} else if (textfield.startsWith("REFRESH")) {
				refresh();
			} else {
				mainTextArea.setText(textarea + "\n\n" + username + ": " + textfield);
				post(textfield);
			}
		}
	}

	private void startChat() {
		ObservableList<String> onFriends;
		// Gets list of all Online Friends selected
		onFriends = onlineFriendsListView.getSelectionModel().getSelectedItems();

		for (String m : onFriends) {
			group(m + username + " " + m);
		}
	}

	private void sessionAttributes() {
		unLabel.setText(username);
	}

	public void searchFriendBtnPushed() {
		System.out.println("Search Friend Button Pressed." + searchTextField.getText());
		// addFriend(searchTextField.getText());
	}

	public void newChatBtnPushed() {
		System.out.println("New Chat Button Pressed.");
	}

	public void logoutBtnPushed() {
		Stage stage = (Stage) hubLabel.getScene().getWindow();
		loggedOn = false;
		quit();
		System.out.println("Application closed.");
		stage.close();
		System.exit(1);
	}

	public void startThread() {
		Thread localServer = new Thread(new Runnable() {
			public void run() {
				while (loggedOn) {
					try {
						String fromServer = dis.readUTF();
						StringTokenizer tokens = new StringTokenizer(fromServer, ": ");
						String command = tokens.nextToken();
						if (command.equals("QUIT")) {
							loggedOn = false;
							newSock.close();
						} else if (command.equals("ALERT")) {

							// TODO show alert
							String alert = tokens.nextToken();
							mainTextArea.setText(textarea + "\n\n" + "Notification" + ": " + alert);

						} else if (command.equals("MESSAGE")) {
							String messageString = tokens.nextToken();
							tokens = new StringTokenizer(messageString, "%");

							// TODO display msg.
							String from = tokens.nextToken();
							String message = tokens.nextToken();

						} else if (command.equals("POST")) {

							String messageString = tokens.nextToken();
							tokens = new StringTokenizer(messageString, "%");

							String poster = tokens.nextToken();
							String message = tokens.nextToken();
							String time = tokens.nextToken();
							String id = tokens.nextToken();

							Status newStatus = new Status(poster, message, time, "0", "0", id);
							mainTextArea.setText(textarea + "\n\n" + poster + ": " + message + " sent at" + time);

							// TODO display newsFeed.
							newsFeed.add(newStatus);

						} else if (command.equals("BATTLE")) {

							// TODO ??????
							String challenger = tokens.nextToken();

						} else if (command.equals("UPDATE")) {

							String postInfo = " ";
							newsFeed.clear();

							do {
								try {

									postInfo = dis.readUTF();
									if (postInfo.equals("END"))
										break;
									StringTokenizer tokens2 = new StringTokenizer(postInfo, "%");
									String user = tokens2.nextToken();
									String msg = tokens2.nextToken();
									String time = tokens2.nextToken();
									String comments = tokens2.nextToken();
									String likes = tokens2.nextToken();
									String ID = tokens2.nextToken();
									Status newStatus = new Status(user, msg, time, comments, likes, ID);
									mainTextArea.setText(textarea + "\n\n" + user + ": " + msg + " sent at" + time);

									newsFeed.add(newStatus);

								} catch (IOException e) {
									e.printStackTrace();
								}
							} while (true);

							Collections.sort(newsFeed);
							System.out.println("Updated News Feed.");

						} else if (command.equals("ONLINE")) {
							String friendInfo = "";
							onlineFriends.clear();
							do {
								try {

									friendInfo = dis.readUTF();
									if (friendInfo.equals("END"))
										break;
									StringTokenizer tokens2 = new StringTokenizer(friendInfo);
									String user = tokens2.nextToken();
									String port = tokens2.nextToken();
									Friend bud = new Friend(user, port);
									onlineFriends.add(bud);

								} catch (IOException e) {
									e.printStackTrace();
								}
							} while (true);

							Collections.sort(onlineFriends);
							for (Friend onlineFriend : onlineFriends) {
								onlineFriendsListView.getItems().add(onlineFriend.userName);
							}
							System.out.println("Updated online friends");
						} else if (command.equals("OFFLINE")) {
							String friendInfo = "";
							offlineFriends.clear();
							do {
								try {

									friendInfo = dis.readUTF();
									if (friendInfo.equals("END"))
										break;
									StringTokenizer tokens2 = new StringTokenizer(friendInfo);
									String user = tokens2.nextToken();
									String port = tokens2.nextToken();
									Friend bud = new Friend(user, port);
									offlineFriends.add(bud);

								} catch (IOException e) {
									e.printStackTrace();
								}
							} while (true);

							Collections.sort(offlineFriends);
							for (Friend offlineFriend : offlineFriends) {
								offlineFriendsListView.getItems().add(offlineFriend.userName);
							}
							System.out.println("Updated offline friends");
						}

					} catch (EOFException e) {
						break;
					} catch (IOException ex1) {
						ex1.printStackTrace();
					} finally {

					}
				}
			}
		});
		// Handles other clients when they need to get a file from this localServer.
		Thread recieveFiles = new Thread(new Runnable() {
			@Override
			public void run() {

				// Will hold all messages received from server.
				String chatText = "";
				ServerSocket sock = null;
				try {
					sock = new ServerSocket(port);
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// If the client is loggedOff they wont receive any messages.
				while (loggedOn) {

					// If the socket is still open.
					// Read the message sent to this client.
					try {
						Socket s = sock.accept();

						try {

							DataInputStream datainput = new DataInputStream(s.getInputStream());
							FileOutputStream fos = new FileOutputStream("file");
							byte[] buffer = new byte[4096];

							int filesize = 15123; // Send file size in separate msg
							int read = 0;
							int totalRead = 0;
							int remaining = filesize;
							while ((read = datainput.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
								totalRead += read;
								remaining -= read;
								System.out.println("read " + totalRead + " bytes.");
								fos.write(buffer, 0, read);
							}

							fos.close();
							datainput.close();
							s.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		});

		// Start up the local Server.
		localServer.start();
		recieveFiles.start();
	}

	// QUIT = Quit.
	// POST = Posting to main feed.
	// SEND = Forwards a message.
	// BATTLE = Invites a friend to a game.
	// ADD = Adds a friend.
	// REMOVE = Remove a friend.
	// LIKE = Like a statusUpdate.
	// COMMENT = Comment on a statusUpdate.
	// GETL = Get users who liked the post.
	// GETC = Get comments on a post.
	// GETF = Get Friend List.
	// GROUP = Start a new group chat with listed users.
	// LEAVE = Leave group chat.
	// REFRESH = Refresh news feed and Online Friends.
	// DELETE = Delete a statusUpdate.

	/****
	 * 
	 * Sends a message to the server closing the connection.
	 * 
	 ****/
	public void quit() {

		try {
			dos.writeUTF("QUIT");
			loggedOn = false;
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Ends the thread.
		loggedOn = false;
	}

	public void post(String msg) {
		try {
			dos.writeUTF("POST%" + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFile() {

		// TODO Assign
		String filename = " ";
		if (new File(filename).exists()) {
			try {
				InetAddress ip = InetAddress.getByName("localhost");
				Socket sock = new Socket(ip, port);
				DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
				FileInputStream fis = new FileInputStream(filename);
				byte[] buffer = new byte[4096];

				while (fis.read(buffer) > 0) {
					dos.write(buffer);
				}

				fis.close();
				dos.close();
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void send() {

		// TODO Assign these

		// Msg Group
		String to = " ";

		// Msg to send
		String msg = " ";

		try {
			dos.writeUTF("SEND%" + to + "%" + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void battle() {

		String opp = " ";
		try {
			dos.writeUTF("BATTLE%" + opp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/****
	 * 
	 * Allows the client to search the Central Server for available files to
	 * download.
	 * 
	 ****/
	public void addFriend(String userName) {
		try {
			dos.writeUTF("ADD%" + userName);
			System.out.println("Adding " + userName);
			dos.writeUTF("REFRESH");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void remove() {

		// TODO Assign
		String userName = " ";
		try {
			dos.writeUTF("REMOVE%" + userName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void like() {

		// TODO Assign
		String postID = " ";
		try {
			dos.writeUTF("LIKE%" + postID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void comment() {

		// TODO Assign
		String postID = " ";
		String comment = " ";
		try {
			dos.writeUTF("COMMENT%" + postID + "%" + comment);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void group(String list) {

		try {
			dos.writeUTF("GROUP%" + list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void leave() {

		// TODO Assign
		String groupChatName = " ";
		try {
			dos.writeUTF("LEAVE%" + groupChatName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/****
	 * 
	 * Returns the List of Available Files.
	 * 
	 ****/
	public void refresh() {
		try {
			dos.writeUTF("REFRESH");
			// update();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * private void update() { String postInfo = ""; String friendInfo = "";
	 * newsFeed.clear(); onlineFriends.clear();
	 * 
	 * do { try {
	 * 
	 * postInfo = dis.readUTF();
	 * 
	 * StringTokenizer tokens = new StringTokenizer(postInfo, "%"); String user =
	 * tokens.nextToken(); String msg = tokens.nextToken(); String time =
	 * tokens.nextToken(); String comments = tokens.nextToken(); String likes =
	 * tokens.nextToken(); String ID = tokens.nextToken(); Status newStatus = new
	 * Status(user, msg, time, comments, likes, ID);
	 * 
	 * newsFeed.add(newStatus);
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } } while
	 * (!postInfo.equals("END"));
	 * 
	 * do { try {
	 * 
	 * friendInfo = dis.readUTF(); StringTokenizer tokens = new
	 * StringTokenizer(friendInfo); String user = tokens.nextToken(); String port =
	 * tokens.nextToken(); Friend bud = new Friend(user, port);
	 * onlineFriends.add(bud);
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } } while
	 * (!friendInfo.equals("END"));
	 * 
	 * Collections.sort(newsFeed); Collections.sort(onlineFriends);
	 * 
	 * }
	 * 
	 */
	public void delete() {

	}
}

class Status implements Comparable<Status> {

	private String name;
	private String msg;
	private String time;
	private String ID;
	public String numComments;
	public String numLikes;
	public int hour;
	public int min;
	public double sec;

	public Status(String name, String msg, String time, String numComments, String numLikes, String id) {
		this.name = name;
		this.msg = msg;
		this.time = time;
		this.numComments = numComments;
		this.numLikes = numLikes;
		this.ID = id;
		formatTime(this.time);
	}

	private void formatTime(String time) {
		StringTokenizer tokens = new StringTokenizer(time, "-");

		// Used to sort.
		String sHour = tokens.nextToken();
		String sMin = tokens.nextToken();
		String sSec = tokens.nextToken();

		hour = Integer.parseInt(sHour);
		min = Integer.parseInt(sMin);
		sec = Double.parseDouble(sSec);

	}

	@Override
	public int compareTo(Status o) {
		int num = 1;
		if (this.hour < o.hour) {
			num = -1;
		} else if (this.hour == o.hour) {
			if (this.min < o.min) {
				num = -1;
			} else if (this.min == o.min) {
				if (this.sec < o.sec) {
					num = -1;
				} else
					num = 0;
			}
		}
		return num;
	}

}

class Friend implements Comparable<Friend> {
	String userName;
	String port;

	public Friend(String userName, String port) {
		this.userName = userName;
		this.port = port;
	}

	@Override
	public int compareTo(Friend f) {
		return this.userName.compareTo(f.userName);
	}
}
