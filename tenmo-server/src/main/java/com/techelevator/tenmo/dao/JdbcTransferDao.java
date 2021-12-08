package com.techelevator.tenmo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import com.techelevator.tenmo.model.Transfers;

import java.math.BigDecimal;

@Component
// Spring ApplicationContext is where Spring holds instances of objects that it has identified to be managed and distributed automatically.
// These are called beans.Spring collects bean instances from our application and uses them at the appropriate time.
//@Component is an annotation that allows Spring to automatically detect our custom beans.
//In other words, without having to write any explicit code, Spring will:
//Scan our application for classes annotated with @Component
//Instantiate them and inject any specified dependencies into them
//Inject them wherever needed

public class JdbcTransferDao implements TransferDao {

    @Autowired
    //The @Autowired annotation provides more fine-grained control over where and how autowiring should be accomplished.
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountDao accountDao;

    //to complete the whole method of sendTransfer we have to add and subtract balance from account of sender and receiver
    // addBalance and Subtract balance is defined in Jdbc account Dao
    //
    @Override
    public String sendTransfer(int userFrom, int userTo, BigDecimal amount) {
        if (userFrom == userTo) {
            return "You cannot send money to yourself." + userFrom + userTo;
        }
        // amount = userFromAmount (sender),
        if (amount.compareTo(accountDao.getBalance(userFrom)) == -1 && amount.compareTo(new BigDecimal(0)) == 1) {
            // transfer_type_id = 2 --- Approved
            //transfer)status_id = 2 -- Approved
            String sql = "Insert into transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount)" +
                    "values (2,2,?,?,?)";
            jdbcTemplate.update(sql, userFrom, userTo, amount);

            //updating account balance of sender and receiver after sending money
            accountDao.addBalance(amount, userTo);
            accountDao.subtractBalance(amount, userFrom);
            return "Transfer Complete";
        } else {
           return "Transfer failed due to a lack of funds or amount was less then or equal to 0 or not a valid user";
        }
    }

    private Transfers mapRowToTransfer(SqlRowSet results) {
        Transfers transfer = new Transfers();
        transfer.setTransfer_id(results.getInt("transfer_id"));
        transfer.setTransfer_type_id(results.getInt("transfer_type_id"));
        transfer.setTransfer_status_id(results.getInt("transfer_status_id"));
        transfer.setAccount_from(results.getInt("account_From"));
        transfer.setAccount_to(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        try {
            transfer.setUserFrom(results.getString("userFrom"));
            transfer.setUserTo(results.getString("userTo"));
        } catch (Exception e) {}
        try {
            transfer.setTransferType(results.getString("transfer_type_desc"));
            transfer.setTransferStatus(results.getString("transfer_status_desc"));
        } catch (Exception e) {}
        return transfer;
    }

}
