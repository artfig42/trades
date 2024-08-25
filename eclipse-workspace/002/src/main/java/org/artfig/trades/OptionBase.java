package org.artfig.trades;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.artfig.util.RegexU;

public class OptionBase {

	enum disposition {
		BOUGHT, SOLD, ASSIGNED, EXPIRED, DISTRIBUTION
	}

	enum callOrPut {
		CALL, PUT
	}

	public static Map<String, OptionBase> optionMap = new TreeMap<>();

	final public String callOrPut;
	final public String symbol;
	final public LocalDate expiry;
	final public double strike;
	final public String tag;

	public long numBought = 0;
	public long numSold = 0;
	public long numAssigned = 0;
	public long numExpired = 0;
	public long cost = 0;

	@Override
	public String toString() {
		return String.format("%s %s Expiry %s Strike %1.2f", callOrPut, symbol, collatableDate(), strike);
	}

	public String collatableDate() {
		return expiry.format(RegexU.collateDate);
	}

	public OptionBase(final FidelityRecord fr) {
		final String securtyDetail = fr.security;

		callOrPut = securtyDetail.startsWith("PUT") ? "PUT" : securtyDetail.startsWith("CALL") ? "CALL" : "BAD";

//		Matcher symbolMatcher = Pattern.compile("\\(([^)]+)\\)").matcher(securtyDetail);
//		if (symbolMatcher.find()) {
//			symbol = symbolMatcher.group(1);
//		} else {
//			symbol = "OUCH";
		// }
		// symbol = RegexU.getFromRegex(fr.symbolRaw, "^[a-zA-Z]+",
		// "OOPS").toUpperCase();
		String regexSymbol = "\\((\\w{1,5})\\)";

		symbol = RegexU.getFromRegex(securtyDetail, regexSymbol, 1, "OOPS").toUpperCase();

		Matcher strikeMatcher = Pattern.compile("\\$\\S+").matcher(securtyDetail);
		if (strikeMatcher.find()) {
			strike = Double.parseDouble(strikeMatcher.group().substring(1));
		} else {
			strike = 0;
		}

		final String regex = "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\\s\\d{2}\\s\\d{2}";
		Matcher expiryMatcher = Pattern.compile(regex).matcher(securtyDetail);
		if (expiryMatcher.find()) {
			expiry = RegexU.dateFromOption(expiryMatcher.group());
		} else {
			expiry = null;
		}

		tag = String.format("%s_%06d_%s", collatableDate(), (int) (strike * 100), callOrPut);

	}

	public long totalHeld() {
		return numBought + numSold + numAssigned + numExpired;
	}

	public static OptionBase process(FidelityRecord fr) {

		if (!optionMap.containsKey(fr.symbolRaw)) {
			optionMap.put(fr.symbolRaw, new OptionBase(fr));
		}
		OptionBase ob = optionMap.get(fr.symbolRaw);
		if (fr.description.contains("SOLD")) {
			ob.numSold += fr.quantity;
		} else if (fr.description.contains("BOUGHT")) {
			ob.numBought += fr.quantity;
		} else if (fr.description.contains("ASSIGNED")) {
			ob.numAssigned += fr.quantity;
		} else if (fr.description.contains("EXPIRED")) {
			ob.numExpired += fr.quantity;
		}
		ob.cost += fr.amount * 100;
		return ob;
	}

}
