import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * 
 * 
 * @author
 *
 */
public class CentralServer {

	// Socket that awaits client connections.
	private static ServerSocket welcomeSocket;
	// Holds all client UserNames that have connected to the server.
	public static ArrayList<User> users = new ArrayList<User>();

	public static ArrayList<User> onlineUsers = new ArrayList<User>();

	public static ArrayList<Post> userPosts = new ArrayList<Post>();

	public static ArrayList<Group> groups = new ArrayList<Group>();

	public static int totalPosts = 0;

	public static int userNum = 0;

	public static void main(String[] args) throws IOException {
		try {
			welcomeSocket = new ServerSocket(3158); // ServerPort
			System.out.println("Server UP!");
		} catch (Exception e) {
			System.err.println("ERROR: Server could not be started.");
		}
		try {
			while (true) {
				// Waits for a client to connect.
				Socket connectionSocket = welcomeSocket.accept();
				// Set up input and output stream with the client to send and receive messages.
				DataInputStream dis = new DataInputStream(connectionSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(connectionSocket.getOutputStream());
				// Creates a clientHandler object with the client.
				ClientHandler client = new ClientHandler(connectionSocket, dis, dos);
				// Makes a thread to allow the client and clientHandler to interact.
				Thread t = new Thread(client);
				t.start();
			}
		} catch (Exception e) {
			System.err.println("ERROR: Connecting Client");
			e.printStackTrace();
		} finally {
			try {
				// Close the Socket in the event of an error.
				welcomeSocket.close();
				System.out.println("Server socket closed.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

/*******************************************************************************************
 * 
 * Handles the client.
 * 
 ******************************************************************************************/
class ClientHandler implements Runnable {
	Socket connectionSocket;

	DataInputStream dis;
	DataOutputStream dos;

	String fromClient;
	String clientName;
	String password;

	User user;

	int port;

	boolean loggedIn;

	/****
	 * 
	 * Sets up the ClientHandler object/
	 * 
	 ****/
	public ClientHandler(Socket connectionSocket, DataInputStream dis, DataOutputStream dos) {
		this.connectionSocket = connectionSocket;
		this.dis = dis;
		this.dos = dos;
		this.loggedIn = true;
	}

	/****
	 * 
	 * Allows multiple clients to interact with the server.
	 * 
	 ****/
	@Override
	public void run() {
		String connectionString;
		String status;
		boolean notSignedIn = true;

		while (notSignedIn) {
			try {
				// Client sends a String filled with information about the client.
				connectionString = dis.readUTF();

				// Sets the first string received as the status, UserName, password.
				StringTokenizer tokens = new StringTokenizer(connectionString);
				status = tokens.nextToken();
				clientName = tokens.nextToken();
				password = tokens.nextToken();

				System.out.println(clientName + " is attempting to Sign In!");
				// If the client is a returning user status will be set to Return.
				if (status.equals("Return")) {

					boolean profileExists = attemptSignIn(clientName, password);

					if (profileExists) {
						dos.writeUTF("200: Sign in Sucessfull-" + CentralServer.userNum);
						CentralServer.userNum++;
						notSignedIn = false;
					} else {
						dos.writeUTF("101: Sign in Failed");
					}

				}

				// If the client is a new user status will be set to New.
				else {

					// Checks to see if the requested Username is available.
					boolean takenUserName = false;
					for (int i = 0; i < CentralServer.users.size(); i++) {

						if (CentralServer.users.get(i).userName.equals(clientName)) {
							// userName is already taken.
							takenUserName = true;
							break;
						}

					}

					// Creates a users object with the information if the userName was not taken.
					if (!takenUserName) {

						createProfile();
						notSignedIn = false;

					} else {
						dos.writeUTF("102: User Name is already taken. :( ");
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		// Once client has signed-in.
		try {
			// Do while conditional.
			boolean hasNotQuit = true;

			updateClientNewsFeed();
			sendOnlineFriends();

			// Main Loop
			do {

				// Waits for a command from the client.
				fromClient = dis.readUTF();
				StringTokenizer tokens = new StringTokenizer(fromClient, "%");

				String command = tokens.nextToken();

				// TODO Make sure commands are broken down correctly.

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
				// ?
				if (fromClient.equals("QUIT")) {

					hasNotQuit = false;

				} else if (command.equals("POST")) {

					String msg = tokens.nextToken();

					postStatus(msg);

				} else if (command.equals("SEND")) {

					String to = tokens.nextToken();
					String msg = tokens.nextToken();

					sendMsg(to, msg);

					// TODO No idea what we are doing with this.
				} else if (command.equals("BATTLE")) {

					String opponent = tokens.nextToken();

					for (int i = 0; i < user.friends.size(); i++) {
						if (user.friends.get(i).userName.equals(opponent)) {
							user.friends.get(i).dos.writeUTF("BATTLE:" + clientName);
						}
					}

					// TODO Right now only adds on one user (like Twitter).
				} else if (command.equals("ADD")) {

					String person = tokens.nextToken();
					addFriend(person);

					// TODO Right now only removes on one user (like Twitter).
				} else if (command.equals("REMOVE")) {

					String person = tokens.nextToken();
					removeFriend(person);

				} else if (command.equals("LIKE")) {

					String sID = tokens.nextToken();
					int id = Integer.parseInt(sID);
					for (Post post : CentralServer.userPosts) {
						if (post.ID == id)
							likePost(post);
					}

				} else if (command.equals("COMMENT")) {

					String sID = tokens.nextToken();
					String msg = tokens.nextToken();
					int id = Integer.parseInt(sID);
					for (Post post : CentralServer.userPosts) {
						if (post.ID == id)
							commentPost(post, msg);
					}
					// commentStatus(post);
				} else if (command.equals("GETL")) {

					String sID = tokens.nextToken();
					int id = Integer.parseInt(sID);
					for (Post post : CentralServer.userPosts) {
						if (post.ID == id)
							getLikes(post);
					}

				} else if (command.equals("GETC")) {

					String sID = tokens.nextToken();
					int id = Integer.parseInt(sID);
					for (Post post : CentralServer.userPosts) {
						if (post.ID == id)
							getComments(post);
					}

				} else if (command.equals("GETF")) {

					sendOnlineFriends();

				} else if (command.equals("GROUP")) {

					String userList = tokens.nextToken();
					formGroupChat(userList);

				} else if (command.equals("LEAVE")) {

					String groupName = tokens.nextToken();
					leaveGroup(groupName);

				} else if (fromClient.equals("REFRESH")) {

					updateClientNewsFeed();
					sendOnlineFriends();

				} else if (command.equals("DELETE")) {

					String sID = tokens.nextToken();
					int id = Integer.parseInt(sID);
					for (Post post : CentralServer.userPosts) {
						if (post.ID == id)
							deletePost(post);
					}

				}

			} while (hasNotQuit);

			// Set the online status to offline.
			loggedIn = false;

			// Close the Socket.
			connectionSocket.close();
			System.out.println(clientName + " has disconnected!");

		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}
	}

	// helpers

	/**
	 * Creates a profile.
	 */
	private synchronized void createProfile() {

		User u = new User(clientName, password, dis, dos, CentralServer.userNum);
		CentralServer.users.add(u);

		try {
			dos.writeUTF("201: Profile sucessfully created" + CentralServer.userNum);
			CentralServer.userNum++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a User Profile.
	 */
	private synchronized void deleteProfile() {
		for (int i = 0; i < CentralServer.users.size(); i++) {
			if (CentralServer.users.get(i).userName == this.clientName) {
				CentralServer.users.remove(i);
			}
		}
	}

	/**
	 * Attempts to sign-in the Client
	 * 
	 * @param userName
	 * @param password
	 * @return signedIn - Whether or not the client signedIn
	 */
	private boolean attemptSignIn(String userName, String password) {
		boolean signedIn = false;

		for (int i = 0; i < CentralServer.users.size(); i++) {

			if (CentralServer.users.get(i).userName.equals(userName))

				// Checks to see if the password is correct.
				if (CentralServer.users.get(i).password.equals(password)) {

					// Assigns the user variable on sign-in.
					user = CentralServer.users.get(i);
					System.out.println(user.userName + " has signed in.");
					signedIn = true;
				}
		}

		return signedIn;
	}

	/**
	 * 
	 */
	private void updateClientNewsFeed() {

		for (User friend : user.friends) {

			for (Post post : friend.posts) {

				try {
					dos.writeUTF(post.userName + "%" + post.msg + "%" + post.getTime() + "%" + post.comments.size()
							+ "%" + post.likes.size() + "%" + post.ID);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			dos.writeUTF("END");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 */
	private void sendOnlineFriends() {

		for (User friend : user.friends) {
			if (CentralServer.onlineUsers.contains(friend)) {
				try {
					dos.writeUTF(friend.userName + " " + friend.port);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			dos.writeUTF("END");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param msg
	 */
	private void sendMsg(String groupName, String msg) {

		for (Group group : CentralServer.groups) {
			if (group.name.equals(groupName)) {
				for (User member : group.members) {

					try {
						member.dos.writeUTF("MESSAGE: " + groupName + "%" + msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}

	private void getLikes(Post post) {
		for (User liker : post.likes) {
			try {
				dos.writeUTF(liker.userName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			dos.writeUTF("END");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getComments(Post post) {
		for (Comment com : post.comments) {
			try {
				dos.writeUTF(com.user.userName + "%" + com.comment);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			dos.writeUTF("END");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param msg
	 */
	private synchronized void postStatus(String msg) {
		LocalDateTime time = java.time.LocalDateTime.now();
		int postID = CentralServer.totalPosts++;

		Post post = new Post(user.userName, msg, time.toString(), postID);

		user.posts.add(post);
		CentralServer.userPosts.add(post);

		if (msg.contains("@")) {
			boolean moreUsers = true;
			do {
				String looking = msg.substring(msg.indexOf("@") + 1, msg.length() - 1);
				StringTokenizer tokens = new StringTokenizer(looking);
				String tagged = tokens.nextToken();
				for (User friend : user.friends) {
					if (tagged.equals(friend.userName)) {
						sendNotification(tagged, user.userName + " has tagged you in a post!");
					}
					break;
				}
				moreUsers = looking.contains("@");
			} while (moreUsers);
		}

		for (int i = 0; i < user.friends.size(); i++) {

			try {
				user.friends.get(i).dos
						.writeUTF("POST:" + post.userName + "%" + post.msg + "%" + post.getTime() + "%" + postID);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 
	 * @param post
	 */
	private synchronized void likePost(Post post) {

		post.likes.add(user);
		sendNotification(post.userName, user.userName + " has liked your post!");

	}

	/**
	 * 
	 * @param post
	 * @param comment
	 */
	private synchronized void commentPost(Post post, String comment) {

		post.addComment(user, comment);
		sendNotification(post.userName, user.userName + " has commented on your post!");

	}

	/**
	 * 
	 * @param post
	 */
	private synchronized void deletePost(Post post) {

		user.posts.remove(post);
		CentralServer.userPosts.remove(post);

	}

	/**
	 * 
	 * @param person
	 */
	private synchronized void addFriend(String person) {

		for (int i = 0; i < CentralServer.users.size(); i++) {

			if (CentralServer.users.get(i).userName.equals(person)) {
				// Adds person to user friends list.
				user.friends.add(CentralServer.users.get(i));
				sendNotification(person, user.userName + " is now following you!");
			}
		}
	}

	/**
	 * 
	 * @param person
	 */
	private synchronized void removeFriend(String person) {

		for (int i = 0; i < user.friends.size(); i++) {

			if (user.friends.get(i).userName.equals(person)) {
				// removes a friend.
				user.friends.remove(i);
				break;
			}
		}
	}

	/**
	 * 
	 * @param list
	 */
	private synchronized void formGroupChat(String list) {

		StringTokenizer tokens = new StringTokenizer(list);
		String groupName = tokens.nextToken();
		ArrayList<User> groupMembers = new ArrayList<User>();
		while (tokens.hasMoreTokens()) {
			String member = tokens.nextToken();
			for (User friend : user.friends) {
				if (friend.userName.equals(member)) {
					groupMembers.add(friend);
				}
			}
		}

		Group newGroup = new Group(groupName, groupMembers);
		CentralServer.groups.add(newGroup);
	}

	/**
	 * 
	 * @param groupName
	 */
	private synchronized void leaveGroup(String groupName) {

		for (Group group : CentralServer.groups) {
			if (group.name.equals(groupName)) {
				if (group.members.contains(user))
					group.members.remove(user);
				break;
			}
		}

	}

	/**
	 * 
	 * @param user
	 * @param alert
	 */
	private void sendNotification(String user, String alert) {
		for (int i = 0; i < CentralServer.users.size(); i++) {

			if (CentralServer.users.get(i).userName.equals(user)) {

				try {
					CentralServer.users.get(i).dos.writeUTF("ALERT: " + alert);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

/*******************************************************************************************
 * 
 * Handles the Users data.
 * 
 ******************************************************************************************/
class User {
	public String realName;
	public String userName;
	public String password;

	public DataInputStream dis;
	public DataOutputStream dos;

	public ArrayList<User> friends = new ArrayList<User>();
	public ArrayList<Post> posts = new ArrayList<Post>();

	// For P2P
	public int port;

	/****
	 * 
	 * Holds all the information of the User.
	 * 
	 ****/
	public User(String userName, String password, DataInputStream dis, DataOutputStream dos, int port) {
		this.userName = userName;
		this.password = password;
		this.dis = dis;
		this.dos = dos;
		this.port = port + 3158;
	}
}

class Post {

	public ArrayList<Comment> comments = new ArrayList<Comment>();
	public ArrayList<User> likes = new ArrayList<User>();

	String userName;
	String msg;
	String time;
	int ID;

	String date;
	int hour;
	int min;
	int sec;

	public Post(String userName, String msg, String time, int ID) {
		this.userName = userName;
		this.msg = msg;
		this.time = time;
		this.ID = ID;
		formatTime();
	}

	public void addComment(User user, String comment) {
		Comment c = new Comment(user, comment);
		comments.add(c);
	}

	public void like(User user) {

		if (likes.contains(user)) {
			likes.remove(user);
		} else {
			likes.add(user);
		}

	}

	private void formatTime() {

		StringTokenizer tokens = new StringTokenizer(time, "T");

		// Used to sort.
		date = tokens.nextToken();
		String tempTime = tokens.nextToken();
		tokens = new StringTokenizer(tempTime, ":");
		String sHour = tokens.nextToken();
		String sMin = tokens.nextToken();
		String sSec = tokens.nextToken();

		hour = Integer.parseInt(sHour);
		min = Integer.parseInt(sMin);
		sec = Integer.parseInt(sSec);
	}

	/**
	 * 
	 * @return
	 */
	public String getTime() {
		return hour + "-" + min + "-" + sec;

	}

}

class Comment {

	public User user;
	public String comment;

	public Comment(User user, String comment) {
		this.user = user;
		this.comment = comment;
	}

}

class Group {

	public String name;
	public ArrayList<User> members = new ArrayList<User>();

	public Group(String name, ArrayList<User> members) {
		this.name = name;
		this.members = members;
	}

}
