import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JMenuBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Scrollbar;
import java.awt.TextArea;
import java.awt.Label;
import java.awt.Button;
import java.awt.Rectangle;
import javax.swing.JSeparator;
import javax.swing.JEditorPane;
import javax.swing.JPasswordField;
import java.awt.Color;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Project3Class {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Project3Class window = new Project3Class();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Project3Class() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 666, 437);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Scrollbar scrollbar = new Scrollbar();
		frame.getContentPane().add(scrollbar, BorderLayout.EAST);
		
		TextArea textArea = new TextArea();
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		
		Label label = new Label("Ask Questions Here:");
		frame.getContentPane().add(label, BorderLayout.WEST);
		
		Button postButton = new Button("Post");
		frame.getContentPane().add(postButton, BorderLayout.SOUTH);
		
		JSeparator separator = new JSeparator();
		frame.getContentPane().add(separator, BorderLayout.NORTH);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JButton btnFileUploadButton = new JButton("Upload File");
		btnFileUploadButton.setBackground(new Color(240, 240, 240));
		btnFileUploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		menuBar.add(btnFileUploadButton);
		
		JButton btnNewButton_1 = new JButton("Upload Photo/Image");
		menuBar.add(btnNewButton_1);
		
		Button button_1 = new Button("Upload Video");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		button_1.setActionCommand("Upload Video");
		menuBar.add(button_1);
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
