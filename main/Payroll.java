import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Payroll {

    // File paths for the employee and attendance CSV files used by the system.
    static final String EMP_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";
    static final String ATT_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";

    // Valid usernames and password used for the required login check.
    static final String PASS = "12345";
    static final String USER_EMPLOYEE = "employee";
    static final String USER_PAYROLL = "payroll_staff";

    // Column indexes for employee data to make the code easier to read and maintain.
    static final int EMP_NUM_INDEX = 0;
    static final int EMP_NAME_INDEX = 1;
    static final int EMP_BDAY_INDEX = 2;
    static final int EMP_RATE_INDEX = 3;

    // Column indexes for attendance data so the program avoids using unclear raw index values.
    static final int ATT_EMPNO_COL = 0;
    static final int ATT_DATE_COL = 3;
    static final int ATT_LOGIN_COL = 4;
    static final int ATT_LOGOUT_COL = 5;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Load the CSV data once at the start so the files do not need to be read repeatedly.
        List<String[]> employeeList = readAllEmployees();
        List<String[]> attendanceList = readAllAttendance();

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        // Stop the program immediately if the username or password is incorrect.
        if (!isValidLogin(username, password)) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        // Continue to the correct menu based on the logged-in role.
        if (username.equals(USER_EMPLOYEE)) {
            employeeFlow(sc, employeeList);
        } else {
            payrollStaffFlow(sc, employeeList, attendanceList);
        }
    }

    // Checks if the entered credentials match the allowed system login values.
    static boolean isValidLogin(String user, String pass) {
        if (!PASS.equals(pass)) return false;
        return USER_EMPLOYEE.equals(user) || USER_PAYROLL.equals(user);
    }

    // Reads all employee records from the CSV and keeps only the fields needed by the payroll system.
    static List<String[]> readAllEmployees() {
        List<String[]> employees = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(EMP_FILE))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",", -1);
                if (data.length < 5) continue;

                String employeeNumber = data[0].trim();
                String lastName = data[1].trim();
                String firstName = data[2].trim();
                String birthday = data[3].trim();
                String hourlyRate = cleanNumber(data[data.length - 1].trim());

                employees.add(new String[] {
                    employeeNumber,
                    lastName + ", " + firstName,
                    birthday,
                    hourlyRate
                });
            }
        } catch (Exception e) {
            System.out.println("Error reading employee file.");
        }

        return employees;
    }

    // Reads all attendance records once so payroll processing can reuse the same loaded data.
    static List<String[]> readAllAttendance() {
        List<String[]> attendanceRecords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ATT_FILE))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",", -1);
                if (data.length <= ATT_LOGOUT_COL) continue;

                attendanceRecords.add(data);
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }

        return attendanceRecords;
    }

    // Finds the employee record that matches the entered employee number.
    static String[] findEmployee(String empNo, List<String[]> employeeList) {
        for (String[] employee : employeeList) {
            if (employee[EMP_NUM_INDEX].equals(empNo)) {
                return employee;
            }
        }
        return null;
    }

    // Employee access only allows viewing basic personal details using the employee number.
    static void employeeFlow(Scanner sc, List<String[]> employeeList) {
        System.out.println("\nDisplay options:");
        System.out.println("1. Enter your employee number");
        System.out.println("2. Exit the program");
        System.out.print("Choose: ");
        String choice = sc.nextLine().trim();

        if ("2".equals(choice)) return;

        if (!"1".equals(choice)) {
            System.out.println("Invalid option.");
            return;
        }

        System.out.print("Enter Employee #: ");
        String empNo = sc.nextLine().trim();

        String[] employee = findEmployee(empNo, employeeList);
        if (employee == null) {
            System.out.println("Employee number does not exist.");
        } else {
            System.out.println("\n===================================");
            System.out.println("Employee # : " + employee[EMP_NUM_INDEX]);
            System.out.println("Employee Name : " + employee[EMP_NAME_INDEX]);
            System.out.println("Birthday : " + employee[EMP_BDAY_INDEX]);
            System.out.println("===================================");
        }
    }

    // Payroll staff access allows payroll processing for one employee or all employees.
    static void payrollStaffFlow(Scanner sc, List<String[]> employeeList, List<String[]> attendanceList) {
        while (true) {
            System.out.println("\nDisplay options:");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if ("2".equals(choice)) return;

            if (!"1".equals(choice)) {
                System.out.println("Invalid option.");
                continue;
            }

            while (true) {
                System.out.println("\nProcess Payroll");
                System.out.println("1. One employee");
                System.out.println("2. All employees");
                System.out.println("3. Exit the program");
                System.out.print("Choose: ");
                String sub = sc.nextLine().trim();

                if ("3".equals(sub)) {
                    return;
                }

                if ("1".equals(sub)) {
                    System.out.print("Enter Employee #: ");
                    String empNo = sc.nextLine().trim();

                    processOneEmployeePayroll(empNo, employeeList, attendanceList);
                    return;
                } else if ("2".equals(sub)) {
                    processAllEmployeesPayroll(employeeList, attendanceList);
                    return;
                } else {
                    System.out.println("Invalid option.");
                }
            }
        }
    }

    // Processes payroll for one employee and displays all required cutoff records from June to December.
    static void processOneEmployeePayroll(String empNo, List<String[]> employeeList, List<String[]> attendanceList) {
        String[] employee = findEmployee(empNo, employeeList);
        if (employee == null) {
            System.out.println("Employee number does not exist.");
            return;
        }

        double hourlyRate = parseNumber(employee[EMP_RATE_INDEX]);
        if (Double.isNaN(hourlyRate)) {
            System.out.println("Invalid hourly rate in employee file.");
            return;
        }

        System.out.println("\n===================================");
        System.out.println("Employee # : " + employee[EMP_NUM_INDEX]);
        System.out.println("Employee Name : " + employee[EMP_NAME_INDEX]);
        System.out.println("Birthday : " + employee[EMP_BDAY_INDEX]);
        System.out.println("===================================");

        for (int month = 6; month <= 12; month++) {
            printPayrollForMonth(empNo, hourlyRate, month, attendanceList);
        }
    }

    // Reuses the same payroll logic for every employee record in the file.
    static void processAllEmployeesPayroll(List<String[]> employeeList, List<String[]> attendanceList) {
        for (String[] employee : employeeList) {
            String empNo = employee[EMP_NUM_INDEX];
            String name = employee[EMP_NAME_INDEX];
            String birthday = employee[EMP_BDAY_INDEX];
            double hourlyRate = parseNumber(employee[EMP_RATE_INDEX]);

            if (Double.isNaN(hourlyRate)) {
                System.out.println("\nSkipping " + empNo + " (invalid hourly rate).");
                continue;
            }

            System.out.println("\n===================================");
            System.out.println("Employee # : " + empNo);
            System.out.println("Employee Name : " + name);
            System.out.println("Birthday : " + birthday);
            System.out.println("===================================");

            for (int month = 6; month <= 12; month++) {
                printPayrollForMonth(empNo, hourlyRate, month, attendanceList);
            }
        }
    }

    // Monthly payroll is processed per cutoff first.
    // The program computes each cutoff separately, then adds both gross amounts
    // to get the monthly gross salary before applying deductions.
    // All government deductions are based on the monthly total
    // and are deducted only in the second cutoff only.
    static void printPayrollForMonth(String empNo, double hourlyRate, int month, List<String[]> attendanceList) {
        int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();

        double hours1 = computeCutoffHours(empNo, month, 1, 15, attendanceList);
        double hours2 = computeCutoffHours(empNo, month, 16, daysInMonth, attendanceList);

        double gross1 = hours1 * hourlyRate;
        double gross2 = hours2 * hourlyRate;

        double monthlyGross = gross1 + gross2;

        double[] deductions = computeDeductions(monthlyGross);
        double sss = deductions[0];
        double philHealth = deductions[1];
        double pagIbig = deductions[2];
        double tax = deductions[3];
        double totalDeductions = sss + philHealth + pagIbig + tax;

        double net1 = gross1;
        double net2 = gross2 - totalDeductions;

        String monthName = monthName(month);

        System.out.println("\nCutoff Date: " + monthName + " 1 to 15");
        System.out.println("Total Hours Worked : " + hours1);
        System.out.println("Gross Salary: " + gross1);
        System.out.println("Net Salary: " + net1);

        System.out.println("\nCutoff Date: " + monthName + " 16 to " + daysInMonth + " (Second payout includes all deductions)");
        System.out.println("Total Hours Worked : " + hours2);
        System.out.println("Gross Salary: " + gross2);
        System.out.println("Deductions:");
        System.out.println("    SSS: " + sss);
        System.out.println("    PhilHealth: " + philHealth);
        System.out.println("    Pag-IBIG: " + pagIbig);
        System.out.println("    Tax: " + tax);
        System.out.println("Total Deductions: " + totalDeductions);
        System.out.println("Net Salary: " + net2);
    }

    // Adds all valid work hours for the employee within the selected cutoff period.
    static double computeCutoffHours(String empNo, int month, int dayStart, int dayEnd, List<String[]> attendanceList) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        double totalHours = 0.0;

        for (String[] attendance : attendanceList) {
            if (!attendance[ATT_EMPNO_COL].trim().equals(empNo)) continue;

            String[] dateParts = attendance[ATT_DATE_COL].trim().split("/");
            if (dateParts.length != 3) continue;

            int recordMonth = Integer.parseInt(dateParts[0]);
            int day = Integer.parseInt(dateParts[1]);

            if (recordMonth != month) continue;
            if (day < dayStart || day > dayEnd) continue;

            LocalTime login = LocalTime.parse(attendance[ATT_LOGIN_COL].trim(), timeFormatter);
            LocalTime logout = LocalTime.parse(attendance[ATT_LOGOUT_COL].trim(), timeFormatter);

            totalHours += computeHours(login, logout);
        }

        return totalHours;
    }

// Counts only the payable hours within the allowed work schedule.
// Time before 8:00 AM and after 5:00 PM is not counted.
// Employees are given a 10-minute grace period, 
// so salary deductions only apply from 8:11 AM onward.
// A 1-hour lunch deduction is applied so only actual payable work hours are counted.
    static double computeHours(LocalTime login, LocalTime logout) {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime grace = LocalTime.of(8, 10);
        LocalTime end = LocalTime.of(17, 0);

        if (login.isBefore(start)) login = start;
        if (logout.isAfter(end)) logout = end;

        if (!login.isAfter(grace)) login = start;

        if (!logout.isAfter(login)) return 0.0;

        long minutesWorked = Duration.between(login, logout).toMinutes();

        if (minutesWorked > 60) minutesWorked -= 60;
        else return 0.0;

        double hoursWorked = minutesWorked / 60.0;
        if (hoursWorked > 8.0) hoursWorked = 8.0;

        return hoursWorked;
    }

    // Government deductions are based on the combined monthly gross salary,
    // then tax is computed after the non-tax deductions are removed first.
    static double[] computeDeductions(double monthlyGross) {
        double sss = computeSSS(monthlyGross);
        double philHealth = computePhilHealth(monthlyGross);
        double pagIbig = computePagIbig(monthlyGross);

        double taxableIncome = monthlyGross - (sss + philHealth + pagIbig);
        if (taxableIncome < 0) taxableIncome = 0;

        double tax = computeWithholdingTax(taxableIncome);

        return new double[] { sss, philHealth, pagIbig, tax };
    }

    // Uses salary brackets to match the employee's monthly gross to the correct SSS contribution amount.
    static double computeSSS(double monthlyGross) {
        if (monthlyGross < 3250) return 135.00;
        else if (monthlyGross < 3750) return 157.50;
        else if (monthlyGross < 4250) return 180.00;
        else if (monthlyGross < 4750) return 202.50;
        else if (monthlyGross < 5250) return 225.00;
        else if (monthlyGross < 5750) return 247.50;
        else if (monthlyGross < 6250) return 270.00;
        else if (monthlyGross < 6750) return 292.50;
        else if (monthlyGross < 7250) return 315.00;
        else if (monthlyGross < 7750) return 337.50;
        else if (monthlyGross < 8250) return 360.00;
        else if (monthlyGross < 8750) return 382.50;
        else if (monthlyGross < 9250) return 405.00;
        else if (monthlyGross < 9750) return 427.50;
        else if (monthlyGross < 10250) return 450.00;
        else if (monthlyGross < 10750) return 472.50;
        else if (monthlyGross < 11250) return 495.00;
        else if (monthlyGross < 11750) return 517.50;
        else if (monthlyGross < 12250) return 540.00;
        else if (monthlyGross < 12750) return 562.50;
        else if (monthlyGross < 13250) return 585.00;
        else if (monthlyGross < 13750) return 607.50;
        else if (monthlyGross < 14250) return 630.00;
        else if (monthlyGross < 14750) return 652.50;
        else if (monthlyGross < 15250) return 675.00;
        else if (monthlyGross < 15750) return 697.50;
        else if (monthlyGross < 16250) return 720.00;
        else if (monthlyGross < 16750) return 742.50;
        else if (monthlyGross < 17250) return 765.00;
        else if (monthlyGross < 17750) return 787.50;
        else if (monthlyGross < 18250) return 810.00;
        else if (monthlyGross < 18750) return 832.50;
        else if (monthlyGross < 19250) return 855.00;
        else if (monthlyGross < 19750) return 877.50;
        else if (monthlyGross < 20250) return 900.00;
        else if (monthlyGross < 20750) return 922.50;
        else if (monthlyGross < 21250) return 945.00;
        else if (monthlyGross < 21750) return 967.50;
        else if (monthlyGross < 22250) return 990.00;
        else if (monthlyGross < 22750) return 1012.50;
        else if (monthlyGross < 23250) return 1035.00;
        else if (monthlyGross < 23750) return 1057.50;
        else if (monthlyGross < 24250) return 1080.00;
        else if (monthlyGross < 24750) return 1102.50;
        else return 1125.00;
    }

    // Uses 3% of monthly gross, applies the minimum and maximum limits,
    // then returns only the employee's share of the contribution.
    static double computePhilHealth(double monthlyGross) {
        double premium = monthlyGross * 0.03;
        if (premium < 300.0) premium = 300.0;
        if (premium > 1800.0) premium = 1800.0;
        return premium * 0.50;
    }

    // Applies the correct employee contribution rate based on salary level
    // and limits the deduction to the maximum allowed amount.
    static double computePagIbig(double monthlyGross) {
        double rate;
        if (monthlyGross < 1000.0) rate = 0.0;
        else if (monthlyGross <= 1500.0) rate = 0.01;
        else rate = 0.02;

        double contribution = monthlyGross * rate;
        if (contribution > 100.0) contribution = 100.0;
        return contribution;
    }

    // Computes withholding tax from taxable income using the salary tax brackets,
    // after SSS, PhilHealth, and Pag-IBIG have already been deducted.
    static double computeWithholdingTax(double taxable) {
        if (taxable <= 20832.0) return 0.0;

        if (taxable < 33333.0) return (taxable - 20833.0) * 0.20;
        else if (taxable < 66667.0) return 2500.0 + (taxable - 33333.0) * 0.25;
        else if (taxable < 166667.0) return 10833.0 + (taxable - 66667.0) * 0.30;
        else if (taxable < 666667.0) return 40833.33 + (taxable - 166667.0) * 0.32;
        else return 200833.33 + (taxable - 666667.0) * 0.35;
    }

    // Converts the month number into a readable month name for payroll display.
    static String monthName(int month) {
        return switch (month) {
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Month " + month;
        };
    }

    // Removes extra symbols so numeric values from the CSV can be safely converted.
    static String cleanNumber(String s) {
        if (s == null) return "";
        return s.replaceAll("[^0-9.\\-]", "");
    }

    // Safely converts cleaned text values into numbers for payroll calculations.
    static double parseNumber(String s) {
        try {
            String cleaned = cleanNumber(s);
            if (cleaned.isEmpty()) return Double.NaN;
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return Double.NaN;
        }
    }
}