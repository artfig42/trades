package org.artfig.trades;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.artfig.util.RegexU;

public class Position {

	private static MathContext bullshit = new MathContext(20, RoundingMode.HALF_EVEN);

	public static Map<String, Position> positionMap = new TreeMap<>();

	public List<FidelityRecord> historyList = new ArrayList<>();

	public BigDecimal shares = BigDecimal.ZERO;
	public BigDecimal sharesBought = BigDecimal.ZERO;
	public BigDecimal sharesSold = BigDecimal.ZERO;
	public BigDecimal sharesDividend = BigDecimal.ZERO;
//	public BigDecimal basisTotal = BigDecimal.ZERO;
	public BigDecimal basisBought = BigDecimal.ZERO;
	public BigDecimal basisSold = BigDecimal.ZERO;
	public BigDecimal totalDividend = BigDecimal.ZERO;
	public BigDecimal totalOption = BigDecimal.ZERO;

	public final String symbol;

	public Position(final String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return ownShares() ? getShares() + " @ " + getPricePosition() : getTotalBasis() + " closed";
	}

	private static Position findPosition(FidelityRecord fr) {
		if (!positionMap.containsKey(fr.symbolRaw)) {
			positionMap.put(fr.symbolRaw, new Position(fr.symbolRaw));
		}
		final Position position = positionMap.get(fr.symbolRaw);
		position.historyList.add(fr);
		return position;

	}

	public static Position processDividend(FidelityRecord fr) {
		final Position position = findPosition(fr);

		position.shares = position.shares.add(new BigDecimal(fr.quantity));
		position.sharesDividend = position.sharesDividend.add(new BigDecimal(fr.quantity));

		position.totalDividend = position.totalDividend.add(new BigDecimal(fr.amount));

		return position;
	}

	public static Position processTrade(FidelityRecord fr) {
		final Position position = findPosition(fr);

		position.shares = position.shares.add(new BigDecimal(fr.quantity));
		// position.basisTotal = position.basisTotal.add(new BigDecimal(fr.amount));
		if (fr.quantity < 0) {
			position.sharesSold = position.sharesSold.add(new BigDecimal(fr.quantity));
			position.basisSold = position.basisSold.add(new BigDecimal(fr.amount));
		} else {
			position.sharesBought = position.sharesBought.add(new BigDecimal(fr.quantity));
			position.basisBought = position.basisBought.add(new BigDecimal(fr.amount));
		}

		return position;
	}

	public static Position processOption(FidelityRecord fr) {
		final Position position = findPosition(fr);
		position.totalOption = position.totalOption.add(BigDecimal.valueOf(fr.amount));
		return position;
	}

	public boolean ownShares() {
		return Math.abs(shares.doubleValue()) > 0.001;
	}

	public String getShares() {
		return RegexU.tenths(shares);
	}

	public String getPriceTrading() {
		return ownShares() ? RegexU.money(basisBought.add(basisSold).divide(shares, bullshit)) : "$0.00";
	}

	public String getPricePosition() {
		return ownShares() ? RegexU.money(basisBought.add(basisSold).add(totalDividend).add(totalOption).divide(shares, bullshit))
				: "$0.00";
	}

	public String getPriceBought() {
		return Math.abs(sharesBought.doubleValue()) > 0.001 ? RegexU.money(basisBought.divide(sharesBought, bullshit))
				: "$0.00";
	}

	public String getPriceSold() {
		return Math.abs(sharesSold.doubleValue()) > 0.001 ? RegexU.money(basisSold.divide(sharesSold, bullshit))
				: "$0.00";
	}

	public String getTotalBasis() {
		return RegexU.money(basisBought.add(basisSold).add(totalDividend).add(totalOption));
	}

	public String getBoughtSoldBasis() {
		return RegexU.money(basisBought.add(basisSold));
	}

	public String getDividends() {
		return RegexU.money(totalDividend);
	}

	public String getOwnedBasis() {
		return String.format("%3.2f", Math.max(1,
				basisBought.add(basisSold).add(totalDividend).add(totalOption).doubleValue() / -shares.doubleValue()));
	}

}
