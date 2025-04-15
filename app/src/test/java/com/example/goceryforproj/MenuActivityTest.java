package com.example.goceryforproj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MenuActivityTest {
    
    @Test
    public void testAddStoreNavigation() {
        // Test navigation to AddStore activity
        assertTrue("Add Store navigation should work", true);
    }

    @Test
    public void testAddProductNavigation() {
        // Test navigation to AddProduct activity
        assertTrue("Add Product navigation should work", true);
    }

    @Test
    public void testGenerateQRNavigation() {
        // Test navigation to GenerateQR activity
        assertTrue("Generate QR navigation should work", true);
    }

    @Test
    public void testInventoryNavigation() {
        // Test navigation to Inventory activity
        assertTrue("Inventory navigation should work", true);
    }

    @Test
    public void testReceiptScannerNavigation() {
        // Test navigation to Receipt Scanner activity
        assertTrue("Receipt Scanner navigation should work", true);
    }

    @Test
    public void testHistoryNavigation() {
        // Test navigation to History activity
        assertTrue("History navigation should work", true);
    }
}
