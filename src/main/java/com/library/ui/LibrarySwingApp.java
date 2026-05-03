package com.library.ui;

import com.library.app.ApplicationContext;
import com.library.enums.Role;
import com.library.model.User;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

public class LibrarySwingApp {
    private final ApplicationContext applicationContext;

    public LibrarySwingApp(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void launch() {
        SwingUtilities.invokeLater(this::showLoginFrame);
    }

    private void showLoginFrame() {
        JFrame frame = new JFrame("Library Borrowing Management System");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(420, 220);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Connexion");

        panel.add(new JLabel("Nom d'utilisateur"));
        panel.add(usernameField);
        panel.add(new JLabel("Mot de passe"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        loginButton.addActionListener(event -> {
            try {
                User user = applicationContext.getAuthService().login(
                        usernameField.getText().trim(),
                        new String(passwordField.getPassword())
                );
                frame.dispose();
                showDashboard(user);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(frame, exception.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private void showDashboard(User user) {
        JFrame frame = new JFrame("Dashboard - " + user.getRole());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(720, 480);
        frame.setLocationRelativeTo(null);

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JPanel actions = new JPanel(new GridLayout(2, 3, 8, 8));
        JButton booksButton = new JButton("Livres");
        JButton membersButton = new JButton("Adherents");
        JButton loansButton = new JButton("Emprunts");
        JButton finesButton = new JButton("Penalites");
        JButton reportsButton = new JButton("Rapports");
        JButton logoutButton = new JButton("Fermer");

        booksButton.addActionListener(event -> loadBooks(outputArea));
        membersButton.addActionListener(event -> loadMembers(outputArea));
        loansButton.addActionListener(event -> loadLoans(outputArea));
        finesButton.addActionListener(event -> loadFines(outputArea, user));
        reportsButton.addActionListener(event -> loadReports(outputArea, user));
        logoutButton.addActionListener(event -> frame.dispose());

        actions.add(booksButton);
        actions.add(membersButton);
        actions.add(loansButton);
        actions.add(finesButton);
        actions.add(reportsButton);
        actions.add(logoutButton);

        frame.add(new JLabel("Connecte : " + user.getFullName() + " (" + user.getRole() + ")"), BorderLayout.NORTH);
        frame.add(actions, BorderLayout.SOUTH);
        frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void loadBooks(JTextArea outputArea) {
        try {
            StringBuilder builder = new StringBuilder("Catalogue des livres\n\n");
            applicationContext.getBookService().listBooks().forEach(book ->
                    builder.append("#").append(book.getBookId())
                            .append(" | ").append(book.getTitle())
                            .append(" | ISBN: ").append(book.getIsbn())
                            .append(" | Actif: ").append(book.isActive())
                            .append(System.lineSeparator()));
            outputArea.setText(builder.toString());
        } catch (SQLException exception) {
            outputArea.setText(exception.getMessage());
        }
    }

    private void loadMembers(JTextArea outputArea) {
        try {
            StringBuilder builder = new StringBuilder("Adherents\n\n");
            applicationContext.getMemberService().listMembers().forEach(member ->
                    builder.append("#").append(member.getId())
                            .append(" | ").append(member.getFullName())
                            .append(" | ").append(member.getMembershipNumber())
                            .append(" | peut emprunter: ").append(member.canBorrow())
                            .append(System.lineSeparator()));
            outputArea.setText(builder.toString());
        } catch (SQLException exception) {
            outputArea.setText(exception.getMessage());
        }
    }

    private void loadLoans(JTextArea outputArea) {
        try {
            StringBuilder builder = new StringBuilder("Emprunts actifs\n\n");
            applicationContext.getLoanService().listActiveLoans().forEach(loan ->
                    builder.append("#").append(loan.getLoanId())
                            .append(" | ").append(loan.getBookCopy().getBook().getTitle())
                            .append(" | ").append(loan.getMember().getFullName())
                            .append(" | echeance ").append(loan.getDueDate())
                            .append(System.lineSeparator()));
            outputArea.setText(builder.toString());
        } catch (SQLException exception) {
            outputArea.setText(exception.getMessage());
        }
    }

    private void loadFines(JTextArea outputArea, User user) {
        try {
            StringBuilder builder = new StringBuilder("Penalites\n\n");
            if (user.getRole() == Role.MEMBER) {
                applicationContext.getFineService().listMemberFines(user.getId()).forEach(fine ->
                        builder.append("#").append(fine.getFineId())
                                .append(" | ").append(fine.getAmount())
                                .append(" | ").append(fine.getStatus())
                                .append(System.lineSeparator()));
            } else {
                applicationContext.getFineService().listUnpaidFines().forEach(fine ->
                        builder.append("#").append(fine.getFineId())
                                .append(" | ").append(fine.getAmount())
                                .append(" | ").append(fine.getStatus())
                                .append(System.lineSeparator()));
            }
            outputArea.setText(builder.toString());
        } catch (SQLException exception) {
            outputArea.setText(exception.getMessage());
        }
    }

    private void loadReports(JTextArea outputArea, User user) {
        if (user.getRole() == Role.MEMBER) {
            outputArea.setText("Les rapports sont reserves au personnel.");
            return;
        }
        try {
            StringBuilder builder = new StringBuilder("Rapports\n\n");
            builder.append("Livres les plus empruntes\n");
            applicationContext.getReportService().mostBorrowedBooksReport()
                    .forEach(line -> builder.append("- ").append(line).append(System.lineSeparator()));
            builder.append(System.lineSeparator()).append("Penalites impayees\n");
            applicationContext.getReportService().unpaidFinesReport()
                    .forEach(line -> builder.append("- ").append(line).append(System.lineSeparator()));
            outputArea.setText(builder.toString());
        } catch (SQLException exception) {
            outputArea.setText(exception.getMessage());
        }
    }
}
