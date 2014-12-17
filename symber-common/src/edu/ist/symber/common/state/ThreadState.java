package edu.ist.symber.common.state;

public class ThreadState {
	public String threadId;
	public long storeClock;
	public long syncClock;

	public ThreadState() {
		storeClock = 0;
		syncClock = 0;
	}

	public ThreadState(String tid) {
		threadId = tid;
		storeClock = 0;
		syncClock = 0;
	}

	public String toString() {
		return ("[T" + threadId + "]\tstoreClock: " + storeClock
				+ "\tsyncClock: " + syncClock);
	}

}
