import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CentralServer {

	// Socket that awaits client connections.
	private static ServerSocket welcomeSocket;
	// Holds all client UserNames that have connected to the server.
	public static ArrayList<User> users = new ArrayList<User>();


	public static void main(String[] args) throws IOException {
		// TESTING
//		User u = new User("pikachu", "password123");
//		User u1 = new User("newguy", "newguypass");
//		String username = "pikachu";
		// ^^TESTING^^

		try {
			welcomeSocket = new ServerSocket(3158); // ServerPort
			System.out.println("Server UP!");

			// TESTING
//			fillUserList(users);
//			printUserList(users);
//			addUser(u);
//			addUser(u1);
//			fillUserList(users);
//			printUserList(users);
//			deleteUser(username);
//			fillUserList(users);
//			printUserList(users);
			// ^^TESTING^^

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

	// FOR TESTING: Needs to be moved to ClientHandler() once Client-side is connected
	// Delete User from Database (userTable.txt)
	private static void deleteUser(String username) throws IOException {
		for (int i = 0; i < CentralServer.users.size(); i++) {
			if (CentralServer.users.get(i).userName.equals(username)) {
				CentralServer.users.remove(i);
			}
		}

		File file = new File("./src/db/userTable.txt");
		FileWriter fw = null;
		BufferedWriter bw = null;

		try {
			fw = new FileWriter(file.getAbsoluteFile(),false);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		assert bw != null;
		try {
			for (int i = 0; i < CentralServer.users.size(); i++) {
				User u = CentralServer.users.get(i);
				String data = u.getUserName() + " " + u.getPassword();

				bw.write(data);
				if(i < CentralServer.users.size() - 1){
					bw.newLine();
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}finally {
			bw.close();
		}

		System.out.println("User Removed from Database.");
	}
	// FOR TESTING: Needs to be moved to ClientHandler() once Client-side is connected
	// Add User to Database (userTable.txt)
	private static void addUser(User u) {
		try {
			String data = u.getUserName() + " " + u.getPassword();
			System.out.println("New User: " + data);
			File file = new File("./src/db/userTable.txt");

			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.newLine();
			bw.write(data);
			bw.close();
			System.out.println("User Added to Database.");
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	// FOR TESTING: Needs to be moved to ClientHandler() once Client-side is connected
	// Fills ArrayList of all known Users in database
	private static void fillUserList(ArrayList<User> users) {
		File file = new File("./src/db/userTable.txt");
		users.clear();

		try(Scanner scanner = new Scanner(file)){
			while(scanner.hasNextLine()){
				User user = new User(scanner.next(), scanner.next());
				users.add(user);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// FOR TESTING: Needs to be moved to ClientHandler() once Client-side is connected
	// Prints all Users of an ArrayList<User>
	private static void printUserList(ArrayList<User> users){
		for (int i = 0; i < CentralServer.users.size(); i++) {
			System.out.println("ID: " + i + " " + CentralServer.users.get(i).toString());
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

		fillUserList(CentralServer.users);
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
						// Set datastreams based on successful sign-in
						setDataStreams(clientName);
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

						User u = new User(this.clientName, this.password, dis, dos);
						CentralServer.users.add(u);
						addUserToDB(u);

						dos.writeUTF("201: Profile sucessfully created");
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


			// Main Loop
			do {

				// Waits for a command from the client.
				fromClient = dis.readUTF();
				StringTokenizer tokens = new StringTokenizer(fromClient);

				// TODO Make sure commands are broken down correctly.

				// POST = Posting to main feed.
				// SEND = Forwards a message.
				// BATTLE = Invites a friend to a game.
				// ADD = Adds a friend.
				// REMOVE = Remove a friend.
				// GETF = Get Friend List.
				// ?
				if (fromClient.equals("QUIT")) {
					hasNotQuit = false;
				} else if (fromClient.startsWith("POST")) {

					String post = tokens.nextToken();
					post = tokens.nextToken("%");

					for (int i = 0; i < user.friends.size(); i++) {
						user.friends.get(i).updateFeed(post);
					}

				} else if (fromClient.startsWith("SEND")) {

					String msg = tokens.nextToken();
					msg = tokens.nextToken("%");

					for (int i = 0; i < user.friends.size(); i++) {
						user.friends.get(i).sendMsg(msg);
					}

				} else if (fromClient.startsWith("BATTLE")) {

					String opponent = tokens.nextToken();
					opponent = tokens.nextToken("%");

					for (int i = 0; i < user.friends.size(); i++) {
						if (user.friends.get(i).userName.equals(opponent)) {
							user.friends.get(i).sendBattleRequest(this.clientName);
						}
					}

					// TODO Right now only adds on one user. Also maybe should wait till other
					// confirms.
				} else if (fromClient.startsWith("ADD")) {
					String person = tokens.nextToken();
					person = tokens.nextToken("%");

					for (int i = 0; i < CentralServer.users.size(); i++) {

						if (CentralServer.users.get(i).userName.equals(person)) {
							// Adds person to user friends list.
							user.friends.add(CentralServer.users.get(i));
						}

					}

					// TODO Right now only removes on one user.
				} else if (fromClient.startsWith("REMOVE")) {

					String person = tokens.nextToken();
					person = tokens.nextToken("%");

					for (int i = 0; i < user.friends.size(); i++) {

						if (user.friends.get(i).userName.equals(person)) {
							// removes a friend.
							user.friends.remove(i);
							break;
						}

					}

					// TODO Return UpdatedFriendList.
				} else if (fromClient.startsWith("GETF")) {

				} else {
					// ?
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

	private void fillUserList(ArrayList<User> users) {
		File file = new File("./src/db/userTable.txt");
		users.clear();

		try(Scanner scanner = new Scanner(file)){
			while(scanner.hasNextLine()){
				User user = new User(scanner.next(), scanner.next());
				users.add(user);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Assign datastreams to user based on new successful sign-in session
	private void setDataStreams(String clientName) {
		for (int i = 0; i < CentralServer.users.size(); i++) {

			if (CentralServer.users.get(i).userName.equals(clientName))
				// Assigns the user variable on sign-in.
				user = CentralServer.users.get(i);
				user.setDis(this.dis);
				user.setDos(this.dos);
		}
	}

	// Append new User to userTable.txt
	private void addUserToDB(User u) {
		try {
			String data = u.getUserName() + " " + u.getPassword();
			System.out.println("New User: " + data);
			File file = new File("./src/db/userTable.txt");

			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.newLine();
			bw.write(data);
			bw.close();
			System.out.println("User Added to Database.");
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Deletes a User Profile.
	 * 
	 */
//	private void deleteProfile(String username) {
//		for (int i = 0; i < CentralServer.users.size(); i++) {
//			if (CentralServer.users.get(i).userName == username) {
//				CentralServer.users.remove(i);
//			}
//		}
//	}

	private static void deleteUser(String username) throws IOException {
		for (int i = 0; i < CentralServer.users.size(); i++) {
			if (CentralServer.users.get(i).userName.equals(username)) {
				CentralServer.users.remove(i);
			}
		}

		File file = new File("./src/db/userTable.txt");
		FileWriter fw = null;
		BufferedWriter bw = null;

		try {
			fw = new FileWriter(file.getAbsoluteFile(),false);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}

		assert bw != null;
		try {
			for (int i = 0; i < CentralServer.users.size(); i++) {
				User u = CentralServer.users.get(i);
				String data = u.getUserName() + " " + u.getPassword();

				bw.write(data);
				if(i < CentralServer.users.size() - 1){
					bw.newLine();
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}finally {
			bw.close();
		}

		System.out.println("User Removed from Database.");
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

}

/*******************************************************************************************
 * 
 * Handles the clients files that are available for download.
 * 
 ******************************************************************************************/
class User {
	public String realName;
	public String userName;
	public String password;

	DataInputStream dis;
	DataOutputStream dos;

	public ArrayList<User> friends = new ArrayList<User>();

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

	public User(String userName, String password){
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setDis(DataInputStream dis) {
		this.dis = dis;
	}

	public void setDos(DataOutputStream dos) {
		this.dos = dos;
	}

	public String toString(){
		return ("Username: " + this.getUserName() +
				"\tPassword: " + this.getPassword());
	}

	/**
	 * Sends a post for the Main Feed.
	 * 
	 * @param post
	 */
	public void updateFeed(String post) {

		try {
			dos.writeUTF("POST" + post);
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
