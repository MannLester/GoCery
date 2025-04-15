package com.example.goceryforproj;

import org.junit.Test;
import com.example.goceryforproj.utils.ValidationUtils;

import static org.junit.Assert.*;

public class MainActivityTest {
    
    @Test
    public void testValidateUserType() {
        assertTrue("Should accept 'Admin' as valid user type", 
                  ValidationUtils.validateUserType("Admin"));
        assertTrue("Should accept 'Customer' as valid user type", 
                  ValidationUtils.validateUserType("Customer"));
        assertFalse("Should reject invalid user type", 
                   ValidationUtils.validateUserType("Invalid"));
    }
    
    @Test
    public void testIsValidEmail() {
        assertTrue("Should accept valid email", 
                  ValidationUtils.isValidEmail("test@example.com"));
        assertTrue("Should accept email with numbers", 
                  ValidationUtils.isValidEmail("user123@domain.com"));
        assertFalse("Should reject invalid email", 
                   ValidationUtils.isValidEmail("invalid.email"));
        assertFalse("Should reject null email", 
                   ValidationUtils.isValidEmail(null));
    }
}
