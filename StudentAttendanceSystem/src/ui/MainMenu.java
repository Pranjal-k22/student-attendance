package com.attendance.ui;

import com.attendance.model.Student;
import com.attendance.service.AttendanceService;
import com.attendance.service.ReportService;
import com.attendance.service.StudentService;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.AttendanceRepository;
import com.attendance.util.ConsoleUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class MainMenu {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ReportService reportService;

    public MainMenu(Path dataDir) {
        Path studentsFile = dataDir.resolve("students.csv");
        Path attendanceFile = dataDir.resolve("attendance.csv");
        StudentRepository sr = new StudentRepository(studentsFile);
        AttendanceRepository ar = new AttendanceRepository(attendanceFile);
        this.studentService = new StudentService(sr);
        this.attendanceService = new AttendanceService(ar, sr);
        this.reportService = new ReportService(attendanceService, studentService, dataDir.resolve("reports"));
    }

    public void start() {
        while (true) {
            System.out.println("\n=== Student Attendance Management ===");
            System.out.println("1. Student Management");
            System.out.println("2. Mark Attendance");
            System.out.println("3. View Attendance for Date");
            System.out.println("4. Generate Monthly Report");
            System.out.println("5. Generate Defaulter List");
            System.out.println("6. Exit");
            int ch = ConsoleUtils.readInt("Choose option: ", -1);
            switch (ch) {
                case 1: studentManagement(); break;
                case 2: markAttendance(); break;
                case 3: viewAttendance(); break;
                case 4: generateReport(); break;
                case 5: generateDefaulter(); break;
                case 6: System.out.println("Exiting. Goodbye!"); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void studentManagement() {
        while (true) {
            System.out.println("\n-- Student Management --");
            System.out.println("1. Add Student");
            System.out.println("2. Update Student");
            System.out.println("3. Delete Student");
            System.out.println("4. View All Students");
            System.out.println("5. Back");
            int ch = ConsoleUtils.readInt("Choose: ", -1);
            switch (ch) {
                case 1:
                    String id = ConsoleUtils.readLine("Student ID: ").trim();
                    String name = ConsoleUtils.readLine("Name: ").trim();
                    String dept = ConsoleUtils.readLine("Department: ").trim();
                    boolean ok = studentService.addStudent(id, name, dept);
                    System.out.println(ok ? "Added." : "Failed (maybe ID exists).");
                    break;
                case 2:
                    id = ConsoleUtils.readLine("Student ID to update: ").trim();
                    Optional<Student> s = studentService.findById(id);
                    if (!s.isPresent()) { System.out.println("Not found."); break; }
                    name = ConsoleUtils.readLine("New name [" + s.get().getName() + "]: ").trim();
                    dept = ConsoleUtils.readLine("New dept [" + s.get().getDepartment() + "]: ").trim();
                    if (name.isEmpty()) name = s.get().getName();
                    if (dept.isEmpty()) dept = s.get().getDepartment();
                    boolean updated = studentService.updateStudent(id, name, dept);
                    System.out.println(updated ? "Updated." : "Failed.");
                    break;
                case 3:
                    id = ConsoleUtils.readLine("Student ID to delete: ").trim();
                    boolean del = studentService.deleteStudent(id);
                    System.out.println(del ? "Deleted." : "Failed (not found).");
                    break;
                case 4:
                    List<Student> all = studentService.listAll();
                    System.out.println(String.format("%-10s %-25s %-10s", "ID", "Name", "Dept"));
                    System.out.println("-------------------------------------------------");
                    for (Student st : all) {
                        System.out.println(String.format("%-10s %-25s %-10s", st.getId(), st.getName(), st.getDepartment()));
                    }
                    break;
                case 5: return;
                default: System.out.println("Invalid.");
            }
        }
    }

    private void markAttendance() {
        LocalDate date = ConsoleUtils.readDate("Attendance date", LocalDate.now());
        List<Student> students = studentService.listAll();
        if (students.isEmpty()) { System.out.println("No students present. Add students first."); return; }

        System.out.println("Press ENTER to mark default 'Present' or type 'A' then ENTER for Absent.");
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (Student s : students) {
            String prompt = String.format("[%s] %s (default P): ", s.getId(), s.getName());
            String ans = ConsoleUtils.readLine(prompt).trim();
            boolean present = true;
            if (!ans.isEmpty() && ans.equalsIgnoreCase("A")) present = false;
            map.put(s.getId(), present);
        }
        boolean ok = attendanceService.markAttendanceForDate(date, map);
        System.out.println(ok ? "Attendance saved." : "Failed to save attendance.");
    }

    private void viewAttendance() {
        LocalDate date = ConsoleUtils.readDate("Date to view", LocalDate.now());
        List<com.attendance.model.Attendance> list = attendanceService.getAttendanceByDate(date);
        if (list.isEmpty()) { System.out.println("No records for date: " + date); return; }
        System.out.println(String.format("%-10s %-12s %-5s", "StudentID", "Date", "Status"));
        for (com.attendance.model.Attendance a : list) {
            System.out.println(String.format("%-10s %-12s %-5s", a.getStudentId(), a.getDate(), a.isPresent() ? "P" : "A"));
        }
    }

    private void generateReport() {
        int year = ConsoleUtils.readInt("Enter year (e.g., 2025): ", LocalDate.now().getYear());
        int month = ConsoleUtils.readInt("Enter month number (1-12): ", LocalDate.now().getMonthValue());
        String content = reportService.generateMonthlyReport(year, month);
        System.out.println(content);
        String save = ConsoleUtils.readLine("Save report to file? (y/N): ").trim();
        if (save.equalsIgnoreCase("y")) {
            try {
                Path p = reportService.exportReportToFile(content, "monthly_" + year + "_" + month + ".txt");
                System.out.println("Saved to: " + p.toAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to save: " + e.getMessage());
            }
        }
    }

    private void generateDefaulter() {
        int year = ConsoleUtils.readInt("Enter year (e.g., 2025): ", LocalDate.now().getYear());
        int month = ConsoleUtils.readInt("Enter month number (1-12): ", LocalDate.now().getMonthValue());
        int threshold = ConsoleUtils.readInt("Threshold percent (e.g., 75): ", 75);
        String content = reportService.generateDefaulterList(year, month, threshold);
        System.out.println(content);
        String save = ConsoleUtils.readLine("Save defaulter list? (y/N): ").trim();
        if (save.equalsIgnoreCase("y")) {
            try {
                Path p = reportService.exportReportToFile(content, "defaulters_" + year + "_" + month + ".txt");
                System.out.println("Saved to: " + p.toAbsolutePath());
            } catch (Exception e) {
                System.out.println("Failed to save: " + e.getMessage());
            }
        }
    }

    // Entry point helper
    public static void main(String[] args) {
        Path dataDir = Path.of("data"); // relative folder named 'data'
        MainMenu app = new MainMenu(dataDir);
        app.start();
    }
}
