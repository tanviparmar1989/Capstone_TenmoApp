package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.User;

public interface UserService {
    User[] getAllUsers(AuthenticatedUser authenticatedUser);

    User getUserByUserId(AuthenticatedUser authenticatedUser, int id);
}
