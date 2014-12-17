package benchmark;

public class IAccount {

	int value;

	public IAccount() {
		value = 0;
	}

	public IAccount(int v) {
		value = v;
	}

	int getBalance() {
		return value;
	}

	void setBalance(int v) {
		value = v;
	}
}
