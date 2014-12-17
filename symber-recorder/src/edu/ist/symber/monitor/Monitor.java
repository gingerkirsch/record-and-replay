package edu.ist.symber.monitor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.ist.symber.Util;
import edu.ist.symber.common.Pair;
import edu.ist.symber.generator.CrashTestCaseGenerator;

//** RECORDER
public class Monitor {
	public static boolean isCrashed = false;
	public static Throwable crashedException = null;
	public static String methodname;
	public static String[] mainargs;
	private static String mainthreadname;

	// ** data structures for thread consistent identification
	public volatile static HashMap<String, Integer> threadChildrenCounter; // **
																			// allows
																			// to
																			// generate
																			// deterministic
																			// thread
																			// identifiers
																			// by
																			// counting
																			// the
																			// number
																			// of
																			// children
																			// threads
																			// spawned
	public static Map<Thread, String> MapBackupThreadName; // ** used to keep
															// the thread name
															// consistent during
															// the execution
															// (because the name
															// can be reset by
															// the target
															// program after the
															// thread
															// initialization)

	// ** data structures for tracing log file events
	// CODE HERE - DEFINE THE NECESSARY DATA STRUCTURES

	public static void initialize() {
		threadChildrenCounter = new HashMap<String, Integer>();
		MapBackupThreadName = new HashMap<Thread, String>();
	}

	// ** for instance fields
	public static void beforeLoad(Object fieldId, String threadId) {
		beforeLoad(System.identityHashCode(fieldId), threadId);
	}

	public static void afterLoad(Object fieldId, String threadId) {
		afterLoad(System.identityHashCode(fieldId), threadId);
	}

	public static void beforeStore(Object fieldId, String threadId) {
		beforeStore(System.identityHashCode(fieldId), threadId);
	}

	public static void afterStore(Object fieldId, String threadId) {
		afterStore(System.identityHashCode(fieldId), threadId);
	}

	// ** for static fields
	/**
	 * Recording load (read) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 */
	public static void beforeLoad(int fieldId, String threadId) {
	}

	/**
	 * Recording load (read) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 */
	public static void afterLoad(int fieldId, String threadId) {
		try {
			// CODE HERE - HANLDE READ OPERATIONS

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Recording store (writes) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 */
	public static void beforeStore(int fieldId, String threadId) {
		try {
			// CODE HERE - HANLDE WRITE OPERATIONS

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Recording store (writes) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 */
	public static void afterStore(int fieldId, String threadId) {
	}

	// ** for static monitors
	public static void afterMonitorEnterStatic(Object o, int monitorId,
			String threadId, String monitorName) {
		afterMonitorEnter(o.getClass(), monitorId, threadId, monitorName);
	}

	public static void beforeMonitorEnterStatic(Object o, int monitorId,
			String threadId, String monitorName) {
		beforeMonitorEnter(o.getClass(), monitorId, threadId, monitorName);
	}

	/**
	 * Recording calls to condition.signal().
	 * 
	 * @param lock
	 * @param cond
	 * @param monitorId
	 * @param threadId
	 * @param monitorName
	 */
	public static void beforeConditionEnter(Object lock, Object cond,
			int monitorId, String threadId, String monitorName) {
	}

	/**
	 * Recording calls to condition.await().
	 * 
	 * @param lock
	 * @param cond
	 * @param monitorId
	 * @param threadId
	 * @param monitorName
	 */
	public static void afterConditionEnter(Object lock, Object cond,
			int monitorId, String threadId, String monitorName) {
	}

	/**
	 * Recording monitor acquisition operations (instance invocations).
	 * 
	 * @param o
	 * @param monitorId
	 * @param threadId
	 */
	public static void beforeMonitorEnter(Object o, int monitorId,
			String threadId, String monitorName) {
	}

	/**
	 * Recording monitor acquisition operations (instance invocations).
	 * 
	 * @param o
	 * @param monitorId
	 * @param threadId
	 */
	public static void afterMonitorEnter(Object o, int monitorId,
			String threadId, String monitorName) {
	}

	public static void exitMonitor(Object o, int monitorId, String threadId,
			String monitorName) {
	}

	public static void mainThreadStartRun(String threadId, String methodName,
			String[] args) {
		try {
			Thread.currentThread().setName("0");
			mainthreadname = Thread.currentThread().getName();
			mainargs = args;
			methodname = methodName;

			// **to generate deterministic thread identifiers
			threadChildrenCounter.put("0", 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Whenever a thread starts running, a new entry is created in the trace
	 * file, to log its execution.
	 * 
	 * @param t
	 * @param parentId
	 */
	public static void threadStartRun(String threadId) {
		try {
			threadChildrenCounter.put(threadId, 1);
			System.out.println(">> Start run T" + threadId);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void threadExitRun(String threadId) {
	}

	/**
	 * Wrap thread creation for consistent thread identification across runs.
	 * The new thread identifier will be the thread's parent thread ID
	 * associated with the counter value. For instance, suppose a thread ti
	 * forks its j-th child thread, this child thread will be identified as ti:j
	 * .
	 * 
	 * @param t
	 * @param parentId
	 */
	public synchronized static void startRunThreadBefore(Thread t,
			String parentId) {
		try {
			parentId = Thread.currentThread().getName();
			int childCounter = threadChildrenCounter.get(parentId);
			String newThreadName;

			// ** the ith thread spawned by the main thread should have thread
			// id = i
			if (!parentId.equals("0"))
				newThreadName = parentId + "." + childCounter;
			else
				newThreadName = String.valueOf(childCounter);

			t.setName(newThreadName);
			childCounter++;
			threadChildrenCounter.put(parentId, childCounter);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public synchronized static void joinRunThreadAfter(Thread t, String threadId) {
	}

	public synchronized static void crashed(Throwable crashedException) {
		isCrashed = true;
		System.out.println("--- crashed! ---" + crashedException);
	}

	public static String saveMonitorData(String appname) {
		String traceFile_ = null;
		File traceFile_monitordata = null;
		ObjectOutputStream fw_monitordata;

		return traceFile_;
	}

	public static void generateTestDriver(String traceFile_) {
	}

}
