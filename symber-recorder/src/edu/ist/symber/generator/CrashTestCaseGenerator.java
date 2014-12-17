package edu.ist.symber.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import edu.ist.symber.Parameters;
import edu.ist.symber.monitor.Monitor;

public class CrashTestCaseGenerator {

	// initial support string
	public final String imports[] = { "import edu.ist.symber.tracer.*;",
			// "import edu.ist.symber.replayer.*;",
			"import edu.ist.symber.monitor.Monitor;" };

	/**
	 * Subclass (generated code and utility)
	 * 
	 * @author hunkim
	 * 
	 */
	static class GeneratedCode {
		String code;

		String className;

		void showCode() {
			System.out.println(code);
		}

		void toFile(String sourceDir, String packageName) throws IOException {
			assert (code != null && className != null && sourceDir != null && packageName != null);
			// packageName ="";
			File javaFile = new File(sourceDir + File.separator
					+ packageName.replace('.', '/'), className + ".java");
			javaFile.getParentFile().mkdirs();
			FileWriter out = new FileWriter(javaFile);
			System.err.println("Generated replay driver go to: " + javaFile);
			out.write(code + "\n");
			out.close();
		}

	}

	/**
	 * Generate code from traceItem
	 * 
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public GeneratedCode codeGeneration(String packageName, String traceFile_)
			throws IOException, ClassNotFoundException {

		String traceFileName_accessVector = traceFile_ + "_trace.gz";
		// String traceFileName_threadNameToIdMap=
		// traceFile_+"_threadNameToIdMap.trace.gz";
		// String traceFileName_nanoTimeDataVec=
		// traceFile_+"_nanoTimeDataVec.trace.gz";
		// String traceFileName_nanoTimeThreadVec=
		// traceFile_+"_nanoTimeThreadVec.trace.gz";

		GeneratedCode generatedCode = new GeneratedCode();

		// Package and starting stuff
		String generatedCodeString = "package " + packageName + ";\n\n";

		String programNameVersionNumber = "XXX";

		generatedCodeString += "// generated by Symber on " + new Date() + "\n";
		generatedCodeString += "// Created for " + programNameVersionNumber
				+ " JVM: " + System.getProperty("java.vm.version") + "\n";

		// print out imports
		for (int i = 0; i < imports.length; i++) {
			generatedCodeString += imports[i] + "\n";
		}

		int last = traceFile_.length();
		int first = last;
		while (first > 0) {
			first--;
			if (traceFile_.charAt(first) > '9'
					|| traceFile_.charAt(first) < '0')
				break;
		}
		// generatedCode.className = "symber"+traceFile_.substring(first+1,
		// last);
		generatedCode.className = "ReplayDriver";

		// get Stack trace

		generatedCodeString += "\n";
		generatedCodeString += "public class " + generatedCode.className
				+ " {\n";// extends TestCase

		String quotedTraceFileName_accessVector = traceFileName_accessVector
				.replace("\\", "\\\\");

		generatedCodeString += "\n\tpublic static void setUp() throws Exception {\n";

		generatedCodeString += "\t\tTraceReader.readTrace(\""
				+ quotedTraceFileName_accessVector + "\");\n";
		generatedCodeString += "\t\tMonitor.initialize();\n";// +","+Monitor.MapSyncVarToState.keySet().size()+");\n";

		generatedCodeString += "\t}\n";

		generatedCodeString += "\n\tpublic static void main(String[] args) throws Throwable {\n";
		generatedCodeString += "\n\t\tsetUp();\n";

		// create new thread
		String createNewThreadString;
		// start new thread
		String startNewThreadString = "\t\t\t" + "}\n" + "\t\t"
				+ "}.start();\n";

		// create new thread
		createNewThreadString = "\n\t\t" + "new Thread(" + "\""
				+ Parameters.MAIN_THREAD_NAME + "\"){\n" + "\t\t\t"
				+ "public void run(){\n";
		generatedCodeString += createNewThreadString;

		String arg = "{";
		// if(Monitor.mainargs != null)
		// {
		for (int i = 0; i < Monitor.mainargs.length; i++) {
			if (i < Monitor.mainargs.length - 1)
				arg += "\"" + Monitor.mainargs[i] + "\"" + ",";
			else
				arg += "\"" + Monitor.mainargs[i] + "\"";

		}
		// }
		arg += "}";

		generatedCodeString += "\t\t\t\t// load arguments\n";
		generatedCodeString += "\t\t\t\t" + "String[]" + " " + "mainargs"
				+ " = " + arg + ";\n";

		String callMethodString = Monitor.methodname + "(" + "mainargs" + ");";

		generatedCodeString += "\t\t\t\ttry {";
		generatedCodeString += "\n\t\t\t\t\t" + callMethodString;
		generatedCodeString += "\n\t\t\t\t} catch (Throwable e) {";
		generatedCodeString += "\n\t\t\t\t\te.printStackTrace();";
		generatedCodeString += "\n\t\t\t\t}\n";

		generatedCodeString += startNewThreadString;

		// generatedCodeString += "\n\t\t(new Scheduler()).start();\n";
		// generatedCodeString += "\n\t\t//WAIT FOR 1 MINUTE TO STOP REPLAY";
		// generatedCodeString +=
		// "\n\t\t//Thread.currentThread().sleep(2000);\n";

		generatedCodeString += "\t}\n";
		generatedCodeString += "}\n";

		generatedCode.code = generatedCodeString;
		return generatedCode;
	}

	/**
	 * Prints out stack trace as comments
	 * 
	 * @param generatedCode
	 * @param stackTraceElements
	 * @return
	 */
	private String generateStackTrace(GeneratedCode generatedCode) {

		// Throwable exception = methodTraceItem.getCrashedException();
		Throwable exception = Monitor.crashedException;

		if (exception == null) {
			return "\n//-----No stack trace ------------\n";
		}

		StackTraceElement[] stackTraceElements = exception.getStackTrace();
		String generatedCodeString = "\n//-----Stack trace: "
				+ exception.toString() + " ------------\n";
		for (int i = 0; i < stackTraceElements.length; i++) {
			generatedCodeString += "//" + stackTraceElements[i] + "\n";
		}

		Throwable cause = exception.getCause();
		if (cause == null) {
			return generatedCodeString;
		}

		stackTraceElements = cause.getStackTrace();
		generatedCodeString += "\n//----- Caused by: " + cause.toString()
				+ " ------------\n";
		for (int i = 0; i < stackTraceElements.length; i++) {
			generatedCodeString += "//" + stackTraceElements[i] + "\n";
		}

		return generatedCodeString;
	}

	static public void main(String args[]) throws IOException,
			ClassNotFoundException {
		String packageName = "replaydriver";

		GeneratedCode generetedCode = new CrashTestCaseGenerator()
				.codeGeneration(packageName, args[0]);
		if (generetedCode == null) {
			System.err
					.println("--- Fail to generate the test driver program --- ");
			return;
		}
		// generated code output directory
		// usually your workspace/project/src
		if (args.length == 2) {
			generetedCode.toFile(args[1], packageName);
		}

		generetedCode.showCode();
	}
}
