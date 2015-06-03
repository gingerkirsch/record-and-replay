package edu.ist.symber.common;

import java.lang.reflect.Field;

public class Event {
	private String threadId;	// id of thread that generated this event
	private EventType eventType; // type of operation
	private int eventId; // counter of operations within current thread (partial order)
	private int fieldId; // variable id
	private int version; // version
	private Object value; // value

	public Event(String threadId, int eventId, EventType eventType, int fieldId, int version, Object value) {
		// READ, WRITE
		super();
		this.setThreadId(threadId);
		this.eventId = eventId;
		this.eventType = eventType;
		this.fieldId = fieldId;
		this.version = version;
		this.value = value;
	}
	
	public Event(String threadId, int eventId, EventType eventType, int fieldId) {
		// LOCK, UNLOCK
		super();
		this.setThreadId(threadId);
		this.eventId = eventId;
		this.eventType = eventType;
		this.fieldId = fieldId;
	}
	
	public Event(String threadId, int eventId, EventType eventType) {
		// START, EXIT
		super();
		this.setThreadId(threadId);
		this.eventId = eventId;
		this.eventType = eventType;
	}
	
	public Event(String threadId, int eventId, String value, EventType eventType) {
		// FORK, JOIN
		super();
		this.setThreadId(threadId);
		this.eventId = eventId;
		this.value = value;
		this.eventType = eventType;
	}
	
	public int getEventId(){
		return eventId;
	}
	
	public void setEventId(int eventId){
		this.eventId = eventId;
	}

	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setIsRead(EventType eventType) {
		this.eventType = eventType;
	}
	
	public String getOrderConstraintName(){
		StringBuilder result = new StringBuilder();
		result.append("O");
		switch (this.getEventType()){
			case READ: result.append("R-"); 
				result.append("field_"+this.fieldId+"-");
				result.append("v"+this.version+"-");
				result.append("T"+this.threadId+"_"+this.eventId+"@");
				result.append(this.value);
				break;
			case WRITE: result.append("W-"); 
				result.append("field_"+this.fieldId+"-");
				result.append("v"+this.version+"-");
				result.append("T"+this.threadId+"_"+this.eventId+"@");
				result.append(this.value);
				break;
			case LOCK: 
				result.append("L-"); 
				result.append("monitor_"+this.fieldId+"-");
				result.append("v"+this.version+"-");
				result.append("T"+this.threadId+"_"+this.eventId);
				break;
			case UNLOCK: 
				result.append("U-"); 
				result.append("monitor_"+this.fieldId+"-");
				result.append("v"+this.version+"-");
				result.append("T"+this.threadId+"_"+this.eventId);
				break;
			case START: 
				result.append("-START-"); 
				result.append("T"+this.threadId+"_"+this.eventId);
				break;
			case EXIT: 
				result.append("-EXIT-"); 
				result.append("T"+this.threadId+"_"+this.eventId);
				break;
			case FORK: 
				result.append("-FORK-"); 
				result.append("T"+this.value+"-T"+this.threadId+"_"+this.eventId);
				break;
			case JOIN: 
				result.append("-JOIN-"); 
				result.append("T"+this.value+"-T"+this.threadId+"_"+this.eventId);
				break;
				
		}
		return result.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{");
		result.append(this.getClass().getName());
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			result.append("  ");
			try {
				result.append(field.getName());
				result.append(": ");
				result.append(field.get(this));
			} catch (IllegalAccessException ex) {
				System.out.println(ex);
			}
		}
		result.append("}");
		return result.toString();
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
}
