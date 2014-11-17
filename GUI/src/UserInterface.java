import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.*;

public class UserInterface extends JFrame { 

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static ServerSocket server = null;
	
	public UserInterface(String name) {
		
		super(name);
		JButton button_Start, button_Stop, button_Reset;
		JLabel label_1,label_2, label_3, label_4, label_5;
		JTextField orientation_Data, left_Eye, right_Eye;
		
	//	int NUM_SERVERS = 1;
		
	//	int arr[NUM_SERVERS];
		
		
		
		getContentPane().setLayout(null);
		
		label_1 = new JLabel(" Head Orientation Data ");
		label_1.setFont(new Font("Serif", Font.BOLD, 16));
		label_1.setLocation(20, 5);
		label_1.setSize(300, 30);
		getContentPane().add(label_1);
		
		label_2 = new JLabel(" Camera Images ");
		label_2.setFont(new Font("Serif", Font.BOLD, 16));
		label_2.setLocation(450, 5);
		label_2.setSize(300, 30);
		getContentPane().add(label_2);
		
		label_3 = new JLabel(" Left Eye ");
		label_3.setFont(new Font("Serif", Font.BOLD, 16));
		label_3.setLocation(350, 50);
		label_3.setSize(300, 30);
		getContentPane().add(label_3);
		
		label_4 = new JLabel(" Right Eye ");
		label_4.setFont(new Font("Serif", Font.BOLD, 16));
		label_4.setLocation(640, 50);
		label_4.setSize(300, 30);
		getContentPane().add(label_4);
		
		label_5 = new JLabel(" ART system control options: ");
		label_5.setFont(new Font("Serif", Font.BOLD, 14));
		label_5.setLocation(20, 270);
		label_5.setSize(300, 50);
		getContentPane().add(label_5);
		
		orientation_Data = new JTextField();
		orientation_Data.setLocation(5, 50);
		orientation_Data.setSize(200, 200);
		getContentPane().add(orientation_Data);
		
		left_Eye = new JTextField();
		left_Eye.setLocation(250, 100);
		left_Eye.setSize(250, 400);
		getContentPane().add(left_Eye);
		
		right_Eye = new JTextField();
		right_Eye.setLocation(550, 100);
		right_Eye.setSize(250, 400);
		getContentPane().add(right_Eye);
		
  	    button_Start = new JButton("Start");
	//	button_Start.addActionListener(this);

  	    button_Start.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  	    		System.out.println("Pressed Start Button");
  	    		
    			StartServer ss = new StartServer(8001, 8000);
    			Thread th = new Thread(ss);
    			th.start();
  	    	}
  	    });
  	    
		button_Start.setFont(new Font("Serif", Font.BOLD, 16));
		button_Start.setForeground(Color.GREEN);
		button_Start.setLocation(45, 350);
		button_Start.setSize(100,30);
		getContentPane().add(button_Start);
		
		button_Stop = new JButton("Stop");
		button_Stop.addActionListener(new ActionListener(){
  	    	public void actionPerformed(ActionEvent e) {
  	    		StopServer(server);
  	    		System.out.print("Hello m stop Server");
  	 	}
  	    });
		button_Stop.setFont(new Font("Serif", Font.BOLD, 16));
		button_Stop.setForeground(Color.RED);
		button_Stop.setLocation(45, 400);
		button_Stop.setSize(100,30);
		getContentPane().add(button_Stop);
		
		button_Reset = new JButton("Reset");
		button_Reset.setFont(new Font("Serif", Font.BOLD, 16));
		button_Reset.setForeground(Color.BLUE);
		button_Reset.setLocation(45, 450);
		button_Reset.setSize(100,30);
		getContentPane().add(button_Reset);
		
	}

	public static void main(String[] args) {
		System.out.print("Art System");
		JFrame frame = new UserInterface("ART System");
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setSize(805,600);
		frame.setResizable(false);
		frame.setVisible(true);

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
