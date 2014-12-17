package benchmark;


public class TestThread extends Thread{

	BankBenchmark bank;

	TestThread(BankBenchmark bank) {
		this.bank = bank;
	}

	public void run() {  
		try 
		{
			int it = 5;
			while(it>0) 
			{
				if (it%2==0) {
					for (int i = 0; i < 30; i++) 
					{
						final int srcIndex = (i*(it+1))%bank.numAccounts;
						final int dstIndex = ((i+1)*(it+2))%bank.numAccounts;
						bank.transfer(srcIndex, dstIndex);
					}
				} 
				else 
				{
					try{
						this.sanityCheck();	
					}
					catch(Exception e)
					{
						"Crashed_with".equals(e);
						BankApp.hasbug = true;
					}
				}
				it--;
			}
		} 
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