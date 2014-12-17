package edu.ist.symber.monitor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	// ** data structures for maintaining runtime state
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
	public static Map<Integer, Integer> objVersions; // ** map: field id ->
														// version (i.e. write
														// version for shared
														// variables and sync
														// version for monitors)

	// ** data structures for tracing log file events
	// public static Map<String,LinkedList<Pair<Integer,Integer>>> readLog;
	// public static Map<String,LinkedList<Pair<Integer,Integer>>> writeLog;
	public static Map<String, LinkedList<Pair<Integer, Integer>>> lockLog;
	public static Map<String, LinkedList<Event>> readLog;
	public static Map<String, LinkedList<Event>> writeLog;
	// public static Map<String, LinkedList<Event>> lockLog;

	public static Map<Integer, Lock> locks;

	public static void initialize() {
		threadChildrenCounter = new HashMap<String, Integer>();
		MapBackupThreadName = new HashMap<Thread, String>();
		readLog = new HashMap<String, LinkedList<Event>>();
		writeLog = new HashMap<String, LinkedList<Event>>();
		lockLog = new HashMap<String, LinkedList<Pair<Integer, Integer>>>();
		objVersions = new HashMap<Integer, Integer>();
		locks = new HashMap<Integer, Lock>();
	}

	// ** for instance fields
	public static void beforeLoad(Object fieldId, String threadId) {
		beforeLoad(System.identityHashCode(fieldId), threadId);
	}

	public static void beforeLoad(Object fieldId, String threadId, int value) {
		beforeLoad(System.identityHashCode(fieldId), threadId, value);
	}

	public static void afterLoad(Object fieldId, String threadId, int value) {
		afterLoad(System.identityHashCode(fieldId), threadId, value);
	}

	public static void beforeLoad(Object fieldId, String threadId, boolean value) {
		beforeLoad(System.identityHashCode(fieldId), threadId, value);
	}

	public static void afterLoad(Object fieldId, String threadId, boolean value) {
		afterLoad(System.identityHashCode(fieldId), threadId, value);
	}

	public static void beforeLoad(Object fieldId, String threadId, Object value) {
		beforeLoad(System.identityHashCode(fieldId), threadId, value);
	}

	public static void afterLoad(Object fieldId, String threadId, Object value) {
		afterLoad(System.identityHashCode(fieldId), threadId, value);
	}

	public static void beforeStore(Object fieldId, String threadId, int value) {
		beforeStore(System.identityHashCode(fieldId), threadId, value);
	}

	public static void afterStore(Object fieldId, String threadId, int value) {
		afterStore(System.identityHashCode(fieldId), threadId, value);
	}

	public static void beforeStore(Object fieldId, String threadId,
			boolean value) {
		beforeStore(System.identityHashCode(fieldId), threadId, value);
	}

	public static void afterStore(Object fieldId, String threadId, boolean value) {
		afterStore(System.identityHashCode(fieldId), threadId, value);
	}

	public static void beforeStore(Object fieldId, String threadId, Object value) {
		beforeStore(System.identityHashCode(fieldId), threadId, value);
	}

	public static void afterStore(Object fieldId, String threadId, Object value) {
		afterStore(System.identityHashCode(fieldId), threadId, value);
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
	 * @param value
	 */
	public static void beforeLoad(int fieldId, String threadId) {
	}

	public static void beforeLoad(int fieldId, String threadId, int value) {
	}

	public static void beforeLoad(int fieldId, String threadId, boolean value) {
	}

	public static void beforeLoad(int fieldId, String threadId, Object value) {
	}

	/**
	 * Recording load (read) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 * @param value
	 */
	public static void afterLoad(int fieldId, String threadId, int value) {
		try {
			int version = 0;
			if (!objVersions.containsKey(fieldId)) {
				objVersions.put(fieldId, version);
			} else {
				version = objVersions.get(fieldId);
			}
			readLog.get(Thread.currentThread().getName()).add(
					new Event(fieldId, version, value));
			System.out.println("[" + threadId + "] Read INT with value:"
					+ value + " (version W" + version + ")");
			// System.out.println("["+threadId+"] "+fieldId+"-R"+version);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, boolean value) {
		try {
			int version = 0;
			if (!objVersions.containsKey(fieldId)) {
				objVersions.put(fieldId, version);
			} else {
				version = objVersions.get(fieldId);
			}
			readLog.get(Thread.currentThread().getName()).add(
					new Event(fieldId, version, value));
			// readLog.get(Thread.currentThread().getName()).add(new
			// Pair<Integer, Integer>(fieldId,version));
			System.out.println("[" + threadId + "] Read BOOLEAN with value:"
					+ value + " (version W" + version + ")");
			// System.out.println("["+threadId+"] "+fieldId+"-R"+version);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, Object value) {
		try {
			int version = 0;
			if (!objVersions.containsKey(fieldId)) {
				objVersions.put(fieldId, version);
			} else {
				version = objVersions.get(fieldId);
			}
			readLog.get(Thread.currentThread().getName()).add(
					new Event(fieldId, version, value));
			System.out.println("[" + threadId + "] Read OBJECT with value:"
					+ value + " (version W" + version + ")");
			// System.out.println("["+threadId+"] "+fieldId+"-R"+version);

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
	 * @param value
	 */
	public static void beforeStore(int fieldId, String threadId, int value) {
		if (locks.containsKey(Integer.valueOf(fieldId))) {
			locks.get(Integer.valueOf(fieldId)).lock();
		} else {
			locks.put(Integer.valueOf(fieldId), new ReentrantLock());
			locks.get(Integer.valueOf(fieldId)).lock();
		}
		try {
			int version = 0;
			if (!objVersions.containsKey(fieldId)) {
				objVersions.put(fieldId, version);
			} else {
				version = objVersions.get(fieldId);
				version++;
				objVersions.put(fieldId, version);
			}
			writeLog.get(Thread.currentThread().getName()).add(
					new Event(fieldId, version, value));
			// writeLog.get(Thread.currentThread().getName()).add(new
			// Pair<Integer, Integer>(fieldId,version));
			System.out
					.println("[" + threadId + "] " + fieldId + "-W" + version);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, boolean value) {
		if (locks.containsKey(Integer.valueOf(fieldId))) {
			locks.get(Integer.valueOf(fieldId)).lock();
		} else {
			locks.put(Integer.valueOf(fieldId), new ReentrantLock());
			locks.get(Integer.valueOf(fieldId)).lock();
		}
		try {
			int version = 0;
			if (!objVersions.containsKey(fieldId)) {
				objVersions.put(fieldId, version);
			} else {
				version = objVersions.get(fieldId);
				version++;
				objVersions.put(fieldId, version);
			}
			writeLog.get(Thread.currentThread().getName()).add(
					new Event(fieldId, version, value));
			// writeLog.get(Thread.currentThread().getName()).add(new
			// Pair<Integer, Integer>(fieldId,version));
			System.out
					.println("[" + threadId + "] " + fieldId + "-W" + version);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, Object value) {
		if (locks.containsKey(Integer.valueOf(fieldId))) {
			locks.get(Integer.valueOf(fieldId)).lock();
		} else {
			locks.put(Integer.valueOf(fieldId), new ReentrantLock());
			locks.get(Integer.valueOf(fieldId)).lock();
		}
		try {
			int version = 0;
			if (!objVersions.containsKey(fieldId)) {
				objVersions.put(fieldId, version);
			} else {
				version = objVersions.get(fieldId);
				version++;
				objVersions.put(fieldId, version);
			}
			writeLog.get(Thread.currentThread().getName()).add(
					new Event(fieldId, version, value));
			// writeLog.get(Thread.currentThread().getName()).add(new
			// Pair<Integer, Integer>(fieldId,version));
			System.out
					.println("[" + threadId + "] " + fieldId + "-W" + version);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterStore(int fieldId, String threadId, int value) {
		locks.get(Integer.valueOf(fieldId)).unlock();
	}

	public static void afterStore(int fieldId, String threadId, boolean value) {
		locks.get(Integer.valueOf(fieldId)).unlock();
	}

	public static void afterStore(int fieldId, String threadId, Object value) {
		locks.get(Integer.valueOf(fieldId)).unlock();
	}

	public static void afterStore(int fieldId, String threadId) {
		locks.get(Integer.valueOf(fieldId)).unlock();
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
		try {

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
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
		try {

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
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
		try {
			int version = 0;
			if (!objVersions.containsKey(monitorId)) {
				objVersions.put(monitorId, version);
			} else {
				version = objVersions.get(monitorId);
				version++;
				objVersions.put(monitorId, version);
			}
			lockLog.get(threadId).add(
					new Pair<Integer, Integer>(monitorId, version));
			System.out.println("[" + threadId + "] " + monitorId + "-L"
					+ version);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
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
			readLog.put(mainthreadname, new LinkedList<Event>());
			writeLog.put(mainthreadname, new LinkedList<Event>());
			lockLog.put(mainthreadname,
					new LinkedList<Pair<Integer, Integer>>());

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
			readLog.put(threadId, new LinkedList<Event>());
			writeLog.put(threadId, new LinkedList<Event>());
			lockLog.put(threadId, new LinkedList<Pair<Integer, Integer>>());

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
		Map<String, LinkedList<String>> conflictLog = new HashMap<String, LinkedList<String>>();
		// Map<String, Integer> conflictLog = new HashMap<String, Integer>();

		for (Entry<String, LinkedList<Event>> entry : writeLog.entrySet()) {
			String key = entry.getKey();
			for (Event event : entry.getValue()) {
				String fieldVersionKey = event.getFieldId() + ":"
						+ event.getVersion();
				Integer counter = 1;
				if (!conflictLog.containsKey(fieldVersionKey)) {
					LinkedList<String> l = new LinkedList<String>();
					l.add(key);
					conflictLog.put(fieldVersionKey, l);
				} else {
					conflictLog.get(fieldVersionKey).add(key);
					// System.out.println("Conflict found for field " +
					// pair.getFirst() + ", conflicting version " +
					// pair.getSecond());
					// counter++;
					// conflictLog.put(fieldVersionKey, counter);
				}
			}
		}

		for (Entry<String, LinkedList<String>> entry : conflictLog.entrySet()) {
			LinkedList<String> value = entry.getValue();
			if (value.size() > 1) {
				StringBuilder sb = new StringBuilder();
				sb.append("Field id " + entry.getKey()
						+ ", threads conflicting: ");
				for (String s : value) {
					sb.append(s + " ");
				}
				System.out.println(sb.toString());
			}

		}

		// ** save Runtime Information
		/*
		 * try { traceFile_monitordata = File.createTempFile(("Symber"),
		 * ("_"+appname+"_trace.gz"), new File(Util.getReplayTmpDirectory()));
		 * 
		 * String traceFileName = traceFile_monitordata.getAbsolutePath(); int
		 * index = traceFileName.indexOf("_trace"); traceFile_ =
		 * traceFileName.substring(0, index);
		 * 
		 * assert (traceFile_monitordata != null);
		 * 
		 * fw_monitordata = new ObjectOutputStream(new GZIPOutputStream(new
		 * FileOutputStream(traceFile_monitordata)));
		 * 
		 * } catch (IOException e) { e.printStackTrace(); }
		 */
		return traceFile_;
	}

	public static void generateTestDriver(String traceFile_) {
		// GENERATE Test Driver
		try {
			CrashTestCaseGenerator.main(new String[] { traceFile_,
					Util.getTmpReplayDirectory() });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
