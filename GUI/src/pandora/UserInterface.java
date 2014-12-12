package pandora;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("restriction")
public class UserInterface extends JFrame { 
	
	public static List<String[]> rpi_list = new ArrayList<String[]>();
//	public static List<String[]> rpi_list = ArrayList<String[]>(new String[] {"127.0.0.1", "yaw"}, new String[] {"127.0.0.1", "pitch"});
//	public static List<String[]> rpi_list = Arrays.asList(new String[] {"127.0.0.1", "yaw"}, new String[] {"127.0.0.1", "pitch"});
	
	private static final int STARTED = 0;
	private static final int STOPPED = 1;
//	private static final int RESET = 2;
	
	public int state = STOPPED;
	
	// font type for labels
	private static final String user_font = "Arial";
	
	// Labels for Camera Content Panes
	private static final String lLeftImage = " Left Eye ";
	private static final String lRightImage = " Right Eye ";
	private static final String lheader = " Camera Images ";
	
	// head orientation and control
	private static final String lHeadOrientation = " Head Orientation Data \n";
	private static final String lControlOptions = " ART system control options: ";
	
	// button labels
	private static final String buttonLabelStart = "Start";
	private static final String buttonLabelStop = "Stop";
	private static final String buttonLabelReset = "Reset";
	
	private int hndlOrientData;

	JButton button_Start = null;
	JButton button_Stop, button_Reset;
	static JLabel[] labels = new JLabel[2];
	
//	static JLabel label_1;
//
//	static JLabel label_2;
//
//	static JTextArea orientation_Data;
	static JLabel orientation_Data;
	static JLabel yaw;
	static JLabel pitch;
	static JLabel IP_Addreses;
 	

	static JLabel label_3;
	
	JLabel label_4;

	JLabel label_5;
 
	public static int index = 0;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static ServerSocket server = null;
	
	@SuppressWarnings("restriction")
	public UserInterface(String name) throws IOException {
		super(name);
		
	//	final JTextArea orientation_Data;
		
		JTextField left_Eye, right_Eye;
		
		getContentPane().setLayout(null);
		

		IP_Addreses = new JLabel("Raspberry Pis' IP Addresses");
		IP_Addreses.setFont(new Font(user_font, Font.BOLD, 15));
		IP_Addreses.setLocation(100, 200);
		IP_Addreses.setSize(300, 20);
		getContentPane().add(IP_Addreses);
		

		labels[0] = new JLabel("PI2: NOT RUNNING");
		labels[0].setFont(new Font(user_font, Font.ITALIC, 14));
		labels[0].setLocation(120, 220);

		labels[0] = new JLabel("PI1: NOT RUNNING");
		labels[0].setFont(new Font(user_font, Font.BOLD, 16));
		labels[0].setLocation(25, 250);

		labels[0].setSize(300, 30);
		getContentPane().add(labels[0]);
	//	String[] helper = new String [2];
	//	helper. =rpi_list.get(0);
	//	labels[0].setText(rpi_list.get(0));
		
		labels[1] = new JLabel("PI2: NOT RUNNING");
		labels[1].setFont(new Font(user_font, Font.ITALIC, 14));
		labels[1].setLocation(120, 250);
		labels[1].setSize(300, 30);
		getContentPane().add(labels[1]);
	//	labels[1].setText(rpi_list.get(1));
		
		
		label_3 = new JLabel("Control IP Address: NOT FOUND");
		label_3.setFont(new Font(user_font, Font.BOLD, 16));
		label_3.setLocation(25, 300);
		label_3.setSize(300, 30);
		getContentPane().add(label_3);
		
		orientation_Data = new JLabel("Head Orientation Data");
		orientation_Data.setFont(new Font(user_font, Font.BOLD, 15));
		orientation_Data.setLocation(230, 30);
		orientation_Data.setSize(300, 20);
		getContentPane().add(orientation_Data);
		
		yaw = new JLabel("Yaw: NOT FOUND");
		yaw.setFont(new Font(user_font, Font.ITALIC, 14));
		yaw.setLocation(250, 60);
		yaw.setSize(300, 20);
		getContentPane().add(yaw);
		
		pitch = new JLabel("Pitch: NOT FOUND");
		pitch.setFont(new Font(user_font, Font.ITALIC, 14));
		pitch.setLocation(250, 90);
	    pitch.setSize(300, 20);
		getContentPane().add(pitch);

/*      Commenting the region below to reduce the GUI size		
/*		label_4 = new JLabel(lRightImage);
		label_4.setFont(new Font(user_font, Font.BOLD, 16));
		label_4.setLocation(640, 50);
		label_4.setSize(300, 30);
		getContentPane().add(label_4); */
		
		label_5 = new JLabel(lControlOptions);
		label_5.setFont(new Font(user_font, Font.BOLD, 14));
		label_5.setLocation(10, 10);
		label_5.setSize(300, 20);
		getContentPane().add(label_5);

//		System.out.printf("%d", hndlOrientData);
		
//		orientation_Data = new JTextField(lHeadOrientation);
		/* Commenting the region below to reduce the GUI size	
		orientation_Data = new JTextArea(lHeadOrientation, 5, 1);
		orientation_Data.setLineWrap(true);
		orientation_Data.setEditable(false);
		orientation_Data.setLocation(5, 50);
		orientation_Data.setSize(200, 200); */
		
		
//		JScrollPane scrollPane = new JScrollPane(orientation_Data);
		
		/* Commenting the region below to reduce the GUI size	
		 
		hndlOrientData = orientation_Data.getComponentCount();
//		System.out.printf("%d", hndlOrientData);
		
//		getContentPane().add(scrollPane);
		getContentPane().add(orientation_Data); 
		
		// Your going to have to change this to a JPanel most likely
		/* Commenting the region below to reduce the GUI size	
		left_Eye = new JTextField();
		left_Eye.setLocation(250, 100);
		left_Eye.setSize(250, 400);
		getContentPane().add(left_Eye);
		
		// Your going to have to change this to a JPanel most likely
		right_Eye = new JTextField();
		right_Eye.setLocation(550, 100);
		right_Eye.setSize(250, 400);
		getContentPane().add(right_Eye);*/
		
//		final JButton button_test = new JButton("TEST");

		JButton button_test = create_button(
				"Test", Color.BLACK, new int[] {280, 300}, new int[] {100, 30});
		button_test.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				test();
			}
		});
		getContentPane().add(button_test);

		button_Start = create_button(
				buttonLabelStart, Color.GREEN, new int[] {45, 50}, new int[] {100, 30});
  	    button_Start.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  	    		occurOnStart();
  	    	}
  	    });
  	    
  	    getContentPane().add(button_Start);
  	    
		button_Stop = create_button(
				buttonLabelStop, Color.RED, new int[] {45,  100}, new int[] {100, 30});
		button_Stop.addActionListener(new ActionListener(){
  	    	public void actionPerformed(ActionEvent e) {
  	    		occurOnStop();
  	    	}
  	    });		
		getContentPane().add(button_Stop);
		

		button_Reset = create_button(buttonLabelReset, Color.BLUE, new int[] {45, 150}, new int[] {100, 30});


		getContentPane().add(button_Reset);
		
		pandora.TestIP.get_addr();

	}
	
	@SuppressWarnings("restriction")
	private JButton create_button(String label, Color colour, int[] pos, int[] size) {
		JButton button = new JButton(label);

		button.setFont(new Font(label, Font.BOLD, 16));
		button.setForeground(colour);
		button.setLocation(pos[0], pos[1]);
		button.setSize(size[0], size[1]);

		return button;
	}
	
	public void occurOnStart() {
  		if (state == STARTED) {
  			System.out.println("System already started");
  			return;
  		}
  		System.out.println("Pressed Start Button");
  		state = STARTED;
		StartServer ss = new StartServer(8000, 8000);
		Thread th = new Thread(ss);
		th.start();
		button_Start.setVisible(false);
	}
	
	public void occurOnStop() {
  		state = STOPPED;
//		StopServer(server);
		System.out.printf("Stopping Server\n");
		button_Start.setVisible(true);
	}
	
	@SuppressWarnings("restriction")
	public static void createAndShowGui() {
		System.out.println("Art System");
		JFrame frame = null;
		try {
			frame = new UserInterface("ART System");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setSize(400,400);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	

	
	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		// Everything should operate on the "event-dispatching-thread"
		// This ensures that no events occur prior to the GUI starting
		javax.swing.SwingUtilities.invokeLater(new Runnable () {
			public void run() {
				createAndShowGui();
			}
		});
	}
	
	

/*	public ServerSocket StartServer(int port) {
		System.out.println("Starting server on port: " + Integer.toString(port));
		ServerSocket server = null;

		try {
			server =  new ServerSocket (port);
			Socket socket_1 = server.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(socket_1.getInputStream()));
			while(in.ready()){
				System.out.print("Server Received string: '");
				System.out.println(in.readLine());
			}
			in.close();
			socket_1.close();
		
		} catch (IOException e1) {
			  System.out.print("Server didn't get initialized\n");
			  e1.printStackTrace();
		}
		return server;
	}
*/
	
	public void test() {
		for (String[] element : rpi_list ) {
			System.out.printf("rpi_list is %s %s:\n", element[0], element[1]);
		//	labels[0].setText(element[0]);
		//	labels[1].setText(element[1]);
		}
	}
	
	public void StopServer(ServerSocket server_1){
		 System.out.print("I am STOP server \n");
		// ServerSocket server = new ServerSocket (Port);
			try {
				// server.getLocalPort();
			     server_1.close();
				}
			 catch (IOException e1) {
				  System.out.print("Server didn't get initialized\n");
				  e1.printStackTrace();
			}
		
	}

}

/*@Override
public void actionPerformed(ActionEvent e) {
	 System.out.print("I am the server \n");
	 ServerSocket server = null;
		try {
			server = new ServerSocket (8001);
			while(true){
				Socket socket_1 = server.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket_1.getInputStream()));
				System.out.print("Server Received string: '");
				System.out.println(in.readLine());
				in.close();
		     	socket_1.close();
		    //	server.close();
			}
		} catch (IOException e1) {
			  System.out.print("Server didn't get initialized\n");
			  e1.printStackTrace();
		}
}*/
