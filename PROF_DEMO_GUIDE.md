# Professor Demo Guide

This file is a practical guide for showing the project to your professor.

The goal is not to give a formal presentation.
The goal is to:
- show that the application works
- explain the important parts simply
- show how the project was built from the start

## 1. What to prepare before the demo

Before meeting the professor:

1. Start MySQL/MariaDB in XAMPP
2. Open the project in IntelliJ
3. Make sure [Main.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/app/Main.java) runs
4. Keep these files open in tabs:
   - [TEST_SCRIPT.md](C:/Users/Mega%20pc/Desktop/java%20project/TEST_SCRIPT.md)
   - [rapport_d](C:/Users/Mega%20pc/Desktop/java%20project/rapport_d)
   - [schema.sql](C:/Users/Mega%20pc/Desktop/java%20project/src/main/resources/schema.sql)
   - [ConsoleMenu.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/ui/ConsoleMenu.java)
   - [LoanService.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/service/LoanService.java)
   - [DatabaseConnection.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/util/DatabaseConnection.java)
   - [UserDAO.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/dao/UserDAO.java)

## 2. Best order for the demo

Use this order. It is simple and logical.

### Step 1 - Show the project structure

Open the project tree and say:

`The project is separated into layers: model, dao, service, ui, util, enums, and exception.`

What to show:
- `src/main/java/com/library/model`
- `src/main/java/com/library/dao`
- `src/main/java/com/library/service`
- `src/main/java/com/library/ui`

What to say:

`I organized the project so each part has one responsibility.`

### Step 2 - Show the database schema

Open [schema.sql](C:/Users/Mega%20pc/Desktop/java%20project/src/main/resources/schema.sql)

What to say:

`I started by identifying the main entities from the cahier des charges, then I created the database schema with users, members, staff, books, copies, loans, fines, reservations, authors, and categories.`

Important point to explain:

`I separated Book and BookCopy because one title can have many physical copies with different statuses.`

That is one of the strongest design choices in the project.

### Step 3 - Run the application

Run [Main.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/app/Main.java)

What to say:

`The application runs in console mode as the main version. I also added a small Swing bonus version.`

### Step 4 - Show admin features

Log in as admin and show:

1. list users
2. create librarian
3. deactivate user
4. reactivate user

What to say:

`The admin manages user accounts and roles.`

If the professor asks why reactivation exists:

`At first there was only deactivation, then I improved it because a permanently blocked account was not practical.`

### Step 5 - Show librarian features

Log in as librarian and show:

1. add category
2. add author
3. add book
4. add copy
5. create member
6. create loan
7. return loan
8. manage fines
9. view reports

What to say:

`The librarian manages the daily library operations: books, copies, members, loans, returns, fines, and reservations.`

### Step 6 - Show member features

Log in as member and show:

1. search books
2. view own loans
3. view fines
4. reserve a book

What to say:

`The member role is limited to consultation and reservation-related actions.`

### Step 7 - Show one important full scenario

This is the best scenario to demonstrate:

1. create a book
2. create a copy
3. create a member
4. borrow the copy
5. return the copy
6. show the updated loan and copy status

What to say:

`This shows that the data moves correctly across the model, business logic, database, and user interface.`

### Step 8 - Show overdue fine example

Use the scenario from [TEST_SCRIPT.md](C:/Users/Mega%20pc/Desktop/java%20project/TEST_SCRIPT.md) where the `due_date` is changed in the database.

What to say:

`To test the fine calculation clearly, I simulated a late return by adjusting the due date in the database. Then the application generated the correct fine automatically.`

This demonstrates:
- SQL persistence
- business rule enforcement
- fine creation logic

### Step 9 - Show reports

Show:
- active loans
- overdue loans
- unpaid fines
- most borrowed books
- most active members
- available copies per book

What to say:

`The reports summarize the stored data and show that the application is not just CRUD, but also useful for management.`

## 3. The easiest way to explain how the code works

Use this short explanation:

### UI layer

What to say:

`The UI layer handles user interaction through menus.`

File:
- [ConsoleMenu.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/ui/ConsoleMenu.java)

### Service layer

What to say:

`The service layer contains the business rules. It decides whether an action is allowed before touching the database.`

Best file to show:
- [LoanService.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/service/LoanService.java)

### DAO layer

What to say:

`The DAO layer contains the SQL logic. It sends queries to the database and converts results into Java objects.`

Best file to show:
- [UserDAO.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/dao/UserDAO.java)

### Model layer

What to say:

`The model layer represents the real objects of the system, like Book, Member, Loan, and Fine.`

Best files to show:
- `Book.java`
- `BookCopy.java`
- `Loan.java`

## 4. Best examples of OOP to explain

If your professor asks about OOP, show these examples.

### Inheritance

What to say:

`I used inheritance to avoid repetition. Person is the base class, then User extends Person, then Member and Staff extend User, and Admin and Librarian extend Staff.`

Best files:
- [Person.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/model/Person.java)
- [User.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/model/User.java)
- [Member.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/model/Member.java)
- [Staff.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/model/Staff.java)

### Encapsulation

What to say:

`I used private attributes with getters and setters to protect internal data.`

Best file:
- any model file such as `Book.java`

### Abstraction

What to say:

`I used abstract classes and interfaces to define common structure without forcing one single implementation.`

Best files:
- `Person.java`
- `User.java`
- `FineCalculator.java`
- `GenericDAO.java`

### Polymorphism

What to say:

`I used polymorphism in the fine calculation and in the user hierarchy.`

Best files:
- `FineCalculator.java`
- `StandardFineCalculator.java`

## 5. Best examples of database work to explain

### JDBC connection

What to say:

`The connection to the database is centralized in one utility class so the DAO classes can reuse it.`

Best file:
- [DatabaseConnection.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/util/DatabaseConnection.java)

### PreparedStatement

What to say:

`I used PreparedStatement to keep SQL safer and cleaner.`

Best file:
- [UserDAO.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/dao/UserDAO.java)

### Transactions

What to say:

`I used transactions for borrow and return operations because they update multiple tables and must stay consistent.`

Best file:
- [LoanService.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/service/LoanService.java)

## 6. How to explain how the project was built from the start

You can say this almost exactly:

`I started from the cahier des charges. First I identified the required entities and relationships. Then I designed the MySQL schema. After that I created the Java model classes, then the DAO layer for JDBC access, then the service layer for business rules, and finally the console interface. After implementation I tested the full workflow, added reports, reservations, and corrected practical issues like user reactivation and JDBC configuration.`

That answer is clean and complete.

## 7. Simple answers to likely questions

### Question: Why did you separate Book and BookCopy?

Answer:

`Because one book title can have many physical copies, and each copy can have a different status like available, borrowed, lost, or damaged.`

### Question: Why did you use DAO?

Answer:

`To separate database access from business logic and keep the code cleaner.`

### Question: Where are the business rules?

Answer:

`Mainly in the service layer, especially LoanService, ReservationService, MemberService, and BookService.`

### Question: How do you connect Java to MySQL?

Answer:

`With JDBC through DatabaseConnection and the DAO classes.`

### Question: Where is the authentication logic?

Answer:

`In AuthService, with password verification and active-account checking.`

### Question: How do you prevent inconsistent borrowing?

Answer:

`The service checks eligibility first, then runs the database updates inside a transaction.`

### Question: How do you calculate fines?

Answer:

`With FineCalculator and StandardFineCalculator, based on delay days multiplied by 2.00 per day.`

## 8. Best files to show if the professor asks for proof

### To prove database design
- [schema.sql](C:/Users/Mega%20pc/Desktop/java%20project/src/main/resources/schema.sql)

### To prove OOP
- [Person.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/model/Person.java)
- [User.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/model/User.java)
- [Member.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/model/Member.java)

### To prove JDBC
- [DatabaseConnection.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/util/DatabaseConnection.java)
- [UserDAO.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/dao/UserDAO.java)

### To prove business logic
- [LoanService.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/service/LoanService.java)
- [ReservationService.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/service/ReservationService.java)

### To prove working interface
- [ConsoleMenu.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/ui/ConsoleMenu.java)

### To prove testing
- [TEST_SCRIPT.md](C:/Users/Mega%20pc/Desktop/java%20project/TEST_SCRIPT.md)
- files in `src/test/java`

## 9. What not to do during the demo

Avoid:
- explaining every file one by one unless asked
- jumping directly into advanced theory
- saying you do not understand DAO
- showing only code without running the program

Better approach:
- run the app
- show one complete workflow
- explain the logic simply
- show code only when needed

## 10. Best final sentence

At the end, you can say:

`This project follows the cahier des charges, uses Java OOP with JDBC and MySQL, separates responsibilities clearly, and supports the full library workflow from account management to loans, returns, fines, reservations, and reports.`

