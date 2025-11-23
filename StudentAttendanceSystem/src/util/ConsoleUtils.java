package com.attendance.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner SC = new Scanner(System.in);
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine();
    }

    public static int readInt(String prompt, int defaultVal) {
        try {
            String s = readLine(prompt);
            if (s.trim().isEmpty()) return defaultVal;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static LocalDate readDate(String prompt, LocalDate defaultDate) {
        try {
            String s = readLine(prompt + " (YYYY-MM-DD) [default: " + defaultDate + "]: ");
            if (s.trim().isEmpty()) return defaultDate;
            return LocalDate.parse(s.trim(), F);
        } catch (Exception e) {
            System.out.println("Invalid date, using default.");
            return defaultDate;
        }
    }
}
