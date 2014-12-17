package edu.ist.symber.transformer.phase2;

import java.util.Iterator;
import java.util.LinkedList;

import edu.ist.symber.Parameters;
import edu.ist.symber.Util;
import edu.ist.symber.transformer.Visitor;
import edu.ist.symber.transformer.contexts.*;
import soot.ArrayType;
import soot.Body;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Value;
import soot.jimple.*;
import soot.util.Chain;

public class SymberVisitor2 extends Visitor {

	public SymberVisitor2(Visitor visitor) {
		super(visitor);
	}

	public void visitStmtAssign(SootMethod sm, Chain units,
			AssignStmt assignStmt) {
		nextVisitor.visitStmtAssign(sm, units, assignStmt);
	}

	public void visitStmtEnterMonitor(SootMethod sm, Chain units,
			EnterMonitorStmt enterMonitorStmt) {
		Visitor.sharedaccessnum++;
		Visitor.totalaccessnum++;
		Visitor.instrusharedaccessnum++;

		Value op = enterMonitorStmt.getOp();
		Type type = op.getType();
		String sig = type.toString() + ".OBJECT";

		Value memory = StringConstant.v(sig);

		Visitor.addCallAccessSyncObj(sm, units, enterMonitorStmt,
				"afterMonitorEnter", op, memory, "SYNCBLOCK");// */

		nextVisitor.visitStmtEnterMonitor(sm, units, enterMonitorStmt);
	}

	public void visitStmtExitMonitor(SootMethod sm, Chain units,
			ExitMonitorStmt exitMonitorStmt) {
		nextVisitor.visitStmtExitMonitor(sm, units, exitMonitorStmt);
	}

	public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s,
			InstanceInvokeExpr invokeExpr, InvokeContext context) {
		String sigclass = invokeExpr.getMethod().getDeclaringClass().getName()
				+ ".OBJECT";
		Value memory = StringConstant.v(sigclass);

		Value base = invokeExpr.getBase();
		String sig = invokeExpr.getMethod().getSubSignature();

		if (sig.equals("void lock()")) // ** handle locks
		{
			Visitor.addCallAccessSyncLock(sm, units, s, "afterMonitorEnter",
					base, memory, "LOCK");
			Visitor.instrusharedaccessnum++;
			Visitor.sharedaccessnum++;
			Visitor.totalaccessnum++;
		} else if (sig.equals("void await()")
				|| sig.equals("void awaitNanos(long)")) // ** handles condition
														// await
		{
			System.out.println("\ts : " + s);
			Value cond = ((AssignStmt) units.getPredOf(s)).getRightOp();
			String lockSig = Visitor.conditionToLockMap.get(cond.toString());
			Value lock = null;

			// ** get the object for the lock
			Iterator stmtIt = units.snapshotIterator();
			while (stmtIt.hasNext()) {
				Stmt stmp = (Stmt) stmtIt.next();
				if (stmp instanceof AssignStmt
						&& stmp.toString().contains(lockSig)) {
					lock = ((AssignStmt) stmp).getLeftOp();
					break;
				}
			}

			Visitor.addCallAccessSyncAwait(sm, units, s, "afterConditionEnter",
					lock, base, memory, "AWAIT");
			Visitor.instrusharedaccessnum++;
			Visitor.sharedaccessnum++;
			Visitor.totalaccessnum++;

			if (Parameters.isReplay && Parameters.removeSync)
				units.remove(s);
		} else if (sig.equals("void wait()") || sig.equals("void wait(long)")
				|| sig.equals("void wait(long,int)")) // ** handles monitor wait
		{
			Visitor.addCallAccessSyncWait(sm, units, s, "afterMonitorEnter",
					base, memory, "WAIT");
			Visitor.instrusharedaccessnum++;
			Visitor.sharedaccessnum++;
			Visitor.totalaccessnum++;

			if (Parameters.isReplay && Parameters.removeSync)
				units.remove(s);// */
		} else if (sig.equals("void signal()")
				|| sig.equals("void signalAll()")) // ** handles condition
													// signal
		{
			System.out.println("\ts : " + s);
			Value cond = ((AssignStmt) units.getPredOf(s)).getRightOp();
			String lockSig = Visitor.conditionToLockMap.get(cond.toString());
			Value lock = null;

			// ** get the object for the lock
			Iterator stmtIt = units.snapshotIterator();
			while (stmtIt.hasNext()) {
				Stmt stmp = (Stmt) stmtIt.next();
				if (stmp instanceof AssignStmt
						&& stmp.toString().contains(lockSig)) {
					lock = ((AssignStmt) stmp).getLeftOp();
					break;
				}
			}

			Visitor.addCallAccessSyncSignal(sm, units, s,
					"beforeConditionEnter", lock, base, memory, "SIGNAL");
			Visitor.instrusharedaccessnum++;
			Visitor.sharedaccessnum++;
			Visitor.totalaccessnum++;

			if (Parameters.isReplay && Parameters.removeSync)
				units.remove(s);// */

		} else if (sig.equals("void notify()")
				|| sig.equals("void notifyAll()")) // ** handles monitor notify
		{
			Visitor.addCallAccessSyncNotify(sm, units, s, "beforeMonitorEnter",
					base, memory, "NOTIFY");
			Visitor.instrusharedaccessnum++;
			Visitor.sharedaccessnum++;
			Visitor.totalaccessnum++;

			if (Parameters.isReplay && Parameters.removeSync)
				units.remove(s);// */

		} else if (sig.equals("void start()")
				&& isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
			Visitor.addCallstartRunThreadBefore(sm, units, s,
					"startRunThreadBefore", invokeExpr.getBase());
		} else if ((sig.equals("void join()") || sig.equals("void join(long)") || sig
				.equals("void join(long,int)"))
				&& isThreadSubType(invokeExpr.getMethod().getDeclaringClass())) {
			Visitor.addCallJoinRunThreadAfter(sm, units, s,
					"joinRunThreadAfter", invokeExpr.getBase());
		} else if (invokeExpr.getMethod().isSynchronized()
				&& Parameters.isReplay
				&& !invokeExpr.getMethod().getSignature().startsWith("<java.")) // **
																				// used
																				// to
																				// instrument
																				// calls
																				// to
																				// synchronized
																				// methods
																				// in
																				// the
																				// caller
																				// method
		{
			LinkedList args = new LinkedList();
			args.addLast(base);
			args.addLast(IntConstant.v(Visitor.getSyncObjectIndex(memory)));
			args.addLast(getMethodThreadName(sm));
			args.addLast(StringConstant.v("SYNCMETHOD"));

			SootMethodRef mr = Scene
					.v()
					.getMethod(
							"<"
									+ observerClass
									+ ": void beforeMonitorEnter(java.lang.Object,int,java.lang.String,java.lang.String)>")
					.makeRef();
			units.insertBefore(
					Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(mr, args)), s);

		}
		nextVisitor.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);
	}

	public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s,
			StaticInvokeExpr invokeExpr, InvokeContext context) {

		// ** if we are dealing with accesses to sync methods from classes of
		// Java libraries, we do not instrument them because soot does not
		// analyze the JDK libraries (which could cause a deadlock)
		if (invokeExpr.getMethod().isSynchronized() && Parameters.isReplay
				&& !invokeExpr.getMethod().getSignature().startsWith("<java.")) // **
																				// used
																				// to
																				// instrument
																				// calls
																				// to
																				// synchronized
																				// methods
																				// in
																				// the
																				// caller
																				// method
		{
			String sigclass = invokeExpr.getMethod().getDeclaringClass()
					.getName()
					+ ".OBJECT";
			Value memory = StringConstant.v(sigclass);

			LinkedList args = new LinkedList();

			args.addLast(IntConstant.v(Visitor.getSyncObjectIndex(memory)));
			args.addLast(getMethodThreadName(sm));
			args.addLast(StringConstant.v("SYNCMETHOD"));
			args.addFirst(Visitor.addLocalClassHandler(sm.retrieveActiveBody()));

			SootMethodRef mr = Scene
					.v()
					.getMethod(
							"<"
									+ observerClass
									+ ": void beforeMonitorEnterStatic(java.lang.Object,int,java.lang.String,java.lang.String)>")
					.makeRef();
			units.insertBefore(
					Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(mr, args)), s);

		}

		nextVisitor.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);
	}

	public void visitArrayRef(SootMethod sm, Chain units, Stmt s,
			ArrayRef arrayRef, RefContext context) {

		nextVisitor.visitArrayRef(sm, units, s, arrayRef, context);
	}

	public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s,
			InstanceFieldRef instanceFieldRef, RefContext context) {
		Visitor.totalaccessnum++;

		String sig = instanceFieldRef.getField().getDeclaringClass().getName()
				+ "." + instanceFieldRef.getField().getName() + ".INSTANCE";
		Value memory = StringConstant.v(sig);

		if (!instanceFieldRef.getField().isFinal()) {
			if (Visitor.sharedVariableWriteAccessSet.contains(sig)
					&& Parameters.traceSharedMem) // && !Visitor.isWrappedByLock
			{
				// ** handle loads and stores
				String methodname = "beforeLoad";

				if (context != RHSContextImpl.getInstance()) {
					methodname = "beforeStore";
				} else if (instanceFieldRef.getField().getType() instanceof ArrayType) {
					Stmt nextStmt = (Stmt) units.getSuccOf(s);
					if (s instanceof AssignStmt
							&& nextStmt instanceof AssignStmt) {
						AssignStmt assgnStmt = (AssignStmt) s;
						AssignStmt assgnNextStmt = (AssignStmt) nextStmt;
						if (assgnNextStmt.getLeftOp().toString()
								.contains(assgnStmt.getLeftOp().toString())) {
							methodname = "beforeStore";
						}
					}
				}
				Visitor.addCallAccessSPEInstance(sm, units, s, methodname,
						memory);

				Visitor.sharedaccessnum++;
				Visitor.instrusharedaccessnum++;
			}
		}
		nextVisitor.visitInstanceFieldRef(sm, units, s, instanceFieldRef,
				context);
	}

	public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s,
			StaticFieldRef staticFieldRef, RefContext context) {
		Visitor.totalaccessnum++;
		String sig = staticFieldRef.getField().getDeclaringClass().getName()
				+ "." + staticFieldRef.getField().getName() + ".STATIC";
		Value memory = StringConstant.v(sig);

		if (!staticFieldRef.getField().isFinal()) {
			if (Visitor.sharedVariableWriteAccessSet.contains(sig)
					&& Parameters.traceSharedMem) {
				// ** handle loads and stores
				String methodname = "beforeLoad";

				if (context != RHSContextImpl.getInstance()) {
					methodname = "beforeStore";
				} else if (staticFieldRef.getField().getType() instanceof ArrayType) {
					Stmt nextStmt = (Stmt) units.getSuccOf(s);
					if (s instanceof AssignStmt
							&& nextStmt instanceof AssignStmt) {
						AssignStmt assgnStmt = (AssignStmt) s;
						AssignStmt assgnNextStmt = (AssignStmt) nextStmt;
						if (assgnNextStmt.getLeftOp().toString()
								.contains(assgnStmt.getLeftOp().toString())) {
							methodname = "beforeStore";
						}
					}
				}

				Visitor.addCallAccessSPEStatic(sm, units, s, methodname, memory);

				Visitor.sharedaccessnum++;
				Visitor.instrusharedaccessnum++;
			}
		}
		nextVisitor.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
	}
}
