package org.artfig.trades;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.artfig.util.RegexU;

public class FidelityRecord implements Comparable<Object> {

	public final static Map<String, Integer> columnMap = new TreeMap<>();

	public String symbolRaw;
	public final double price;
	public final double amount;
	public final String account;
	public final String description;
	public final double quantity;
	public final String security;
	public final String date;
	public final String type;
	// Calculated
	public final LocalDate calDate;
	public final RecordEnum recordEnum;

	public void dump() {
		System.out.println("\nsymbol: " + symbolRaw);
		System.out.println("price: " + price);
		System.out.println("amount: " + amount);
		System.out.println("account: " + account);
		System.out.println("description: " + description);
		System.out.println("quantity: " + quantity);
		System.out.println("security: " + security);
		System.out.println("date: " + date);
		System.out.println("type: " + type);
	}

	public FidelityRecord(final CSVRecord record) {
		symbolRaw = record.get(columnMap.get("Symbol"));
		price = RegexU.stringToDouble(record.get(columnMap.get("Price")));
		amount = RegexU.stringToDouble(record.get(columnMap.get("Amount")));
		account = record.get(columnMap.get("Account"));
		description = record.get(columnMap.get("Description"));
		quantity = RegexU.stringToDouble(record.get(columnMap.get("Quantity")));
		security = record.get(columnMap.get("Security Description"));
		date = record.get(columnMap.get("Date"));
		calDate = RegexU.dateFromFidelity(date);
		type = record.get(columnMap.get("Type"));
		recordEnum = RecordEnum.fromDescription(description, security);
	}

	public static void printLoggingHeader(CSVPrinter printer) {
		try {
			printer.printRecord("date", "symbol", "description", "enum", "sharePrice", "shareAmount", "numberShares",
					"optionPrice", "optionPremium", "numberContracts", "dividend", "security", "account");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public enum RecordEnum {
		DIVIDEND, SHARES, OPTIONS, MISC;

		public static RecordEnum fromDescription(final String description, final String security) {
			if (StringUtils.startsWithAny(security.toUpperCase(), "CALL", "PUT"))
				return OPTIONS;
			if (StringUtils.containsIgnoreCase(description, "DIVIDEND"))
				return DIVIDEND;
			if (StringUtils.startsWithIgnoreCase(description, "YOU"))
				return SHARES;
			return MISC;
		}
	};

	public void printDetail(CSVPrinter printer) {
		try {
			switch (recordEnum) {
			case DIVIDEND:
				printer.printRecord(calDate, symbolRaw, description, recordEnum, null, null, null, null, null, null,
						amount, security, StringUtils.lowerCase(account));
				break;

			case SHARES:
			case MISC:

				printer.printRecord(calDate, symbolRaw, description, recordEnum, price, amount, quantity, null, null,
						null, null, security, StringUtils.lowerCase(account));
				break;

			case OPTIONS:
				printer.printRecord(calDate, symbolRaw, description, recordEnum, null, null, null, price, amount,
						quantity, null, security, StringUtils.lowerCase(account));
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof FidelityRecord) {
			FidelityRecord fr = (FidelityRecord) o;
			return calDate.compareTo(fr.calDate);
		}
		return 0;
	}

}
