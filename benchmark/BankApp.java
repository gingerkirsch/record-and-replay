
package benchmark;

public class BankApp {

	static BankBenchmark bank;
	// array with all accounts
	protected static final int DEFAULT_NUM_ACCOUNTS = 10;  //** should be equal to the number of indexes in class BankBenchmark (for 32 SPEs - 30 accounts; 64 SPEs - 62 accounts)
	protected static final int NUM_THREADS = 2;
	public static boolean hasbug = false;

	public static void main(String[] args) 
	{
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
				System.out.println("None");
		}
		catch(Exception e)
		{
			System.out.println("Error: "+e.getMessage());
		}  		
	}

}
