package edu.ist.symber.resolver.util;

import java.util.Comparator;

import edu.ist.symber.common.Event;


public class WriteEventComparator implements Comparator<Event>{

	@Override
	public int compare(Event o1, Event o2) {
		double v1 = o1.getVersion();
		double v2 = o2.getVersion();
		if (v1 > v2) { 
			return 1;
		}
		else if(v1 == v2){
			return 0;
		}
		else {
			return -1;
		}
	}
}
