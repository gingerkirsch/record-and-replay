package benchmark;

public class IAccount2 {
int value;
	
	public IAccount2()
	{value = 0;}
	
	public IAccount2(int v)
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
    
    IAccount toIAccount()
    {
    	return new IAccount(value);
    }
}
