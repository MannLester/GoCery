package com.example.goceryforproj.utils;

public class ValidationUtils {
    
    public static boolean validateUserType(String userType) {
        return "Admin".equalsIgnoreCase(userType) || "Customer".equalsIgnoreCase(userType);
    }
    
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
