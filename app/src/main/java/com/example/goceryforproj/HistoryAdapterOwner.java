package com.example.goceryforproj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class HistoryAdapterOwner extends ArrayAdapter<HistoryItem> {

    private Context context;
    private List<HistoryItem> historyList;
    private FirebaseFirestore db;

    public HistoryAdapterOwner(Context context, List<HistoryItem> historyList) {
        super(context, 0, historyList);
        this.context = context;
        this.historyList = historyList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        }

        // Bind Views
        TextView storeNameText = convertView.findViewById(R.id.store_name_text);
        TextView dateText = convertView.findViewById(R.id.purchase_date);
        TextView totalPriceText = convertView.findViewById(R.id.total_price);
        Button deleteButton = convertView.findViewById(R.id.delete_button);

        // Get current item
        HistoryItem item = historyList.get(position);

        // Set Data
        storeNameText.setText("Customer: " + item.getStoreName());
        dateText.setText("Date: " + item.getDate());
        totalPriceText.setText("Total: $" + item.getTotalCost());

        // Delete Button
        deleteButton.setOnClickListener(v -> deleteItem(item, position));

        return convertView;
    }

    private void deleteItem(HistoryItem item, int position) {
        db.collection("reports").document(item.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Deleted: " + item.getStoreName(), Toast.LENGTH_SHORT).show();
                    historyList.remove(position);
                    notifyDataSetChanged(); // Refresh ListView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show();
                });
    }
}
