# Concepts Used In This Project

This file is a study sheet for the `Library Borrowing Management System`.

For each concept, you will find:
- the concept name
- a simple definition
- where it appears in the project

## 1. Classes and Objects

Definition:
A class is a blueprint. An object is a real instance created from that class.

Where it appears:
- `Book`, `Member`, `Loan`, `Fine`, `Reservation`
- almost every file in `src/main/java/com/library/model`

## 2. Constructors

Definition:
A constructor is a special method used to create and initialize an object.

Where it appears:
- `Author.java`
- `Category.java`
- `Book.java`
- `BookCopy.java`
- `Loan.java`
- `Fine.java`
- `Reservation.java`

## 3. Attributes and Methods

Definition:
Attributes store data inside an object. Methods define behavior.

Where it appears:
- attributes like `title`, `isbn`, `status`
- methods like `canBorrow()`, `markAsReturned()`, `calculateDelayDays()`

## 4. Access Modifiers

Definition:
Access modifiers control visibility of attributes and methods.

Types used:
- `private`
- `protected`
- `public`

Where it appears:
- model classes
- service classes
- utility classes

## 5. Encapsulation

Definition:
Encapsulation means hiding internal data and controlling access through methods.

Where it appears:
- private fields in model classes
- getters and setters in `Person`, `User`, `Book`, `Loan`, etc.

## 6. Getters and Setters

Definition:
Getters return values. Setters modify values.

Where it appears:
- all main model classes

## 7. Packages

Definition:
Packages are folders that organize classes by responsibility.

Where it appears:
- `com.library.app`
- `com.library.model`
- `com.library.dao`
- `com.library.service`
- `com.library.ui`
- `com.library.util`
- `com.library.enums`
- `com.library.exception`

## 8. Inheritance

Definition:
Inheritance means one class extends another and reuses its properties and behavior.

Where it appears:
- `User extends Person`
- `Member extends User`
- `Staff extends User`
- `Librarian extends Staff`
- `Admin extends Staff`

## 9. Abstraction

Definition:
Abstraction means keeping only the important parts visible and hiding unnecessary details.

Where it appears:
- abstract class `Person`
- abstract class `User`
- interface `FineCalculator`
- interface `GenericDAO`

## 10. Interfaces

Definition:
An interface defines a contract that classes must follow.

Where it appears:
- `FineCalculator`
- `GenericDAO<T, ID>`

## 11. Polymorphism

Definition:
Polymorphism means different classes can be used through the same parent type or interface.

Where it appears:
- `StandardFineCalculator` implements `FineCalculator`
- `Member`, `Admin`, `Librarian` are all treated as `User`
- `UserDAO` maps different `Role` values to different object types

## 12. Method Overriding

Definition:
Method overriding means redefining inherited behavior in a child class.

Where it appears:
- in test fake DAO classes inside:
  - `ReservationServiceTest.java`
  - `LoanServiceRulesTest.java`

## 13. Composition

Definition:
Composition means one object contains or uses other objects.

Where it appears:
- `Book` contains `Category`, `List<Author>`, `List<BookCopy>`
- `Loan` contains `Member`, `BookCopy`, `Librarian`
- `Fine` contains `Loan`
- `Reservation` contains `Book` and `Member`

## 14. Enums

Definition:
Enums are fixed sets of constant values.

Where it appears:
- `Role`
- `CopyStatus`
- `LoanStatus`
- `FineStatus`
- `ReservationStatus`

## 15. Generics

Definition:
Generics allow code to work with different types safely.

Where it appears:
- `GenericDAO<T, ID>`
- collections like `List<Author>`

## 16. Collections

Definition:
Collections store groups of objects.

Where it appears:
- `List`
- `ArrayList`
- examples in `Book`, DAO classes, service classes, reports

## 17. Exception Handling

Definition:
Exception handling is used to detect and manage errors safely.

Where it appears:
- `try/catch`
- `throws SQLException`
- custom exceptions in `com.library.exception`

## 18. Custom Exceptions

Definition:
Custom exceptions are user-defined error types for clearer business errors.

Where it appears:
- `AuthenticationException`
- `BookUnavailableException`
- `LoanLimitExceededException`
- `InvalidReturnException`
- `DatabaseException`
- `ValidationException`
- `EntityNotFoundException`

## 19. Layered Architecture

Definition:
Layered architecture separates the application into levels with different responsibilities.

Layers used:
- UI
- Service
- DAO
- Model
- Util

Where it appears:
- full project structure

## 20. Separation of Responsibilities

Definition:
Each part of the project has one main job.

Where it appears:
- `ui` handles interaction
- `service` handles business rules
- `dao` handles SQL
- `model` represents data
- `util` provides helper logic

## 21. DAO Pattern

Definition:
DAO means `Data Access Object`. It separates database access from the rest of the code.

Where it appears:
- `UserDAO`
- `BookDAO`
- `BookCopyDAO`
- `LoanDAO`
- `FineDAO`
- `ReservationDAO`
- `AuthorDAO`
- `CategoryDAO`
- `MemberDAO`
- `StaffDAO`

## 22. Service Layer Pattern

Definition:
The service layer contains business logic between the UI and the DAO layer.

Where it appears:
- `AuthService`
- `UserService`
- `MemberService`
- `BookService`
- `BookCopyService`
- `LoanService`
- `FineService`
- `ReservationService`
- `ReportService`

## 23. Utility Classes

Definition:
Utility classes contain reusable helper methods.

Where it appears:
- `DatabaseConnection`
- `PasswordUtil`
- `Validator`

## 24. Relational Database Design

Definition:
A relational database stores data in linked tables.

Where it appears:
- `schema.sql`
- all DAO classes

## 25. Tables

Definition:
Tables are structures that store rows of data.

Where it appears in `schema.sql`:
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

## 26. Primary Keys

Definition:
A primary key uniquely identifies each row.

Where it appears:
- `user_id`
- `book_id`
- `copy_id`
- `loan_id`
- `fine_id`
- `reservation_id`

## 27. Foreign Keys

Definition:
Foreign keys create links between tables.

Where it appears:
- `members.member_id -> users.user_id`
- `staff.staff_id -> users.user_id`
- `books.category_id -> categories.category_id`
- `book_copies.book_id -> books.book_id`
- `loans.copy_id -> book_copies.copy_id`
- `loans.member_id -> members.member_id`
- `loans.librarian_id -> staff.staff_id`
- `fines.loan_id -> loans.loan_id`
- `reservations.book_id -> books.book_id`
- `reservations.member_id -> members.member_id`

## 28. One-to-One, One-to-Many, Many-to-Many Relationships

Definition:
These describe how records in one table relate to records in another.

Where it appears:
- one-to-one: `users` with `members` or `staff`
- one-to-many: one `book` to many `book_copies`
- many-to-many: `books` and `authors` through `book_authors`

## 29. Join Table

Definition:
A join table connects two tables in a many-to-many relationship.

Where it appears:
- `book_authors`

## 30. SQL CRUD

Definition:
CRUD means Create, Read, Update, Delete.

Where it appears:
- `INSERT`
- `SELECT`
- `UPDATE`
- `DELETE`
- especially in DAO classes

## 31. PreparedStatement

Definition:
`PreparedStatement` is a JDBC object used to execute parameterized SQL safely.

Where it appears:
- all DAO classes

Why it matters:
- prevents SQL injection
- required by the project rules

## 32. ResultSet

Definition:
`ResultSet` stores rows returned by an SQL query.

Where it appears:
- all DAO `find...` methods

## 33. JDBC

Definition:
JDBC is the Java API used to connect Java programs to relational databases.

Where it appears:
- `DatabaseConnection.java`
- all DAO classes

## 34. DriverManager and Connection

Definition:
`DriverManager` creates database connections. `Connection` represents an open session with the database.

Where it appears:
- `DatabaseConnection.getConnection()`

## 35. Transactions

Definition:
A transaction groups multiple SQL operations so they either all succeed or all fail.

Where it appears:
- `LoanService.borrowBook()`
- `LoanService.returnBook()`
- `UserDAO.save()`
- `UserDAO.update()`
- `BookDAO.save()`
- `BookDAO.update()`

Related concepts:
- `setAutoCommit(false)`
- `commit()`
- `rollback()`

## 36. Validation

Definition:
Validation checks that user input or data is acceptable before processing it.

Where it appears:
- `Validator.java`
- `UserService`
- `MemberService`
- `BookService`
- `AuthService`
- `PasswordUtil`

## 37. Password Hashing

Definition:
Hashing transforms a password into a secure encoded value instead of storing it as plain text.

Where it appears:
- `PasswordUtil.hashPassword()`
- `PasswordUtil.verifyPassword()`

## 38. Salt

Definition:
A salt is random data added before hashing a password to improve security.

Where it appears:
- `PasswordUtil`

## 39. PBKDF2

Definition:
PBKDF2 is a secure password hashing algorithm.

Where it appears:
- `PasswordUtil`

## 40. Soft Delete / Logical Deactivation

Definition:
Instead of deleting a record physically, the program marks it inactive.

Where it appears:
- `users.active`
- `books.active`
- `UserDAO.delete()`
- `BookDAO.delete()`

## 41. Authentication

Definition:
Authentication checks if a user is really who they claim to be.

Where it appears:
- `AuthService.login()`

## 42. Authorization by Role

Definition:
Authorization decides what a logged-in user can access based on their role.

Where it appears:
- `ConsoleMenu`
- role-based menus for admin, librarian, member

## 43. Business Rules

Definition:
Business rules are the real rules of how the library must work.

Where it appears:
- only available copies can be borrowed
- max active loans
- unpaid fines block borrowing
- returned loan cannot be returned again
- duplicate pending reservation is forbidden

Main file:
- `LoanService`
- `ReservationService`
- `BookService`
- `MemberService`

## 44. Fine Calculation

Definition:
The program calculates a penalty when a book is returned late.

Where it appears:
- `FineCalculator`
- `StandardFineCalculator`
- `LoanService.returnBook()`

## 45. BigDecimal

Definition:
`BigDecimal` is used for exact decimal values, especially money.

Where it appears:
- `Fine.amount`
- `StandardFineCalculator`
- `LoanService`

## 46. Date and Time API

Definition:
Java date/time classes are used for dates and timestamps.

Where it appears:
- `LocalDate`
- `LocalDateTime`
- used in loans, fines, reservations, registration, hire date

## 47. Search Functionality

Definition:
Search allows filtering data by keyword or field.

Where it appears:
- `UserDAO.search()`
- `MemberDAO.search()`
- `BookDAO.search()`
- console menu search actions

## 48. Aggregate Queries

Definition:
Aggregate queries calculate totals, sums, counts, rankings.

Where it appears:
- `ReportService`
- `COUNT(*)`
- `SUM(...)`

## 49. Reports

Definition:
Reports summarize useful data for decision-making.

Where it appears:
- active loans
- overdue loans
- unpaid fines
- most borrowed books
- most active members
- available copies per book

Main file:
- `ReportService.java`

## 50. Console UI

Definition:
A console UI is a text-based interface shown in the terminal.

Where it appears:
- `ConsoleMenu.java`

## 51. Swing UI

Definition:
Swing is a Java library for building graphical interfaces.

Where it appears:
- `LibrarySwingApp.java`

## 52. JUnit 5

Definition:
JUnit 5 is a Java testing framework.

Where it appears:
- all files in `src/test/java`

## 53. Unit Testing

Definition:
Unit testing checks small pieces of logic independently.

Where it appears:
- `LoanTest`
- `MemberTest`
- `StandardFineCalculatorTest`
- `LoanServiceRulesTest`
- `ReservationServiceTest`

## 54. Integration Testing

Definition:
Integration testing checks if multiple real parts work together.

Where it appears:
- `DatabaseConnectionIntegrationTest`

## 55. Test Doubles / Fake Objects

Definition:
Fake objects replace real database behavior during testing.

Where it appears:
- inner fake DAO classes in:
  - `ReservationServiceTest`
  - `LoanServiceRulesTest`

## 56. Maven

Definition:
Maven is a build and dependency management tool for Java projects.

Where it appears:
- `pom.xml`

## 57. IntelliJ Project Integration

Definition:
The project is structured so IntelliJ can import, run, and manage it easily.

Where it appears:
- Maven structure
- `.idea` folder
- `src/main/java`
- `src/main/resources`
- `src/test/java`

## 58. Resource Files

Definition:
Resource files are non-Java files used by the application at runtime.

Where it appears:
- `db.properties`
- `schema.sql`

## 59. Compiled Output

Definition:
Compiled output is generated from source code.

Where it appears:
- `out/`
- `target/`

## 60. Documentation Files

Definition:
Documentation files explain the project, testing, and implementation.

Where it appears:
- `README.md`
- `TEST_SCRIPT.md`
- `rapport_d`
- `CONCEPTS_USED.md`

## Final Summary

The project uses concepts from:
- core Java
- object-oriented programming
- database design
- JDBC
- software architecture
- security
- testing
- user interface design

The most important concepts for your course presentation are probably:
- classes and objects
- encapsulation
- inheritance
- polymorphism
- abstraction
- interfaces
- enums
- collections
- exceptions
- JDBC
- prepared statements
- transactions
- layered architecture
- DAO pattern
- service layer

