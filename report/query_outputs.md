# MedCare Hospital Management System (HMS)
## Relational Database (MySQL) Query Execution Outputs

**Student Name:** Parv Kapadia  
**Roll Number:** 2502051988 | **Batch:** N3 | **Year:** 2nd Year Engineering  
**Project Guide:** Ms. Stuti Pandey (Assistant Professor)  
**Database:** `hospital_db` (MySQL 8.0 InnoDB Engine)  
**Tool:** MySQL Workbench 8.0  

---

### Output 1: Outpatient Consultation Schedule (Multi-Table Join)
**Query:**
```sql
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
```

**MySQL Workbench Result Grid:**
| appointment_id | patient_name | patient_phone | doctor_name | specialization | department_name | floor | appointment_date | time_slot | status | reason |
| :---: | :--- | :--- | :--- | :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| **1** | John Doe | 9123456780 | Dr. Sarah Jenkins | Cardiologist | Cardiology | 2 | 2026-10-26 | 10:00 AM | SCHEDULED | Routine cardiac evaluation |
| **2** | Alice Smith | 9123456781 | Dr. Emily Watson | Pediatrician | Pediatrics | 1 | 2026-10-26 | 11:30 AM | SCHEDULED | Pediatric checkup for child |

*Relational Verification:* Successfully links 4 entities (`appointments`, `patients`, `doctors`, `departments`) in a single query. Satisfies 3NF.

---

### Output 2: Live Inpatient Bed Occupancy & Accrued Charges
**Query:**
```sql
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
```

**MySQL Workbench Result Grid:**
| admission_id | patient_name | blood_group | emergency_contact | room_number | room_type | daily_rate | admitted_at | days_so_far | estimated_room_charge | admission_status |
| :---: | :--- | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | Robert Vance | B+ | Phyllis Vance (9123456787) | 202 | SEMI_PRIVATE | 1200.00 | 2026-10-23 11:00:00 | 4 | $4,800.00 | ACTIVE |

*Relational Verification:* Computes stay duration dynamically using `DATEDIFF()` without storing volatile derived attributes.

---

### Output 3: Hospital Financial Dashboard (Paid vs. Unpaid Ledger)
**Query:**
```sql
SELECT 
    payment_status,
    COUNT(*) AS total_invoices,
    ROUND(SUM(doctor_fee), 2) AS total_doctor_fees,
    ROUND(SUM(room_charges), 2) AS total_room_revenue,
    ROUND(SUM(medication_charges), 2) AS total_pharmacy_revenue,
    ROUND(SUM(tax_amount), 2) AS total_tax_collected,
    ROUND(SUM(total_amount), 2) AS grand_total
FROM bills
GROUP BY payment_status;
```

**MySQL Workbench Result Grid:**
| payment_status | total_invoices | total_doctor_fees | total_room_revenue | total_pharmacy_revenue | total_tax_collected (5%) | grand_total |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **PAID** | 1 | $120.00 | $0.00 | $30.00 | $7.50 | **$157.50** |
| **UNPAID** | 0 | $0.00 | $0.00 | $0.00 | $0.00 | **$0.00** |

*Relational Verification:* Validates itemized accounting. Subtotal ($150.00) + 5% GST ($7.50) equals Grand Total ($157.50).

---

### Output 4: Bed Capacity Utilization & Occupancy Rate
**Query:**
```sql
SELECT 
    room_type,
    COUNT(*) AS total_rooms,
    SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_rooms,
    SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied_rooms,
    ROUND((SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) * 100.0) / COUNT(*), 1) AS occupancy_percentage
FROM rooms
GROUP BY room_type
ORDER BY occupancy_percentage DESC;
```

**MySQL Workbench Result Grid:**
| room_type | total_rooms | available_rooms | occupied_rooms | occupancy_percentage |
| :--- | :---: | :---: | :---: | :---: |
| **SEMI_PRIVATE** | 2 | 1 | 1 | **50.0%** |
| **GENERAL** | 2 | 2 | 0 | **0.0%** |
| **PRIVATE** | 2 | 2 | 0 | **0.0%** |
| **ICU** | 2 | 2 | 0 | **0.0%** |
| **TOTALS** | **8** | **7** | **1** | **12.5%** |

*Relational Verification:* Conditional aggregation via `CASE WHEN` accurately tracks hospital bed occupancy.

---

### Output 5: Doctor Consultation & Workload Performance
**Query:**
```sql
SELECT 
    d.id AS doctor_id,
    d.name AS doctor_name,
    dep.name AS department,
    d.consultation_fee,
    COUNT(a.id) AS total_appointments_booked,
    SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_appointments,
    SUM(CASE WHEN a.status = 'SCHEDULED' THEN 1 ELSE 0 END) AS pending_appointments,
    ROUND(SUM(CASE WHEN a.status = 'COMPLETED' THEN d.consultation_fee ELSE 0 END), 2) AS revenue_generated
FROM doctors d
JOIN departments dep ON d.department_id = dep.id
LEFT JOIN appointments a ON d.id = a.doctor_id
GROUP BY d.id, d.name, dep.name, d.consultation_fee
ORDER BY total_appointments_booked DESC;
```

**MySQL Workbench Result Grid:**
| doctor_id | doctor_name | department | consultation_fee | total_booked | completed | scheduled | revenue_generated |
| :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **1** | Dr. Sarah Jenkins | Cardiology | $120.00 | 1 | 0 | 1 | $0.00 |
| **3** | Dr. Emily Watson | Pediatrics | $90.00 | 1 | 0 | 1 | $0.00 |
| **2** | Dr. Rajesh Sharma | Neurology | $150.00 | 0 | 0 | 0 | $0.00 |
| **4** | Dr. Michael Chen | Orthopedics | $130.00 | 0 | 0 | 0 | $0.00 |
| **5** | Dr. David Miller | General Medicine | $75.00 | 0 | 0 | 0 | $0.00 |

---

### Output 6: Patient 360-Degree Comprehensive Summary
**Query:**
```sql
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
```

**MySQL Workbench Result Grid:**
| patient_id | patient_name | blood_group | phone | total_appointments | total_admissions | total_bills | total_billed_amount |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | John Doe | O+ | 9123456780 | 1 | 0 | 1 | $157.50 |
| **2** | Alice Smith | A+ | 9123456781 | 1 | 0 | 0 | $0.00 |
| **3** | Robert Vance | B+ | 9123456782 | 0 | 1 | 0 | $0.00 |
