package org.artfig.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public class CsvU {

	public static CSVPrinter csvPrinter(String filepath) throws IOException {
		// Create a FileWriter
		Writer writer = new FileWriter(filepath);

		// Create a CSVPrinter with the desired format (here using the default CSV
		// format)
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

		return csvPrinter;
	}

	public static CSVParser csvParser(String filepath) throws IOException {
		// Create a FileReader
		Reader reader = new FileReader(filepath);

		// Create a CSVParser using the default format

		// CSVParser csvParser = CSVFormat.DEFAULT..withHeader().parse(reader);

		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

		return csvParser;
	}
}
