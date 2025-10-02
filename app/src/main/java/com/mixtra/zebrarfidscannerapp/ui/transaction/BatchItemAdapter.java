package com.mixtra.zebrarfidscannerapp.ui.transaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.model.PalletTransactionResponse;

import java.util.List;

public class BatchItemAdapter extends RecyclerView.Adapter<BatchItemAdapter.ViewHolder> {
    
    private List<PalletTransactionResponse.BatchItem> batchItems;
    private OnItemRemoveListener onItemRemoveListener;
    
    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    public BatchItemAdapter(List<PalletTransactionResponse.BatchItem> batchItems) {
        this.batchItems = batchItems;
    }
    
    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.onItemRemoveListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_batch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PalletTransactionResponse.BatchItem item = batchItems.get(position);
        
        holder.tvItemTitle.setText("Item #" + (position + 1));
        
        // Clear any existing listeners to prevent conflicts
        holder.etBatchNo.removeTextChangedListener(holder.batchNoWatcher);
        holder.etQuantity.removeTextChangedListener(holder.quantityWatcher);
        
        // Set initial values
        holder.etBatchNo.setText(item.getBatchNo());
        holder.etQuantity.setText(String.valueOf(item.getQuantity()));
        
        // Set up text watchers to update the data model
        holder.batchNoWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                item.setBatchNo(s.toString());
            }
        };
        
        holder.quantityWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String quantityStr = s.toString().trim();
                    if (quantityStr.isEmpty()) {
                        item.setQuantity(0);
                    } else {
                        int quantity = Integer.parseInt(quantityStr);
                        item.setQuantity(quantity);
                    }
                } catch (NumberFormatException e) {
                    item.setQuantity(0);
                }
            }
        };
        
        // Add the watchers
        holder.etBatchNo.addTextChangedListener(holder.batchNoWatcher);
        holder.etQuantity.addTextChangedListener(holder.quantityWatcher);
        
        // Set up remove button click listener
        holder.btnRemoveItem.setOnClickListener(v -> {
            if (onItemRemoveListener != null) {
                onItemRemoveListener.onItemRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return batchItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemTitle;
        TextInputEditText etBatchNo;
        TextInputEditText etQuantity;
        ImageButton btnRemoveItem;
        TextWatcher batchNoWatcher;
        TextWatcher quantityWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemTitle = itemView.findViewById(R.id.tv_item_title);
            etBatchNo = itemView.findViewById(R.id.et_batch_no);
            etQuantity = itemView.findViewById(R.id.et_quantity);
            btnRemoveItem = itemView.findViewById(R.id.btn_remove_item);
        }
    }
}