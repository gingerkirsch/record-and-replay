package edu.ist.symber;

import java.io.FileNotFoundException;

import edu.ist.symber.monitor.Monitor;

public class MonitorThread extends Thread
{

	String appname;
	
	MonitorThread(String app)
	{
		super("MonitorThread");
		this.appname = app;
	}
	
	public void run()
	{
		
			try { 
				Monitor.saveMonitorData(appname);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(Monitor.isCrashed)
			{
				System.err.println(">> Program crashed! Generating the test driver program ... ");
				//Monitor.generateTestDriver(traceFile_);
			}
			else
			{
				//Monitor.generateTestDriver(Monitor.saveMonitorData(appname));
			}

	}
}	
