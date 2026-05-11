# Library Borrowing Management System V2

V2 is a course-aligned Java library project. The main goal is to show Java OOP concepts with a real MySQL/MariaDB database and a console demo.

## Main Focus

- Java classes and objects
- Encapsulation with private/protected fields, getters, and setters
- Constructors
- Inheritance: `Person -> User -> Member` and `Person -> User -> Staff -> Admin/Librarian`
- Polymorphism and overriding through the user hierarchy
- Abstraction with abstract classes
- Exceptions for business errors
- Collections such as `List<Book>`, `List<Author>`, and `List<Loan>`
- Packages for organization
- JDBC with MySQL/MariaDB

## V2 Architecture

```text
src/main/java/com/library/
  Main.java
  model/
  enums/
  database/
    DatabaseConnection.java
    LibraryRepository.java
  service/
    LibraryService.java
  ui/
    ConsoleMenu.java
  exception/
  util/
    PasswordUtil.java
```

Simple explanation:

- `model` contains the OOP entities.
- `database/DatabaseConnection.java` opens the JDBC connection.
- `database/LibraryRepository.java` contains the SQL methods.
- `service/LibraryService.java` contains the business rules.
- `ui/ConsoleMenu.java` is the official console demo.

## Database

The database is required. This project is not an in-memory-only application.

Main tables:

- `users`
- `members`
- `staff`
- `categories`
- `authors`
- `books`
- `book_authors`
- `book_copies`
- `loans`
- `fines`
- `reservations`

`Book` and `BookCopy` are separated because one book title can have many physical copies, and each copy can have its own status.

`seed.sql` loads a richer demo database with several users, categories, authors, books, copies, loans, fines, and reservations. See `DEMO_DATABASE_SCENARIOS.md` for phpMyAdmin demo ideas.

## Setup

1. Start MySQL/MariaDB.
2. Check `src/main/resources/db.properties`.
3. Import the schema:

```sql
SOURCE src/main/resources/schema.sql;
```

4. Import demo data:

```sql
SOURCE src/main/resources/seed.sql;
```

5. Compile:

```bash
mvn clean compile
```

6. Run:

```bash
mvn exec:java -Dexec.mainClass="com.library.Main"
```

Or run `com.library.Main` directly from IntelliJ.


## Quick Setup Script

On Windows, you can run the setup helper:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-and-open.ps1
```

Or use the wrapper:

```bat
setup-and-open.bat
```

The script:

- checks Java/JDK 17+
- checks Maven
- checks MySQL/MariaDB availability
- checks required project and database files
- runs `mvn clean compile`
- optionally imports the demo database with `schema.sql` and `seed.sql`
- optionally runs the console app
- opens the project in IntelliJ IDEA, VS Code, or Windows Explorer

The script asks before installing tools or importing/resetting the demo database.

## Demo Accounts

| Role | Username | Password |
|---|---|---|
| Admin | `admin1` | `admin123` |
| Librarian | `lib1` | `lib123` |
| Member | `mem1` | `mem123` |

Passwords are stored hashed in the seed data. This is a small security bonus, not the central topic of the project.

## Main Workflows

- Admin creates staff accounts and activates/deactivates users.
- Librarian manages books, authors, categories, copies, members, loans, returns, fines, and reservations.
- Member searches books, views loans/fines/reservations, and reserves books.

Reservation rule:

- A member can reserve a book only when no physical copy is currently `AVAILABLE`.
- If a copy is available, the normal action is borrowing that copy through the librarian.

## Clean Submission ZIP

When preparing a ZIP for submission, exclude generated or local-only files:

- `.git/`
- `target/`
- `out/`
- `.idea/`
- `*.class`
- `setup-report.txt`

## Bonus / Future Improvements

- A visual interface can be added later if needed.
- JUnit tests can be expanded later.
- Reports/statistics can be added as bonus features.
- Maven is used to manage the MySQL connector, but it is not the main course concept.
