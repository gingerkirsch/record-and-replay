package edu.ist.symber.common;

import java.io.Serializable;
import java.util.Vector;

public class ThreadTraceShort implements Serializable {

	public String threadId;
	public Vector<Pair<Short, Short>> storeTrace;
	public Vector<Short> loadTrace;
	public Vector<Short> syncTrace;

	public ThreadTraceShort() {
		threadId = "";
		storeTrace = new Vector<Pair<Short, Short>>();
		loadTrace = new Vector<Short>();
		syncTrace = new Vector<Short>();
	}

	public ThreadTraceShort(String tid) {
		threadId = tid;
		storeTrace = new Vector<Pair<Short, Short>>();
		loadTrace = new Vector<Short>();
		syncTrace = new Vector<Short>();
	}

	public void traceLoadConstraint(short storeclock) {
		loadTrace.add(storeclock);
	}

	public void traceStoreConstraint(short storeclock, short loadcount) {
		Pair<Short, Short> store = new Pair<Short, Short>(storeclock, loadcount);
		storeTrace.add(store);
	}

	public void traceSyncConstraint(short syncclock) {
		syncTrace.add(syncclock);
	}

	public Pair<Short, Short> getStoreConstraint() {
		return storeTrace.remove(0);
	}

	public Short getLoadConstraint() {
		return loadTrace.remove(0);
	}

	public Short getSyncConstraint() {
		return syncTrace.remove(0);
	}

	public void printTrace() {
		System.out.print("-- T" + threadId + "\n  storeTrace ("
				+ storeTrace.size() + "): ");
		for (Pair<Short, Short> l : storeTrace) {
			System.out.print(l.toString() + ", ");
		}
		System.out.print("\n  loadTrace (" + loadTrace.size() + "): ");
		for (Short l : loadTrace) {
			System.out.print(l + ", ");
		}
		System.out.print("\n  syncTrace (" + syncTrace.size() + "): ");
		for (Short l : syncTrace) {
			System.out.print(l + ", ");
		}
		System.out.println("");

	}

}
