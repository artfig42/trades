package org.artfig.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorMessage {

	final private static Logger log = LogManager.getLogger(ErrorMessage.class);

	public final String message;
	public final String trace;

	public ErrorMessage(final String message) {
		this.message = message;
		this.trace = StackTrace.traceCallerIgnore("ErrorMessage");
	}

	public String toString() {
		return message + "@" + trace;
	}

	public static void main(String... args) {
		final ErrorMessage em = new ErrorMessage("sample");
		log.error(em);
	}

}
