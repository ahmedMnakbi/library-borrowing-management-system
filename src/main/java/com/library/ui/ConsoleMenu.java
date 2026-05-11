package com.library.ui;

import com.library.enums.CopyStatus;
import com.library.enums.Role;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Category;
import com.library.model.Fine;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.model.Staff;
import com.library.model.User;
import com.library.service.LibraryService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {
    private final LibraryService service;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleMenu(LibraryService service) {
        this.service = service;
    }

    public void start() {
        System.out.println("=== Library Borrowing Management System V2 ===");
        bootstrapAdminIfNeeded();

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. Connexion");
            System.out.println("2. Quitter");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> loginFlow();
                case 2 -> running = false;
                default -> System.out.println("Choix invalide.");
            }
        }
        System.out.println("Application fermee.");
    }

    private void bootstrapAdminIfNeeded() {
        try {
            if (service.listUsers().isEmpty()) {
                System.out.println("Aucun utilisateur trouve. Creation du premier administrateur.");
                createStaffAccount(Role.ADMIN);
            }
        } catch (Exception exception) {
            System.out.println("Impossible de verifier les utilisateurs : " + exception.getMessage());
        }
    }

    private void loginFlow() {
        try {
            User user = service.login(readLine("Nom d'utilisateur : "), readLine("Mot de passe : "));
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
            System.out.println("5. Desactiver un utilisateur");
            System.out.println("6. Reactiver un utilisateur");
            System.out.println("7. Deconnexion");
            int choice = readInt("Choix : ");
            try {
                switch (choice) {
                    case 1 -> createStaffAccount(Role.LIBRARIAN);
                    case 2 -> createStaffAccount(Role.ADMIN);
                    case 3 -> service.listUsers().forEach(this::printUser);
                    case 4 -> service.searchUsers(readLine("Mot-cle : ")).forEach(this::printUser);
                    case 5 -> service.deactivateUser(readInt("ID utilisateur : "), currentUser.getId());
                    case 6 -> service.reactivateUser(readInt("ID utilisateur : "));
                    case 7 -> back = true;
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
            System.out.println("6. Gerer les penalites");
            System.out.println("7. Gerer les reservations");
            System.out.println("8. Deconnexion");
            int choice = readInt("Choix : ");
            try {
                switch (choice) {
                    case 1 -> booksMenu();
                    case 2 -> copiesMenu();
                    case 3 -> membersMenu();
                    case 4 -> createLoan(currentUser);
                    case 5 -> returnLoan();
                    case 6 -> finesMenu();
                    case 7 -> reservationsMenu();
                    case 8 -> back = true;
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
                    case 1 -> service.searchBooks(readLine("Mot-cle : ")).forEach(this::printBook);
                    case 2 -> service.listMemberLoans(member.getId()).forEach(this::printLoan);
                    case 3 -> service.listMemberFines(member.getId()).forEach(this::printFine);
                    case 4 -> service.listMemberReservations(member.getId()).forEach(this::printReservation);
                    case 5 -> {
                        Reservation reservation = service.createReservation(readInt("ID livre : "), member.getId());
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
            System.out.println("1. Ajouter une categorie");
            System.out.println("2. Ajouter un auteur");
            System.out.println("3. Ajouter un livre");
            System.out.println("4. Lister les livres");
            System.out.println("5. Rechercher un livre");
            System.out.println("6. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> System.out.println("Categorie creee : " + service.createCategory(readLine("Nom : "), readLine("Description : ")).getName());
                case 2 -> System.out.println("Auteur cree : " + service.createAuthor(readLine("Prenom : "), readLine("Nom : ")).getFullName());
                case 3 -> addBook();
                case 4 -> service.listBooks().forEach(this::printBook);
                case 5 -> service.searchBooks(readLine("Mot-cle : ")).forEach(this::printBook);
                case 6 -> back = true;
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
            System.out.println("2. Lister les exemplaires");
            System.out.println("3. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> addCopy();
                case 2 -> service.listCopies().forEach(this::printCopy);
                case 3 -> back = true;
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
            System.out.println("2. Lister les utilisateurs");
            System.out.println("3. Rechercher un utilisateur");
            System.out.println("4. Voir les emprunts d'un adherent");
            System.out.println("5. Voir les penalites d'un adherent");
            System.out.println("6. Retour");
            int choice = readInt("Choix : ");
            switch (choice) {
                case 1 -> addMember();
                case 2 -> service.listUsers().forEach(this::printUser);
                case 3 -> service.searchUsers(readLine("Mot-cle : ")).forEach(this::printUser);
                case 4 -> service.listMemberLoans(readInt("ID adherent : ")).forEach(this::printLoan);
                case 5 -> service.listMemberFines(readInt("ID adherent : ")).forEach(this::printFine);
                case 6 -> back = true;
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
                case 1 -> service.listUnpaidFines().forEach(this::printFine);
                case 2 -> service.markFinePaid(readInt("ID penalite : "));
                case 3 -> service.cancelFine(readInt("ID penalite : "));
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
                case 1 -> service.listReservations().forEach(this::printReservation);
                case 2 -> service.cancelReservation(readInt("ID reservation : "));
                case 3 -> back = true;
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private void createStaffAccount(Role role) {
        try {
            Staff staff = service.createStaffAccount(
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

    private void addMember() throws SQLException {
        Member member = service.createMember(
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

    private void addBook() throws SQLException {
        Book book = new Book();
        book.setIsbn(readLine("ISBN (optionnel) : "));
        book.setTitle(readLine("Titre : "));
        book.setPublisher(readLine("Editeur : "));
        book.setPublicationYear(readOptionalInt(readLine("Annee de publication : "), 0));
        book.setCategory(chooseCategory());
        book.setAuthors(chooseAuthors());
        Book saved = service.createBook(book);
        System.out.println("Livre cree avec l'ID " + saved.getBookId());
    }

    private void addCopy() throws SQLException {
        Book book = service.getBookById(readInt("ID livre : "));
        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setBarcode(readLine("Code-barres : "));
        String status = readLine("Statut (vide pour AVAILABLE) : ");
        copy.setStatus(status.isBlank() ? CopyStatus.AVAILABLE : CopyStatus.valueOf(status.toUpperCase()));
        copy.setAcquisitionDate(parseOptionalDate(readLine("Date acquisition (YYYY-MM-DD, vide possible) : ")));
        BookCopy saved = service.createCopy(copy);
        System.out.println("Exemplaire cree avec l'ID " + saved.getCopyId());
    }

    private void createLoan(User currentUser) throws SQLException {
        Loan loan = service.borrowBook(readInt("ID adherent : "), readInt("ID exemplaire : "), currentUser.getId());
        System.out.println("Emprunt cree : #" + loan.getLoanId() + ", retour prevu le " + loan.getDueDate());
    }

    private void returnLoan() throws SQLException {
        BigDecimal fineAmount = service.returnBook(readInt("ID emprunt : "));
        System.out.println("Retour enregistre. Penalite : " + fineAmount);
    }

    private Category chooseCategory() throws SQLException {
        List<Category> categories = service.listCategories();
        if (categories.isEmpty()) {
            System.out.println("Aucune categorie. Creation rapide.");
            return service.createCategory(readLine("Nom categorie : "), readLine("Description : "));
        }
        categories.forEach(category -> System.out.println(category.getCategoryId() + " - " + category.getName()));
        int id = readInt("ID categorie (0 pour aucune) : ");
        if (id == 0) {
            return null;
        }
        return categories.stream().filter(category -> category.getCategoryId() == id).findFirst().orElse(null);
    }

    private List<Author> chooseAuthors() throws SQLException {
        List<Author> authors = service.listAuthors();
        if (authors.isEmpty()) {
            System.out.println("Aucun auteur. Creation rapide.");
            return List.of(service.createAuthor(readLine("Prenom auteur : "), readLine("Nom auteur : ")));
        }
        authors.forEach(author -> System.out.println(author.getAuthorId() + " - " + author.getFullName()));
        String input = readLine("IDs auteurs separes par des virgules, ou vide pour creer un auteur : ");
        if (input.isBlank()) {
            return List.of(service.createAuthor(readLine("Prenom auteur : "), readLine("Nom auteur : ")));
        }
        List<Author> selected = new ArrayList<>();
        for (String token : input.split(",")) {
            int id = Integer.parseInt(token.trim());
            authors.stream().filter(author -> author.getAuthorId() == id).findFirst().ifPresent(selected::add);
        }
        return selected;
    }

    private void printUser(User user) {
        System.out.println("#" + user.getId() + " | " + user.getRole() + " | " + user.getFullName()
                + " | " + user.getUsername() + " | actif=" + user.isActive());
    }

    private void printBook(Book book) {
        String category = book.getCategory() != null ? book.getCategory().getName() : "Aucune categorie";
        System.out.println("#" + book.getBookId() + " | " + book.getTitle() + " | ISBN=" + book.getIsbn()
                + " | categorie=" + category + " | auteurs=" + book.getAuthors()
                + " | disponibles=" + book.getAvailableCopiesCount());
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
        System.out.println("#" + fine.getFineId() + " | montant=" + fine.getAmount()
                + " | statut=" + fine.getStatus() + " | emprunt=" + fine.getLoan().getLoanId());
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
}
