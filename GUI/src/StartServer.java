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
	
	public String rem_server = "127.0.0.1";
	public static int thread_cnt = 1;
	
	public Socket producer;
	
	public StartServer(int local_port, int remote_port) {
		
		thread_cnt++;
		name = String.format("THREAD-%d", thread_cnt);
		port = local_port;
		rem_port = remote_port;
		System.out.println("Set port to: " + Integer.toString(port));
	}
	
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

		updateGUI("test");
		
		System.out.printf("Starting server on port: %d\n", port);
		String data = null;
		Socket client = null;
		PrintWriter printwriter = null;
		BufferedReader in = null;
		producer = null;
		
		try {
			producer = new Socket(rem_server, rem_port);
			printwriter = new PrintWriter(producer.getOutputStream(), true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, rem_server, rem_port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.printf(
				"%s:ERROR: Failed to connecto to server: %s:%d\n", name, rem_server, rem_port);
		}

		if (producer == null)
			return;

		try {
			server = new ServerSocket(port);
		} catch (IOException e1) {
			  System.out.printf("%s:Error: Tried to create server but failed\n", name);
		}
		try {
			client = server.accept();
		} catch (IOException e1) {
			  System.out.printf(
				  "%s:Error: Error tried to connect to server but failed\n", name);
		}

		
		while (true) {

			InputStreamReader in_stream;
			try {
				in_stream = new InputStreamReader(client.getInputStream());
				in = new BufferedReader(in_stream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.printf(
					"%s:Error: Failed to get input stream from socket\n", name);
			}
			
			try {
				data = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.printf(
					"%s:Error: Failed to create an input buffer\n", name);
			}
			
			if (data == null)
				break;
			System.out.println("Received Data: " + data);
			printwriter.write(data); // write the message to output stream
			printwriter.flush();
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
			producer.close();
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
