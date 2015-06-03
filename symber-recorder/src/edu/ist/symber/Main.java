package edu.ist.symber;

import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.ist.symber.monitor.Monitor;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long start, end;
		start = System.nanoTime(); //start timestamp
		Monitor.TIME_OF_SAVEMONITORDATA = start;
		List<String>  arg = new LinkedList(Arrays.asList(args));
		int len = arg.size();
		if(len==0)
		{
			System.err.println("please specify: <main class> <parameters> --num-shared=<> --num-sync=<>... ");
		}
		else 
		{
			
			for(int i = 0; i < args.length; i++){
				if(args[i].contains("-stride")){ //look for stride mode
					System.out.println(">> STRIDE MODE: ON");
					Monitor.STRIDEMODE = true;
				}
				else if(args[i].contains("--num-shared")){
					String numSh = args[i].substring(args[i].indexOf("=")+1);
					Parameters.numShared = Integer.valueOf(numSh);					
				}
				else if(args[i].contains("--num-sync")){
					String numSy = args[i].substring(args[i].indexOf("=")+1);
					Parameters.numSync = Integer.valueOf(numSy);					
				}
			}
			process(arg);
		}
	
		end = System.nanoTime(); //** end timestamp
		double time = (((double)(end - start)/1000000000));
		System.out.println("Time of recorder with logging time excluded:" +time);
		Writer writer = new BufferedWriter(new FileWriter("recorder-without-logtime.txt", true));
		writer.append(time + "\t");
		writer.append("\r\n");
		writer.close();//*/
		System.out.println("\nRECORDER TIME: "+time+"s");
		
		
		//System.out.println("Overall time of recording: " + elapsedSeconds + "sec");
	}
			
	private static void process(List<String> args)
	{
		Monitor.initialize();//(Integer.valueOf(args.get(0)),Integer.valueOf(args.get(1)));
		
		run(args);//args.subList(2, args.size()));
	}
	private static void run(List<String> args)
	{
		try 
		{
			String appname = args.get(0);
			
			MonitorThread monThread = new MonitorThread(appname);
			Runtime.getRuntime().addShutdownHook(monThread);
					
			Class<?> c = Class.forName(appname);
		    Class[] argTypes = new Class[] { String[].class };
		    Method main = c.getDeclaredMethod("main", argTypes);
		   
		    String[] mainArgs = {};

		    if(args.size()>1)
		    {
		    	mainArgs = new String[args.size()-1];
		    	for(int k=0;k<args.size()-1;k++)
		    		mainArgs[k] = args.get(k+1);
		    }
		    main.invoke(null, (Object)mainArgs);
			// production code should handle these exceptions more gracefully
			} catch (Exception x) {
			    x.printStackTrace();
			}
	}

}
