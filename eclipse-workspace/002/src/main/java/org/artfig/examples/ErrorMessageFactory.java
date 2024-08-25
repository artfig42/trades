package org.artfig.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorMessageFactory {

	final private static Logger log = LogManager.getLogger(ErrorMessageFactory.class);

	public ErrorMessageFactory() {

	}

	public ErrorMessage getErrorMessage(final String message) {
		ErrorMessage em = new ErrorMessage(message);
		return em;
	}

	public static void main(String... args) {		
		ErrorMessageFactory emf = new ErrorMessageFactory();
		final ErrorMessage em = emf.getErrorMessage("sample");
		log.error(em);
	}
}
