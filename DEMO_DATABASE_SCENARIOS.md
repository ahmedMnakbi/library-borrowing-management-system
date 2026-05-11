# Demo Database Scenarios

This guide explains the richer demo data loaded by `src/main/resources/seed.sql`.

## Demo Accounts

| Role | Username | Password |
|---|---|---|
| Admin | `admin1` | `admin123` |
| Librarian | `lib1` | `lib123` |
| Librarian | `lib2` | `lib123` |
| Member | `mem1` | `mem123` |
| Member | `mem2` | `mem123` |
| Member | `mem3` | `mem123` |
| Member | `mem4` | `mem123` |
| Member | `mem5` | `mem123` |

Passwords are stored as hashes, not plain text.

## What To Show In phpMyAdmin

Open these tables:

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

## Useful SQL Checks

Use the actual V2 column names:

```sql
USE library_db;

SELECT username, role, active FROM users;
SELECT title, isbn FROM books;
SELECT barcode, status FROM book_copies;
SELECT loan_id, member_id, copy_id, status FROM loans;
SELECT fine_id, amount, status FROM fines;
SELECT reservation_id, member_id, book_id, status FROM reservations;
```

## Best Database Demo

1. Show `users` and explain roles: admin, librarian, member.
2. Show `books`, `authors`, `categories`, and `book_authors`.
3. Show `book_copies` and explain why `Book` and `BookCopy` are separated.
4. Borrow an available copy, then refresh `book_copies` and `loans`.
5. Return a borrowed copy, then refresh `book_copies`, `loans`, and `fines`.
6. Show the seeded unpaid fine and paid fine.
7. Show pending and cancelled reservations.

## Ready-Made Scenarios

- Clean borrow demo: use `mem1` with an `AVAILABLE` copy such as `BC002`, `BC004`, or `BC005`.
- Late return/fine demo: borrow an available copy, update its `due_date` in MySQL to a past date, then return it from the console.
- Reservation demo: `Foundation` has all copies unavailable, so it can be reserved.
- Duplicate reservation demo: reserve the same unavailable book twice with the same member.
- Borrow blocked by fine: `mem4` has an unpaid fine and should be blocked from borrowing.
