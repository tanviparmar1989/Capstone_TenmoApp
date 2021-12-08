package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Scanner;

public class TransferService {
    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;

    public TransferService(String url, AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
        BASE_URL = url;
    }

    public void sendBucks() {
        User[] users = null;

        // created a model of Transfers in tenmo-client side.
        Transfers transfer = new Transfers();
        try {
            Scanner scanner = new Scanner(System.in);
            users = restTemplate.exchange(BASE_URL + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Users\r\n" +
                    "ID\t\tName\r\n" +
                    "-------------------------------------------");

            // iterating to print the list of user with its Id.
            for (User i : users) {
                if (i.getId() != currentUser.getUser().getId()) {
                    System.out.println(i.getId() + "\t\t" + i.getUsername());
                }
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Enter ID of user you are sending to (0 to cancel): ");

            // taking Id of receiver (userTo)
            transfer.setAccountTo(Integer.parseInt(scanner.nextLine()));
            // taking Id of sender i.e current user
            transfer.setAccountFrom(currentUser.getUser().getId());

            // if transfer amount of receiver is not zero, then enter the amount.
            // transfer.getAccountTo == transfer money
            if (transfer.getAccountTo() != 0) {
                System.out.print("Enter amount: ");
                try {
                    // taking the money from  (Enter amount:), and converting to Big Decimal
                    transfer.setAmount(new BigDecimal(Double.parseDouble(scanner.nextLine())));
                } catch (NumberFormatException e) {
                    System.out.println("Error when entering amount");
                }
                String output = restTemplate.exchange(BASE_URL + "transfer", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Bad input.");
        }
    }

    private HttpEntity<Transfers> makeTransferEntity(Transfers transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Transfers> entity = new HttpEntity<>(transfer, headers);
        return entity;
    }

    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }

    public User[] getUsers() {
        User[] user = null;
        try {
            user = restTemplate.exchange(BASE_URL + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            for (User i : user) {
                System.out.println(i);
            }
        } catch (RestClientResponseException e) {
            System.out.println("Error getting users");
        }
        return user;
    }
}

