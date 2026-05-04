# Library Borrowing Management System

Java OOP application for managing a library with:
- user authentication by role
- books, authors, categories, and physical copies
- members and staff accounts
- loans and returns
- late fines
- reservations
- simple reports
- MySQL/MariaDB persistence through JDBC

The project was built for a student-level object-oriented programming assignment with a clean layered architecture and an IntelliJ-friendly structure.

## Features

- Role-based login: `ADMIN`, `LIBRARIAN`, `MEMBER`
- User management for admins
- Member management for librarians
- Book, author, category, and copy management
- Loan creation with eligibility checks
- Return processing with automatic fine calculation
- Fine payment and cancellation
- Reservation management
- Reports:
  - active loans
  - overdue loans
  - unpaid fines
  - most borrowed books
  - most active members
  - available copies per book
- Console UI as the main interface
- Minimal Swing bonus UI with `--swing`

## Tech Stack

- Java 17+
- Maven
- JDBC
- MySQL Connector/J
- MySQL or MariaDB
- JUnit 5
- IntelliJ IDEA

## Project Structure

```text
src/
├─ main/
│  ├─ java/com/library/
│  │  ├─ app
│  │  ├─ dao
│  │  ├─ enums
│  │  ├─ exception
│  │  ├─ model
│  │  ├─ service
│  │  ├─ ui
│  │  └─ util
│  └─ resources/
│     ├─ db.properties
│     └─ schema.sql
└─ test/
   └─ java/com/library/
```

## Architecture

- `model`: domain objects such as `Book`, `Member`, `Loan`, `Fine`
- `dao`: JDBC data access classes
- `service`: business rules and workflow logic
- `ui`: console and Swing user interfaces
- `util`: helper classes such as DB connection and password hashing
- `enums`: typed statuses and roles
- `exception`: custom exceptions

## Main Business Rules

- Inactive users cannot log in
- Only `AVAILABLE` copies can be borrowed
- A member cannot exceed the active loan limit
- A member with unpaid fines cannot borrow
- A returned loan cannot be returned again
- A fine is created only when a return is late
- A book with active loans should not be deactivated as available inventory
- A member cannot create duplicate pending reservations for the same book

## Database

The SQL schema is in:

- [src/main/resources/schema.sql](src/main/resources/schema.sql)

Default JDBC configuration:

- [src/main/resources/db.properties](src/main/resources/db.properties)

Default values:

```properties
db.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=
```

## Run in IntelliJ IDEA

## Quick Setup After Clone / Pull

If you want the project to check the environment and prepare as much as possible automatically, run:

```powershell
.\setup.bat
```

Or directly:

```powershell
.\setup.ps1
```

What the setup script does:

- checks that Java is available
- checks whether Maven is available
- checks `db.properties`
- updates the JDBC configuration file if needed
- checks for a local MySQL or MariaDB client
- if XAMPP is detected and MySQL is not running, it tries to start MySQL automatically
- applies `schema.sql` automatically if the DB connection works
- compiles the project with Maven if Maven is installed
- creates `setup-report.txt` with a summary

Useful options:

```powershell
.\setup.ps1 -DbUser root -DbPassword your_password
.\setup.ps1 -DbHost localhost -DbPort 3306 -DbName library_db
.\setup.ps1 -SkipDatabase
.\setup.ps1 -SkipBuild
```

If something cannot be automated, the script prints exactly what is missing and what still needs to be done manually.

## Run in IntelliJ IDEA

### Option 1: Console version

1. Open the project folder in IntelliJ IDEA.
2. Let IntelliJ import the Maven project from `pom.xml`.
3. Make sure the project JDK is Java 17 or newer.
4. Start MySQL or MariaDB.
5. Create the database using `schema.sql`.
6. Update `db.properties` if needed.
7. Run:
   - `com.library.app.Main`

### Option 2: Swing bonus version

Run `com.library.app.Main` with this program argument:

```text
--swing
```

## Run on Another Machine

This section is the safest way to launch the project on a different computer.

### 1. Install the required software

Install:

- Java JDK 17 or newer
- IntelliJ IDEA
- MySQL Server or MariaDB
- Git

Optional but useful:

- XAMPP if you want a quick MariaDB setup on Windows
- MySQL Workbench or phpMyAdmin to inspect the database

### 2. Clone the repository

```bash
git clone https://github.com/ahmedMnakbi/library-borrowing-management-system.git
cd library-borrowing-management-system
```

### 3. Open the project

Open the cloned folder in IntelliJ IDEA and allow Maven import.

If IntelliJ asks for a JDK, choose Java 17+.

### 4. Create the database

Using MySQL or MariaDB, run:

```sql
SOURCE src/main/resources/schema.sql;
```

Or from a terminal:

```bash
mysql -u root -p < src/main/resources/schema.sql
```

On XAMPP for Windows, an example is:

```powershell
C:\xampp\mysql\bin\mysql.exe -u root < src\main\resources\schema.sql
```

### 5. Configure the JDBC connection

Edit:

- `src/main/resources/db.properties`

Example:

```properties
db.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_password
```

If the database runs on another port, update `3306`.

### 6. Build dependencies

If IntelliJ imported the project correctly, Maven dependencies should download automatically.

If you prefer terminal and Maven is installed:

```bash
mvn clean test
```

### 7. Launch the application

Run:

- `com.library.app.Main`

At first launch:

- if the `users` table is empty, the app will ask you to create the first administrator account

### 8. Log in and use the system

Suggested order:

1. create the first admin
2. log in as admin
3. create a librarian
4. log in as librarian
5. create categories, authors, books, copies, and members
6. test loans, returns, fines, reservations, and reports

## Tests

Test files are in:

- `src/test/java`

Covered areas include:

- fine calculation
- member eligibility
- loan rules
- reservation rules
- database connection integration

If Maven is installed:

```bash
mvn test
```

Note:
- the integration test depends on a valid database configuration and an active DB server

## Demo and Study Files

Extra documentation included in the repository:

- [TEST_SCRIPT.md](TEST_SCRIPT.md): step-by-step testing guide
- [CONCEPTS_USED.md](CONCEPTS_USED.md): concepts used in the project
- [PROF_DEMO_GUIDE.md](PROF_DEMO_GUIDE.md): practical demo guide for the professor
- [rapport_d](rapport_d): detailed implementation report

## Important Source Files

- [src/main/java/com/library/app/Main.java](src/main/java/com/library/app/Main.java)
- [src/main/java/com/library/ui/ConsoleMenu.java](src/main/java/com/library/ui/ConsoleMenu.java)
- [src/main/java/com/library/service/LoanService.java](src/main/java/com/library/service/LoanService.java)
- [src/main/java/com/library/dao/UserDAO.java](src/main/java/com/library/dao/UserDAO.java)
- [src/main/java/com/library/util/DatabaseConnection.java](src/main/java/com/library/util/DatabaseConnection.java)
- [src/main/resources/schema.sql](src/main/resources/schema.sql)

## Notes

- The project is designed to work well in IntelliJ IDEA even if Maven is not installed globally.
- The main interface is the console version.
- The Swing version is a small bonus, not the full primary UI.
- Passwords are stored as hashes, not plaintext.

## Submission Summary

This project demonstrates:

- object-oriented design
- layered architecture
- JDBC database access
- MySQL/MariaDB persistence
- validation and exception handling
- business-rule implementation
- testing and documentation
