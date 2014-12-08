import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class IPaddress {
	
	public static void main(String[] args) throws IOException {
		int port = 8000;
		//StartServer("localhost", 8000);
		try {
			
			String MyIpAddress = InetAddress.getLocalHost().getHostAddress();
			System.out.print("My IPaddress is :" +MyIpAddress);
			int IpAddrLength = MyIpAddress.length();
			String TestIpAddress = MyIpAddress;
			
			for (int i=1;i<255; i++){
				if(TestIpAddress.length() >IpAddrLength){
					TestIpAddress = TestIpAddress.substring(0, IpAddrLength-2) ;
				}
				TestIpAddress = TestIpAddress.substring(0, IpAddrLength-2) + i ;
				
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
	
		try {
				skt = new Socket(remote_server, port);
				skt.setSoTimeout(1000);
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
	}
}

