package com.prateek.learning.java.day02.collections;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        Employee employee1 =
                new Employee(101, "prateek@example.com", "Prateek");

        Employee employee2 =
                new Employee(101, "prateek@example.com", "P. Singh");

        Employee employee3 =
                new Employee(102, "other@example.com", "Other");

        System.out.println(employee1 == employee2);
        System.out.println(employee1.equals(employee2));

        Set<Employee> employees = new HashSet<>();
        employees.add(employee1);
        employees.add(employee2);
        employees.add(employee3);

        System.out.println(employees.size());

        Employee employee =
                new Employee(101, "prateek@example.com", "Prateek");


        employees.clear();
        int hashBeforeInsertion = employee.hashCode();
        employees.add(employee);

        employee.setEmail("updated@example.com");

        int hashAfterMutation = employee.hashCode();

        System.out.println("Hash before: " + hashBeforeInsertion);
        System.out.println("Hash after: " + hashAfterMutation);
        System.out.println("Set contains employee: " + employees.contains(employee));
        System.out.println("Set removes employee: " + employees.remove(employee));
    }
}
