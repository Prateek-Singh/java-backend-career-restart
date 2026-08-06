package com.prateek.learning.java.day02;

import com.prateek.learning.java.day02.collections.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    @Test
    void employeesWithSameIdAndEmailShouldBeEqual() {
        Employee employee1 =
                new Employee(101, "prateek@example.com", "Prateek");

        Employee employee2 =
                new Employee(101, "prateek@example.com", "P. Singh");

        assertEquals(employee1, employee2);
        assertEquals(employee1.hashCode(), employee2.hashCode());
    }

    @Test
    void hashSetShouldIgnoreLogicallyDuplicateEmployee() {
        Set<Employee> employees = new HashSet<>();

        employees.add(new Employee(
                101,
                "prateek@example.com",
                "Prateek"
        ));

        employees.add(new Employee(
                101,
                "prateek@example.com",
                "P. Singh"
        ));

        employees.add(new Employee(
                102,
                "other@example.com",
                "Other"
        ));

        assertEquals(2, employees.size());
    }

    @Test
    void changingKeyFieldsAfterInsertionShouldBreakHashSetLookup() {
        Employee employee =
                new Employee(101, "prateek@example.com", "Prateek");

        Set<Employee> employees = new HashSet<>();
        employees.add(employee);

        assertTrue(employees.contains(employee));

        employee.setEmail("updated@example.com");

        assertFalse(employees.contains(employee));

        assertEquals(1, employees.size());
    }

    @Test
    void shouldDemonstrateRiskOfMutatingHashSetElement() {
        Employee employee =
                new Employee(101, "prateek@example.com", "Prateek");

        Set<Employee> employees = new HashSet<>();

        int hashBeforeInsertion = employee.hashCode();
        employees.add(employee);

        employee.setEmail("updated@example.com");

        int hashAfterMutation = employee.hashCode();

        assertNotEquals(hashBeforeInsertion, hashAfterMutation);
        assertEquals(1, employees.size());

        System.out.println("Hash before: " + hashBeforeInsertion);
        System.out.println("Hash after: " + hashAfterMutation);
        System.out.println("Set contains employee: " + employees.contains(employee));
        System.out.println("Set removes employee: " + employees.remove(employee));
    }

}
