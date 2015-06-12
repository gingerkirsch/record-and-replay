package benchmark;


public class TestThread extends Thread{

	BankBenchmark bank;

	TestThread(BankBenchmark bank) {
		this.bank = bank;
	}

	public void run() {  
		try 
		{
			for (int i = 0; i < BankApp.NUM_OPS/BankApp.NUM_THREADS; i++) 
			{
				final int srcIndex = i%bank.numAccounts;
				final int dstIndex = (i+1)%bank.numAccounts;
				bank.transfer(srcIndex, dstIndex);
			}
			
			this.sanityCheck();
		} 
		catch (Exception g) 
		{
		}
	}


	public void sanityCheck() throws Exception{
		if (!bank.checkBalances()) {
			BankApp.hasbug = true;
		}
	}
}