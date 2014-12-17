package edu.ist.symber.common;

import java.io.Serializable;
import java.util.Vector;

public class ThreadTrace implements Serializable {

	public String threadId;
	public Vector<Pair<Long, Long>> storeTrace;
	public Vector<Long> loadTrace;
	public Vector<Long> syncTrace;

	public ThreadTrace() {
		threadId = "";
		storeTrace = new Vector<Pair<Long, Long>>();
		loadTrace = new Vector<Long>();
		syncTrace = new Vector<Long>();
	}

	public ThreadTrace(String tid) {
		threadId = tid;
		storeTrace = new Vector<Pair<Long, Long>>();
		loadTrace = new Vector<Long>();
		syncTrace = new Vector<Long>();
	}

	public void traceLoadConstraint(long storeclock) {
		loadTrace.add(storeclock);
	}

	public void traceStoreConstraint(long storeclock, long loadcount) {
		Pair<Long, Long> store = new Pair<Long, Long>(storeclock, loadcount);
		storeTrace.add(store);
	}

	public void traceSyncConstraint(long syncclock) {
		syncTrace.add(syncclock);
	}

	public Pair<Long, Long> getStoreConstraint() {
		return storeTrace.remove(0);
	}

	public Long getLoadConstraint() {
		return loadTrace.remove(0);
	}

	public Long getSyncConstraint() {
		return syncTrace.remove(0);
	}

	public void printTrace() {
		System.out.print("-- T" + threadId + "\n  storeTrace ("
				+ storeTrace.size() + "): ");
		for (Pair<Long, Long> l : storeTrace) {
			System.out.print(l.toString() + ", ");
		}
		System.out.print("\n  loadTrace (" + loadTrace.size() + "): ");
		for (Long l : loadTrace) {
			System.out.print(l + ", ");
		}
		System.out.print("\n  syncTrace (" + syncTrace.size() + "): ");
		for (Long l : syncTrace) {
			System.out.print(l + ", ");
		}
		System.out.println("");

	}

}
