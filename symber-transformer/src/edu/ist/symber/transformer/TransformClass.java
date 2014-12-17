package edu.ist.symber.transformer;

import edu.ist.symber.Parameters;
import edu.ist.symber.Util;
import edu.ist.symber.transformer.phase1.TransformerForInstrumentation;
import edu.ist.symber.transformer.phase1.WholeProgramTransformer;
import edu.ist.symber.transformer.phase2.JTPTransformer;

import soot.*;
import soot.jimple.*;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;

import java.io.*;

import java.util.*;

public class TransformClass {
	private FileWriter printer;

	public void print(String str) {
		System.err.println(str);
		try {
			printer.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processAllAtOnce(String[] args, Visitor visitor) {

		TransformerForInstrumentation.v().setVisitor(visitor);
		String mainclass = args[0];
		try {
			printer = new FileWriter(System.getProperty("user.dir")
					+ System.getProperty("file.separator") + "tmp"
					+ System.getProperty("file.separator") + mainclass);
		} catch (IOException e) {
			e.printStackTrace();
		}

		transformRuntimeVersion(mainclass);
		transformReplayVersion(mainclass);

		print("*** *** *** *** *** *** *** *** *** ***");
		// print("\n*** Total access number: "+ Visitor.totalaccessnum);
		// print("\n*** SPE access number: "+ Visitor.sharedaccessnum);
		// print("\n*** Instrumented SPE access number: "+
		// Visitor.instrusharedaccessnum);
		print("*** SHARED VARIABLES [" + Visitor.speIndexMap.size() + "]");
		Iterator speSetIt = Visitor.speIndexMap.keySet().iterator();
		// print("\n*** *** *** *** *** *** *** *** *** *** ");
		// print("\n*** SPE name: ");
		while (speSetIt.hasNext()) {
			Object spe = speSetIt.next();
			print("   " + Visitor.speIndexMap.get(spe) + " - " + spe);
		}

		print("\n*** SYNCHRONIZATION VARIABLES ["
				+ Visitor.syncObjIndexMap.size() + "]");
		Iterator syncSetIt = Visitor.syncObjIndexMap.keySet().iterator();
		// print("\n*** *** *** *** *** *** *** *** *** *** ");
		// print("\n*** SPE name: ");
		while (syncSetIt.hasNext()) {
			Object spe = syncSetIt.next();
			print("   " + Visitor.syncObjIndexMap.get(spe) + " - " + spe);
		}

		try {
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void transformRuntimeVersion(String mainclass) {
		Parameters.isRuntime = true;
		Parameters.isReplay = false;
		setRecordOptions(mainclass);

		String path = Util.getTmpDirectory();// .replace("\\", "\\\\")
		String[] args1 = {
				"-cp",
				".",
				"-pp",// "-validate",
				mainclass,
				"-d",
				path,
				"-f",
				"jimple",
				"-x",
				"org.apache.xalan.",
				"-x",
				"org.apache.xpath.",
				"-i",
				"org.apache.derby.",

				"-i",
				"org.apache.catalina", // bugTomcat37458
				"-i",
				"org.apache.naming", // bugTomcat37458
				"-i",
				"org.apache.commons.logging", // bugTomcat37458
				"-i",
				"org.apache.naming.resources", // bugTomcat37458*/

				"-i",
				"org.apache.derby.impl.jdbc.EmbedConnection", // derby2861
				"-i",
				"org.apache.derby.impl.services.daemon.SingleThreadDaemonFactory", // derby2861
				"-i",
				"org.apache.derby.iapi.services.daemon.DaemonService", // derby2861
				"-i",
				"org.apache.derby.jdbc.EmbeddedDriver", // derby2861*/

				"-x", "jrockit.", "-x", "edu.", "-x", "com.", "-x",
				"checkers.", "-x", "org.xmlpull.", "-x", "org.apache.xml.",
				"-x", "org.apache.xpath." };

		String[] args2 = {
				"-cp",
				".",
				"-pp",
				mainclass,
				"-d",
				path,
				"-x",
				"org.apache.xalan.",
				"-x",
				"org.apache.xpath.",
				"-i",
				"org.apache.derby.",

				"-i",
				"org.apache.catalina", // bugTomcat37458
				"-i",
				"org.apache.naming", // bugTomcat37458
				"-i",
				"org.apache.commons.logging", // bugTomcat37458
				"-i",
				"org.apache.naming.resources", // bugTomcat37458*/

				"-i",
				"org.apache.derby.impl.jdbc.EmbedConnection", // derby2861
				"-i",
				"org.apache.derby.impl.services.daemon.SingleThreadDaemonFactory", // derby2861
				"-i",
				"org.apache.derby.iapi.services.daemon.DaemonService", // derby2861
				"-i",
				"org.apache.derby.jdbc.EmbeddedDriver", // derby2861*/

				"-x", "java.", "-x", "javax.", "-x", "sun.", "-x", "com.",
				"-x", "jrockit.", "-x", "edu.", "-x", "checkers.", "-x",
				"org.xmlpull.", "-x", "org.apache.xml.", "-x",
				"org.apache.xpath." };

		if (Parameters.isOutputJimple) {
			soot.Main.main(args1);// "-f","jimple",c"-x","javato.","-x","edu."
									// \\sootOutput "-process-dir", processDir
		} else {
			soot.Main.main(args2);// "-f","jimple",c"-x","javato.","-x","edu."
									// \\sootOutput "-process-dir", processDir
		}

		soot.G.reset();
		System.err.println("***** Runtime version generated *****");
	}

	private void transformReplayVersion(String mainclass) {
		Parameters.isRuntime = false;
		Parameters.isReplay = true;
		setReplayOptions(mainclass);
		Visitor.resetParameter();

		String path = Util.getTmpDirectory();// .replace("\\", "\\\\")
		String[] args1 = {
				"-cp",
				".",
				"-pp",// "-validate",
				mainclass,
				"-d",
				path,
				"-f",
				"jimple",
				"-x",
				"org.apache.xalan.",
				"-x",
				"org.apache.xpath.",
				"-i",
				"org.apache.derby.",

				"-i",
				"org.apache.catalina", // bugTomcat37458
				"-i",
				"org.apache.naming", // bugTomcat37458
				"-i",
				"org.apache.commons.logging", // bugTomcat37458
				"-i",
				"org.apache.naming.resources", // bugTomcat37458*/

				"-i",
				"org.apache.derby.impl.jdbc.EmbedConnection", // derby2861
				"-i",
				"org.apache.derby.impl.services.daemon.SingleThreadDaemonFactory", // derby2861
				"-i",
				"org.apache.derby.iapi.services.daemon.DaemonService", // derby2861
				"-i",
				"org.apache.derby.jdbc.EmbeddedDriver", // derby2861*/

				"-x", "jrockit.", "-x", "edu.", "-x", "com.", "-x",
				"checkers.", "-x", "org.xmlpull.", "-x", "org.apache.xml.",
				"-x", "org.apache.xpath." };

		String[] args2 = {
				"-cp",
				".",
				"-pp",// "-validate",
				mainclass,
				"-d",
				path,
				"-x",
				"org.apache.xalan.",
				"-x",
				"org.apache.xpath.",
				"-i",
				"org.apache.derby.",

				"-i",
				"org.apache.catalina", // bugTomcat37458
				"-i",
				"org.apache.naming", // bugTomcat37458
				"-i",
				"org.apache.commons.logging", // bugTomcat37458
				"-i",
				"org.apache.naming.resources", // bugTomcat37458*/

				"-i",
				"org.apache.derby.impl.jdbc.EmbedConnection", // derby2861
				"-i",
				"org.apache.derby.impl.services.daemon.SingleThreadDaemonFactory", // derby2861
				"-i",
				"org.apache.derby.iapi.services.daemon.DaemonService", // derby2861
				"-i",
				"org.apache.derby.jdbc.EmbeddedDriver", // derby2861*/

				"-x", "java.", "-x", "javax.", "-x", "sun.", "-x", "com.",
				"-x", "jrockit.", "-x", "edu.", "-x", "checkers.", "-x",
				"org.xmlpull.", "-x", "org.apache.xml." };

		if (Parameters.isOutputJimple) {
			soot.Main.main(args1);// "-f","jimple",c"-x","javato.","-x","edu."
									// \\sootOutput "-process-dir", processDir
		} else {
			soot.Main.main(args2);// "-f","jimple",c"-x","javato.","-x","edu."
									// \\sootOutput "-process-dir", processDir
		}

		soot.G.reset();
		System.err.println("--- Replay version generated ---");
	}

	private void setRecordOptions(String mainclass) {
		PhaseOptions.v().setPhaseOption("jb", "enabled:true");
		Options.v().set_keep_line_number(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().set_whole_program(true);
		Options.v().set_app(true);

		// Enable Spark
		HashMap<String, String> opt = new HashMap<String, String>();
		// opt.put("verbose","true");
		opt.put("propagator", "worklist");
		opt.put("simple-edges-bidirectional", "false");
		opt.put("on-fly-cg", "true");
		opt.put("set-impl", "double");
		opt.put("double-set-old", "hybrid");
		opt.put("double-set-new", "hybrid");
		opt.put("pre_jimplify", "true");
		SparkTransformer.v().transform("", opt);
		PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");

		Scene.v().setSootClassPath(
				System.getProperty("sun.boot.class.path") + File.pathSeparator
						+ System.getProperty("java.class.path"));

		PackManager
				.v()
				.getPack("wjtp")
				.add(new Transform("wjtp.transformer1",
						new WholeProgramTransformer()));
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.transformer2", new JTPTransformer()));

		SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
		if (appclass.declaresMethodByName("main")) // ** to avoid crashing when
													// it doens't exist a main
													// class
			Scene.v().setMainClass(appclass);

		Scene.v().loadClassAndSupport(Visitor.observerClass);
		// Scene.v().loadClassAndSupport("edu.hkust.leap.monitor.MyMonitorInfer");
	}

	private void setReplayOptions(String mainclass) {
		Options.v().set_keep_line_number(true);
		Options.v().set_app(true);

		Scene.v().setSootClassPath(
				System.getProperty("sun.boot.class.path") + File.pathSeparator
						+ System.getProperty("java.class.path"));

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.transformer", new JTPTransformer()));

		SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
		if (appclass.declaresMethodByName("main")) // ** this avoids a crash
													// when the instrumented
													// classes don't have a main
													// method
			Scene.v().setMainClass(appclass);

		Scene.v().loadClassAndSupport(Visitor.observerClass);
		// Scene.v().loadClassAndSupport("edu.hkust.leap.monitor.MyMonitorInfer");
	}
}
