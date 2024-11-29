package com.example.goceryforproj;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryProductAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> categories;
    private Map<String, List<GetProduct>> categoryProducts;
    private OnProductUpdateListener listener;

    public interface OnProductUpdateListener {
        void onQuantityChanged();
        void onProductRemoved(GetProduct product);
    }

    public CategoryProductAdapter(Context context, List<GetProduct> products, OnProductUpdateListener listener) {
        this.context = context;
        this.listener = listener;
        this.categories = new ArrayList<>();
        this.categoryProducts = new HashMap<>();

        // Group products by category
        for (GetProduct product : products) {
            String category = product.getCategory();
            if (!categories.contains(category)) {
                categories.add(category);
                categoryProducts.put(category, new ArrayList<>());
            }
            categoryProducts.get(category).add(product);
        }
    }

    @Override
    public int getGroupCount() {
        Log.d("CategoryAdapter", "Group count: " + categories.size());
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String category = categories.get(groupPosition);
        int count = categoryProducts.get(category).size();
        Log.d("CategoryAdapter", "Children count for " + category + ": " + count);
        return count;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryProducts.get(categories.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent, false);
        }

        TextView categoryText = convertView.findViewById(R.id.categoryText);
        String category = categories.get(groupPosition);
        categoryText.setText(category);
        Log.d("CategoryAdapter", "Setting group view for category: " + category);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_child, parent, false);
        }

        GetProduct product = (GetProduct) getChild(groupPosition, childPosition);
        Log.d("CategoryAdapter", "Setting child view for product: " + product.getProductName());

        TextView productNameText = convertView.findViewById(R.id.productNameText);
        TextView priceText = convertView.findViewById(R.id.priceText);
        TextView quantityText = convertView.findViewById(R.id.quantityText);
        Button decreaseButton = convertView.findViewById(R.id.decreaseButton);
        Button increaseButton = convertView.findViewById(R.id.increaseButton);
        Button removeButton = convertView.findViewById(R.id.removeButton);

        productNameText.setText(product.getProductName());
        priceText.setText(String.format("$%.2f", product.getPriceAsDouble()));
        quantityText.setText(String.valueOf(product.getSelectedQuantity()));

        decreaseButton.setOnClickListener(v -> {
            if (product.getSelectedQuantity() > 0) {
                product.setSelectedQuantity(product.getSelectedQuantity() - 1);
                quantityText.setText(String.valueOf(product.getSelectedQuantity()));
                listener.onQuantityChanged();
            }
        });

        increaseButton.setOnClickListener(v -> {
            if (product.getSelectedQuantity() < product.getInventoryCount()) {
                product.setSelectedQuantity(product.getSelectedQuantity() + 1);
                quantityText.setText(String.valueOf(product.getSelectedQuantity()));
                listener.onQuantityChanged();
            } else {
                Toast.makeText(context, "Cannot exceed available inventory", Toast.LENGTH_SHORT).show();
            }
        });

        removeButton.setOnClickListener(v -> {
            listener.onProductRemoved(product);
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void updateProducts(List<GetProduct> products) {
        categories.clear();
        categoryProducts.clear();

        Log.d("CategoryAdapter", "Updating with " + products.size() + " products");

        for (GetProduct product : products) {
            String category = product.getCategory();
            Log.d("CategoryAdapter", "Processing product: " + product.getProductName() + " with category: " + category);

            if (!categories.contains(category)) {
                categories.add(category);
                categoryProducts.put(category, new ArrayList<>());
            }
            categoryProducts.get(category).add(product);
        }
        notifyDataSetChanged();
    }
}