package edu.ist.symber.common;

public enum EventType {
	READ("READ"), WRITE("WRITE"), LOCK("LOCK"), UNLOCK("UNLOCK"), START("START"), EXIT("EXIT"), FORK("FORK"), JOIN("JOIN");

	private final String text;

	private EventType(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
