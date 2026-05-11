# Manual Test Script V2

Use this script for the official console + database demo.

## 1. Database Import

Start MySQL/MariaDB, then run:

```sql
SOURCE src/main/resources/schema.sql;
SOURCE src/main/resources/seed.sql;
```

Expected result:

- database `library_db` exists
- all demo accounts exist
- at least 10 books exist
- multiple copies, loans, fines, and reservations exist

For a database-focused presentation, see `DEMO_DATABASE_SCENARIOS.md`.

Seeded login accounts:

- `admin1` / `admin123`
- `lib1` / `lib123`
- `lib2` / `lib123`
- `mem1` / `mem123`
- `mem2` / `mem123`
- `mem3` / `mem123`
- `mem4` / `mem123`
- `mem5` / `mem123`

## 2. Compile

```bash
mvn clean compile
```

Expected result:

- build success

Optional Windows helper:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-and-open.ps1
```

This checks Java 17+, Maven, MySQL, required project files, compiles the project, and can optionally import the demo database, run the console app, and open the project in IntelliJ or VS Code.

## 3. Run

Run `com.library.Main` from IntelliJ, or:

```bash
mvn exec:java -Dexec.mainClass="com.library.Main"
```

## 4. Login Admin

Credentials:

- username: `admin1`
- password: `admin123`

Expected result:

- admin menu opens
- user list shows admin, librarian, and member

## 5. Login Librarian

Credentials:

- username: `lib1`
- password: `lib123`

Expected result:

- librarian menu opens

## 6. Add/Search Book

From librarian:

1. Open book management.
2. List books.
3. Search `Clean`.

Expected result:

- `Clean Code` appears
- category and copies are shown

Optional:

1. Add a new category.
2. Add a new author.
3. Add a new book.
4. Add a copy.

## 7. Borrow Book

From librarian:

1. Choose create loan.
2. Member ID: id of `mem1`.
3. Copy ID: id of `BC001`.

Expected result:

- a loan is created
- due date is today + 14 days
- copy `BC001` becomes `BORROWED`

## 8. Login Member

Credentials:

- username: `mem1`
- password: `mem123`

Expected result:

- member menu opens
- member can search books
- member can view own loans

## 9. Return Book

From librarian:

1. Choose return loan.
2. Enter the active loan ID.

Expected result:

- loan becomes `RETURNED`
- copy becomes `AVAILABLE`
- fine is `0` if returned on time

## 10. Fine Creation

Create another loan, then in MySQL run:

```sql
UPDATE loans
SET due_date = DATE_SUB(CURDATE(), INTERVAL 3 DAY)
WHERE loan_id = YOUR_LOAN_ID;
```

Return the loan from the console.

Expected result:

- fine amount is `6.00`
- fine status is `UNPAID`
- member cannot borrow again until the fine is paid

## 11. Fine Payment

From librarian:

1. Open fines menu.
2. List unpaid fines.
3. Mark the fine as paid.

Expected result:

- fine status becomes `PAID`
- member can borrow again

## 12. Reservation

To test reservation:

1. Make all copies of a book unavailable by borrowing them.
2. Login as member.
3. Reserve the book.
4. Try reserving the same book again.

Expected result:

- first reservation is created
- duplicate reservation is refused

Extra rule:

- if at least one copy is `AVAILABLE`, reservation is refused because the book can be borrowed directly.

From librarian:

1. List reservations.
2. Cancel the reservation.

Expected result:

- reservation status becomes `CANCELLED`

## 13. Role-Based Menus

Verify:

- admin sees user management
- librarian sees library operations
- member sees only search, own loans, own fines, and reservations

## Notes

This is the main test path for V2. JUnit tests are optional bonus; the manual database demo is more important for the course defense.
