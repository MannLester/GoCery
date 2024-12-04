package com.example.goceryforproj;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConsumerActivity extends AppCompatActivity implements CategoryProductAdapter.OnProductUpdateListener {
    private static final int SCANNER_REQUEST_CODE = 100;
    private FirebaseFirestore db;
    private ExpandableListView expandableListView;
    private TextView totalPriceText;
    private FloatingActionButton scanButton;
    private MaterialButton checkoutButton;
    private List<GetProduct> productList;
    private CategoryProductAdapter adapter;
    private double totalPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ConsumerActivity", "Starting onCreate");
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_consumer);
            Log.d("ConsumerActivity", "Layout set successfully");

            db = FirebaseFirestore.getInstance();
            Log.d("ConsumerActivity", "Firebase initialized");

            try {
                expandableListView = findViewById(R.id.expandableListView);
                Log.d("ConsumerActivity", "ExpandableListView found");

                totalPriceText = findViewById(R.id.totalPriceText);
                Log.d("ConsumerActivity", "TotalPriceText found");

                scanButton = findViewById(R.id.scanButton);
                Log.d("ConsumerActivity", "ScanButton found");

                checkoutButton = findViewById(R.id.checkoutButton);
                Log.d("ConsumerActivity", "CheckoutButton found");
            } catch (Exception e) {
                Log.e("ConsumerActivity", "Error finding views", e);
                throw e;
            }

            try {
                productList = new ArrayList<>();
                Log.d("ConsumerActivity", "ProductList initialized");

                adapter = new CategoryProductAdapter(this, productList, this);
                Log.d("ConsumerActivity", "Adapter created");

                expandableListView.setAdapter(adapter);
                Log.d("ConsumerActivity", "Adapter set to list view");
            } catch (Exception e) {
                Log.e("ConsumerActivity", "Error setting up adapter", e);
                throw e;
            }

            // Set up click listeners
            try {
                scanButton.setOnClickListener(v -> {
                    Intent intent = new Intent(ConsumerActivity.this, ScannerActivity.class);
                    startActivityForResult(intent, SCANNER_REQUEST_CODE);
                });
                Log.d("ConsumerActivity", "Scan button listener set");

                checkoutButton.setOnClickListener(v -> handleCheckout());
                Log.d("ConsumerActivity", "Checkout button listener set");
            } catch (Exception e) {
                Log.e("ConsumerActivity", "Error setting click listeners", e);
                throw e;
            }

            updateTotalPrice();
            Log.d("ConsumerActivity", "onCreate completed successfully");

        } catch (Exception e) {
            Log.e("ConsumerActivity", "Fatal error in onCreate", e);
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCANNER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("is_store_qr", false)) {
                // Handle store QR code
                String storeId = data.getStringExtra("store_id");
                fetchStoreProducts(storeId);
            } else {
                // Handle product QR code (existing code)
                String productId = data.getStringExtra("scanned_product_id");
                if (productId != null) {
                    fetchProductDetails(productId);
                }
            }
        }
    }

    private void fetchProductDetails(String productId) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        GetProduct product = new GetProduct();
                        product.setId(productId);
                        product.setProductName(documentSnapshot.getString("productName"));
                        product.setCategory(documentSnapshot.getString("category"));
                        product.setPrice(documentSnapshot.getString("price"));
                        product.setWeight(documentSnapshot.getString("weight"));
                        product.setSelectedQuantity(1);
                        
                        Log.d("ConsumerActivity", "Fetched product category: " + product.getCategory());

                        findProductInventory(product);
                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching product", Toast.LENGTH_SHORT).show());
    }

    private void findProductInventory(GetProduct product) {
        db.collection("stores")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<Object> products = (List<Object>) document.get("products");
                        if (products != null) {
                            for (Object obj : products) {
                                if (obj instanceof Map) {
                                    Map<String, Object> productMap = (Map<String, Object>) obj;
                                    if (product.getId().equals(productMap.get("productId"))) {
                                        Long inventoryCount = (Long) productMap.get("inventoryCount");
                                        product.setInventoryCount(inventoryCount.intValue());
                                        addProductToCart(product);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    Toast.makeText(this, "Product not found in any store", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error checking inventory", Toast.LENGTH_SHORT).show());
    }

    private void addProductToCart(GetProduct product) {
        for (GetProduct p : productList) {
            if (p.getId().equals(product.getId())) {
                Toast.makeText(this, "Product already in cart", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        productList.add(product);
        adapter.updateProducts(productList);
        
        // Automatically expand all groups after adding a product
        for(int i = 0; i < adapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }
        
        Log.d("ConsumerActivity", "Product added to cart: " + product.getProductName());
        updateTotalPrice();
    }

    @Override
    public void onQuantityChanged() {
        updateTotalPrice();
    }

    @Override
    public void onProductRemoved(GetProduct product) {
        productList.remove(product);
        adapter.updateProducts(productList);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        totalPrice = 0.0;
        for (GetProduct product : productList) {
            totalPrice += product.getPriceAsDouble() * product.getSelectedQuantity();
        }
        totalPriceText.setText(String.format("Total: $%.2f", totalPrice));
    }

    private void handleCheckout() {
        if (productList.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String buyerUid = currentUser.getUid(); // Get current user's UID

        List<String> productsBought = new ArrayList<>();
        List<Double> productsCost = new ArrayList<>();
        List<Integer> productsCount = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<String> productIds = new ArrayList<>();

        for (GetProduct product : productList) {
            if (product.getSelectedQuantity() > 0) {
                productsBought.add(product.getProductName());
                productsCost.add(product.getPriceAsDouble());
                productsCount.add(product.getSelectedQuantity());
                categories.add(product.getCategory());
                productIds.add(product.getId());
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Find the sellerId based on productIds
        db.collection("stores")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String sellerId = null;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<Map<String, Object>> products = (List<Map<String, Object>>) document.get("products");
                            if (products != null) {
                                for (Map<String, Object> product : products) {
                                    String productId = (String) product.get("productId");
                                    if (productIds.contains(productId)) {
                                        sellerId = document.getString("ownerId");
                                        break;
                                    }
                                }
                            }
                            if (sellerId != null) break;
                        }

                        if (sellerId == null) {
                            Toast.makeText(this, "Seller not found for the products", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Create report and navigate
                        Map<String, Object> report = new HashMap<>();
                        report.put("buyerId", buyerUid);
                        report.put("sellerId", sellerId);
                        report.put("date", new Date());
                        report.put("productsBought", productsBought);
                        report.put("productsCost", productsCost);
                        report.put("productsCount", productsCount);
                        report.put("categories", categories);
                        report.put("totalCost", totalPrice);
                        report.put("productIds", productIds);

                        String finalSellerId = sellerId;
                        db.collection("reports")
                                .add(report)
                                .addOnSuccessListener(documentReference -> {
                                    String documentId = documentReference.getId();
                                    Intent intent = new Intent(ConsumerActivity.this, ReceiptActivity.class); // Use explicit context
                                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a", Locale.getDefault());
                                    intent.putExtra("date", sdf.format(new Date()));
                                    intent.putExtra("sellerId", finalSellerId); // Ensure sellerId is not null
                                    intent.putExtra("total", totalPrice);
                                    intent.putStringArrayListExtra("products", new ArrayList<>(productsBought));
                                    intent.putStringArrayListExtra("categories", new ArrayList<>(categories));
                                    intent.putExtra("costs", productsCost.stream().mapToDouble(Double::doubleValue).toArray());
                                    intent.putExtra("counts", productsCount.stream().mapToInt(Integer::intValue).toArray());
                                    intent.putExtra("documentId", documentId);

                                    startActivity(intent);

                                    productList.clear();
                                    adapter.updateProducts(productList);
                                    updateTotalPrice();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error creating receipt", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Failed to fetch store data", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void fetchStoreProducts(String storeId) {
        // Clear existing cart
        productList.clear();
        adapter.updateProducts(productList);
        updateTotalPrice();

        // Fetch store details first
        db.collection("stores")
            .document(storeId)
            .get()
            .addOnSuccessListener(storeDoc -> {
                if (storeDoc.exists()) {
                    List<Map<String, Object>> storeProducts = (List<Map<String, Object>>) storeDoc.get("products");
                    if (storeProducts != null && !storeProducts.isEmpty()) {
                        // Show store name at the top
                        String storeName = storeDoc.getString("storeName");
                        Toast.makeText(this, "Viewing products from: " + storeName, Toast.LENGTH_LONG).show();
                        
                        // Fetch each product's details
                        for (Map<String, Object> productInfo : storeProducts) {
                            String productId = (String) productInfo.get("productId");
                            Long inventoryCount = (Long) productInfo.get("inventoryCount");
                            
                            db.collection("products")
                                .document(productId)
                                .get()
                                .addOnSuccessListener(productDoc -> {
                                    if (productDoc.exists()) {
                                        GetProduct product = new GetProduct();
                                        product.setId(productId);
                                        product.setProductName(productDoc.getString("productName"));
                                        product.setCategory(productDoc.getString("category"));
                                        product.setPrice(productDoc.getString("price"));
                                        product.setWeight(productDoc.getString("weight"));
                                        product.setInventoryCount(inventoryCount.intValue());
                                        product.setSelectedQuantity(1);
                                        
                                        productList.add(product);
                                        adapter.updateProducts(productList);
                                        
                                        // Expand all groups after adding products
                                        for(int i = 0; i < adapter.getGroupCount(); i++) {
                                            expandableListView.expandGroup(i);
                                        }
                                    }
                                });
                        }
                    } else {
                        Toast.makeText(this, "No products found in this store", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error fetching store products", Toast.LENGTH_SHORT).show());
    }

}