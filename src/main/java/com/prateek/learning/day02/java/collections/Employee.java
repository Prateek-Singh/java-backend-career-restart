package com.prateek.learning.day02.java.collections;

import java.util.Objects;

public class Employee {


    // Fields used in equals() and hashCode() should ideally be immutable
    // when objects may be stored in HashMap or HashSet.
    private int employeeId;
    private String email;
    private String name;

    // Preferred design:
    // private final int employeeId;
    // private final String email;
    // private String name;

    public Employee() {
    }

    public Employee(int employeeId, String email, String name) {
        this.employeeId = employeeId;
        this.email = email;
        this.name = name;
    }

    public int getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Employee employee = (Employee) o;

        return employeeId == employee.employeeId
                && Objects.equals(email, employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, email);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
