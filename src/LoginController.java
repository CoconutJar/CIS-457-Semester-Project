import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

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

public class LoginController implements Initializable {
	@FXML
	private Label closeBtn;
	@FXML
	private TextField unTextfield, pwTextfield;
	@FXML
	private Button loginBtn;

	private double x, y;

	private String userName;
	private String password;
	private int port;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Socket s;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		InetAddress ip;
		// Connection to server.
		try {
			ip = InetAddress.getByName("localhost");
			s = new Socket(ip, 3158);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Handles closing of window
	public void closeBtnAction() {
		Stage stage = (Stage) closeBtn.getScene().getWindow();
		try {
			dos.writeUTF("QUIT");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Application closed.");
		stage.close();
		System.exit(1);
	}

	// Prints user input to system
	private void printConnectInput() {
		System.out.println("Username: " + unTextfield.getText() + "\nPassword: " + pwTextfield.getText());
	}

	// Handles "Connect" Button. Submits connection/user information.
	// Changes window to FileTable window.
	public void loginBtnPushed(ActionEvent event) throws IOException {
		printConnectInput();
		String rec = signIn("NEW", unTextfield.getText(), pwTextfield.getText());
		StringTokenizer tokens = new StringTokenizer(rec, ":");
		String status = tokens.nextToken();
		String msg = tokens.nextToken();

		if (status.startsWith("2")) {
			System.out.println("Sucess: " + msg);
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("home.fxml"));
			Parent fileTableParent = loader.load();

			// Setup for window (stage) change.
			Scene fileTableScene = new Scene(fileTableParent);

			HomeController controller = loader.getController();

			controller.initData(s, dis, dos, port, userName, password);

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
		} else {
			System.out.println("Error " + msg);
		}
	}

	public String signIn(String status, String userName, String password) throws IOException {

		// IP of the server to connect to.
		this.userName = userName;
		this.password = password;
		// Sends the initial connectionString that holds information about the client.
		dos.writeUTF(status + " " + userName + " " + password);

		String response = dis.readUTF();

		int sep = response.lastIndexOf(":");
		int div = response.lastIndexOf("-");

		String code = response.substring(0, sep);
		String msg = response.substring(sep + 2, div);
		System.out.println(msg);

		if (code.startsWith("20")) {

			String port2 = response.substring(div + 1);

			port = Integer.parseInt(port2);
			port += 4158;
			System.out.println(port2);

			return "200:" + msg;
		} else {
			return "100:" + msg;
		}

	}
}
