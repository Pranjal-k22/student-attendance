package com.attendance.service;

import com.attendance.model.Attendance;
import com.attendance.model.Student;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.StudentRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceService {
    private final AttendanceRepository attendanceRepo;
    private final StudentRepository studentRepo;

    public AttendanceService(AttendanceRepository attendanceRepo, StudentRepository studentRepo) {
        this.attendanceRepo = attendanceRepo;
        this.studentRepo = studentRepo;
    }

    public boolean markAttendanceForDate(LocalDate date, Map<String, Boolean> presentMap) {
        // Prevent duplicate entries for same student-date
        List<Attendance> toSave = new ArrayList<>();
        for (Map.Entry<String, Boolean> e : presentMap.entrySet()) {
            String sid = e.getKey();
            if (attendanceRepo.existsForStudentOnDate(sid, date)) continue;
            toSave.add(new Attendance(sid, date, e.getValue()));
        }
        return attendanceRepo.saveAll(toSave);
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepo.getByDate(date);
    }

    public Map<String, Double> monthlyPercentage(int year, int month) {
        List<Attendance> all = attendanceRepo.getAll().stream()
                .filter(a -> a.getDate().getYear() == year && a.getDate().getMonthValue() == month)
                .collect(Collectors.toList());

        List<Student> students = studentRepo.getAll();
        Map<String, List<Attendance>> byStudent = new HashMap<>();
        for (Attendance a : all) {
            byStudent.computeIfAbsent(a.getStudentId(), k -> new ArrayList<>()).add(a);
        }

        Map<String, Double> result = new HashMap<>();
        // number of working days = number of unique dates in that month for which there is at least one record
        Set<LocalDate> uniqueDates = all.stream().map(Attendance::getDate).collect(Collectors.toSet());
        int totalDays = uniqueDates.size();
        if (totalDays == 0) {
            // if no data, return zeros
            for (Student s : students) result.put(s.getId(), 0.0);
            return result;
        }

        for (Student s : students) {
            List<Attendance> entries = byStudent.getOrDefault(s.getId(), Collections.emptyList());
            long presentCount = entries.stream().filter(Attendance::isPresent).count();
            double percent = (presentCount * 100.0) / totalDays;
            result.put(s.getId(), percent);
        }
        return result;
    }

    public List<String> getDefaulters(int year, int month, double thresholdPercent) {
        Map<String, Double> perc = monthlyPercentage(year, month);
        List<String> def = new ArrayList<>();
        for (Map.Entry<String, Double> e : perc.entrySet()) {
            if (e.getValue() < thresholdPercent) def.add(e.getKey());
        }
        return def;
    }
}
