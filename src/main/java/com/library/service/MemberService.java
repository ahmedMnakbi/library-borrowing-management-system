package com.library.service;

import com.library.dao.FineDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.exception.EntityNotFoundException;
import com.library.model.Fine;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.util.PasswordUtil;
import com.library.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MemberService {
    private final MemberDAO memberDAO;
    private final LoanDAO loanDAO;
    private final FineDAO fineDAO;

    public MemberService() {
        this(new MemberDAO(), new LoanDAO(), new FineDAO());
    }

    public MemberService(MemberDAO memberDAO, LoanDAO loanDAO, FineDAO fineDAO) {
        this.memberDAO = memberDAO;
        this.loanDAO = loanDAO;
        this.fineDAO = fineDAO;
    }

    public Member createMember(String username, String plainPassword, String firstName, String lastName, String email,
                               String phone, String membershipNumber, String address, LocalDate registrationDate,
                               int maxLoans) throws SQLException {
        Validator.requireNotBlank(username, "Le nom d'utilisateur est obligatoire.");
        Validator.requireNotBlank(plainPassword, "Le mot de passe est obligatoire.");
        Validator.requireNotBlank(firstName, "Le prenom est obligatoire.");
        Validator.requireNotBlank(lastName, "Le nom est obligatoire.");
        Validator.requireNotBlank(membershipNumber, "Le numero d'adhesion est obligatoire.");
        Validator.requireValidEmail(email, "L'email est invalide.");

        Member member = new Member();
        member.setUsername(username);
        member.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setPhone(phone);
        member.setActive(true);
        member.setMembershipNumber(membershipNumber);
        member.setAddress(address);
        member.setRegistrationDate(registrationDate != null ? registrationDate : LocalDate.now());
        member.setMaxLoans(maxLoans > 0 ? maxLoans : 3);
        return memberDAO.save(member);
    }

    public void updateMember(Member member) throws SQLException {
        Validator.requireNotBlank(member.getMembershipNumber(), "Le numero d'adhesion est obligatoire.");
        memberDAO.update(member);
    }

    public Member getMemberById(int memberId) throws SQLException {
        return memberDAO.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Adherent introuvable."));
    }

    public List<Member> listMembers() throws SQLException {
        return memberDAO.findAll();
    }

    public List<Member> searchMembers(String keyword) throws SQLException {
        return memberDAO.search(keyword);
    }

    public List<Loan> getLoanHistory(int memberId) throws SQLException {
        return loanDAO.findHistoryByMemberId(memberId);
    }

    public List<Fine> getFines(int memberId) throws SQLException {
        return fineDAO.findByMemberId(memberId);
    }

    public boolean canBorrow(int memberId) throws SQLException {
        Member member = getMemberById(memberId);
        memberDAO.enrichMember(member);
        return member.canBorrow();
    }

    public void deactivateMember(int memberId) throws SQLException {
        memberDAO.delete(memberId);
    }
}
