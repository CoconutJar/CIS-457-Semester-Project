import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CentralServer {

	// Socket that awaits client connections.
	private static ServerSocket welcomeSocket;
	// Holds all client UserNames that have connected to the server.
	public static ArrayList<User> users = new ArrayList<User>();

	public static ArrayList<User> onlineUsers = new ArrayList<User>();

	public static ArrayList<Post> userPosts = new ArrayList<Post>();

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
						dos.writeUTF("200: Sign in Sucessfull");
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
				StringTokenizer tokens = new StringTokenizer(fromClient);

				String command = tokens.nextToken();

				// TODO Make sure commands are broken down correctly.

				// POST = Posting to main feed.
				// SEND = Forwards a message.
				// BATTLE = Invites a friend to a game.
				// ADD = Adds a friend.
				// REMOVE = Remove a friend.
				// LIKE = Like a statusUpdate.
				// COMMENT = Comment on a statusUpdate
				// GETF = Get Friend List.
				// REFRESH = Refresh news feed and Online Friends.
				// DELETE = Delete a statusUpdate.
				// ?
				if (fromClient.equals("QUIT")) {

					hasNotQuit = false;

				} else if (command.equals("POST")) {

					String msg = tokens.nextToken("%");
					String time = tokens.nextToken("%");

					postStatus(msg, time);

				} else if (command.equals("SEND")) {

					String msg = tokens.nextToken("%");

					for (int i = 0; i < user.friends.size(); i++) {
						user.friends.get(i).sendMsg(msg);
					}

					// TODO No idea what we are doing with this.
				} else if (command.equals("BATTLE")) {

					String opponent = tokens.nextToken("%");

					for (int i = 0; i < user.friends.size(); i++) {
						if (user.friends.get(i).userName.equals(opponent)) {
							user.friends.get(i).sendBattleRequest(this.clientName);
						}
					}

					// TODO Right now only adds on one user (like Twitter).
				} else if (command.equals("ADD")) {

					String person = tokens.nextToken("%");
					addFriend(person);

					// TODO Right now only removes on one user.
				} else if (command.equals("REMOVE")) {

					String person = tokens.nextToken("%");
					removeFriend(person);

				} else if (command.equals("LIKE")) {

					// TODO find a way to identify a post maybe id?
					// likeStatus(post);

				} else if (command.equals("COMMENT")) {

					// TODO find a way to identify a post maybe id?
					// commentStatus(post);

				} else if (command.equals("GETF")) {

					sendOnlineFriends();

				} else if (command.equals("REFRESH")) {

					updateClientNewsFeed();
					sendOnlineFriends();

				} else if (command.equals("DELETE")) {

					// TODO find a way to identify a post maybe id?
					// deleteStatus(post);

				}

			} while (hasNotQuit);

			// Set the online status to offline.
			this.loggedIn = false;

			// Close the Socket.
			this.connectionSocket.close();
			System.out.println(clientName + " has disconnected!");

		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}
	}

	private synchronized void createProfile() {

		User u = new User(this.clientName, this.password, dis, dos);
		CentralServer.users.add(u);

		try {
			dos.writeUTF("201: Profile sucessfully created");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Deletes a User Profile.
	 * 
	 */
	private synchronized void deleteProfile() {
		for (int i = 0; i < CentralServer.users.size(); i++) {
			if (CentralServer.users.get(i).userName == this.clientName) {
				CentralServer.users.remove(i);
			}
		}
	}

	/**
	 * 
	 * Tries to sign into a User Profile.
	 * 
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

	private void updateClientNewsFeed() {

		for (User friend : user.friends) {

			for (Post post : friend.posts) {

				try {
					dos.writeUTF(post.userName + post.msg + post.time + post.comments.size() + post.likes.size());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		try {
			dos.writeUTF("END");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendOnlineFriends() {

		for (User friend : user.friends) {
			for (User online : CentralServer.onlineUsers) {
				if (friend.userName.equals(online.userName)) {
					try {
						dos.writeUTF(friend.userName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		try {
			dos.writeUTF("END");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void postStatus(String msg, String time) {

		Post post = new Post(user.userName, msg, time);

		user.posts.add(post);
		CentralServer.userPosts.add(post);

		for (int i = 0; i < user.friends.size(); i++) {
			user.friends.get(i).updateFeed(post);
		}
	}

	private synchronized void likePost(Post post) {

		post.likes.add(user);
		sendNotification(post.userName, user.userName + " has liked your post!");

	}

	private synchronized void commentPost(Post post, String comment) {

		post.addComment(user, comment);
		sendNotification(post.userName, user.userName + " has commented on your post!");

	}

	private synchronized void deleteStatus(Post post) {

		user.posts.remove(post);
		CentralServer.userPosts.remove(post);

	}

	private synchronized void addFriend(String person) {

		for (int i = 0; i < CentralServer.users.size(); i++) {

			if (CentralServer.users.get(i).userName.equals(person)) {
				// Adds person to user friends list.
				user.friends.add(CentralServer.users.get(i));
				sendNotification(person, user.userName + " is now following you!");
			}
		}
	}

	private synchronized void removeFriend(String person) {
		for (int i = 0; i < user.friends.size(); i++) {

			if (user.friends.get(i).userName.equals(person)) {
				// removes a friend.
				user.friends.remove(i);
				break;
			}
		}
	}

	private void sendNotification(String user, String alert) {
		for (int i = 0; i < CentralServer.users.size(); i++) {

			if (CentralServer.users.get(i).userName.equals(user)) {

				try {
					CentralServer.users.get(i).dos.writeUTF("ALERT " + alert);
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
	private int port;

	/****
	 * 
	 * Holds all the information of the User.
	 * 
	 ****/
	public User(String userName, String password, DataInputStream dis, DataOutputStream dos) {
		this.userName = userName;
		this.password = password;
		this.dis = dis;
		this.dos = dos;
	}

	/**
	 * Sends a post for the Main Feed.
	 * 
	 * @param post
	 */
	public void updateFeed(Post post) {

		try {
			dos.writeUTF("POST" + post.userName + post.msg + post.time);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Forwards a message.
	 * 
	 * @param msg
	 */
	public void sendMsg(String msg) {
		try {
			dos.writeUTF("SENT" + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a game invite.
	 * 
	 * @param challenger
	 */
	public void sendBattleRequest(String challenger) {
		try {
			dos.writeUTF("BATTLE" + challenger);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class Post {

	public ArrayList<Comment> comments = new ArrayList<Comment>();
	public ArrayList<User> likes = new ArrayList<User>();

	String userName;
	String msg;
	String time;

	public Post(String userName, String msg, String time) {
		this.userName = userName;
		this.msg = msg;
		this.time = time;
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

}

class Comment {

	public User user;
	public String comment;

	public Comment(User user, String comment) {
		this.user = user;
		this.comment = comment;
	}

}
