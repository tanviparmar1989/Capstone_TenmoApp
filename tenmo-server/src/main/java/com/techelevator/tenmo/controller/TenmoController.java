package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.exceptions.Insufficientfunds;
import com.techelevator.tenmo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TenmoController {

    @Autowired
    AccountDao accountDAO;
    @Autowired
    UserDao userDao;
    @Autowired
    TransferDao transferDAO;
    @Autowired
    TransferTypeDao transferTypeDao;
    @Autowired
    TransferStatusDao transferStatusDao;


    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public Balance getBalance(Principal principal) {
        System.out.println(principal.getName());
        return accountDAO.getBalance(principal.getName());
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> getUsers() {
        return userDao.findAll();
    }

    //request bucks
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.POST)
    public void addTransfer(@RequestBody Transfer transfer, @PathVariable int id) throws Insufficientfunds {

        BigDecimal amountToTransfer = transfer.getAmount();
        Account accountFrom = accountDAO.getAccountByAccountID(transfer.getAccountFrom());
        Account accountTo = accountDAO.getAccountByAccountID(transfer.getAccountTo());

        accountFrom.getBalance().sendMoney(amountToTransfer);
        accountTo.getBalance().receiveMoney(amountToTransfer);

        transferDAO.createTransfer(transfer);

        accountDAO.updateAccount(accountFrom);
        accountDAO.updateAccount(accountTo);
    }

    @RequestMapping(path = "/transfertype/filter", method = RequestMethod.GET)
    public TransferType getTransferTypeFromDescription(@RequestParam String description) {
        return transferTypeDao.getTransferTypeFromDescription(description);
    }

    @RequestMapping(path = "/transferstatus/filter", method = RequestMethod.GET)
    public TransferStatus getTransferStatusByDescription(@RequestParam String description) {
        return transferStatusDao.getTransferStatusByDesc(description);
    }

    @RequestMapping(path = "/account/user/{id}", method = RequestMethod.GET)
    public Account getAccountByUserId(@PathVariable int id) {
        return accountDAO.getAccountByUserID(id);
    }

    @RequestMapping(path = "/account/{id}", method = RequestMethod.GET)
    public Account getAccountFromAccountId(@PathVariable int id) {
        return accountDAO.getAccountByAccountID(id);
    }

    @RequestMapping(path = "/transfers/user/{userId}", method = RequestMethod.GET)
    public List<Transfer> getTransfersByUserId(@PathVariable int userId) {
        return transferDAO.getTransfersByUserId(userId);
    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int id) {
        return transferDAO.getTransferByTransferId(id);
    }

    @RequestMapping(path = "/users/{id}", method = RequestMethod.GET)
    public User getUserByUserId(@PathVariable int id) {
        return userDao.getUserByUserId(id);
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> getAllTransfers() {
        return transferDAO.getAllTransfers();
    }

    @RequestMapping(path = "/transfertype/{id}", method = RequestMethod.GET)
    public TransferType getTransferDescFromId(@PathVariable int id) {
        return transferTypeDao.getTransferTypeFromId(id);
    }

    @RequestMapping(path = "/transferstatus/{id}", method = RequestMethod.GET)
    public TransferStatus getTransferStatusFromId(@PathVariable int id) {
        return transferStatusDao.getTransferStatusById(id);
    }

    @RequestMapping(path = "/transfers/user/{userId}/pending", method = RequestMethod.GET)
    public List<Transfer> getPendingTransfersByUserId(@PathVariable int userId) {
        return transferDAO.getPendingTransfers(userId);
    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.PUT)
    public void updateTransferStatus(@RequestBody Transfer transfer, @PathVariable int id) throws Insufficientfunds {

        // only go through with the transfer if it is approved
        if (transfer.getTransferStatusId() == transferStatusDao.getTransferStatusByDesc("Approved").getTransferStatusId()) {

            BigDecimal amountToTransfer = transfer.getAmount();
            Account accountFrom = accountDAO.getAccountByAccountID(transfer.getAccountFrom());
            Account accountTo = accountDAO.getAccountByAccountID(transfer.getAccountTo());

            accountFrom.getBalance().sendMoney(amountToTransfer);
            accountTo.getBalance().receiveMoney(amountToTransfer);

            transferDAO.updateTransfer(transfer);

            accountDAO.updateAccount(accountFrom);
            accountDAO.updateAccount(accountTo);
        } else {
            transferDAO.updateTransfer(transfer);
        }

    }

}
