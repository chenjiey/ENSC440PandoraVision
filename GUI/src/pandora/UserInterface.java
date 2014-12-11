package pandora;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

@SuppressWarnings("restriction")
public class UserInterface extends JFrame { 
	
	public static List<String[]> rpi_list = new ArrayList<String[]>();
	
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

	final JButton button_Start;
	JButton button_Stop, button_Reset;
	static JLabel[] labels = new JLabel[2];
//	static JLabel label_1;
//
//	static JLabel label_2;
//
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
		
		final JTextArea orientation_Data;
		
		JTextField left_Eye, right_Eye;
		
	//	int NUM_SERVERS = 1;
		
	//	int arr[NUM_SERVERS];
		
		getContentPane().setLayout(null);
		
		
		labels[0] = new JLabel("PI1: NOT RUNNING");
		labels[0].setFont(new Font(user_font, Font.BOLD, 16));
		labels[0].setLocation(25, 250);
		labels[0].setSize(300, 30);
		getContentPane().add(labels[0]);
		
	
		labels[1] = new JLabel("PI2: NOT RUNNING");
		labels[1].setFont(new Font(user_font, Font.BOLD, 16));
		labels[1].setLocation(25, 275);
		labels[1].setSize(300, 30);
		getContentPane().add(labels[1]);
		
		label_3 = new JLabel("Control IP Address: NOT FOUND");
		label_3.setFont(new Font(user_font, Font.BOLD, 16));
		label_3.setLocation(25, 300);
		label_3.setSize(300, 30);
		getContentPane().add(label_3);

/*      Commenting the region below to reduce the GUI size		
/*		label_4 = new JLabel(lRightImage);
		label_4.setFont(new Font(user_font, Font.BOLD, 16));
		label_4.setLocation(640, 50);
		label_4.setSize(300, 30);
		getContentPane().add(label_4); */
		
		label_5 = new JLabel(lControlOptions);
		label_5.setFont(new Font(user_font, Font.BOLD, 14));
		label_5.setLocation(100, 10);
		label_5.setSize(300, 50);
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
		getContentPane().add(orientation_Data); */
		
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
		
  	    button_Start = new JButton(buttonLabelStart);
  	    
		final JButton button_test = new JButton("TEST");
		button_test.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				test();
				// TODO Auto-generated method stub
				
			}
		});
		
		button_test.setFont(new Font("TEST", Font.BOLD, 16));
		button_test.setForeground(Color.GREEN);
		button_test.setLocation(200, 150);
		button_test.setSize(100,30);
		getContentPane().add(button_test);
		
	//	button_Start.addActionListener(this);

  	    button_Start.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
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
  	    });
  	    
		button_Start.setFont(new Font(user_font, Font.BOLD, 16));
		button_Start.setForeground(Color.GREEN);
		button_Start.setLocation(145, 100);
		button_Start.setSize(100,30);
		getContentPane().add(button_Start);
		
		
		button_Stop = new JButton(buttonLabelStop);
		button_Stop.addActionListener(new ActionListener(){
  	    	public void actionPerformed(ActionEvent e) {
  	    		state = STOPPED;
//  	    		StopServer(server);
  	    		System.out.printf("Stopping Server\n");
  	    		button_Start.setVisible(true);
  	    		
  	    	/*	orientation_Data.append("test\n");*/
  	 	}
  	    });
		
		button_Stop.setFont(new Font(user_font, Font.BOLD, 16));
		button_Stop.setForeground(Color.RED);
		button_Stop.setLocation(145, 150);
		button_Stop.setSize(100,30);
		getContentPane().add(button_Stop);
		
		button_Reset = new JButton(buttonLabelReset);
		button_Reset.setFont(new Font(user_font, Font.BOLD, 16));
		button_Reset.setForeground(Color.BLUE);
		button_Reset.setLocation(145, 200);
		button_Reset.setSize(100,30);
		getContentPane().add(button_Reset);
		
		pandora.TestIP.get_addr();
		
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
