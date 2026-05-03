package com.library.model;

import com.library.enums.FineStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Fine {
    private int fineId;
    private Loan loan;
    private BigDecimal amount;
    private String reason;
    private FineStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public Fine() {
    }

    public Fine(int fineId, Loan loan, BigDecimal amount, String reason, FineStatus status,
                LocalDateTime createdAt, LocalDateTime paidAt) {
        this.fineId = fineId;
        this.loan = loan;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public int getFineId() {
        return fineId;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public FineStatus getStatus() {
        return status;
    }

    public void setStatus(FineStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
