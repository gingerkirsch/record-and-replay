package edu.ist.symber.monitor;

import java.util.ArrayList;
import java.util.Iterator;

//** TRANSFORMER
public class Monitor {

	public synchronized static void crashed(Throwable crashedException) {
	}

	// ** for instance fields
	public static void beforeLoad(Object fieldId, String threadId) {
	}

	public static void afterLoad(Object fieldId, String threadId) {
	}

	public static void beforeStore(Object fieldId, String threadId) {
	}

	public static void afterStore(Object fieldId, String threadId) {
	}

	// ** for static fields
	public static void beforeLoad(int fieldId, String threadId) {
	}

	public static void afterLoad(int fieldId, String threadId) {
	}

	public static void beforeStore(int fieldId, String threadId) {
	}

	public static void afterStore(int fielId, String threadId) {
	}

	public static void afterMonitorEnterStatic(Object o, int monitorId,
			String threadId, String monitorName) {
	}

	public static void beforeMonitorEnterStatic(Object o, int monitorId,
			String threadId, String monitorName) {
	}

	public static void afterMonitorEnter(Object o, int monitorId,
			String threadId, String monitorName) {
	}

	public static void beforeMonitorEnter(Object o, int monitorId,
			String threadId, String monitorName) {
	}

	public static void afterConditionEnter(Object lock, Object cond,
			int monitorId, String threadId, String monitorName) {
	}

	public static void beforeConditionEnter(Object lock, Object cond,
			int monitorId, String threadId, String monitorName) {
	}

	public static void exitMonitor(Object o, int monitorId, String threadId,
			String monitorName) {
	}

	public static void mainThreadStartRun(String threadId, String methodName,
			String[] args) {
	}

	public static void threadStartRun(String threadId) {
	}

	public static void threadExitRun(String threadId) {
	}

	public static void startRunThreadBefore(Thread t, String id) {
	}

	public static void joinRunThreadAfter(Thread t, String id) {
	}

}
