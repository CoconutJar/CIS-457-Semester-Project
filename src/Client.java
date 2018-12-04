import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class Client {
	private Socket s;
	private DataOutputStream dos;
	private DataInputStream dis;
	// private BufferedReader dis;
	private ArrayList<String> onlineFriends = new ArrayList<String>();
	private ArrayList<Status> newsFeed;
	private String userName;
	private String password;

	public boolean loggedOn;

	/****
	 * 
	 * Forms a connection to the Centralized_Server and starts the local server
	 * thread.
	 * 
	 * @throws IOException
	 * 
	 ****/
	public Client() throws IOException {
		InetAddress ip = InetAddress.getByName("localhost");
		// Connection to server.
		s = new Socket(ip, 3158);

		// Set up input and output stream to send and receive messages.
		dis = new DataInputStream(s.getInputStream());

		dos = new DataOutputStream(s.getOutputStream());
	}

	public String signIn(String status, String userName, String password) throws IOException {

		// IP of the server to connect to.
		this.userName = userName;
		this.password = password;
		// Sends the initial connectionString that holds information about the client.
		dos.writeUTF(status + " " + userName + " " + password);

		String response = dis.readUTF();

		StringTokenizer tokens = new StringTokenizer(response, ": ");
		String stat = tokens.nextToken();
		String msg = tokens.nextToken();

		if (stat.startsWith("20")) {
			loggedOn = true;
			startThread();
			update();
			return "200" + msg;
		} else {
			return "100" + msg;
		}

	}

	public void startThread() {
		Thread localServer = new Thread(new Runnable() {
			public void run() {
				while (loggedOn) {
					try {
						String fromServer = dis.readUTF();
						StringTokenizer tokens = new StringTokenizer(fromServer, ": ");
						String command = tokens.nextToken();
						if (command.equals("ALERT")) {

							// TODO show alert
							String alert = tokens.nextToken();

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

							// TODO display newsFeed.
							newsFeed.add(newStatus);

						} else if (command.equals("BATTLE")) {

							// TODO ??????
							String challenger = tokens.nextToken();

						}

					} catch (IOException e) {
						e.printStackTrace();
					}
					update();
				}
			}
		});
		localServer.start();
	}

	/****
	 * 
	 * Allows the client to search the Central Server for available files to
	 * download.
	 * 
	 ****/
	public void addFriend(String userName) {
		try {
			dos.writeUTF("ADD" + userName);
			update();
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
			update();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/****
	 * 
	 * Sends a message to the server closing the connection.
	 * 
	 ****/
	public void quit() {

		try {
			dos.writeUTF("QUIT");
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Ends the thread.
		loggedOn = false;
	}

	private void update() {
		String postInfo = "";
		String friendInfo = "";
		newsFeed.clear();
		onlineFriends.clear();

		do {
			try {

				postInfo = dis.readUTF();

				StringTokenizer tokens = new StringTokenizer(postInfo, "%");
				String user = tokens.nextToken();
				String msg = tokens.nextToken();
				String time = tokens.nextToken();
				String comments = tokens.nextToken();
				String likes = tokens.nextToken();
				String ID = tokens.nextToken();
				Status newStatus = new Status(user, msg, time, comments, likes, ID);

				newsFeed.add(newStatus);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (!postInfo.equals("END"));

		do {
			try {

				friendInfo = dis.readUTF();

				onlineFriends.add(friendInfo);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (!friendInfo.equals("END"));

		Collections.sort(newsFeed);
		Collections.sort(onlineFriends);

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
	public int sec;

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
		String tempTime = tokens.nextToken();
		tokens = new StringTokenizer(tempTime, ":");

		String sHour = tokens.nextToken();
		String sMin = tokens.nextToken();
		String sSec = tokens.nextToken();

		hour = Integer.parseInt(sHour);
		min = Integer.parseInt(sMin);
		sec = Integer.parseInt(sSec);

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
