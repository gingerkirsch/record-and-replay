
package benchmark;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class BankApp {

	static BankBenchmark bank;
	// array with all accounts
	protected static final int DEFAULT_NUM_ACCOUNTS = 10;  //** should be equal to the number of indexes in class BankBenchmark (for 32 SPEs - 30 accounts; 64 SPEs - 62 accounts)
	protected static int NUM_THREADS;// = 2;
	protected static final int NUM_OPS = 80;
	public static boolean hasbug = false;

	public static void main(String[] args) throws IOException 
	{
		if(args.length==0)
		{
			System.err.println("please specify: --threads=<>... ");
		}
		else 
		{
			
			for(int i = 0; i < args.length; i++){
				if(args[i].contains("--threads")){ 
					int numTh = Integer.valueOf(args[i].substring(args[i].indexOf("=")+1));
					NUM_THREADS = Integer.valueOf(numTh);
				}
			}
		}
		
		long start, end;
		start = System.nanoTime(); //start timestamp
		
		TestThread[] threads = new TestThread[NUM_THREADS];
		bank = new BankBenchmark();

		try{
			for(int i = 0; i< NUM_THREADS; i++){    		
				threads[i] = (TestThread)bank.createThread();
			}
			for(int i = 0; i< NUM_THREADS; i++){
				threads[i].start();
			}
			for(int i = 0; i< NUM_THREADS; i++){
				threads[i].join();
			}  
			if(!hasbug)
				System.out.println("[OK]");
		}
		catch(Exception e)
		{
			System.out.println("Error: "+e.getMessage());
		}  	
		finally
		{
			end = System.nanoTime(); //** end timestamp
			double time = (((double)(end - start)/1000000000));
			Writer writer = new BufferedWriter(new FileWriter("original-time-data.txt", true));
			writer.append(time + " ");
			if (hasbug) writer.append("bug");
			writer.append("\r\n");
			writer.close();//*/
			System.out.println("\nEXECUTION TIME: "+time+"s");
		}
		
	}

}
