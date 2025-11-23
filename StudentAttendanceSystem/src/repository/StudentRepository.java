package com.attendance.repository;

import com.attendance.model.Student;
import com.attendance.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class StudentRepository {
    private final Path file;

    public StudentRepository(Path file) {
        this.file = file;
        FileUtils.ensureFile(file);
    }

    public List<Student> getAll() {
        List<Student> list = new ArrayList<>();
        try {
            List<String> lines = FileUtils.readAllLines(file);
            for (String l : lines) {
                Student s = Student.fromCsv(l);
                if (s != null) list.add(s);
            }
        } catch (IOException e) {
            System.err.println("Failed to read students: " + e.getMessage());
        }
        return list;
    }

    public Optional<Student> findById(String id) {
        return getAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public boolean add(Student s) {
        if (findById(s.getId()).isPresent()) return false;
        try {
            FileUtils.appendLine(file, s.toString());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to add student: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Student s) {
        List<Student> all = getAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(s.getId())) {
                all.set(i, s);
                found = true;
                break;
            }
        }
        if (!found) return false;
        try {
            FileUtils.writeLines(file, toLines(all));
            return true;
        } catch (IOException e) {
            System.err.println("Failed to update student: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        List<Student> all = getAll();
        boolean removed = all.removeIf(s -> s.getId().equals(id));
        if (!removed) return false;
        try {
            FileUtils.writeLines(file, toLines(all));
            return true;
        } catch (IOException e) {
            System.err.println("Failed to delete student: " + e.getMessage());
            return false;
        }
    }

    private List<String> toLines(List<Student> list) {
        List<String> out = new ArrayList<>();
        for (Student s : list) out.add(s.toString());
        return out;
    }
}
