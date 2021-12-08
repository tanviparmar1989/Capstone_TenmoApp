package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Transfers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {

    @Autowired
    private TransferDao transferDao;

    @RequestMapping(path = "transfer", method = RequestMethod.POST)
    public String sendTransferRequest(@RequestBody Transfers transfer){
        String result = transferDao.sendTransfer(transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
        return result;
    }


}
