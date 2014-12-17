package edu.ist.symber.transformer.loop;

import java.util.*;

import soot.SootMethod;
import soot.jimple.toolkits.thread.mhp.PegGraph;

public class Globals {
	public static PegGraph pegGraph;
	public static HashMap<SootMethod, Collection<MyLoop>> loopsMap = new HashMap<SootMethod, Collection<MyLoop>>();
}
