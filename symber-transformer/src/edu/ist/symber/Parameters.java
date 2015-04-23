package edu.ist.symber;

public class Parameters 
{
	public static int LOOP_STMT_COUNT = 0;
	public static final String CRASH_ANNOTATION="Crashed_with";
	public static final String CATCH_EXCEPTION_SIG = "<edu.ist.symber.monitor.Monitor: void crashed(java.lang.Throwable)>";
	

	public static String OUTPUT_JIMPLE ="jimple";
	public static String PHASE_RECORD ="runtime";
	public static String PHASE_REPLAY ="replay";
	public static String OUTPUT_JIMPLE_SYMBERLEAP ="jimpleSymberLEAP";		//** to distinguish output folders when in SymberLEAP mode
	public static String PHASE_RECORD_SYMBERLEAP ="runtimeSymberLEAP";		//** to distinguish output folders when in SymberLEAP mode
	public static String PHASE_REPLAY_SYMBERLEAP ="replaySymberLEAP";		//** to distinguish output folders when in SymberLEAP mode
	public static boolean shouldInstru = false;
	public static boolean removeSync = true;		//** tells whether the original synchronization calls are removed from the bytecode or not (this should be set to true!)
	public static boolean traceSharedMem = true;	//** tells whether the shared memory access order is to be traced or not
	public static boolean isLEAPmode = false;		//** tells whether the trace mode is similar to LEAP (thread access vectors w.r.t shared variables) or traditional symber (vector clocks per thread) 
	public static boolean isOREOmode = false;		//** indicates whether we are instrumenting in OREO mode or not 
	
	public static boolean isMethodPublic=false;
	public static boolean isMethodStatic = false;
	public static boolean isMethodRunnable = false;
	public static boolean isMethodSynchronized = false;
	public static boolean isMethodMain = false;
	
	public static boolean isRuntime=true;
	public static boolean isReplay=false;
	public static boolean isOutputJimple=false;
	
	public static boolean isInnerClass = false;
	public static boolean isAnonymous = false;
	public static boolean isStmtInLoop = false;
		
	public static int lockCount=0;
}
