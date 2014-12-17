package edu.ist.symber.transformer;

import edu.ist.symber.transformer.phase1.SymberVisitor1;
import edu.ist.symber.transformer.phase1.RecursiveVisitor1;

public class SymberTransform {
	public static void main(String[] args) {
		RecursiveVisitor1 vv = new RecursiveVisitor1(null);
		SymberVisitor1 pv = new SymberVisitor1(vv);
		vv.setNextVisitor(pv);
		Visitor.setObserverClass("edu.ist.symber.monitor.Monitor");
		// Visitor.setMyClass("edu.hkust.leap.monitor.MyMonitorInfer");
		TransformClass processor = new TransformClass();
		processor.processAllAtOnce(args, pv);
	}
}
