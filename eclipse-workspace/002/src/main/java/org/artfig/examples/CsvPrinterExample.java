package org.artfig.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.artfig.util.CsvU;

public class CsvPrinterExample {
	public static void main(String[] args) {
		// Replace "path/to/your/output/file.csv" with the actual path for the output
		// CSV file
		String filePath = "src/main/resources/file2.csv";

		// Sample data to be written to the CSV file
		List<String> header = Arrays.asList("Name", "Age", "City");
		List<List<String>> data = Arrays.asList(Arrays.asList("John Doe", "30", "New York"),
				Arrays.asList("Jane Smith", "25", "Los Angeles"), Arrays.asList("Bob Johnson", "40", "Chicago"));

		try (CSVPrinter csvPrinter = CsvU.csvPrinter(filePath)) {

			// Print the header
			csvPrinter.printRecord(header);

			// Print the data rows
			for (List<String> record : data) {
				csvPrinter.printRecord(record);
			}

			System.out.println("CSV file created successfully.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
