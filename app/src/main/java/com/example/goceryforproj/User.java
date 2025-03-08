package com.example.goceryforproj;

import java.util.List;

public class User {

    private String username;
    private String email;
    private List<String> ownedStores;
    private String profilePic;

    // Default constructor required for Firestore
    public User() {
        // Default constructor required for Firestore serialization
    }

    // Constructor with parameters to initialize the User object
    public User(String username, String email, List<String> ownedStores, String profilePic) {
        this.username = username;
        this.email = email;
        this.ownedStores = ownedStores;
        this.profilePic = profilePic;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getOwnedStores() {
        return ownedStores;
    }

    public void setOwnedStores(List<String> ownedStores) {
        this.ownedStores = ownedStores;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}