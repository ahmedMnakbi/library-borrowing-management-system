package com.library.service;

import com.library.database.LibraryRepository;
import com.library.enums.CopyStatus;
import com.library.enums.Role;
import com.library.exception.AuthenticationException;
import com.library.exception.BookUnavailableException;
import com.library.exception.DuplicateReservationException;
import com.library.exception.EntityNotFoundException;
import com.library.exception.InvalidReturnException;
import com.library.exception.LoanLimitExceededException;
import com.library.exception.ValidationException;
import com.library.model.Admin;
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
import com.library.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LibraryService {
    private static final int LOAN_DAYS = 14;
    private static final BigDecimal DAILY_FINE = new BigDecimal("2.00");

    private final LibraryRepository repository;

    public LibraryService() {
        this(new LibraryRepository());
    }

    public LibraryService(LibraryRepository repository) {
        this.repository = repository;
    }

    public User login(String username, String password) throws SQLException {
        requireText(username, "Le nom d'utilisateur est obligatoire.");
        requireText(password, "Le mot de passe est obligatoire.");
        User user = repository.findUserByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Identifiants invalides."));
        if (!user.isActive()) {
            throw new AuthenticationException("Ce compte est desactive.");
        }
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Identifiants invalides.");
        }
        return user;
    }

    public Staff createStaffAccount(Role role, String username, String password, String firstName, String lastName,
                                    String email, String phone, String employeeNumber, LocalDate hireDate) throws SQLException {
        if (role != Role.ADMIN && role != Role.LIBRARIAN) {
            throw new ValidationException("Le role du personnel doit etre ADMIN ou LIBRARIAN.");
        }
        requireText(username, "Le nom d'utilisateur est obligatoire.");
        requireText(password, "Le mot de passe est obligatoire.");
        requireText(firstName, "Le prenom est obligatoire.");
        requireText(lastName, "Le nom est obligatoire.");
        requireText(employeeNumber, "Le numero employe est obligatoire.");

        Staff staff = role == Role.ADMIN ? new Admin() : new Librarian();
        staff.setUsername(username);
        staff.setPasswordHash(PasswordUtil.hashPassword(password));
        staff.setFirstName(firstName);
        staff.setLastName(lastName);
        staff.setEmail(blankToNull(email));
        staff.setPhone(blankToNull(phone));
        staff.setEmployeeNumber(employeeNumber);
        staff.setHireDate(hireDate != null ? hireDate : LocalDate.now());
        staff.setActive(true);
        return repository.saveStaff(staff);
    }

    public Member createMember(String username, String password, String firstName, String lastName, String email,
                               String phone, String membershipNumber, String address, LocalDate registrationDate,
                               int maxLoans) throws SQLException {
        requireText(username, "Le nom d'utilisateur est obligatoire.");
        requireText(password, "Le mot de passe est obligatoire.");
        requireText(firstName, "Le prenom est obligatoire.");
        requireText(lastName, "Le nom est obligatoire.");
        requireText(membershipNumber, "Le numero d'adhesion est obligatoire.");
        if (maxLoans <= 0) {
            throw new ValidationException("Le nombre maximum d'emprunts doit etre positif.");
        }

        Member member = new Member();
        member.setUsername(username);
        member.setPasswordHash(PasswordUtil.hashPassword(password));
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(blankToNull(email));
        member.setPhone(blankToNull(phone));
        member.setMembershipNumber(membershipNumber);
        member.setAddress(blankToNull(address));
        member.setRegistrationDate(registrationDate != null ? registrationDate : LocalDate.now());
        member.setMaxLoans(maxLoans);
        member.setActive(true);
        return repository.saveMember(member);
    }

    public List<User> listUsers() throws SQLException {
        return repository.listUsers();
    }

    public List<User> searchUsers(String keyword) throws SQLException {
        return repository.searchUsers(keyword);
    }

    public User getUserById(int userId) throws SQLException {
        return repository.findUserById(userId).orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable."));
    }

    public Member getMemberById(int memberId) throws SQLException {
        User user = getUserById(memberId);
        if (user instanceof Member member) {
            return member;
        }
        throw new EntityNotFoundException("Adherent introuvable.");
    }

    public void updateUser(User user) throws SQLException {
        repository.updateUser(user);
    }

    public void deactivateUser(int userId) throws SQLException {
        repository.setUserActive(userId, false);
    }

    public void deactivateUser(int userId, int currentUserId) throws SQLException {
        if (userId == currentUserId) {
            throw new ValidationException("Un administrateur ne peut pas desactiver son propre compte.");
        }
        deactivateUser(userId);
    }

    public void reactivateUser(int userId) throws SQLException {
        repository.setUserActive(userId, true);
    }

    public Category createCategory(String name, String description) throws SQLException {
        requireText(name, "Le nom de categorie est obligatoire.");
        return repository.saveCategory(new Category(0, name, blankToNull(description)));
    }

    public Author createAuthor(String firstName, String lastName) throws SQLException {
        requireText(firstName, "Le prenom de l'auteur est obligatoire.");
        requireText(lastName, "Le nom de l'auteur est obligatoire.");
        return repository.saveAuthor(new Author(0, firstName, lastName));
    }

    public List<Category> listCategories() throws SQLException {
        return repository.listCategories();
    }

    public List<Author> listAuthors() throws SQLException {
        return repository.listAuthors();
    }

    public Book createBook(Book book) throws SQLException {
        requireText(book.getTitle(), "Le titre du livre est obligatoire.");
        book.setActive(true);
        return repository.saveBook(book);
    }

    public Book getBookById(int bookId) throws SQLException {
        return repository.findBookById(bookId).orElseThrow(() -> new EntityNotFoundException("Livre introuvable."));
    }

    public List<Book> listBooks() throws SQLException {
        return repository.listBooks();
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return repository.searchBooks(keyword);
    }

    public BookCopy createCopy(BookCopy copy) throws SQLException {
        if (copy.getBook() == null || copy.getBook().getBookId() <= 0) {
            throw new ValidationException("Un exemplaire doit etre lie a un livre.");
        }
        requireText(copy.getBarcode(), "Le code-barres est obligatoire.");
        if (copy.getStatus() == null) {
            copy.setStatus(CopyStatus.AVAILABLE);
        }
        return repository.saveCopy(copy);
    }

    public List<BookCopy> listCopies() throws SQLException {
        return repository.listCopies();
    }

    public Loan borrowBook(int memberId, int copyId, int librarianId) throws SQLException {
        Member member = getMemberById(memberId);
        if (!member.isActive()) {
            throw new LoanLimitExceededException("L'adherent est desactive.");
        }
        int activeLoans = repository.countActiveLoansByMember(memberId);
        if (activeLoans >= member.getMaxLoans()) {
            throw new LoanLimitExceededException("L'adherent a atteint la limite d'emprunts.");
        }
        if (repository.hasUnpaidFines(memberId)) {
            throw new LoanLimitExceededException("L'adherent a des penalites impayees.");
        }
        BookCopy copy = repository.findCopyById(copyId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaire introuvable."));
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BookUnavailableException("Cet exemplaire n'est pas disponible.");
        }
        User librarian = getUserById(librarianId);
        if (librarian.getRole() != Role.LIBRARIAN && librarian.getRole() != Role.ADMIN) {
            throw new ValidationException("Seul le personnel peut creer un emprunt.");
        }
        LocalDate borrowDate = LocalDate.now();
        return repository.createLoan(memberId, copyId, librarianId, borrowDate, borrowDate.plusDays(LOAN_DAYS));
    }

    public BigDecimal returnBook(int loanId) throws SQLException {
        Loan loan = repository.findLoanById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Emprunt introuvable."));
        if (loan.getStatus().name().equals("RETURNED")) {
            throw new InvalidReturnException("Cet emprunt a deja ete retourne.");
        }
        LocalDate returnDate = LocalDate.now();
        long lateDays = loan.calculateDelayDays(returnDate);
        BigDecimal fine = DAILY_FINE.multiply(BigDecimal.valueOf(lateDays));
        loan.markAsReturned(returnDate);
        repository.returnLoan(loan, fine, lateDays > 0 ? "Retard de " + lateDays + " jour(s)" : null);
        return fine;
    }

    public List<Loan> listActiveLoans() throws SQLException {
        return repository.listActiveLoans();
    }

    public List<Loan> listMemberLoans(int memberId) throws SQLException {
        return repository.listMemberLoans(memberId);
    }

    public List<Fine> listUnpaidFines() throws SQLException {
        return repository.listUnpaidFines();
    }

    public List<Fine> listMemberFines(int memberId) throws SQLException {
        return repository.listMemberFines(memberId);
    }

    public void markFinePaid(int fineId) throws SQLException {
        repository.markFinePaid(fineId);
    }

    public void cancelFine(int fineId) throws SQLException {
        repository.cancelFine(fineId);
    }

    public Reservation createReservation(int bookId, int memberId) throws SQLException {
        getBookById(bookId);
        getMemberById(memberId);
        if (repository.hasAvailableCopy(bookId)) {
            throw new BookUnavailableException("Ce livre a encore un exemplaire disponible. Il faut emprunter l'exemplaire au lieu de reserver.");
        }
        if (repository.hasPendingReservation(memberId, bookId)) {
            throw new DuplicateReservationException("Une reservation en attente existe deja pour ce livre.");
        }
        LocalDate today = LocalDate.now();
        return repository.createReservation(bookId, memberId, today, today.plusDays(7));
    }

    public List<Reservation> listReservations() throws SQLException {
        return repository.listReservations();
    }

    public List<Reservation> listMemberReservations(int memberId) throws SQLException {
        return repository.listMemberReservations(memberId);
    }

    public void cancelReservation(int reservationId) throws SQLException {
        repository.cancelReservation(reservationId);
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
