import java.io.*;
import java.net.*;
import java.util.*;
// ClientHandler class
    public class ClientThread extends Thread {
        private Socket socket = null;
        private AuctionItem item;
		private static BufferedReader fromClientMessages;
		private PrintWriter toClientMessages;
		private static ArrayList<ClientThread> ClientThreads = new ArrayList<ClientThread>();
		private String clientName;
		private Producer producer;
        // Client Thread Constructor
        public ClientThread(Socket socket, AuctionItem item, Producer producer)
        {	
            try
            {
            	this.producer = producer;
            	this.socket = socket;
            	toClientMessages = new PrintWriter(socket.getOutputStream(), true);
            	fromClientMessages = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            this.item = item;
	            this.clientName = fromClientMessages.readLine();//Reads the new clients name
	            ClientThreads.add(this);
	            toAllMessage("Welcome " +clientName + " for joining the Auction.");//Welcomes the new user and informs the other clients
	            toClientMessages.println(item.getResources());
	            toClientMessages.println("The current item on bid is " + item.getCurrentItem() + "\n");
            }catch(IOException IOEX){
        		IOEX.printStackTrace();
        	}
        }
        public void printMenu(){//Menu to be dispalyed to the user for the auction
        	toClientMessages.println(
        		"==================Menu===================\n"+
        		"=========================================\n"+
        		"Enter 0 to list items on Auction \n" +
        		"Enter 1 to show current item on Auction \n"+
        		"Enter 2 to bid on current item\n"+
        		"Enter 3 leave the Auction\n"+
        		"Enter 4 for your competition\n"+
        		"=========================================\n"
        	);
        }
  
		public void run(){
			
			String clientMessage = "";
			this.toAllMessage("You have 45 seconds to make your bid.");
			while((socket.isConnected()))//Runs as long as the client is connectsd
			{
				try{
					clientMessage = fromClientMessages.readLine();
					switch(clientMessage){
					case "0"://Show all the auction items
						toClientMessages.println(item.getResources());
						printMenu();
						break;
					case "1"://Show Current Item
						toClientMessages.println(item.getCurrentItem());
						printMenu();
						break;
					case "2"://Bid on item
						String msg = "";
						BufferedReader option = new BufferedReader(new InputStreamReader(socket.getInputStream()));;
						Double pr = item.getPrice();
						toClientMessages.println("Enter the bid price over " + item.getPrice());
						msg = option.readLine();
						Double bidPrice = Double.parseDouble(msg);

						if(bidPrice > pr){//check if the new bid price is greater than current bid price
							item.addPriceatLoc(bidPrice);
							this.toAllMessage(clientName + " has been raised to " + item.getPrice());
							producer.setPausePeriod((int)(Math.random() * 45000));
							this.toAllMessage("Auction time reset to 45 seconds");
						}else{
							toClientMessages.println("Please! Enter the bid price over " + item.getPrice() + " to bid");
						}
						printMenu();
						break;
					case "3"://Leave the auction
						leaveAuction();
						printMenu();
						break;
					case "4":
						clientNames();//print clients namees
						printMenu();
						break;
					}
					clientMessage = "\n";
				}catch(IOException IOEX){
					closeSocket( socket,  toClientMessages,  fromClientMessages);
					break;
				}
			}
        }
//Closes the socket buffered reader and printwriter
        public void closeSocket(Socket socket, PrintWriter toClientMessages, BufferedReader fromClientMessages){
        	try{
        		if(fromClientMessages != null) fromClientMessages.close();
        		if(toClientMessages != null) toClientMessages.close();
        		if(socket != null) socket.close();
        	}
        	catch(IOException IOEX){
        		closeSocket( socket,  toClientMessages,  fromClientMessages);
        	}
        }
//This helps in leaving the auction 
//After which all other cleints are informed that you have left the auction
        //Your socket is closed
        public void leaveAuction(){
        	try{
	        	String name = clientName;
	        	ClientThreads.remove(this);
	        	toAllMessage(name + " has left the Auction.");
	        	this.toClientMessages.close();
	        	fromClientMessages.close();
	        	this.socket.close();
        	}catch(IOException e){
        		closeSocket( socket,  toClientMessages,  fromClientMessages);
        	}

        }
//Messages to be sent to all connected clients
        public void toAllMessage(String sr){
        	
        	for(ClientThread thre : ClientThreads){

        			if(!thre.clientName.equals(clientName)){
        				thre.toClientMessages.println(sr);
        			}
        	}
        }
//Print all connected clients to the current client
		public void clientNames(){
        	for(ClientThread thre : ClientThreads){
				if(!thre.clientName.equals(clientName)){
					toClientMessages.println(thre.clientName);
				}
				
        	}
		}

    }