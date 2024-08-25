package org.artfig.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class RegexU {

	public static DateTimeFormatter collateDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static DateTimeFormatter fidelityDate = DateTimeFormatter.ofPattern("M/d/yyyy");

	public static boolean matches(final String mainString, final String regexPattern) {
		return Pattern.compile(regexPattern).matcher(mainString).find();
	}

	public static String getFromRegex(final String mainString, final String regexPattern, final String defaultValue) {
		return getFromRegex(mainString, regexPattern, 0, defaultValue);
	}

	public static String getFromRegex(final String mainString, final String regexPattern, int groupPos,
			final String defaultValue) {
		final Matcher matcher = Pattern.compile(regexPattern).matcher(mainString);

		if (matcher.find())
			return (groupPos == 0) ? matcher.group() : matcher.group(groupPos);
		else
			return defaultValue;
	}

	public static LocalDate stringToLocalDate(final String dateString, DateTimeFormatter dtf) {
		return LocalDate.parse(dateString, dtf);
	}

	public static LocalDate dateFromFidelity(final String dateString) {
		return stringToLocalDate(dateString, fidelityDate);
	}

	public static LocalDate dateFromOption(final String dateString) {
		// Define the date format pattern
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yy");
		try {
			return sdf.parse(dateString).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} catch (ParseException e) {
			return LocalDate.now();
		}
	}

	public static double stringToDouble(String data) {
		try {
			return Double.parseDouble(StringUtils.replace(StringUtils.replace(data, "$", ""), ",", ""));
		} catch (Throwable t) {
			return 0.0;
		}
	}

	public static String money(BigDecimal bigDecimal) {
		try {
			return money(bigDecimal.doubleValue());
		} catch (Exception e) {
			return "$0.00";
		}
	}

	public static String money(double doubleValue) {
		return String.format("$%3.2f", doubleValue);
	}

	public static String percent(BigDecimal bigDecimal) {
		try {
			return percent(bigDecimal.doubleValue());
		} catch (Exception e) {
			return "0.0%";
		}
	}

	public static String percent(double doubleValue) {
		return String.format("%1.1f", doubleValue * 100) + "%";
	}

	public static String tenths(BigDecimal bigDecimal) {
		try {
			return String.format("%1.1f", bigDecimal.doubleValue());
		} catch (Exception e) {
			return "0.0";
		}
	}

}
