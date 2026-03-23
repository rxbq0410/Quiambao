package com.mycompany.quiambao;

import java.io.*;
import java.util.Scanner;

public class MotorPH_ReadFromFile {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        String filePath = "employee_data.txt";

        int choice;

        do {
            System.out.print("\nEnter Employee Number: ");
            String inputNumber = scanner.nextLine();

            boolean found = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

                String line;

                // Read each line from file
                while ((line = reader.readLine()) != null) {

                    String[] data = line.split(",");

                    // Validate correct format
                    if (data.length < 3) {
                        continue;
                    }

                    String empNumber = data[0].trim();
                    String name = data[1].trim();
                    double grossSalary;

                    try {
                        grossSalary = Double.parseDouble(data[2].trim());

                        if (grossSalary < 0) {
                            continue;
                        }

                    } catch (NumberFormatException e) {
                        continue;
                    }

                    // Check if employee matches input
                    if (empNumber.equals(inputNumber)) {

                        found = true;

                        // Compute deductions
                        double sss = computeSSS(grossSalary);
                        double philhealth = computePhilHealth(grossSalary);
                        double pagibig = computePagIBIG(grossSalary);
                        double tax = computeTax(grossSalary);

                        double totalDeductions = sss + philhealth + pagibig + tax;
                        double netPay = grossSalary - totalDeductions;

                        // Display payroll details
                        System.out.println("\n===== PAYROLL DETAILS =====");
                        System.out.println("Employee Number: " + empNumber);
                        System.out.println("Employee Name: " + name);
                        System.out.println("Gross Salary: " + grossSalary);

                        System.out.println("\n--- Deductions ---");
                        System.out.println("SSS: " + sss);
                        System.out.println("PhilHealth: " + philhealth);
                        System.out.println("Pag-IBIG: " + pagibig);
                        System.out.println("Income Tax: " + tax);

                        System.out.println("\nTotal Deductions: " + totalDeductions);
                        System.out.println("Net Salary: " + netPay);
                        System.out.println("============================");

                        break;
                    }
                }

                // If employee not found
                if (!found) {
                    System.out.println("Employee does not exist.");
                }

            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }

            // Ask user to continue
            System.out.print("\nProcess another employee? (1 = Yes, 2 = No): ");
            choice = scanner.nextInt();
            scanner.nextLine(); // clear buffer

        } while (choice == 1);

        System.out.println("Program terminated.");
    }

    // Deduction Methods (you can adjust based on Task 9 formulas)

    // SSS calculation
    public static double computeSSS(double salary) {
        return salary * 0.045;
    }

    // PhilHealth calculation
    public static double computePhilHealth(double salary) {
        return salary * 0.02;
    }

    // Pag-IBIG fixed contribution
    public static double computePagIBIG(double salary) {
        return 100;
    }

    // Income Tax calculation
    public static double computeTax(double salary) {
        return salary * 0.10;
    }
}