package com.techelevator.tenmo.exceptions;

public class Insufficientfunds extends Exception {
    public Insufficientfunds() {
        super("Insufficient balance");
    }
}
