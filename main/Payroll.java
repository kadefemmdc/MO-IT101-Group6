import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Payroll {

    // CSV FILES
    static final String EMP_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";
    static final String ATT_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";

    // login credentials 
    static final String PASS = "12345";
    static final String USER_EMPLOYEE = "employee";
    static final String USER_PAYROLL = "payroll_staff";

    // Attendance CSV column indexes 
    static final int ATT_EMPNO_COL = 0;
    static final int ATT_DATE_COL = 3;
    static final int ATT_LOGIN_COL = 4;
    static final int ATT_LOGOUT_COL = 5;

    // LOGIN 
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        // If login is invalid, terminate 
        if (!isValidLogin(username, password)) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        // Route user based on role 
        if (username.equals(USER_EMPLOYEE)) {
            employeeFlow(sc);
        } else {
            payrollStaffFlow(sc);
        }
    }

    // Validates username and password 
    static boolean isValidLogin(String user, String pass) {
        if (!PASS.equals(pass)) return false;
        return USER_EMPLOYEE.equals(user) || USER_PAYROLL.equals(user);
    }

    // EMPLOYEE VIEW 
    static void employeeFlow(Scanner sc) {
        while (true) {
            System.out.println("\nDisplay options:");
            System.out.println("1. Enter your employee number");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            // Exit option terminates employee menu
            if ("2".equals(choice)) return;

            if (!"1".equals(choice)) {
                System.out.println("Invalid option.");
                continue;
            }

            System.out.print("Enter Employee #: ");
            String empNo = sc.nextLine().trim();

            // Look up employee record from CSV
            String[] emp = findEmployee(empNo);
            if (emp == null) {
                System.out.println("Employee number does not exist.");
            } else {
                // Display Employee #, Name, Birthday
                System.out.println("\n===================================");
                System.out.println("Employee # : " + emp[0]);
                System.out.println("Employee Name : " + emp[1]);
                System.out.println("Birthday : " + emp[2]);
                System.out.println("===================================");
            }
        }
    }

    // PAYROLL STAFF VIEW 
    static void payrollStaffFlow(Scanner sc) {
        while (true) {
            System.out.println("\nDisplay options:");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            // Exit option terminates payroll staff menu
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
                    processOneEmployeePayroll(empNo);
                } else if ("2".equals(sub)) {
                    processAllEmployeesPayroll();
                } else {
                    System.out.println("Invalid option.");
                }
            }
        }
    }

    // READS EMPLOYEE DETAILS FROM CSV 
    static String[] findEmployee(String empNo) {
        try (BufferedReader br = new BufferedReader(new FileReader(EMP_FILE))) {
            br.readLine(); 
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",", -1);
                if (data.length < 5) continue;

                String fileEmpNo = data[0].trim();
                if (!fileEmpNo.equals(empNo)) continue;

                String last = data[1].trim();
                String first = data[2].trim();
                String bday = data[3].trim();

                String rateRaw = data[data.length - 1].trim();
                String rateClean = cleanNumber(rateRaw);

                return new String[] { fileEmpNo, last + ", " + first, bday, rateClean };
            }
        } catch (Exception e) {
            System.out.println("Error reading employee file.");
        }
        return null;
    }

    // Processes payroll for one employee
    static void processOneEmployeePayroll(String empNo) {
        String[] emp = findEmployee(empNo);
        if (emp == null) {
            System.out.println("Employee number does not exist.");
            return;
        }

        // Convert hourly rate string to a number for salary computation
        double hourlyRate = parseNumber(emp[3]);
        if (Double.isNaN(hourlyRate)) {
            System.out.println("Invalid hourly rate in employee file.");
            return;
        }

        // Display employee details before payroll outputs 
        System.out.println("\n===================================");
        System.out.println("Employee # : " + emp[0]);
        System.out.println("Employee Name : " + emp[1]);
        System.out.println("Birthday : " + emp[2]);
        System.out.println("===================================");

        // range: June to December
        for (int month = 6; month <= 12; month++) {
            printPayrollForMonth(empNo, hourlyRate, month);
        }
    }

    // Processes payroll for all employees 
    static void processAllEmployeesPayroll() {
        try (BufferedReader br = new BufferedReader(new FileReader(EMP_FILE))) {
            br.readLine(); 
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",", -1);
                if (data.length < 5) continue;

                String empNo = data[0].trim();
                String last = data[1].trim();
                String first = data[2].trim();
                String bday = data[3].trim();

                // Get and parse hourly rate from employee CSV
                String rateRaw = data[data.length - 1].trim();
                double hourlyRate = parseNumber(rateRaw);

                if (Double.isNaN(hourlyRate)) {
                    System.out.println("\nSkipping " + empNo + " (invalid hourly rate).");
                    continue;
                }

                // Display employee details before payroll outputs 
                System.out.println("\n===================================");
                System.out.println("Employee # : " + empNo);
                System.out.println("Employee Name : " + last + ", " + first);
                System.out.println("Birthday : " + bday);
                System.out.println("===================================");

                // range: June to December
                for (int month = 6; month <= 12; month++) {
                    printPayrollForMonth(empNo, hourlyRate, month);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading employee file.");
        }
    }

    // Prints payroll for two cutoffs 
    static void printPayrollForMonth(String empNo, double hourlyRate, int month) {
        int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();

        // Compute total hours for each cutoff period
        double hours1 = computeCutoffHours(empNo, month, 1, 15);
        double hours2 = computeCutoffHours(empNo, month, 16, daysInMonth);

        // Compute gross pay per cutoff
        double gross1 = hours1 * hourlyRate;
        double gross2 = hours2 * hourlyRate;

        // add both cutoffs before deductions
        double monthlyGross = gross1 + gross2;

        // Compute deductions based on monthly gross salary
        double[] d = computeDeductions(monthlyGross);
        double sss = d[0], phil = d[1], pagibig = d[2], tax = d[3];
        double totalDed = sss + phil + pagibig + tax;

        // First cutoff has no deductions; second cutoff includes all deductions
        double net1 = gross1;
        double net2 = gross2 - totalDed;

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
        System.out.println("    PhilHealth: " + phil);
        System.out.println("    Pag-IBIG: " + pagibig);
        System.out.println("    Tax: " + tax);
        System.out.println("Total Deductions: " + totalDed);
        System.out.println("Net Salary: " + net2);
    }

    // Reads attendance CSV and totals work hours for the given cutoff date range
    static double computeCutoffHours(String empNo, int month, int dayStart, int dayEnd) {
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("H:mm");
        double total = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(ATT_FILE))) {
            br.readLine(); 
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",", -1);
                if (data.length <= ATT_LOGOUT_COL) continue;

                // Only compute hours for the selected employee
                if (!data[ATT_EMPNO_COL].trim().equals(empNo)) continue;

                // Filter records by month and cutoff day range
                String[] dateParts = data[ATT_DATE_COL].trim().split("/");
                if (dateParts.length != 3) continue;

                int recordMonth = Integer.parseInt(dateParts[0]);
                int day = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[2]);

                if (year != 2024) continue;
                if (recordMonth != month) continue;
                if (day < dayStart || day > dayEnd) continue;

                // Parse login/logout time and compute daily hours
                LocalTime login = LocalTime.parse(data[ATT_LOGIN_COL].trim(), tf);
                LocalTime logout = LocalTime.parse(data[ATT_LOGOUT_COL].trim(), tf);

                total += computeHours(login, logout);
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }

        return total;
    }

    // Computes daily work hours based on attendance rules:
    // - Counts only 8:00 AM–5:00 PM
    // - Applies 10-minute grace period 
    // - Deducts 1 hour lunch break
    static double computeHours(LocalTime login, LocalTime logout) {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime grace = LocalTime.of(8, 10);
        LocalTime end = LocalTime.of(17, 0);

        if (login.isBefore(start)) login = start;
        if (logout.isAfter(end)) logout = end;

        if (!login.isAfter(grace)) login = start;

        if (!logout.isAfter(login)) return 0.0;

        long minutes = Duration.between(login, logout).toMinutes();

        if (minutes > 60) minutes -= 60;
        else return 0.0;

        double hours = minutes / 60.0;
        if (hours > 8.0) hours = 8.0;

        return hours;
    }

    // DEDUCTION CALCULATIONS 
    static double[] computeDeductions(double monthlyGross) {
        double sss = computeSSS(monthlyGross);
        double phil = computePhilHealth(monthlyGross);
        double pagibig = computePagIbig(monthlyGross);

        double taxableIncome = monthlyGross - (sss + phil + pagibig);
        if (taxableIncome < 0) taxableIncome = 0;

        double tax = computeWithholdingTax(taxableIncome);

        return new double[] { sss, phil, pagibig, tax };
    }

    // SSS Contribution
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

    // PhilHealth Contribution
    static double computePhilHealth(double monthlyGross) {
        double premium = monthlyGross * 0.03;
        if (premium < 300.0) premium = 300.0;
        if (premium > 1800.0) premium = 1800.0;
        return premium * 0.50;
    }

    // Pag-IBIG Contribution
    static double computePagIbig(double monthlyGross) {
        double rate;
        if (monthlyGross < 1000.0) rate = 0.0;
        else if (monthlyGross <= 1500.0) rate = 0.01;
        else rate = 0.02;

        double contrib = monthlyGross * rate;
        if (contrib > 100.0) contrib = 100.0;
        return contrib;
    }

    // Withholding Tax Calculation
    static double computeWithholdingTax(double taxable) {
        if (taxable <= 20832.0) return 0.0;

        if (taxable < 33333.0) return (taxable - 20833.0) * 0.20;
        else if (taxable < 66667.0) return 2500.0 + (taxable - 33333.0) * 0.25;
        else if (taxable < 166667.0) return 10833.0 + (taxable - 66667.0) * 0.30;
        else if (taxable < 666667.0) return 40833.33 + (taxable - 166667.0) * 0.32;
        else return 200833.33 + (taxable - 666667.0) * 0.35;
    }

    // Converts month number to printable month name 
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

    // Removes non-numeric characters from values read from CSV (for safe parsing)
    static String cleanNumber(String s) {
        if (s == null) return "";
        return s.replaceAll("[^0-9.\\-]", "");
    }

    // Parses numeric strings from CSV into double values (returns NaN if invalid)
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