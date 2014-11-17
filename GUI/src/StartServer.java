import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;



public class StartServer implements Runnable {

	public String name;
	public ServerSocket server;
	public int port, rem_port;
	public String ip;

	public Socket producer;
	
	public StartServer(int local_port, int remote_port) {
		
		port = local_port;
		rem_port = remote_port;
		System.out.println("Set port to: " + Integer.toString(port));
	}
	
	public void run() {

		System.out.println("Starting server on port: " + Integer.toString(port));
		String data = null;
		Socket client = null;
		PrintWriter printwriter = null;
		
		try {
			producer = new Socket("127.0.0.1", rem_port);
			printwriter = new PrintWriter(producer.getOutputStream(), true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			server = new ServerSocket(port);
			client = server.accept();
			while (true) {

				InputStreamReader in_stream = new InputStreamReader(client.getInputStream());
				BufferedReader in = new BufferedReader(in_stream);
				
				data = in.readLine();
				
				if (data == null)
					break;
				System.out.println("Received Data: " + data);
		
				printwriter.write(data); // write the message to output stream
				printwriter.flush();
			}

			System.out.println("Shutting down the server");
			client.close();
			producer.close();
			server.close();
		
		} catch (IOException e1) {
			  System.out.print("Server didn't get initialized\n");
			  e1.printStackTrace();
		}			
		
	}

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		
//	}

}
