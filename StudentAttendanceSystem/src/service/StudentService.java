package com.attendance.service;

import com.attendance.model.Student;
import com.attendance.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public List<Student> listAll() {
        return repo.getAll();
    }

    public boolean addStudent(String id, String name, String dept) {
        if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty()) return false;
        Student s = new Student(id.trim(), name.trim(), dept == null ? "" : dept.trim());
        return repo.add(s);
    }

    public boolean updateStudent(String id, String name, String dept) {
        Optional<Student> opt = repo.findById(id);
        if (!opt.isPresent()) return false;
        Student s = opt.get();
        s.setName(name);
        s.setDepartment(dept);
        return repo.update(s);
    }

    public boolean deleteStudent(String id) {
        return repo.delete(id);
    }

    public Optional<Student> findById(String id) {
        return repo.findById(id);
    }
}
