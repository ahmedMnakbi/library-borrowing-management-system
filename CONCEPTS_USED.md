# Concepts Used In V2

This file is a short study sheet for the course defense.

## Classes And Objects

Examples:

- `Book`
- `BookCopy`
- `Member`
- `Loan`
- `Fine`
- `Reservation`

A class is the blueprint. An object is a real instance used by the program.

## Attributes And Methods

Examples:

- `Book.title`
- `BookCopy.status`
- `Loan.borrowDate`
- `Loan.markAsReturned()`
- `Loan.calculateDelayDays()`
- `Member.canBorrow()`

## Constructors

The model classes use constructors to create initialized objects, for example:

- `Book(...)`
- `Member(...)`
- `Loan(...)`
- `Fine(...)`

## Encapsulation

The model classes use private/protected fields and public getters/setters.

Example:

- fields are hidden inside `Book`
- access is done through `getTitle()` and `setTitle()`

## Inheritance

Main hierarchy:

```text
Person
  User
    Member
    Staff
      Admin
      Librarian
```

This avoids repeating common fields such as id, name, email, username, role, and active status.

## Polymorphism / Overriding

The program can handle `Admin`, `Librarian`, and `Member` as `User` objects. At runtime, the real object type decides which role/menu is used.

## Abstraction

`Person` and `User` are abstract classes. They describe common structure, but the real objects are concrete classes such as `Member`, `Admin`, and `Librarian`.

## Exceptions

Custom exceptions make business errors clear:

- `AuthenticationException`
- `BookUnavailableException`
- `LoanLimitExceededException`
- `DuplicateReservationException`
- `EntityNotFoundException`
- `ValidationException`

## Collections

The project uses collections such as:

- `List<Book>`
- `List<Author>`
- `List<BookCopy>`
- `List<Loan>`
- `List<Reservation>`

## Packages

Packages organize the code:

- `model`
- `database`
- `service`
- `ui`
- `enums`
- `exception`
- `util`

## Database And JDBC

The database is required in V2.

Important files:

- `schema.sql`
- `seed.sql`
- `DatabaseConnection.java`
- `LibraryRepository.java`

`DatabaseConnection` opens the JDBC connection. `LibraryRepository` uses SQL and `PreparedStatement` to read/write data.

## Main Course Message

V2 demonstrates Java OOP with a real database and a console interface. Advanced items such as password hashing, Maven, tests, reports, and future visual interfaces are bonus topics only.
