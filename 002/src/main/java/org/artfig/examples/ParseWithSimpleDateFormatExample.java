package org.artfig.examples;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ParseWithSimpleDateFormatExample {
    public static void main(String[] args) {
        // Input date strings
        String dateString1 = "OCT 01 23";
        String dateString2 = "NOV 27 24";

        // Define the date format pattern
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yy");

        try {
            // Parse the date strings to Date objects
            Date date1 = sdf.parse(dateString1);
            Date date2 = sdf.parse(dateString2);

            // Convert Date objects to LocalDate (Java 8 and later)
            LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Print the parsed LocalDates
            System.out.println("Parsed LocalDate 1: " + localDate1);
            System.out.println("Parsed LocalDate 2: " + localDate2);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
