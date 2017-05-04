import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class FactorClient 
{
	public static Scanner sc = new Scanner(System.in);
	BufferedReader in;
    PrintWriter out;
    private final static BigInteger ZERO = new BigInteger("0");
    private final static BigInteger ONE = new BigInteger("1");
    private final static BigInteger TWO = new BigInteger("2");
    private final static SecureRandom random = new SecureRandom();
    
    public static String results = "";
    
    private void run() throws IOException 
    {
    	System.out.print("Please enter the Server IP: ");
        String serverAddress = sc.nextLine();
        Socket socket = new Socket(serverAddress, 1882);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Connected to Server@" + serverAddress);

        // Process all messages from server, according to the protocol.
        out.println("NEXT");
        while (true) 
        {
        	if(socket.isClosed())break;
            String line = in.readLine();
            if (line.startsWith("NUMBER")) 
            {
            	System.out.println("Recieved Number: " + line.substring(6));
            	BigInteger N = new BigInteger(line.substring(6));
                factor(N);
                out.println("RESULTS" + results);
                System.out.println("Found factor: " + results);
            } 
            if (line.startsWith("CONTINUE")) 
            {
            		out.println("NEXT");
            }
            if(line.startsWith("DONE"))
            {
            	socket.close();
            	System.exit(0);
            }
        }
    }
    
    public static BigInteger construct(BigInteger N) 
	{
        BigInteger divisor;
        BigInteger c = new BigInteger(N.bitLength(), random);
        BigInteger x = new BigInteger(N.bitLength(), random);
        BigInteger xx = x;
        if (N.mod(TWO).compareTo(ZERO) == 0)
        {
            return TWO;
        }

        do 
		{
            x = x.multiply(x).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            divisor = x.subtract(xx).gcd(N);
        } while ((divisor.compareTo(ONE)) == 0);

        return divisor;
    }
    
    public static void factor(BigInteger N) 
	{
        if (N.compareTo(ONE) == 0)
            return;
        if (N.isProbablePrime(20)) 
		{
            results = N.toString();
            return;
        }
        BigInteger divisor = construct(N);
        factor(divisor);
        factor(N.divide(divisor));
    }
    
    public static void main(String[] args) throws IOException
    {
    	FactorClient client = new FactorClient();
        client.run();
    }
}
