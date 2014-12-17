package edu.ist.symber;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.ist.symber.monitor.Monitor;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> arg = new LinkedList(Arrays.asList(args));
		int len = arg.size();
		if (len == 0) {
			System.err.println("please specify: <main class> <parameters>... ");
		} else {
			process(arg);
		}
	}

	private static void process(List<String> args) {
		Monitor.initialize();// (Integer.valueOf(args.get(0)),Integer.valueOf(args.get(1)));

		run(args);// args.subList(2, args.size()));
	}

	private static void run(List<String> args) {
		try {
			String appname = args.get(0);

			MonitorThread monThread = new MonitorThread(appname);
			Runtime.getRuntime().addShutdownHook(monThread);

			Class<?> c = Class.forName(appname);
			Class[] argTypes = new Class[] { String[].class };
			Method main = c.getDeclaredMethod("main", argTypes);

			String[] mainArgs = {};

			if (args.size() > 1) {
				mainArgs = new String[args.size() - 1];
				for (int k = 0; k < args.size() - 1; k++)
					mainArgs[k] = args.get(k + 1);
			}
			main.invoke(null, (Object) mainArgs);
			// production code should handle these exceptions more gracefully
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

}
