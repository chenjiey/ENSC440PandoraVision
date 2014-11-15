/**
 * 
 */
package fortyonepost.com.ag;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

/**
 * @author jeremy
 *
 */
public class SendMsg implements Runnable {

	
	public Boolean stop;
	public String ip;
	public int port;
	public Socket client;
	public BlockingQueue<String> queue;
	/**
	 * 
	 */
	public SendMsg(String server_ip, int server_port) {
		// TODO Auto-generated constructor stub
//		BlockingQueue<String> orientation_data = queue;
		// connect to port
		port = server_port;
		ip = server_ip;
		System.out.println("RECEIVED IP");
		System.out.println(ip);
		System.out.println(port);
		
	}

	@Override
	public void run() {
		// Moves current thread into background
		// android.os.Process.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		String message = null;
		stop = true;

		System.out.println("THREAD-2: HAS STARTED");
		// connect the client to the server
		try {
			System.out.println("THREAD-2: TRYING TO CONNECT");
			client = new Socket(ip, port);
			
			System.out.println("THREAD-2: HAS CONNECTED");
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		while (!stop) {
			try {
				message = queue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PrintWriter printwriter = null;
			try {
				printwriter = new PrintWriter(client.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			printwriter.write(message); // write the message to output stream
			printwriter.flush();
			printwriter.close();
		}
		
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}