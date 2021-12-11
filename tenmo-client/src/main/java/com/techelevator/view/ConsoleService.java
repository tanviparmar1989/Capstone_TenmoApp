package com.techelevator.view;


import com.techelevator.tenmo.model.User;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleService {

    private PrintWriter out;
    private Scanner in;

    public ConsoleService(InputStream input, OutputStream output) {
        this.out = new PrintWriter(output, true);
        this.in = new Scanner(input);
    }

    public Object getChoiceFromOptions(Object[] options) {
        Object choice = null;
        while (choice == null) {
            displayMenuOptions(options);
            choice = getChoiceFromUserInput(options);
        }
        out.println();
        return choice;
    }

    private Object getChoiceFromUserInput(Object[] options) {
        Object choice = null;
        String userInput = in.nextLine();
        try {
            int selectedOption = Integer.valueOf(userInput);
            if (selectedOption > 0 && selectedOption <= options.length) {
                choice = options[selectedOption - 1];
            }
        } catch (NumberFormatException e) {
            // eat the exception, an error message will be displayed below since choice will be null
        }
        if (choice == null) {
            out.println(System.lineSeparator() + "*** " + userInput + " is not a valid option ***" + System.lineSeparator());
        }
        return choice;
    }

    private void displayMenuOptions(Object[] options) {
        out.println();
        for (int i = 0; i < options.length; i++) {
            int optionNum = i + 1;
            out.println(optionNum + ") " + options[i]);
        }
        out.print(System.lineSeparator() + "Please choose an option >>> ");
        out.flush();
    }

    public String getUserInput(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        return in.nextLine();
    }

    public Integer getUserInputInteger(String prompt) {
        Integer result = null;
        do {
            out.print(prompt + ": ");
            out.flush();
            String userInput = in.nextLine();
            try {
                result = Integer.parseInt(userInput);
            } catch (NumberFormatException e) {
                out.println(System.lineSeparator() + "*** " + userInput + " is not valid ***" + System.lineSeparator());
            }
        } while (result == null);
        return result;
    }

    public void printTransfers(int transferId, String fromOrTo, BigDecimal amount) {
        out.println(transferId + "     " + fromOrTo + "          " + "$ " + amount);
    }

    public void printUsers(User[] users) {
        for(User user: users) {
            out.println(user.getId() + "          " + user.getUsername());
        }
        out.println("---------");
        out.flush();
    }

    public void printTransferDetails(int id, String from, String to, String type, String status, BigDecimal amount) {
        out.println("-------------------------------");
        out.println("Transfer Details");
        out.println("-------------------------------");
        out.println("Id: " + id);
        out.println("From: " + from);
        out.println("To: " + to);
        out.println("Type: " + type);
        out.println("Status: " + status);
        out.println("Amount: $" + amount);
    }
    public void printApproveOrRejectOptions() {
        out.println("1: Approve");
        out.println("2: Reject");
        out.println("0: Don't approve or reject\n");
    }
}
