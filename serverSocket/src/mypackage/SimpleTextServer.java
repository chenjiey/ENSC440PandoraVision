package mypackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
 
/**
 * This is a simple server application. This server receive a string message
 * from the Android mobile phone and show it on the console.
 * Author by Lak J Comspace
 */
public class SimpleTextServer {
 
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static InputStreamReader inputStreamReader;
	private static BufferedReader bufferedReader;
	private static String message;
 
	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(4444); // Server socket
			System.out.println("Server started. Listening to the port 4444");
		} catch (IOException e) {
			System.out.println("Could not listen on port: 4444");
		}

		try {
			clientSocket = serverSocket.accept(); // accept the client connection
		} catch (IOException ex) {System.out.println("Problem in message reading");}
		
		// int i = 0;
		while (true) {
			System.out.println("Looping");
			try {

				// clientSocket.getInputStream is the part where we actually
				// read the socket. So we need to call this multiple times. 

				inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
				bufferedReader = new BufferedReader(inputStreamReader); // get the client message
				
				// (Jeremy Nov.11.2014) It looks like the server is reading a 
				// single line this is why when the client passes Yaw\nPitch\n 
				// "Yaw" is the first line and hence it is only written.

				message = bufferedReader.readLine();
				System.out.println(message);

			} catch (IOException ex) {
				System.out.println("Problem in message reading2");
			}
			// if (i >= 20) {
			// 	break;
			// } else {
			// 	i++;
			// }
		}
		try {
			inputStreamReader.close();
			clientSocket.close();
		} catch (IOException ex) {
			System.out.println("Problem in message reading3");
		}
		
 
	}
 
}