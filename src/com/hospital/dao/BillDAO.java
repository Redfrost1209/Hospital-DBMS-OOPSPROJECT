package com.hospital.dao;

import com.hospital.model.Bill;
import com.hospital.model.PaymentStatus;
import java.util.List;

public interface BillDAO extends GenericDAO<Bill, Integer> {
    List<Bill> findByPatientId(int patientId);
    List<Bill> findByStatus(PaymentStatus status);
    boolean markAsPaid(int billId, String paymentMethod, String paidAtTimestamp);
}
