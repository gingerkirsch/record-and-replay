package edu.ist.symber.resolver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

public class Z3Connector {
	Process process;
	OutputStream stdin;
	InputStream stdout;
	BufferedReader brCleanUp;
	boolean sat;
	File z3File;
	FileWriter fw;
	BufferedWriter bw;
	boolean isMacOS = false;

	public Z3Connector() {
		try {
			
			//decideZ3Version();
			
			if(isMacOS){
				process = Runtime.getRuntime().exec("./lib/z3_mac -smt2 -in");
			}
			else{
				process = Runtime.getRuntime().exec("d:\\record-and-replay\\offline-resolver\\lib\\z3.exe -smt2 -in");
			}
			stdin = process.getOutputStream();
			stdout = process.getInputStream();
			brCleanUp = new BufferedReader (new InputStreamReader (stdout));
			
			
			
			z3File = new File(".\\z3InputFile.txt");
			if (!z3File.exists()) {
				z3File.createNewFile();
			}
			fw = new FileWriter(z3File.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			
//			stdin.write(("(declare-const x Int)\n(assert (>= x 0))\n(assert (<= x 50))\n(check-sat)\n").getBytes());
//			stdin.flush();
//			System.out.println("Test sat: "+brCleanUp.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void decideZ3Version() throws IOException {
		process = Runtime.getRuntime().exec("./lib/z3 -version");
		stdout = process.getInputStream();
		brCleanUp = new BufferedReader (new InputStreamReader (stdout));
		String line = brCleanUp.readLine();
		System.out.println("Version: "+line);
		process.destroy();
		stdout.close();
		brCleanUp.close();
	}
	
	public void writeLineZ3(String content){
		try {
			content = content+"\n";
			stdin.write(content.getBytes());
			stdin.flush();
			bw.write(content);
			bw.flush();
			//System.out.println("[stdoutZ3] "+content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error while posting on Z3");
		}
	}

	public Object makeRealVar(String name, double min, double max) {
		writeLineZ3("(declare-const "+name+" Real)");
		writeLineZ3("(assert (>= "+name+" "+min+"))\n(assert (<= "+name+" "+max+"))");
		return name;
	}
	
	public void printModel() {
		try {
			String content = "(get-model)\n";
			stdin.write(content.getBytes());
			stdin.flush();
			bw.write(content);
			bw.flush();
			String line = brCleanUp.readLine();
			while (line!=null){
				System.out.println("[Z3:stdout] "+line);
				line = brCleanUp.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Modelo:" + model.constraintsToString());
	}

	
	public Boolean solve() {
		try {
			String line = "";
			
			// Checking satisfiability
			System.out.println("Z3: About to solve");
			long startTime = System.nanoTime();
			
			stdin.write(("(check-sat)\n").getBytes());
			stdin.flush();
			bw.write("(check-sat)\n");
			bw.flush();
			
			line = brCleanUp.readLine();
			
			long endTime = System.nanoTime();
			double duration = ((double) (endTime - startTime)) / 1000000000;
			System.out.println("Duration solving phase: "+duration+"(s)");
			
			if (line.equals("sat")){
				System.out.println("It is satisfiable?: "+line);
				//this.printModel();
				//bw.close();
				return true;
			}else{
				//bw.close();
				System.out.println("[Z3stdout] unsat");
				System.out.println(line);
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public Object makeIntVar(String name, int min, int max) {
		writeLineZ3("(declare-const "+name+" Int)");
		writeLineZ3("(assert (>= "+name+" "+min+"))\n(assert (<= "+name+" "+max+"))");
		return name;
	}
	
	public Object declareConst(String name) {
		writeLineZ3("(declare-const "+name+" Int)");
		return name;
	}

	public String makeIntVarAndStore(String name, int min, int max) {
		makeIntVar(name, min, max);
		//operationsVariables.add(name);
		return name;
	}

	public Object and(int value, Object exp) {
		return "(and "+value+" "+exp+")";
	}

	public Object and(Object exp, int value) {
		return "(and "+exp+" "+value+")";
	}

	public Object and(Object exp1, Object exp2) {
		return "(and "+exp1+" "+exp2+")";
	}
	
	public Object and(String concat){
		return "(and "+concat+")";
	}

	public Object div(int value, Object exp) {

		return "(div "+value+" "+exp+")";
	}

	public Object div(Object exp, int value) {
		return "(div "+exp+" "+value+")";
	}

	public Object div(Object exp1, Object exp2) {
		return "(div "+exp1+" "+exp2+")";
	}

	public Object div(double value, Object exp) {
		return "(div "+value+" "+exp+")";
	}

	public Object div(Object exp, double value) {
		return "(div "+exp+" "+value+")";
	}

	public Object mod(Object exp1, Object exp2) {
		return "(mod "+exp1+" "+exp2+")";
	}

	public Object mod(int value, Object exp) {
		return "(mod "+value+" "+exp+")";
	}

	public Object mod(Object exp, int value) {
		return "(mod "+exp+" "+value+")";
	}

	public Object eq(int value, Object exp) {
		return "(= "+value+" "+exp+")";
	}

	public Object eq(Object exp, int value) {
		return "(= "+exp+" "+value+")";
	}

	public Object eq(Object exp1, Object exp2) {
		return "(= "+exp1+" "+exp2+")";
	}

	public Object eq(double value, Object exp) {
		return "(= "+value+" "+exp+")";
	}

	public Object eq(Object exp, double value) {
		return "(= "+exp+" "+value+")";
	}

	public Object geq(int value, Object exp) {
		return "(>= "+value+" "+exp+")";
	}

	public Object geq(Object exp, int value) {
		return "(>= "+exp+" "+value+")";
	}

	public Object geq(Object exp1, Object exp2) {
		return "(>= "+exp1+" "+exp2+")";
	}

	public Object geq(double value, Object exp) {
		return "(>= "+value+" "+exp+")";
	}

	public Object geq(Object exp, double value) {
		return "(>= "+exp+" "+value+")";
	}

	public int getIntValue(Object dpVar) {
		try {
			stdin.write(("(get-value ("+dpVar+"))\n").getBytes());
			stdin.flush();
			bw.write("(get-value ("+dpVar+"))\n");
			bw.flush();
			String line = brCleanUp.readLine();
			System.out.println("Value for "+dpVar+"= "+line);
			String chunk = line.split(" ")[1];
			int value = Integer.valueOf(chunk.substring(0, chunk.length()-2));
			return value;
			
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public double getRealValue(Object dpVar) {
		throw new RuntimeException("## Unsupported get real value ");
	}

	public double getRealValueInf(Object dpvar) {
		throw new RuntimeException("## Unsupported get real value ");
	}

	public double getRealValueSup(Object dpVar) {
		throw new RuntimeException("## Unsupported get real value ");
	}

	public Object gt(int value, Object exp) {
		return "(> "+value+" "+exp+")";
	}

	public Object gt(Object exp, int value) {
		return "(> "+exp+" "+value+")";
	}

	public Object gt(Object exp1, Object exp2) {
		return "(> "+exp1+" "+exp2+")";
	}

	public Object gt(double value, Object exp) {
		return "(> "+value+" "+exp+")";
	}

	public Object gt(Object exp, double value) {
		return "(> "+exp+" "+value+")";
	}
	
	public Object gt(List<Object> exps) {
		StringBuilder sb = new StringBuilder();
		sb.append("(> ");
		for (Object exp : exps){
			sb.append(exp+" ");
		}
		sb.append(")");
		return sb.toString();
	}
	
	public Object gt(Object hb, List<Object> exps) {
		StringBuilder sb = new StringBuilder();
		sb.append("(> "+hb+" ");
		for (Object exp : exps){
			sb.append(exp+" ");
		}
		sb.append(")");
		return sb.toString();
	}

	public Object leq(int value, Object exp) {
		return "(<= "+value+" "+exp+")";
	}

	public Object leq(Object exp, int value) {
		return "(<= "+exp+" "+value+")";
	}

	public Object leq(Object exp1, Object exp2) {
		return "(<= "+exp1+" "+exp2+")";
	}

	public Object leq(double value, Object exp) {
		return "(<= "+value+" "+exp+")";
	}

	public Object leq(Object exp, double value) {
		return "(<= "+exp+" "+value+")";
	}

	public Object lt(int value, Object exp) {
		return "(< "+value+" "+exp+")";
	}

	public Object lt(Object exp, int value) {
		return "(< "+exp+" "+value+")";
	}

	public Object lt(Object exp1, Object exp2) {
		return "(< "+exp1+" "+exp2+")";
	}

	public Object lt(double value, Object exp) {
		return "(< "+value+" "+exp+")";
	}

	public Object lt(Object exp, double value) {
		return "(< "+exp+" "+value+")";
	}
	
	public Object lt(List<Object> exps) {
		StringBuilder sb = new StringBuilder();
		sb.append("(< ");
		for (Object exp : exps){
			sb.append(exp+" ");
		}
		sb.append(")");
		return sb.toString();
	}
	
	public Object lt(Object hb, List<Object> exps) {
		StringBuilder sb = new StringBuilder();
		sb.append("(< "+hb+" ");
		for (Object exp : exps){
			sb.append(exp+" ");
		}
		sb.append(")");
		return sb.toString();
	}

	public String summation(String[] sum){
		StringBuilder finalString = new StringBuilder("(+");
		for (String s : sum){
			finalString.append(" "+s);
		}
		finalString.append(")");
		return finalString.toString();
	}
	
	public Object minus(int value, Object exp) {
		return "(- "+value+" "+exp+")";
	}

	public Object minus(Object exp, int value) {
		return "(- "+exp+" "+value+")";
	}

	public Object minus(Object exp1, Object exp2) {
		return "(- "+exp1+" "+exp2+")";
	}

	public Object minus(double value, Object exp) {
		return "(- "+value+" "+exp+")";
	}

	public Object minus(Object exp, double value) {
		return "(- "+exp+" "+value+")";
	}

	public Object mixed(Object exp1, Object exp2) {
		throw new RuntimeException("## Unsupported mixed ");
	}

	public Object mult(int value, Object exp) {
		return "(* "+value+" "+exp+")";
	}

	public Object mult(Object exp, int value) {
		return "(* "+exp+" "+value+")";
	}

	public Object mult(Object exp1, Object exp2) {
		return "(* "+exp1+" "+exp2+")";
	}

	public Object mult(double value, Object exp) {
		return "(* "+value+" "+exp+")";
	}

	public Object mult(Object exp, double value) {
		return "(* "+exp+" "+value+")";
	}

	public Object neq(int value, Object exp) {
		return "(not (= "+value+" "+exp+"))";
	}

	public Object neq(Object exp, int value) {
		return "(not (= "+exp+" "+value+"))";
	}

	public Object neq(Object exp1, Object exp2) {
		return "(not (= "+exp1+" "+exp2+"))";
	}

	public Object neq(double value, Object exp) {
		return "(not (= "+value+" "+exp+"))";
	}

	public Object neq(Object exp, double value) {
		return "(not (= "+exp+" "+value+"))";
	}

	public Object or(int value, Object exp) {
		return "(or "+value+" "+exp+")";
	}

	public Object or(Object exp, int value) {
		return "(or "+exp+" "+value+")";
	}

	public Object or(Object exp1, Object exp2) {
		return "(or "+exp1+" "+exp2+")";
	}
	
	public Object or(String concat){
		return "(or "+concat+")";
	}

	public Object plus(int value, Object exp) {
		return "(+ "+value+" "+exp+")";
	}

	public Object plus(Object exp, int value) {
		return "(+ "+exp+" "+value+")";
	}

	public Object plus(Object exp1, Object exp2) {
		return "(+ "+exp1+" "+exp2+")";
	}

	public Object plus(double value, Object exp) {
		return "(+ "+value+" "+exp+")";
	}

	public Object plus(Object exp, double value) {
		return "(+ "+exp+" "+value+")";
	}

	public void post(Object constraint) {
		writeLineZ3("(assert "+constraint+")");
	}

	public Object shiftL(int value, Object exp) {
		throw new RuntimeException("## Unsupported shiftL");
	}

	public Object shiftL(Object exp, int value) {
		throw new RuntimeException("## Unsupported shiftL");
	}

	public Object shiftL(Object exp1, Object exp2) {
		throw new RuntimeException("## Unsupported shiftL");
	}

	public Object shiftR(int value, Object exp) {
		throw new RuntimeException("## Unsupported shiftR");
	}

	public Object shiftR(Object exp, int value) {
		throw new RuntimeException("## Unsupported shiftR");
	}

	public Object shiftR(Object exp1, Object exp2) {
		throw new RuntimeException("## Unsupported shiftR");
	}

	public Object shiftUR(int value, Object exp) {
		throw new RuntimeException("## Unsupported shiftUR");
	}

	public Object shiftUR(Object exp, int value) {
		throw new RuntimeException("## Unsupported shiftUR");
	}

	public Object shiftUR(Object exp1, Object exp2) {
		throw new RuntimeException("## Unsupported shiftUR");
	}

	public Object xor(int value, Object exp) {
		throw new RuntimeException("## Unsupported XOR ");
	}

	public Object xor(Object exp, int value) {
		throw new RuntimeException("## Unsupported XOR");
	}

	public Object xor(Object exp1, Object exp2) {
		throw new RuntimeException("## Unsupported XOR");
	}

	public void postLogicalOR(Object[] constraint) {
		throw new RuntimeException("## Unsupported LogicalOR");
	}
}
