package com.techelevator.tenmo;

import com.techelevator.tenmo.exceptions.InvalidTransferIdChoice;
import com.techelevator.tenmo.exceptions.InvalidUserChoiceException;
import com.techelevator.tenmo.exceptions.UserNotFoundException;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import com.techelevator.view.ConsoleService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
    private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
    private static final String[] LOGIN_MENU_OPTIONS = {LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};
    private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
    private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
    private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
    private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
    private static final String[] MAIN_MENU_OPTIONS = {MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};
    private static int transferIdNumber;
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private AccountService accountService;
    private UserService userService;
    private TransferTypeService transferTypeService;
    private TransferStatusService transferStatusService;
    private TransferService transferService;


    public App(ConsoleService console, AuthenticationService authenticationService) {
        this.console = console;
        this.authenticationService = authenticationService;
        this.accountService = new RestAccountService(API_BASE_URL);
        this.userService = new RestUserService();
        this.transferTypeService = new RestTransferTypeService(API_BASE_URL);
        this.transferStatusService = new RestTransferStatusService(API_BASE_URL);
        this.transferService = new RestTransferService(API_BASE_URL);
    }

    public static void main(String[] args) {
        App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
        app.run();
    }

    public static void incrementTransferIdNumber() {
        transferIdNumber++;
    }


    public void run() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");

        registerAndLogin();
        mainMenu();
    }

    private void mainMenu() {
        while (true) {
            String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
            if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
                viewCurrentBalance();
            } else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
                viewTransferHistory();
            } else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
                viewPendingRequests();
            } else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
                sendBucks();
            } else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
                requestBucks();
            } else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else {
                // the only other option on the main menu is to exit
                exitProgram();
            }
        }
    }

    private void viewCurrentBalance() {
        Balance balance = accountService.getBalance(currentUser);
        System.out.println("Your current account balance is:  $" + balance.getBalance());

    }


    //javadoc
    private void viewTransferHistory() {
        Transfer[] transfers = transferService.getTransfersFromUserId(currentUser, currentUser.getUser().getId());
        System.out.println("-------------------------------");
        System.out.println("Transfers");
        System.out.println("ID     From/To          Amount");
        System.out.println("-------------------------------");

        int currentUserAccountId = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getAccountId();
        for (Transfer transfer : transfers) {
            printTransferStubDetails(currentUser, transfer);
        }

        int transferIdChoice = console.getUserInputInteger("\nPlease enter transfer ID to view details (0 to cancel)");
        Transfer transferChoice = validateTransferIdChoice(transferIdChoice, transfers, currentUser);
        if (transferChoice != null) {
            printTransferDetails(currentUser, transferChoice);
        }

    }

    private void viewPendingRequests() {
        Transfer[] transfers = transferService.getPendingTransfersByUserId(currentUser);
        if (transfers.length == 0) {
            console.getUserInput("\n You don't have any pending request, press Enter to continue.");
        }
        System.out.println("-------------------------------");
        System.out.println("Pending Transfers");
        System.out.println("ID          To          Amount");
        System.out.println("-------------------------------");

        for (Transfer transfer : transfers) {
            printTransferStubDetails(currentUser, transfer);
        }
        // TODO ask to view details
        int transferIdChoice = console.getUserInputInteger("\nPlease enter transfer ID to approve/reject (0 to cancel)");
        Transfer transferChoice = validateTransferIdChoice(transferIdChoice, transfers, currentUser);
        if (transferChoice != null) {
            approveOrReject(transferChoice, currentUser);
        }
    }

    private void sendBucks() {
        User[] users = userService.getAllUsers(currentUser);
        printUserOptions(currentUser, users);

        int userIdChoice = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
        if (validateUserChoice(userIdChoice, users, currentUser)) {
            String amountChoice = console.getUserInput("Enter amount");
            createTransfer(userIdChoice, amountChoice, "Send", "Approved");
        }
    }

    private void printUserOptions(AuthenticatedUser authenticatedUser, User[] users) {

        System.out.println("-------------------------------");
        System.out.println("Users");
        System.out.println("ID          Name");
        System.out.println("-------------------------------");

        // TODO: update this to not display current user
        console.printUsers(users);
    }

    private boolean validateUserChoice(int userIdChoice, User[] users, AuthenticatedUser currentUser) {
        if (userIdChoice != 0) {
            try {
                boolean validUserIdChoice = false;

                for (User user : users) {
                    if (userIdChoice == currentUser.getUser().getId()) {
                        throw new InvalidUserChoiceException();
                    }
                    if (user.getId() == userIdChoice) {
                        validUserIdChoice = true;
                        break;
                    }
                }
                if (validUserIdChoice == false) {
                    throw new UserNotFoundException();
                }
                return true;
            } catch (UserNotFoundException | InvalidUserChoiceException e) {
                System.out.println(e.getMessage());
            }
        }
        return false;
    }

    private Transfer createTransfer(int accountChoiceUserId, String amountString, String transferType, String status) {

        int transferTypeId = transferTypeService.getTransferType(currentUser, transferType).getTransferTypeId();
        int transferStatusId = transferStatusService.getTransferStatus(currentUser, status).getTransferStatusId();
        int accountToId;
        int accountFromId;
        if (transferType.equals("Send")) {
            accountToId = accountService.getAccountByUserId(currentUser, accountChoiceUserId).getAccountId();
            accountFromId = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getAccountId();
        } else {
            accountToId = accountService.getAccountByUserId(currentUser, currentUser.getUser().getId()).getAccountId();
            accountFromId = accountService.getAccountByUserId(currentUser, accountChoiceUserId).getAccountId();
        }

        BigDecimal amount = new BigDecimal(amountString);

        Transfer transfer = new Transfer();
        transfer.setAccountFrom(accountFromId);
        transfer.setAccountTo(accountToId);
        transfer.setAmount(amount);
        transfer.setTransferStatusId(transferStatusId);
        transfer.setTransferTypeId(transferTypeId);
        transfer.setTransferId(transferIdNumber);

        transferService.createTransfer(currentUser, transfer);
        // increment transferIdNumber so it is always unique
        App.incrementTransferIdNumber();
        return transfer;
    }


    private void requestBucks() {
        User[] users = userService.getAllUsers(currentUser);
        printUserOptions(currentUser, users);
        int userIdChoice = console.getUserInputInteger("Enter ID of user you are requesting from (0 to cancel)");
        if (validateUserChoice(userIdChoice, users, currentUser)) {
            String amountChoice = console.getUserInput("Enter amount");
            createTransfer(userIdChoice, amountChoice, "Request", "Pending");
        }

    }

    private void exitProgram() {
        System.exit(0);
    }

    private void registerAndLogin() {
        while (!isAuthenticated()) {
            String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
            if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
                register();
            } else {
                // the only other option on the login menu is to exit
                exitProgram();
            }
        }
    }

    private boolean isAuthenticated() {
        return currentUser != null;
    }

    private void register() {
        System.out.println("Please register a new user account");
        boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                authenticationService.register(credentials);
                isRegistered = true;
                System.out.println("Registration successful. You can now login.");
            } catch (AuthenticationServiceException e) {
                System.out.println("REGISTRATION ERROR: " + e.getMessage());
                System.out.println("Please attempt to register again.");
            }
        }
    }

    private void login() {
        System.out.println("Please log in");
        currentUser = null;
        while (currentUser == null) //will keep looping until user is logged in
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                currentUser = authenticationService.login(credentials);
            } catch (AuthenticationServiceException e) {
                System.out.println("LOGIN ERROR: " + e.getMessage());
                System.out.println("Please attempt to login again.");
            }
        }
        transferIdNumber = getHighestTransferIdNumber() + 1;
    }

    private UserCredentials collectUserCredentials() {
        String username = console.getUserInput("Username");
        String password = console.getUserInput("Password");
        return new UserCredentials(username, password);
    }

    private void printTransferStubDetails(AuthenticatedUser authenticatedUser, Transfer transfer) {
        String fromOrTo = "";
        int accountFrom = transfer.getAccountFrom();
        int accountTo = transfer.getAccountTo();
        if (accountService.getAccountById(currentUser, accountTo).getUserId() == authenticatedUser.getUser().getId()) {
            int accountFromUserId = accountService.getAccountById(currentUser, accountFrom).getUserId();
            String userFromName = userService.getUserByUserId(currentUser, accountFromUserId).getUsername();
            fromOrTo = "From: " + userFromName;
        } else {
            int accountToUserId = accountService.getAccountById(currentUser, accountTo).getUserId();
            String userToName = userService.getUserByUserId(currentUser, accountToUserId).getUsername();
            fromOrTo = "To: " + userToName;
        }

        console.printTransfers(transfer.getTransferId(), fromOrTo, transfer.getAmount());
    }

    private void printTransferDetails(AuthenticatedUser currentUser, Transfer transferChoice) {
        int id = transferChoice.getTransferId();
        BigDecimal amount = transferChoice.getAmount();
        int fromAccount = transferChoice.getAccountFrom();
        int toAccount = transferChoice.getAccountTo();
        int transactionTypeId = transferChoice.getTransferTypeId();
        int transactionStatusId = transferChoice.getTransferStatusId();

        int fromUserId = accountService.getAccountById(currentUser, fromAccount).getUserId();
        String fromUserName = userService.getUserByUserId(currentUser, fromUserId).getUsername();
        int toUserId = accountService.getAccountById(currentUser, toAccount).getUserId();
        String toUserName = userService.getUserByUserId(currentUser, toUserId).getUsername();
        String transactionType = transferTypeService.getTransferTypeFromId(currentUser, transactionTypeId).getTransferTypeDescription();
        String transactionStatus = transferStatusService.getTransferStatusById(currentUser, transactionStatusId).getTransferStatusDesc();

        console.printTransferDetails(id, fromUserName, toUserName, transactionType, transactionStatus, amount);
    }

    private Transfer validateTransferIdChoice(int transferIdChoice, Transfer[] transfers, AuthenticatedUser currentUser) {
        Transfer transferChoice = null;
        if (transferIdChoice != 0) {
            try {
                boolean validTransferIdChoice = false;
                for (Transfer transfer : transfers) {
                    if (transfer.getTransferId() == transferIdChoice) {
                        validTransferIdChoice = true;
                        transferChoice = transfer;
                        break;
                    }
                }
                if (!validTransferIdChoice) {
                    throw new InvalidTransferIdChoice();
                }
            } catch (InvalidTransferIdChoice e) {
                System.out.println(e.getMessage());
            }
        }
        return transferChoice;
    }

    private void approveOrReject(Transfer pendingTransfer, AuthenticatedUser authenticatedUser) {
        // TODO: write method to approve or reject transfer
        console.printApproveOrRejectOptions();
        int choice = console.getUserInputInteger("Please choose an option");

        if (choice != 0) {
            if (choice == 1) {
                int transferStatusId = transferStatusService.getTransferStatus(currentUser, "Approved").getTransferStatusId();
                pendingTransfer.setTransferStatusId(transferStatusId);
            } else if (choice == 2) {
                int transferStatusId = transferStatusService.getTransferStatus(currentUser, "Rejected").getTransferStatusId();
                pendingTransfer.setTransferStatusId(transferStatusId);
            } else {
                System.out.println("Invalid choice.");
            }
            transferService.updateTransfer(currentUser, pendingTransfer);
        }

    }

    private int getHighestTransferIdNumber() {
        Transfer[] transfers = transferService.getAllTransfers(currentUser);
        int highestTransferIdNumber = 0;
        for (Transfer transfer : transfers) {
            if (transfer.getTransferId() > highestTransferIdNumber) {
                highestTransferIdNumber = transfer.getTransferId();
            }
        }
        return highestTransferIdNumber;
    }

}
