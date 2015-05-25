package edu.ist.symber.transformer;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.AbstractStmt;
import soot.jimple.internal.JNopStmt;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.thread.mhp.pegcallgraph.PegCallGraph;
import soot.tagkit.*;
import soot.util.Chain;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ist.symber.Parameters;
import edu.ist.symber.Util;
import edu.ist.symber.tloax.XFieldThreadEscapeAnalysis;
import edu.ist.symber.transformer.contexts.*;

public class Visitor {

	public static Value mainArgs;
	public static boolean methodEntryPointFlag;
	public static ThreadLocalObjectsAnalysis tlo;
	public static XFieldThreadEscapeAnalysis ftea;

	public static PegCallGraph pecg;
	public static long totalaccessnum = 0;
	public static long sharedaccessnum = 0;
	public static long instrusharedaccessnum = 0;

	public static HashSet<String> sharedInstanceVariableSet = new HashSet<String>();
	public static HashSet<String> sharedStaticVariableSet = new HashSet<String>();
	public static HashSet<String> synchronizedMethodSet = new HashSet<String>();
	public static HashSet<SootMethod> synchronizedIgnoreMethodSet = new HashSet<SootMethod>();
	public static String sharedVariableSig = "";

	public static HashMap<SootMethod, Local> methodToThreadIdMap = new HashMap<SootMethod, Local>();
	public static HashSet<String> sharedVariableWriteAccessSet = new HashSet<String>();
	public static HashMap<Value, Integer> speIndexMap = new HashMap<Value, Integer>();
	public static HashMap<Value, Integer> syncObjIndexMap = new HashMap<Value, Integer>();

	public static HashMap<String, String> conditionToLockMap = new HashMap<String, String>(); 	//** (java.concurrent.locks) maps a Condition to its respective Lock; this is necessary to correctly update the sync var state during runtime
	//public static boolean isWrappedByLock = false;		//** tells whether we are analyzing instructions wrapped by a monitor/lock. If so, we don't need to instrument accesses to loads and stores

	public static HashSet<String> visitedBranches = new HashSet<String>();

	protected Visitor nextVisitor;
	static private int counter = 0;
	static private int indexCounter = 0;
	static private int syncIndexCounter = 0;

	static public String myClass;
	// static public SootMethod logDecision;

	static public String observerClass;

	private static String logPath = System.getProperty("user.dir");;

	public static void resetParameter() {

		methodEntryPointFlag = false;

		totalaccessnum = 0;
		sharedaccessnum = 0;
		instrusharedaccessnum = 0;

		methodToThreadIdMap.clear();
		// sharedVariableWriteAccessSet.clear();

		// counter = 0;
		// indexCounter=0;
	}

	public int getCounter() {
		return counter;
	}

	public static void setObserverClass(String s) {
		observerClass = s;
	}

	public static void setMyClass(String s) {
		observerClass = s;
	}

	public Visitor(Visitor nextVisitor) {
		this.nextVisitor = nextVisitor;
		// myClass =
		// Scene.v().loadClassAndSupport("emdc.jpf.infer.util.MyMonitorInfer");
		// myClass =
		// Scene.v().loadClassAndSupport("edu.hkust.leap.monitor.MyMonitorInfer");
		// logDecision = myClass.getMethod("void logDecision(int)");
	}

	private void setMethodEntryPointFlag() {

		methodEntryPointFlag = true;
	}

	public boolean getMethodEntryPointFlag() {

		return methodEntryPointFlag;
	}

	public void clearMethodEntryPointFlag() {

		methodEntryPointFlag = false;
	}

	public void visitMethodBegin(SootMethod sm, Chain units) {
		setMethodEntryPointFlag();
	}

	public void visitMethodEnd(SootMethod sm, Chain units) {

	}

	public void visitStmt(SootMethod sm, Chain units, Stmt s) {
		nextVisitor.visitStmt(sm, units, s);
	}

	public void visitStmtNop(SootMethod sm, Chain units, NopStmt nopStmt) {
		nextVisitor.visitStmtNop(sm, units, nopStmt);
	}

	public void visitStmtBreakpoint(SootMethod sm, Chain units,
			BreakpointStmt breakpointStmt) {
		nextVisitor.visitStmtBreakpoint(sm, units, breakpointStmt);
	}/*
	 * ThrowStmt ::= 'throw' LocalOrConstant@ThrowContext
	 */

	public void visitStmtThrow(SootMethod sm, Chain units, ThrowStmt throwStmt) {
		nextVisitor.visitStmtThrow(sm, units, throwStmt);
	}

	public void visitStmtReturnVoid(SootMethod sm, Chain units, ReturnVoidStmt returnVoidStmt) {
		if (Parameters.isMethodRunnable) {
			Visitor.addCallRunMethodExitBefore(sm, units, returnVoidStmt);
		}/*
		if (Parameters.isMethodSynchronized && Parameters.removeSync) {
			String sig;
			Value memory;
			Value base;
			sig = sm.getDeclaringClass().getName() + ".OBJECT";// +"."+invokeExpr.getMethod().getName();

			if (sm.isStatic()) {
				memory = StringConstant.v(sig);
				Visitor.addCallAccessSyncObj(sm, units, returnVoidStmt,	"exitMonitorBefore", memory);
			} else {
				memory = StringConstant.v(sig);
				Stmt firstStmt = (Stmt) units.getFirst();
				if (firstStmt instanceof IdentityStmt) {
					base = ((IdentityStmt) firstStmt).getLeftOp();
					Visitor.addCallAccessSyncObjInstance(sm, units,	returnVoidStmt, "exitMonitorBefore", base, memory);
				}
			}

			Visitor.instrusharedaccessnum++;
			Visitor.totalaccessnum++;
		}*/

		//if (Parameters.isMethodSynchronized)
		//isWrappedByLock = false;
		nextVisitor.visitStmtReturnVoid(sm, units, returnVoidStmt);
	}/*
	 * ReturnStmt ::= 'return' LocalOrConstant@ReturnContext
	 */

	public void visitStmtReturn(SootMethod sm, Chain units, ReturnStmt returnStmt) {
		/*	if (Parameters.isMethodSynchronized && Parameters.removeSync) {
			String sig;
			Value memory;
			Value base;
			sig = sm.getDeclaringClass().getName() + ".OBJECT";// +"."+invokeExpr.getMethod().getName();

			if (sm.isStatic()) {
				memory = StringConstant.v(sig);
				Visitor.addCallAccessSyncObj(sm, units, returnStmt,"exitMonitorBefore", memory);
			} else {
				memory = StringConstant.v(sig);
				Stmt firstStmt = (Stmt) units.getFirst();
				if (firstStmt instanceof IdentityStmt) {
					base = ((IdentityStmt) firstStmt).getLeftOp();
					Visitor.addCallAccessSyncObjInstance(sm, units, returnStmt,"exitMonitorBefore", base, memory);
				}
			}

			Visitor.instrusharedaccessnum++;
			Visitor.totalaccessnum++;
		}*/

		//if (Parameters.isMethodSynchronized)
		//isWrappedByLock = false;
		nextVisitor.visitStmtReturn(sm, units, returnStmt);
	}/*
	 * MonitorStmt ::= EnterMonitorStmt | ExitMonitorStmt
	 */

	public void visitStmtMonitor(SootMethod sm, Chain units,
			MonitorStmt monitorStmt) {
		nextVisitor.visitStmtMonitor(sm, units, monitorStmt);
	}/*
	 * ExitMonitorStmt ::= 'monitorexit' LocalOrConstant@ExitMonitorContext
	 */

	public void visitStmtExitMonitor(SootMethod sm, Chain units,
			ExitMonitorStmt exitMonitorStmt) {
		nextVisitor.visitStmtExitMonitor(sm, units, exitMonitorStmt);
	}/*
	 * EnterMonitorStmt ::= 'monitorenter' LocalOrConstant@EnterMonitorContext
	 */

	public void visitStmtEnterMonitor(SootMethod sm, Chain units,
			EnterMonitorStmt enterMonitorStmt) {
		nextVisitor.visitStmtEnterMonitor(sm, units, enterMonitorStmt);
	}/*
	 * LookupSwitchStmt ::= LocalOrConstant@LookupSwitchContext
	 * (LookupValue@LookupSwitchContext Label@LookupSwitchContext)*
	 * Label@LookupSwitchDefaultContext
	 */

	public void visitStmtLookupSwitch(SootMethod sm, Chain units,
			LookupSwitchStmt lookupSwitchStmt) {
		nextVisitor.visitStmtLookupSwitch(sm, units, lookupSwitchStmt);
	}

	public void visitLookupValue(SootMethod sm, Chain units, Stmt stmt,
			int lookupValue) {
		nextVisitor.visitLookupValue(sm, units, stmt, lookupValue);
	}/*
	 * TableSwitchStmt ::= LocalOrConstant@TableSwitchContext
	 * (LookupValue@TableSwitchContext Label@TableSwitchContext)*
	 * Label@TableSwitchDefaultContext
	 */

	public void visitStmtTableSwitch(SootMethod sm, Chain units,
			TableSwitchStmt tableSwitchStmt) {
		nextVisitor.visitStmtTableSwitch(sm, units, tableSwitchStmt);
	}/*
	 * InvokeStmt ::= InvokeExpr@InvokeOnlyContext
	 */

	public void visitStmtInvoke(SootMethod sm, Chain units,
			InvokeStmt invokeStmt) {
		nextVisitor.visitStmtInvoke(sm, units, invokeStmt);
	}

	public boolean checkKindOfIf(Chain units, IfStmt ifStmt){

		Stmt limit = ifStmt.getTarget();
		Stmt next = (Stmt) units.getSuccOf(ifStmt);
		int dif=0;
		while ((!next.equals(limit))||(dif>0)){
			if (next instanceof GotoStmt){
				dif = dif-1;
			}else if (next instanceof IfStmt){
				dif = dif+1;
			}
			next = (Stmt) units.getSuccOf(next);
		}
		if (dif<0){
			return true;
		}else{
			return false;
		}
	}

	public void ifElseInstrumentation(SootMethod sm, Chain units, IfStmt ifStmt){
		//System.out.println("How many? Ans. "+ifStmt.getTarget().getBoxesPointingToThis().size());
		if (Parameters.isRuntime) {

			//Instrumenting when else
			LinkedList args = new LinkedList();
			args.addLast(IntConstant.v(1));
			args.addLast(getMethodThreadId(sm));

			SootMethodRef mr = Scene.v().getMethod("<" + observerClass+ ": void logDecision(int,long)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr,args)),ifStmt.getTarget());

			//Instrumenting when If
			args.removeFirst();
			args.addFirst(IntConstant.v(0));

			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr,args)),units.getSuccOf(ifStmt));
		}
	}

	public void ifNoElseInstrumentation(SootMethod sm, Chain units, IfStmt ifStmt){
		//System.out.println("How many? Ans. "+ifStmt.getTarget().getBoxesPointingToThis().size());
		if (Parameters.isRuntime) {

			//Instrumenting when else
			LinkedList args = new LinkedList();
			args.addLast(IntConstant.v(1));
			args.addLast(getMethodThreadId(sm));
			args.addLast(IntConstant.v(Integer.valueOf(ifStmt.getTag("LineNumberTag").toString())));


			SootMethodRef mr = Scene.v().getMethod("<" + observerClass+ ": void logDecisionNoElse(int,long,int)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr,args)),ifStmt.getTarget());

			//Instrumenting when If
			args.removeFirst();
			args.addFirst(IntConstant.v(0));

			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr,args)),units.getSuccOf(ifStmt));
		}
	}

	public void visitStmtIf(SootMethod sm, Chain units, IfStmt ifStmt) {
		nextVisitor.visitStmtIf(sm, units, ifStmt);
		//CHANGE MANUEL {
		/*String line = sm.getDeclaringClass().toString() + ":"
		+ ifStmt.getTag("LineNumberTag").toString();
		if (!visitedBranches.contains(line)) {
			visitedBranches.add(line);
			System.out.print(line + " goes to: ");
			System.out.println(ifStmt.getTarget().getTag("LineNumberTag")
					.toString());

			//System.out.println("Number of targets: "+listboxes.size());

			boolean elsePart = checkKindOfIf(units, ifStmt);
			//boolean elsePart = true;
			if (elsePart){
				System.out.println("I have else clause!");
				ifElseInstrumentation(sm, units, ifStmt);
			}else{
				ifNoElseInstrumentation(sm, units, ifStmt);
			}
		}
		//CHANGE MANUEL }*/

	}/*
	 * GotoStmt ::= Label@GotoContext
	 */

	public void visitStmtGoto(SootMethod sm, Chain units, GotoStmt gotoStmt) {
		nextVisitor.visitStmtGoto(sm, units, gotoStmt);
	}/*
	 * IdentityStmt ::= Local@IdentityContext ThisRef@IdentityContext |
	 * Local@IdentityContext ParameterRef@IdentityContext | Local@IdentityCntext
	 * CaughtExceptionRef@IdentityContext
	 */

	public void visitStmtIdentity(SootMethod sm, Chain units,
			IdentityStmt identityStmt) {
		nextVisitor.visitStmtIdentity(sm, units, identityStmt);
	}/*
	 * AssignStmt ::= ConcreteRef@LHSContext LocalOrConstant@RHSContext |
	 * Local@LHSContext RHS@LHSContext
	 */

	public void visitStmtAssign(SootMethod sm, Chain units,
			AssignStmt assignStmt) {
		nextVisitor.visitStmtAssign(sm, units, assignStmt);
	}/*
	 * RHS{LHSContext} ::= ConcreteRef@RHSContext | LocalOrConstant@RHSContext |
	 * Expr@RSHContext
	 */

	public void visitRHS(SootMethod sm, Chain units, Stmt s, Value right) {
		nextVisitor.visitRHS(sm, units, s, right);
	}/*
	 * Expr{RHSContext} ::= BinopExpr@RHSContext | CastExpr@RHSContext |
	 * InstanceOfExpr@RHSContext | InvokeExpr@RHSContext | NewExpr@RHSContext |
	 * NewArrayExpr@RHSContext | NewMultiArrayExpr@RHSContext |
	 * LengthExpr@RHSContext | NegExpr@RHSContext
	 */

	public void visitExpr(SootMethod sm, Chain units, Stmt s, Expr expr) {
		nextVisitor.visitExpr(sm, units, s, expr);
	}/*
	 * NegExpr{RHSContext} ::= LocalOrConstant@NegContext
	 */

	public void visitNegExpr(SootMethod sm, Chain units, Stmt s, NegExpr negExpr) {
		nextVisitor.visitNegExpr(sm, units, s, negExpr);
	}/*
	 * LengthExpr{RHSContext} ::= LocalOrConstant@LengthContext
	 */

	public void visitLengthExpr(SootMethod sm, Chain units, Stmt s,
			LengthExpr lengthExpr) {
		nextVisitor.visitLengthExpr(sm, units, s, lengthExpr);
	}/*
	 * NewMultiArrayExpr{RHSContext} ::= Type@NewMultiArrayContext
	 * (LocalOrConstant@NewMultiArrayContext)*
	 */

	public void visitNewMultiArrayExpr(SootMethod sm, Chain units, Stmt s,
			NewMultiArrayExpr newMultiArrayExpr) {
		nextVisitor.visitNewMultiArrayExpr(sm, units, s, newMultiArrayExpr);
	}/*
	 * NewArrayExpr{RHSContext} ::= Type@NewArrayContext
	 * (LocalOrConstant@NewArrayContext)*
	 */

	public void visitNewArrayExpr(SootMethod sm, Chain units, Stmt s,
			NewArrayExpr newArrayExpr) {
		nextVisitor.visitNewArrayExpr(sm, units, s, newArrayExpr);
	}/*
	 * NewExpr{RHSContext} ::= Type@NewArrayContext
	 */

	public void visitNewExpr(SootMethod sm, Chain units, Stmt s, NewExpr newExpr) {
		nextVisitor.visitNewExpr(sm, units, s, newExpr);
	}/*
	 * InvokeExpr{InvokeAndAssignContext,InvokeOnlyContext} ::=
	 * LocalOrConstant@InvokeAndAssignTargetContextImpl
	 * Signature@InvokeAndAssignContext
	 * (LocalOrConstant@InvokeAndAssignArgumentContext)* |
	 * LocalOrConstant@InvokeOnlyTargetContext Signature@InvokeOnlyContext
	 * (LocalOrConstant@InvokeOnlyArgumentContext)*
	 */

	public void visitInvokeExpr(SootMethod sm, Chain units, Stmt s,
			InvokeExpr invokeExpr, InvokeContext context) {
		nextVisitor.visitInvokeExpr(sm, units, s, invokeExpr, context);
	}/*
	 * InstanceOfExpr{RHSContext} ::= LocalOrConstant@InstanceOfContext
	 * Type@InstanceOfContext
	 */

	public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s,
			StaticInvokeExpr invokeExpr, InvokeContext context) {
		nextVisitor.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);
	}

	public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s,
			InstanceInvokeExpr invokeExpr, InvokeContext context) {
		nextVisitor.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);
	}

	public void visitInstanceOfExpr(SootMethod sm, Chain units, Stmt s,
			InstanceOfExpr instanceOfExpr) {
		nextVisitor.visitInstanceOfExpr(sm, units, s, instanceOfExpr);
	}/*
	 * CastExpr{RHSContext} ::= Type@CastContext LocalOrConstant@CastContext
	 */

	public void visitCastExpr(SootMethod sm, Chain units, Stmt s,
			CastExpr castExpr) {
		nextVisitor.visitCastExpr(sm, units, s, castExpr);
	}/*
	 * Type{CastContext,InstanceOfContext,NewArrayContext,NewExpr,
	 * NewMultiArrayContext}
	 */

	public void visitType(SootMethod sm, Chain units, Stmt s, Type castType,
			TypeContext context) {
		nextVisitor.visitType(sm, units, s, castType, context);
	}/*
	 * BinopExpr{RHSContext,IfContext} ::= LocalOrConstant@RHSFirstContext
	 * Binop@RHSContext LocalOrConstant@RHSSecondContext |
	 * LocalOrConstant@IfFirstContext Binop@IfContext
	 * LocalOrConstant@IfSecondContext
	 */

	public void visitBinopExpr(SootMethod sm, Chain units, Stmt s,
			BinopExpr expr, BinopExprContext context) {
		nextVisitor.visitBinopExpr(sm, units, s, expr, context);
	}/*
	 * ConcreteRef{RHSContext,LHSContext} ::= InstanceFieldRef{RHSContext} |
	 * ArrayRef{RHSContext} | StaticFieldRef{RHSContext} |
	 * InstanceFieldRef{LHSContext} | ArrayRef{LHSContext} |
	 * StaticFieldRef{LHSContext}
	 */

	public void visitConcreteRef(SootMethod sm, Chain units, Stmt s,
			ConcreteRef concreteRef, RefContext context) {
		nextVisitor.visitConcreteRef(sm, units, s, concreteRef, context);
	}/*
	 * LocalOrConstant{RHSFirstContext,RHSSecondContext,IfFirstContext,
	 * IfSecondContext,CastContext,InstanceOfContext,
	 * InvokeAndAssignTargetContextImpl
	 * ,InvokeAndAssignArgumentContext,InvokeOnlyTargetContext
	 * ,InvokeOnlyArgumentContext,
	 * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
	 * RHSContext,
	 * EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext
	 * , ReturnContext,ThrowContext} ::= Local | Constant
	 */

	public void visitLocalOrConstant(SootMethod sm, Chain units, Stmt s,
			Value right, LocalOrConstantContext context) {
		nextVisitor.visitLocalOrConstant(sm, units, s, right, context);
	}/*
	 * Constant{{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,
	 * CastContext,InstanceOfContext,
	 * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext
	 * ,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
	 * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
	 * RHSContext,
	 * EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext
	 * , ReturnContext,ThrowContext}
	 */

	public void visitConstant(SootMethod sm, Chain units, Stmt s,
			Constant constant, LocalOrConstantContext context) {
		nextVisitor.visitConstant(sm, units, s, constant, context);
	}/*
	 * Local{RHSFirstContext,RHSSecondContext,IfFirstContext,IfSecondContext,
	 * CastContext,InstanceOfContext,
	 * InvokeAndAssignTargetContextImpl,InvokeAndAssignArgumentContext
	 * ,InvokeOnlyTargetContext,InvokeOnlyArgumentContext,
	 * LengthContext,NegContext,NewMultiArrayContext,NewArrayContext,
	 * RHSContext,
	 * EnterMonitorContext,ExitMonitorContext,LookupSwitchContext,TableSwitchContext
	 * , ReturnContext,ThrowContext,IdentityContext,LHSContext}
	 */

	public void visitLocal(SootMethod sm, Chain units, Stmt s, Local local,
			LocalContext context) {
		nextVisitor.visitLocal(sm, units, s, local, context);
	}/*
	 * StaticFieldRef{RHSContext,LHSContext}
	 */

	public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s,	StaticFieldRef staticFieldRef, RefContext context) {
		nextVisitor.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
	}/*
	 * ArrayRef{RHSContext,LHSContext}
	 */

	public void visitArrayRef(SootMethod sm, Chain units, Stmt s,
			ArrayRef arrayRef, RefContext context) {
		nextVisitor.visitArrayRef(sm, units, s, arrayRef, context);
	}/*
	 * InstanceFieldRef{RHSContext,LHSContext}
	 */

	public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s,
			InstanceFieldRef instanceFieldRef, RefContext context) {
		nextVisitor.visitInstanceFieldRef(sm, units, s, instanceFieldRef,
				context);
	}/*
	 * CaughtExceptionRef{IdentityContext}
	 */

	public void visitCaughtExceptionRef(SootMethod sm, Chain units,
			IdentityStmt s, CaughtExceptionRef caughtExceptionRef) {
		nextVisitor.visitCaughtExceptionRef(sm, units, s, caughtExceptionRef);
	}/*
	 * ParameterRef{IdentityContext}
	 */

	public void visitParameterRef(SootMethod sm, Chain units, IdentityStmt s,
			ParameterRef parameterRef) {
		nextVisitor.visitParameterRef(sm, units, s, parameterRef);
	}/*
	 * ThisRef{IdentityContext}
	 */

	public void visitThisRef(SootMethod sm, Chain units, IdentityStmt s,
			ThisRef thisRef) {
		nextVisitor.visitThisRef(sm, units, s, thisRef);
	}/*
	 * Binop{RHSContext,IfContext}
	 */

	public void visitBinop(SootMethod sm, Chain units, Stmt s, String op,
			BinopExprContext context) {
		nextVisitor.visitBinop(sm, units, s, op, context);
	}/*
	 * Signature{InvokeAndAssignContext,InvokeOnlyContext}
	 */

	public void visitSignature(SootMethod sm, Chain units, Stmt s,
			String signature, InvokeContext context) {
		nextVisitor.visitSignature(sm, units, s, signature, context);
	}/*
	 * Label{GotoContext,IfContext,LookupSwitchContext,LookupSwitchDefaultContext
	 * ,TableSwitchContext,TableSwitchDefaultContext}
	 */

	public void visitLabel(SootMethod sm, Chain units, Stmt gotoStmt,
			Unit target, LabelContext context) {
		nextVisitor.visitLabel(sm, units, gotoStmt, target, context);
	}

	protected static boolean isThreadSubType(SootClass c) {
		if (c.getName().equals("java.lang.Thread"))
			return true;
		if (!c.hasSuperclass()) {
			return false;
		}
		return isThreadSubType(c.getSuperclass());
	}

	protected static boolean isRunnableSubType(SootClass c) {
		if (c.implementsInterface("java.lang.Runnable"))
			return true;
		if (c.hasSuperclass())
			return isRunnableSubType(c.getSuperclass());
		return false;
	}

	protected boolean isSubClass(SootClass c, String typeName) {
		if (c.getName().equals(typeName))
			return true;
		if (c.implementsInterface(typeName))
			return true;
		if (!c.hasSuperclass()) {
			return false;
		}
		return isSubClass(c.getSuperclass(), typeName);
	}



	public void setStaticReceiver(SootMethod sm, Chain units) {

	}



	public static int getSyncObjectIndex(Value v) {

		if (syncObjIndexMap.containsKey(v))
			return syncObjIndexMap.get(v);
		else {
			syncObjIndexMap.put(v, syncIndexCounter);
			return syncIndexCounter++;
		}

	}

	private static Stmt getFirstNonIdentityStmt(Chain units) {
		Stmt s = (Stmt) units.getFirst();
		while (s instanceof IdentityStmt)
			s = (Stmt) units.getSuccOf(s);
		return s;

	}

	private static Stmt getLastIdentityStmt(Chain units) {
		Stmt s = getFirstNonIdentityStmt(units);
		return (Stmt) units.getPredOf(s);

	}

	private static Stmt getMainThreadIdentityNameStmt(Chain units)
	{
		Stmt s = (Stmt) units.getFirst();
		while (true) {
			if (s.toString().contains("tname_main"))
				break;
			s = (Stmt) units.getSuccOf(s);
		}
		return s;
	}

	private static Stmt getMainThreadIdentityStmt(Chain units) {
		Stmt s = (Stmt) units.getFirst();
		while (true) {
			if (s.toString().contains("tid_main"))
				break;
			s = (Stmt) units.getSuccOf(s);
		}
		return s;
	}

	private static Stmt getRunThreadIdentityStmt(Chain units) {
		Stmt s = (Stmt) units.getFirst();
		while (true) {
			if (s.toString().contains("tid_run"))
				break;
			s = (Stmt) units.getSuccOf(s);
		}
		return s;
	}

	private static Stmt getRunThreadIdentityNameStmt(Chain units) {
		Stmt s = (Stmt) units.getFirst();
		while (true) {
			if (s.toString().contains("tname_run"))
				break;
			s = (Stmt) units.getSuccOf(s);
		}
		return s;
	}



	private static Stmt getThreadIdentityNameStmt(Chain units, String methodname) {
		Stmt s = (Stmt) units.getFirst();
		while (true) {
			if (s.toString().contains("tname_" + methodname))
				break;
			s = (Stmt) units.getSuccOf(s);
		}
		return s;
	}

	private static Stmt getThreadIdentityStmt(Chain units, String methodname) {
		Stmt s = (Stmt) units.getFirst();
		while (true) {
			if (s.toString().contains("tid_" + methodname))
				break;
			s = (Stmt) units.getSuccOf(s);
		}
		return s;
	}

	private static int getSPEIndex(Value v) {

		if (speIndexMap.containsKey(v))
			return speIndexMap.get(v);
		else {
			speIndexMap.put(v, indexCounter);
			return indexCounter++;
		}
	}

	public static Local getMethodThreadId(SootMethod sm) {
		if (Visitor.methodToThreadIdMap.get(sm) == null) {
			try {
				Body body = sm.retrieveActiveBody();
				Visitor.addLocalThreadId(body);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Visitor.methodToThreadIdMap.get(sm);
	}

	public static Local getMethodThreadName(SootMethod sm) {
		if (Visitor.methodToThreadIdMap.get(sm) == null) {
			try {
				Body body = sm.retrieveActiveBody();
				Visitor.addLocalThreadName(body);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Visitor.methodToThreadIdMap.get(sm);
	}



	/**
	 * Checks for the initialization of java.concurrency.Condition objects in the class method <clinit>/<init> 
	 * @param thisMethod
	 * @param units
	 */
	public static void checkForLockConditions(SootMethod thisMethod, Chain units) 
	{
		Value lockObj, condObj;
		Iterator stmtIt = units.snapshotIterator();  

		while (stmtIt.hasNext()) 
		{
			Stmt s = (Stmt) stmtIt.next();
			if (s.toString().contains("newCondition()"))
			{
				lockObj = ((AssignStmt)units.getPredOf(s)).getRightOp(); 
				condObj = ((AssignStmt)stmtIt.next()).getLeftOp();
				conditionToLockMap.put(condObj.toString(), lockObj.toString());
			}
		}
	}


	/**
	 * Checks for calls of synchronized methods inside the class method <clinit>/<init> (this is used to instrument beforeEnterMonitorStatic in the replay version).
	 * @param thisMethod
	 * @param units
	 */
	public static void checkForSyncMethods(SootMethod thisMethod, Chain units) 
	{
		Iterator stmtIt = units.snapshotIterator();  

		while (stmtIt.hasNext()) 
		{
			Stmt stmt = (Stmt) stmtIt.next();
			if(stmt instanceof AssignStmt)
			{
				Value s = ((AssignStmt) stmt).getRightOp();
				if(s instanceof StaticInvokeExpr)
				{
					SootMethod syncmethod = ((StaticInvokeExpr) s).getMethod();
					if(syncmethod.isSynchronized() && Parameters.isReplay && !syncmethod.getSignature().startsWith("<java."))	//** used to instrument calls to synchronized methods in the caller method
					{		
						String sigclass = syncmethod.getDeclaringClass().getName()+".OBJECT";//+"."+invokeExpr.getMethod().getName();
						Value memory = StringConstant.v(sigclass);

						LinkedList args = new LinkedList();

						args.addLast(IntConstant.v(Visitor.getSyncObjectIndex(memory)));
						args.addLast(getMethodThreadName(thisMethod));
						args.addLast(StringConstant.v("SYNCMETHOD"));
						args.addFirst(Visitor.addLocalCalleeClassHandler(thisMethod.retrieveActiveBody(),syncmethod));

						SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void beforeMonitorEnterStatic(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
						units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), stmt);

					}
				}
				else if(s instanceof InstanceInvokeExpr)
				{
					SootMethod syncmethod = ((InstanceInvokeExpr) s).getMethod();
					if(syncmethod.isSynchronized() && Parameters.isReplay && !syncmethod.getSignature().startsWith("<java."))	//** used to instrument calls to synchronized methods in the caller method
					{
						String sigclass = syncmethod.getDeclaringClass().getName()+".OBJECT";//+"."+invokeExpr.getMethod().getName();
						Value memory = StringConstant.v(sigclass);

						Value base = ((InstanceInvokeExpr) s).getBase();

						LinkedList args = new LinkedList();
						args.addLast(base);
						args.addLast(IntConstant.v(Visitor.getSyncObjectIndex(memory)));
						args.addLast(getMethodThreadName(thisMethod));
						args.addLast(StringConstant.v("SYNCMETHOD"));

						SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void beforeMonitorEnter(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
						units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), stmt);
					}
				}
			}
		}
	}

	/**
	 * Checks for thread.stard() invocations in the class method <init>
	 * @param thisMethod
	 * @param units
	 */
	public static void checkForThreadInit(SootMethod thisMethod, Chain units) 
	{
		Iterator stmtIt = units.snapshotIterator();  

		while (stmtIt.hasNext()) 
		{
			Stmt s = (Stmt) stmtIt.next();
			if(s instanceof InvokeStmt)
			{
				InvokeExpr expr = s.getInvokeExpr();
				String sig = expr.getMethod().getSubSignature();
				if(sig.contains("void start()") && isThreadSubType(expr.getMethod().getDeclaringClass()))
				{
					addCallstartRunThreadBefore(thisMethod, units, s, "startRunThreadBefore", ((InstanceInvokeExpr)expr).getBase());
				}
			}
		}
	}

	public void addCallReCrashWith(SootMethod sm, Chain units,
			AssignStmt assignStmt) {
		Stmt s1 = (Stmt) units.getSuccOf(assignStmt);
		Value e = s1.getInvokeExpr().getArg(0);
		LinkedList args = new LinkedList();
		args.addLast(e);
		SootMethodRef mr = Scene.v().getMethod(Parameters.CATCH_EXCEPTION_SIG)
				.makeRef();
		units.insertAfter(
				Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(mr, args)), s1);
	}

	public static void addLocalThreadId(Body body) {

		Chain units = body.getUnits();

		Local tid = Jimple.v().newLocal("tid_" + body.getMethod().getName(),
				LongType.v());
		Local thread_ = Jimple.v().newLocal(
				"thread_" + body.getMethod().getName(),
				RefType.v("java.lang.Thread"));

		body.getLocals().add(tid);
		methodToThreadIdMap.put(body.getMethod(), tid);

		body.getLocals().add(thread_);

		String methodSig1 = "<" + "java.lang.Thread"
				+ ": java.lang.Thread currentThread()>";

		SootMethodRef mr1 = Scene.v().getMethod(methodSig1).makeRef();

		Value staticInvoke = Jimple.v().newStaticInvokeExpr(mr1);

		AssignStmt newAssignStmt1 = Jimple.v().newAssignStmt(thread_,
				staticInvoke);

		String methodSig2 = "<" + "java.lang.Thread" + ": long getId()>";

		SootMethodRef mr2 = Scene.v().getMethod(methodSig2).makeRef();

		Value virtualInvoke = Jimple.v().newVirtualInvokeExpr(thread_, mr2);

		AssignStmt newAssignStmt2 = Jimple.v()
				.newAssignStmt(tid, virtualInvoke);

		Stmt insertStmt = getLastIdentityStmt(units);
		if (insertStmt != null)
			units.insertAfter(newAssignStmt2, insertStmt);
		else
			units.insertBefore(newAssignStmt2, getFirstNonIdentityStmt(units));

		units.insertBefore(newAssignStmt1, newAssignStmt2);
	}


	public static void addLocalThreadName(Body body) {

		Chain units = body.getUnits();

		Local tname = Jimple.v().newLocal("tname_" + body.getMethod().getName(),
				RefType.v("java.lang.String"));
		Local thread_ = Jimple.v().newLocal(
				"thread_" + body.getMethod().getName(),
				RefType.v("java.lang.Thread"));

		body.getLocals().add(tname);
		methodToThreadIdMap.put(body.getMethod(), tname);

		body.getLocals().add(thread_);

		String methodSig1 = "<" + "java.lang.Thread"
				+ ": java.lang.Thread currentThread()>";

		SootMethodRef mr1 = Scene.v().getMethod(methodSig1).makeRef();

		Value staticInvoke = Jimple.v().newStaticInvokeExpr(mr1);

		AssignStmt newAssignStmt1 = Jimple.v().newAssignStmt(thread_,
				staticInvoke);

		String methodSig2 = "<" + "java.lang.Thread" + ": java.lang.String getName()>";

		SootMethodRef mr2 = Scene.v().getMethod(methodSig2).makeRef();

		Value virtualInvoke = Jimple.v().newVirtualInvokeExpr(thread_, mr2);

		AssignStmt newAssignStmt2 = Jimple.v()
				.newAssignStmt(tname, virtualInvoke);

		Stmt insertStmt = getLastIdentityStmt(units);
		if (insertStmt != null)
			units.insertAfter(newAssignStmt2, insertStmt);
		else
			units.insertBefore(newAssignStmt2, getFirstNonIdentityStmt(units));

		units.insertBefore(newAssignStmt1, newAssignStmt2);
	}


	/**
	 * Inserts a Local containing a runtime handler for the class (used when dealing with synchronized static methods to get the class monitor object)  
	 * @param body
	 */
	public static Local addLocalClassHandler(Body body)
	{
		Chain units = body.getUnits();

		Local classMonitor = Jimple.v().newLocal("classMonitor_" + body.getMethod().getName(), RefType.v("java.lang.Object"));
		body.getLocals().add(classMonitor);

		SootMethod appMethod = body.getMethod();
		SootClass appClass = appMethod.getDeclaringClass();

		AssignStmt newAssignStmt1 = Jimple.v().newAssignStmt(classMonitor,soot.jimple.ClassConstant.v(appClass.getName().replaceAll("\\.", "/")));
		Stmt insertStmt = getLastIdentityStmt(units);
		if (insertStmt != null)
			units.insertAfter(newAssignStmt1, insertStmt);
		else
			units.insertBefore(newAssignStmt1, getFirstNonIdentityStmt(units)); 

		return classMonitor;
	}


	/**
	 * Inserts a Local containing a runtime handler for the callee class (used to get the callee class monitor object when dealing with the invoke of synchronized methods of other classes)  
	 * @param body
	 */
	public static Local addLocalCalleeClassHandler(Body callerbody, SootMethod calleemethod)
	{
		Chain units = callerbody.getUnits();

		Local classMonitor = Jimple.v().newLocal("classMonitor_" + calleemethod.getName(), RefType.v("java.lang.Object"));
		callerbody.getLocals().add(classMonitor);

		SootClass calleeAppClass = calleemethod.getDeclaringClass();

		AssignStmt newAssignStmt1 = Jimple.v().newAssignStmt(classMonitor,soot.jimple.ClassConstant.v(calleeAppClass.getName().replaceAll("\\.", "/")));

		Stmt insertStmt = getLastIdentityStmt(units);
		if (insertStmt != null)
			units.insertAfter(newAssignStmt1, insertStmt);
		else
			units.insertBefore(newAssignStmt1, getFirstNonIdentityStmt(units));

		return classMonitor;
	}



	public static void addCallMainMethodEnterInsert(SootMethod sm, Chain units) {

		LinkedList args = new LinkedList();
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(sm.getDeclaringClass().getName()+"."+sm.getName()));
		args.addLast(((IdentityStmt)units.getFirst()).getLeftOp());
		String methodSig ="<" + observerClass +": void mainThreadStartRun(java.lang.String,java.lang.String,java.lang.String[])>";

		SootMethodRef mr = Scene.v().getMethod(methodSig).makeRef();
		Value staticInvoke = Jimple.v().newStaticInvokeExpr(mr, args);    
		units.insertAfter(Jimple.v().newInvokeStmt(staticInvoke), getMainThreadIdentityNameStmt(units));
	}


	public static void addCallRunMethodEnterInsert(SootMethod sm, Chain units) {

		LinkedList args = new LinkedList();
		args.addLast(getMethodThreadName(sm));
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + "threadStartRun" + "(java.lang.String)>").makeRef();
		Value staticInvoke = Jimple.v().newStaticInvokeExpr(mr, args);
		units.insertAfter(Jimple.v().newInvokeStmt(staticInvoke),getRunThreadIdentityNameStmt(units));

	}

	public static void addCallRunMethodExitBefore(SootMethod sm, Chain units, Stmt returnVoidStmt) {

		LinkedList args = new LinkedList();
		args.addLast(getMethodThreadName(sm));
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + "threadExitRun" + "(java.lang.String)>").makeRef();
		Value staticInvoke = Jimple.v().newStaticInvokeExpr(mr, args);
		units.insertBefore(Jimple.v().newInvokeStmt(staticInvoke), returnVoidStmt);
		// Iterator stmtIt = units.snapshotIterator();
		// while (stmtIt.hasNext())
		// {
		// Stmt s = (Stmt) stmtIt.next();
		// if(s instanceof ReturnVoidStmt)
		// units.insertBefore(Jimple.v().newInvokeStmt(staticInvoke), s);
		// }
	}

	public static void addCallCatchException(Body body) {
		Chain units = body.getUnits();

		Local l_r0, l_r1;
		l_r0 = Jimple.v().newLocal("$exp_r0", RefType.v("java.lang.Throwable"));
		l_r1 = Jimple.v().newLocal("exp_r1", RefType.v("java.lang.Throwable"));
		body.getLocals().add(l_r0);
		body.getLocals().add(l_r1);

		Unit beginStmt = null;
		Unit returnStmt = null;
		Unit endStmt = null;
		Stmt s = null;
		Iterator stmtIt = units.snapshotIterator();

		boolean oops = true;
		while (stmtIt.hasNext()) {
			s = (Stmt) stmtIt.next();

			if (s instanceof IdentityStmt)
				continue;
			else {
				if (oops) {
					beginStmt = s;
					oops = false;
				}
				if (s instanceof ReturnVoidStmt)
					returnStmt = s;
			}
		}

		if (returnStmt == null)
			return;

		Stmt handlerStart = Jimple.v().newIdentityStmt(l_r0,
				Jimple.v().newCaughtExceptionRef());
		units.insertBefore(handlerStart, returnStmt);

		units.insertBefore(Jimple.v().newAssignStmt(l_r1, l_r0), returnStmt);
		LinkedList args = new LinkedList();
		args.addLast(l_r1);
		SootMethodRef mr = Scene.v().getMethod(Parameters.CATCH_EXCEPTION_SIG)
				.makeRef();
		units.insertBefore(
				Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(mr, args)), returnStmt);

		endStmt = Jimple.v().newGotoStmt(returnStmt);
		units.insertBefore(endStmt, handlerStart);
		SootClass exceptionClass = Scene.v()
				.getSootClass("java.lang.Exception");
		body.getTraps().add(
				Jimple.v().newTrap(exceptionClass, beginStmt, endStmt,
						handlerStart));

		body.validate();
	}


	/**
	 * Wraps each access to a shared program element instance (SPE)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodNameBefore
	 * @param spe
	 */
	public static void addCallAccessSPEInstance(SootMethod sm, Chain units, Stmt s, String methodNameBefore, Value spe) {

		if(!Parameters.isLEAPmode)	//** if isLEAPmode, then we add a static identifier for each SPE, otherwise we add a reference to the object instance	
		{
			Value base = ((InstanceFieldRef)s.getFieldRef()).getBase();
			int fieldId = getSPEIndex(spe); //value used to differentiate between different fields of the same object
			LinkedList args = new LinkedList();
			args.addLast(base);
			args.addLast(IntConstant.v(fieldId));
			args.addLast(getMethodThreadName(sm));
			String pattern = "(before|after)(Load|Store)";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(methodNameBefore);
			if (m.find()){
				//System.out.println("STMT: "+s);
				String type = s.getDefBoxes().get(0).getValue().getType().toString();
				//System.out.println("type (read): "+type);
				Value value = ((AssignStmt) s).getLeftOp();
				//for write operations, we have to obtain the right operand
				if(methodNameBefore.contains("Store")){
					value = ((AssignStmt) s).getRightOp();
					type = value.getType().toString();
					//System.out.println("type (write): "+type);
					if(type.contains("null")) //do not support null_type
						return;
				}
				args.addLast(value); 
				//System.out.println("value added to args: "+value);			

				SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,int,java.lang.String,java.lang.Object)>").makeRef();
				String methodNameAfter = methodNameBefore.replace("before", "after");
				SootMethodRef mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(java.lang.Object,int,java.lang.String,java.lang.Object)>").makeRef();
				
				//methods for general value types
				mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(java.lang.Object,int,java.lang.String,"+type+")>").makeRef();
				mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,int,java.lang.String,"+type+")>").makeRef();
				
				//System.out.println("BEFORE: "+mrBefore);
				//System.out.println("AFTER: "+mrAfter+"\n");
				if(methodNameBefore.contains("Store")){
					//for stores we only instrument with the value before the write operation
					units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s); 

					//we don't instrument the after methods for the OREO runtime version
					if(!(Parameters.isOREOmode && Parameters.isRuntime)){
						args.removeLast();
						mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(java.lang.Object,int,java.lang.String)>").makeRef();
						units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter,args)), s); 
					}
				}
				else{ 
					//for loads we only instrument with the value after the read operation 
					units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter,args)), s); 

					//instrument beforeLoad only for replay version
					if(Parameters.isReplay){
						args.removeLast();
						mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,int,java.lang.String)>").makeRef();
						units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s); 
					}
				}
			}
		}
		else 
			addCallAccessSPEStatic(sm, units, s, methodNameBefore, spe);

	}

	/**
	 * Wraps each access to a static shared program element (SPE)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodNameBefore
	 * @param spe
	 */
	public static void addCallAccessSPEStatic(SootMethod sm, Chain units, Stmt s, String methodNameBefore, Value v) {

		LinkedList args = new LinkedList();
		args.addLast(IntConstant.v(getSPEIndex(v)));
		args.addLast(getMethodThreadName(sm)); 
		String pattern = "(before|after)(Load|Store)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(methodNameBefore);
		if (m.find()){
			String type = s.getDefBoxes().get(0).getValue().getType().toString();
			//System.out.println("STMT: "+s);
			//System.out.println("type (read): "+type);
			Value value = ((AssignStmt) s).getLeftOp();

			//for write operations, we have to obtain the right operand
			if(methodNameBefore.contains("Store")){
				value = ((AssignStmt) s).getRightOp();
				type = value.getType().toString();
				//System.out.println("type (write): "+type);
			}

			//Value value = s.getDefBoxes().get(0).getValue();
			args.addLast((Object)value);
			//System.out.println("value added to args: "+value);
			//System.out.println("-------END: Analyzing value from Stmt-------");
			//SootMethodRef mrBefore = 

			SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(int,java.lang.String,java.lang.Object)>").makeRef();
			String methodNameAfter = methodNameBefore.replace("before", "after");
			SootMethodRef mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(int,java.lang.String,java.lang.Object)>").makeRef();

			//methods for general value types
			mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(int,java.lang.String,"+type+")>").makeRef();
			mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(int,java.lang.String,"+type+")>").makeRef();
			
			if(methodNameBefore.contains("Store")){
				//for stores we only instrument with the value before the write operation
				units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s); 

				//we don't instrument the after methods for the OREO runtime version
				if(!(Parameters.isOREOmode && Parameters.isRuntime)){
					args.removeLast();
					mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(int,java.lang.String)>").makeRef();
					units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter,args)), s); 
				}
			}
			else{ 
				//for loads we only instrument with the value after the read operation 
				units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter,args)), s); 

				//instrument beforeLoad only for replay version
				if(Parameters.isReplay){
					args.removeLast();
					mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(int,java.lang.String)>").makeRef();
					units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s); 
				}
			}
		} 
	}

	/**
	 * Instruments accesses to synchronization variables (locks).
	 * Method injected is afterMonitorEnter(Object o, int monitorId, String threadId) and beforeMonitorEnter(Object o, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodName
	 * @param obj
	 * @param spe
	 */
	public static void addCallAccessSyncLock(SootMethod sm, Chain units, Stmt s, String methodName, Value obj, Value spe, String monitorName) {
		LinkedList args = new LinkedList();
		args.addLast(obj);
		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));

		SootMethodRef mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter, args)), s);

		if (Parameters.isReplay) 
		{
			String methodNameBefore = methodName.replace("after","before");
			SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s);
		}
	}

	/**
	 * Instruments accesses to synchronization variables (condition signal).
	 * Method injected is afterConditionEnter(Object lock, Object condition, int monitorId, String threadId) and beforeConditionEnter(Object lock, Object condition, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodName
	 * @param obj
	 * @param spe
	 */
	public static void addCallAccessSyncSignal(SootMethod sm, Chain units, Stmt s, String methodName, Value lock, Value cond, Value spe, String monitorName) {

		LinkedList args = new LinkedList();
		args.addLast(lock);
		args.addLast(cond);
		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));

		//if(Parameters.isRuntime){
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Object,java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);

		if (Parameters.isReplay) 
		{
			String methodNameAfter = methodName.replace("before","after");
			SootMethodRef mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(java.lang.Object,java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
			units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter, args)), s);
		}
	}


	/**
	 * Instruments accesses to synchronization variables (notify and condition signal).
	 * Method injected is afterMonitorEnter(Object o, int monitorId, String threadId) and beforeMonitorEnter(Object o, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodName
	 * @param obj
	 * @param spe
	 */
	public static void addCallAccessSyncNotify(SootMethod sm, Chain units, Stmt s, String methodName, Value obj, Value spe, String monitorName) {

		LinkedList args = new LinkedList();
		args.addLast(obj);
		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));

		//if(Parameters.isRuntime){
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);

		if (Parameters.isReplay) 
		{
			String methodNameAfter = methodName.replace("before","after");
			SootMethodRef mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodNameAfter + "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
			units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter, args)), s);
		}
	}


	/**
	 * Instruments accesses to synchronization variables (condition awaits).
	 * Method injected is afterConditionEnter(Object lock, Object condition, int monitorId, String threadId) and beforeConditionEnter(Object lock, Object condition, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodName
	 * @param obj
	 * @param spe
	 */
	public static void addCallAccessSyncAwait(SootMethod sm, Chain units, Stmt s, String methodName, Value lock, Value cond, Value spe, String monitorName) {

		LinkedList args = new LinkedList();
		args.addLast(lock);
		args.addLast(cond);
		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));

		//if(Parameters.isRuntime){
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Object,java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
		//}

		if(Parameters.isReplay)
		{
			String methodNameBefore = methodName.replace("after","before");
			SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s);
		}
	}



	/**
	 * Instruments accesses to synchronization variables (waits and condition awaits).
	 * Method injected is afterMonitorEnter(Object o, int monitorId, String threadId) and beforeMonitorEnter(Object o, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodName
	 * @param obj
	 * @param spe
	 */
	public static void addCallAccessSyncWait(SootMethod sm, Chain units, Stmt s, String methodName, Value obj, Value spe, String monitorName) {

		LinkedList args = new LinkedList();
		args.addLast(obj);
		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));

		//if(Parameters.isRuntime){
		SootMethodRef mrAfter = Scene.v().getMethod("<" + observerClass + ": void " + methodName + "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrAfter, args)), s);
		//}
		if(Parameters.isReplay)
		{
			String methodNameBefore = methodName.replace("after","before");
			SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s);
		}
	}


	/**
	 * Instruments accesses to synchronization variables (java monitors).
	 * Method injected is afterMonitorEnter(Object o, int monitorId, String threadId) and beforeMonitorEnter(Object o, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param s
	 * @param methodName
	 * @param spe
	 */
	public static void addCallAccessSyncObj(SootMethod sm, Chain units, Stmt s, String methodName, Value obj, Value spe, String monitorName) {

		LinkedList args = new LinkedList();
		args.addLast(obj);
		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));

		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);

		if (Parameters.isReplay) 
		{
			String methodNameBefore = methodName.replace("after","before");
			SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), s);
		}

	}

	/**
	 * Instruments accesses to synchronization variables (java monitors).
	 * Method injected is afterMonitorEnter(Object o, int monitorId, String threadId) and beforeMonitorEnter(Object o, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param methodName
	 * @param obj
	 * @param spe
	 */
	public static void addCallJavaMonitorEntryInstance(SootMethod sm, Chain units, String methodName, Value obj, Value spe, String monitorName) {

		LinkedList args = new LinkedList();
		args.addLast(obj);
		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));

		Stmt s = getThreadIdentityNameStmt(units, sm.getName());
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);

		/*if (Parameters.isReplay) 	//** the before statement is already inserted in SymberVisitor2 line 151
		{
			String methodNameBefore = methodName.replace("after","before");
			SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), units.getSuccOf(s));
		}*/

	}

	/**
	 * Instruments accesses to synchronization variables (java monitors).
	 * Method injected is afterMonitorEnter(Object o, int monitorId, String threadId) and beforeMonitorEnter(Object o, int monitorId, String threadId)
	 * @param sm
	 * @param units
	 * @param methodName
	 * @param spe
	 */
	public static void addCallJavaMonitorEntryStatic(SootMethod sm, Chain units,String methodName, Value spe, String monitorName) {

		LinkedList args = new LinkedList();

		args.addLast(IntConstant.v(getSyncObjectIndex(spe)));
		args.addLast(getMethodThreadName(sm));
		args.addLast(StringConstant.v(monitorName));
		args.addFirst(addLocalClassHandler(sm.retrieveActiveBody())); //** gets the class object

		Stmt s = getThreadIdentityNameStmt(units, sm.getName());

		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Object,int,java.lang.String,java.lang.String)>").makeRef();
		units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);

		/*if (Parameters.isReplay) 	//** the before statement is already inserted in SymberVisitor2 line 166
		{
			String methodNameBefore = methodName.replace("after","before");
			SootMethodRef mrBefore = Scene.v().getMethod("<" + observerClass + ": void " + methodNameBefore + "(int,java.lang.String,java.lang.String)>").makeRef();
			units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mrBefore, args)), units.getSuccOf(s));
		}*/
	}

	public static void addCallMonitorEntry(Body body) {
		SootMethod appMethod = body.getMethod();
		SootClass appClass = appMethod.getDeclaringClass();
		Chain units = body.getUnits();
		String sig;
		Value memory;
		Value base;

		sig = appClass.getName() + ".OBJECT";// +"."+invokeExpr.getMethod().getName();
		memory = StringConstant.v(sig);


		if (appMethod.isStatic()) 
		{
			Visitor.addCallJavaMonitorEntryStatic(appMethod, units, "afterMonitorEnterStatic", memory, "SYNCMETHOD");
		} 
		else 
		{
			Stmt firstStmt = (Stmt) units.getFirst();
			if (firstStmt instanceof IdentityStmt) {
				base = ((IdentityStmt) firstStmt).getLeftOp();
				Visitor.addCallJavaMonitorEntryInstance(appMethod, units, "afterMonitorEnter", base, memory, "SYNCMETHOD");
			}
		}
		Visitor.instrusharedaccessnum++;
		Visitor.totalaccessnum++;
	}


	/**
	 * Instruments starRunThreadBefore(Thread t, String threadName) 
	 */
	public static void addCallstartRunThreadBefore(SootMethod sm, Chain units, Stmt s, String methodName, Value v) {
		LinkedList args = new LinkedList();
		args.addLast(v);
		args.addLast(getMethodThreadName(sm));
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Thread,java.lang.String)>").makeRef();
		units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
	}


	public static void addCallJoinRunThreadAfter(SootMethod sm, Chain units, Stmt s, String methodName, Value v) {
		LinkedList args = new LinkedList();
		args.addLast(v);
		args.addLast(getMethodThreadName(sm));
		SootMethodRef mr = Scene.v().getMethod("<" + observerClass + ": void " + methodName	+ "(java.lang.Thread,java.lang.String)>").makeRef();
		units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(mr, args)), s);
	}


	public static void writingLog(String line) {
		try {

			File file = new File(logPath + "/SPEaccesses.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(line);
			bw.newLine();
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
