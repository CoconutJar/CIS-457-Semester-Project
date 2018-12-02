
	import java.io.BufferedReader;
	import java.io.DataInputStream;
	import java.io.DataOutputStream;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.util.ArrayList;
	import java.util.StringTokenizer;

	public class CentralServer {
		
		// Socket that awaits client connections.
		private static ServerSocket welcomeSocket;

		// Holds all client UserNames that have connected to the server.
		public static ArrayList<User> users = new ArrayList<User>();

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

			while(notSignedIn) {
			try {

				// Sets the first string received as the UserName, hostName and speed for the
				// client.
				connectionString = dis.readUTF();

				// Client sends a String filled with information about the client.
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
					}
					else {
						dos.writeUTF("101: Sign in Failed");
					}
					
				}
				
				// If the client is a new user status will be set to New.
				else {
					
					// Checks to see if the requested Username is available.
					boolean takenUserName = false;
					for (int i = 0; i < CentralServer.users.size(); i++) {
						
						if(CentralServer.users.get(i).userName.equals(clientName)) {
							// userName is already taken.
							takenUserName = true;
							break;
						}
					
					}
					
					// Creates a users object with the information if the userName was not taken.
					if(!takenUserName) {
						
						User u = new User(this.clientName, this.password, dis, dos);
						CentralServer.users.add(u);
						
						dos.writeUTF("Profile sucessfully created");
						notSignedIn = false;
						
					}
					else {
						dos.writeUTF("User Name is already taken. :( ");
					}
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			}
			
			
			
			// Main Loop
			try {

				// Do while conditional.
				boolean hasNotQuit = true;

				// Breaks down the messages received by the client into a command.
				do {

					// Waits for data.
					fromClient = dis.readUTF();
					StringTokenizer tokens = new StringTokenizer(fromClient);

					if (fromClient.equals("QUIT")) {

						hasNotQuit = false;

					} 
					else if(fromClient.startsWith("POST")){
						
						String post = tokens.nextToken();
						post = tokens.nextToken("%");
						
						for (int i = 0; i < user.friends.size(); i++) {
							 user.friends.get(i).updateFeed(post);
						}
						
					}
					else if(fromClient.startsWith("SEND")){
						
						String msg = tokens.nextToken();
						msg = tokens.nextToken("%");
						
						for (int i = 0; i < user.friends.size(); i++) {
							 user.friends.get(i).sendMsg(msg);
						}
						
					}
					else if(fromClient.startsWith("BATTLE")){
						
						String opponent = tokens.nextToken();
						opponent = tokens.nextToken("%");
						
						for (int i = 0; i < user.friends.size(); i++) {
							 if(user.friends.get(i).userName.equals(opponent)) {
								 user.friends.get(i).sendBattleRequest(this.clientName);
							 }
						}
						
					}
					else {

						
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
		
		private void deleteProfile() {
			for (int i = 0; i < CentralServer.users.size(); i++) {
				if (CentralServer.users.get(i).userName == this.clientName) {
					CentralServer.users.remove(i);
				}
			}
		}
		
		private boolean attemptSignIn(String userName, String password) {
			boolean signedIn = false;
			
			for (int i = 0; i < CentralServer.users.size(); i++) {
				
				if (CentralServer.users.get(i).userName.equals(userName)) 	
					if (CentralServer.users.get(i).password.equals(password)) {
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
		
		public void updateFeed(String post) {
			
			try {
				dos.writeUTF("POST" + post);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void sendMsg(String msg) {
			try {
				dos.writeUTF("SENT" + msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void sendBattleRequest(String challenger) {
			try {
				dos.writeUTF("BATTLE" + challenger);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

