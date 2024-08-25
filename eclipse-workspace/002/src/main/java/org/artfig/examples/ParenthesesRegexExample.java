package org.artfig.examples;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParenthesesRegexExample {
	public static void main(String[] args) {
		String input = "This is a (sample) text with (words) in parentheses.";

		// Define the regex pattern
		String regex = "\\(([^)]+)\\)";

		// Create a Pattern object
		Pattern pattern = Pattern.compile(regex);

		// Create a Matcher object
		Matcher matcher = pattern.matcher(input);

		// Find and print all matches
		if (matcher.find()) {
			// Group 1 contains the word within parentheses
			String wordWithinParentheses = matcher.group(1);
			System.out.println("Word within parentheses: " + wordWithinParentheses);
		}
	}
}
