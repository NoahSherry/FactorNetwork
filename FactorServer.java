import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * TODO List
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging.
 *     
 *     3. Check for Infinite Number Loop
 */
public class FactorServer 
{
    private static final int PORT = 1882;
    private static File logFile = new File("log.txt");
    public static PrintWriter log;
    public static int init = 0;
    public static int results = 0;
    public static boolean fin = false;
    public static ArrayList<BigInteger> numbers = new ArrayList<BigInteger>();
    
    public static void main(String[] args) throws Exception 
    {
    	System.out.print("Enter the file name with extension : ");

        Scanner input = new Scanner(System.in);
        String filename = input.nextLine();
        if(filename.isEmpty()) filename = "numbers.txt";
        File file = new File(filename);
        input.close();
        input = new Scanner(file);
     
    	while(input.hasNextLine())
    	{
    		numbers.add(new BigInteger(input.nextLine()));
    		init++;
    	}
    	input.close();
    	System.out.println("Numbers: " + numbers);
    	log = new PrintWriter(logFile);
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try 
        {
            while (true) 
            {
                new Client(listener.accept()).start();
            }
        } finally 
        {
            listener.close();
        }
    }
    
    private static class Client extends Thread
    {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private BigInteger current;

        public Client(Socket socket) 
        {
            this.socket = socket;
            name = socket.getRemoteSocketAddress().toString();
            name = name.substring(1);
        }

        public void run()
        {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("ClientID: " + name + " connected to Server.");
                log.println("ClientID: " + name + " connected to Server.");
                log.println();
                while (true) 
                {
                	if((init == results) && numbers.isEmpty() && current.equals(new BigInteger("0")))
                	{
                		out.println("DONE");
               			break;
               		}
                	
                    String input = in.readLine();
                    if(input.startsWith("NEXT") && (!numbers.isEmpty()))
                    {
                    	if(numbers.get(0) != BigInteger.ZERO)out.println("NUMBER" + numbers.get(0));
                    	current = new BigInteger(numbers.get(0).toString());
                    	System.out.println("Gave " + name + " number: " + numbers.get(0).toString());
                    	log.println("Gave " + name + " number: " + numbers.get(0).toString());
                    	numbers.remove(0);
                    }
                    
                    
               	 	input = in.readLine();
                    if(input.startsWith("RESULTS"))
                    {
                    	results++;
                    	System.out.println("ClientID: " + name + " Number: " + current +" Factor:" + input.substring(7));
                    	log.println("ClientID: " + name + " Number: " + current +" Factor:" + input.substring(7));
                    	current = new BigInteger("0");
                    	out.println("CONTINUE");
                    }
                }
            } catch (IOException e) 
            {
                //System.out.println(e);
            } finally 
            {
                try 
                {
                	if(!current.equals(BigInteger.ZERO))numbers.add(current);
                	current = new BigInteger("0");
                	System.out.println("ClientID: " + name + " disconnected from Server.");
                	log.println("ClientID: " + name + " disconnected from Server.");
                    socket.close();
                    if(numbers.isEmpty() && (init == results))
                    {
                    	String done = "All numbers factored. Good work team.";
                    	System.out.println(done);
                    	log.println(done);
                    	log.close();
                    	System.exit(0);
                    }
                } catch (IOException e) {}
            }
        }
    }
}
