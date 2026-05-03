package com.library.app;

import com.library.service.AuthService;
import com.library.service.BookCopyService;
import com.library.service.BookService;
import com.library.service.FineService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import com.library.service.ReportService;
import com.library.service.ReservationService;
import com.library.service.UserService;

public class ApplicationContext {
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();
    private final MemberService memberService = new MemberService();
    private final BookService bookService = new BookService();
    private final BookCopyService bookCopyService = new BookCopyService();
    private final LoanService loanService = new LoanService();
    private final FineService fineService = new FineService();
    private final ReservationService reservationService = new ReservationService();
    private final ReportService reportService = new ReportService();

    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }

    public MemberService getMemberService() {
        return memberService;
    }

    public BookService getBookService() {
        return bookService;
    }

    public BookCopyService getBookCopyService() {
        return bookCopyService;
    }

    public LoanService getLoanService() {
        return loanService;
    }

    public FineService getFineService() {
        return fineService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public ReportService getReportService() {
        return reportService;
    }
}
