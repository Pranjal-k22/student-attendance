package com.attendance.model;

public class Student {
    private String id;
    private String name;
    private String department;

    public Student(String id, String name, String department) {
        this.id = id.trim();
        this.name = name.trim();
        this.department = department.trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }

    public void setName(String name) { this.name = name.trim(); }
    public void setDepartment(String department) { this.department = department.trim(); }

    @Override
    public String toString() {
        return id + "," + name + "," + department;
    }

    public static Student fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 3) return null;
        return new Student(parts[0], parts[1], parts[2]);
    }
}
