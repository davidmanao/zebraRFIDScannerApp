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
        
        // Format and set date/time
        String[] formattedDateTime = formatDateTimeAsSeparate(transaction.getTransactionDate());
        holder.tvDate.setText(formattedDateTime[0]);
        holder.tvTime.setText(formattedDateTime[1]);
        
        // Set pallet code
        String palletCode = transaction.getPalletCode();
        holder.tvPalletCode.setText(palletCode != null ? palletCode : "N/A");
        
        // Set pallet type (optional secondary text)
        if (holder.tvPalletType != null) {
            holder.tvPalletType.setText("Standard Pallet");
        }
        
        // Set item count with badge styling
        int detailCount = transaction.getDetailCount();
        holder.tvItemCount.setText(String.valueOf(detailCount));
        
        // Set status with badge styling
        String type = transaction.getType();
        holder.tvType.setText(type);
        updateStatusBadge(holder.tvType, type);

        // Set click listener for the entire item
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

    private String[] formatDateTimeAsSeparate(String dateString) {
        try {
            // Try parsing with different formats
            SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            Date date = null;
            try {
                date = inputFormat1.parse(dateString);
            } catch (ParseException e) {
                date = inputFormat2.parse(dateString);
            }
            
            return new String[]{dateFormat.format(date), timeFormat.format(date)};
        } catch (ParseException e) {
            return new String[]{dateString, ""}; // Return original string if parsing fails
        }
    }
    

    
    private void updateStatusBadge(TextView statusView, String status) {
        // Set appropriate background based on status
        int backgroundRes;
        switch (status != null ? status.toUpperCase() : "") {
            case "IN":
                backgroundRes = R.drawable.bg_status_in;
                break;
            case "OUT":
                backgroundRes = R.drawable.bg_status_out;
                break;
            case "GATE OUT":
                backgroundRes = R.drawable.bg_status_gate_out;
                break;
            default:
                backgroundRes = R.drawable.bg_status_badge;
                break;
        }
        
        statusView.setBackgroundResource(backgroundRes);
        statusView.setTextColor(statusView.getContext().getResources().getColor(android.R.color.white, null));
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvTime;
        TextView tvPalletCode;
        TextView tvPalletType;
        TextView tvItemCount;
        TextView tvType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPalletCode = itemView.findViewById(R.id.tvPalletCode);
            tvPalletType = itemView.findViewById(R.id.tvPalletType);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}