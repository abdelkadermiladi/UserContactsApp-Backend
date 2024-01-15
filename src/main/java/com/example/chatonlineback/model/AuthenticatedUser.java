package com.example.chatonlineback.model;

public class AuthenticatedUser {

    private User authenticatedUser;

    //convert the authenticated user to User type:
    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }
}
