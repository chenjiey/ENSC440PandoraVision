/**
 * 
 */
package fortyonepost.com.ag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.R.integer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * @author jeremy
 *
 */
public class SendMsg implements Runnable {

	public Boolean stop;
	public String ip;
	public int port;
	public Socket client;
	public BlockingQueue<String> queue = null;
//	public BlockingQueue<Integer> queue = null;
	
	public final int FAIL = -1;
	public final int SUCCESS = 0;
	public final int CONNECTING = 1;
	public final int CONNECTED = 2;
	public final int RUNNING = 3;
	public int state = SUCCESS;
	/**
	 * 
	 */
	public SendMsg(String server_ip, int server_port) {
		// TODO Auto-generated constructor stub
		// connect to port
		port = server_port;
		ip = server_ip;
		System.out.println("RECEIVED IP");
		System.out.println(ip);
		System.out.println(port);
		System.out.println("Creating Queue");
		queue = new ArrayBlockingQueue<String>(5);
//		queue = new ArrayBlockingQueue<Integer>(5);
	}

	@Override
	public void run() {
		// Moves current thread into background
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
		String message = null;
//		int message = 0;
		
		PrintWriter printwriter = null;
		
		System.out.println("THREAD-2: HAS STARTED");
		// connect the client to the server
		try {
			System.out.printf("THREAD-2: CONNECTING TO: %s:%d\n", ip, port);
			client = new Socket(ip, port);
			System.out.println("THREAD-2: HAS CONNECTED");

			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String rpi_ips = in.readLine();
			Log.i("SEND_MSG", String.format("Received from Control PC: %s", rpi_ips));

			printwriter = new PrintWriter(client.getOutputStream(), true);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.state = this.FAIL;
			System.out.printf("ERROR: Failed to connect to the server");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.state = this.FAIL;
			System.out.printf("ERROR: Failed to create Printwrtier");
			return;
		}
		
		this.state = this.CONNECTED;
		
		while (true) {
			try {
				message = queue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			if (message == "stop") {
				break;
			}
//			if (message == -1) {
//				break;
//			}
			System.out.println("Received message");
			System.out.println(message);					
			System.out.println("Sending message to server");

			printwriter.write(message); // write the message to output stream
			
			System.out.println("Flushing the writer object");
			printwriter.flush();
		}
		System.out.println("Closing the print writer");
		printwriter.close();
		System.out.println("Exiting While loop");
		
		try {
			System.out.println("Socket closed");
			client.close();		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.state = this.SUCCESS;
	} //end of run()
	
}