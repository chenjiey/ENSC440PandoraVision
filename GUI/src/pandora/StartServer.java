package pandora;

import java.awt.PageAttributes.OrientationRequestedType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
//	public String rem_server1 = "127.0.0.1"; //rpi1
//	public String rem_server2 = "127.0.0.1"; //rpi2

	public static int thread_cnt = 1;
	
	public Socket yaw, pitch;
	
	public StartServer(int local_port, int remote_port) {
		
		thread_cnt++;
		name = String.format("THREAD-%d", thread_cnt);
		port = local_port;
		rem_port = remote_port;
		System.out.println("Set port to: " + Integer.toString(port));
	}

	@SuppressWarnings("restriction")
	private void updateGUI(final String yawUpdate, final String pitchUpdate) {
		// This adds a function to execute in the Event-Dispatch-Thread
		// Avoiding any race conditions that could occur!
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				getContentPane().get
			//	pandora.UserInterface.orientation_Data.append(t_update);
				String yawLabel = String.format("Yaw: %s Degrees", yawUpdate);
				String pitchLabel = String.format("Pitch: %s Degrees", pitchUpdate);
				pandora.UserInterface.yaw.setText(yawLabel);
				pandora.UserInterface.pitch.setText(pitchLabel);
			}
		});
	}

//	@SuppressWarnings("restriction")
//	private void updateGUI(final String t_update) {
//		// This adds a function to execute in the Event-Dispatch-Thread
//		// Avoiding any race conditions that could occur!
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				System.out.println(t_update);
////				getContentPane()
////				orientation_Data.append(t_update);
//			}


	private static String setIP(String axis_expected) {
		String ip = null;
		if (pandora.UserInterface.rpi_list.isEmpty()) {
			System.out.println("ERROR: No Raspberry Pis were found");
			return null;
		}
		
		for (String[] element : pandora.UserInterface.rpi_list){
			if (element[0].startsWith("/")) {
				ip = element[0].replaceFirst("/", "");
			} else {
				ip = element[0]; 
			}
			if (element[1].equals(axis_expected)) {
				return ip;
			}
		}
		System.out.printf("ERROR: Could not find %s\n", axis_expected);
		return null;
	}
	
	public void run() {
		
		System.out.printf("Starting server on port: %d\n", port);
		String data = null;
		Socket client = null;
		PrintWriter yawWriter = null;
		PrintWriter pitchWriter = null;
		BufferedReader in = null;
		yaw = null;
		pitch = null;
		
		String yaw_ip = setIP("yaw");
		String pitch_ip = setIP("pitch");
	
	/*	try {
			// Connect to the Raspberry Pi Yaw
			yaw = new Socket(yaw_ip, rem_port);
			yawWriter = new PrintWriter(yaw.getOutputStream(), true);
		} catch (UnknownHostException e) {
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, yaw_ip, rem_port);
		} catch (IOException e) {
		
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, yaw_ip, rem_port);
		}

		if (yaw == null)
			return;

		try {
			pitch = new Socket(pitch_ip, rem_port); //client socket for the raspberry pi
			pitchWriter = new PrintWriter(pitch.getOutputStream(), true);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, pitch_ip, rem_port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, pitch_ip, rem_port);
		}

		if (pitch == null)
			return;
*/
		//server socket for the android app		
		try {
			// Waiting for connection from Android App

			System.out.print("Starting server for android app");
			server = new ServerSocket(port); 
			client = server.accept();
			System.out.println("Accepted the client connection");

//			PrintWriter out = new PrintWriter(client.getOutputStream(), true);

			System.out.printf("PRINTWRITER CREATED!!!");
			
//			out.write(String.format("%s,%s\n", yaw_ip, pitch_ip));
//			out.flush();

			System.out.printf("Finished!!!");

		} catch (IOException e1) {
			  System.out.printf("%s:Error: Tried to create server but failed\n", name);
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
			updateGUI(datas[0], datas[1]);

			if (data == null)
				break;

			System.out.println("Received Data1: " + datas[0]);
			System.out.println("Received Data2: " + datas[1]);

		//	printwriter1.write(datas[0]); // write the message to output stream
		//	printwriter2.write(datas[1]);
		//	printwriter1.flush();
		//	printwriter2.flush();
			System.out.println("Ending data received 1");

	/*		yawWriter.write(datas[0]); // write the message to output stream
			yawWriter.flush();
			pitchWriter.write(datas[1]);
			pitchWriter.flush(); 

			yawWriter.write(datas[0]); // write the message to output stream
			yawWriter.flush();
			pitchWriter.write(datas[1]);
			pitchWriter.flush();*/
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
			yaw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			pitch.close();
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
