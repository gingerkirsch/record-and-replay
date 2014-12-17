package edu.ist.symber.monitor;

import java.lang.reflect.Field;

public class Event {
	private int fieldId;
	private int version;
	private Object value;

	public Event(int fieldId, int version, Object value) {
		super();
		this.fieldId = fieldId;
		this.version = version;
		this.value = value;
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

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		// String newLine = System.getProperty("line.separator");
		result.append(this.getClass().getName());
		result.append(" Object {");
		// result.append(newLine);
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
			// result.append(newLine);
		}
		result.append("}");
		return result.toString();
	}
}
