package org.artfig.examples;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StackTrace {

	final private static Logger log = LogManager.getLogger(StackTrace.class);

	public static String trace() {
		final StackTraceElement[] st = Thread.currentThread().getStackTrace();
		return st[2].toString();
	}

	public static String traceCaller() {
		final StackTraceElement[] st = Thread.currentThread().getStackTrace();
		return st[3].toString();
	}

	public static String traceCallerIgnore(String... ignoreArgs) {
		final StackTraceElement[] st = Thread.currentThread().getStackTrace();

		String trace = st[st.length - 1].toString();
		for (int index = 3; index < st.length; ++index) {
			trace = st[index].toString();
			if (!StringUtils.containsAny(trace, ignoreArgs)) {
				return trace;
			}
		}
		return "final-" + trace;
	}

	public static void traceAllWrapper() {
		traceAll();
	}

	public static String traceAll(String... ignoreArgs) {
		final StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String trace = st[st.length - 1].toString();
		for (int index = 0; index < st.length; ++index) {
			log.warn(st[index]);
		}
		return "final-" + trace;
	}

	public static void main(String... args) {
		log.error(StackTrace.trace());
		traceAllWrapper();
	}

}
