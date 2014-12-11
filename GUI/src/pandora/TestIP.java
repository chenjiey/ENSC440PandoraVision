package pandora;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;


public class TestIP {
	
	public static void get_addr() throws IOException {
		int port = 8000;
	//	StartServer("localhost", 8000);
		try {

			String MyIpAddress = InetAddress.getLocalHost().getHostAddress();
			System.out.printf("My IPaddress is : %s\n", MyIpAddress);
			int IpAddrLength = MyIpAddress.length();
			String TestIpAddress = "192.168.1.2";
			
			updateLabel(MyIpAddress);
			
			for (int i=2;i<256; i++){

					if(TestIpAddress.length() >11){
						TestIpAddress = TestIpAddress.substring(0, 10) ;
					}
					TestIpAddress = TestIpAddress.substring(0, 10) + i ;
//					System.out.print(" \nNew IPaddress is :" +TestIpAddress);
					new Thread(new ScanIP(TestIpAddress, port)).start();
					
//					ScanIP("TestIpAddress", port).run();
				}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("Finished Spawning Threads!");
		
	}
	
	 @SuppressWarnings("restriction")
	private static void updateLabel(final String ip) {
		// This adds a function to execute in the Event-Dispatch-Thread
		// Avoiding any race conditions that could occur!
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.printf("Adding %s:\n", ip);
				String label = String.format("Control IP Address: %s", ip);
				pandora.UserInterface.label_3.setText(label); 
			}
		});
	 }
}

 class ScanIP implements Runnable {
	 String remote_server = null;
	 int port = 0;
	 
	 ScanIP(String remServer, int testPort) {
		 remote_server = remServer;
		 port = testPort;
		 
	 }
	 
	 @SuppressWarnings("restriction")
	private void updateGUI(final String ip, final String rotation) {
		// This adds a function to execute in the Event-Dispatch-Thread
		// Avoiding any race conditions that could occur!
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				pandora.UserInterface.rpi_list.add(new String[] {ip, rotation}); 
				System.out.printf("Adding %s: %s\n", ip, rotation);
				String label = String.format("RPI%d: %s", pandora.UserInterface.index + 1, ip);
				pandora.UserInterface.labels[pandora.UserInterface.index].setText(label); 
				pandora.UserInterface.index += 1;
			}
		});
	 }

	 
	 public void run() {
		String data = null;
		Socket skt = null;
		InetSocketAddress skt_addr = null;
		PrintWriter out = null;

		try {
		    InetAddress addr = InetAddress.getByName(remote_server);
		    skt = new Socket();
		    skt_addr = new InetSocketAddress(addr, port);
		    skt.connect(skt_addr, 1000);
			} catch (UnknownHostException e1) {		
//				System.out.println(" Socket Host not found ");
				return;
			} catch (IOException e1) {
//				System.out.println(" Socket cannot be initialised ");
				return;
			}
		System.out.println("Successfully Found");
		BufferedReader in = null;
		try {
			out = new PrintWriter(skt.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
		} catch (IOException e1) {
			System.out.println(" Input stream cannot be initiallised ");
			return;
		}
		System.out.println("Instream created");
		
		try {
			out.println("whoami");
			data = in.readLine();
		} catch (IOException e1) {
//			System.out.print("Data cannot be read ");
			return;

		} finally {
				try {
						in.close();
					} catch (IOException e1) {
						System.out.println(" The input Stream cannot be closed ");
					}
				
				try {
						skt.close();
					} catch (IOException e1) {
						System.out.println(" The socket cannot be closed");
					}
			}

		if (data.equals("yaw")) {
			System.out.printf("The Yaw raspberry pi ipaddress is: %s\n", skt.getInetAddress().toString());
			
		} else if (data.equals("pitch")) {
			System.out.printf("The Pitch raspberry pi ipaddress is: %s\n", skt.getInetAddress().toString());
		}
		System.out.print("Adding IP Address\n");
		updateGUI(skt.getInetAddress().toString(), data);
	}
}

 
/*public static void main(String[] args) throws IOException {
int port = 8007;
//	StartServer("localhost", 8000);
try {
	
	String MyIpAddress = InetAddress.getLocalHost().getHostAddress();
	System.out.print("My IPaddress is :" +MyIpAddress);
	int IpAddrLength = MyIpAddress.length();
	String TestIpAddress = "192.168.1.2";
	
	for (int i=2;i<256; i++){
			
			if(TestIpAddress.length() >11){
				TestIpAddress = TestIpAddress.substring(0, 10) ;
			}
			TestIpAddress = TestIpAddress.substring(0, 10) + i ;
			
			System.out.print(" \nNew IPaddress is :" +TestIpAddress);
			StartServer("TestIpAddress", port);
		}
	
} catch (UnknownHostException e) {

	e.printStackTrace();
}

}

public static void StartServer(String remote_server, int port) throws IOException {
System.out.println("\n Starting server on port: " + Integer.toString(port));
String data = null;
Socket skt = null;
InetSocketAddress skt_addr = null;

try {
	    System.out.println("getting the Inet address");
	    InetAddress addr = InetAddress.getByName(remote_server);
//		skt = new Socket(remote_server, port);
		System.out.println("The inet Address is:" +addr);
	    skt = new Socket();
	    System.out.println("Socket Created");
	    System.out.println("Creating Socket Address");
	    skt_addr = new InetSocketAddress(addr, port);
	    System.out.println("Connecting to Server");
	    skt.connect(skt_addr, 1000);
	    System.out.println("Connected to Server");

	} catch (UnknownHostException e1) {		
		System.out.print(" Socket Host not found ");
		return;
	} catch (IOException e1) {
		System.out.print(" Socket cannot be initialised ");
		return;
	}

	BufferedReader in = null;
try {
		in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
	} catch (IOException e1) {
		System.out.print(" Input stream cannot be initiallised ");
		if(skt.isConnected()){
			skt.close();
		}
		return;
	}
try {
		data = in.readLine();
		System.out.print("Server Received string: "+data);
	} catch (IOException e1) {
		System.out.print(" Data cannot ne read ");
		if(skt.isConnected()){
			skt.close();
		}
		if(in.ready()){
			in.close();
		}
		return;
	}
finally {
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
	}

if (data.equals("yaw")){
	System.out.print("The Yaw raspberry pi ipaddress is:" +skt.getInetAddress());
} else if (data.equals("pitch")) {
	System.out.print("The Pitch raspberry pi ipaddress is:" +skt.getInetAddress());
}
}*/

