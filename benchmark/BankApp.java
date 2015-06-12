
package benchmark;

public class BankApp {

	static BankBenchmark bank;
	// array with all accounts
	protected static final int DEFAULT_NUM_ACCOUNTS = 2;  //** should be equal to the number of indexes in class BankBenchmark (for 32 SPEs - 30 accounts; 64 SPEs - 62 accounts)
	protected static final int NUM_THREADS = 2;
	protected static final int NUM_OPS = 4;
	public static boolean hasbug = false;

	public static void main(String[] args) 
	{
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
			System.out.println("\nEXECUTION TIME: "+time+"s");
		}
		
	}

}
