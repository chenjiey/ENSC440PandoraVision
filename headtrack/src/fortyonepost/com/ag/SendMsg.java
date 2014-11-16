/**
 * 
 */
package fortyonepost.com.ag;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.R.integer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
		System.out.println("Creating Queue");
		queue = new ArrayBlockingQueue<String>(5);
	}

	@Override
	public void run() {
		// Moves current thread into background
		// android.os.Process.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					String message = null;
					stop = true;

					PrintWriter printwriter = null;
					
					System.out.println("THREAD-2: HAS STARTED");
					// connect the client to the server
					try {
						System.out.println("THREAD-2: TRYING TO CONNECT");
						client = new Socket(ip, port);
						
						System.out.println("THREAD-2: HAS CONNECTED");
						
						printwriter = new PrintWriter(client.getOutputStream(), true);
						
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					
					while (stop) {
						try {
							message = queue.take();
//							printwriter = new PrintWriter(client.getOutputStream(), true);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						System.out.println("Received message");
						System.out.println(message);					
						System.out.println("Sending message to server");

						printwriter.write(message); // write the message to output stream
						
						System.out.println("Flushing the writer object");
						printwriter.flush();
						
//						printwriter.close();
					}
//					printwriter.flush();
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
	} //end of run()
	
}