package com.hospital.service;

import com.hospital.dao.AdmissionDAO;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.RoomDAO;
import com.hospital.dao.impl.*;
import com.hospital.exception.EntityNotFoundException;
import com.hospital.exception.ValidationException;
import com.hospital.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service managing Invoicing, Bill Generation, and Payment Settlement.
 * Demonstrates business computations, polymorphism, and financial consistency.
 */
public class BillingService {
    private final BillDAO billDAO;
    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;
    private final DoctorDAO doctorDAO;
    private final AdmissionDAO admissionDAO;
    private final RoomDAO roomDAO;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BillingService() {
        this.billDAO = new BillDAOImpl();
        this.patientDAO = new PatientDAOImpl();
        this.appointmentDAO = new AppointmentDAOImpl();
        this.doctorDAO = new DoctorDAOImpl();
        this.admissionDAO = new AdmissionDAOImpl();
        this.roomDAO = new RoomDAOImpl();
    }

    public BillingService(BillDAO billDAO, PatientDAO patientDAO, AppointmentDAO appointmentDAO,
                          DoctorDAO doctorDAO, AdmissionDAO admissionDAO, RoomDAO roomDAO) {
        this.billDAO = billDAO;
        this.patientDAO = patientDAO;
        this.appointmentDAO = appointmentDAO;
        this.doctorDAO = doctorDAO;
        this.admissionDAO = admissionDAO;
        this.roomDAO = roomDAO;
    }

    /**
     * Generates a bill for an outpatient doctor appointment.
     */
    public Bill generateAppointmentBill(int appointmentId, double medicationCharges) {
        Appointment appt = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment", appointmentId));

        Doctor doctor = doctorDAO.findById(appt.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor", appt.getDoctorId()));

        Bill bill = new Bill();
        bill.setPatientId(appt.getPatientId());
        bill.setAppointmentId(appt.getId());
        bill.setDoctorFee(doctor.getConsultationFee());
        bill.setRoomCharges(0.0);
        bill.setMedicationCharges(Math.max(0, medicationCharges));
        bill.calculateTotalWithTax();
        bill.setPaymentStatus(PaymentStatus.UNPAID);
        bill.setCreatedAt(LocalDateTime.now());

        // Mark appointment as COMPLETED
        appointmentDAO.updateStatus(appointmentId, AppointmentStatus.COMPLETED);

        return billDAO.save(bill);
    }

    /**
     * Generates a bill for inpatient room stay and discharges patient.
     */
    public Bill generateInpatientStayBill(int admissionId, double doctorFee, double medicationCharges) {
        Admission admission = admissionDAO.findById(admissionId)
                .orElseThrow(() -> new EntityNotFoundException("Admission", admissionId));

        Room room = roomDAO.findById(admission.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room", admission.getRoomId()));

        long days = admission.calculateDaysStayed();
        double roomCharges = days * room.getDailyRate();

        Bill bill = new Bill();
        bill.setPatientId(admission.getPatientId());
        bill.setAdmissionId(admission.getId());
        bill.setDoctorFee(Math.max(0, doctorFee));
        bill.setRoomCharges(roomCharges);
        bill.setMedicationCharges(Math.max(0, medicationCharges));
        bill.calculateTotalWithTax();
        bill.setPaymentStatus(PaymentStatus.UNPAID);
        bill.setCreatedAt(LocalDateTime.now());

        return billDAO.save(bill);
    }

    /**
     * Records payment settlement for an invoice.
     */
    public Bill processPayment(int billId, String paymentMethod) {
        Bill bill = billDAO.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException("Bill", billId));

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Invoice #" + billId + " is already paid.");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
        }

        String paidAtTimestamp = LocalDateTime.now().format(FORMATTER);
        billDAO.markAsPaid(billId, paymentMethod.trim().toUpperCase(), paidAtTimestamp);

        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaymentMethod(paymentMethod.trim().toUpperCase());
        bill.setPaidAt(LocalDateTime.now());
        return bill;
    }

    public List<Bill> getAllBills() {
        return billDAO.findAll();
    }

    public List<Bill> getUnpaidBills() {
        return billDAO.findByStatus(PaymentStatus.UNPAID);
    }

    public List<Bill> getPaidBills() {
        return billDAO.findByStatus(PaymentStatus.PAID);
    }

    public List<Bill> getBillsByPatient(int patientId) {
        return billDAO.findByPatientId(patientId);
    }

    public Bill getBillById(int id) {
        return billDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bill", id));
    }
}
