package edu.ist.symber.common.logreducer;

import java.util.HashMap;
import java.util.Map;

import edu.ist.symber.common.Pair;
import edu.ist.symber.common.ThreadTrace;
import edu.ist.symber.common.ThreadTraceShort;

public class LogReducer {

	/**
	 * Transforms a MapThreadToTrace (which uses long absolute values for clocks) into a MapThreadtoTraceShort (which uses short deltas instead of absolute values)
	 * @param mapThreadtoTrace
	 * @return
	 */
	public static Map<String, ThreadTraceShort> reduceThreadTrace(Map<String, ThreadTrace> mapThreadtoTrace)
	{
		Map<String, ThreadTraceShort> res = new HashMap<String, ThreadTraceShort>();

		for(Map.Entry<String,ThreadTrace> entry : mapThreadtoTrace.entrySet())
		{
			ThreadTrace trace = entry.getValue();
			ThreadTraceShort newtrace = new ThreadTraceShort(entry.getKey());

			//** transform absolute values into deltas (storeTrace)
			if(!trace.storeTrace.isEmpty())
			{
				Pair<Long,Long> prevS = trace.storeTrace.firstElement();
				newtrace.storeTrace.add(new Pair<Short,Short>((short)(prevS.k - 0),(short) (prevS.v - 0)));	//** I subtract 0 to allow casting easily to short
				for(Pair<Long, Long> sclock : trace.storeTrace.subList(1, trace.storeTrace.size()))
				{
					Pair<Short,Short> newsclock = new Pair<Short, Short>((short)0,(short)0);
					newsclock.k = (short) (sclock.k - prevS.k);
					newsclock.v = (short) (sclock.v - prevS.v);
					newtrace.storeTrace.add(newsclock);

					prevS.k = sclock.k;
					prevS.v = sclock.v;
				}
			}

			//** transform absolute values into deltas (loadTrace)
			if(!trace.loadTrace.isEmpty())
			{
				Long prevL = trace.loadTrace.firstElement();
				newtrace.loadTrace.add((short) (prevL - 0));	//** I subtract 0 to allow casting easily to short
				for(Long lclock : trace.loadTrace.subList(1, trace.loadTrace.size()))
				{
					short newlclock = 0;
					newlclock = (short) (lclock - prevL);
					newlclock = (short) (lclock - prevL);
					newtrace.loadTrace.add(newlclock);

					prevL = lclock;
					prevL = lclock;
				}
			}

			//** transform absolute values into deltas (syncTrace)
			if(!trace.syncTrace.isEmpty())
			{
				Long prevSync = trace.syncTrace.firstElement();
				newtrace.syncTrace.add((short) (prevSync - 0));	//** I subtract 0 to allow casting easily to short
				for(Long syncclock : trace.syncTrace.subList(1, trace.syncTrace.size()))
				{
					short newsclock = 0;
					newsclock = (short) (syncclock - prevSync);
					newsclock = (short) (syncclock - prevSync);
					newtrace.syncTrace.add(newsclock);

					prevSync = syncclock;
					prevSync = syncclock;
				}
			}

			res.put(entry.getKey(), newtrace);
		}

		return res;		
	}



	/**
	 * Transforms a MapThreadToTraceShort (which uses short deltas instead of absolute values) into a MapThreadtoTrace (which uses long absolute values for clocks)
	 * @param mapThreadtoTrace
	 * @return
	 */
	public static Map<String, ThreadTrace> expandThreadTrace(Map<String, ThreadTraceShort> mapThreadtoTraceShort)
	{
		Map<String, ThreadTrace> res = new HashMap<String, ThreadTrace>();

		for(Map.Entry<String,ThreadTraceShort> entry : mapThreadtoTraceShort.entrySet())
		{
			ThreadTraceShort trace = entry.getValue();
			ThreadTrace newtrace = new ThreadTrace(entry.getKey());

			//** transform deltas into absolute values (storeTrace)
			if(!trace.storeTrace.isEmpty())
			{
				Pair<Long,Long> prevS = new Pair<Long,Long>((long) trace.storeTrace.firstElement().k,(long) trace.storeTrace.firstElement().v) ;
				newtrace.storeTrace.add(new Pair<Long,Long>(prevS.k,prevS.v));	
				for(Pair<Short, Short> sclock : trace.storeTrace.subList(1, trace.storeTrace.size()))
				{
					Pair<Long,Long> newsclock = new Pair<Long, Long>((long) 0, (long) 0);
					newsclock.k = (long) (sclock.k + prevS.k);
					newsclock.v = (long) (sclock.v + prevS.v);
					newtrace.storeTrace.add(newsclock);

					prevS.k = newsclock.k;
					prevS.v = newsclock.v;
				}
			}

			//** transform deltas into absolute values (loadTrace)
			if(!trace.loadTrace.isEmpty())
			{
				Long prevL = (long) trace.loadTrace.firstElement();
				newtrace.loadTrace.add((long) prevL);	//** I subtract 0 to allow casting easily to short
				for(Short lclock : trace.loadTrace.subList(1, trace.loadTrace.size()))
				{
					long newlclock = 0;
					newlclock = (long) (lclock + prevL);
					newlclock = (long) (lclock + prevL);
					newtrace.loadTrace.add(newlclock);

					prevL = newlclock;
					prevL = newlclock;
				}
			}

			//** transform deltas into absolute values (syncTrace)
			if(!trace.syncTrace.isEmpty())
			{
				Long prevSync = (long) trace.syncTrace.firstElement();
				newtrace.syncTrace.add((long) prevSync);	//** I subtract 0 to allow casting easily to short
				for(Short syncclock : trace.syncTrace.subList(1, trace.syncTrace.size()))
				{
					long newsclock = 0;
					newsclock = (long) (syncclock + prevSync);
					newsclock = (long) (syncclock + prevSync);
					newtrace.syncTrace.add(newsclock);

					prevSync = newsclock;
					prevSync = newsclock;
				}
			}

			res.put(entry.getKey(), newtrace);
		}

		return res;
	}

}
