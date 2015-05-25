package edu.ist.symber;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.ist.symber.ArgParserReplayer.Option;
import edu.ist.symber.monitor.Monitor;


public class Main {

	public static final ArgParserReplayer CONFIGURATION = new ArgParserReplayer();
	public static long startRep, endRep;
	
	
	/**
	 * Parse input arguments. 
	 * @param args
	 */
	public static boolean parseArgs(String[] args)
	{
		try{
			CONFIGURATION.parse(args);
			CONFIGURATION.validate();

			String mainclass = CONFIGURATION.getValue(Option.MAIN_CLASS);
			Parameters.logpath = CONFIGURATION.getValue(Option.LOG_PATH);
			Parameters.numShared = CONFIGURATION.getValueAsInt(Option.NUM_SHARED);
			Parameters.numSync = CONFIGURATION.getValueAsInt(Option.NUM_SYNC);

			System.out.println("[OREO-Replayer] Settings:");
			System.out.println("  >> MAIN CLASS: "+mainclass);
			System.out.println("  >> NUM SHARED: "+Parameters.numShared);
			System.out.println("  >> NUM SYNC: "+Parameters.numSync);
			System.out.println("  >> LOG: "+Parameters.logpath);
			
			//** parse main input parameters
			Parameters.params = mainclass.split(" ");  
			Parameters.mainClass = Parameters.params[0];
			
			return true;
		}
		catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public static void replay()
	{		
		//** initialize monitor
		Monitor.initialize();
		
		new Thread("main"){
			public void run(){
				// load arguments
				//String[] mainargs = {};
				try {
					
					Class<?> c = Class.forName(Parameters.mainClass);
					Class<?>[] argTypes = new Class[] { String[].class };
					Method main = c.getDeclaredMethod("main", argTypes);

					String[] mainArgs = {};
					if(Parameters.params.length>0)
					{
						mainArgs = new String[Parameters.params.length-1];
						for(int k=0;k<Parameters.params.length-1;k++)
							mainArgs[k] = Parameters.params[k+1];
					} 

					startRep = System.nanoTime();
					main.invoke(null, (Object)mainArgs);
					endRep = System.nanoTime();
					System.err.println("\n[OREO-Replayer] REPLAYING TIME: " + Util.getElaspedTime(startRep, endRep)+" seconds");
					Writer writer = new BufferedWriter(new FileWriter("replayer-logtime.txt", true));
					writer.append(Util.getElaspedTime(startRep, endRep) + "\t");
					writer.append("\r\n");
					writer.close();//*/
				}catch(InvocationTargetException e)
				{
					System.err.println("[OREO-Replayer] ERROR: No such method. Please instrument again the mainclass using the correct mode.");
					e.printStackTrace();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	
	
	public static void main(String[] args) {
		boolean ok = parseArgs(args);
		if(ok){
			replay();
		}
	}
	
}
