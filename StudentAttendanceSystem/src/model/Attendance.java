package com.attendance.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Attendance {
    private String studentId;
    private LocalDate date;
    private boolean present; // true = present, false = absent
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;

    public Attendance(String studentId, LocalDate date, boolean present) {
        this.studentId = studentId.trim();
        this.date = date;
        this.present = present;
    }

    public String getStudentId() { return studentId; }
    public LocalDate getDate() { return date; }
    public boolean isPresent() { return present; }

    @Override
    public String toString() {
        return studentId + "," + date.format(F) + "," + (present ? "P" : "A");
    }

    public static Attendance fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 3) return null;
        LocalDate d = LocalDate.parse(parts[1], F);
        boolean p = "P".equalsIgnoreCase(parts[2].trim());
        return new Attendance(parts[0], d, p);
    }
}
