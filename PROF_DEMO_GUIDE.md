# Professor Demo Guide V2

The V2 demo should be simple: show the console application, the OOP model, and the database.

## Before The Demo

1. Start MySQL/MariaDB.
2. Import `src/main/resources/schema.sql`.
3. Import `src/main/resources/seed.sql`.
4. Open the project in IntelliJ.
5. Run `com.library.Main`.

Keep these files ready:

- `src/main/java/com/library/Main.java`
- `src/main/java/com/library/ui/ConsoleMenu.java`
- `src/main/java/com/library/service/LibraryService.java`
- `src/main/java/com/library/database/LibraryRepository.java`
- `src/main/java/com/library/database/DatabaseConnection.java`
- `src/main/resources/schema.sql`
- `src/main/resources/seed.sql`
- model classes: `Person`, `User`, `Member`, `Staff`, `Admin`, `Librarian`, `Book`, `BookCopy`, `Loan`

## Demo Order

1. Show the package structure.
2. Show the model inheritance.
3. Show the database schema.
4. Run the console application.
5. Login as admin.
6. Login as librarian.
7. Login as member.
8. Demonstrate one complete borrow/return/fine/reservation scenario.

## What To Say About The Architecture

Use this explanation:

`The project has a simple structure. The model package contains the OOP classes. The console menu handles user interaction. The service class contains business rules. The repository class contains SQL. The database stores the real data.`

## Strong OOP Examples

Inheritance:

`Person -> User -> Member`

`Person -> User -> Staff -> Admin/Librarian`

Encapsulation:

`Book`, `Loan`, and `Member` use private fields with getters and setters.

Abstraction:

`Person` and `User` are abstract.

Polymorphism:

The login returns a `User`, then the program opens the correct menu depending on whether the real object is an admin, librarian, or member.

Exceptions:

Borrowing an unavailable copy throws a clear business exception.

Collections:

Books have `List<Author>` and `List<BookCopy>`.

## Strong Database Examples

Show these tables:

- `users`
- `members`
- `staff`
- `books`
- `book_copies`
- `loans`
- `fines`
- `reservations`

Important design choice:

`Book` and `BookCopy` are separated because a single title can have multiple physical copies.

## Demo Accounts

- Admin: `admin1` / `admin123`
- Librarian: `lib1` / `lib123`
- Member: `mem1` / `mem123`

## Avoid Overexplaining

Do not spend too much time on:

- Maven
- password hashing
- tests
- advanced architecture terms
- reports/statistics
- future visual interface

The professor requirement is mainly Java OOP plus a database. Everything else is bonus.
