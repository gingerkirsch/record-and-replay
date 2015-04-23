package benchmark;

import java.util.concurrent.locks.ReentrantLock;

public class TestThread extends Thread{

	BankBenchmark bank;

	TestThread(BankBenchmark bank) {
		this.bank = bank;
	}

	public void run() {  
		try 
			{
				for (int i = 0; i < BankApp.NUM_OPS / BankApp.NUM_THREADS; i++) 
				{
					final int srcIndex = (i)%bank.numAccounts;
					final int dstIndex = (i + 1)%bank.numAccounts;
					bank.transfer(srcIndex, dstIndex);/*
					BankBenchmark.lock.lock();
					bank.transfer(srcIndex, dstIndex);
					BankBenchmark.lock.unlock();
					//*/
				}
			} 
			/*else 
			{
				try{
					this.sanityCheck();	
				}
				catch(Exception e)
				{
					"Crashed_with".equals(e);
					BankApp.hasbug = true;
				}
			} */
			catch (Exception g) 
			{
			}
		}



	public void sanityCheck() throws Exception{
		if (!bank.checkBalances()) {
			throw new Exception();
		}
	}
}