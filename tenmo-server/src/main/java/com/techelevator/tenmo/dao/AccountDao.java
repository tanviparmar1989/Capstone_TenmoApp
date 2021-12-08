package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal getBalance(int userId);

    BigDecimal addBalance(BigDecimal amountToAdd, int Id);

    BigDecimal subtractBalance(BigDecimal amountToSubtract, int Id);

    //for sendTransfer method
    Account findAccountById(int Id);

    Account findUserById(int userId);


}
