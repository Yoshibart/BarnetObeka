import java.io.*;
import java.net.*;
import java.util.*;

// Client class
class AuctionClient {
	private PrintWriter toServerMessages;
	private BufferedReader fromServerMessages;
	private Socket socket;
	private String username;
	// driver code
	public static void main(String[] args){
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your name for the Auction: ");
		String username = scanner.nextLine();
		Socket socket = null;
		try
		{
			//argument 0 and 1 are got from the script file client.sh
			//Since argument 1 commes as a string we need to pass an int for the port
			socket = new Socket(args[0], Integer.parseInt(args[1]));
		}
		catch (IOException ioEx)
		{
			System.out.println("\nUnable to set up port!");
			System.exit(1);
		}
		
		AuctionClient ba = new AuctionClient(socket, username);
		ba.fromServerMessages();//Messages from the server
		ba.sendServerMessages();//Message to be sent to the server

	}
//Auction client constructor
	public AuctionClient(Socket socket, String username){
        try{
        	this.socket = socket;
        	toServerMessages = new PrintWriter(socket.getOutputStream(), true);
        	fromServerMessages = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch(IOException IOEX){
			IOEX.printStackTrace();
		}
        this.username = username;//client username form the main function
	}
//creates a new thread for the communication between other clients
	public void fromServerMessages(){
		new Thread(new Runnable(){
			public void run(){
				String messages;
				while(socket.isConnected()){
					try{
						messages = fromServerMessages.readLine();
						if(messages!=null){
							System.out.println(messages);
						}else{
							socket.close();
						}
						
					}catch(IOException IOEX){
						IOEX.printStackTrace();
					}
				}
			}
		}).start();
	}
//Messages to be sent to the server if the socket is connected
	public void sendServerMessages(){	
		toServerMessages.println(username);

		Scanner scanner = new Scanner(System.in);
		while(socket.isConnected()){
			String serverMessage = scanner.nextLine();
			toServerMessages.println(serverMessage);
			toServerMessages.println("\n");
		}
	}

}






