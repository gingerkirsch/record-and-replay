package edu.ist.symber;

import edu.ist.symber.monitor.Monitor;

public class MonitorThread extends Thread {

	String appname;

	MonitorThread(String app) {
		super("MonitorThread");
		this.appname = app;
	}

	public void run() {
		if (Monitor.isCrashed) {
			// System.err.println("--- program crashed! ---");
			// System.err.println("--- preparing for reproducing the crash ... ");
			String traceFile_ = Monitor.saveMonitorData(appname);
			System.err
					.println(">> Program crashed! Generating the test driver program ... ");
			// Monitor.generateTestDriver(traceFile_);
		} else {
			// Monitor.generateTestDriver(Monitor.saveMonitorData(appname));
		}
	}
}
