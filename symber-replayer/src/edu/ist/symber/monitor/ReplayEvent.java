package edu.ist.symber.monitor;

import java.io.Serializable;

public class ReplayEvent implements Serializable{
	public String threadId;
	private static final long serialVersionUID = 1L;
	
	public ReplayEvent()
	{
		threadId = "";
	}
	
	public ReplayEvent(String tid)
	{
		threadId = tid;
	}
	
	public ReplayEvent(ReplayEvent e)
	{
		this.threadId = e.threadId;
	}
	
	public String getThreadId()
	{
		return threadId;
	}
	
	public void setThreadId(String tid)
	{
		threadId = tid;
	}
	
	public String toString()
	{
		return threadId;
	}
}
