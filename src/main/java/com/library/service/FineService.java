package com.library.service;

import com.library.dao.FineDAO;
import com.library.exception.EntityNotFoundException;
import com.library.model.Fine;

import java.sql.SQLException;
import java.util.List;

public class FineService {
    private final FineDAO fineDAO;

    public FineService() {
        this(new FineDAO());
    }

    public FineService(FineDAO fineDAO) {
        this.fineDAO = fineDAO;
    }

    public List<Fine> listAllFines() throws SQLException {
        return fineDAO.findAll();
    }

    public List<Fine> listMemberFines(int memberId) throws SQLException {
        return fineDAO.findByMemberId(memberId);
    }

    public List<Fine> listUnpaidFines() throws SQLException {
        return fineDAO.findUnpaidFines();
    }

    public Fine getFineById(int fineId) throws SQLException {
        return fineDAO.findById(fineId)
                .orElseThrow(() -> new EntityNotFoundException("Penalite introuvable."));
    }

    public void markPaid(int fineId) throws SQLException {
        getFineById(fineId);
        fineDAO.markPaid(fineId);
    }

    public void cancel(int fineId) throws SQLException {
        getFineById(fineId);
        fineDAO.cancel(fineId);
    }
}
