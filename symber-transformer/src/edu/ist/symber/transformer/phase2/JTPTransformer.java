package edu.ist.symber.transformer.phase2;

import java.util.Iterator;
import java.util.Map;

import edu.ist.symber.Parameters;
import edu.ist.symber.Util;
import edu.ist.symber.transformer.*;
import soot.Body;
import soot.BodyTransformer;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;

public class JTPTransformer extends BodyTransformer {
	private Visitor visitor;
	public JTPTransformer()
	{
        RecursiveVisitor2 vv = new RecursiveVisitor2(null);
        SymberVisitor2 pv = new SymberVisitor2(vv);
        vv.setNextVisitor(pv);
        visitor = pv;
	}
	protected void internalTransform(Body body, String pn, Map map) {
		
		Util.resetParameters();
		SootMethod thisMethod = body.getMethod();
		
		if(!Util.shouldInstruThisMethod(thisMethod.getName()))
		{
			Visitor.checkForSyncMethods(thisMethod, body.getUnits());
			Visitor.checkForLockConditions(thisMethod,body.getUnits());
			//Visitor.checkForThreadInit(thisMethod, body.getUnits());
			return;
		}
		
		SootClass thisClass = thisMethod.getDeclaringClass();
		String scname = thisClass.getName();
		//System.out.println("scname: "+scname);
		if(!Util.shouldInstruThisClass(scname)) 
			return;
				 	
		if(thisMethod.toString().contains("void main(java.lang.String[])"))
		{
			Parameters.isMethodMain = true;
		}
		else if(thisMethod.toString().contains("void run()") && Util.isRunnableSubType(thisClass))
		{
			Parameters.isMethodRunnable = true;
		}
		if(thisMethod.isSynchronized())
		{
			Parameters.isMethodSynchronized = true;
			//Visitor.isWrappedByLock = true;
		}
			
		Visitor.checkForLockConditions(thisMethod,body.getUnits());
		
		Chain units = body.getUnits();
		
		//NO IDEA WHY THIS
		//To enable insert tid
		if(thisMethod.isStatic()&&thisMethod.getParameterCount()==0)
		{
			Stmt nop=Jimple.v().newNopStmt();
			//insert the nop just before the return stmt
			units.insertBefore(nop, units.getFirst());
		}
		
        Iterator stmtIt = units.snapshotIterator();    	       
        while (stmtIt.hasNext()) 
        {
            Stmt s = (Stmt) stmtIt.next();
            visitor.visitStmt(thisMethod, units, s);
        }
        
    	if(Parameters.isMethodMain||Parameters.isMethodRunnable)
    	{
    		if(Parameters.isRuntime)
    		{
    			//DO OR DO NOT CATCH EXCEPTION??
    			//Visitor.addCallCatchException(body);
    		}
    		
    		if(Parameters.isMethodMain)
    			Visitor.addCallMainMethodEnterInsert(thisMethod, units);
    		else
    		{
    			Visitor.addCallRunMethodEnterInsert(thisMethod, units);
    		}
        }
    	
    	if(Parameters.isMethodSynchronized)//&&Parameters.removeSync)
    	{
    		//thisMethod.setModifiers(thisMethod.getModifiers()&~Modifier.SYNCHRONIZED);	//** this line removes the synchronized flag from the method signature
    		Visitor.addCallMonitorEntry(body);
    	}
    	
    	//body.validate();
	}

}
