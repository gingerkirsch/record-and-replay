package edu.ist.symber.monitor;

import java.util.ArrayList;
import java.util.Iterator;

//** TRANSFORMER
public class SymberMonitor{

	static ArrayList<Integer> path = new ArrayList<Integer>();
	static ArrayList<Integer> linesAlarm = new ArrayList<Integer>();
	private static String logPath = System.getProperty("user.dir");

	public synchronized static void crashed(Throwable crashedException) {
	}

	//** for instance fields
	public static void beforeLoad(Object fieldId, String threadId){}
	
	public static void afterLoad(Object fieldId, String threadId){}

	public static void beforeStore(Object fieldId, String threadId){}
	
	public static void afterStore(Object fieldId, String threadId){}
	
	//** for static fields
	public static void beforeLoad(int fieldId, String threadId){}
	
	public static void afterLoad(int fieldId, String threadId){}

	public static void beforeStore(int fieldId, String threadId){}
	
	public static void afterStore(int fielId, String threadId){}  

	
	public static void afterMonitorEnterStatic(Object o, int monitorId, String threadId, String monitorName){}
	
	public static void beforeMonitorEnterStatic(Object o,int monitorId, String threadId, String monitorName){}
	
	public static void afterMonitorEnter(Object o, int monitorId, String threadId, String monitorName){}
	
	public static void beforeMonitorEnter(Object o, int monitorId, String threadId, String monitorName){}
	
	public static void afterConditionEnter(Object lock, Object cond, int monitorId, String threadId, String monitorName){}
	
	public static void beforeConditionEnter(Object lock, Object cond, int monitorId, String threadId, String monitorName){}
	
	public static void exitMonitor(Object o, int monitorId, String threadId, String monitorName){}
	
	
	public static void mainThreadStartRun(String threadId,String methodName, String[] args){}

	public static void threadStartRun(String threadId)	{}
	
	public static void threadExitRun(String threadId) {}
	
	public static void startRunThreadBefore(Thread t,String id) {}
	
	public static void joinRunThreadAfter(Thread t,String id){}
	
	
	public static void logDecision(int decision, long id){
		addingToPath(decision);
		printingPath(id);
	}
	public static void logDecisionNoElse(int decision, long id, int line){
		if (decision==0){
			linesAlarm.add(line);
			addingToPath(decision);
			printingPath(id);
		}else{
			if (!linesAlarm.contains(line)){
				addingToPath(decision);
				printingPath(id);
			}else{
				linesAlarm.remove(linesAlarm.indexOf(line));
			}
		}
	}
	private static void addingToPath(int decision){
		path.add(decision);
	}

	private static void printingPath(long lastThread){
		System.out.print("Path till now: ");
		Iterator<Integer> it= path.iterator();
		while (it.hasNext()){
			System.out.print(it.next()+"("+lastThread+") ");
		}
		System.out.println();
	}
}
