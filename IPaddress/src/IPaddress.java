import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class IPaddress {
	
	public String test_server1 = "127.0.0.1"; 
	public String test_server2 = "127.0.0.1"; 
	public String test_server3 = "127.0.0.1"; 
	public String test_server4 = "127.0.0.1"; 
	public String test_server5 = "127.0.0.1"; 
	public String test_server6 = "127.0.0.1"; 
	
	public static void main(String[] args) {
		try {
			
			String MyIpAddress = InetAddress.getLocalHost().getHostAddress();
			System.out.print("My IPaddress is :" +MyIpAddress);
			
			
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		StartServer("localhost", 8000);
	}
	
	public static void StartServer(String remote_server, int port) {
		System.out.println("Starting server on port: " + Integer.toString(port));
		String data = null;
		Socket skt = null;
		try {
			skt = new Socket(remote_server, port);
		
		} catch (UnknownHostException e1) {		
			System.out.print(" Socket Host not found ");
		} catch (IOException e1) {
			System.out.print(" Socket cannot be initialised ");
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
		} catch (IOException e1) {
			System.out.print(" Input stream cannot be initiallised ");
		}
		try {
			data = in.readLine();
			System.out.print("Server Received string: "+data);
		} catch (IOException e1) {
			System.out.print(" Data cannot ne read ");
		}
		
	    try {
			in.close();
		} catch (IOException e1) {
			System.out.print(" The input Stream cannot be closed ");
		}
	    try {
			skt.close();
		} catch (IOException e1) {
			System.out.print(" The socket cannot be closed");
		}
	    if (data.equals("yaw")||(data.equals("pitch"))){
			System.out.print("Yaw/pitch is received");
				  System.out.print("The raspberry pi ipaddress is:" +skt.getInetAddress());
			}
	}
}
