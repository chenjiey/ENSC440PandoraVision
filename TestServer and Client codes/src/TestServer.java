import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {

	public static void main(String[] args) {
		
			ServerSocket server = null;
			
		try {
				server = new ServerSocket (8000);
				Socket socket_1 = server.accept();
			    PrintWriter out = new PrintWriter(socket_1.getOutputStream(), true);
			    out.print("yaw");	
	            out.close();
			    socket_1.close();
			    server.close();
			} catch (IOException e1) {
				  System.out.print("Server didn't get initialized\n");
				  e1.printStackTrace();
			}
			
		}	
	}

