package com.library.dao;

import com.library.model.Member;
import com.library.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDAO implements GenericDAO<Member, Integer> {
    private final UserDAO userDAO = new UserDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final FineDAO fineDAO = new FineDAO();

    @Override
    public Optional<Member> findById(Integer id) throws SQLException {
        Optional<User> user = userDAO.findById(id);
        if (user.isPresent() && user.get() instanceof Member member) {
            enrichMember(member);
            return Optional.of(member);
        }
        return Optional.empty();
    }

    public Optional<Member> findByMembershipNumber(String membershipNumber) throws SQLException {
        for (Member member : findAll()) {
            if (membershipNumber.equalsIgnoreCase(member.getMembershipNumber())) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    public List<Member> search(String keyword) throws SQLException {
        List<Member> members = new ArrayList<>();
        for (User user : userDAO.search(keyword)) {
            if (user instanceof Member member) {
                enrichMember(member);
                members.add(member);
            }
        }
        return members;
    }

    @Override
    public List<Member> findAll() throws SQLException {
        List<Member> members = new ArrayList<>();
        for (User user : userDAO.findAll()) {
            if (user instanceof Member member) {
                enrichMember(member);
                members.add(member);
            }
        }
        return members;
    }

    @Override
    public Member save(Member entity) throws SQLException {
        Member saved = (Member) userDAO.save(entity);
        enrichMember(saved);
        return saved;
    }

    @Override
    public void update(Member entity) throws SQLException {
        userDAO.update(entity);
    }

    @Override
    public void delete(Integer id) throws SQLException {
        userDAO.delete(id);
    }

    public void enrichMember(Member member) throws SQLException {
        member.setActiveLoansCount(loanDAO.countActiveLoansByMember(member.getId()));
        member.setUnpaidFines(fineDAO.hasUnpaidFines(member.getId()));
    }
}
