package com.mycompany.quiambao;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MotorPH_ReadFromFile {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String empFile = "src/main/resources/employee_details.txt";
        String attFile = "src/main/resources/attendance_records.txt";

        // --- LOGIN SYSTEM ---
        System.out.print("Enter Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        if (!( (username.equals("employee") || username.equals("payroll_staff")) && password.equals("12345") )) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        // --- MENU BASED ON ROLE ---
        if (username.equals("employee")) {
            System.out.println("\n1. Enter Employee Number");
            System.out.println("2. Exit");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                processSingleEmployee(sc, empFile, attFile);
            } else {
                System.out.println("Program terminated.");
            }

        } else if (username.equals("payroll_staff")) {
            System.out.println("\n1. Process Payroll");
            System.out.println("2. Exit");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                System.out.println("\n1. One employee");
                System.out.println("2. All employees");
                System.out.println("3. Exit");
                int subChoice = sc.nextInt();
                sc.nextLine();

                if (subChoice == 1) {
                    processSingleEmployee(sc, empFile, attFile);
                } else if (subChoice == 2) {
                    processAllEmployees(empFile, attFile);
                } else {
                    System.out.println("Program terminated.");
                }
            } else {
                System.out.println("Program terminated.");
            }
        }

        sc.close();
        System.out.println("\nProgram terminated.");
    }

    // --- PROCESS ONE EMPLOYEE ---
    static void processSingleEmployee(Scanner sc, String empFile, String attFile) {
        System.out.print("Enter Employee #: ");
        String inputEmpNo = sc.nextLine().trim();

        Map<String, String[]> employees = readEmployees(empFile);
        if (!employees.containsKey(inputEmpNo)) {
            System.out.println("Employee number does not exist.");
            return;
        }

        String[] emp = employees.get(inputEmpNo);
        String empNo = emp[0], lastName = emp[1], firstName = emp[2], birthday = emp[3];
        System.out.println("\n===================================");
        System.out.println("Employee # : " + empNo);
        System.out.println("Employee Name : " + lastName + ", " + firstName);
        System.out.println("Birthday : " + birthday);
        System.out.println("===================================");

        computePayroll(inputEmpNo, attFile);
    }

    // --- PROCESS ALL EMPLOYEES ---
    static void processAllEmployees(String empFile, String attFile) {
        Map<String, String[]> employees = readEmployees(empFile);
        for (String empNo : employees.keySet()) {
            String[] emp = employees.get(empNo);
            System.out.println("\n===================================");
            System.out.println("Employee # : " + empNo);
            System.out.println("Employee Name : " + emp[1] + ", " + emp[2]);
            System.out.println("Birthday : " + emp[3]);
            System.out.println("===================================");
            computePayroll(empNo, attFile);
        }
    }

    // --- READ EMPLOYEE FILE ---
    static Map<String, String[]> readEmployees(String empFile) {
        Map<String, String[]> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                map.put(data[0], data); // empNo as key
            }
        } catch (Exception e) {
            System.out.println("Error reading employee file.");
        }
        return map;
    }

    // --- COMPUTE PAYROLL ---
    static void computePayroll(String empNo, String attFile) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
        try (BufferedReader br = new BufferedReader(new FileReader(attFile))) {
            br.readLine(); // skip header
            String line;
            Map<String, Double[]> monthHours = new TreeMap<>();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                if (!data[0].equals(empNo)) continue;

                String[] dateParts = data[1].split("/"); // MM/DD/YYYY
                int month = Integer.parseInt(dateParts[0]);
                int day = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[2]);
                if (year != 2024 || month < 6 || month > 12) continue;

                LocalTime login = LocalTime.parse(data[2].trim(), timeFormat);
                LocalTime logout = LocalTime.parse(data[3].trim(), timeFormat);
                double hours = computeHours(login, logout);

                Double[] halves = monthHours.getOrDefault(month + "", new Double[]{0.0, 0.0});
                if (day <= 15) halves[0] += hours;
                else halves[1] += hours;
                monthHours.put(month + "", halves);
            }

            for (String monthStr : monthHours.keySet()) {
                int month = Integer.parseInt(monthStr);
                Double[] halves = monthHours.get(monthStr);
                int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();
                String monthName = Month.of(month).name();

                // First cutoff (June 1-15, no deductions)
                System.out.println("\nCutoff Date: " + monthName + " 1 to 15");
                System.out.println("Total Hours Worked : " + halves[0]);
                System.out.println("Gross Salary: " + (halves[0] * 100));
                System.out.println("Net Salary: " + (halves[0] * 100)); // no deductions

                // Second cutoff (June 16-30, include deductions)
                System.out.println("\nCutoff Date: " + monthName + " 16 to " + daysInMonth);
                System.out.println("Total Hours Worked : " + halves[1]);
                double gross = halves[1] * 100;
                double sss = 100; // example fixed deduction
                double philHealth = 50;
                double pagIBIG = 50;
                double tax = gross * 0.1;
                double totalDeduction = sss + philHealth + pagIBIG + tax;
                System.out.println("Gross Salary: " + gross);
                System.out.println("Each Deduction:");
                System.out.println("  SSS: " + sss);
                System.out.println("  PhilHealth: " + philHealth);
                System.out.println("  Pag-IBIG: " + pagIBIG);
                System.out.println("  Tax: " + tax);
                System.out.println("Total Deductions: " + totalDeduction);
                System.out.println("Net Salary: " + (gross - totalDeduction));
            }

        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }
    }

    // --- CALCULATE HOURS WORKED ---
    static double computeHours(LocalTime login, LocalTime logout) {
        LocalTime graceTime = LocalTime.of(8, 10);
        LocalTime cutoffTime = LocalTime.of(17, 0);
        if (logout.isAfter(cutoffTime)) logout = cutoffTime;

        long minutesWorked = Duration.between(login, logout).toMinutes();
        if (minutesWorked > 60) minutesWorked -= 60; // lunch
        else minutesWorked = 0;

        double hours = minutesWorked / 60.0;
        if (!login.isAfter(graceTime)) return 8.0;
        return Math.min(hours, 8.0);
    }
}