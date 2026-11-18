package com.hospital.ui;

import com.hospital.config.DatabaseManager;
import com.hospital.exception.HospitalException;
import com.hospital.model.*;
import com.hospital.service.*;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive Console User Interface for the Hospital Management System.
 */
public class ConsoleMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final RoomService roomService = new RoomService();
    private final BillingService billingService = new BillingService();

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Select an option (0-6): ");
            System.out.println();
            try {
                switch (choice) {
                    case 1 -> handlePatientManagement();
                    case 2 -> handleDoctorManagement();
                    case 3 -> handleAppointmentManagement();
                    case 4 -> handleRoomManagement();
                    case 5 -> handleBillingManagement();
                    case 6 -> showHospitalDashboard();
                    case 0 -> {
                        System.out.println("Thank you for using MedCare Hospital Management System. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please choose between 0 and 6.");
                }
            } catch (HospitalException e) {
                System.out.println("[ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[UNEXPECTED ERROR] " + e.getMessage());
            }
            if (running) {
                pause();
            }
        }
    }

    private void printBanner() {
        String dbInfo = DatabaseManager.getInstance().getDbType().toUpperCase() + " (" + DatabaseManager.getInstance().getDatabaseName() + ")";
        System.out.println("========================================================================");
        System.out.println("             MEDCARE HOSPITAL MANAGEMENT SYSTEM (HMS)                   ");
        System.out.println("        Powered by Java OOP (Inheritance, Polymorphism) & " + dbInfo);
        System.out.println("========================================================================");
    }

    private void printMainMenu() {
        System.out.println("\n------------- MAIN MENU -------------");
        System.out.println(" 1.  Patient Management");
        System.out.println(" 2.  Doctor & Department Management");
        System.out.println(" 3.  Appointment Scheduling");
        System.out.println(" 4.  Inpatient Room & Bed Allocation");
        System.out.println(" 5.  Billing, Invoicing & Payments");
        System.out.println(" 6.  Hospital Overview Dashboard");
        System.out.println(" 0.  Exit System");
        System.out.println("-------------------------------------");
    }

    // =========================================================================
    // 1. PATIENT MANAGEMENT
    // =========================================================================
    private void handlePatientManagement() {
        System.out.println("=== PATIENT MANAGEMENT ===");
        System.out.println(" 1. View All Patients");
        System.out.println(" 2. Search Patient by Name");
        System.out.println(" 3. Register New Patient");
        System.out.println(" 4. View Detailed Patient Profile");
        int sub = readInt("Enter choice (1-4): ");
        System.out.println();

        switch (sub) {
            case 1 -> listAllPatients();
            case 2 -> {
                String kw = readString("Enter patient name or keyword: ");
                List<Patient> results = patientService.searchPatients(kw);
                renderPatientsTable(results);
            }
            case 3 -> {
                System.out.println("--- Register New Patient ---");
                String name = readString("Full Name: ");
                String gender = readString("Gender (Male/Female/Other): ");
                String phone = readString("Phone Number (7-15 digits): ");
                String email = readString("Email Address (or press enter for N/A): ");
                String blood = readString("Blood Group (e.g. O+, A+, B-): ");
                String emergency = readString("Emergency Contact (Name & Phone): ");
                String history = readString("Medical History / Allergies: ");

                Patient p = new Patient(name, gender, phone, email, blood, emergency, history);
                Patient saved = patientService.registerPatient(p);
                System.out.println("[SUCCESS] Patient registered successfully with Patient ID: " + saved.getId());
            }
            case 4 -> {
                int pid = readInt("Enter Patient ID: ");
                Patient p = patientService.getPatientById(pid);
                System.out.println("\n------------------------------------------------------------");
                System.out.println("                PATIENT MEDICAL PROFILE                    ");
                System.out.println("------------------------------------------------------------");
                System.out.println(" ID:                 " + p.getId());
                System.out.println(" Name:               " + p.getName());
                System.out.println(" Role:               " + p.getRole());
                System.out.println(" Gender:             " + p.getGender());
                System.out.println(" Contact:            " + p.getContactInfo());
                System.out.println(" Blood Group:        " + p.getBloodGroup());
                System.out.println(" Emergency Contact:  " + p.getEmergencyContact());
                System.out.println(" Medical History:    " + p.getMedicalHistory());
                System.out.println(" Registered Since:   " + p.getFormattedRegistrationDate());
                System.out.println("------------------------------------------------------------");

                // Appointments for this patient
                List<Appointment> appts = appointmentService.getAppointmentsByPatient(pid);
                System.out.println("\n* Appointments (" + appts.size() + "):");
                if (appts.isEmpty()) {
                    System.out.println("  No appointment history.");
                } else {
                    for (Appointment a : appts) {
                        System.out.println("  - #" + a.getId() + " with Dr. " + a.getDoctorName() +
                                " on " + a.getAppointmentDate() + " at " + a.getTimeSlot() + " [" + a.getStatus() + "]");
                    }
                }

                // Invoices for this patient
                List<Bill> bills = billingService.getBillsByPatient(pid);
                System.out.println("\n* Billing History (" + bills.size() + "):");
                if (bills.isEmpty()) {
                    System.out.println("  No billing records.");
                } else {
                    for (Bill b : bills) {
                        System.out.printf("  - Invoice #%d: $%.2f [%s] on %s%n",
                                b.getId(), b.getTotalAmount(), b.getPaymentStatus(), b.getFormattedCreatedAt());
                    }
                }
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void listAllPatients() {
        List<Patient> list = patientService.getAllPatients();
        renderPatientsTable(list);
    }

    private void renderPatientsTable(List<Patient> list) {
        TablePrinter tp = new TablePrinter("ID", "Name", "Gender", "Phone", "Blood Group", "Emergency Contact", "Registered At");
        for (Patient p : list) {
            tp.addRow(
                    String.valueOf(p.getId()),
                    p.getName(),
                    p.getGender(),
                    p.getPhone(),
                    p.getBloodGroup() != null ? p.getBloodGroup() : "-",
                    p.getEmergencyContact() != null ? p.getEmergencyContact() : "-",
                    p.getFormattedRegistrationDate()
            );
        }
        tp.print();
    }

    // =========================================================================
    // 2. DOCTOR & DEPARTMENT MANAGEMENT
    // =========================================================================
    private void handleDoctorManagement() {
        System.out.println("=== DOCTOR & DEPARTMENT MANAGEMENT ===");
        System.out.println(" 1. View All Doctors");
        System.out.println(" 2. View Available (On-Duty) Doctors");
        System.out.println(" 3. View Doctors by Department");
        System.out.println(" 4. Register New Doctor");
        System.out.println(" 5. Toggle Doctor On-Duty / Leave Status");
        System.out.println(" 6. View All Hospital Departments");
        int sub = readInt("Enter choice (1-6): ");
        System.out.println();

        switch (sub) {
            case 1 -> renderDoctorsTable(doctorService.getAllDoctors());
            case 2 -> renderDoctorsTable(doctorService.getAvailableDoctors());
            case 3 -> {
                listDepartments();
                int deptId = readInt("Enter Department ID: ");
                renderDoctorsTable(doctorService.getDoctorsByDepartment(deptId));
            }
            case 4 -> {
                System.out.println("--- Register New Doctor ---");
                String name = readString("Doctor Name (e.g. Dr. Jane Smith): ");
                String gender = readString("Gender: ");
                String phone = readString("Phone Number: ");
                String email = readString("Email Address: ");
                String spec = readString("Specialization (e.g. Cardiologist): ");
                String qual = readString("Qualifications (e.g. MBBS, MD): ");
                double fee = readDouble("Consultation Fee ($): ");
                listDepartments();
                int deptId = readInt("Department ID: ");

                Doctor doc = new Doctor(name, gender, phone, email, spec, qual, fee, deptId);
                Doctor saved = doctorService.registerDoctor(doc);
                System.out.println("[SUCCESS] Doctor registered with Doctor ID: " + saved.getId());
            }
            case 5 -> {
                int docId = readInt("Enter Doctor ID to toggle status: ");
                boolean newStatus = doctorService.toggleDoctorAvailability(docId);
                System.out.println("[UPDATED] Doctor availability is now: " + (newStatus ? "AVAILABLE (On-Duty)" : "ON LEAVE"));
            }
            case 6 -> listDepartments();
            default -> System.out.println("Invalid selection.");
        }
    }

    private void renderDoctorsTable(List<Doctor> list) {
        TablePrinter tp = new TablePrinter("ID", "Doctor Name", "Specialization", "Qualification", "Department", "Fee ($)", "Status");
        for (Doctor d : list) {
            tp.addRow(
                    String.valueOf(d.getId()),
                    d.getName(),
                    d.getSpecialization(),
                    d.getQualification(),
                    d.getDepartmentName() != null ? d.getDepartmentName() : "Dept #" + d.getDepartmentId(),
                    String.format("%.2f", d.getConsultationFee()),
                    d.isAvailable() ? "AVAILABLE" : "ON LEAVE"
            );
        }
        tp.print();
    }

    private void listDepartments() {
        List<Department> list = doctorService.getAllDepartments();
        TablePrinter tp = new TablePrinter("Dept ID", "Department Name", "Floor");
        for (Department d : list) {
            tp.addRow(String.valueOf(d.getId()), d.getName(), "Floor " + d.getFloor());
        }
        tp.print();
    }

    // =========================================================================
    // 3. APPOINTMENT MANAGEMENT
    // =========================================================================
    private void handleAppointmentManagement() {
        System.out.println("=== APPOINTMENT SCHEDULING ===");
        System.out.println(" 1. View All Scheduled Appointments");
        System.out.println(" 2. Book New Appointment");
        System.out.println(" 3. Mark Appointment as Completed");
        System.out.println(" 4. Cancel Appointment");
        int sub = readInt("Enter choice (1-4): ");
        System.out.println();

        switch (sub) {
            case 1 -> renderAppointmentsTable(appointmentService.getAllAppointments());
            case 2 -> {
                System.out.println("--- Book New Appointment ---");
                listAllPatients();
                int patientId = readInt("Enter Patient ID: ");
                renderDoctorsTable(doctorService.getAvailableDoctors());
                int doctorId = readInt("Enter Doctor ID: ");
                String date = readString("Appointment Date (YYYY-MM-DD): ");
                String time = readString("Time Slot (e.g. 10:30 AM, 02:00 PM): ");
                String reason = readString("Reason for Visit: ");

                Appointment appt = appointmentService.bookAppointment(patientId, doctorId, date, time, reason);
                System.out.println("[SUCCESS] Appointment scheduled successfully! Appointment ID: " + appt.getId());
            }
            case 3 -> {
                int aid = readInt("Enter Appointment ID to mark Completed: ");
                appointmentService.completeAppointment(aid);
                System.out.println("[SUCCESS] Appointment marked as COMPLETED. You can now generate an invoice for it.");
            }
            case 4 -> {
                int aid = readInt("Enter Appointment ID to Cancel: ");
                appointmentService.cancelAppointment(aid);
                System.out.println("[SUCCESS] Appointment #" + aid + " has been CANCELLED.");
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void renderAppointmentsTable(List<Appointment> list) {
        TablePrinter tp = new TablePrinter("Appt ID", "Patient Name", "Doctor Name", "Specialization", "Date", "Slot", "Status", "Reason");
        for (Appointment a : list) {
            tp.addRow(
                    String.valueOf(a.getId()),
                    a.getPatientName() != null ? a.getPatientName() : "Patient #" + a.getPatientId(),
                    a.getDoctorName() != null ? a.getDoctorName() : "Doctor #" + a.getDoctorId(),
                    a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "-",
                    a.getAppointmentDate(),
                    a.getTimeSlot(),
                    a.getStatus().name(),
                    a.getReason() != null ? a.getReason() : "-"
            );
        }
        tp.print();
    }

    // =========================================================================
    // 4. INPATIENT ROOM & BED ALLOCATION
    // =========================================================================
    private void handleRoomManagement() {
        System.out.println("=== ROOM & INPATIENT BED ALLOCATION ===");
        System.out.println(" 1. View All Hospital Rooms & Occupancy");
        System.out.println(" 2. View Available Rooms Only");
        System.out.println(" 3. Admit Patient to a Room");
        System.out.println(" 4. View Active Inpatient Admissions");
        System.out.println(" 5. Discharge Patient from Room");
        int sub = readInt("Enter choice (1-5): ");
        System.out.println();

        switch (sub) {
            case 1 -> renderRoomsTable(roomService.getAllRooms());
            case 2 -> renderRoomsTable(roomService.getAvailableRooms());
            case 3 -> {
                System.out.println("--- Inpatient Admission ---");
                listAllPatients();
                int patientId = readInt("Enter Patient ID: ");
                renderRoomsTable(roomService.getAvailableRooms());
                int roomId = readInt("Enter Room ID to allocate: ");

                Admission adm = roomService.admitPatient(patientId, roomId);
                System.out.println("[SUCCESS] Patient successfully admitted! Admission ID: " + adm.getId());
            }
            case 4 -> renderAdmissionsTable(roomService.getActiveAdmissions());
            case 5 -> {
                renderAdmissionsTable(roomService.getActiveAdmissions());
                int admId = readInt("Enter Admission ID to discharge: ");
                Admission adm = roomService.dischargePatient(admId);
                System.out.println("[SUCCESS] Patient discharged from Room ID: " + adm.getRoomId() +
                        ". Total stay calculated: " + adm.calculateDaysStayed() + " day(s).");
                System.out.println("Notice: Proceed to Billing to generate the inpatient settlement invoice.");
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void renderRoomsTable(List<Room> list) {
        TablePrinter tp = new TablePrinter("Room ID", "Room Number", "Room Type", "Daily Rate ($)", "Status");
        for (Room r : list) {
            tp.addRow(
                    String.valueOf(r.getId()),
                    r.getRoomNumber(),
                    r.getRoomType().getDisplayName(),
                    String.format("%.2f", r.getDailyRate()),
                    r.getStatus()
            );
        }
        tp.print();
    }

    private void renderAdmissionsTable(List<Admission> list) {
        TablePrinter tp = new TablePrinter("Adm ID", "Patient Name", "Room", "Admitted At", "Discharged At", "Days Stayed", "Status");
        for (Admission a : list) {
            tp.addRow(
                    String.valueOf(a.getId()),
                    a.getPatientName() != null ? a.getPatientName() : "ID #" + a.getPatientId(),
                    a.getRoomNumber() != null ? a.getRoomNumber() : "Room #" + a.getRoomId(),
                    a.getFormattedAdmissionDate(),
                    a.getFormattedDischargeDate(),
                    String.valueOf(a.calculateDaysStayed()),
                    a.getStatus()
            );
        }
        tp.print();
    }

    // =========================================================================
    // 5. BILLING & FINANCIAL MANAGEMENT
    // =========================================================================
    private void handleBillingManagement() {
        System.out.println("=== BILLING, INVOICING & PAYMENTS ===");
        System.out.println(" 1. Generate Outpatient Appointment Invoice");
        System.out.println(" 2. Generate Inpatient Stay Discharge Invoice");
        System.out.println(" 3. View All Invoices");
        System.out.println(" 4. View Unpaid / Pending Invoices");
        System.out.println(" 5. Process Invoice Payment");
        System.out.println(" 6. View Specific Invoice Details");
        int sub = readInt("Enter choice (1-6): ");
        System.out.println();

        switch (sub) {
            case 1 -> {
                System.out.println("--- Generate Outpatient Consultation Bill ---");
                renderAppointmentsTable(appointmentService.getAllAppointments());
                int apptId = readInt("Enter Appointment ID: ");
                double meds = readDouble("Additional Medication / Diagnostics Charges ($): ");
                Bill bill = billingService.generateAppointmentBill(apptId, meds);
                System.out.println("[SUCCESS] Bill generated with Invoice ID: " + bill.getId());
                renderSingleBill(bill);
            }
            case 2 -> {
                System.out.println("--- Generate Inpatient Stay Bill ---");
                renderAdmissionsTable(roomService.getAllAdmissions());
                int admId = readInt("Enter Admission ID: ");
                double docFee = readDouble("Treating Doctor Consultation/Visit Fee ($): ");
                double meds = readDouble("Medications / Nursing / Lab Charges ($): ");
                Bill bill = billingService.generateInpatientStayBill(admId, docFee, meds);
                System.out.println("[SUCCESS] Inpatient settlement bill created! Invoice ID: " + bill.getId());
                renderSingleBill(bill);
            }
            case 3 -> renderBillsTable(billingService.getAllBills());
            case 4 -> renderBillsTable(billingService.getUnpaidBills());
            case 5 -> {
                renderBillsTable(billingService.getUnpaidBills());
                int billId = readInt("Enter Invoice ID to settle: ");
                System.out.println("Payment Methods: 1. CASH  2. CARD  3. UPI  4. INSURANCE");
                int mChoice = readInt("Select payment method (1-4): ");
                String method = switch (mChoice) {
                    case 2 -> "CARD";
                    case 3 -> "UPI";
                    case 4 -> "INSURANCE";
                    default -> "CASH";
                };
                Bill paid = billingService.processPayment(billId, method);
                System.out.println("[SUCCESS] Invoice #" + paid.getId() + " marked as PAID via " + method + ".");
            }
            case 6 -> {
                int billId = readInt("Enter Invoice ID: ");
                Bill b = billingService.getBillById(billId);
                renderSingleBill(b);
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void renderBillsTable(List<Bill> list) {
        TablePrinter tp = new TablePrinter("Inv ID", "Patient Name", "Consult Fee", "Room Fee", "Meds/Lab", "Tax (5%)", "Total ($)", "Status", "Date");
        for (Bill b : list) {
            tp.addRow(
                    String.valueOf(b.getId()),
                    b.getPatientName() != null ? b.getPatientName() : "ID #" + b.getPatientId(),
                    String.format("%.2f", b.getDoctorFee()),
                    String.format("%.2f", b.getRoomCharges()),
                    String.format("%.2f", b.getMedicationCharges()),
                    String.format("%.2f", b.getTaxAmount()),
                    String.format("%.2f", b.getTotalAmount()),
                    b.getPaymentStatus().name(),
                    b.getFormattedCreatedAt()
            );
        }
        tp.print();
    }

    private void renderSingleBill(Bill b) {
        System.out.println("\n========================================================");
        System.out.println("                  HOSPITAL INVOICE                      ");
        System.out.println("========================================================");
        System.out.printf(" Invoice Number:      #%d%n", b.getId());
        System.out.printf(" Patient:             %s (ID: %d)%n", b.getPatientName(), b.getPatientId());
        System.out.printf(" Date:                %s%n", b.getFormattedCreatedAt());
        System.out.println("--------------------------------------------------------");
        System.out.printf(" Doctor Consultation: $%.2f%n", b.getDoctorFee());
        System.out.printf(" Room / Bed Stay:     $%.2f%n", b.getRoomCharges());
        System.out.printf(" Medicines & Tests:   $%.2f%n", b.getMedicationCharges());
        System.out.printf(" Subtotal:            $%.2f%n", b.calculateSubtotal());
        System.out.printf(" Tax (5%% GST):        $%.2f%n", b.getTaxAmount());
        System.out.println("--------------------------------------------------------");
        System.out.printf(" TOTAL PAYABLE:       $%.2f%n", b.getTotalAmount());
        System.out.printf(" Payment Status:      %s%n", b.getPaymentStatus());
        if (b.getPaymentStatus() == PaymentStatus.PAID) {
            System.out.printf(" Method / Paid At:    %s on %s%n", b.getPaymentMethod(), b.getFormattedPaidAt());
        }
        System.out.println("========================================================\n");
    }

    // =========================================================================
    // 6. HOSPITAL OVERVIEW DASHBOARD
    // =========================================================================
    private void showHospitalDashboard() {
        List<Patient> patients = patientService.getAllPatients();
        List<Doctor> doctors = doctorService.getAllDoctors();
        List<Room> rooms = roomService.getAllRooms();
        List<Admission> activeAdmissions = roomService.getActiveAdmissions();
        List<Appointment> appointments = appointmentService.getAllAppointments();
        List<Bill> bills = billingService.getAllBills();

        long availableRooms = rooms.stream().filter(Room::isAvailable).count();
        double totalRevenue = bills.stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID)
                .mapToDouble(Bill::getTotalAmount).sum();
        double pendingRevenue = bills.stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.UNPAID)
                .mapToDouble(Bill::getTotalAmount).sum();

        System.out.println("==============================================================");
        System.out.println("                 HOSPITAL EXECUTIVE DASHBOARD                 ");
        System.out.println("==============================================================");
        System.out.printf("  Total Registered Patients:        %d%n", patients.size());
        System.out.printf("  Total Medical Specialists:        %d (%d on duty)%n",
                doctors.size(), doctors.stream().filter(Doctor::isAvailable).count());
        System.out.printf("  Total Bed Capacity:               %d rooms (%d available, %d occupied)%n",
                rooms.size(), availableRooms, activeAdmissions.size());
        System.out.printf("  Total Scheduled Consultations:    %d%n", appointments.size());
        System.out.println("--------------------------------------------------------------");
        System.out.printf("  Realized Revenue (Collected):     $%,.2f%n", totalRevenue);
        System.out.printf("  Outstanding Balance (Unpaid):     $%,.2f%n", pendingRevenue);
        System.out.println("==============================================================");
    }

    // Helper input methods
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Please try again.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void pause() {
        System.out.print("\nPress [Enter] to continue...");
        scanner.nextLine();
    }
}
