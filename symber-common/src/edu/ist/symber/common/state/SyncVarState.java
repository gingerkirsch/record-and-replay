package edu.ist.symber.common.state;

public class SyncVarState {
	public int varId;
	public long syncClock;
	
	public SyncVarState()
	{
		syncClock = 0;
	}
	
	public SyncVarState(int id)
	{
		varId = id;
		syncClock = 0;
	}
	
	public String toString()
	{
		return ("["+varId+"]\tsyncClock: "+syncClock);
	}
}
