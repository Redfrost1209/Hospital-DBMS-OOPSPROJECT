# MedCare Hospital Management System (HMS)

An Object-Oriented Java application with a Relational Database Management System (RDBMS) configured for **MySQL Workbench** (and embedded SQLite fallback), demonstrating production-grade OOP pillars, design patterns, and relational modeling.

---

## 🐬 Using with MySQL Workbench

This project comes with dedicated MySQL scripts, JDBC drivers (`mysql-connector-j.jar`), and full configuration for **MySQL Workbench**.

### Step 1: Open and Run the Script in MySQL Workbench
1. Open **MySQL Workbench**.
2. Connect to your MySQL Server instance (e.g. `Local instance MySQL80` on port `3306`).
3. Click **File** $\rightarrow$ **Open SQL Script...** (or press `Ctrl + Shift + O`).
4. Select the file:
   ```
   C:\Users\kapad\.gemini\antigravity\scratch\HospitalManagementSystem\hospital_management_mysql.sql
   ```
5. Click the **Execute (Lightning Bolt ⚡)** button to run the entire script.
   * This creates the `hospital_db` database.
   * Creates 7 relational tables with `InnoDB`, primary keys, foreign keys, and cascades.
   * Inserts starter seed data (departments, doctors, rooms, patients, appointments, and bills).

### Step 2: View and Manage Tables in MySQL Workbench
1. In the left sidebar under the **Navigator**, switch to the **Schemas** tab.
2. Click the **Refresh 🔄** button.
3. Expand **`hospital_db`** $\rightarrow$ **Tables**:
   - `departments`
   - `doctors`
   - `patients`
   - `rooms`
   - `admissions`
   - `appointments`
   - `bills`
4. Hover over any table and click the **Table grid icon 🗄️** (or right-click $\rightarrow$ **Select Rows - Limit 1000**) to inspect and edit records directly.

### Step 3: Generate the Visual ER Diagram (EER Diagram)
MySQL Workbench allows you to generate a visual Entity-Relationship diagram in 10 seconds:
1. In MySQL Workbench top menu, click **Database** $\rightarrow$ **Reverse Engineer...** (or press `Ctrl + R`).
2. Click **Next** $\rightarrow$ enter your password if prompted $\rightarrow$ click **Next**.
3. Check **`hospital_db`** and click **Next** $\rightarrow$ **Execute** $\rightarrow$ **Finish**.
4. You will see an interactive **EER Diagram** displaying all 7 tables and their relational foreign-key connections.

### Step 4: Configure Database Password in Java
Open `db.properties` in the project root:
```properties
db.type=mysql
db.mysql.host=localhost
db.mysql.port=3306
db.mysql.database=hospital_db
db.mysql.user=root
db.mysql.password=YOUR_MYSQL_PASSWORD_HERE
```
*(Replace `YOUR_MYSQL_PASSWORD_HERE` with the root password you chose during MySQL installation).*

---

## 🏛️ Architecture & Project Structure

The project follows a **Layered Architecture** adhering to the **Separation of Concerns** principle:

```
HospitalManagementSystem/
├── hospital_management_mysql.sql # MySQL Workbench DDL & Seed script
├── db.properties                 # Active database configuration (MySQL/SQLite)
├── build.bat / build.ps1         # One-click compilation scripts
├── run.bat / run.ps1             # One-click execution scripts
├── pom.xml                       # Maven configuration for IDEs
├── lib/                          # Standalone JDBC dependencies
│   ├── mysql-connector-j.jar     # MySQL 8.3 JDBC Driver
│   ├── sqlite-jdbc.jar           # SQLite JDBC Driver (Fallback)
│   ├── slf4j-api.jar
│   └── slf4j-simple.jar
├── src/
│   └── com/hospital/
│       ├── Main.java             # Entry point
│       ├── config/
│       │   └── DatabaseManager.java  # Singleton DB connection & multi-dialect migration
│       ├── model/                # OOP Domain Models
│       │   ├── Billable.java         # Interface (Polymorphism)
│       │   ├── Person.java           # Abstract Base Class (Abstraction, Encapsulation)
│       │   ├── Doctor.java           # Subclass (Inheritance)
│       │   ├── Patient.java          # Subclass (Inheritance)
│       │   ├── Department.java
│       │   ├── Room.java
│       │   ├── RoomType.java         # Enum with custom fields and methods
│       │   ├── Admission.java        # Inpatient Bed/Stay tracking
│       │   ├── Appointment.java
│       │   ├── AppointmentStatus.java
│       │   ├── PaymentStatus.java
│       │   └── Bill.java             # Implements Billable
│       ├── dao/                  # Data Access Object Pattern
│       │   ├── GenericDAO.java       # Generic CRUD Interface
│       │   ├── DoctorDAO.java
│       │   ├── PatientDAO.java
│       │   ├── DepartmentDAO.java
│       │   ├── RoomDAO.java
│       │   ├── AdmissionDAO.java
│       │   ├── AppointmentDAO.java
│       │   ├── BillDAO.java
│       │   └── impl/                 # JDBC implementations with PreparedStatements
│       ├── service/              # Business Logic & Validation Layer
│       ├── exception/            # Custom Exception Hierarchy
│       └── ui/                   # TablePrinter & interactive CLI menus
```

---

## 💡 Core OOP Principles Implemented

### 1. Encapsulation
- All domain entity fields (`Person`, `Doctor`, `Patient`, `Room`, `Bill`) are marked `private`.
- Controlled access via getters and setters with business validation rules (e.g. non-empty names, validated phone digits, non-negative consultation fees and room rates).

### 2. Abstraction
- **Abstract Class `Person`**: Represents common human traits (`id`, `name`, `gender`, `phone`, `email`) while declaring the abstract method `public abstract String getRole()`.
- **DAO Interfaces (`GenericDAO<T, ID>`, etc.)**: Hide all underlying database access mechanics, SQL queries, and connection handling from the service and UI layers.

### 3. Inheritance
- `Doctor` extends `Person`: Inherits personal identity data and adds professional attributes (`specialization`, `qualification`, `consultationFee`, `departmentId`). Uses `super(...)` constructor chaining.
- `Patient` extends `Person`: Inherits identity data and adds clinical attributes (`bloodGroup`, `emergencyContact`, `medicalHistory`, `registeredAt`).

### 4. Polymorphism
- **Method Overriding**: Subclasses provide concrete implementations of `getRole()`, `getContactInfo()`, and `toString()`.
- **Interface-based Polymorphism (`Billable`)**: The `Billable` interface defines `calculateSubtotal()` and `getBillingDescription()`, implemented by `Bill` to dynamically compute itemized charges (doctor fees, room days, medication, and 5% taxes).

---

## 🚀 How to Run the Java Application

### Option 1: Double-Click or Run Batch Script (Windows CMD)
```cmd
cd C:\Users\kapad\.gemini\antigravity\scratch\HospitalManagementSystem
run.bat
```

### Option 2: PowerShell
```powershell
Set-Location "C:\Users\kapad\.gemini\antigravity\scratch\HospitalManagementSystem"
.\run.ps1
```

### Option 3: In Your IDE (VS Code / IntelliJ / Eclipse)
Open the folder in your IDE. The included `pom.xml` configures dependencies and run configurations automatically.
