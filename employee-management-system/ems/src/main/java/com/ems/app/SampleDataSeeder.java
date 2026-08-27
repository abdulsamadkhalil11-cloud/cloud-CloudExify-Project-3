package com.ems.app;

import com.ems.model.*;
import com.ems.model.enums.Department;
import com.ems.model.enums.Gender;
import com.ems.model.enums.LeaveType;
import com.ems.service.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Populates a brand-new system with a believable starting roster so
 * the app is demonstrable immediately, without requiring the grader
 * to type in employees by hand first. Only runs when no saved data
 * file exists yet.
 */
public class SampleDataSeeder {

    public static Admin seed(EmployeeService employeeService, LeaveService leaveService,
                              PerformanceService performanceService) throws Exception {

        Admin admin = new Admin("admin", "Admin@123");

        Manager eng = new Manager("MGR-0001", "Ayesha Raza", 34, Gender.FEMALE, "35202-1234567-1",
                "+923001234567", "ayesha.raza@ems.com", "House 12, Street 4, Islamabad",
                Department.ENGINEERING, "Engineering Manager", 180000, LocalDate.of(2021, 3, 15),
                LocalDate.of(1992, 2, 18), "ayesha.raza", "Manager@123", 6, 500000);

        Manager sales = new Manager("MGR-0002", "Bilal Ahmed", 41, Gender.MALE, "35202-2345678-2",
                "+923011234567", "bilal.ahmed@ems.com", "Flat 3B, Wah Cantt",
                Department.SALES, "Sales Manager", 170000, LocalDate.of(2019, 6, 1),
                LocalDate.of(1985, 11, 3), "bilal.ahmed", "Manager@123", 5, 350000);

        Developer dev1 = new Developer("DEV-0001", "Hassan Ali", 26, Gender.MALE, "35202-3456789-3",
                "+923021234567", "hassan.ali@ems.com", "Sector G-9, Islamabad",
                Department.ENGINEERING, "Backend Developer", 120000, LocalDate.of(2023, 1, 10),
                LocalDate.of(2000, 8, 4), "hassan.ali", "Dev@1234", "Java", "Spring Boot", "hassanali-dev");
        dev1.setReportsToManagerId("MGR-0001");

        Developer dev2 = new Developer("DEV-0002", "Sara Khan", 24, Gender.FEMALE, "35202-4567890-4",
                "+923031234567", "sara.khan@ems.com", "DHA Phase 2, Islamabad",
                Department.ENGINEERING, "Frontend Developer", 110000, LocalDate.of(2023, 8, 1),
                LocalDate.of(2002, 5, 22), "sara.khan", "Dev@1234", "JavaScript", "React", "sarakhan-dev");
        dev2.setReportsToManagerId("MGR-0001");

        Developer dev3 = new Developer("DEV-0003", "Usman Tariq", 29, Gender.MALE, "35202-5678901-5",
                "+923041234567", "usman.tariq@ems.com", "Wah Cantt",
                Department.ENGINEERING, "Mobile Developer", 130000, LocalDate.of(2022, 2, 20),
                LocalDate.of(1997, 12, 30), "usman.tariq", "Dev@1234", "Kotlin", "Jetpack Compose", "usmant-dev");
        dev3.setReportsToManagerId("MGR-0001");

        HR hr1 = new HR("HR-0001", "Mahnoor Iqbal", 31, Gender.FEMALE, "35202-6789012-6",
                "+923051234567", "mahnoor.iqbal@ems.com", "F-10, Islamabad",
                Department.HUMAN_RESOURCES, "HR Executive", 100000, LocalDate.of(2020, 9, 5),
                LocalDate.of(1995, 3, 14), "mahnoor.iqbal", "Hr@12345");

        HR hr2 = new HR("HR-0002", "Ahmed Raza", 38, Gender.MALE, "35202-7890123-7",
                "+923061234567", "ahmed.raza@ems.com", "Taxila Road, Wah Cantt",
                Department.HUMAN_RESOURCES, "Senior HR Executive", 115000, LocalDate.of(2018, 11, 12),
                LocalDate.of(1988, 7, 9), "ahmed.raza", "Hr@12345");

        Employee[] all = {eng, sales, dev1, dev2, dev3, hr1, hr2};
        for (Employee e : all) {
            employeeService.addEmployee(e);
        }

        // Reserve the ID counters so the next auto-generated ID doesn't collide.
        com.ems.util.IdGenerator.reserve("MGR", 2);
        com.ems.util.IdGenerator.reserve("DEV", 3);
        com.ems.util.IdGenerator.reserve("HR", 2);

        seedAttendance(dev1);
        seedAttendance(dev2);
        seedAttendance(eng);

        seedPerformance(performanceService, dev1, 88, "Consistently ships clean, well-tested code.", "MGR-0001");
        seedPerformance(performanceService, dev2, 76, "Good UI work, needs to speed up code reviews.", "MGR-0001");
        seedPerformance(performanceService, dev3, 91, "Top performer this quarter, shipped the mobile app ahead of schedule.", "MGR-0001");
        seedPerformance(performanceService, eng, 84, "Strong team leadership.", "admin");
        seedPerformance(performanceService, hr1, 79, "Reliable, could improve interview turnaround time.", "admin");

        dev3.submitProject("Mobile Attendance App");
        dev3.addAchievement("Top Performer - Q1 2026");
        dev1.submitProject("Employee API v2");
        hr1.scheduleInterview("Zainab Malik", LocalDate.now().plusDays(3));

        leaveService.applyLeave("DEV-0002", LeaveType.CASUAL,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(9), "Family event");
        leaveService.applyLeave("DEV-0001", LeaveType.MEDICAL,
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(18), "Flu");
        leaveService.getAll(); // no-op read, keeps symmetry with other seed() calls

        return admin;
    }

    private static void seedAttendance(Employee employee) {
        for (int i = 5; i >= 1; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            if (day.getDayOfWeek().getValue() >= 6) {
                continue; // skip weekends
            }
            LocalTime checkIn = LocalTime.of(9, (i == 3) ? 25 : 2); // one late day for realism
            Attendance record = new Attendance(day, checkIn);
            record.checkOut(LocalTime.of(17, 10));
            employee.addAttendanceRecord(record);
        }
    }

    private static void seedPerformance(PerformanceService performanceService, Employee employee,
                                         double rating, String feedback, String reviewerId) {
        performanceService.addReview(employee, rating, feedback, reviewerId);
    }
}
