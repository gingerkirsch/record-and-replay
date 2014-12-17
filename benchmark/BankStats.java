package benchmark;

public class BankStats {
	protected int transferCalls;
	protected int checkCalls;

	BankStats(int transferCalls, int checkCalls) {
		this.transferCalls = transferCalls;
		this.checkCalls = checkCalls;
	}

	public void reportRawInfo(char separator) {
		System.out.printf("%c%d%c%d", separator, transferCalls, separator,
				checkCalls);
	}
}
