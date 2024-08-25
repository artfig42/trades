package org.artfig.examples;

import static org.artfig.util.RegexU.money;
import static org.artfig.util.RegexU.percent;
import static org.artfig.util.RegexU.tenths;

import java.io.File;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.artfig.trades.FidelityRecord;
import org.artfig.trades.OptionBase;
import org.artfig.trades.Position;
import org.artfig.util.CsvU;
import org.artfig.util.RegexU;

public class CsvReaderExample {

	final private static Logger log = LogManager.getLogger(CsvReaderExample.class);

	public static void main(String... args) {

		// System.setProperty("log4j2.debug", "true");
		// System.setProperty("log4j.configurationFile",
		// "c:\\Users\\artfi\\eclipse-workspace\\002\\src\\log4j2.xml");

		log.error("Sample logging");
		log.info("Sample info logging");

		// final String[] ALL_FILES = new String[] { "IRA-2023", "HSA-2023", "TOD-2023",
		// "IRA-2024", "HSA-2024",
		// "TOD-2024" };
		final String[] ALL_FILES = new String[] { "IRA-2023", "HSA-2023", "TOD-2023", "ALL-2024" };
		final Set<String> tickerBlackList = new TreeSet<>(Arrays.asList("BRKB", "HII", "NOC"));
		final Map<String, Integer> columnMap = new TreeMap<>();
		final Set<String> accountSet = new TreeSet<>();
		final Set<String> symbolSet = new TreeSet<>();
		final Set<String> descriptionSet = new TreeSet<>();
		double totalDividends = 0.0;
		String timeHack = "" + System.currentTimeMillis();

		String statsPath = String.format("/Trades/Summary/%s/", timeHack);
		new File(statsPath).mkdirs();

		String tickerPath = String.format("/Trades/Tickers/%s/", timeHack);
		new File(tickerPath).mkdirs();

		try (CSVPrinter allPrinter = CsvU.csvPrinter(statsPath + "all.csv");
				CSVPrinter optPrinter = CsvU.csvPrinter(statsPath + "opt.csv");
				CSVPrinter divPrinter = CsvU.csvPrinter(statsPath + "div.csv");
				CSVPrinter youPrinter = CsvU.csvPrinter(statsPath + "you.csv");
				CSVPrinter jnlPrinter = CsvU.csvPrinter(statsPath + "jnl.csv");
				CSVPrinter fidPrinter = CsvU.csvPrinter(statsPath + "fid.csv");
				CSVPrinter usbPrinter = CsvU.csvPrinter(statsPath + "usb.csv")) {

			FidelityRecord.printLoggingHeader(allPrinter);
			FidelityRecord.printLoggingHeader(divPrinter);
			FidelityRecord.printLoggingHeader(optPrinter);
			FidelityRecord.printLoggingHeader(jnlPrinter);
			FidelityRecord.printLoggingHeader(fidPrinter);
			FidelityRecord.printLoggingHeader(usbPrinter);
			FidelityRecord.printLoggingHeader(youPrinter);

			for (String fileName : ALL_FILES) {
				int stage = 0;

				FidelityRecord.columnMap.clear();

				String filePath = String.format("/Trades/History/%s.csv", fileName);

				try (CSVParser csvParser = CsvU.csvParser(filePath)) {
					// Iterate through records
					for (CSVRecord record : csvParser) {

						if (stage == 0) {
							// System.out.println("record: " + record);
							if (StringUtils.equals("Date", record.get(0))) {
								for (int col = 0; col < record.size(); ++col) {
									FidelityRecord.columnMap.put(record.get(col), col);
								}
								++stage;
							}
							continue;
						}

						if (stage == 1) {
							if (StringUtils.isBlank(record.get(0))) {
								continue;
							}

							FidelityRecord fr = new FidelityRecord(record);
							// fr.dump();

							if (RegexU.matches(fr.symbolRaw, "91279")) {
								fr.printDetail(usbPrinter);
								continue;
							}

							if (RegexU.matches(fr.security, "FIDELITY")) {
								fr.printDetail(fidPrinter);
								continue;
							}

							if (RegexU.matches(fr.security, "(?m)^(CALL|PUT)")) {
								OptionBase ob = OptionBase.process(fr);
								long daysUntil = ChronoUnit.DAYS.between(fr.calDate, ob.expiry) + 1;
								Period period = Period.between(fr.calDate, ob.expiry);
								double ret = fr.price / ob.strike * (360.0 / daysUntil);
								optPrinter.printRecord(fr.calDate.format(RegexU.collateDate), ob.symbol, fr.symbolRaw,
										ob.tag + " / " + fr.description, RegexU.money(fr.price), money(fr.amount),
										fr.quantity, ob, fr.account, daysUntil, percent(ret), fr.security);
								fr.symbolRaw = ob.symbol;
								Position.processOption(fr);
								continue;
							}

							if (StringUtils.startsWith(fr.description, "YOU B")
									|| StringUtils.startsWith(fr.description, "YOU S")) {
								fr.printDetail(youPrinter);
								Position.processTrade(fr);
								continue;
							}

							if (RegexU.matches(fr.description, "LOANED|LOAN RE|COLLATERAL")) {
								fr.printDetail(jnlPrinter);
								continue;
							}

							if (Math.abs(fr.quantity) < 0.001) {
								fr.printDetail(divPrinter);
								Position.processDividend(fr);
								continue;
							}

							if (Math.abs(fr.amount) < 0.001) {
								fr.printDetail(youPrinter);
								Position.processDividend(fr);
								continue;
							}

							fr.printDetail(allPrinter);

							accountSet.add(fr.account);

							accountSet.add(fr.account);
							symbolSet.add(fr.symbolRaw);
							descriptionSet.add(fr.description);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (CSVPrinter ownPrinter = CsvU.csvPrinter(statsPath + "own.csv");
				CSVPrinter oldPrinter = CsvU.csvPrinter(statsPath + "old.csv");
				CSVPrinter portAllPrinter = CsvU.csvPrinter(statsPath + "portAll.csv");) {
			ownPrinter.printRecord("symbol", "shares", "pricePosition", "priceTrading", "totalBasis", "sharesBought",
					"priceBought", "sharesSold", "priceSold", "dividends", "sharesDividend", "optionPremium");
			oldPrinter.printRecord("symbol", "shares", "pricePosition", "priceTrading", "totalBasis", "sharesBought",
					"priceBought", "sharesSold", "priceSold", "dividends", "sharesDividend");
			for (Entry<String, Position> entry : Position.positionMap.entrySet()) {
				final Position pos = entry.getValue();
				if (!tickerBlackList.contains(pos.symbol) && pos.ownShares()) {
					ownPrinter.printRecord(pos.symbol, pos.getShares(), pos.getPricePosition(), pos.getPriceTrading(),
							pos.getTotalBasis(), tenths(pos.sharesBought), pos.getPriceBought(), tenths(pos.sharesSold),
							pos.getPriceSold(), pos.getDividends(), tenths(pos.sharesDividend), money(pos.totalOption));

					// symbol, quantity, cost, date (MM/DD/YYYY, YYYY-MM-DD)
					portAllPrinter.printRecord(pos.symbol, pos.shares, pos.getOwnedBasis(), "01/01/2024");

					// symbol, quantity, cost, date (MM/DD/YYYY, YYYY-MM-DD)

				} else
					oldPrinter.printRecord(pos.symbol, pos.getShares(), pos.getPricePosition(), pos.getPriceTrading(),
							pos.getTotalBasis(), tenths(pos.sharesBought), pos.getPriceBought(), tenths(pos.sharesSold),
							pos.getPriceSold(), pos.getDividends(), tenths(pos.sharesDividend));
				try (CSVPrinter posPrinter = CsvU.csvPrinter(tickerPath + pos.symbol + ".csv");) {
					FidelityRecord.printLoggingHeader(posPrinter);
					Collections.sort(pos.historyList);
					for (FidelityRecord fr : pos.historyList) {
						fr.printDetail(posPrinter);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		try (CSVPrinter opnPrinter = CsvU.csvPrinter(statsPath + "opn.csv");
				CSVPrinter clsPrinter = CsvU.csvPrinter(statsPath + "cls.csv");) {
			opnPrinter.printRecord("symbol", "key", "held", "cost", "numBought", "numSold", "numExpired",
					"numAssigned");
			for (Entry<String, OptionBase> entry : OptionBase.optionMap.entrySet()) {
				OptionBase ob = entry.getValue();
				opnPrinter.printRecord(ob.symbol, entry.getKey(), ob.totalHeld(), ob.cost / 100.0, ob.numBought,
						ob.numSold, ob.numExpired, ob.numAssigned);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
