package edu.ist.symber.monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
//import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.ist.symber.Parameters;
import edu.ist.symber.Util;
import edu.ist.symber.common.Event;
import edu.ist.symber.common.EventType;
import edu.ist.symber.common.Pair;
import edu.ist.symber.generator.CrashTestCaseGenerator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import benchmark.BankBenchmark;

//** RECORDER
public class Monitor {
	public static boolean isCrashed = false;
	public static boolean STRIDEMODE = false;
	public static Throwable crashedException = null;
	public static String methodname;
	public static String[] mainargs;
	public static Pair<Integer, String> result;
	private static String mainthreadname;

	// ** data structures for thread consistent identification
	public volatile static HashMap<String, Integer> threadChildrenCounter; // **
	// allows to generate deterministic thread identifiers by
	// counting the number of children threads spawned

	// ** data structures for maintaining runtime state
	public static Map<Thread, String> MapBackupThreadName; // ** used to keep
	// the thread name consistent during the execution
	// (because the name can be reset by the target program after the thread
	// initialization)
	public static Map<Integer, Integer> objVersions; // ** map: field id ->
	// version (i.e. write version for shared variables and sync version for
	// monitors)

	// ** data structures for tracing log file events
	public static Map<String, LinkedList<Event>> log;
	public static Map<String, Integer> localCounters;

	// constants
	public static double TIME_OF_SAVEMONITORDATA;
	final static String LOGS_DIRECTORY = "logs";
	final static String LOG_FILE_NAME = "log";
	final static String CONFLICT_LOG_FILE_NAME = "conflict-log";
	final static String FILE_EXTENSION_JSON = ".json";
	final static String FILE_EXTENSION_TXT = ".txt";
	PrintWriter logger;

	public static Map<Integer, Lock> locks;

	public static void initialize() {
		threadChildrenCounter = new HashMap<String, Integer>();
		MapBackupThreadName = new HashMap<Thread, String>();
		log = new HashMap<String, LinkedList<Event>>();
		localCounters = new HashMap<String, Integer>();
		objVersions = new HashMap<Integer, Integer>();
		locks = new HashMap<Integer, Lock>();
	}

	// ** for instance fields
	public static void beforeLoad(Object objId, int fieldId, String threadId) {
		beforeLoad(fieldId, threadId);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			Object value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			boolean value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			int value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			long value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			double value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			int[] value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			long[] value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			double[] value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			int[][] value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			long[][] value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			double[][] value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void beforeLoad(Object objId, int fieldId, String threadId,
			BankBenchmark value) {
		beforeLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId) {
		afterLoad(fieldId, threadId);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			Object value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			boolean value) {
		int v = value ? 1 : 0;
		afterLoad(fieldId, threadId, (Object) v);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			int value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			long value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			double value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			int[] value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			long[] value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			double[] value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			int[][] value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			long[][] value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			double[][] value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void afterLoad(Object objId, int fieldId, String threadId,
			BankBenchmark value) {
		afterLoad(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId) {
		beforeStore(fieldId, threadId);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			Object value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			boolean value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			int value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			long value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			double value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			int[] value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			long[] value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			double[] value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			int[][] value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			long[][] value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			double[][] value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId,
			BankBenchmark value) {
		beforeStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId) {
		afterStore(fieldId, threadId);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			Object value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			boolean value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			int value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			long value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			double value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			int[] value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			long[] value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			double[] value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			int[][] value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			long[][] value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			double[][] value) {
		afterStore(fieldId, threadId, value);
	}

	public static void afterStore(Object objId, int fieldId, String threadId,
			BankBenchmark value) {
		afterStore(fieldId, threadId, value);
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

	public static void beforeLoad(int fieldId, String threadId, Object value) {
	}

	public static void beforeLoad(int fieldId, String threadId, boolean value) {
	}

	public static void beforeLoad(int fieldId, String threadId, int value) {
	}

	public static void beforeLoad(int fieldId, String threadId, long value) {
	}

	public static void beforeLoad(int fieldId, String threadId, double value) {
	}

	public static void beforeLoad(int fieldId, String threadId, int[] value) {
	}

	public static void beforeLoad(int fieldId, String threadId, long[] value) {
	}

	public static void beforeLoad(int fieldId, String threadId, double[] value) {
	}

	public static void beforeLoad(int fieldId, String threadId, int[][] value) {
	}

	public static void beforeLoad(int fieldId, String threadId, long[][] value) {
	}

	public static void beforeLoad(int fieldId, String threadId, double[][] value) {
	}

	public static void beforeLoad(int fieldId, String threadId,
			BankBenchmark value) {
	}

	/**
	 * Recording load (read) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 * @param value
	 */
	public static void afterLoad(int fieldId, String threadId) {
	}

	public static void afterLoad(int fieldId, String threadId, Object value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, boolean value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}

		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, int value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}

		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, long value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, double value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, int[] value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, long[] value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, double[] value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, int[][] value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, long[][] value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId, double[][] value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterLoad(int fieldId, String threadId,
			BankBenchmark value) {
		int version = 0;
		try {
			version = objVersions.get(fieldId);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.READ, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);

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
	public static void beforeStore(int fieldId, String threadId) {
	}

	public static void beforeStore(int fieldId, String threadId, Object value) {
		/*
		 * STRIDE { if (locks.containsKey(Integer.valueOf(fieldId))) {
		 * locks.get(Integer.valueOf(fieldId)).lock(); } else {
		 * locks.put(Integer.valueOf(fieldId), new ReentrantLock());
		 * locks.get(Integer.valueOf(fieldId)).lock(); } } /*
		 */
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, boolean value) {
		/*
		 * STRIDE { if (locks.containsKey(Integer.valueOf(fieldId))) {
		 * locks.get(Integer.valueOf(fieldId)).lock(); } else {
		 * locks.put(Integer.valueOf(fieldId), new ReentrantLock());
		 * locks.get(Integer.valueOf(fieldId)).lock(); } } /*
		 */
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, int value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, long value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, double value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, int[] value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, long[] value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, double[] value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, int[][] value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId, long[][] value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId,
			double[][] value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void beforeStore(int fieldId, String threadId,
			BankBenchmark value) {
		if (Monitor.STRIDEMODE){
			if (locks.containsKey(Integer.valueOf(fieldId))) {
				locks.get(Integer.valueOf(fieldId)).lock(); 
			} else {
				locks.put(Integer.valueOf(fieldId), new ReentrantLock());
				locks.get(Integer.valueOf(fieldId)).lock(); 
				} 
		} 
		int version = 0;
		try {
			version = objVersions.get(fieldId);
			version++;
			objVersions.put(fieldId, version);
		} catch (NullPointerException e) {
			objVersions.put(fieldId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.WRITE, fieldId, version, value));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void afterStore(int fieldId, String threadId) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, Object value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, boolean value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, int value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, long value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, double value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, int[] value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, long[] value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, double[] value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, int[][] value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, long[][] value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId, double[][] value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
	}

	public static void afterStore(int fieldId, String threadId,
			BankBenchmark value) {
		if (Monitor.STRIDEMODE){
			locks.get(Integer.valueOf(fieldId)).unlock();
		} 
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
		if(threadId.contains("main")){
			threadId = "0";
		}
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

		if (threadId.contains("main")) {
			threadId = "0";
		}

		monitorId += Parameters.numShared;
		int version = 0;
		try {
			version = objVersions.get(monitorId);
			version++;
			objVersions.put(monitorId, version);
		} catch (NullPointerException e) {
			objVersions.put(monitorId, version);
		}
		try {
			String thread = Thread.currentThread().getName();
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.LOCK, monitorId));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
    }

	public static void exitMonitor(Object o, int monitorId, String threadId,
			String monitorName) {
		try {
			String thread = Thread.currentThread().getName();
			int version = objVersions.get(monitorId);
			log.get(thread).add(
					new Event(thread, localCounters.get(thread),
							EventType.UNLOCK, monitorId));
			localCounters.put(thread, localCounters.get(thread) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
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
			log.put(mainthreadname, new LinkedList<Event>());
			localCounters.put(mainthreadname, new Integer(0));

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
	/*
	 * public static void threadStartRun(String threadId) { try {
	 * threadChildrenCounter.put(threadId, 1); log.put(threadId, new
	 * LinkedList<Event>()); localCounters.put(threadId, new Integer(0)); }
	 * catch (Exception e) { System.err.println(">> Monitor_ERROR: " +
	 * e.getMessage()); e.printStackTrace(); } }
	 */

	public static void threadStartRun(String threadId) {
		try {
			if (log.containsKey(threadId)) {
				return;
			}
			threadChildrenCounter.put(threadId, 1);
			log.put(threadId, new LinkedList<Event>());
			localCounters.put(threadId, new Integer(0));
			System.out.println("[OREO-Recorder] T" + threadId
					+ " started running");
			log.get(threadId).add(
					new Event(threadId, localCounters.get(threadId),
							EventType.START));
			localCounters.put(threadId, localCounters.get(threadId) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void threadExitRun(String threadId) {
		System.out.println("EXIT " + threadId);
		try {
			log.get(threadId).add(
					new Event(threadId, localCounters.get(threadId),
							EventType.EXIT));
			localCounters.put(threadId, localCounters.get(threadId) + 1);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
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
			log.get(parentId).add(
					new Event(parentId, localCounters.get(parentId),
							newThreadName, EventType.FORK));
			threadChildrenCounter.put(parentId, childCounter);
		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public synchronized static void joinRunThreadAfter(Thread t, String threadId) {
		try {
			if (threadId.contains("main"))
				threadId = "0";
			System.out.println("JOIN-" + t.getName() + "-" + threadId);
			log.get(threadId).add(
					new Event(threadId, localCounters.get(threadId), t
							.getName(), EventType.JOIN));
			localCounters.put(threadId, localCounters.get(threadId) + 1);

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public synchronized static void crashed(Throwable crashedException) {
		isCrashed = true;
		System.out.println("--- crashed! ---" + crashedException);
	}

	public static void saveMonitorData(String appname)
			throws FileNotFoundException {
		long end;

		String conflitsThreads = "";
		Integer conflictsRatio = 0;
		Map<String, HashSet<String>> conflictLog = new HashMap<String, HashSet<String>>();
		for (Entry<String, LinkedList<Event>> entry : log.entrySet()) {
			String key = entry.getKey();
			File file = new File(LOGS_DIRECTORY);
			file.mkdirs();
			PrintWriter printWriter = new PrintWriter(LOGS_DIRECTORY
					+ File.separator + LOG_FILE_NAME + key
					+ FILE_EXTENSION_JSON);
			JSONArray jsList = new JSONArray();
			ArrayList<String> list = new ArrayList<String>();
			JSONObject jsObj = new JSONObject();
			for (Event event : entry.getValue()) {
				// System.out.println(event);
				jsObj = new JSONObject();
				// jsObj.clear();
				jsObj.put("threadId", event.getThreadId());
				jsObj.put("eventId", String.valueOf(event.getEventId()));
				jsObj.put("eventType",
						String.valueOf(event.getEventType().toString()));
				if (!(event.getEventType().equals(EventType.START)
						|| event.getEventType().equals(EventType.EXIT))) {
					if (event.getEventType().equals(EventType.LOCK)
							|| event.getEventType().equals(EventType.UNLOCK)) {
						jsObj.put("monitorId",
								String.valueOf(event.getFieldId()));
					} else if (event.getEventType().equals(EventType.FORK)
							|| event.getEventType().equals(EventType.JOIN)) {
						jsObj.put("value", String.valueOf(event.getValue()));
					} else {
						jsObj.put("version", String.valueOf(event.getVersion()));
						jsObj.put("fieldId", String.valueOf(event.getFieldId()));
						jsObj.put("value", String.valueOf(event.getValue()));
					}
				}
				jsList.add(jsObj);
				list.add(String.valueOf(jsObj.get("eventId")));
				// conflict list logic
				if (event.getEventType().equals(EventType.WRITE)) {
					String fieldVersionKey = event.getFieldId() + ":"
							+ event.getVersion();
					if (!conflictLog.containsKey(fieldVersionKey)) {
						HashSet<String> l = new HashSet<String>();
						l.add(key);
						conflictLog.put(fieldVersionKey, l);
					} else {
						conflictLog.get(fieldVersionKey).add(key);
					}
				}

				// conflict list logic
				if(event.getEventType().equals(EventType.WRITE))
				{
					String fieldVersionKey = event.getFieldId() + ":"
							+ event.getVersion();
					if (!conflictLog.containsKey(fieldVersionKey)) {
						HashSet<String> l = new HashSet<String>();
						l.add(key);
						conflictLog.put(fieldVersionKey, l);
					} else {
						conflictLog.get(fieldVersionKey).add(key);
					}
				}
			}
			//System.out.println(jsList);
			try {
				jsList.writeJSONString(printWriter);
			} catch (IOException e) {
				e.printStackTrace();
			}
			printWriter.close();
		}

		File file = new File(LOGS_DIRECTORY);
		StringBuilder sb = new StringBuilder();
		file.mkdirs();
		PrintWriter printWriter = new PrintWriter(LOGS_DIRECTORY
				+ File.separator + CONFLICT_LOG_FILE_NAME + FILE_EXTENSION_JSON);
		for (Entry<String, HashSet<String>> entry : conflictLog.entrySet()) {
			HashSet<String> value = entry.getValue();
			if (value.size() > 1) {
				conflictsRatio++;
				sb.append("\nField (id:version) " + entry.getKey()
						+ ", threads conflicting: ");
				for (String s : value) {
					conflitsThreads += s + " ";
					sb.append(s + " ");
				}
				conflitsThreads += ";";
			}

		}
		printWriter.println(sb.toString());
		printWriter.close();
		result = new Pair<Integer, String>(conflictsRatio, conflitsThreads);

		try {
			Writer writer = new BufferedWriter(new FileWriter("conflicts.txt",
					true));
			writer.append(result.getFirst() + "\t" + result.getSecond());
			if (Monitor.STRIDEMODE) writer.append("-stride");
			writer.append("\r\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		end = System.nanoTime(); // ** end timestamp
		double time = (((double) (end - TIME_OF_SAVEMONITORDATA) / 1000000000));
		System.out.println("Time of recorder with logging time included:"
				+ time);
		System.out.println(sb.toString());
		Writer writer;
		try {
			writer = new BufferedWriter(new FileWriter(
					"recorder-with-logtime.txt", true));
			writer.append(String.valueOf(time));// + "\t");
			if (Monitor.STRIDEMODE) writer.append("-stride");
			writer.append("\r\n");
			writer.close();// */
			writer = new BufferedWriter(new FileWriter("log-size.txt", true));
			writer.append(String.valueOf(folderSize(file)));
			if (Monitor.STRIDEMODE) writer.append("-stride");
			writer.append("\r\n");
			writer.close();
			System.out.println("\nLOG SIZE: " + folderSize(file) + " bytes");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("\nRECORDER TIME: " + time + "s");
	}

	public static long folderSize(File directory) {
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}

	public static void generateTestDriver(String traceFile_) {
		// GENERATE Test Driver
		try {
			CrashTestCaseGenerator.main(new String[] { traceFile_,
					Util.getTmpReplayDirectory() });
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
