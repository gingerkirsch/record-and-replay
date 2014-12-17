package edu.ist.symber.common.state;

public class SharedVarState {
	public int varId;
	public long storeClock;
	public long loadCount;

	public SharedVarState() {
		storeClock = 0;
		loadCount = 0;
	}

	public SharedVarState(int id) {
		varId = id;
		storeClock = 0;
		loadCount = 0;
	}

	public String toString() {
		return ("[" + varId + "]\tstoreClock: " + storeClock + "\tloadCount: " + loadCount);
	}

}
