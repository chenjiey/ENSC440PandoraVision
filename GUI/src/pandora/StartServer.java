package pandora;

import java.awt.PageAttributes.OrientationRequestedType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;



public class StartServer implements Runnable {
	
	public String name;
	public ServerSocket server;
	public int port, rem_port;
	public String ip;
	
//	public String rem_server = "127.0.0.1";
	public String rem_server1 = "127.0.0.1"; //rpi1
	public String rem_server2 = "127.0.0.1"; //rpi2
	public static int thread_cnt = 1;
	
	public Socket producer1, producer2;
	
	public StartServer(int local_port, int remote_port) {
		
		thread_cnt++;
		name = String.format("THREAD-%d", thread_cnt);
		port = local_port;
		rem_port = remote_port;
		System.out.println("Set port to: " + Integer.toString(port));
	}
	
	@SuppressWarnings("restriction")
	private void updateGUI(final String t_update) {
		// This adds a function to execute in the Event-Dispatch-Thread
		// Avoiding any race conditions that could occur!
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println(t_update);
//				getContentPane().get
//				orientation_Data.append(t_update);
			}
		});
	}
	
	public void run() {

//		updateGUI("test");
		
		System.out.printf("Starting server on port: %d\n", port);
		String data = null;
		Socket client = null;
		PrintWriter printwriter1 = null;
		PrintWriter printwriter2 = null;
		BufferedReader in = null;
		producer1 = null;
		producer2 = null;
		
		try {
			System.out.println("I am the client1");
			producer1 = new Socket(rem_server1, rem_port); //client socket for the raspberry pi
			printwriter1 = new PrintWriter(producer1.getOutputStream(), true);
			System.out.println("I am the client1 connected");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, rem_server1, rem_port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, rem_server1, rem_port);
		}

		if (producer1 == null)
			return;

		try {
			System.out.println("I am the client2");
			producer2 = new Socket(rem_server2, 8002
					); //client socket for the raspberry pi
			printwriter2 = new PrintWriter(producer2.getOutputStream(), true);
			System.out.println("I am the client2 connected");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, rem_server1, rem_port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, rem_server1, rem_port);
		}

		if (producer2 == null)
			return;

		//server socket for the android app		
		try {
			System.out.println("I am the app server");
			server = new ServerSocket(port); 
		} catch (IOException e1) {
			  System.out.printf("%s:Error: Tried to create server but failed\n", name);
		}
		try {
			client = server.accept();
			System.out.println("I got the client connection");
		} catch (IOException e1) {
			  System.out.printf(
				  "%s:Error: Error tried to connect to server but failed\n", name);
		}

		
		while (true) {
			
			InputStreamReader in_stream;
			try {
				System.out.println("I am reading the app data");
				in_stream = new InputStreamReader(client.getInputStream());
				in = new BufferedReader(in_stream);
				
			} catch (IOException e) {
			
				e.printStackTrace();
				System.out.printf(
					"%s:Error: Failed to get input stream from socket\n", name);
			}
			
			try {
				System.out.println("Reading the app data with comma");
				data = in.readLine();
				System.out.println("done reading the app data");
			} catch (IOException e) {
				
				e.printStackTrace();
				System.out.printf(
					"%s:Error: Failed to create an input buffer\n", name);
			}

			String[] datas = data.split(",");
			
			
			if (data == null)
				break;
			System.out.println("Received Data1: " + datas[0]);
			System.out.println("Received Data2: " + datas[1]);
			printwriter1.write(datas[0]); // write the message to output stream
			printwriter2.write(datas[1]);
			printwriter1.flush();
			printwriter2.flush();
		}

		System.out.println("Shutting down the server");
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			producer1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			producer2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		
//	}

}
