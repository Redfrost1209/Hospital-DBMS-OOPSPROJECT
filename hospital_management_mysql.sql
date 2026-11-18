-- =====================================================================
-- MEDCARE HOSPITAL MANAGEMENT SYSTEM (HMS)
-- MySQL Workbench Database Script
-- =====================================================================

-- 1. Create and Switch to Database
CREATE DATABASE IF NOT EXISTS hospital_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hospital_db;

-- 2. Drop existing tables if recreating (ordered to satisfy foreign key constraints)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS admissions;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS departments;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- TABLE: departments
-- =====================================================================
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    floor INT NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- TABLE: doctors
-- =====================================================================
CREATE TABLE doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    specialization VARCHAR(100) NOT NULL,
    qualification VARCHAR(100),
    consultation_fee DECIMAL(10, 2) NOT NULL,
    department_id INT NOT NULL,
    is_available TINYINT(1) DEFAULT 1,
    CONSTRAINT fk_doctor_department FOREIGN KEY (department_id) 
        REFERENCES departments(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- TABLE: patients
-- =====================================================================
CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    blood_group VARCHAR(10),
    emergency_contact VARCHAR(100),
    medical_history TEXT,
    registered_at DATETIME NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- TABLE: rooms
-- =====================================================================
CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type VARCHAR(50) NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

-- =====================================================================
-- TABLE: admissions (Inpatient Bed Tracking)
-- =====================================================================
CREATE TABLE admissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    room_id INT NOT NULL,
    admitted_at DATETIME NOT NULL,
    discharged_at DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_admission_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_admission_room FOREIGN KEY (room_id) 
        REFERENCES rooms(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- TABLE: appointments
-- =====================================================================
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    reason VARCHAR(255),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctors(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- TABLE: bills
-- =====================================================================
CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    appointment_id INT NULL,
    admission_id INT NULL,
    doctor_fee DECIMAL(10, 2) DEFAULT 0.00,
    room_charges DECIMAL(10, 2) DEFAULT 0.00,
    medication_charges DECIMAL(10, 2) DEFAULT 0.00,
    tax_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    payment_method VARCHAR(50) NULL,
    created_at DATETIME NOT NULL,
    paid_at DATETIME NULL,
    CONSTRAINT fk_bill_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_id) 
        REFERENCES appointments(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_bill_admission FOREIGN KEY (admission_id) 
        REFERENCES admissions(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- SEED DATA
-- =====================================================================

-- 1. Departments
INSERT INTO departments (id, name, floor) VALUES
(1, 'Cardiology', 2),
(2, 'Neurology', 3),
(3, 'Pediatrics', 1),
(4, 'Orthopedics', 2),
(5, 'General Medicine', 1);

-- 2. Doctors
INSERT INTO doctors (id, name, gender, phone, email, specialization, qualification, consultation_fee, department_id, is_available) VALUES
(1, 'Dr. Sarah Jenkins', 'Female', '9876543210', 'sarah.j@hospital.com', 'Cardiologist', 'MD, FACC', 120.00, 1, 1),
(2, 'Dr. Rajesh Sharma', 'Male', '9876543211', 'rajesh.s@hospital.com', 'Neurologist', 'MD, DM Neurology', 150.00, 2, 1),
(3, 'Dr. Emily Watson', 'Female', '9876543212', 'emily.w@hospital.com', 'Pediatrician', 'MD Pediatrics', 90.00, 3, 1),
(4, 'Dr. Michael Chen', 'Male', '9876543213', 'mchen@hospital.com', 'Orthopedic Surgeon', 'MS Ortho', 130.00, 4, 1),
(5, 'Dr. David Miller', 'Male', '9876543214', 'miller@hospital.com', 'General Physician', 'MBBS, MD', 75.00, 5, 1);

-- 3. Rooms
INSERT INTO rooms (id, room_number, room_type, daily_rate, status) VALUES
(1, '101-A', 'GENERAL', 500.00, 'AVAILABLE'),
(2, '101-B', 'GENERAL', 500.00, 'AVAILABLE'),
(3, '201', 'SEMI_PRIVATE', 1200.00, 'AVAILABLE'),
(4, '202', 'SEMI_PRIVATE', 1200.00, 'OCCUPIED'),
(5, '301', 'PRIVATE', 2500.00, 'AVAILABLE'),
(6, '302', 'PRIVATE', 2500.00, 'AVAILABLE'),
(7, 'ICU-1', 'ICU', 5000.00, 'AVAILABLE'),
(8, 'ICU-2', 'ICU', 5000.00, 'AVAILABLE');

-- 4. Patients
INSERT INTO patients (id, name, gender, phone, email, blood_group, emergency_contact, medical_history, registered_at) VALUES
(1, 'John Doe', 'Male', '9123456780', 'john.doe@gmail.com', 'O+', 'Jane Doe (9123456789)', 'Hypertension, Mild Asthma', '2026-10-20 10:30:00'),
(2, 'Alice Smith', 'Female', '9123456781', 'alice.s@gmail.com', 'A+', 'Bob Smith (9123456788)', 'None', '2026-10-22 14:15:00'),
(3, 'Robert Vance', 'Male', '9123456782', 'robert.v@gmail.com', 'B+', 'Phyllis Vance (9123456787)', 'Type 2 Diabetes', '2026-10-24 09:00:00');

-- 5. Active Admission
INSERT INTO admissions (id, patient_id, room_id, admitted_at, discharged_at, status) VALUES
(1, 3, 4, '2026-10-23 11:00:00', NULL, 'ACTIVE');

-- 6. Appointments
INSERT INTO appointments (id, patient_id, doctor_id, appointment_date, time_slot, status, reason) VALUES
(1, 1, 1, '2026-10-26', '10:00 AM', 'SCHEDULED', 'Routine cardiac evaluation'),
(2, 2, 3, '2026-10-26', '11:30 AM', 'SCHEDULED', 'Pediatric checkup for child');

-- 7. Sample Bill
INSERT INTO bills (id, patient_id, appointment_id, admission_id, doctor_fee, room_charges, medication_charges, tax_amount, total_amount, payment_status, payment_method, created_at, paid_at) VALUES
(1, 1, 1, NULL, 120.00, 0.00, 30.00, 7.50, 157.50, 'PAID', 'CARD', '2026-10-24 16:30:00', '2026-10-24 16:35:00');
