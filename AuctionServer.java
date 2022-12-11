import java.io.*;
import java.net.*;
import java.util.*;


public class AuctionServer
{
	private static ServerSocket servSocket;
	private static  int PORT;
	private  static Scanner scanner;
	public static void main(String[] args) throws IOException
	{
		PORT = Integer.parseInt(args[0]);//argument 0 from the server.sh script
		try
		{
			servSocket = new ServerSocket(PORT);
			scanner = new Scanner(System.in);
		}
		catch (IOException ioEx)
		{
			System.out.println("\nUnable to set up port!");
			System.exit(1);
		}

		//Create a Resource object with
		//a starting resource level of 1...
		AuctionItem item = new AuctionItem();//Creates a shared resource
		item.addData("Infinite Games Book",29.23);
		item.addData("Gheigas Kahn Potrait",25.65);
		// System.out.println("\nGheigas Kahn Potrait.\n");
		item.addData("Ticket_Master Resale", 101.87);
		// System.out.println("\nTicket_Master Resale.\n");
		item.addData("W.B Yeats Manuscripts", 100.21);
		// System.out.println("\nW.B Yeats Manuscripts.\n");
		item.addData("Monalisa", 112.32);
		// System.out.println("\nMonalisa.\n");
		item.addData("CaeserPotrait" ,178.50);
		// System.out.println("\nCaeser potrait.\n");
		//Create a Producer thread, passing a reference
		//to the Resource object as an argument to the
		//thread constructor...
		Producer producer = new Producer(item);

		//Start the Producer thread running...

		producer.start();

		new Thread(new Runnable(){//Creates a new thread for the server to add and Item to the Auction itesm
			public void run(){
				System.out.println("Enter new Item for Auction: ");
				String itemName = scanner.nextLine();
				System.out.println("Enter new Item for Auction Price: ");
				Double priceItem = Double.parseDouble(scanner.nextLine());
				item.addData(itemName, priceItem);
			}			
		}).start();


		while(true)//Creates clientthread and start the start them.
		{

			//Wait for a client to make connection...
			Socket client = servSocket.accept();
			System.out.println("\nNew Client has connected.\n");

			//Create a ClientHandler thread to handle all
			//subsequent dialogue with the client, passing
			//references to both the client's socket and
			//the Resource object...
			ClientThread handler = new ClientThread(client,item,producer);

			//Start the ClientThread running...
			handler.start();


		}//Server will run indefinitely.
	}



}

//produces the auction items to be auctioned
class Producer extends Thread
{
	private AuctionItem item;
	private int pausePeriod;

	public Producer(AuctionItem resource)
	{
		item = resource;
		pausePeriod = (int)(Math.random() * 45000);
	}

	public int getPausePeriod(){
		return pausePeriod;
	}
	public void setPausePeriod(int p){
		pausePeriod = p;
	}

	public void run()
	{

		String newLevel;

		do
		{
			try
			{
				//Return the next item for auction
				newLevel = item.nextItem();
				System.out.println("Auction New Item: " + newLevel);

			//'Sleep' for 0-45 seconds...
				sleep(pausePeriod);
			}
			catch (InterruptedException interruptEx)
			{
				System.out.println(interruptEx.toString());
			}
		}while (true);
	}
}

class AuctionItem
{
	private ArrayList<String> Resources;//Holds the auction items
	private ArrayList<Double> prices;//Holds the prices for the autioned items
	private int MAX = 6;
	private int numberResources = -1;//Holds the index for the current auctioned item

	public AuctionItem()
	{
		Resources  = new ArrayList<String>();
		prices  = new ArrayList<Double>();
	}
//Resturns the list of items to be auctioned
	public String getResources()
	{
		String Resource = "============== Auction Items ================\n";

    	for(int i = 0; i < Resources.size(); i++){
    		Resource += "Item: "+(i+1) + " " + Resources.get(i) + " at " + prices.get(i)+ "\n";
    	}

		return Resource;
	}
//returns price of the current auctioned item
	public Double getPrice(){
		return prices.get(numberResources);
	}
	//Replaces the current price with a new bid price
	public void addPriceatLoc(Double price){
		prices.set(numberResources,price);
	}
	//returns the curent item being auctioned
	public String getCurrentItem(){
		return Resources.get(numberResources) + " at " + prices.get(numberResources);
	}
	//adds the price and item for auction
	public void addData(String item, Double price)
	{
		Resources.add(item);
		prices.add(price);
	}

	public String remove(int item)
	{	String ret = "";
		Resources.get(item);
		Resources.remove(item);
		
		return ret;
	}
//Returns the next item to be auctioned
	public synchronized String nextItem()
	{
		try
		{
			while (numberResources >= MAX-1)
				wait();
			numberResources++;

		//'Wake up' any waiting consumer...
			notify();
		}
		catch (InterruptedException interruptEx)
		{
			System.out.println(interruptEx);
		}
		return Resources.get(numberResources) + " at " + prices.get(numberResources);
	}
//Removes the item to be autioned
	public synchronized String removeItem()
	{
		String ret = "";
		try
		{
			while (Resources.size() == -1)
				wait();
			ret = Resources.get(numberResources);
			Resources.remove(numberResources);

		//'Wake up' any waiting producer...
			notify();
		}
		catch (InterruptedException interruptEx)
		{
			System.out.println(interruptEx);
		}
		return ret;
	}
}


