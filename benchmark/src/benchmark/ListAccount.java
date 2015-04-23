package benchmark;


public class ListAccount {
	
		int value;
		IAccount l1 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l2 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l3 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l4 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l5 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l6 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l7 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l8 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l9 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l10 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l11 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l12 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l13 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l14 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l15 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l16 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l17 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l18 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l19 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount l20 = new IAccount(BankBenchmark.INITIAL_BALANCE);
		IAccount head = new IAccount(BankBenchmark.INITIAL_BALANCE);
		
		public ListAccount()
		{ value = 0;
		  
		}
		
		public ListAccount(int v)
		{
			value = v;
		}
		
	    int getBalance()
	    {
	    	return value;
	    }
	    
	    void setBalance(int v)
	    {
	    	value = v;
	    }
	  
	    
	    IAccount get(int index)
	    {
	    	
	    	IAccount ret = null;
	    	switch(index)
	    	{
	    	case 0: return head; 
	    	case 1: return l1;
	    	case 2: return l2;
	    	case 3: return l3;
	    	case 4: return l4;
	    	case 5: return l5;
	    	case 6: return l6;
	    	case 7: return l7;
	    	case 8: return l8;
	    	case 9: return l9;
	    	case 10: return l10;
	    	case 11: return l11;
	    	case 12: return l12;
	    	case 13: return l13;
	    	case 14: return l14;
	    	case 15: return l15;
	    	case 16: return l16;
	    	case 17: return l17;
	    	case 18: return l18;
	    	case 19: return l19;
	    	case 20: return l20;
	    	
	    	}
	    	return ret;
	    }
	    
	    
	    public void gerador()
	    {
	    	for(int i = 0; i < 150 ; i++)
	    	System.out.println("int l"+i+" = INITIAL_BALANCE;");
	    	
	    	System.out.println("\n\n");
	    	
	    	System.out.println("int get(int index)\n{\nswitch(index)\n{");
	    	
	    	for(int i = 0; i < 150 ; i++)
		    	System.out.println("case "+i+": return l"+i+";");
	    	System.out.println("}\nreturn -1;\n}");
	    	
	    	
	    	System.out.println("\n\nint set(int index,int value)\n{\nswitch(index)\n{");
	    	for(int i = 0; i < 150 ; i++)
		    	System.out.println("case "+i+": l"+i+"=value;");
	    	System.out.println("}\nreturn; \n}");
	    	
	    }
	    
}
