package com.attendance.service;

import com.attendance.model.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReportService {
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final Path reportsDir;

    public ReportService(AttendanceService attendanceService, StudentService studentService, Path reportsDir) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.reportsDir = reportsDir;
        try { Files.createDirectories(reportsDir); } catch (IOException ignored) {}
    }

    public String generateMonthlyReport(int year, int month) {
        Map<String, Double> percent = attendanceService.monthlyPercentage(year, month);
        StringBuilder sb = new StringBuilder();
        sb.append("Monthly Attendance Report - ").append(Month.of(month)).append(" ").append(year).append("\n\n");
        sb.append(String.format("%-10s %-25s %-10s\n", "StudentID", "Name", "Percent"));
        sb.append("------------------------------------------------------\n");
        for (Map.Entry<String, Double> e : percent.entrySet()) {
            Optional<Student> s = studentService.findById(e.getKey());
            String name = s.map(Student::getName).orElse("Unknown");
            sb.append(String.format("%-10s %-25s %6.2f%%\n", e.getKey(), name, e.getValue()));
        }
        return sb.toString();
    }

    public Path exportReportToFile(String content, String fileName) throws IOException {
        Path out = reportsDir.resolve(fileName);
        Files.write(out, content.getBytes());
        return out;
    }

    public String generateDefaulterList(int year, int month, double threshold) {
        List<String> def = attendanceService.getDefaulters(year, month, threshold);
        StringBuilder sb = new StringBuilder();
        sb.append("Defaulter List (< ").append(threshold).append("%) - ").append(month).append("/").append(year).append("\n\n");
        sb.append(String.format("%-10s %-25s\n", "StudentID", "Name"));
        sb.append("------------------------------------\n");
        for (String id : def) {
            Optional<Student> s = studentService.findById(id);
            String name = s.map(Student::getName).orElse("Unknown");
            sb.append(String.format("%-10s %-25s\n", id, name));
        }
        return sb.toString();
    }
}
