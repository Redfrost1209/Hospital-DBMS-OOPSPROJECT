package com.hospital.config;

import com.hospital.exception.DatabaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Singleton Database Manager.
 * Supports MySQL (primary for MySQL Workbench) with fallback to SQLite.
 * Reads configurations from db.properties if available.
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    private String dbType = "mysql"; // "mysql" or "sqlite"
    private String mysqlHost = "localhost";
    private int mysqlPort = 3306;
    private String mysqlDatabase = "hospital_db";
    private String mysqlUser = "root";
    private String mysqlPassword = "root";
    private String sqliteUrl = "jdbc:sqlite:hospital_management.db";

    private DatabaseManager() {
        loadProperties();
        loadDriver();
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                Properties props = new Properties();
                props.load(in);
                this.dbType = props.getProperty("db.type", "mysql").trim().toLowerCase();
                this.mysqlHost = props.getProperty("db.mysql.host", "localhost").trim();
                this.mysqlPort = Integer.parseInt(props.getProperty("db.mysql.port", "3306").trim());
                this.mysqlDatabase = props.getProperty("db.mysql.database", "hospital_db").trim();
                this.mysqlUser = props.getProperty("db.mysql.user", "root").trim();
                this.mysqlPassword = props.getProperty("db.mysql.password", "root").trim();
                this.sqliteUrl = props.getProperty("db.sqlite.url", "jdbc:sqlite:hospital_management.db").trim();
            } catch (Exception e) {
                System.out.println("[WARN] Could not read db.properties, using default settings: " + e.getMessage());
            }
        }
    }

    private void loadDriver() {
        try {
            if ("mysql".equals(dbType)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                Class.forName("org.sqlite.JDBC");
            }
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Failed to load JDBC driver for " + dbType + ": " + e.getMessage(), e);
        }
    }

    public Connection getConnection() throws SQLException {
        if ("mysql".equals(dbType)) {
            String url = String.format(
                    "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    mysqlHost, mysqlPort, mysqlDatabase
            );
            return DriverManager.getConnection(url, mysqlUser, mysqlPassword);
        } else {
            Connection conn = DriverManager.getConnection(sqliteUrl);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        }
    }

    public String getDbType() {
        return dbType;
    }

    public String getDatabaseName() {
        return "mysql".equals(dbType) ? mysqlDatabase : "hospital_management.db";
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if ("mysql".equals(dbType)) {
                initializeMySQLSchema(stmt);
            } else {
                initializeSQLiteSchema(stmt);
            }
        } catch (SQLException e) {
            if ("mysql".equals(dbType)) {
                System.err.println("\n==========================================================================");
                System.err.println("[MySQL CONNECTION ERROR]");
                System.err.println("Could not connect to MySQL Server at " + mysqlHost + ":" + mysqlPort + " with user '" + mysqlUser + "'.");
                System.err.println("Reason: " + e.getMessage());
                System.err.println("\nSteps to fix:");
                System.err.println("1. Verify MySQL Server service is running in Windows Services / Task Manager.");
                System.err.println("2. Check the MySQL root password in 'db.properties' (set db.mysql.password=<your_password>).");
                System.err.println("3. Or set 'db.type=sqlite' in 'db.properties' to run in embedded SQLite mode.");
                System.err.println("==========================================================================\n");
            }
            throw new DatabaseException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private void initializeMySQLSchema(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS departments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                floor INT NOT NULL
            ) ENGINE=InnoDB;
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS doctors (
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
                FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
            ) ENGINE=InnoDB;
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS patients (
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
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS rooms (
                id INT AUTO_INCREMENT PRIMARY KEY,
                room_number VARCHAR(20) NOT NULL UNIQUE,
                room_type VARCHAR(50) NOT NULL,
                daily_rate DECIMAL(10, 2) NOT NULL,
                status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'
            ) ENGINE=InnoDB;
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS admissions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                patient_id INT NOT NULL,
                room_id INT NOT NULL,
                admitted_at DATETIME NOT NULL,
                discharged_at DATETIME NULL,
                status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT,
                FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
            ) ENGINE=InnoDB;
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS appointments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                patient_id INT NOT NULL,
                doctor_id INT NOT NULL,
                appointment_date DATE NOT NULL,
                time_slot VARCHAR(30) NOT NULL,
                status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
                reason VARCHAR(255),
                FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
                FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT
            ) ENGINE=InnoDB;
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS bills (
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
                FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT,
                FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
                FOREIGN KEY (admission_id) REFERENCES admissions(id) ON DELETE SET NULL
            ) ENGINE=InnoDB;
        """);

        seedInitialMySQLData(stmt);
    }

    private void seedInitialMySQLData(Statement stmt) throws SQLException {
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM departments");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("""
                INSERT INTO departments (id, name, floor) VALUES
                (1, 'Cardiology', 2),
                (2, 'Neurology', 3),
                (3, 'Pediatrics', 1),
                (4, 'Orthopedics', 2),
                (5, 'General Medicine', 1);
            """);

            stmt.execute("""
                INSERT INTO doctors (id, name, gender, phone, email, specialization, qualification, consultation_fee, department_id, is_available) VALUES
                (1, 'Dr. Sarah Jenkins', 'Female', '9876543210', 'sarah.j@hospital.com', 'Cardiologist', 'MD, FACC', 120.00, 1, 1),
                (2, 'Dr. Rajesh Sharma', 'Male', '9876543211', 'rajesh.s@hospital.com', 'Neurologist', 'MD, DM Neurology', 150.00, 2, 1),
                (3, 'Dr. Emily Watson', 'Female', '9876543212', 'emily.w@hospital.com', 'Pediatrician', 'MD Pediatrics', 90.00, 3, 1),
                (4, 'Dr. Michael Chen', 'Male', '9876543213', 'mchen@hospital.com', 'Orthopedic Surgeon', 'MS Ortho', 130.00, 4, 1),
                (5, 'Dr. David Miller', 'Male', '9876543214', 'miller@hospital.com', 'General Physician', 'MBBS, MD', 75.00, 5, 1);
            """);

            stmt.execute("""
                INSERT INTO rooms (id, room_number, room_type, daily_rate, status) VALUES
                (1, '101-A', 'GENERAL', 500.00, 'AVAILABLE'),
                (2, '101-B', 'GENERAL', 500.00, 'AVAILABLE'),
                (3, '201', 'SEMI_PRIVATE', 1200.00, 'AVAILABLE'),
                (4, '202', 'SEMI_PRIVATE', 1200.00, 'OCCUPIED'),
                (5, '301', 'PRIVATE', 2500.00, 'AVAILABLE'),
                (6, '302', 'PRIVATE', 2500.00, 'AVAILABLE'),
                (7, 'ICU-1', 'ICU', 5000.00, 'AVAILABLE'),
                (8, 'ICU-2', 'ICU', 5000.00, 'AVAILABLE');
            """);

            stmt.execute("""
                INSERT INTO patients (id, name, gender, phone, email, blood_group, emergency_contact, medical_history, registered_at) VALUES
                (1, 'John Doe', 'Male', '9123456780', 'john.doe@gmail.com', 'O+', 'Jane Doe (9123456789)', 'Hypertension, Mild Asthma', '2026-10-20 10:30:00'),
                (2, 'Alice Smith', 'Female', '9123456781', 'alice.s@gmail.com', 'A+', 'Bob Smith (9123456788)', 'None', '2026-10-22 14:15:00'),
                (3, 'Robert Vance', 'Male', '9123456782', 'robert.v@gmail.com', 'B+', 'Phyllis Vance (9123456787)', 'Type 2 Diabetes', '2026-10-24 09:00:00');
            """);

            stmt.execute("""
                INSERT INTO admissions (id, patient_id, room_id, admitted_at, discharged_at, status) VALUES
                (1, 3, 4, '2026-10-23 11:00:00', NULL, 'ACTIVE');
            """);

            stmt.execute("""
                INSERT INTO appointments (id, patient_id, doctor_id, appointment_date, time_slot, status, reason) VALUES
                (1, 1, 1, '2026-10-26', '10:00 AM', 'SCHEDULED', 'Routine cardiac evaluation'),
                (2, 2, 3, '2026-10-26', '11:30 AM', 'SCHEDULED', 'Pediatric checkup for child');
            """);

            stmt.execute("""
                INSERT INTO bills (id, patient_id, appointment_id, admission_id, doctor_fee, room_charges, medication_charges, tax_amount, total_amount, payment_status, payment_method, created_at, paid_at) VALUES
                (1, 1, 1, NULL, 120.00, 0.00, 30.00, 7.50, 157.50, 'PAID', 'CARD', '2026-10-24 16:30:00', '2026-10-24 16:35:00');
            """);
        }
    }

    private void initializeSQLiteSchema(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS departments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                floor INTEGER NOT NULL
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS doctors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                gender TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT,
                specialization TEXT NOT NULL,
                qualification TEXT,
                consultation_fee REAL NOT NULL,
                department_id INTEGER NOT NULL,
                is_available INTEGER DEFAULT 1,
                FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS patients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                gender TEXT NOT NULL,
                phone TEXT NOT NULL UNIQUE,
                email TEXT,
                blood_group TEXT,
                emergency_contact TEXT,
                medical_history TEXT,
                registered_at TEXT NOT NULL
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS rooms (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                room_number TEXT NOT NULL UNIQUE,
                room_type TEXT NOT NULL,
                daily_rate REAL NOT NULL,
                status TEXT NOT NULL DEFAULT 'AVAILABLE'
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS admissions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                room_id INTEGER NOT NULL,
                admitted_at TEXT NOT NULL,
                discharged_at TEXT,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT,
                FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS appointments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                doctor_id INTEGER NOT NULL,
                appointment_date TEXT NOT NULL,
                time_slot TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'SCHEDULED',
                reason TEXT,
                FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
                FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE RESTRICT
            );
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS bills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                appointment_id INTEGER,
                admission_id INTEGER,
                doctor_fee REAL DEFAULT 0.0,
                room_charges REAL DEFAULT 0.0,
                medication_charges REAL DEFAULT 0.0,
                tax_amount REAL DEFAULT 0.0,
                total_amount REAL NOT NULL,
                payment_status TEXT NOT NULL DEFAULT 'UNPAID',
                payment_method TEXT,
                created_at TEXT NOT NULL,
                paid_at TEXT,
                FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT,
                FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
                FOREIGN KEY (admission_id) REFERENCES admissions(id) ON DELETE SET NULL
            );
        """);

        seedInitialSQLiteData(stmt);
    }

    private void seedInitialSQLiteData(Statement stmt) throws SQLException {
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM departments");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute("""
                INSERT INTO departments (id, name, floor) VALUES
                (1, 'Cardiology', 2),
                (2, 'Neurology', 3),
                (3, 'Pediatrics', 1),
                (4, 'Orthopedics', 2),
                (5, 'General Medicine', 1);
            """);

            stmt.execute("""
                INSERT INTO doctors (id, name, gender, phone, email, specialization, qualification, consultation_fee, department_id, is_available) VALUES
                (1, 'Dr. Sarah Jenkins', 'Female', '9876543210', 'sarah.j@hospital.com', 'Cardiologist', 'MD, FACC', 120.0, 1, 1),
                (2, 'Dr. Rajesh Sharma', 'Male', '9876543211', 'rajesh.s@hospital.com', 'Neurologist', 'MD, DM Neurology', 150.0, 2, 1),
                (3, 'Dr. Emily Watson', 'Female', '9876543212', 'emily.w@hospital.com', 'Pediatrician', 'MD Pediatrics', 90.0, 3, 1),
                (4, 'Dr. Michael Chen', 'Male', '9876543213', 'mchen@hospital.com', 'Orthopedic Surgeon', 'MS Ortho', 130.0, 4, 1),
                (5, 'Dr. David Miller', 'Male', '9876543214', 'miller@hospital.com', 'General Physician', 'MBBS, MD', 75.0, 5, 1);
            """);

            stmt.execute("""
                INSERT INTO rooms (id, room_number, room_type, daily_rate, status) VALUES
                (1, '101-A', 'GENERAL', 500.0, 'AVAILABLE'),
                (2, '101-B', 'GENERAL', 500.0, 'AVAILABLE'),
                (3, '201', 'SEMI_PRIVATE', 1200.0, 'AVAILABLE'),
                (4, '202', 'SEMI_PRIVATE', 1200.0, 'OCCUPIED'),
                (5, '301', 'PRIVATE', 2500.0, 'AVAILABLE'),
                (6, '302', 'PRIVATE', 2500.0, 'AVAILABLE'),
                (7, 'ICU-1', 'ICU', 5000.0, 'AVAILABLE'),
                (8, 'ICU-2', 'ICU', 5000.0, 'AVAILABLE');
            """);

            stmt.execute("""
                INSERT INTO patients (id, name, gender, phone, email, blood_group, emergency_contact, medical_history, registered_at) VALUES
                (1, 'John Doe', 'Male', '9123456780', 'john.doe@gmail.com', 'O+', 'Jane Doe (9123456789)', 'Hypertension, Mild Asthma', '2026-10-20 10:30'),
                (2, 'Alice Smith', 'Female', '9123456781', 'alice.s@gmail.com', 'A+', 'Bob Smith (9123456788)', 'None', '2026-10-22 14:15'),
                (3, 'Robert Vance', 'Male', '9123456782', 'robert.v@gmail.com', 'B+', 'Phyllis Vance (9123456787)', 'Type 2 Diabetes', '2026-10-24 09:00');
            """);

            stmt.execute("""
                INSERT INTO admissions (id, patient_id, room_id, admitted_at, discharged_at, status) VALUES
                (1, 3, 4, '2026-10-23 11:00', NULL, 'ACTIVE');
            """);

            stmt.execute("""
                INSERT INTO appointments (id, patient_id, doctor_id, appointment_date, time_slot, status, reason) VALUES
                (1, 1, 1, '2026-10-26', '10:00 AM', 'SCHEDULED', 'Routine cardiac evaluation'),
                (2, 2, 3, '2026-10-26', '11:30 AM', 'SCHEDULED', 'Pediatric checkup for child');
            """);

            stmt.execute("""
                INSERT INTO bills (id, patient_id, appointment_id, admission_id, doctor_fee, room_charges, medication_charges, tax_amount, total_amount, payment_status, payment_method, created_at, paid_at) VALUES
                (1, 1, 1, NULL, 120.0, 0.0, 30.0, 7.5, 157.5, 'PAID', 'CARD', '2026-10-24 16:30', '2026-10-24 16:35');
            """);
        }
    }
}
