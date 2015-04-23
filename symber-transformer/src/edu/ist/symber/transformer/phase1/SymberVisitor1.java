package edu.ist.symber.transformer.phase1;

import edu.ist.symber.transformer.Visitor;
import edu.ist.symber.transformer.contexts.InvokeContext;
import edu.ist.symber.transformer.contexts.RHSContextImpl;
import edu.ist.symber.transformer.contexts.RefContext;
import soot.*;
import soot.jimple.*;
import soot.util.*;

public class SymberVisitor1 extends Visitor {

	public SymberVisitor1(Visitor visitor) {
		super(visitor);
	}

	public void visitStmtAssign(SootMethod sm, Chain units, AssignStmt assignStmt) {
		nextVisitor.visitStmtAssign(sm, units, assignStmt);
	}

	public void visitStmtEnterMonitor(SootMethod sm, Chain units, EnterMonitorStmt enterMonitorStmt) {
		nextVisitor.visitStmtEnterMonitor(sm, units, enterMonitorStmt);
	}

	public void visitStmtExitMonitor(SootMethod sm, Chain units, ExitMonitorStmt exitMonitorStmt) {

		nextVisitor.visitStmtExitMonitor(sm, units, exitMonitorStmt);
	}

	/** Although synchronized instance method invocation and static method invocation
	 *  target at different locks,
	 * we still use the same SPE for them
	 */
	public void visitInstanceInvokeExpr(SootMethod sm, Chain units, Stmt s, InstanceInvokeExpr invokeExpr, InvokeContext context) {

		nextVisitor.visitInstanceInvokeExpr(sm, units, s, invokeExpr, context);

	}

	public void visitStaticInvokeExpr(SootMethod sm, Chain units, Stmt s, StaticInvokeExpr invokeExpr, InvokeContext context) {

		nextVisitor.visitStaticInvokeExpr(sm, units, s, invokeExpr, context);   
	}


	public void visitArrayRef(SootMethod sm, Chain units, Stmt s, ArrayRef arrayRef, RefContext context) {
		nextVisitor.visitArrayRef(sm, units, s, arrayRef, context);
	}

	public void visitInstanceFieldRef(SootMethod sm, Chain units, Stmt s, InstanceFieldRef instanceFieldRef, RefContext context) {

		String sig = instanceFieldRef.getField().getDeclaringClass().getName()+"."+instanceFieldRef.getField().getName()+".INSTANCE";

		//write instance field & handle array ref
		if (context != RHSContextImpl.getInstance())	//** CHANGE this may cut off some shared vars
		{
			if((/*Visitor.ftea.isFieldThreadShared(instanceFieldRef.getField()) &&*/ !Visitor.tlo.isObjectThreadLocal(instanceFieldRef, sm)) || sig.contains("TableDescriptor.referencedColumnMap"))	//** TableDescriptor.referencedColumnMap is for derby2861 bug
			{
				//if(!sig.contains("java.util.concurrent.locks"))	//** we don't want to trace locks
				
					sharedVariableWriteAccessSet.add(sig);
				 ///////
    			String shortclass = sm.getDeclaringClass().toString();
    			if(shortclass.contains("$"))
    				shortclass = shortclass.substring(0,shortclass.indexOf("$"));
    			String tag;
    			
    			if(s.getTag("LineNumberTag")!=null)
    				tag = s.getTag("LineNumberTag").toString();
    			else
    				tag = "0";
    			String line =  shortclass + "."
    					+ instanceFieldRef.getField().getName() + "@"
    					+ tag
    					;//+ " instance";
    			System.out.println("[SymbiosisTransformer] instance shared access: "+line);
    			///////
			}
			//System.err.println("---> is field shared "+instanceFieldRef.getField()+"? "+Visitor.ftea.isFieldThreadShared(instanceFieldRef.getField()));
			//System.err.println("---> is field local "+instanceFieldRef.getField()+"? "+Visitor.tlo.isObjectThreadLocal(instanceFieldRef, sm));
		}
		nextVisitor.visitInstanceFieldRef(sm, units, s, instanceFieldRef, context);
	}

	public void visitStaticFieldRef(SootMethod sm, Chain units, Stmt s, StaticFieldRef staticFieldRef, RefContext context) {

		String sig = staticFieldRef.getField().getDeclaringClass().getName()+"."+staticFieldRef.getField().getName()+".STATIC";

		//write static field & handle array ref
		if (context != RHSContextImpl.getInstance()) //** CHANGE this may cut off some shared vars
		{
			if(/*Visitor.ftea.isFieldThreadShared(staticFieldRef.getField()) &&*/ !Visitor.tlo.isObjectThreadLocal(staticFieldRef, sm))	//** TableDescriptor.referencedColumnMap is for derby2861 bug
			{
				//if(!sig.contains("java.util.concurrent.locks"))	//** we don't want to trace locks
				sharedVariableWriteAccessSet.add(sig);	
				 ///////
    			String shortclass = sm.getDeclaringClass().toString();
    			if(shortclass.contains("$"))
    				shortclass = shortclass.substring(0,shortclass.indexOf("$"));
    			String tag;
    			
    			if(s.getTag("LineNumberTag")!=null)
    				tag = s.getTag("LineNumberTag").toString();
    			else
    				tag = "0";
    			String line =  shortclass + "."
    					+ staticFieldRef.getField().getName() + "@"
    					+ tag
    					;//+ " instance";
    			System.out.println("[SymbiosisTransformer] static shared access: "+line);
    			///////
			}
			// System.out.println("---> is field shared "+staticFieldRef.getField()+"? "+Visitor.ftea.isFieldThreadShared(staticFieldRef.getField()));
		}
		nextVisitor.visitStaticFieldRef(sm, units, s, staticFieldRef, context);
	}

}
