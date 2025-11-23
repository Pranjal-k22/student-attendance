package com.attendance.repository;

import com.attendance.model.Attendance;
import com.attendance.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceRepository {
    private final Path file;

    public AttendanceRepository(Path file) {
        this.file = file;
        FileUtils.ensureFile(file);
    }

    public List<Attendance> getAll() {
        List<Attendance> list = new ArrayList<>();
        try {
            List<String> lines = FileUtils.readAllLines(file);
            for (String l : lines) {
                Attendance a = Attendance.fromCsv(l);
                if (a != null) list.add(a);
            }
        } catch (IOException e) {
            System.err.println("Failed to read attendance: " + e.getMessage());
        }
        return list;
    }

    public List<Attendance> getByDate(LocalDate date) {
        return getAll().stream().filter(a -> a.getDate().equals(date)).collect(Collectors.toList());
    }

    public List<Attendance> getByStudent(String studentId) {
        return getAll().stream().filter(a -> a.getStudentId().equals(studentId)).collect(Collectors.toList());
    }

    public boolean saveAll(List<Attendance> entries) {
        try {
            // Append entries one by one
            for (Attendance a : entries) {
                FileUtils.appendLine(file, a.toString());
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save attendance: " + e.getMessage());
            return false;
        }
    }

    public boolean overwriteAll(List<Attendance> all) {
        List<String> lines = new ArrayList<>();
        for (Attendance a : all) lines.add(a.toString());
        try {
            FileUtils.writeLines(file, lines);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to overwrite attendance: " + e.getMessage());
            return false;
        }
    }

    public boolean existsForStudentOnDate(String studentId, LocalDate date) {
        return getAll().stream().anyMatch(a -> a.getStudentId().equals(studentId) && a.getDate().equals(date));
    }
}
