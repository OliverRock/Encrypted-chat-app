package ChatServer;

import java.io.IOException;import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	private ServerSocket serverSocket; 
	public HashMap<String, String> users = new HashMap<String, String>();
	private static Server server = null; 
	public List<Message> messages = new LinkedList<>();
	public String ID = "Example Server v1.0";
	public String host;
	public int port;
	
	public void start() {
		try {
			System.out.println(host);
			serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		while(true) {
			try {
				Socket socket = serverSocket.accept();
				pool.execute(new Handler(socket));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	}
	
	
	public static Server getInstance(){
		if(server == null){
			server = new Server();
		}
		return server;
	}

	public static void main(String[] args) {
		/**
		 * To run the server give arguments of IP address and port eg 127.0.0.1 50002
		 */
		Server.getInstance().host = args[0];
		Server.getInstance().port = Integer.parseInt(args[1]);
		Server.getInstance().start();
	}

}
