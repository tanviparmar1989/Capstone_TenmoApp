package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class JdbcAccountDao implements AccountDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcAccountDao() {
    }

    @Override
    public BigDecimal getBalance(int userId) {
        String sqlString = "SELECT balance FROM accounts WHERE user_id = ?";
        SqlRowSet results = null;
        BigDecimal balance = null;
        try {
            results = jdbcTemplate.queryForRowSet(sqlString, userId);
            if (results.next()) {
                balance = results.getBigDecimal("balance");
            }
        } catch (DataAccessException e) {
            System.out.println("Error accessing data");
        }
        return balance;
    }

    @Override
    public BigDecimal addBalance(BigDecimal amountToAdd, int Id) {

        //selecting single account detail as per account_Id.
        Account account = findAccountById(Id);
        //adding the new amount
        BigDecimal newBalance = account.getBalance().add(amountToAdd);
        System.out.println(newBalance);
        String sqlString = "Update accounts set balance =?" +
                "Where user_Id = ?";
        try {
            jdbcTemplate.update(sqlString, newBalance, Id);
        } catch (DataAccessException e) {
            System.out.println("Error accessing data");
        }

        return account.getBalance();
    }

    @Override
    public BigDecimal subtractBalance(BigDecimal amountToSubtract, int Id) {
        Account account = findAccountById(Id);
        BigDecimal newBalance = account.getBalance().subtract(amountToSubtract);
        System.out.println(newBalance);
        String sqlString = "Update accounts set balance = ?" +
                "where user_id = ?";
        try {
            jdbcTemplate.update(sqlString, newBalance, Id);
        } catch (DataAccessException e) {
            System.out.println("Error accessing data");
        }
        return account.getBalance();
    }

    public Account findAccountById(int Id) {
        Account account = null;
        String sqlString = "Select * from accounts where account_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, Id);
        if (results.next()) {
            account = mapRowToAccount(results);
        }
        return account;
    }

    public Account findUserById(int userId) {
        Account account = null;
        String sqlString = "Select * from accounts where user_id =?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, userId);
            account = mapRowToAccount(results);
        } catch (DataAccessException e) {
            System.out.println("Error accessing Data");
        }
        return account;
    }

    public Account mapRowToAccount(SqlRowSet result) {
        Account account = new Account();
        account.setBalance(result.getBigDecimal("balance"));
        account.setAccountId(result.getInt("account_Id"));
        account.setUserId(result.getInt("user_Id"));
        return account;
    }
}
