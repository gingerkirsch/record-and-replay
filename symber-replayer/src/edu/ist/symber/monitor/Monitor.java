package edu.ist.symber.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import benchmark.BankBenchmark;
import edu.ist.symber.Parameters;
import edu.ist.symber.Util;
import edu.ist.symber.common.Event;

//** REPLAYER
public class Monitor {
	private static boolean isDebug = true;
	private static String mainthreadname;
	public static String methodname;
	public static String[] mainargs;
	public static final String INPUT_DIR = "D:\\record-and-replay\\symber-replayer\\logs";

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

	// ** data structures to enforce replay
	public static Map<Integer, ReentrantLock> lockVars; // ** shared/sync var id
														// -> var Lock, contains
														// a lock per
														// shared/sync variable
														// to allow fine grained
														// synchronization in
														// the replay
	public static Map<Integer, Condition> condVars; // ** shared/sync var id ->
													// var Condition, contains a
													// condition per shared/sync
													// variable (associated to
													// the respective lock) to
													// allow fine grained
													// synchronization in the
													// replay
	public static Vector<ReplayEvent>[] accessVector; // ** maps SPE id ->
														// access vector, i.e.
														// for each shared
														// program element S
														// maps a vector
														// containing the local
														// order of thread
														// accesses to S

	// ** stuff to handle possible issues in the replay execution
	public static Map<String, Condition> MaplastSignal = new HashMap<String, Condition>(); // **
																							// map
																							// threadId
																							// ->
																							// condition
																							// object
																							// ;
																							// used
																							// to
																							// avoid
																							// 'lost'
																							// signals;
																							// a
																							// deadlock
																							// can
																							// happen
																							// when
																							// a
																							// thread
																							// calling
																							// cond.signal()
																							// wakes
																							// another
																							// thread
																							// that
																							// isn't
																							// allowed
																							// to
																							// proceed
																							// as
																							// it
																							// isn't
																							// the
																							// next
																							// thread
																							// allowed,
																							// which
																							// makes
																							// the
																							// latter
																							// thread
																							// to
																							// 'await'
																							// again
																							// in
																							// the
																							// condition,
																							// thus
																							// losing
																							// the
																							// correct
																							// signal
	public static int noMoreConstThreads = 0; // ** number of threads that have
												// no more constraints and,
												// therefore, should stop
												// running
	public static int threadsFinished = 0; // ** number of threads that
											// successfully finished its
											// execution

	public static void initialize() {
		try {
			loadLogFile();
			threadChildrenCounter = new HashMap<String, Integer>();
			MapBackupThreadName = new HashMap<Thread, String>();

			// ** initialize the lock for each shared variable (this allows to
			// have fine grained locking instead of just synchronizing accesses
			// to accessVector)
			lockVars = new HashMap<Integer, ReentrantLock>();
			condVars = new HashMap<Integer, Condition>();
			for (int i = 0; i < Parameters.numShared; i++) {
				ReentrantLock l = new ReentrantLock();
				Condition c = l.newCondition();
				lockVars.put(i, l);
				condVars.put(i, c);
			}

			for (int i = 0; i < Parameters.numSync; i++) {
				ReentrantLock l = new ReentrantLock();
				lockVars.put(i + Parameters.numShared, l);
				condVars.put(i + Parameters.numShared, l.newCondition());
			}

			if (Parameters.isDebug) {
				for (int i = 0; i < accessVector.length; i++) {
					if (!accessVector[i].isEmpty()) {
						System.err.print("SPE " + i + ": [");
						for (int j = 0; j < accessVector[i].size() - 1; j++) {
							System.err.print(accessVector[i].get(j).toString()
									+ ", ");
						}
						System.err.println(accessVector[i].get(
								accessVector[i].size() - 1).toString()
								+ "]");
					}
				}
			}

		} catch (Exception e) {
			System.err.println(">> Monitor_ERROR: " + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Load log file containing the events recorded at runtime.
	 */
	public static void loadLogFile() {
		ObjectInputStream in = null;
		try {
			//File traceFile = new File(Parameters.logpath);
			
			System.out.println(INPUT_DIR + File.separator + Parameters.logpath);
			if (Parameters.logpath.endsWith(".gz")) {
				in = new ObjectInputStream(new GZIPInputStream(
						new FileInputStream(INPUT_DIR + File.separator + Parameters.logpath)));
				// ** initialize accessVector
				accessVector = (Vector<ReplayEvent>[]) Util.loadObject(in);
			} else if (Parameters.logpath.endsWith(".json")) {
				FileReader reader = new FileReader(INPUT_DIR + File.separator + Parameters.logpath);
				JSONParser jsonParser = new JSONParser();
				JSONArray log = (JSONArray) jsonParser.parse(reader);
				accessVector = new Vector[log.size()];
				//System.out.println("accessVector size " +accessVector.length);
				// ** initialize accessVector
				Iterator i = log.iterator();
				while (i.hasNext()) {
					JSONObject innerObj = (JSONObject) i.next();
					Integer fieldId = Integer.valueOf((String) innerObj
							.get("fieldId"));
					JSONArray events = (JSONArray) innerObj.get("events");
					//changed : accessVector[fieldId]
					accessVector[fieldId] = new Vector<ReplayEvent>(events.size());
					
					for (int j = 0; j < events.size(); j++) {
						//changed : accessVector[fieldId]
						accessVector[fieldId].add(new ReplayEvent(
								(String) events.get(j)));
					}
				}
			} else {
				in = new ObjectInputStream(new FileInputStream(
						Parameters.logpath));
				// ** initialize accessVector
				accessVector = (Vector<ReplayEvent>[]) Util.loadObject(in);
			}
		} catch (IOException e) {
			System.err.println("[OREO-Replayer] Error loading log file "
					+ Parameters.logpath + ": " + e.getMessage());
			System.exit(0);
		} catch (ParseException e) {
			System.err.println("[OREO-Replayer] Error parsing log file "
					+ Parameters.logpath + ": " + e.getMessage());
			System.exit(0);
		} catch (NullPointerException e) {
			System.err.println("[OREO-Replayer] Null pointer exception for "
					+ Parameters.logpath + ": " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	// ** for instance fields
	public static void beforeLoad(Object objId, int fieldId, String threadId){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, Object value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, boolean value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, int value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, long value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, double value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, int[] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, long[] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, double[] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, int[][] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, long[][] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, double[][] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(Object objId, int fieldId, String threadId, BankBenchmark value){
		beforeLoad(fieldId, threadId);
	}
	
	public static void afterLoad(Object objId, int fieldId, String threadId){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, Object value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, boolean value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, int value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, long value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, double value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, int[] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, long[] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, double[] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, int[][] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, long[][] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, double[][] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(Object objId, int fieldId, String threadId, BankBenchmark value){
		afterLoad(fieldId, threadId);
	}

	public static void beforeStore(Object objId, int fieldId, String threadId){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, Object value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, boolean value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, int value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, long value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, double value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, int[] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, long[] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, double[] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, int[][] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, long[][] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, double[][] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(Object objId, int fieldId, String threadId, BankBenchmark value){
		beforeStore(fieldId, threadId);
	}

	public static void afterStore(Object objId, int fieldId, String threadId){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, Object value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, boolean value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, int value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, long value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, double value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, int[] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, long[] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, double[] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, int[][] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, long[][] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, double[][] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(Object objId, int fieldId, String threadId, BankBenchmark value){
		afterStore(fieldId, threadId);
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

		ReentrantLock fieldLock = lockVars.get(fieldId);
		fieldLock.lock();

		try {
			String currentThread = threadId; // ** current thread trying to
												// execute the shared operation
			if(currentThread.contains("main")) 
				currentThread = "0";
			String nextThread = accessVector[fieldId].get(0).getThreadId(); // **
																			// next
																			// allowed
																			// thread
																			// according
																			// to
																			// the
																			// recorded
																			// interleaving
			while (!currentThread.equals(nextThread)) {
				if (Parameters.isDebug)
					System.out.println("[OREO-Replayer] beforeLoad:\tT"
							+ currentThread + " R(" + fieldId
							+ ") -> WAIT\t\t\t[" + currentThread
							+ " (currentThread) != " + nextThread
							+ " (nextThread)]");

				try {
					condVars.get(fieldId).await();
				} catch (InterruptedException e) {
				}

				nextThread = accessVector[fieldId].get(0).getThreadId(); // **
																			// update
																			// nextThread
																			// in
																			// case
																			// it
																			// is
																			// stale
			}
			if (Parameters.isDebug)
				System.out.println("[OREO-Replayer] beforeLoad:\tT"
						+ currentThread + " R(" + fieldId + ") -> OK\t\t\t["
						+ currentThread + " (currentThread) == " + nextThread
						+ " (nextThread)]");
		} catch (ArrayIndexOutOfBoundsException e) // ** if the thread has no
													// more constraints to go,
													// it should stop running...
		{
			System.err.println("[OREO-Replayer MONITOR 475] ERROR: T"
					+ Thread.currentThread().getName()
					+ ": access vector of SPE " + fieldId + " is empty!");
			noMoreConstThreads++;
			while (noMoreConstThreads > 0) {
				if (noMoreConstThreads == (MapBackupThreadName.keySet().size() - 1)) // **
																						// all
																						// threads
																						// should
																						// have
																						// stopped
																						// so
																						// we
																						// can
																						// exit
																						// the
																						// execution
					System.exit(-1);
				try {
					condVars.get(fieldId).await();
				} catch (InterruptedException e1) {
				}
			}
			fieldLock.unlock();
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR [T" + threadId + " -> "
					+ fieldId + "]: " + e.getMessage());
			e.printStackTrace();
		}
	}
	public static void beforeLoad(int fieldId, String threadId, Object value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, boolean value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, int value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, long value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, double value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, int[] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, long[] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, double[] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, int[][] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, long[][] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, double[][] value){
		beforeLoad(fieldId, threadId);
	}
	public static void beforeLoad(int fieldId, String threadId, BankBenchmark value){
		beforeLoad(fieldId, threadId);
	}


	/**
	 * Replaying load (read) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 * @param value
	 */
	public static void afterLoad(int fieldId, String threadId) {
		try {
			ReentrantLock fieldLock = lockVars.get(fieldId);
			accessVector[fieldId].remove(0);
			condVars.get(fieldId).signalAll();
			fieldLock.unlock();
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR[ T"
					+ Thread.currentThread().getName() + " -> " + fieldId
					+ "]: " + e.getMessage());
			e.printStackTrace();
		}
	}
	public static void afterLoad(int fieldId, String threadId, Object value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, boolean value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, int value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, long value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, double value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, int[] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, long[] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, double[] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, int[][] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, long[][] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, double[][] value){
		afterLoad(fieldId, threadId);
	}
	public static void afterLoad(int fieldId, String threadId, BankBenchmark value){
		afterLoad(fieldId, threadId);
	}
	
	/**
	 * Recording store (writes) memory access operations
	 * 
	 * @param fieldId
	 * @param threadId
	 * @param value
	 */
	public static void beforeStore(int fieldId, String threadId) {

		ReentrantLock fieldLock = lockVars.get(fieldId);
		fieldLock.lock();
		try {
			String currentThread = threadId; // ** current thread trying to
												// execute the shared operation
			if(currentThread.contains("main")) 
				currentThread = "0";
			String nextThread = accessVector[fieldId].get(0).getThreadId(); // **
																			// next
																			// allowed
																			// thread
																			// according
																			// to
																			// the
																			// recorded
																			// interleaving
			while (!currentThread.equals(nextThread)) {
				if (Parameters.isDebug)
					System.out.println("[OREO-Replayer] beforeStore:\tT"
							+ currentThread + " W(" + fieldId
							+ ") -> WAIT\t\t\t[" + currentThread
							+ " (currentThread) != " + nextThread
							+ " (nextThread)]");

				try {
					condVars.get(fieldId).await();
					nextThread = accessVector[fieldId].get(0).getThreadId(); // **
																				// update
																				// nextThread
																				// in
																				// case
																				// it
																				// is
																				// stale
				} catch (InterruptedException e) {
				}
			}
			if (Parameters.isDebug)
				System.out.println("[OREO-Replayer] beforeStore:\tT"
						+ currentThread + " W(" + fieldId + ") -> OK\t\t\t["
						+ currentThread + " (currentThread) == " + nextThread
						+ " (nextThread)]");

		} catch (ArrayIndexOutOfBoundsException e) // ** if the thread has no
													// more constraints, it
													// should stop running...
		{
			System.err.println("[OREO-Replayer MONITOR 656] ERROR: T" + threadId
					+ ": access vector of SPE " + fieldId + " is empty!");
			noMoreConstThreads++;
			while (noMoreConstThreads > 0) {
				if (noMoreConstThreads == (MapBackupThreadName.keySet().size() - 1)) // **
																						// all
																						// threads
																						// should
																						// have
																						// stopped
																						// so
																						// we
																						// can
																						// exit
																						// the
																						// execution
					System.exit(-1);
				try {
					condVars.get(fieldId).await();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			fieldLock.unlock();
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR [T" + threadId + " -> "
					+ fieldId + "]: " + e.getMessage());
			// Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}
	public static void beforeStore(int fieldId, String threadId, Object value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, boolean value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, int value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, long value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, double value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, int[] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, long[] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, double[] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, int[][] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, long[][] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, double[][] value){
		beforeStore(fieldId, threadId);
	}
	public static void beforeStore(int fieldId, String threadId, BankBenchmark value){
		beforeStore(fieldId, threadId);
	}
	
	public static void afterStore(int fieldId, String threadId) {
		try {
			ReentrantLock fieldLock = lockVars.get(fieldId);
			accessVector[fieldId].remove(0);
			condVars.get(fieldId).signalAll();
			fieldLock.unlock();
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR [ T"
					+ Thread.currentThread().getName() + " -> " + fieldId
					+ "]: " + e.getMessage());
			e.printStackTrace();
		}
	}
	public static void afterStore(int fieldId, String threadId, Object value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, boolean value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, int value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, long value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, double value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, int[] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, long[] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, double[] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, int[][] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, long[][] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, double[][] value){
		afterStore(fieldId, threadId);
	}
	public static void afterStore(int fieldId, String threadId, BankBenchmark value){
		afterStore(fieldId, threadId);
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

		monitorId += Parameters.numShared; // ** update monitor Id to point to
											// the correct position in
											// accessVector data structure

		try {
			ReentrantLock monitorLock = lockVars.get(monitorId);
			monitorLock.lock();
			String currentThread = Thread.currentThread().getName(); // **
																		// current
																		// thread
																		// trying
																		// to
																		// execute
																		// the
																		// shared
																		// operation
			///!!!!!!!!!!!!!!!check the index
			String nextThread = accessVector[monitorId].get(0).getThreadId(); // **
																				// next
																				// allowed
																				// thread
																				// according
																				// to
																				// the
																				// recorded
																				// interleaving

			if (monitorName.equals("AWAIT")) {
				if (currentThread.equals(nextThread)) // ** since we remove the
														// await call, we must
														// ensure that it is
														// still called even
														// when the thread is
														// the next one to
														// execute
				{
					if (Parameters.isDebug)
						System.out.println("[OREO-Replayer] beforeCond:\tT"
								+ currentThread + " (" + monitorId + "_"
								+ cond.hashCode() + ").AWAIT -> OK\t["
								+ currentThread + " (currentThread) == "
								+ nextThread + " (nextThread)])");
					monitorLock.unlock(); // ** to avoid deadlocks
					((Condition) cond).await();
					monitorLock.lock();
				} else
					while (!currentThread.equals(nextThread)) // ** wait until
																// its turn
					{
						if (Parameters.isDebug)
							System.out.println("[OREO-Replayer] beforeCond:\tT"
									+ currentThread + " (" + monitorId + "_"
									+ cond.hashCode() + ").AWAIT -> WAIT\t["
									+ currentThread + " (currentThread) != "
									+ nextThread + " (nextThread)])");

						// ** since we delete the original await call, we
						// simulate it by using the Condition in
						// condVars.get(monitorId) as synchronization point
						((ReentrantLock) lock).unlock(); // ** Nuno: is this
															// safe?
						condVars.get(monitorId).await();
						monitorLock.unlock(); // ** unlock fieldLock to get it
												// only after acquiring the obj
												// lock
						((ReentrantLock) lock).lock();
						monitorLock.lock();
						nextThread = accessVector[monitorId].get(0)
								.getThreadId(); // ** update nextThread in case
												// it is stale
					}

			} else // ** monitorName == "SIGNAL"
			{
				if (currentThread.equals(nextThread)) {
					if (Parameters.isDebug)
						System.out.println("[OREO-Replayer] beforeCond:\tT"
								+ currentThread + " (" + monitorId + "_"
								+ cond.hashCode() + ").SIGNAL -> OK\t["
								+ currentThread + " (currentThread) == "
								+ nextThread + " (nextThread)])");

					((Condition) cond).signalAll(); // ** proceed with signal
				} else
					while (!currentThread.equals(nextThread)) // ** wait until
																// its turn
					{
						if (Parameters.isDebug)
							System.out.println("[OREO-Replayer] beforeCond:\tT"
									+ currentThread + " (" + monitorId + "_"
									+ cond.hashCode() + ").SIGNAL -> WAIT\t["
									+ currentThread + " (currentThread) != "
									+ nextThread + " (nextThread)])");

						condVars.get(monitorId).await();
						nextThread = accessVector[monitorId].get(0)
								.getThreadId(); // ** update nextThread in case
												// it is stale
					}
			}
			monitorLock.unlock();
		} catch (ArrayIndexOutOfBoundsException e) // ** if the thread has no
													// more constraints, it
													// should stop running...
		{
			System.err.println("[OREO-Replayer] ERROR: T"
					+ Thread.currentThread().getName()
					+ ": access vector of SPE MONITOR 906" + monitorId + " is empty!");
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR[T"
					+ Thread.currentThread().getName() + " -> " + monitorId
					+ "_" + System.identityHashCode(lock) + "]: "
					+ e.getMessage());
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
			monitorId += Parameters.numShared; // ** update monitor Id to point
												// to the correct position in
												// accessVector data structure
			ReentrantLock monitorLock = lockVars.get(monitorId);
			monitorLock.lock();
			accessVector[monitorId].remove(0);
			condVars.get(monitorId).signalAll();

			// ** since we remove the signal call, we must ensure that it is
			// still called
			if (Parameters.isDebug)
				System.out.println("[OREO-Replayer] afterCond:\tT" + threadId
						+ " (" + monitorId + "_"
						+ System.identityHashCode(cond) + ")." + monitorName
						+ " -> PROCEED");

			if (monitorName.equals("SIGNAL"))
				MaplastSignal.put(threadId, (Condition) cond);
			monitorLock.unlock();
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR[ T" + threadId + " -> "
					+ monitorId + "]: " + e.getMessage());
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

		monitorId += Parameters.numShared; // ** update monitor Id to point to
											// the correct position in
											// accessVector data structure
		ReentrantLock monitorLock = lockVars.get(monitorId);
		try {
			monitorLock.lock();
			String currentThread = threadId; // ** current thread trying to
												// execute the shared operation
			String nextThread = accessVector[monitorId].get(0).getThreadId(); // **
																				// next
																				// allowed
																				// thread
																				// according
																				// to
																				// the
																				// recorded
																				// interleaving

			if (monitorName.equals("WAIT")) {
				if (currentThread.equals(nextThread)) // ** since we remove the
														// wait call, we must
														// ensure that it is
														// still called even
														// when the thread is
														// the next one to
														// execute
				{
					if (Parameters.isDebug)
						System.out.println("[OREO-Replayer] beforeMon:\tT"
								+ currentThread + " (" + monitorId + "_"
								+ System.identityHashCode(o)
								+ ").WAIT -> OK\t[" + currentThread
								+ " (currentThread) == " + nextThread
								+ " (nextThread)])");
					monitorLock.unlock();
					o.wait();
					monitorLock.lock();
				} else {
					monitorLock.unlock(); // ** unlock to avoid deadlocks
					while (!currentThread.equals(nextThread)) // ** wait until
																// its turn
					{
						if (Parameters.isDebug)
							System.out.println("[OREO-Replayer] beforeMon:\tT"
									+ currentThread + " (" + monitorId + "_"
									+ System.identityHashCode(o)
									+ ").WAIT -> WAIT\t[" + currentThread
									+ " (currentThread) != " + nextThread
									+ " (nextThread)])");

						o.wait();
						nextThread = accessVector[monitorId].get(0)
								.getThreadId(); // ** update nextThread in case
												// it is stale
					}
					monitorLock.lock();
				}
			} else {
				if (currentThread.equals(nextThread)) {
					if (Parameters.isDebug)
						System.out.println("[OREO-Replayer] beforeMon:\tT"
								+ currentThread + " (" + monitorId + "_"
								+ System.identityHashCode(o) + ")."
								+ monitorName + " -> OK\t[" + currentThread
								+ " (currentThread) == " + nextThread
								+ " (nextThread)])");

					if (monitorName.equals("NOTIFY"))
						o.notifyAll(); // ** proceed with notify
				} else
					while (!currentThread.equals(nextThread)) // ** wait until
																// its turn
					{
						if (Parameters.isDebug)
							System.out.println("[OREO-Replayer] beforeMon:\tT"
									+ currentThread + " (" + monitorId + "_"
									+ System.identityHashCode(o) + ")."
									+ monitorName + " -> WAIT\t["
									+ currentThread + " (currentThread) != "
									+ nextThread + " (nextThread)])");

						condVars.get(monitorId).await();
						nextThread = accessVector[monitorId].get(0)
								.getThreadId(); // ** update nextThread in case
												// it is stale
					}
			}
			monitorLock.unlock();
		} catch (ArrayIndexOutOfBoundsException e) // ** if the thread has no
													// more constraints, it
													// should stop running...
		{
			// ** if the monitor is WAIT, then we can let the thread proceed
			// with no harm
			if (monitorName.equals("WAIT")) {
				System.err
						.println("[OREO-Replayer] T"
								+ Thread.currentThread().getName()
								+ ": Access vector of SPE  monitor 100"
								+ monitorId
								+ " is empty! But is an access to WAIT, so allow the thread to proceed.");
				while (true) {
					monitorLock.unlock();
					try {
						o.wait();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					monitorLock.lock();
				}
			} else {
				System.err.println("[OREO-Replayer] ERROR: T"
						+ Thread.currentThread().getName()
						+ ": access vector of SPE monitor 1080 " + monitorId + " is empty!");
			}
		} catch (Exception e) {
			System.err
					.println("[OREO-Replayer] ERROR [T"
							+ Thread.currentThread().getName() + " -> "
							+ monitorId + "_" + System.identityHashCode(o)
							+ "]: " + e.getMessage());
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
	public static void afterMonitorEnter(Object o, int monitorId,
			String threadId, String monitorName) {

		System.out.println("monitor id a " + monitorId);
		
		try {
			monitorId += Parameters.numShared -  2; // ** update monitor Id to point
												// to the correct position in
												// accessVector data structure
			System.out.println("monitor id b " + monitorId);
			ReentrantLock monitorLock = lockVars.get(monitorId);
			monitorLock.lock();
			if (!accessVector[monitorId].isEmpty())
				accessVector[monitorId].remove(0);
			condVars.get(monitorId).signalAll();
			synchronized (o) {
				o.notifyAll(); // ** necessary to wake up threads waiting in the
								// obj monitor
			}

			// ** since we remove the signal call, we must ensure that it is
			// still called
			if (Parameters.isDebug)
				System.out.println("[OREO-Replayer] afterMon:\tT" + threadId
						+ " (" + monitorId + "_" + System.identityHashCode(o)
						+ ")." + monitorName + " -> PROCEED");
			// TODO: do we need some sort of MapLastNotify here?
			monitorLock.unlock();
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR[ T" + threadId + " -> "
					+ monitorId + "]: " + e.getMessage());
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

			// ** to generate deterministic thread identifiers
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
			MapBackupThreadName.put(Thread.currentThread(), threadId);

			if (Parameters.isDebug)
				System.out.println("[OREO-Replayer] T" + threadId
						+ " started running");
		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR: " + e.getMessage());
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
			if(parentId.contains("main")) 
				parentId = "0";
			int childCounter = threadChildrenCounter.get(parentId);
			String newThreadName;

			// ** the ith thread spawned by the main thread should have thread
			// id = i
			if (!parentId.equals("0"))
				newThreadName = parentId + ":" + childCounter;
			else
				newThreadName = String.valueOf(childCounter);

			t.setName(newThreadName);
			childCounter++;
			threadChildrenCounter.put(parentId, childCounter);

		} catch (Exception e) {
			System.err.println("[OREO-Replayer] ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public synchronized static void joinRunThreadAfter(Thread t, String threadId) {

	}
}
