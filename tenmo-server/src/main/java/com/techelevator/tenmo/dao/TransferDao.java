package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

public interface TransferDao {

    public String sendTransfer(int userFrom, int userTo, BigDecimal amount);
}
