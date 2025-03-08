package com.example.goceryforproj;

public class HistoryItem {
    private String storeName;
    private String date;
    private double totalCost;
    private String documentId; // Needed for deletion

    public HistoryItem(String storeName, String date, double totalCost, String documentId) {
        this.storeName = storeName;
        this.date = date;
        this.totalCost = totalCost;
        this.documentId = documentId;
    }

    public String getStoreName() { return storeName; }
    public String getDate() { return date; }
    public double getTotalCost() { return totalCost; }
    public String getDocumentId() { return documentId; }
}
