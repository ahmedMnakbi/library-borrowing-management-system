package com.library.dao;

import com.library.model.Staff;
import com.library.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffDAO implements GenericDAO<Staff, Integer> {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public Optional<Staff> findById(Integer id) throws SQLException {
        Optional<User> user = userDAO.findById(id);
        if (user.isPresent() && user.get() instanceof Staff staff) {
            return Optional.of(staff);
        }
        return Optional.empty();
    }

    @Override
    public List<Staff> findAll() throws SQLException {
        List<Staff> staffMembers = new ArrayList<>();
        for (User user : userDAO.findAll()) {
            if (user instanceof Staff staff) {
                staffMembers.add(staff);
            }
        }
        return staffMembers;
    }

    @Override
    public Staff save(Staff entity) throws SQLException {
        return (Staff) userDAO.save(entity);
    }

    @Override
    public void update(Staff entity) throws SQLException {
        userDAO.update(entity);
    }

    @Override
    public void delete(Integer id) throws SQLException {
        userDAO.delete(id);
    }
}
