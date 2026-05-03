package com.library.ui;

import com.library.app.ApplicationContext;
import com.library.enums.CopyStatus;
import com.library.enums.Role;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Category;
import com.library.model.Fine;
import com.library.model.Librarian;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.model.Staff;
import com.library.model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {
    private final ApplicationContext applicationContext;
    private final Scanner scanner;

    public ConsoleMenu(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Library Borrowing Management System ===");
        bootstrapIfNeeded();

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. Connexion");
            System.out.println("2. Ouvrir le bonus Swing");
            System.out.println("3. Quitter");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> loginFlow();
                case 2 -> new LibrarySwingApp(applicationContext).launch();
                case 3 -> running = false;
                default -> System.out.println("Choix invalide.");
            }
        }
        System.out.println("Application fermee.");
    }

    private void bootstrapIfNeeded() {
        try {
            if (applicationContext.getUserService().listUsers().isEmpty()) {
                System.out.println("Aucun utilisateur detecte. Creation du premier administrateur.");
                createStaffAccount(Role.ADMIN);
            }
        } catch (Exception exception) {
            System.out.println("Bootstrap saute : " + exception.getMessage());
        }
    }

    private void loginFlow() {
        try {
            String username = readLine("Nom d'utilisateur : ");
            String password = readLine("Mot de passe : ");
            User user = applicationContext.getAuthService().login(username, password);
            switch (user.getRole()) {
                case ADMIN -> adminMenu(user);
                case LIBRARIAN -> librarianMenu(user);
                case MEMBER -> memberMenu((Member) user);
            }
        } catch (Exception exception) {
            System.out.println("Erreur : " + exception.getMessage());
        }
    }

    private void adminMenu(User currentUser) {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Menu administrateur ===");
            System.out.println("1. Ajouter un bibliothecaire");
            System.out.println("2. Ajouter un administrateur");
            System.out.println("3. Lister les utilisateurs");
            System.out.println("4. Rechercher un utilisateur");
            System.out.println("5. Modifier un utilisateur");
            System.out.println("6. Desactiver un utilisateur");
            System.out.println("7. Reactiver un utilisateur");
            System.out.println("8. Consulter les rapports");
            System.out.println("9. Deconnexion");
            int choice = readInt("Choix : ");
            try {
                switch (choice) {
                    case 1 -> createStaffAccount(Role.LIBRARIAN);
                    case 2 -> createStaffAccount(Role.ADMIN);
                    case 3 -> applicationContext.getUserService().listUsers().forEach(this::printUser);
                    case 4 -> applicationContext.getUserService().searchUsers(readLine("Mot-cle : ")).forEach(this::printUser);
                    case 5 -> updateUser();
                    case 6 -> applicationContext.getUserService().deactivateUser(readInt("ID utilisateur : "));
                    case 7 -> applicationContext.getUserService().reactivateUser(readInt("ID utilisateur : "));
                    case 8 -> printReports();
                    case 9 -> back = true;
                    default -> System.out.println("Choix invalide.");
                }
            } catch (Exception exception) {
                System.out.println("Erreur : " + exception.getMessage());
            }
        }
    }

    private void librarianMenu(User currentUser) {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Menu bibliothecaire ===");
            System.out.println("1. Gerer les livres");
            System.out.println("2. Gerer les exemplaires");
            System.out.println("3. Gerer les adherents");
            System.out.println("4. Creer un emprunt");
            System.out.println("5. Enregistrer un retour");
            System.out.println("6. Consulter les retards");
            System.out.println("7. Gerer les penalites");
            System.out.println("8. Gerer les reservations");
            System.out.println("9. Consulter les rapports");
            System.out.println("10. Deconnexion");
            int choice = readInt("Choix : ");
            try {
                switch (choice) {
                    case 1 -> booksMenu();
                    case 2 -> copiesMenu();
                    case 3 -> membersMenu();
                    case 4 -> createLoan(currentUser);
                    case 5 -> returnLoan();
                    case 6 -> applicationContext.getLoanService().listOverdueLoans().forEach(this::printLoan);
                    case 7 -> finesMenu();
                    case 8 -> reservationsMenu();
                    case 9 -> printReports();
                    case 10 -> back = true;
                    default -> System.out.println("Choix invalide.");
                }
            } catch (Exception exception) {
                System.out.println("Erreur : " + exception.getMessage());
            }
        }
    }

    private void memberMenu(Member member) {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Menu adherent ===");
            System.out.println("1. Rechercher des livres");
            System.out.println("2. Consulter mes emprunts");
            System.out.println("3. Consulter mes penalites");
            System.out.println("4. Consulter mes reservations");
            System.out.println("5. Reserver un livre");
            System.out.println("6. Deconnexion");
            int choice = readInt("Choix : ");
            try {
                switch (choice) {
                    case 1 -> applicationContext.getBookService().searchBooks(readLine("Mot-cle : ")).forEach(this::printBook);
                    case 2 -> applicationContext.getMemberService().getLoanHistory(member.getId()).forEach(this::printLoan);
                    case 3 -> applicationContext.getMemberService().getFines(member.getId()).forEach(this::printFine);
                    case 4 -> applicationContext.getReservationService().listMemberReservations(member.getId()).forEach(this::printReservation);
                    case 5 -> {
                        Reservation reservation = applicationContext.getReservationService()
                                .createReservation(readInt("ID livre : "), member.getId());
                        System.out.println("Reservation creee : #" + reservation.getReservationId());
                    }
                    case 6 -> back = true;
                    default -> System.out.println("Choix invalide.");
                }
            } catch (Exception exception) {
                System.out.println("Erreur : " + exception.getMessage());
            }
        }
    }

    private void booksMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Gestion des livres ===");
            System.out.println("1. Ajouter un livre");
            System.out.println("2. Modifier un livre");
            System.out.println("3. Desactiver un livre");
            System.out.println("4. Lister les livres");
            System.out.println("5. Rechercher un livre");
            System.out.println("6. Ajouter une categorie");
            System.out.println("7. Ajouter un auteur");
            System.out.println("8. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> addBook();
                case 2 -> updateBook();
                case 3 -> applicationContext.getBookService().deactivateBook(readInt("ID livre : "));
                case 4 -> applicationContext.getBookService().listBooks().forEach(this::printBook);
                case 5 -> applicationContext.getBookService().searchBooks(readLine("Mot-cle : ")).forEach(this::printBook);
                case 6 -> System.out.println("Categorie creee : " + applicationContext.getBookService()
                        .createCategory(readLine("Nom : "), readLine("Description : ")).getName());
                case 7 -> System.out.println("Auteur cree : " + applicationContext.getBookService()
                        .createAuthor(readLine("Prenom : "), readLine("Nom : ")).getFullName());
                case 8 -> back = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private void copiesMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Gestion des exemplaires ===");
            System.out.println("1. Ajouter un exemplaire");
            System.out.println("2. Modifier le statut");
            System.out.println("3. Lister tous les exemplaires");
            System.out.println("4. Supprimer un exemplaire");
            System.out.println("5. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> addCopy();
                case 2 -> updateCopyStatus();
                case 3 -> applicationContext.getBookCopyService().listCopies().forEach(this::printCopy);
                case 4 -> applicationContext.getBookCopyService().deleteCopy(readInt("ID exemplaire : "));
                case 5 -> back = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private void membersMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Gestion des adherents ===");
            System.out.println("1. Ajouter un adherent");
            System.out.println("2. Modifier un adherent");
            System.out.println("3. Lister les adherents");
            System.out.println("4. Rechercher un adherent");
            System.out.println("5. Voir l'historique d'un adherent");
            System.out.println("6. Voir les penalites d'un adherent");
            System.out.println("7. Desactiver un adherent");
            System.out.println("8. Verifier l'eligibilite d'emprunt");
            System.out.println("9. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> addMember();
                case 2 -> updateMember();
                case 3 -> applicationContext.getMemberService().listMembers().forEach(this::printMember);
                case 4 -> applicationContext.getMemberService().searchMembers(readLine("Mot-cle : ")).forEach(this::printMember);
                case 5 -> applicationContext.getMemberService().getLoanHistory(readInt("ID adherent : ")).forEach(this::printLoan);
                case 6 -> applicationContext.getMemberService().getFines(readInt("ID adherent : ")).forEach(this::printFine);
                case 7 -> applicationContext.getMemberService().deactivateMember(readInt("ID adherent : "));
                case 8 -> System.out.println(applicationContext.getMemberService().canBorrow(readInt("ID adherent : "))
                        ? "L'adherent peut emprunter." : "L'adherent ne peut pas emprunter.");
                case 9 -> back = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private void finesMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Gestion des penalites ===");
            System.out.println("1. Lister les penalites impayees");
            System.out.println("2. Marquer comme payee");
            System.out.println("3. Annuler une penalite");
            System.out.println("4. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> applicationContext.getFineService().listUnpaidFines().forEach(this::printFine);
                case 2 -> applicationContext.getFineService().markPaid(readInt("ID penalite : "));
                case 3 -> applicationContext.getFineService().cancel(readInt("ID penalite : "));
                case 4 -> back = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private void reservationsMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("=== Gestion des reservations ===");
            System.out.println("1. Lister les reservations");
            System.out.println("2. Annuler une reservation");
            System.out.println("3. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> applicationContext.getReservationService().listReservations().forEach(this::printReservation);
                case 2 -> applicationContext.getReservationService().cancelReservation(readInt("ID reservation : "));
                case 3 -> back = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private void createLoan(User currentUser) throws SQLException {
        int memberId = readInt("ID adherent : ");
        int copyId = readInt("ID exemplaire : ");
        Loan loan = applicationContext.getLoanService().borrowBook(memberId, copyId, currentUser.getId());
        System.out.println("Emprunt cree : #" + loan.getLoanId() + ", retour prevu le " + loan.getDueDate());
    }

    private void returnLoan() throws SQLException {
        BigDecimal fineAmount = applicationContext.getLoanService().returnBook(readInt("ID emprunt : "));
        System.out.println("Retour enregistre. Penalite : " + fineAmount);
    }

    private void printReports() throws SQLException {
        System.out.println("--- Emprunts actifs ---");
        applicationContext.getReportService().activeLoansReport().forEach(System.out::println);
        System.out.println("--- Retards ---");
        applicationContext.getReportService().overdueLoansReport().forEach(System.out::println);
        System.out.println("--- Penalites impayees ---");
        applicationContext.getReportService().unpaidFinesReport().forEach(System.out::println);
        System.out.println("--- Livres les plus empruntes ---");
        applicationContext.getReportService().mostBorrowedBooksReport().forEach(System.out::println);
        System.out.println("--- Adherents les plus actifs ---");
        applicationContext.getReportService().mostActiveMembersReport().forEach(System.out::println);
        System.out.println("--- Disponibilite par livre ---");
        applicationContext.getReportService().availableCopiesPerBookReport().forEach(System.out::println);
    }

    private void createStaffAccount(Role role) {
        try {
            Staff staff = applicationContext.getUserService().createStaffAccount(
                    role,
                    readLine("Nom d'utilisateur : "),
                    readLine("Mot de passe : "),
                    readLine("Prenom : "),
                    readLine("Nom : "),
                    readLine("Email : "),
                    readLine("Telephone : "),
                    readLine("Numero employe : "),
                    parseOptionalDate(readLine("Date embauche (YYYY-MM-DD, vide pour aujourd'hui) : "))
            );
            System.out.println("Compte cree avec l'ID " + staff.getId());
        } catch (Exception exception) {
            System.out.println("Erreur : " + exception.getMessage());
        }
    }

    private void updateUser() throws SQLException {
        User user = applicationContext.getUserService().getUserById(readInt("ID utilisateur : "));
        user.setFirstName(readLineWithDefault("Prenom", user.getFirstName()));
        user.setLastName(readLineWithDefault("Nom", user.getLastName()));
        user.setEmail(readLineWithDefault("Email", user.getEmail()));
        user.setPhone(readLineWithDefault("Telephone", user.getPhone()));
        user.setUsername(readLineWithDefault("Nom d'utilisateur", user.getUsername()));
        applicationContext.getUserService().updateUser(user);
        String password = readLine("Nouveau mot de passe (vide pour conserver) : ");
        if (!password.isBlank()) {
            applicationContext.getUserService().changePassword(user.getId(), password);
        }
    }

    private void addMember() throws SQLException {
        Member member = applicationContext.getMemberService().createMember(
                readLine("Nom d'utilisateur : "),
                readLine("Mot de passe : "),
                readLine("Prenom : "),
                readLine("Nom : "),
                readLine("Email : "),
                readLine("Telephone : "),
                readLine("Numero d'adhesion : "),
                readLine("Adresse : "),
                parseOptionalDate(readLine("Date inscription (YYYY-MM-DD, vide pour aujourd'hui) : ")),
                readOptionalInt(readLine("Nombre max d'emprunts (vide pour 3) : "), 3)
        );
        System.out.println("Adherent cree avec l'ID " + member.getId());
    }

    private void updateMember() throws SQLException {
        Member member = applicationContext.getMemberService().getMemberById(readInt("ID adherent : "));
        member.setFirstName(readLineWithDefault("Prenom", member.getFirstName()));
        member.setLastName(readLineWithDefault("Nom", member.getLastName()));
        member.setEmail(readLineWithDefault("Email", member.getEmail()));
        member.setPhone(readLineWithDefault("Telephone", member.getPhone()));
        member.setAddress(readLineWithDefault("Adresse", member.getAddress()));
        member.setMembershipNumber(readLineWithDefault("Numero d'adhesion", member.getMembershipNumber()));
        member.setMaxLoans(readOptionalInt(readLineWithDefault("Max emprunts", String.valueOf(member.getMaxLoans())), member.getMaxLoans()));
        applicationContext.getMemberService().updateMember(member);
    }

    private void addBook() throws SQLException {
        Book book = new Book();
        book.setIsbn(readLine("ISBN (optionnel) : "));
        book.setTitle(readLine("Titre : "));
        book.setPublisher(readLine("Editeur : "));
        book.setPublicationYear(readOptionalInt(readLine("Annee de publication : "), 0));
        book.setActive(true);
        book.setCategory(chooseCategory());
        book.setAuthors(chooseAuthors());
        Book saved = applicationContext.getBookService().createBook(book);
        System.out.println("Livre cree avec l'ID " + saved.getBookId());
    }

    private void updateBook() throws SQLException {
        Book book = applicationContext.getBookService().getBookById(readInt("ID livre : "));
        book.setIsbn(readLineWithDefault("ISBN", book.getIsbn()));
        book.setTitle(readLineWithDefault("Titre", book.getTitle()));
        book.setPublisher(readLineWithDefault("Editeur", book.getPublisher()));
        book.setPublicationYear(readOptionalInt(readLineWithDefault("Annee", String.valueOf(book.getPublicationYear())), book.getPublicationYear()));
        if (askYesNo("Changer la categorie ? (o/n) : ")) {
            book.setCategory(chooseCategory());
        }
        if (askYesNo("Changer les auteurs ? (o/n) : ")) {
            book.setAuthors(chooseAuthors());
        }
        applicationContext.getBookService().updateBook(book);
    }

    private void addCopy() throws SQLException {
        Book book = applicationContext.getBookService().getBookById(readInt("ID livre : "));
        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setBarcode(readLine("Code-barres : "));
        copy.setStatus(CopyStatus.valueOf(readLine("Statut (AVAILABLE/BORROWED/RESERVED/LOST/DAMAGED) : ").toUpperCase()));
        copy.setAcquisitionDate(parseOptionalDate(readLine("Date acquisition (YYYY-MM-DD, vide possible) : ")));
        BookCopy saved = applicationContext.getBookCopyService().createCopy(copy);
        System.out.println("Exemplaire cree avec l'ID " + saved.getCopyId());
    }

    private void updateCopyStatus() throws SQLException {
        int copyId = readInt("ID exemplaire : ");
        CopyStatus status = CopyStatus.valueOf(readLine("Nouveau statut : ").toUpperCase());
        applicationContext.getBookCopyService().updateStatus(copyId, status);
    }

    private Category chooseCategory() throws SQLException {
        List<Category> categories = applicationContext.getBookService().listCategories();
        if (categories.isEmpty()) {
            System.out.println("Aucune categorie. Creation rapide.");
            return applicationContext.getBookService().createCategory(readLine("Nom categorie : "), readLine("Description : "));
        }
        categories.forEach(category -> System.out.println(category.getCategoryId() + " - " + category.getName()));
        int id = readInt("ID categorie (0 pour aucune) : ");
        if (id == 0) {
            return null;
        }
        return categories.stream().filter(category -> category.getCategoryId() == id).findFirst().orElse(null);
    }

    private List<Author> chooseAuthors() throws SQLException {
        List<Author> authors = applicationContext.getBookService().listAuthors();
        if (authors.isEmpty()) {
            System.out.println("Aucun auteur. Creation rapide.");
            return List.of(applicationContext.getBookService().createAuthor(readLine("Prenom auteur : "), readLine("Nom auteur : ")));
        }
        authors.forEach(author -> System.out.println(author.getAuthorId() + " - " + author.getFullName()));
        String input = readLine("IDs auteurs separes par des virgules, ou vide pour creer un auteur : ");
        if (input.isBlank()) {
            return List.of(applicationContext.getBookService().createAuthor(readLine("Prenom auteur : "), readLine("Nom auteur : ")));
        }
        List<Author> selected = new ArrayList<>();
        for (String token : input.split(",")) {
            int id = Integer.parseInt(token.trim());
            authors.stream().filter(author -> author.getAuthorId() == id).findFirst().ifPresent(selected::add);
        }
        return selected;
    }

    private void printUser(User user) {
        System.out.println("#" + user.getId() + " | " + user.getRole() + " | " + user.getFullName() + " | " + user.getUsername() + " | actif=" + user.isActive());
    }

    private void printMember(Member member) {
        System.out.println("#" + member.getId() + " | " + member.getFullName() + " | " + member.getMembershipNumber()
                + " | emprunts actifs=" + member.getActiveLoansCount() + " | impayes=" + member.hasUnpaidFines());
    }

    private void printBook(Book book) {
        String category = book.getCategory() != null ? book.getCategory().getName() : "Aucune categorie";
        System.out.println("#" + book.getBookId() + " | " + book.getTitle() + " | ISBN=" + book.getIsbn()
                + " | categorie=" + category + " | auteurs=" + book.getAuthors()
                + " | copies disponibles=" + book.getAvailableCopiesCount() + " | actif=" + book.isActive());
    }

    private void printCopy(BookCopy copy) {
        String title = copy.getBook() != null ? copy.getBook().getTitle() : "Livre inconnu";
        System.out.println("#" + copy.getCopyId() + " | " + title + " | " + copy.getBarcode() + " | " + copy.getStatus());
    }

    private void printLoan(Loan loan) {
        System.out.println("#" + loan.getLoanId() + " | " + loan.getBookCopy().getBook().getTitle()
                + " | " + loan.getMember().getFullName() + " | " + loan.getStatus()
                + " | echeance " + loan.getDueDate() + " | retour " + loan.getReturnDate());
    }

    private void printFine(Fine fine) {
        System.out.println("#" + fine.getFineId() + " | montant=" + fine.getAmount() + " | statut=" + fine.getStatus() + " | emprunt=" + fine.getLoan().getLoanId());
    }

    private void printReservation(Reservation reservation) {
        System.out.println("#" + reservation.getReservationId() + " | " + reservation.getBook().getTitle()
                + " | " + reservation.getMember().getFullName() + " | " + reservation.getStatus()
                + " | expiration=" + reservation.getExpiryDate());
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readLineWithDefault(String label, String currentValue) {
        System.out.print(label + " [" + (currentValue == null ? "" : currentValue) + "] : ");
        String input = scanner.nextLine().trim();
        return input.isBlank() ? currentValue : input;
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException exception) {
                System.out.println("Veuillez saisir un entier valide.");
            }
        }
    }

    private int readOptionalInt(String input, int defaultValue) {
        return input == null || input.isBlank() ? defaultValue : Integer.parseInt(input.trim());
    }

    private LocalDate parseOptionalDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value.trim());
    }

    private boolean askYesNo(String prompt) {
        return readLine(prompt).equalsIgnoreCase("o");
    }
}
