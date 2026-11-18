-- =====================================================================
-- MEDCARE HOSPITAL MANAGEMENT SYSTEM (HMS)
-- Advanced Demonstration Queries for MySQL Workbench
-- =====================================================================

USE hospital_db;

SELECT 
    a.id AS appointment_id,
    p.name AS patient_name,
    p.phone AS patient_phone,
    d.name AS doctor_name,
    d.specialization,
    dep.name AS department_name,
    dep.floor AS department_floor,
    a.appointment_date,
    a.time_slot,
    a.status,
    a.reason
FROM appointments a
JOIN patients p ON a.patient_id = p.id
JOIN doctors d ON a.doctor_id = d.id
JOIN departments dep ON d.department_id = dep.id
ORDER BY a.appointment_date ASC, a.time_slot ASC;


-- 1.2 Live Inpatient Bed Occupancy Details
-- Displays currently admitted patients, room numbers, room types, and days admitted so far
SELECT 
    adm.id AS admission_id,
    p.name AS patient_name,
    p.blood_group,
    p.emergency_contact,
    r.room_number,
    r.room_type,
    r.daily_rate,
    adm.admitted_at,
    DATEDIFF(CURRENT_TIMESTAMP, adm.admitted_at) + 1 AS days_admitted_so_far,
    ROUND((DATEDIFF(CURRENT_TIMESTAMP, adm.admitted_at) + 1) * r.daily_rate, 2) AS estimated_room_charge,
    adm.status AS admission_status
FROM admissions adm
JOIN patients p ON adm.patient_id = p.id
JOIN rooms r ON adm.room_id = r.id
WHERE adm.status = 'ACTIVE';


-- 1.3 Detailed Patient Billing Ledger & Breakdown
-- Connects invoices to patients and displays itemized cost breakdown (Doctor, Room, Meds, Tax, Total)
SELECT 
    b.id AS invoice_id,
    p.name AS patient_name,
    p.phone AS contact_number,
    b.doctor_fee,
    b.room_charges,
    b.medication_charges,
    (b.doctor_fee + b.room_charges + b.medication_charges) AS subtotal,
    b.tax_amount AS tax_5_percent,
    b.total_amount,
    b.payment_status,
    IFNULL(b.payment_method, 'PENDING') AS payment_method,
    b.created_at AS billed_on,
    IFNULL(b.paid_at, 'NOT PAID') AS settled_on
FROM bills b
JOIN patients p ON b.patient_id = p.id
ORDER BY b.id DESC;


-- =====================================================================
-- 2. AGGREGATIONS & EXECUTIVE BUSINESS METRICS
-- =====================================================================

-- 2.1 Hospital Financial Dashboard (Realized vs. Pending Revenue)
SELECT 
    payment_status,
    COUNT(*) AS total_invoices,
    ROUND(SUM(doctor_fee), 2) AS total_doctor_fees,
    ROUND(SUM(room_charges), 2) AS total_room_revenue,
    ROUND(SUM(medication_charges), 2) AS total_pharmacy_revenue,
    ROUND(SUM(tax_amount), 2) AS total_tax_collected,
    ROUND(SUM(total_amount), 2) AS total_amount
FROM bills
GROUP BY payment_status;


-- 2.2 Revenue Collection by Payment Channel
SELECT 
    IFNULL(payment_method, 'UNPAID') AS payment_channel,
    COUNT(*) AS transaction_count,
    ROUND(SUM(total_amount), 2) AS revenue_collected
FROM bills
GROUP BY payment_method;


-- 2.3 Doctor Consultation & Workload Performance
-- Aggregates total appointments booked per doctor and estimated consultation fees generated
SELECT 
    d.id AS doctor_id,
    d.name AS doctor_name,
    dep.name AS department,
    d.consultation_fee,
    COUNT(a.id) AS total_appointments_booked,
    SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_appointments,
    SUM(CASE WHEN a.status = 'SCHEDULED' THEN 1 ELSE 0 END) AS pending_appointments,
    SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_appointments,
    ROUND(SUM(CASE WHEN a.status = 'COMPLETED' THEN d.consultation_fee ELSE 0 END), 2) AS revenue_generated
FROM doctors d
JOIN departments dep ON d.department_id = dep.id
LEFT JOIN appointments a ON d.id = a.doctor_id
GROUP BY d.id, d.name, dep.name, d.consultation_fee
ORDER BY total_appointments_booked DESC;


-- 2.4 Room Utilization & Bed Occupancy Report
-- Shows total rooms, occupied rooms, and current occupancy rate per room type
SELECT 
    room_type,
    COUNT(*) AS total_rooms,
    SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_rooms,
    SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied_rooms,
    ROUND((SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) * 100.0) / COUNT(*), 1) AS occupancy_percentage
FROM rooms
GROUP BY room_type
ORDER BY occupancy_percentage DESC;


-- =====================================================================
-- 3. FILTERING & DIAGNOSTIC QUERIES
-- =====================================================================

-- 3.1 Unpaid Invoices Requiring Follow-Up (Debtors List)
SELECT 
    b.id AS invoice_id,
    p.name AS patient_name,
    p.phone AS phone_number,
    p.emergency_contact,
    b.total_amount AS amount_due,
    b.created_at AS invoice_date
FROM bills b
JOIN patients p ON b.patient_id = p.id
WHERE b.payment_status = 'UNPAID'
ORDER BY b.created_at ASC;


-- 3.2 Available Doctors On-Duty Right Now
SELECT 
    d.id AS doctor_id,
    d.name AS doctor_name,
    d.specialization,
    dep.name AS department,
    dep.floor,
    d.phone AS contact_number,
    d.consultation_fee
FROM doctors d
JOIN departments dep ON d.department_id = dep.id
WHERE d.is_available = 1
ORDER BY dep.name, d.name;


-- 3.3 Patient 360-Degree Comprehensive Summary View
-- Shows patient details with counts of appointments, admissions, and total bills
SELECT 
    p.id AS patient_id,
    p.name AS patient_name,
    p.blood_group,
    p.phone,
    COUNT(DISTINCT a.id) AS total_appointments,
    COUNT(DISTINCT adm.id) AS total_admissions,
    COUNT(DISTINCT b.id) AS total_bills,
    IFNULL(ROUND(SUM(b.total_amount), 2), 0.00) AS total_billed_amount
FROM patients p
LEFT JOIN appointments a ON p.id = a.patient_id
LEFT JOIN admissions adm ON p.id = adm.patient_id
LEFT JOIN bills b ON p.id = b.patient_id
GROUP BY p.id, p.name, p.blood_group, p.phone
ORDER BY p.id ASC;
