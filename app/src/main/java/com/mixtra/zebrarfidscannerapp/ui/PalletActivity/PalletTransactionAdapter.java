package com.mixtra.zebrarfidscannerapp.ui.PalletActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionListByCodeResponse;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionListResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PalletTransactionAdapter extends RecyclerView.Adapter<PalletTransactionAdapter.ViewHolder> {

    private List<PalletTransactionListByCodeResponse.PalletTransactionItem> transactions;
    private OnTransactionActionListener listener;

    public interface OnTransactionActionListener {
        void onTransactionClicked(PalletTransactionListByCodeResponse.PalletTransactionItem transaction);
    }

    public PalletTransactionAdapter(OnTransactionActionListener listener) {
        this.transactions = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pallet_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PalletTransactionListByCodeResponse.PalletTransactionItem transaction = transactions.get(position);
        
        // Format and set date/time (2024-01-15 • 14:30)
        String formattedDateTime = formatDateTime(transaction.getTransactionDate());
        holder.tvDateTime.setText(formattedDateTime);
        
        // Set pallet code (from nested pallet object)
        String palletCode = transaction.getPalletCode();
        holder.tvPalletCode.setText(palletCode != null ? palletCode : " N/A");
        
        // Set status with appropriate styling
//        String status = mapTransactionStatus(transaction.getTransactionType());
//        holder.tvStatus.setText(status);
//        updateStatusBadge(holder.tvStatus, status);

        String type = transaction.getType();
        holder.tvItemType.setText(type);
        updateStatusBadge(holder.tvItemType, type);
        
        // Set item count (placeholder - you may need to get this from transaction details)
        int detailCount = transaction.getDetailCount();
        holder.tvItemCount.setText(detailCount == 1 ? "1 item" : detailCount + " items");
//

        // Set click listener for the entire item (since we removed edit/delete buttons)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClicked(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<PalletTransactionListByCodeResponse.PalletTransactionItem> newTransactions) {
        this.transactions.clear();
        if (newTransactions != null) {
            this.transactions.addAll(newTransactions);
        }
        notifyDataSetChanged();
    }

    public void addTransactions(List<PalletTransactionListByCodeResponse.PalletTransactionItem> newTransactions) {
        if (newTransactions != null) {
            int startPosition = this.transactions.size();
            this.transactions.addAll(newTransactions);
            notifyItemRangeInserted(startPosition, newTransactions.size());
        }
    }

    private String formatDateTime(String dateString) {
        try {
            // Try parsing with different formats
            SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd • HH:mm", Locale.getDefault());
            
            Date date = null;
            try {
                date = inputFormat1.parse(dateString);
            } catch (ParseException e) {
                date = inputFormat2.parse(dateString);
            }
            
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Return original string if parsing fails
        }
    }
    
    private String mapTransactionStatus(String transactionType) {
        // Map transaction type to status display
        if (transactionType == null) return "Menunggu";
        
        switch (transactionType.toUpperCase()) {
            case "COMPLETED":
            case "SELESAI":
            case "DONE":
                return "Selesai";
            case "PENDING":
            case "WAITING":
            case "DRAFT":
                return "Menunggu";
            case "IN_PROGRESS":
            case "PROCESSING":
                return "Proses";
            default:
                return "Menunggu";
        }
    }
    
    private void updateStatusBadge(TextView statusView, String status) {
        // Update badge color based on status
        int colorRes;
        switch (status) {
            case "Selesai":
                colorRes = R.color.green;
                break;
            case "Proses":
                colorRes = R.color.blue;
                break;
            case "Menunggu":
            default:
                colorRes = R.color.yellow;
                break;
        }
        
        // Get color from resources and set background
        int color = statusView.getContext().getResources().getColor(colorRes, null);
        statusView.setBackgroundColor(color);
    }

    private void updateTypeBadge(TextView typeView, String type) {
        // Update badge color based on status
        int colorRes;
        switch (type) {
            case "IN":
                colorRes = R.color.green;
                break;
            case "OUT":
                colorRes = R.color.blue;
                break;
            case "GATE OUT":
            default:
                colorRes = R.color.yellow;
                break;
        }

        // Get color from resources and set background
        int color = typeView.getContext().getResources().getColor(colorRes, null);
        typeView.setBackgroundColor(color);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateTime;
        TextView tvStatus;
        TextView tvPalletCode;
        TextView tvItemCount;
        TextView tvItemType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
//            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPalletCode = itemView.findViewById(R.id.tvPalletCode);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvItemType = itemView.findViewById(R.id.tvType);
        }
    }
}