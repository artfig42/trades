package org.artfig.examples;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractCodeExample {
    public static void main(String[] args) {
        String input1 = "PUT (AGNC) AGNC INVT CORP COM FEB 09 24 $9.5 (100 SHS)"
        	;
        String input2 = "PUT (XY) ffffffffffffffff";

        // Define the regex pattern
        String regex = "\\((\\w{1,5})\\)";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(regex);

        // Create a Matcher object for input1
        Matcher matcher1 = Pattern.compile(regex).matcher(input1);

        // Find and print the code for input1
        if (matcher1.find()) {
            String code1 = matcher1.group(1);
            System.out.println("Extracted Code from input1: " + code1);
        }

        // Create a Matcher object for input2
        Matcher matcher2 = pattern.matcher(input2);

        // Find and print the code for input2
        if (matcher2.find()) {
            String code2 = matcher2.group(1);
            System.out.println("Extracted Code from input2: " + code2);
        }
    }
}
